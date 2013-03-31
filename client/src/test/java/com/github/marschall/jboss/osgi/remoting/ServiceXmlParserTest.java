package com.github.marschall.jboss.osgi.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.hasSize;

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
    
    assertThat(result.services, hasSize(4));
    
    ServiceInfo service = result.services.get(0);
    assertEquals("com.github.marschall.jboss.osgi.remoting.StatelessRemote1", service.interfaceName);
    assertEquals("ejb:jboss-osgi-remoting-sample-ear/jboss-osgi-remoting-sample-ejb//StatelessBean!com.github.marschall.jboss.osgi.remoting.StatelessRemote1", service.jndiName);
    
    service = result.services.get(1);
    assertEquals("com.github.marschall.jboss.osgi.remoting.StatelessRemote2", service.interfaceName);
    assertEquals("ejb:jboss-osgi-remoting-sample-ear/jboss-osgi-remoting-sample-ejb//StatelessBean!com.github.marschall.jboss.osgi.remoting.StatelessRemote2", service.jndiName);
    
    service = result.services.get(2);
    assertEquals("com.github.marschall.jboss.osgi.remoting.StatefulRemote1", service.interfaceName);
    assertEquals("ejb:jboss-osgi-remoting-sample-ear/jboss-osgi-remoting-sample-ejb//StatefulBean!com.github.marschall.jboss.osgi.remoting.StatefulRemote1?stateful", service.jndiName);
    
    service = result.services.get(3);
    assertEquals("com.github.marschall.jboss.osgi.remoting.StatefulRemote2", service.interfaceName);
    assertEquals("ejb:jboss-osgi-remoting-sample-ear/jboss-osgi-remoting-sample-ejb//StatefulBean!com.github.marschall.jboss.osgi.remoting.StatefulRemote2?stateful", service.jndiName);
  }

}
