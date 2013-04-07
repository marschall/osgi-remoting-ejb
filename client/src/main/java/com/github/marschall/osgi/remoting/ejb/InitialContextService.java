package com.github.marschall.osgi.remoting.ejb;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

// will be implemented by jboss specific implementation
public interface InitialContextService {

  // TODO identifier (eg. jboss) is a service property

  Hashtable<?,?> getEnvironment();

}
