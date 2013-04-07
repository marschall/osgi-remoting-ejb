package com.github.marschall.osgi.remoting.ejb;

import java.util.ArrayList;
import java.util.List;

final class ParseResult {

  final List<ServiceInfo> services;

  ParseResult(List<ServiceInfo> services) {
    this.services = services;
  }

  boolean isEmpty() {
    return this.services.isEmpty();
  }

  int size() {
    return this.services.size();
  }

  static ParseResult flatten(List<ParseResult> results) {
    if (results.isEmpty()) {
      throw new IllegalArgumentException("collection must not be empty");
    }
    if (results.size() == 1) {
      return results.get(0);
    }

    int size = 0;
    for (ParseResult result : results) {
      size += result.size();
    }
    List<ServiceInfo> services = new ArrayList<ServiceInfo>(size);
    for (ParseResult result : results) {
      services.addAll(result.services);
    }
    return new ParseResult(services);
  }

}