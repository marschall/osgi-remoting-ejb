package com.github.marschall.jboss.osgi.remoting;

import static javax.lang.model.SourceVersion.RELEASE_6;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedOptions({
  "javax.ejb.module.name",
  "javax.ejb.application.name"
})
// TODO
//  application name
//  module name
//  implenetation prefix (eg. jboss)
@SupportedSourceVersion(RELEASE_6)
@SupportedAnnotationTypes({
  "javax.ejb.Stateful",
  "javax.ejb.Stateless",
  "javax.ejb.Singleton"
})
public class ServiceXmlGenerator extends AbstractProcessor {
  
  static final String MODULE_NAME_OPTION = "javax.ejb.module.name";
  static final String APPLICATION_NAME_OPTION = "javax.ejb.application.name";
  
  private EjbCollector collector;
  private String applicationName;
  private String moduleName;

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
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.errorRaised()) {
      return false;
    } else if (roundEnv.processingOver()) {
      this.processingEnv.getMessager().printMessage(Kind.NOTE, "module name: " + moduleName);
      this.processingEnv.getMessager().printMessage(Kind.NOTE, "application name: " + applicationName);
      
      for (EjbInfo ejb : this.collector.beans) {
        this.processingEnv.getMessager().printMessage(Kind.NOTE, ejb.nonQualifiedClassName
            + (ejb.stateful ? "(stateful)" : "")
            + " implements " + ejb.remoteInterfaces,
            ejb.originatingElement);
      }
      return false;
    } else {
      this.collector.processRound(roundEnv);
      return true;
    }
  }

}
