#!/bin/bash

VERSION=0.5.1

tmpdir=$( mktemp -d ) || exit 1
DESTDIR="$tmpdir/tv_grab_nl_java-$VERSION"
ZIPFILE="$PWD/../tv_grab_nl_java-$VERSION.zip"

mkdir "$DESTDIR"
rm -f "$ZIPFILE"

mvn compile
mvn assembly:single
cp target/tv_grab_nl_java-$VERSION-dep.jar "$DESTDIR/tv_grab_nl_java.jar"
cp tv_grab_nl_java README Changelog "$DESTDIR"

pushd "$tmpdir"
zip -r "$ZIPFILE" *
popd

rm -rf $tmpdir
