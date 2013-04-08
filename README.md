OSGi Remoting EJB
=================
OSGi Remoting over EJB remoting

The purpose if this project is to allow you to call remote EJBs from within any OSGi container. The EJBs will be presented to you as (remote) OSGi services.

Contents
--------
* client magic
* annotation processor
* sample EJB and EAR
* sample glue code for JBoss and Glassfish

Authentication
--------------

Design Decisions
----------------
InitialContext per bundle
 