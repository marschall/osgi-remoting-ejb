package com.github.marschall.jboss.osgi.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.osgi.framework.ServiceException.REMOTE;

import org.osgi.framework.ServiceException;

class ServiceCaller implements InvocationHandler {
  
  private final Object serviceProxy;
  
  private final ClassLoader classLoader;

  
  ServiceCaller(Object serviceProxy, ClassLoader classLoader) {
    this.serviceProxy = serviceProxy;
    this.classLoader = classLoader;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Thread currentThread = Thread.currentThread();
    ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(this.classLoader);
    try {
      // TODO switch TCCL
      return method.invoke(method, args);
    } catch (Throwable /* JBossRemotingException */ t) {
      // TODO log
      throw new ServiceException("service call failed", REMOTE, t);
    } finally {
      currentThread.setContextClassLoader(oldContextClassLoader);
    }
  }

  void invalidate() {
    // TODO Auto-generated method stub
    
  }


}
