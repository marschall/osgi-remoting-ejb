package com.github.marschall.jboss.osgi.remoting.ejb.sample.client;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatefulRemote1;
import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatefulRemote2;
import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatelessRemote1;
import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatelessRemote2;

public class Activator implements BundleActivator {

  private volatile Filter filter;

  private volatile Queue<ServiceReference<?>> serviceReferences;

  private volatile BundleContext context;

  private ExecutorService executor;

  @Override
  public void start(BundleContext context) throws Exception {
    this.context = context;
    this.serviceReferences = new LinkedBlockingQueue<ServiceReference<?>>();
    this.filter = context.createFilter("(service.imported=*)");

    this.executor = Executors.newSingleThreadExecutor(new PollerThreadFactory());
    this.executor.submit(new WaitForProxies());
  }

  void callServices() throws InvalidSyntaxException {
    StatelessRemote1 statelessRemote1 = this.lookup(context, StatelessRemote1.class);
    System.out.println(statelessRemote1.statelessRemote1());

    StatelessRemote2 statelessRemote2 = this.lookup(context, StatelessRemote2.class);
    System.out.println(statelessRemote2.statelessRemote2());

    StatefulRemote1 statefulRemote1 = this.lookup(context, StatefulRemote1.class);
    System.out.println(statefulRemote1.statefulRemote1());

    StatefulRemote2 statefulRemote2 = this.lookup(context, StatefulRemote2.class);
    System.out.println(statefulRemote2.statefulRemote2());
  }

  final class WaitForProxies implements Runnable {

    @Override
    public void run() {
      ExecutorService e = executor;
      if (e != null) {
        try {
          ServiceReference<?>[] serviceReferences = context.getAllServiceReferences(StatelessRemote1.class.getName(), null);
          if (serviceReferences != null) {
            callServices();
          } else {
            try {
              Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100L));
              e.submit(this);
            } catch (InterruptedException e1) {
              Thread.currentThread().interrupt();
            }

          }
        } catch (InvalidSyntaxException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }

    }
  }


  private <T> T lookup(BundleContext context, Class<T> clazz) throws InvalidSyntaxException {
    ServiceReference<?>[] references = context.getAllServiceReferences(clazz.getName(), this.filter.toString());
    if (references != null && references.length > 0) {
      ServiceReference<?> reference = references[0];
      this.serviceReferences.add(reference);
      return clazz.cast(context.getService(reference));
    } else {
      throw new IllegalArgumentException("service not found: " + clazz);
    }
    //    Collection<ServiceReference<T>> references = context.getServiceReferences(clazz, null);
    //    if (!references.isEmpty()) {
    //      ServiceReference<T> reference = references.iterator().next();
    //      this.serviceReferences.add(reference);
    //      return context.getService(reference);
    //    } else {
    //      throw new IllegalArgumentException("service not found: " + clazz);
    //    }
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    this.filter = null;

    ServiceReference<?> reference = this.serviceReferences.poll();
    while (reference != null) {
      context.ungetService(reference);
      reference = this.serviceReferences.poll();
    }
    this.executor.shutdownNow();
    this.serviceReferences = null;
    this.executor = null;

  }

  static final class PollerThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "sample-client-proxy-poller");
    }

  }

}
