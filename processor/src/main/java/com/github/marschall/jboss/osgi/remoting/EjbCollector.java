package com.github.marschall.jboss.osgi.remoting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

final class EjbCollector {

  private final List<EjbInfo> beans;

  private final ProcessingEnvironment processingEnv;

  private TypeElement stateless;

  private TypeElement stateful;

  private TypeElement singleton;

  private TypeElement remote;

  private Elements elements;


  EjbCollector(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
    this.beans = new ArrayList<EjbInfo>();
    
    this.elements = this.processingEnv.getElementUtils();
    
    this.stateless = this.elements.getTypeElement("javax.ejb.Stateless");
    this.stateful = this.elements.getTypeElement("javax.ejb.Stateful");
    this.singleton = this.elements.getTypeElement("javax.ejb.Singleton");
    this.remote = this.elements.getTypeElement("javax.ejb.Remote");
  }
  
  void processRound(RoundEnvironment roundEnv) {
    this.searchForBeansAnnotatedWith(roundEnv, this.stateless);
    this.searchForBeansAnnotatedWith(roundEnv, this.stateful);
    this.searchForBeansAnnotatedWith(roundEnv, this.singleton);
  }
  
  private void searchForBeansAnnotatedWith(RoundEnvironment roundEnv, TypeElement annotation) {
    boolean stateful = annotation.equals(this.stateful);
    Set<? extends Element> ejbs = roundEnv.getElementsAnnotatedWith(annotation);
    for (Element ejb : ejbs) {
      Messager messager = this.processingEnv.getMessager();
      messager.printMessage(Kind.NOTE, "processing ejb", ejb);
      AnnotationMirror remoteAnnotation = this.getRemoteAnnotation(ejb);
      if (remoteAnnotation != null) {
        // TODO get value of remote annotation
      } else {
        // TODO search for implemented interfaces
      }
    }
  }
  
  private AnnotationMirror getRemoteAnnotation(Element ejb) {
    List<? extends AnnotationMirror> mirrors = elements.getAllAnnotationMirrors(ejb);
    for (AnnotationMirror annotationMirror : mirrors) {
      Element annotationElement = annotationMirror.getAnnotationType().asElement();
      if (remote.equals(annotationElement)) {
        return annotationMirror;
      }
    }
    return null;
  }
  
}
