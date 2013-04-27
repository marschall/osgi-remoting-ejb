OSGi Remoting EJB [![Build Status](https://travis-ci.org/marschall/osgi-remoting-ejb.png?branch=master)](https://travis-ci.org/marschall/osgi-remoting-ejb)
=================
OSGi Remoting over EJB remoting

The purpose of this project is allowing to call remote EJBs from within any OSGi container. The EJBs will be presented as (remote) OSGi services. This project does not implement any remoting protocol, instead it delegates to the corresponding, vendor dependent EJB client library.

At its core the what this project does is setting the thread context classloader (TCCL) to an appropriate classloader during
* `new InitialContext()`
* `InitialContext#lookup()`
* method calls on objects retrieved trough the above JNDI look up (EJB service calls)

This makes it possible to run any EJB client library that builds on top of JNDI.

The TCCL is set to a custom classloader that contains:
* the classloader of the ejb-client JAR
* the classloader of the EJB client library bundles

To support an EJB client library only two things have to be provided
* the environment used to create the `InitialContext`
* the bundle symbolic names of the bundles that make up the EJB client library

In addition the ejb-client JARs have to be OSGi bundles with correct dependencies (minus the EJB client library) and OSGi Remoting service.xml files that map the service interfaces to JNDI names.

Contents
--------
This project includes the following components:
* core, sever independent library that switches the TCCL and registers the OSGi services
* Java 6 and 7 annotation processors that generate the required service.xml files for the ejb-client JARs
* sample integration for JBoss
* sample JBoss EJB client library bundle
* sample EJB
* sample EAR
* sample client

The provided JBoss integration is just a sample (eg. only supports localhost) and can be replaced by one that fits custom needs.

Pros
----
The advantages of this project are:
* supports dynamic starting and stopping of EJB client bundles at run time
* with little effort support every EJB client library that uses JNDI can be supported
* remote EJBs can be injected using [OSGi Declarative Services](http://wiki.osgi.org/wiki/Declarative_Services)
* remote EJBs can be injected using [Eclipse 4 Dependency Injection](http://wiki.eclipse.org/Eclipse4/RCP/Dependency_Injection)
* client code has no dependencies on EJB libraries
* the look up of service proxies (can involve network access) is in its own thread to reduce impact on framework start up

In addition the following «OSGi smells» are avoided:
* [buddy classloading](http://wiki.eclipse.org/Context_Class_Loader_Enhancements#Buddy_Class_Loading)
* [DynamicImport-Package](http://wiki.osgi.org/wiki/DynamicImport-Package)
* dependencies from the ejb-client JARs to the EJB client library
* one huge bundle containing all ejb-client JARs
* fragments

Cons
----
Only one EJB client library is supported at runtime — starting, stopping and restarting this library is not supported.

Annotation Processor
--------------------
Both a Java 6 and a 7 annotation processor are provided that generate OSGi Remoting service.xml files that follow Java EE 6 portable JNDI syntax

	app-name/module-name/bean-name!bean-interface

To use the processor
* a dependency on `com.github.marschall:osgi-remoting-ejb-processor6` or `com.github.marschall:osgi-remoting-ejb-processor7` with scope `provided` has to be specified in the EJB project
* the processor argument `javax.ejb.module.name` has to be set to the module name
* the processor argument `javax.ejb.application.name` has to be set to the application name

In addition when the argument `org.jboss.distinct.name` is set (can be empty) then JBoss proprietary EJB names are generated

	ejb:app-name/module-name/distinct-name/bean-name!fully-qualified-classname-of-the-remote-interface(?stateful)

A concrete example can be found in osgi-remoting-ejb-sample-ejb.

Authentication
--------------
As OSGi Remoting does not cover authentication no API for authentication is provided. Authentication has to be done through the proprietary EJB client library API.

Lazy Login
----------
There is support for starting with unauthenticated calls first, logging in and from then on performing only authenticated calls. The following steps have to be taken
* make unauthenticated calls
* authenticate through client library (proprietary EJB client library API)
* look up `com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher`
* call `com.github.marschall.osgi.remoting.ejb.api.ProxyFlusher#flushProxies()`
* wait for method call to return
* no need to re-lookup OSGi services, they stay valid and use authenticated calls not

Design Decisions/Trade Offs
---------------------------
To enable dynamic starting and stopping of ejb-client JARs an `InitialContext` per bundle is created (and closed). This adds a small overhead.

The methods `#equals(Object)`, `#hashCode()` and `#toString()` are not intercepted by the OSGi service but instead forwarded to the EJB client proxy.

> ### 3.4.7 Session Object Identity ###
> 
> A client can test two EJB 3.x Remote/Local view references for identity by means of the `Object.equals` and `Object.hashCode` methods.

Deploying
---------
When deploying care has to be taken that that
* the bundle `osgi-remoting-ejb-client` is started automatically
* an implementation of `com.github.marschall.osgi.remoting.ejb.api.InitialContextService` (eg. `osgi-remoting-ejb-client`) is registered (eg. through Dynamic Services / Service Component Runtime)

Sample Client on Equinox
------------------------
When deploying the sample client on Equinox the bundles `org.eclipse.equinox.ds` and `org.eclipse.equinox.util` have to be deployed. In addition it's recommended to deploy the bundles `org.eclipse.equinox.console` and `org.apache.gogo.shell`. In addition `osgi-remoting-ejb-client` has to be auto stared.

JBoss
-----
To make the JBoss client libraries provided with this project work the following steps have to be taken:
* the following VM argument has to be set `-Dorg.osgi.framework.system.packages.extra=sun.nio.ch,sun.refelect`
* the following bundles have to be deployed:
 * org.jboss.spec.javax.transaction.jboss-transaction-api_1.1_spec
 * org.jboss.spec.javax.ejb.jboss-ejb-api_3.1_spec
 * javax.xml.jaxrpc-api-osgi
 * org.jboss.logging.jboss-logging
* the client library (osgi-remoting-ejb-jboss-client) has to be deployed unpacked

OSGi Service Lookup
-------------------
When looking up the EJB service proxies though OSGi "manually" `ServiceTracker#open(true)` or `BundleContext#getAllServiceReferences` have to be used.

The filter `(service.imported=*)` can be used to ensure only imported remote services are found.

ejb-client JARs
---------------
ejb-client JARs deployed in OSGi have to meet the following requirements
* valid OSGi bundles with (direct) dependencies specified through `Require-Bundle` or `Import-Package` 
* no dependencies on EJB client libraries
* when running in Equinox the bundle header `Bundle-ActivationPolicy: lazy` has to be set
* an OSGi remote service.xml in `OSGI-INF/remote-service` or a different location specified through the `Remote-Service` bundle header. The easiest way to do this is through the included annotation processor.

Requirements
------------
This project requires at least Java 6.

Caveats
-------
You must not make any service calls from an OSGi callback thread (`BundleListener`, `ServiceListener`, `ServiceTrackerCustomizer`, …).

You should not make any service calls from the UI thread.

Service proxies can only be registered once the sole `InitialContextService` is registered because it's required for defining proper class loader.

Customization
-------------
To add custom features like call logging or exception handling implementing a `ServiceListener` and wrapping the services is recommended.

Open Issues
-----------
GlassFish client library is not yet implemented.

Geronimo client library depends on [TOMEE-903](https://issues.apache.org/jira/browse/TOMEE-903) being fixed.

An ESA (Enterprise Subsystem Archive) would be nice http://coderthoughts.blogspot.ch/2013/04/osgi-subsystems.html http://svn.apache.org/repos/asf/aries/trunk/esa-maven-plugin/pom.xml 

