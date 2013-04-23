/*
 * Copyright (C) 2013 by Netcetera AG.
 * All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of Netcetera AG, Switzerland.
 * The program(s) may be used and/or copied only with the written permission of Netcetera AG or
 * in accordance with the terms and conditions stipulated in the agreement/contract under which 
 * the program(s) have been supplied.
 */
package com.github.marschall.osgi.remoting.ejb.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class SettableFuture<T> implements Future<T> {

  private volatile T value;
  
  private final CountDownLatch countDownLatch;
  
  SettableFuture() {
    this.countDownLatch = new CountDownLatch(1);
  }
  
  void setValue(T value) {
    this.value = value;
    this.countDownLatch.countDown();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public T get() throws InterruptedException {
    this.countDownLatch.await();
    return this.value;
  }

  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException {
    this.countDownLatch.await(timeout, unit);
    return this.value;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return this.countDownLatch.getCount() == 0;
  }
  
}