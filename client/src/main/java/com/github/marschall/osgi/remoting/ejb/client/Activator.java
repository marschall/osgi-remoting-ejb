package com.github.marschall.osgi.remoting.ejb.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

public class Activator implements BundleActivator {

  private volatile ProxyService proxyService;
  private volatile LoggerBridge logger;
  private volatile ServiceReference<InitialContextService> initialContextService;
  private volatile ExecutorService executor;
  private volatile boolean stopped;
  private volatile BundleContext context;

  @Override
  public void start(BundleContext context) throws Exception {
    this.context = context;
    this.stopped = false;
    this.logger = new LoggerBridge(context);
    this.executor = Executors.newSingleThreadExecutor(new LookUpThreadFactory());
    
    this.proxyService = new ProxyService(context, this.logger, this.executor);
    
    // this will trigger the loading of the InitialContextService service implementation
    // however loading can only start once this bundle has been activated
    // therefore we need to move the waiting to a different thread
    this.executor.submit(new WaitForInitialContextService());
  }
  
  final class WaitForInitialContextService implements Runnable {

    @Override
    public void run() {
      ExecutorService e = executor;
      BundleContext c = context;
      if (e != null && c != null && !stopped) {
        ServiceReference<InitialContextService> s = c.getServiceReference(InitialContextService.class);
        if (s != null) {
          ProxyService p = proxyService;
          if (p != null) {
            // TODO potential race with #close
            initialContextService = s;
            p.setInitialContextService(c.getService(s));
          }
        } else {
          try {
            // TODO schedule
            Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100L));
            e.submit(this);
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
    
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    this.stopped = true;
    this.proxyService.stop();
    this.logger.stop();
    if (initialContextService != null) {
      context.ungetService(initialContextService);
    }
    this.executor.shutdownNow();

    this.proxyService = null;
    this.logger = null;
    this.initialContextService = null;
    this.executor = null;
  }
  
  static final class LookUpThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "osgi-remoting-ejb-proxy-lookup");
    }
    
  }

}
