package com.github.marschall.jboss.osgi.remoting;

import java.util.List;

final class EjbInfo {

  final String nonQualifiedClassName;
  
  final boolean stateful;
  
  final List<String> remoteInterfaces;

  EjbInfo(String nonQualifiedClassName, boolean stateful,
      List<String> remoteInterfaces) {
    this.nonQualifiedClassName = nonQualifiedClassName;
    this.stateful = stateful;
    this.remoteInterfaces = remoteInterfaces;
  }
  
  

}
