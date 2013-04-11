package com.github.marschall.osgi.remoting.ejb.client;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

public class Activator implements BundleActivator {

  private volatile ProxyService proxyService;
  private volatile LoggerBridge logger;
  private volatile ServiceReference<InitialContextService> serviceReference;

  @Override
  public void start(BundleContext context) throws Exception {
    this.logger = new LoggerBridge(context);
    this.serviceReference = context.getServiceReference(InitialContextService.class);
    
    this.proxyService = new ProxyService(context, this.logger, context.getService(this.serviceReference));
    context.addBundleListener(this.proxyService);

    Bundle[] bundles = context.getBundles();
    this.proxyService.initialBundles(bundles);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    context.removeBundleListener(this.proxyService);
    this.proxyService.stop();
    this.logger.stop();
    context.ungetService(this.serviceReference);

    this.proxyService = null;
    this.logger = null;
    this.serviceReference = null;
  }

}
