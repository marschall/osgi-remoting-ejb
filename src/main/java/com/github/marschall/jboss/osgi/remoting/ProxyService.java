package com.github.marschall.jboss.osgi.remoting;

import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

final class ProxyService implements BundleListener {
  
  private final ConcurrentMap<Bundle, BundleProxyContext> contexts;
  
  private final XMLInputFactory inputFactory;
  

  ProxyService() {
    this.contexts = new ConcurrentHashMap<>();
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
      this.parseServiceXml(serviceXml);
    }
  }
  
  private void parseServiceXml(URL serviceXml) {
    try (InputStream stream = serviceXml.openStream()) {
      XMLStreamReader reader = this.inputFactory.createXMLStreamReader(stream);
      try {
        
      } finally {
        // TODO CR
        reader.close();
      }
    } catch (IOException e) {
      
    }
    
  }

  void removePotentialBundle(Bundle bundle) {
    BundleProxyContext context = this.contexts.remove(bundle);
    if (context != null) {
      context.release();
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
  
  static final class BundleProxyContext {
    
    private final Collection<ServiceCaller> callers;

    BundleProxyContext(Collection<ServiceCaller> callers) {
      this.callers = callers;
    }
    
    void release() {
      for (ServiceCaller caller : callers) {
        caller.invalidate();
      }
    }
    
  }

}
