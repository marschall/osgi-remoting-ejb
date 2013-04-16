package com.github.marschall.osgi.remoting.ejb.client;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import com.github.marschall.osgi.remoting.ejb.client.BundlesProxyClassLoader.ArrayIterator;

public class ArrayIteratorTest {

  @Test
  public void empty() {
    Iterator<String> iterator = new ArrayIterator<String>(new String[0]);
    assertFalse(iterator.hasNext());
  }
  
  @Test
  public void one() {
    Iterator<String> iterator = new ArrayIterator<String>(new String[]{"one"});
    assertTrue(iterator.hasNext());
    assertEquals("one", iterator.next());
    assertFalse(iterator.hasNext());
  }
  
  @Test
  public void two() {
    Iterator<String> iterator = new ArrayIterator<String>(new String[]{"one", "two"});
    assertTrue(iterator.hasNext());
    assertEquals("one", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("two", iterator.next());
    assertFalse(iterator.hasNext());
  }

}
