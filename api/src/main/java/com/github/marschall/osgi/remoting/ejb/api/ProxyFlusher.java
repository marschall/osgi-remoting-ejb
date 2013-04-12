package com.github.marschall.osgi.remoting.ejb.api;

/**
 * Allows to flush and relookup all client proxies.
 * 
 * <p>
 * This is mostly useful for lazy login. It can be used as follows:
 * <ol>
 *  <li>do unauthenticated class</li>
 *  <li>authenticate (through proprietary API)</li>
 *  <li>call {@link #flushProxies()}</li>
 *  <li>all calls from this moment on will be authenticated</li>
 * </ol>
 * 
 * <p>
 * All the services stay valid after the flushing.
 */
public interface ProxyFlusher {

  /**
   * Flushes all client proxies and looks them up again. Blocks until
   * successful.
   */
  public void flushProxies();

}
