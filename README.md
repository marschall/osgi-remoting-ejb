OSGi Remoting EJB [![Build Status](https://travis-ci.org/marschall/osgi-remoting-ejb.png?branch=master)](https://travis-ci.org/marschall/osgi-remoting-ejb)
=================
OSGi Remoting over EJB remoting

The purpose if this project is to allow you to call remote EJBs from within any OSGi container. The EJBs will be presented to you as (remote) OSGi services. This project does not implement any remoting protocol instead it delegates to the corresponding, vendor dependent EJB client library.

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

Sample Client
-------------
Make sure you deploy `org.eclipse.equinox.ds`, `org.eclipse.equinox.util`
and `org.eclipse.equinox.console` `org.apache.gogo.shell` are recommended

Adding a new Client Library
---------------------------
* have all reqired client libraries available as OSGi bundles (can be done through wrapping)
* implement com.github.marschall.osgi.remoting.ejb.api.InitialContextService
* make sure osgi-remoting-ejb-client is started (eg. Dynamic Services / Service Component Runtime)

JBoss
-----
To make the JBoss client libraries work optimal you need to add the following argument. This is independent of this project.

* add the following VM argument `-Dorg.osgi.framework.system.packages.extra=sun.nio.ch,sun.refelect`
* deploy the following bundles:
 * org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec
 * org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec
 * javax.xml.jaxrpc-api-osgi
 * org.jboss.logging.jboss-logging

EJB Client Bundles
-------------------
* need correct dependencies over Require-Bundle oder Import-Package (no dependency on client libs)
* need to have in Equinox
    Bundle-ActivationPolicy: lazy
