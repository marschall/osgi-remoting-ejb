OSGi Remoting EJB
=================
OSGi Remoting over EJB remoting [![Build Status](https://travis-ci.org/marschall/osgi-remoting-ejb.png?branch=master)](https://travis-ci.org/marschall/osgi-remoting-ejb)

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
* look up is in own thread to reduce impact on framework start up

Cons
----
* only one EJB provider

Authentication
--------------

Lazy Login
----------
* make unauthenticated class
* authenticate through client library (through proprietary API)
* look up com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher
* call com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher
* wait for method call to return
* no need to re-lookup OSGi services

Design Decisions
----------------
InitialContext per bundle

Generic Client
--------------
Needs to be started somehow (eg. autostart).

Adding a new Client Library
---------------------------
* have all reqired client libraries available as OSGi bundles (can be done through wrapping)
* implement com.github.marschall.osgi.remoting.ejb.api.InitialContextService
* make sure osgi-remoting-ejb-client is started (eg. Dynamic Services / Service Component Runtime)

EJB Client Bundles
-------------------
* need correct dependencies over Require-Bundle oder Import-Package (no dependency on client libs)
* need to have in Equinox
    Bundle-ActivationPolicy: lazy
