package com.github.marschall.osgi.remoting.ejb.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.osgi.framework.Bundle;

final class BundlesProxyClassLoader extends ClassLoader {

  static {
    // try to call registerAsParallelCapable
    try {
      Method registerAsParallelCapable = ClassLoader.class.getDeclaredMethod("registerAsParallelCapable");
      registerAsParallelCapable.setAccessible(true);
      registerAsParallelCapable.invoke(null);
    } catch (SecurityException e) {
      // ignore, no permissions
    } catch (IllegalAccessException e) {
      // shouldn't happen
    } catch (InvocationTargetException e) {
      // shouldn't happen
    } catch (NoSuchMethodException e) {
      // ignore, on 1.7
    }
  }

  private final Bundle[] bundles;

  BundlesProxyClassLoader(Bundle[] bundles) {
    this.bundles = bundles;
  }


  // Note: Both ClassLoader.getResources(...) and bundle.getResources(...) consult
  // the boot classloader. As a result, BundleProxyClassLoader.getResources(...)
  // might return duplicate results from the boot classloader. Prior to Java 5
  // Classloader.getResources was marked final. If your target environment requires
  // at least Java 5 you can prevent the occurence of duplicate boot classloader
  // resources by overriding ClassLoader.getResources(...) instead of
  // ClassLoader.findResources(...).
  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    return new ResourceEnumeration(name);
  }

  class ResourceEnumeration implements Enumeration<URL> {

    private URL url;
    private final Iterator<Bundle> bundleIterator;
    private Bundle bundle;
    private Enumeration<URL> enumeration;
    private final String name;

    ResourceEnumeration(String name) {
      this.name = name;
      this.bundleIterator = new ArrayIterator<Bundle>(bundles);
    }

    private boolean next() {
      if (this.url != null) {
        return true;
      }
      while (this.enumeration == null || !this.enumeration.hasMoreElements()) {
        // try next bundle
        if (this.bundleIterator.hasNext()) {
          this.bundle = this.bundleIterator.next();
        } else {
          // no more bundles left, stop searching
          return false;
        }

        try {
          this.enumeration = this.bundle.getResources(name);
        } catch (IOException excetpion) {
          // REVIEW skip bundle?
          throw new RuntimeException("could not get resoruces", excetpion);
        }
      }
      this.url = this.enumeration.nextElement();
      return true;
    }

    @Override
    public boolean hasMoreElements() {
      return next();
    }

    @Override
    public URL nextElement() {
      if (!next()) {
        throw new NoSuchElementException();
      }
      URL u = this.url;
      this.url = null;
      return u;
    }



  }

  @Override
  public URL findResource(String name) {
    for (Bundle bundle : this.bundles) {
      URL resource = bundle.getResource(name);
      if (resource != null) {
        return resource;
      }
    }
    return null;
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    for (Bundle bundle : this.bundles) {
      try {
        return bundle.loadClass(name);
      } catch (ClassNotFoundException e) {
        continue;
      }
    }
    throw new ClassNotFoundException(name);
  }

  @Override
  public URL getResource(String name) {
    return this.findResource(name);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> clazz = this.findClass(name);
    if (resolve) {
      super.resolveClass(clazz);
    }
    return clazz;
  }
  
  static final class ArrayIterator<T> implements Iterator<T> {
    
    private final T[] array;
    private int index;

    ArrayIterator(T[] array) {
      this.array = array;
      this.index = 0;
    }

    @Override
    public boolean hasNext() {
      return this.index < this.array.length;
    }

    @Override
    public T next() {
      return this.array[this.index++];
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    
    
  }
 
}
