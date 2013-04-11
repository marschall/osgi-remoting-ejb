package com.github.marschall.osgi.remoting.ejb.client;

// implemented by this bundle
// can be used
// TODO identifier (eg. jboss) is a service property
public interface ProxyFlusher {

  void flushProxies();

}
