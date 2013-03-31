package com.github.marschall.jboss.osgi.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

public class ServiceXmlParserTest {

  private ServiceXmlParser parser;

  @Before
  public void setUp() {
    this.parser = new ServiceXmlParser();
  }
  
  @Test
  public void parseSampleJBossXml() throws IOException, XMLStreamException {
    URL resource = this.getClass().getClassLoader().getResource("ejb-client.xml");
    assertNotNull(resource);
    ParseResult result = this.parser.parseServiceXml(resource);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(4, result.size());
    
  }

}
