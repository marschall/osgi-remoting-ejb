package com.github.marschall.osgi.remoting.ejb.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

public class Activator implements BundleActivator {

  private volatile ProxyService proxyService;
  private volatile LoggerBridge logger;
  private volatile ServiceTracker<InitialContextService, InitialContextService> initialContextServiceTracker;
  private volatile ExecutorService executor;
  private volatile boolean stopped;

  @Override
  public void start(BundleContext context) throws Exception {
    this.stopped = false;
    this.logger = new LoggerBridge(context);
    this.executor = Executors.newSingleThreadExecutor(new LookUpThreadFactory());
    
    this.initialContextServiceTracker = new ServiceTracker<InitialContextService, InitialContextService>(context, InitialContextService.class, null);
    
    // this will trigger the loading of the InitialContextService service implementation
    // however loading can only start once this bundle has been activated
    // therefore we need to move the waiting to a different thread
    this.initialContextServiceTracker.open();
    
    this.executor.submit(new WaitForInitialContextService());
    
    this.proxyService = new ProxyService(context, this.logger, this.executor);
  }
  
  final class WaitForInitialContextService implements Runnable {

    @Override
    public void run() {
      ExecutorService e = executor;
      ServiceTracker<InitialContextService, InitialContextService> t = initialContextServiceTracker;
      if (e != null && t != null && !stopped) {
        InitialContextService initialContextService = t.getService();
        if (initialContextService == null) {
          try {
            initialContextService = t.waitForService(TimeUnit.SECONDS.toMillis(1L));
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
          }
        }
        if (initialContextService != null) {
          ProxyService s = proxyService;
          if (s != null) {
            s.setInitialContextService(initialContextService);
          }
        } else {
          e.submit(this);
        }
      }
    }
    
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    this.stopped = true;
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
