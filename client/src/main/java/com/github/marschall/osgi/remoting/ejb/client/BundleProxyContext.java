/*
 * Copyright (C) 2013 by Netcetera AG.
 * All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of Netcetera AG, Switzerland.
 * The program(s) may be used and/or copied only with the written permission of Netcetera AG or
 * in accordance with the terms and conditions stipulated in the agreement/contract under which 
 * the program(s) have been supplied.
 */
package com.github.marschall.osgi.remoting.ejb.client;

import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.github.marschall.osgi.remoting.ejb.api.InitialContextService;

final class BundleProxyContext {

  private volatile Context namingContext;

  private final Collection<ServiceCaller> callers;

  private final Collection<ServiceRegistration<?>> registrations;

  private final ClassLoader classLoader;

  BundleProxyContext(Context namingContext, Collection<ServiceCaller> callers,
      Collection<ServiceRegistration<?>> registrations, ClassLoader classLoader) {
    this.namingContext = namingContext;
    this.callers = callers;
    this.registrations = registrations;
    this.classLoader = classLoader;
  }

  void release(BundleContext bundleContext) throws NamingException {
    this.unregisterServices(bundleContext);
    this.invalidateCallers();
    this.closeNamingConext();
  }

  private void closeNamingConext() throws NamingException {
    this.namingContext.close();
  }

  void unregisterServices(BundleContext bundleContext) {
    for (ServiceRegistration<?> registration : this.registrations) {
      bundleContext.ungetService(registration.getReference());
    }
  }

  void flushProxies(InitialContextService initialContextService) throws NamingException {
    Thread currentThread = Thread.currentThread();
    ClassLoader oldClassLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(this.classLoader);

    try {
      this.namingContext.close();
      Hashtable<?,?> environment = initialContextService.getEnvironment();
      if (environment != null) {
        this.namingContext = new InitialContext(environment);
      } else {
        this.namingContext = new InitialContext();
      }
      for (ServiceCaller caller : this.callers) {
        // TODO catch NamingException (collect causes for SE 7)
        caller.flushProxy(this.namingContext);
      }
    } finally{
      currentThread.setContextClassLoader(oldClassLoader);
    }
  }

  private void invalidateCallers() {
    for (ServiceCaller caller : callers) {
      caller.invalidate();
    }
  }

}