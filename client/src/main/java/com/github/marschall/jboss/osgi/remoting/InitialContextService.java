package com.github.marschall.jboss.osgi.remoting;

import javax.naming.Context;
import javax.naming.NamingException;

// will be implemented by jboss specific implementation
public interface InitialContextService {

  // TODO identifier (eg. jboss) is a service property

  Context getInitialContext() throws NamingException;

}
