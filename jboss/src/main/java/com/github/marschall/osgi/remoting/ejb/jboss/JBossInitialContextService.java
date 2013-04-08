package com.github.marschall.osgi.remoting.ejb.jboss;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

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
  private static final Set<String> BUNDLE_IDS;
  
  static {
    Set<String> bundleIds = new HashSet<String>(PARENT_BUNDLE_IDS.length);
    for (String bundleId : PARENT_BUNDLE_IDS) {
      bundleIds.add(bundleId);
    }
    BUNDLE_IDS = Collections.unmodifiableSet(bundleIds);
  }

  @Override
  public Hashtable<?, ?> getEnvironment() {
    // TODO Auto-generated method stub
    // http://stackoverflow.com/questions/6244993/no-access-to-bundle-resource-file-osgi
    return null;
  }

  @Override
  public Set<String> getClientBundleSymbolicNames() {
    return BUNDLE_IDS;
  }


}
