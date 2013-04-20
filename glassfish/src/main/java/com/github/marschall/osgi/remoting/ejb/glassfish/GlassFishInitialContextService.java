package com.github.marschall.osgi.remoting.ejb.glassfish;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

// http://glassfish.java.net/javaee5/ejb/EJB_FAQ.html#StandaloneRemoteEJB
public class GlassFishInitialContextService implements InitialContextService {
  
  private static final String[] PARENT_BUNDLE_IDS = {
    "osgi-remoting-ejb-glassfish-client"
  };
  private static final Set<String> BUNDLE_IDS;
  
  static {
    Set<String> bundleIds = new HashSet<String>(PARENT_BUNDLE_IDS.length);
    for (String bundleId : PARENT_BUNDLE_IDS) {
      bundleIds.add(bundleId);
    }
    BUNDLE_IDS = Collections.unmodifiableSet(bundleIds);
  }

  @Override
  public Hashtable<?, ?> getEnvironment() {
    // use the no-args constructor
    return null;
  }
  
  private Properties getProperties() {
    Properties props = new Properties();

    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
    props.setProperty(Context.URL_PKG_PREFIXES, "com.sun.enterprise.naming");
    props.setProperty(Context.STATE_FACTORIES, "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

    // optional.  Defaults to localhost.  Only needed if web server is running 
    // on a different host than the appserver    
    props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");

    // optional.  Defaults to 3700.  Only needed if target orb port is not 3700.
    props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
    
    return props;
  }

  @Override
  public Set<String> getClientBundleSymbolicNames() {
    return BUNDLE_IDS;
  }

}
