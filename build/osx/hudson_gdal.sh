#!/bin/bash

# Script directory
d=`dirname $0`

# Load versions
source ${d}/hudson_config.sh

function usage() {
  echo "Usage: $0 <srcdir> <destdir>"
  exit 1
}

if [ $# -lt 1 ]; then
    usage
fi

srcdir=$1

if [ "x$2" = "x" ]; then
  destdir=$webroot
else
  destdir=$2
fi
 
if [ ! -d $srcdir ]; then
  echo "Source directory is missing."
  exit 1
else 
  pushd $srcdir
fi

./autogen.sh
export CXX=g++-4.0 
export CC=gcc-4.0 
export CXXFLAGS="-O2 -arch i386 -arch ppc -mmacosx-version-min=10.4" 
export CFLAGS="-O2 -arch i386 -arch ppc -mmacosx-version-min=10.4" 
export ARCHFLAGS="-arch ppc -arch i386"
./configure --prefix=${buildroot}/gdal --with-curl=/usr/bin/curl-config
make clean && make all
# Make sure the Java SWIG wrapper can find our java headers
sed -i -e 's:^JAVA_HOME.*:JAVA_HOME=/Library/Java/Home:' swig/java/java.opt

# Build MrSID plugin
g++-4.0 -g frmts/mrsid/*.cpp -dynamiclib -o gdal_MrSID.dylib \
-O2 -arch ppc -arch i386 -mmacosx-version-min=10.4 \
-DOGR_ENABLED -D_REENTRANT -DMRSID_J2K -fPIC -DPIC \
-Ifrmts/gtiff/libgeotiff/ -Igcore -Iogr -Iport -I${buildroot}/Raster_DSDK/include \
-L${buildroot}/Raster_DSDK/lib -L.libs \
-lgdal -lltidsdk -lpthread -ldl
# Build Java SWIG bindings
(cd swig/java; make)
checkrv $? "GDAL build"

rm -rf ${buildroot}/gdal
mkdir ${buildroot}/gdal
make install
# Install MrSID plugin
mkdir -p ${buildroot}/gdal/lib/gdalplugins
cp gdal_MrSID.dylib ${buildroot}/gdal/lib/gdalplugins
cp ${buildroot}/Raster_DSDK/lib/*.dylib ${buildroot}/gdal/lib
# Install Java SWIG bindings
cp swig/java/.libs/*.dylib ${buildroot}/gdal/lib
pushd ${buildroot}/gdal
rm -f ${destdir}/gdal-osx.zip
zip -r9 ${destdir}/gdal-osx.zip *
checkrv $? "GDAL zip"
popd

popd

exit 0
    
