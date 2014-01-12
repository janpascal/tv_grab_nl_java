#!/bin/sh

VERSION=$( xsltproc ../pom_version.xsl ../pom.xml )

GRABBER="java -jar ../target/tv_grab_nl_java-$VERSION-dep.jar"
CONFFILE="all.conf"
CACHEFILE="all.cache"

$GRABBER --configure --config-yes --config-file=${CONFFILE}
$GRABBER --cache $CACHEFILE --config-file ${CONFFILE} --days 31 --output tv.xml
