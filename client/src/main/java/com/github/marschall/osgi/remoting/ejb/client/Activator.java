package com.github.marschall.osgi.remoting.ejb.client;

import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;
import com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher;

public class Activator implements BundleActivator {

  private volatile ProxyService proxyService;
  private volatile LoggerBridge logger;
  private volatile ServiceReference<InitialContextService> initialContextServiceReference;
  private volatile ServiceRegistration<ProxyFlusher> flusherRegisterService;
  private volatile ScheduledExecutorService executor;

  @Override
  public void start(BundleContext context) throws Exception {
    this.logger = new LoggerBridge(context);
    this.initialContextServiceReference = context.getServiceReference(InitialContextService.class);
    
    this.executor = Executors.newSingleThreadScheduledExecutor(new LookUpThreadFactory());
    
    InitialContextService initialContextService = context.getService(this.initialContextServiceReference);
    this.proxyService = new ProxyService(context, this.logger, initialContextService, this.executor);
    context.addBundleListener(this.proxyService);

    Bundle[] bundles = context.getBundles();
    this.proxyService.initialBundles(bundles);
    
    this.flusherRegisterService = context.registerService(ProxyFlusher.class, this.proxyService, new Hashtable<String, Object>());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    context.removeBundleListener(this.proxyService);
    this.proxyService.stop();
    this.logger.stop();
    context.ungetService(this.initialContextServiceReference);
    this.flusherRegisterService.unregister();
    this.executor.shutdownNow();

    this.proxyService = null;
    this.logger = null;
    this.initialContextServiceReference = null;
    this.flusherRegisterService = null;
    this.executor = null;
  }
  
  static final class LookUpThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "osgi-remoting-ejb-proxy-lookup");
    }
    
  }

}
