package com.github.marschall.osgi.remoting.ejb.processor6;

import static javax.lang.model.SourceVersion.RELEASE_6;

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
@SupportedSourceVersion(RELEASE_6)
public class ServiceXmlGenerator6 extends ServiceXmlGenerator {

}
