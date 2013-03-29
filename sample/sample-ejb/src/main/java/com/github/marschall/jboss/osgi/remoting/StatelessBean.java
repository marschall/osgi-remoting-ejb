package com.github.marschall.jboss.osgi.remoting;

import javax.ejb.Stateless;

@Stateless
public class StatelessBean implements StatelessNonRemote, StatelessRemote1, StatelessRemote2 {
  
  @Override
  public String statelessNonRemote() {
    return "StatelessNonRemote";
  }

  @Override
  public String statelessRemote1() {
    return "StatelessRemote1";
  }

  @Override
  public String statelessRemote2() {
    return "StatelessRemote2";
  }


}
