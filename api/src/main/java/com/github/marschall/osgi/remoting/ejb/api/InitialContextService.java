package com.github.marschall.osgi.remoting.ejb.api;

import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

// will be implemented by jboss specific implementation
public interface InitialContextService {

  // TODO identifier (eg. jboss) is a service property

  public Hashtable<?,?> getEnvironment();
  
  /**
   * The symbolic names of the bundles that have to be added to the
   * class loader of each client bundle. This contains the classes need
   * by jboss-remoting, not the classes need by the client bundle. Those
   * should be dealt with by the manifest of the client bundle.
   */
  public Set<String> getClientBundleSymbolicNames();

}
