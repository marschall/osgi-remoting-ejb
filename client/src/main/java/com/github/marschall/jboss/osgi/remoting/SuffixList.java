package com.github.marschall.jboss.osgi.remoting;

import java.util.AbstractList;
import java.util.List;

final class SuffixList<E> extends AbstractList<E> {

  private final List<E> list;
  private final E suffix;


  SuffixList(List<E> list, E suffix) {
    this.list = list;
    this.suffix = suffix;
  }

  //TODO index of
  //TODO contains

  @Override
  public E get(int index) {
    if (index == this.list.size()) {
      return this.suffix;
    } else {
      return this.list.get(index);
    }
  }
  @Override
  public int size() {
    return this.list.size() + 1;
  }

}
