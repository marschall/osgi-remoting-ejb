package com.github.marschall.jboss.osgi.remoting;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SuffixListTest {

  @Test
  public void simple() {
    List<String> list = new SuffixList<String>(Arrays.asList("1", "2"), "3");
    
    assertEquals(Arrays.asList("1", "2", "3"), list);
    assertThat(list, hasSize(3));
    assertEquals("1", list.get(0));
    assertEquals("2", list.get(1));
    assertEquals("3", list.get(2));
  }
  
  @Test
  public void emptyPrefix() {
    List<String> list = new SuffixList<String>(Collections.<String>emptyList(), "1");
    
    assertEquals(Collections.singletonList("1"), list);
    assertThat(list, hasSize(1));
    assertEquals("1", list.get(0));
  }

}
