#!/bin/bash

VERSION=$( xsltproc pom_version.xsl pom.xml )

if [ "$1" != "--testing" ]; then
  if ! head Changelog | grep -q "tv_grab_nl_java-$VERSION"; then
    echo "Release $VERSION not found in changelog, please update Changelog first";
    exit 1;
  fi
fi

FILENAME="tv_grab_nl_java-$VERSION.zip"
EXEFILENAME="Setup-tv_grab_nl_java-$VERSION.exe"
ZIPFILE="$PWD/../$FILENAME"
EXEFILE="$PWD/../$EXEFILENAME"

echo "Uploading $ZIPFILE"
perl ./upload.pl --location "$ZIPFILE" \
  --description "tv_grab_nl_java release $VERSION" \
  --content-type "application/zip"

echo "Uploading $EXEFILE"
perl ./upload.pl --location "$EXEFILE" \
  --description "tv_grab_nl_java release $VERSION (Windows installer)" \
  --content-type "application/exe"

