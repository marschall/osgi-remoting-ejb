package com.github.marschall.osgi.remoting.ejb.processor;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class ServiceXmlGenerator extends AbstractProcessor {

  private static final String FILE_NAME = "ejb-client.xml";
  static final String MODULE_NAME_OPTION = "javax.ejb.module.name";
  static final String APPLICATION_NAME_OPTION = "javax.ejb.application.name";
  static final String DISTINCT_NAME_OPTION = "org.jboss.distinct.name";

  private EjbCollector collector;
  private String applicationName;
  private String moduleName;
  private boolean jbossSyntax;
  private String distinctName;

  public ServiceXmlGenerator() {
    super();
  }

  @Override
  public void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    Map<String, String> options = processingEnv.getOptions();
    this.applicationName = options.get(APPLICATION_NAME_OPTION);
    this.moduleName = options.get(MODULE_NAME_OPTION);
    this.collector = new EjbCollector(processingEnv);
    
    this.jbossSyntax = options.containsKey(DISTINCT_NAME_OPTION);
    if (this.jbossSyntax) {
      this.distinctName = options.get(DISTINCT_NAME_OPTION);
      if (this.distinctName == null) {
        this.distinctName = "";
      }
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.errorRaised()) {
      return false;
    } else if (roundEnv.processingOver()) {
      this.writeServiceXml();
      return false;
    } else {
      this.collector.processRound(roundEnv);
      return true;
    }
  }

  private void writeServiceXml() {
    // TODO move to own class
    if (!this.collector.isEmpty()) {
      Messager messager = this.processingEnv.getMessager();
      try {
        this.writeServiceXmlProtected();
      } catch (IOException e) {
        messager.printMessage(ERROR, "IOException: " + e.getMessage());
        throw new RuntimeException("could not write " + FILE_NAME, e);
      } catch (XMLStreamException e) {
        messager.printMessage(ERROR, "XMLStreamException: " + e.getMessage());
        throw new RuntimeException("could not write " + FILE_NAME, e);
      }
    }
    // TODO delete if present?

  }

  private void writeServiceXmlProtected() throws IOException, XMLStreamException {
    Filer filer = this.processingEnv.getFiler();
    // TODO update if present?
    FileObject serviceXml = filer.createResource(CLASS_OUTPUT, "", "OSGI-INF/remote-service/" + FILE_NAME, this.collector.getElements());
    OutputStream outputStream = serviceXml.openOutputStream();
    BufferedOutputStream output = null;
    XMLStreamWriter writer = null;
    try {
      output = new BufferedOutputStream(outputStream);
      XMLOutputFactory factory = XMLOutputFactory.newInstance();
      // TODO tweak options
      String encoding = "UTF-8";
      writer = factory.createXMLStreamWriter(output, encoding);
      writer.writeStartDocument(encoding, "1.0");
      
      writeServiceDescriptons(writer);
      
      writer.writeEndDocument();
    } finally {
      if (writer != null) {
        writer.close();
      }
      if (output != null) {
        output.close();
      }
      outputStream.close();
    }
  }

  private void writeServiceDescriptons(XMLStreamWriter writer) throws XMLStreamException {
//    writer.writeStartElement("http://www.osgi.org/xmlns/sd/v1.0.0", "service-descriptions");
    writer.writeStartElement("service-descriptions");
    writer.writeDefaultNamespace("http://www.osgi.org/xmlns/sd/v1.0.0");
    
    for (EjbInfo bean : this.collector.beans) {
      this.writeServiceDescripton(bean, writer);
    }
    
    writer.writeEndElement(); //service-descriptions
  }

  private void writeServiceDescripton(EjbInfo bean, XMLStreamWriter writer) throws XMLStreamException {
    for (String remoteInterface : bean.remoteInterfaces) {
      this.writeServiceDescripton(bean, remoteInterface, writer);
    }
    
  }

  private void writeServiceDescripton(EjbInfo bean, String remoteInterface, XMLStreamWriter writer) throws XMLStreamException {
    writer.writeStartElement("service-description");
    
    // <provide interface="org.coderthoughts.auction.AuctionService"/>
    writer.writeStartElement("provide");
    writer.writeAttribute("interface", remoteInterface);
    writer.writeEndElement(); //provide
    
    // http://cxf.apache.org/distributed-osgi-reference.html
    // http://wiki.eclipse.org/EIG:OSGi_Remote_Services
    
    // <property name="service.exported.interfaces">*</property>
    writeProperty("service.exported.interfaces", "*", writer);
    
    // <property name="service.exported.configs">pojo</property>
    writeProperty("service.exported.configs", "com.github.marschall.osgi.remoting.ejb", writer);
    
    // <property name="com.github.marschall.ejb">foo/bar</property>
    String jndiName = jndiName(bean, remoteInterface);
    writeProperty("com.github.marschall.osgi.remoting.ejb.jndiName", jndiName, writer);
    
    writer.writeEndElement(); //service-description
  }

  private String jndiName(EjbInfo bean, String remoteInterface) {
    // TODO optimize
    if (this.jbossSyntax) {
      return "ejb:" + this.applicationName + '/' + this.moduleName +'/' + this.distinctName + '/' + bean.nonQualifiedClassName + '!' + remoteInterface
          + (bean.stateful ? "?stateful" : "");
      
    } else {
      return this.applicationName + '/' + this.moduleName +'/' + bean.nonQualifiedClassName + '!' + remoteInterface;
    }
  }
  
  void writeProperty(String name, String value, XMLStreamWriter writer) throws XMLStreamException {
    writer.writeStartElement("property");
    writer.writeAttribute("name", name);
    writer.writeCharacters(value);
    writer.writeEndElement(); // property
  }

}
