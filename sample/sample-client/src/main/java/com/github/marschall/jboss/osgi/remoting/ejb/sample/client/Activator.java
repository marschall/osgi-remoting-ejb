package com.github.marschall.jboss.osgi.remoting.ejb.sample.client;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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

  @Override
  public void start(BundleContext context) throws Exception {
    this.serviceReferences = new LinkedBlockingQueue<ServiceReference<?>>();
    filter = context.createFilter("(service.imported=*)");
    StatelessRemote1.class.getName();
    
//    StatelessRemote1 statelessRemote1 = this.lookup(context, StatelessRemote1.class);
//    System.out.println(statelessRemote1.statelessRemote1());
//    
//    StatelessRemote2 statelessRemote2 = this.lookup(context, StatelessRemote2.class);
//    System.out.println(statelessRemote2.statelessRemote2());
//    
//    StatefulRemote1 statefulRemote1 = this.lookup(context, StatefulRemote1.class);
//    System.out.println(statefulRemote1.statefulRemote1());
//    
//    StatefulRemote2 statefulRemote2 = this.lookup(context, StatefulRemote2.class);
//    System.out.println(statefulRemote2.statefulRemote2());
  }
  
  private <T> T lookup(BundleContext context, Class<T> clazz) throws InvalidSyntaxException {
    Collection<ServiceReference<T>> references = context.getServiceReferences(clazz, this.filter.toString());
    if (!references.isEmpty()) {
      ServiceReference<T> reference = references.iterator().next();
      this.serviceReferences.add(reference);
      return context.getService(reference);
    } else {
      throw new IllegalArgumentException("service not found: " + clazz);
    }
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    this.filter = null;
    
    ServiceReference<?> reference = this.serviceReferences.poll();
    while (reference != null) {
      context.ungetService(reference);
      reference = this.serviceReferences.poll();
    }
    this.serviceReferences = null;
  }

}
