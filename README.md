OSGi Remoting EJB [![Build Status](https://travis-ci.org/marschall/osgi-remoting-ejb.png?branch=master)](https://travis-ci.org/marschall/osgi-remoting-ejb)
=================
OSGi Remoting over EJB remoting

The purpose if this project is allowing to call remote EJBs from within any OSGi container. The EJBs will be presented as (remote) OSGi services. This project does not implement any remoting protocol instead it delegates to the corresponding, vendor dependent EJB client library.

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
This project includes the following components:
* the core, sever independent library that switches the TCCL and registers the OSGi services
* a Java 6 annotation processor processor that generates the required service.xml files for the ejb-client JARs
* sample integration for JBoss
* a sample JBoss EJB client library bundle
* a sample EJB
* a sample EAR
* a sample client

It's important that the provided JBoss integration is just a sample and can be replaced by one that fits custom needs.

Pros
----
The advantages of this project are:
* supports dynamic starting and stopping of EJB client bundles at run time
* with little effort support every EJB client library that uses the thread context class loader can be supported
* the look up of service proxies (can involve network access) is in its own thread to reduce impact on framework start up

In addition the following "OSGi smells" are avoided:
* [buddy class loading](http://wiki.eclipse.org/Context_Class_Loader_Enhancements#Buddy_Class_Loading)
* [DynamicImport-Package](http://wiki.osgi.org/wiki/DynamicImport-Package)
* dependencies from the ejb-client JARs to the EJB client library
* fragments

Cons
----
Only one EJB client library is supported at runtime, starting, stopping and restarting this library is not supported.

Annotation Processor
--------------------
A Java 6 annotation processor is provided that generates OSGi Remoting service XML files that follow Java EE 6 portable JNDI syntax

	app-name/module-name/bean-name!bean-interface

To use the processor
* a dependency on `com.github.marschall:osgi-remoting-ejb-processor6` with scope `provided` has to be specified in the EJB project
* the processor argument `javax.ejb.module.name` has to be set to the module name
* the processor argument `javax.ejb.application.name` has to be set to the application name

In addition when the argument `org.jboss.distinct.name` is set (can be empty) then JBoss proprietary EJB names are generated

	ejb:app-name/module-name/distinct-name/bean-name!fully-qualified-classname-of-the-remote-interface(?stateful)

A concrete example can be found in osgi-remoting-ejb-sample-ejb.

Authentication
--------------
There is not API for doing authentication as it's not covered by OSGi Remoting. Authentication has to be done through the proprietary EJB client library API.

Lazy Login
----------
There is support for starting with unauthenticated calls first, then login in and from then on performing only authenticated calls. The following steps have to be taken
* make unauthenticated calls
* authenticate through client library (through proprietary EJB client library API)
* look up `com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher`
* call `com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher#flushProxies()`
* wait for method call to return
* no need to re-lookup OSGi services, they stay valid and use authenticated calls not

Design Decisions/Trade Offs
---------------------------
To enable dynamic starting and stopping of ejb-client JARs an `InitialContext` per bundle is created (and closed). This adds a small overhead.

Deploying
---------
When deploying attention has to be paid that
* the bundle `osgi-remoting-ejb-client` is started automatically
* an implementation of `com.github.marschall.osgi.remoting.ejb.api.InitialContextService` (eg. `osgi-remoting-ejb-client`) is registered (eg. through Dynamic Services / Service Component Runtime)

Adding a new Client Library
---------------------------
To add support for a new EJB client library the following things have to be provided
* all required EJB client libraries as OSGi bundles (can be done through wrapping)

Sample Client on Equinox
------------------------
When you deploy the sample client on Equinox make sure you deploy the bundles `org.eclipse.equinox.ds` and `org.eclipse.equinox.util`. In addition it's recommended that you deploy the bundles `org.eclipse.equinox.console` and `org.apache.gogo.shell`. In addition `osgi-remoting-ejb-client` has to be auto stared.

JBoss
-----
To make the JBoss client libraries provided with this project work the following steps have to be taken:
* the following VM argument has to be set `-Dorg.osgi.framework.system.packages.extra=sun.nio.ch,sun.refelect`
* the following bundles have to be deployed:
 * org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec
 * org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec
 * javax.xml.jaxrpc-api-osgi
 * org.jboss.logging.jboss-logging
* client library (osgi-remoting-ejb-jboss-client) has to be deployed unpacked

OSGi Service Lookup
-------------------
When looking up the EJB service proxies though OSGi "manually" `ServiceTracker#open(true)` or `BundleContext#getAllServiceReferences` have to be used.

You can use the filter `(service.imported=*)` to be sure you only get remote services.

ejb-client JARs
---------------
ejb-client JARs deployed in OSGi have to meet the following requirements
* have to be OSGi bundles with their (direct) dependencies specified through `Require-Bundle` or `Import-Package` but no dependencies on EJB client libraries
* when running in Equinox need to have the following bundle header `Bundle-ActivationPolicy: lazy`
* have to have an OSGi remote service XML in `OSGI-INF/remote-service` or a different location specified through the `Remote-Service` bundle header. The easiest way to do this is through the included annotation processor.

Open Issues
-----------
* JBoss client library is twice the size it needs to be.
* GlassFish library is not complete

