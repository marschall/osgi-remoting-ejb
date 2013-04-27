package com.github.marschall.osgi.remoting.ejb.geronimo;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

public class GeronimoInitialContextService implements InitialContextService {
  
  static final Set<String> SYMBOLIC_NAMES;
  
  static {
    // everything else is a dependency of this
    SYMBOLIC_NAMES = Collections.singleton("org.apache.openejb.client");
  }

  @Override
  public Hashtable<?, ?> getEnvironment() {
    //http://apache-geronimo.328035.n3.nabble.com/ejb-client-td338382.html
    Hashtable<String, Object> env = new Hashtable<String, Object>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
    env.put(Context.PROVIDER_URL, "ejbd://localhost:4201");
//    env.put("java.naming.factory.host", "ejb_container_ip");
//    env.put("java.naming.factory.port", "4201");
//    env.put("openejb.authentication.realmName","geronimo-admin"); 
    env.put(Context.SECURITY_PRINCIPAL, "system");
    env.put(Context.SECURITY_CREDENTIALS, "manager");
    return env;
  }

  @Override
  public Set<String> getClientBundleSymbolicNames() {
    return SYMBOLIC_NAMES;
  }

}
