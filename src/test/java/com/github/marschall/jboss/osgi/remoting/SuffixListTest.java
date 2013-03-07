package com.github.marschall.jboss.osgi.remoting;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SuffixListTest {

  @Test
  public void simple() {
    List<String> list = new SuffixList<String>(Arrays.asList("1", "2"), "3");
    
    assertEquals(Arrays.asList("1", "2", "3"), list);
  }
  
  @Test
  public void emptyPrefix() {
    List<String> list = new SuffixList<String>(Collections.<String>emptyList(), "1");
    
    assertEquals(Collections.singletonList("1"), list);
  }

}
