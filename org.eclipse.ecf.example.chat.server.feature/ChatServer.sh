#!/bin/bash

BASEDIR=$(dirname $0)

test -e plugins/org.eclipse.ecf.server.generic_*.jar && rm plugins/org.eclipse.ecf.server.generic_*.jar
test -e plugins/org.eclipse.ecf.remoteservice.servlet_*.jar && rm plugins/org.eclipse.ecf.remoteservice.servlet_*.jar

test -e plugins/org.eclipse.osgi_*.jar && mv plugins/org.eclipse.osgi_*.jar $BASEDIR
test -e plugins/org.eclipse.equinox.simpleconfigurator_*.jar && mv plugins/org.eclipse.equinox.simpleconfigurator_*.jar $BASEDIR

java -Dzoodiscovery.flavor=zoodiscovery.flavor.centralized=disco.ecf-project.org -Dzoodiscovery.clientPort=2181 -Dzoodiscovery.consoleLog -Dzoodiscovery.autoStart -Dch.ethz.iks.r_osgi.topic.filter=org/eclipse/e4/* -Declipse.ignoreApp=true -Dosgi.noShutdown=true -jar $BASEDIR/org.eclipse.osgi_*.jar -console -consoleLog 
