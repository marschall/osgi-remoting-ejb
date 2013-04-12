OSGi Remoting EJB
=================
OSGi Remoting over EJB remoting [![Build Status](https://travis-ci.org/marschall/osgi-remoting-ejb.png?branch=master)](https://travis-ci.org/marschall/osgi-remoting-ejb)]

The purpose if this project is to allow you to call remote EJBs from within any OSGi container. The EJBs will be presented to you as (remote) OSGi services.

Contents
--------
* client magic
* annotation processor
* sample EJB and EAR
* sample glue code for JBoss and Glassfish


Pros
----
* no buddy class loading
* no dynamic import
* no dependencies EJB for client bundles or client code
* supports dynamic starting and stopping of EJB client bundles
* can with little effort support every EJB client library that uses the thread context classloader

Cons
----
* only one EJB proider

Authentication
--------------

Design Decisions
----------------
InitialContext per bundle

Generic Client
--------------
Needs to be started somehow (eg. autostart).

EJB Client Bundles
-------------------
* need correct dependencies over Require-Bundle oder Import-Package (no dependency on client libs)
* need to have in Equinox
    Bundle-ActivationPolicy: lazy
