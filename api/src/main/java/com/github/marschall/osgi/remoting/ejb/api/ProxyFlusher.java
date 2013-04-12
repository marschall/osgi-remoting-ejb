package com.github.marschall.osgi.remoting.ejb.api;

/**
 * Allows to flush and relookup all client proxies.
 * 
 * <p>
 * This i
 */
public interface ProxyFlusher {

  /**
   * Flushes all client proxies and looks them up again. Blocks until
   * successful.
   */
  public void flushProxies();

}
