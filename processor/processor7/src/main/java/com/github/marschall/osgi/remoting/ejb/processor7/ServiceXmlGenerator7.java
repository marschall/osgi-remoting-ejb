package com.github.marschall.osgi.remoting.ejb.processor7;

import static javax.lang.model.SourceVersion.RELEASE_7;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;

import com.github.marschall.osgi.remoting.ejb.processor.ServiceXmlGenerator;

@SupportedOptions({
  "javax.ejb.module.name",
  "javax.ejb.application.name",
  "org.jboss.distinct.name",
})
@SupportedAnnotationTypes({
  "javax.ejb.Stateful",
  "javax.ejb.Stateless",
  "javax.ejb.Singleton"
})
@SupportedSourceVersion(RELEASE_7)
public class ServiceXmlGenerator7 extends ServiceXmlGenerator {

}
