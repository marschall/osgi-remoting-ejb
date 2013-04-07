package com.github.marschall.osgi.remoting.ejb;

final class ServiceInfo {

  final String interfaceName;
  final String jndiName;

  ServiceInfo(String interfaceName, String jndiName) {
    this.interfaceName = interfaceName;
    this.jndiName = jndiName;
  }

}