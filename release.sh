#!/bin/bash

VERSION=$( xsltproc pom_version.xsl pom.xml )

if [ "$1" != "--testing" ]; then
  if ! head Changelog | grep -q "tv_grab_nl_java-$VERSION"; then
    echo "Release $VERSION not found in changelog, please update Changelog first";
    exit 1;
  fi
fi

tmpdir=$( mktemp -d ) || exit 1
DESTDIR="$tmpdir/tv_grab_nl_java-$VERSION"
ZIPFILE="$PWD/../tv_grab_nl_java-$VERSION.zip"

mkdir "$DESTDIR"
rm -f "$ZIPFILE"

mvn compiler:compile
mvn test
if [ "$?" != 0 ]; then
  echo "Test failed, aborting"
  exit 1;
fi
mvn package
mvn assembly:single
cp target/tv_grab_nl_java-$VERSION-dep.jar "$DESTDIR/tv_grab_nl_java.jar"
cp tv_grab_nl_java install.sh README LICENSE Changelog "$DESTDIR"

pushd "$tmpdir"
zip -r "$ZIPFILE" *
popd

makensis -DVERSION=$VERSION tv_grab_nl_java.nsi

rm -rf $tmpdir
