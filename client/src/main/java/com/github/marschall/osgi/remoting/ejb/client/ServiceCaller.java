package com.github.marschall.osgi.remoting.ejb.client;

import static org.osgi.framework.ServiceException.REMOTE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.NamingException;

import org.osgi.framework.ServiceException;

class ServiceCaller implements InvocationHandler {

  private volatile Future<?> serviceProxy;

  private final ClassLoader classLoader;

  private final LoggerBridge logger;

  private volatile boolean valid;

  private final String jndiName;


  ServiceCaller(Future<?> serviceProxy, ClassLoader classLoader, LoggerBridge logger, String jndiName) {
    this.serviceProxy = serviceProxy;
    this.classLoader = classLoader;
    this.logger = logger;
    this.jndiName = jndiName;
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
      return method.invoke(this.serviceProxy.get(), args);
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
  
  void flushProxy(Context namingContext) throws NamingException {
    this.serviceProxy = new CompletedFuture<Object>(namingContext.lookup(jndiName));
  }

  void invalidate() {
    this.valid = false;
  }
  
  static final class CompletedFuture<T> implements Future<T> {
    
    private final T value;
    
    CompletedFuture(T value) {
      this.value = value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public T get() {
      return this.value;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
      return this.value;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return true;
    }
    
  }

}
