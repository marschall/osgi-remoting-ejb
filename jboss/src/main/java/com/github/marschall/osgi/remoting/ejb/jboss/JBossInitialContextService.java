package com.github.marschall.osgi.remoting.ejb.jboss;

import java.util.Hashtable;

import com.github.marschall.osgi.remoting.ejb.InitialContextService;

public class JBossInitialContextService implements InitialContextService {
  
  // mvn dependency:copy-dependencies -DoutputDirectory=lib
  // unzip -c jboss-transaction-api_1.1_spec-1.0.1.Final.jar META-INF/MANIFEST.MF
  private static final String[] PARENT_BUNDLE_IDS = {
    // TODO jboss specific
    "org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec",
    "org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec",
    // missing SASL bundle
    // http://github.com/jboss/jboss-parent-pom/jboss-sasl
    // jboss-marshalling-river
    // jboss-marshalling
    // jboss-ejb-client
    // xnio-nio
    // xnio-api
    // http://github.com/jboss/jboss-parent-pom/xnio-all/xnio-api
  };

  @Override
  public Hashtable<?, ?> getEnvironment() {
    // TODO Auto-generated method stub
    return null;
  }


}
