package com.github.marschall.osgi.remoting.ejb.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

public class Activator implements BundleActivator {

  private volatile ProxyService proxyService;
  private volatile LoggerBridge logger;
  private volatile ServiceTracker<InitialContextService, InitialContextService> initialContextServiceTracker;
  private volatile ExecutorService executor;
  private volatile BundleContext context;

  @Override
  public void start(BundleContext context) throws Exception {
    this.context = context;
    this.logger = new LoggerBridge(context);
    this.executor = Executors.newSingleThreadExecutor(new LookUpThreadFactory());
    
    this.proxyService = new ProxyService(context, this.logger, this.executor);
    
    // this will trigger the loading of the InitialContextService service implementation
    // however loading can only start once this bundle has been activated
    // therefore we need to move the waiting to a different thread
    this.initialContextServiceTracker = new ServiceTracker<InitialContextService, InitialContextService>(context, InitialContextService.class, new WaitForInitialContextService());
    this.initialContextServiceTracker.open(true);
  }
  
  final class WaitForInitialContextService implements ServiceTrackerCustomizer<InitialContextService, InitialContextService> {

    @Override
    public InitialContextService addingService(ServiceReference<InitialContextService> reference) {
      InitialContextService service = context.getService(reference);
      proxyService.setInitialContextService(service);
      return service;
    }

    @Override
    public void modifiedService(ServiceReference<InitialContextService> reference, InitialContextService service) {
      // nothing
    }

    @Override
    public void removedService(ServiceReference<InitialContextService> reference, InitialContextService service) {
      context.ungetService(reference);
    }
    
  }
  
  @Override
  public void stop(BundleContext context) throws Exception {
    this.proxyService.stop();
    this.logger.stop();
    this.initialContextServiceTracker.close();
    this.executor.shutdownNow();

    this.proxyService = null;
    this.logger = null;
    this.initialContextServiceTracker = null;
    this.executor = null;
  }
  
  static final class LookUpThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "osgi-remoting-ejb-proxy-lookup");
    }
    
  }

}
