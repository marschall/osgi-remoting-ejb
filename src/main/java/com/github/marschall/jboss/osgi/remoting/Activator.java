package com.github.marschall.jboss.osgi.remoting;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
  
  private volatile ProxyService proxyService;

  @Override
  public void start(BundleContext context) throws Exception {
    this.proxyService = new ProxyService(context);
    context.removeBundleListener(this.proxyService);
    
    Bundle[] bundles = context.getBundles();
    this.proxyService.initialBundles(bundles);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    context.removeBundleListener(this.proxyService);
    this.proxyService = null;
  }

}
