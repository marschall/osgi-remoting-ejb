package com.github.marschall.jboss.osgi.remoting;

import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;

final class ProxyService implements BundleListener {

  /**
   * The symbolic names of the bundles that have to be added to the
   * class loader of each client bundle. This contains the classes need
   * by jboss-remoting, not the classes need by the client bundle. Those
   * should be dealt with by the manifest of the client bundle. 
   */
  // mvn dependency:copy-dependencies -DoutputDirectory=lib
  // unzip -c jboss-transaction-api_1.1_spec-1.0.1.Final.jar META-INF/MANIFEST.MF
  private static final String[] PARENT_BUNDLE_IDS = {
    "org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec",
    "org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec",
    // missing SASL bundle
    // http://github.com/jboss/jboss-parent-pom/jboss-sasl
    // jboss-marshalling-river
    // jboss-marshalling
    // jboss-ejb-client
    // xnio-nio
    // xnio-api
    // http://github.com/jboss/jboss-parent-pom/xnio-all/xnio-api
  };

  private final ConcurrentMap<Bundle, BundleProxyContext> contexts;

  private final XMLInputFactory inputFactory;

  private final BundleContext bundleContext;

  private final Logger logger;

  private final ClassLoader parent;


  ProxyService(BundleContext bundleContext, Logger logger) {
    this.bundleContext = bundleContext;
    this.logger = logger;
    this.contexts = new ConcurrentHashMap<Bundle, BundleProxyContext>();
    this.inputFactory = this.createInputFactory();
    this.parent = new BundlesProxyClassLoader(this.lookUpParentBundles(bundleContext));
  }
  
  private Collection<Bundle> lookUpParentBundles(BundleContext bundleContext) {
    Set<String> symbolicNames = new HashSet<String>(Arrays.asList(PARENT_BUNDLE_IDS));
    Map<String, Bundle> found = new HashMap<String, Bundle>(symbolicNames.size());
    for (Bundle bundle : bundleContext.getBundles()) {
      String symbolicName = bundle.getSymbolicName();
      if (symbolicNames.contains(symbolicName)) {
        // TODO check version
        found.put(symbolicName, bundle);
      }
    }
    // TODO sort?
    return found.values();
  }

  private XMLInputFactory createInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    //disable various features that we don't need and just cost performance
    factory.setProperty(IS_VALIDATING, Boolean.FALSE);
    factory.setProperty(IS_NAMESPACE_AWARE, Boolean.FALSE);
    factory.setProperty(IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    factory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    factory.setProperty(SUPPORT_DTD, Boolean.FALSE);
    return factory;
  }

  void initialBundles(Bundle[] bundles) {
    for (Bundle bundle : bundles) {
      int bundleState = bundle.getState();
      if (bundleState == BundleEvent.STARTING || bundleState == BundleEvent.STARTED) {
        this.addPotentialBundle(bundle);
      }
    }
  }

  private String getResourceLocation(Bundle bundle) {
    // http://cxf.apache.org/distributed-osgi-reference.html
    Dictionary<String,String> headers = bundle.getHeaders();
    String remoteServiceHeader = headers.get("Remote-Service");
    if (remoteServiceHeader != null) {
      return remoteServiceHeader;
    } else {
      // TODO check
      return "OSGI-INF/remote-service";
    }
  }

  private List<URL> getServiceUrls(Bundle bundle) {
    String resourceLocation = this.getResourceLocation(bundle);
    Enumeration<URL> resources;
    try {
      resources = bundle.getResources(resourceLocation);
    } catch (IOException e) {
      this.logger.warning("failed to access location '" + resourceLocation + "' in bundle: " + bundle);
      return Collections.emptyList();
    }
    if (resources != null && resources.hasMoreElements()) {
      List<URL> serviceXmls = new ArrayList<URL>(1);
      while (resources.hasMoreElements()) {
        URL nextElement = resources.nextElement();
        if (nextElement.getFile().endsWith(".xml")) {
          serviceXmls.add(nextElement);
        }
      }
      return serviceXmls;
    } else {
      return Collections.emptyList();
    }

  }

  void addPotentialBundle(Bundle bundle) {
    List<URL> serviceUrls = this.getServiceUrls(bundle);
    if (!serviceUrls.isEmpty()) {
      List<ParseResult> results = new ArrayList<ParseResult>(serviceUrls.size());
      for (URL serviceXml : serviceUrls) {
        ParseResult result;
        try {
          result = this.parseServiceXml(serviceXml);
        } catch (IOException e) {
          this.logger.warning("could not parse XML: " + serviceXml + " in bundle:" + bundle + ", ignoring",  e);
          continue;
        } catch (XMLStreamException e) {
          this.logger.warning("could not parse XML: " + serviceXml + " in bundle:" + bundle + ", ignoring",  e);
          continue;
        }
        if (!result.isEmpty()) {
          results.add(result);
        }
      }
      if (results.isEmpty()) {
        return;
      }

      ParseResult result = ParseResult.flatten(results);
      this.registerServices(bundle, result);
    }
  }

  void registerServices(Bundle bundle, ParseResult result) {
    ClassLoader classLoader = createClassLoader(bundle);
    List<ServiceCaller> callers = new ArrayList<ServiceCaller>(result.size());
    List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>(result.size());
    Context namingContext;
    try {
      namingContext = this.createNamingContext();
    } catch (NamingException e) {
      // there isn't really anything anybody can do
      // but we shouldn't pump exception into the OSGi framework
      this.logger.warning("could not register bundle: " + bundle, e);
      return;
    }

    Thread currentThread = Thread.currentThread();
    ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
    // switch TCCL only once for all the look ups
    currentThread.setContextClassLoader(classLoader);
    try {
      for (ServiceInfo info : result.services) {
        Class<?> interfaceClazz;
        Object jBossProxy;
        try {
          interfaceClazz = classLoader.loadClass(info.interfaceName);
          jBossProxy = this.lookUpJBossProxy(interfaceClazz, info.jndiName, namingContext);
        } catch (ClassNotFoundException e) {
          this.logger.warning("failed to load interface class: " + info.interfaceName
              + ", remote service will not be available", e);
          continue;
        } catch (NamingException e) {
          this.logger.warning("failed to look up interface class: " + info.interfaceName
              + " with JNDI name: " + info.jndiName
              + ", remote service will not be available", e);
          continue;
        } catch (ClassCastException e) {
          this.logger.warning("failed to load interface class: " + info.interfaceName
              + ", remote service will not be available", e);
          continue;
        }
        ServiceCaller serviceCaller = new ServiceCaller(jBossProxy, classLoader, this.logger);
        Object service = Proxy.newProxyInstance(classLoader, new Class[]{interfaceClazz}, serviceCaller);
        callers.add(serviceCaller);
        // TODO properties
        // TODO connection name
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("service.imported", true);
        ServiceRegistration<?> serviceRegistration = this.bundleContext.registerService(info.interfaceName, service, properties);
        registrations.add(serviceRegistration);
      }
    } finally {
      currentThread.setContextClassLoader(oldContextClassLoader);
    }

    BundleProxyContext bundleProxyContext = new BundleProxyContext(namingContext, callers, registrations);
    // detect double registration is case of concurrent call by #bundleChanged and #initialBundles
    BundleProxyContext previous = this.contexts.putIfAbsent(bundle, bundleProxyContext);
    if (previous != null) {
      // undo registration
      bundleProxyContext.unregisterServices(this.bundleContext);
    }
  }

  ClassLoader createClassLoader(Bundle bundle) {
    return new BundleProxyClassLoader(bundle, this.parent);
  }

  private Object lookUpJBossProxy(Class<?> interfaceClazz, String jndiName, Context namingContext)
      throws NamingException, ClassCastException {
    // TODO needs to go to custom thread for stateful
    Object proxy = namingContext.lookup(jndiName);
    return interfaceClazz.cast(proxy);
  }

  private ParseResult parseServiceXml(URL serviceXml) throws IOException, XMLStreamException {
    InputStream stream = serviceXml.openStream();
    try {
      XMLStreamReader reader = this.inputFactory.createXMLStreamReader(stream);
      try {
        return this.parseSafe(reader);
      } finally {
        // TODO CR
        reader.close();
      }
    } finally {
      stream.close();
    }

  }
  
  private ParseResult parseSafe(XMLStreamReader reader) {
    
  }

  private Context createNamingContext() throws NamingException {
    Properties jndiProps = new Properties();
    jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
    // TODO configure
    jndiProps.put(Context.PROVIDER_URL,"remote://localhost:4447");
    // create a namingContext passing these properties
    return new InitialContext(jndiProps);
  }

  void removePotentialBundle(Bundle bundle) {
    BundleProxyContext context = this.contexts.remove(bundle);
    if (context != null) {
      try {
        context.release(bundleContext);
      } catch (NamingException e) {
        // there isn't really anything anybody can do
        // but we shouldn't pump exception into the OSGi framework
        this.logger.warning("could not unregister bundle: " + bundle, e);
      }
    }
  }

  @Override
  public void bundleChanged(BundleEvent event) {
    int eventType = event.getType();
    switch (eventType) {
    // TODO installed? uninstalled? started? resolved?
    case BundleEvent.STARTING:
      this.addPotentialBundle(event.getBundle());
      break;
    case BundleEvent.STOPPING: 
      this.removePotentialBundle(event.getBundle());
      break;

    }

  }

  void stop() {
    for (BundleProxyContext context : this.contexts.values()) {
      try {
        context.release(bundleContext);
      } catch (NamingException e) {
        // there isn't really anything anybody can do
        // but we shouldn't pump exception into the OSGi framework
        // and we should continue the loop
        this.logger.warning("could not unregister service", e);
      }
    }
  }

  static final class ParseResult {

    final List<ServiceInfo> services;

    ParseResult(List<ServiceInfo> services) {
      this.services = services;
    }

    boolean isEmpty() {
      return this.services.isEmpty();
    }

    int size() {
      return this.services.size();
    }

    static ParseResult flatten(List<ParseResult> results) {
      if (results.isEmpty()) {
        throw new IllegalArgumentException("collection must not be empty");
      }
      if (results.size() == 1) {
        return results.get(0);
      }

      int size = 0;
      for (ParseResult result : results) {
        size += result.size();
      }
      List<ServiceInfo> services = new ArrayList<ServiceInfo>(size);
      for (ParseResult result : results) {
        services.addAll(result.services);
      }
      return new ParseResult(services);
    }

  }

  static final class ServiceInfo {

    final String interfaceName;
    final String jndiName;

    ServiceInfo(String interfaceName, String jndiName) {
      this.interfaceName = interfaceName;
      this.jndiName = jndiName;
    }

  }

  static final class BundleProxyContext {

    private final Context namingContext;

    private final Collection<ServiceCaller> callers;

    private final Collection<ServiceRegistration<?>> registrations;

    BundleProxyContext(Context namingContext, Collection<ServiceCaller> callers, Collection<ServiceRegistration<?>> registrations) {
      this.namingContext = namingContext;
      this.callers = callers;
      this.registrations = registrations;
    }

    void release(BundleContext bundleContext) throws NamingException {
      this.unregisterServices(bundleContext);
      this.invalidateCallers();
      this.closeNamingConext();
    }

    private void closeNamingConext() throws NamingException {
      this.namingContext.close();
    }

    private void unregisterServices(BundleContext bundleContext) {
      for (ServiceRegistration<?> registration : this.registrations) {
        bundleContext.ungetService(registration.getReference());
      }
    }

    private void invalidateCallers() {
      for (ServiceCaller caller : callers) {
        caller.invalidate();
      }
    }

  }


}
