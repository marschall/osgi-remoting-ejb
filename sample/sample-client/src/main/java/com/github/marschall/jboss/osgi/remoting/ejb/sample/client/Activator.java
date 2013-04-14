package com.github.marschall.jboss.osgi.remoting.ejb.sample.client;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatefulRemote1;
import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatefulRemote2;
import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatelessRemote1;
import com.github.marschall.jboss.osgi.remoting.ejb.sample.StatelessRemote2;

public class Activator implements BundleActivator {

  private volatile Filter filter;

  private volatile Queue<ServiceTracker<?, ?>> trackers;

  private volatile BundleContext context;

  private ServiceTracker<StatelessRemote1, StatelessRemote1> serviceTracker;

  @Override
  public void start(BundleContext context) throws Exception {
    this.context = context;
    this.trackers = new LinkedBlockingQueue<ServiceTracker<?, ?>>();
    this.filter = context.createFilter("(service.imported=*)");

    this.serviceTracker = new ServiceTracker<StatelessRemote1, StatelessRemote1>(context, StatelessRemote1.class, new WaitForProxies());
    this.serviceTracker.open(true);
  }

  void callServices() {
    StatelessRemote1 statelessRemote1 = this.lookup(StatelessRemote1.class);
    System.out.println(statelessRemote1.statelessRemote1());

    StatelessRemote2 statelessRemote2 = this.lookup(StatelessRemote2.class);
    System.out.println(statelessRemote2.statelessRemote2());

    StatefulRemote1 statefulRemote1 = this.lookup(StatefulRemote1.class);
    System.out.println(statefulRemote1.statefulRemote1());

    StatefulRemote2 statefulRemote2 = this.lookup(StatefulRemote2.class);
    System.out.println(statefulRemote2.statefulRemote2());
  }
  
  final class WaitForProxies implements ServiceTrackerCustomizer<StatelessRemote1, StatelessRemote1> {

    @Override
    public StatelessRemote1 addingService(ServiceReference<StatelessRemote1> reference) {
      StatelessRemote1 service = context.getService(reference);
      // we are inside the callback that's executed when a service is registered
      // in this thread we can not wait for services being registered
      Runnable runnable = new Runnable() {
        public void run() {
          callServices();
        }
      };
      Thread thread = new Thread(runnable, "service-caller");
      thread.start();
      return service;
    }

    @Override
    public void modifiedService(ServiceReference<StatelessRemote1> reference, StatelessRemote1 service) {
      // nothing
    }

    @Override
    public void removedService(ServiceReference<StatelessRemote1> reference, StatelessRemote1 service) {
      context.ungetService(reference);
    }
    
  }

  private <T> T lookup(Class<T> clazz) {
    // TODO filter
    ServiceTracker<T,T> tracker = new ServiceTracker<T, T>(context, clazz, null);
    tracker.open(true);
    this.trackers.add(tracker);
    T service = tracker.getService();
    if (service != null) {
      return service;
    } else {
      try {
        service = tracker.waitForService(TimeUnit.SECONDS.toMillis(1L));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalArgumentException("interrupted while waiting for: " + clazz);
      }
      if (service == null) {
        throw new IllegalArgumentException("service not found: " + clazz);
      }
      return service;
    }
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    this.filter = null;

    ServiceTracker<?,?> tracker = this.trackers.poll();
    while (tracker != null) {
      tracker.close();
      tracker = this.trackers.poll();
    }
    this.serviceTracker.close();
    this.serviceTracker = null;
    this.trackers = null;

  }

}
