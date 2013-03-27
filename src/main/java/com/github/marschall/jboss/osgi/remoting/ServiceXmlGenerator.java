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

@SupportedOptions({""})
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
  
  public ServiceXmlGenerator() {
    super();
  }
  
  @Override
  public void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    Map<String, String> options = processingEnv.getOptions();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.errorRaised()) {
      return false;
    } else if (roundEnv.processingOver()) {
      // write xml
      return false;
    } else {
      // collect data
      return true;
    }
  }

}
