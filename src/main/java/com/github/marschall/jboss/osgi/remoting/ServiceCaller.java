package com.github.marschall.jboss.osgi.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.osgi.framework.ServiceException.REMOTE;

import org.osgi.framework.ServiceException;

class ServiceCaller implements InvocationHandler {
  
  private final Object serviceProxy;
  
  private final ClassLoader classLoader;

  private final Logger logger;

  
  ServiceCaller(Object serviceProxy, ClassLoader classLoader, Logger logger) {
    this.serviceProxy = serviceProxy;
    this.classLoader = classLoader;
    this.logger = logger;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Thread currentThread = Thread.currentThread();
    ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(this.classLoader);
    try {
      return method.invoke(method, args);
    } catch (Throwable /* JBossRemotingException */ t) {
      // TODO service reference
      String message = "service call " + method.getDeclaringClass().getName() + "#" + method.getName() + "() failed";
      this.logger.error(message, t);
      throw new ServiceException(message, REMOTE, t);
    } finally {
      currentThread.setContextClassLoader(oldContextClassLoader);
    }
  }

  void invalidate() {
    // TODO Auto-generated method stub
    
  }


}
