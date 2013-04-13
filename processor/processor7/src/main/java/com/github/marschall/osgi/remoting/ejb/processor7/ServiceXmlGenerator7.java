package com.github.marschall.osgi.remoting.ejb.processor7;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;

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
// TODO
//@SupportedSourceVersion(RELEASE_7)
public class ServiceXmlGenerator7 extends ServiceXmlGenerator {

}
