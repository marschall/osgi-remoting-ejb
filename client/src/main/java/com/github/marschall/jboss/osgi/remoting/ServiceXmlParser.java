package com.github.marschall.jboss.osgi.remoting;

import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.CDATA;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

final class ServiceXmlParser {

  private final XMLInputFactory inputFactory;

  ServiceXmlParser() {
    this.inputFactory = this.createInputFactory();
  }

  private XMLInputFactory createInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    //disable various features that we don't need and just cost performance
    factory.setProperty(IS_VALIDATING, Boolean.FALSE);
    factory.setProperty(IS_NAMESPACE_AWARE, Boolean.FALSE);
    factory.setProperty(IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
    factory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    factory.setProperty(SUPPORT_DTD, Boolean.FALSE);
    return factory;
  }

  ParseResult parseServiceXml(URL serviceXml) throws IOException, XMLStreamException {
    InputStream stream = openBufferedStream(serviceXml);
    try {
      XMLStreamReader reader = this.inputFactory.createXMLStreamReader(stream);
      try {
        return this.parseSafe(reader);
      } finally {
        // TODO CR
        reader.close();
      }
    } finally {
      stream.close();
    }

  }

  InputStream openBufferedStream(URL serviceXml) throws IOException {
    InputStream stream = serviceXml.openStream();
    if (stream instanceof BufferedInputStream) {
      return stream;
    } else {
      return new BufferedInputStream(stream);
    }
  }

  private String nextElementName(XMLStreamReader reader) throws XMLStreamException {
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == START_ELEMENT) {
        return reader.getLocalName();
      }
    }
    return null;
  }

  private ParseResult parseSafe(XMLStreamReader reader) throws XMLStreamException {
    String root = this.nextElementName(reader);
    if (root == null || !root.equals("service-descriptions")) {
      // TODO log?
      return new ParseResult(Collections.<ServiceInfo>emptyList());
    }

    List<ServiceInfo> services = new ArrayList<ServiceInfo>(3);
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == START_ELEMENT) {
        String localName = reader.getLocalName();
        if (localName.equals("service-description")) {
          ServiceInfo serviceDescription = this.parseServiceDescription(reader);
          if (serviceDescription != null) {
            services.add(serviceDescription);
          }
        }
      } else if (event == END_ELEMENT) {
        // done
        break;
      }
    }
    return new ParseResult(services);
  }


  private ServiceInfo parseServiceDescription(XMLStreamReader reader) throws XMLStreamException {
    // TODO make sure service description is not from other provider
    String interfaceName = null; 
    String jndiName = null;
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == START_ELEMENT) {
        String localName = reader.getLocalName();
        if (localName.equals("provide")) {
          interfaceName = this.readAttributeValue("interface", reader);
          this.consumeElement(reader);
        } else if (localName.equals("property")) {
          String propertyName = this.readAttributeValue("name", reader);
          if (propertyName.equals("osgi.remote.configuration.pojo.jndiName")) {
            jndiName = this.parseStringContent(reader);
          } else {
            this.consumeElement(reader);
          }
        }
      } else if (event == END_ELEMENT) {
        break;
      }
    }
    if (interfaceName != null && jndiName != null) {
      return new ServiceInfo(interfaceName, jndiName);
    } else {
      return null;
    }
  }

  private String readAttributeValue(String attributeName, XMLStreamReader reader) throws XMLStreamException {
    for (int i = 0; i < reader.getAttributeCount(); ++i) {
      String attributeLocalName = reader.getAttributeLocalName(i);
      if (attributeLocalName.equals(attributeName)) {
        return reader.getAttributeValue(i);

      }
    }
    return null;
  }
  
  private String parseStringContent(XMLStreamReader reader) throws XMLStreamException {
    StringBuilder builder = new StringBuilder();
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == END_ELEMENT) {
        break;
      } else if (event == CHARACTERS || event == CDATA) {
        builder.append(reader.getText());
      }
    }
    return builder.toString();
  }

  private void consumeElement(XMLStreamReader reader) throws XMLStreamException {
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == END_ELEMENT) {
        break;
      }
    }
  }

}
