package com.github.marschall.jboss.osgi.remoting;

import javax.ejb.Remote;
import javax.ejb.Stateful;

@Stateful
@Remote({StatefulRemote1.class, StatefulRemote2.class})
public class StatefulBean implements StatefulNonRemote, StatefulRemote1, StatefulRemote2 {
  
  @Override
  public String statefulNonRemote() {
//    return "StatefulNonRemote";
    return com.github.marschall.jboss.osgi.remoting.ServiceXmlGenerator.class.getName();
  }
  
  @Override
  public String statefulRemote1() {
    return "StatefulRemote1";
  }

  @Override
  public String statefulRemote2() {
    return "StatefulRemote2";
  }


}
