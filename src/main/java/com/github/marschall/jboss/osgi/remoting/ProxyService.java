package com.github.marschall.jboss.osgi.remoting;

import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;

final class ProxyService implements BundleListener {
  
  private final ConcurrentMap<Bundle, BundleProxyContext> contexts;
  
  private final XMLInputFactory inputFactory;

  private final BundleContext bundleContext;
  

  ProxyService(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
    this.contexts = new ConcurrentHashMap<Bundle, BundleProxyContext>();
    this.inputFactory = this.createInputFactory();
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
  
  void addPotentialBundle(Bundle bundle) {
    // TODO check
    URL serviceXml = bundle.getResource("OSGI-INF/services/service.xml");
    if (serviceXml != null) {
      ParseResult result = this.parseServiceXml(serviceXml);
      
      if (!result.isEmpty()) {
        // TODO parent = this parent?
        ClassLoader classLoader = new BundleProxyClassLoader(bundle);
        Collection<ServiceCaller> callers = new ArrayList<ServiceCaller>(result.size());
        Collection<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>(result.size());
        for (ServiceInfo info : result.services) {
          Class<?> interfaceClazz = classLoader.loadClass(info.interfaceName);
          Object jBossProxy = this.lookUpJBossProxy(interfaceClazz, info.jndiName);
          ServiceCaller serviceCaller = new ServiceCaller(jBossProxy, classLoader);
          callers.add(serviceCaller);
          // TODO properties
          Dictionary<String, Object> properties = new Hashtable<String, Object>();
          ServiceRegistration<?> serviceRegistration = this.bundleContext.registerService(info.interfaceName, serviceCaller, properties);
          registrations.add(serviceRegistration);
        }
        BundleProxyContext bundleProxyContext = new BundleProxyContext(callers, registrations);
        this.contexts.put(bundle, bundleProxyContext);
      }
    }
  }
  
  private Object lookUpJBossProxy(Class<?> interfaceClazz, String jndiName) {
    Object proxy = null;
    return interfaceClazz.cast(proxy);
  }
  
  private ParseResult parseServiceXml(URL serviceXml) {
    InputStream stream = serviceXml.openStream();
    try {
      XMLStreamReader reader = this.inputFactory.createXMLStreamReader(stream);
      try {

      } finally {
        // TODO CR
        reader.close();
      }
    } catch (IOException e) {

    } finally {
      stream.close();
    }

  }

  void removePotentialBundle(Bundle bundle) {
    BundleProxyContext context = this.contexts.remove(bundle);
    if (context != null) {
      context.invalidateCallers();
      context.unregisterServices(bundleContext);
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
    
    private final Collection<ServiceCaller> callers;
    
    private final Collection<ServiceRegistration<?>> registrations;

    BundleProxyContext(Collection<ServiceCaller> callers, Collection<ServiceRegistration<?>> registrations) {
      this.callers = callers;
      this.registrations = registrations;
    }
    
    void unregisterServices(BundleContext bundleContext) {
      for (ServiceRegistration<?> registration : this.registrations) {
        bundleContext.ungetService(registration.getReference());
      }
    }
    
    void invalidateCallers() {
      for (ServiceCaller caller : callers) {
        caller.invalidate();
      }
    }
    
  }

}
