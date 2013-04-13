OSGi Remoting EJB [![Build Status](https://travis-ci.org/marschall/osgi-remoting-ejb.png?branch=master)](https://travis-ci.org/marschall/osgi-remoting-ejb)
=================
OSGi Remoting over EJB remoting

The purpose if this project is to allow you to call remote EJBs from within any OSGi container. The EJBs will be presented to you as (remote) OSGi services. This project does not implement any remoting protocol instead it delegates to the corresponding, vendor dependent EJB client library.

At it's core the what this project does it sets the thread context classloader (TCCL) to an appropriate classloader during
* `new InitialContext()`
* `InitialContext#lookup()`
* method calls on objects retrieved trough the above JNDI look up (EJB service calls)

This makes it possible to run any EJB client library that builds on top of JNDI, which to my knowledge any every library does.

The TCCL used is a custom classloader that contains
* the classloader of the ejb-client JAR
* the classloader of the EJB client library bundles

To support an EJB client library only two things have to be provided
* the environment used to create the initial context
* the bundle symbolic names of the bundles making up the client library

In addition the ejb-client JARs have to be OSGi bundles with correct dependencies (minus the EJB client library).

Contents
--------
* client magic
* annotation processor
* sample EJB and EAR
* sample glue code for JBoss and Glassfish


Pros
----
The advantages of this project are
* supports dynamic starting and stopping of EJB client bundles at run time
* with little effort support every EJB client library that uses the thread context class loader can be supported
* the look up of service proxies (can involve network access) is in its own thread to reduce impact on framework start up

In addition the following "OSGi smells" are avoided
* [buddy class loading](http://wiki.eclipse.org/Context_Class_Loader_Enhancements#Buddy_Class_Loading)
* [DynamicImport-Package](http://wiki.osgi.org/wiki/DynamicImport-Package)
* dependencies from the ejb-client JARs to the EJB client library
* fragments

Cons
----
* only one EJB provider
* no dynamic discovery

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

Design Decisions/Trade Offs
---------------------------
To enable dynamic starting and stopping of ejb-client JARs an `InitialContext` per bundle is created (and closed). This adds a small overhead.

Deploying
---------
following bundles have to be auto started
* generic client
* specific client (eg. autostart)

Sample Client
-------------
Make sure you deploy `org.eclipse.equinox.ds`, `org.eclipse.equinox.util`
and `org.eclipse.equinox.console` `org.apache.gogo.shell` are recommended

Adding a new Client Library
---------------------------
* have all required client libraries available as OSGi bundles (can be done through wrapping)
* implement com.github.marschall.osgi.remoting.ejb.api.InitialContextService
* make sure osgi-remoting-ejb-client is started (eg. Dynamic Services / Service Component Runtime)

JBoss
-----
To make the JBoss client libraries provided with this project work the following steps have to be taken:
* add the following VM argument `-Dorg.osgi.framework.system.packages.extra=sun.nio.ch,sun.refelect`, this is independent of this project
* deploy the following bundles:
 * org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec
 * org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec
 * javax.xml.jaxrpc-api-osgi
 * org.jboss.logging.jboss-logging
* client library has to be deployed unpacked

OSGi Service Lookup
-------------------
When looking up the EJB service proxies though OSGi "manually" `ServiceTracker#open(true)` or `BundleContext#getAllServiceReferences` have to be used.

You can use the filter `(service.imported=*)` to be sure you only get remote services.

ejb-client JARs
---------------
* have to OSGi bundles with their (direct) dependencies specified through `Require-Bundle` or `Import-Package` but no dependencies on EJB client libraries
* when running in Equinox need to have the following bundle header `Bundle-ActivationPolicy: lazy`
* have to have an OSGi remote service XML in `OSGI-INF/remote-service` or a different location specified through the `Remote-Service` bundle header. The easiest way to do this is through the included annotation processor.

Open Issues
-----------
JBoss client library is twice the size it needs to be.

