package com.github.marschall.jboss.osgi.remoting;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.Element;

final class EjbInfo {

  final String nonQualifiedClassName;
  
  final boolean stateful;
  
  final Set<String> remoteInterfaces;
  
  final Element originatingElement;

  EjbInfo(String nonQualifiedClassName, boolean stateful, List<String> remoteInterfaces, Element originatingElement) {
    this.nonQualifiedClassName = nonQualifiedClassName;
    this.stateful = stateful;
    this.originatingElement = originatingElement;
    // order interfaces
    this.remoteInterfaces = new TreeSet<String>(remoteInterfaces);
  }
  
  

}
