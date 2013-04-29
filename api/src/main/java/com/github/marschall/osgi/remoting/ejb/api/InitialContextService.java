package com.github.marschall.osgi.remoting.ejb.api;

import java.util.Hashtable;
import java.util.Set;

/**
 * 
 * <p>
 * This has to be implemented by every client bridge (JBoss, GlassFish, ...)
 * and be registered before osgi-remoting-ejb-client is started
 * (eg. Dynamic Services / Service Component Runtime).
 */
public interface InitialContextService {

  // TODO identifier (eg. jboss) is a service property

  /**
   * Returns the environment used to create the initial context.
   * 
   * <p>
   * If {@code null} is returned then
   * {@link javax.naming.InitialContext#InitialContext()} is invoked.
   * 
   * @see javax.naming.InitialContext.InitialContext(Hashtable<?, ?>)
   * @return  the environment used to create the initial context
   */
  public Hashtable<?,?> getEnvironment();
  
  /**
   * Returns the symbolic names of the bundles that have to be added to the
   * class loader of each client bundle.
   * 
   * <p>
   * This contains the classes need by the ejb client library (eg. jboss-remoting),
   * not the classes need by the client bundle. Those should be dealt with by the
   * manifest of the client bundle.
   * 
   * @return symbolic names of the bundles that have to be added to the
   *  class loader of each client bundle
   */
  // TODO add option to add version (range)
  public Set<String> getClientBundleSymbolicNames();

}
