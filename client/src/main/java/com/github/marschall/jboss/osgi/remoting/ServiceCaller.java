package com.github.marschall.jboss.osgi.remoting;

import static org.osgi.framework.ServiceException.REMOTE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.osgi.framework.ServiceException;

class ServiceCaller implements InvocationHandler {

  private final Object serviceProxy;

  private final ClassLoader classLoader;

  private final LoggerBridge logger;

  private volatile boolean valid;


  ServiceCaller(Object serviceProxy, ClassLoader classLoader, LoggerBridge logger) {
    this.serviceProxy = serviceProxy;
    this.classLoader = classLoader;
    this.logger = logger;
    this.valid = true;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO check we're not in the UI thread
    Thread currentThread = Thread.currentThread();
    ClassLoader oldContextClassLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(this.classLoader);
    try {
      if (!this.valid) {
        throw new IllegalStateException("service is no longer valid");
      }
      return method.invoke(this.serviceProxy, args);
      // javax.security.sasl.SaslException
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
    this.valid = false;
  }


}
