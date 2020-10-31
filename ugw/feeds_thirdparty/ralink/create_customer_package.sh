#!/bin/sh

if [ ! -n "$1" ] ; then
        echo "Usage : create_customer_package.sh SOURCE_PATH"
        exit 0
fi

rm -rf *

mkdir -p ./os/linux/
install -m 777 $1/os/linux/rt5592ap.ko ./os/linux/
install -m 777 $1/RT2860AP.dat ./

echo -en "\nall:\n\techo -n\n\nclean:\n\techo -n\n" > Makefile
echo -en "\nall:\n\techo -n\n\nclean:\n\techo -n\n" > ./os/linux/Makefile.6
echo -en "\nall:\n\techo -n\n\nclean:\n\techo -n\n" > ./os/linux/config.mk

if [ -n "$FEEDDIR" ] && [ -d "$FEEDDIR/" ]; then
        rm -f $FEEDDIR/patches/*.patch
        rm -f "$FEEDDIR/create_customer_package.sh"
fi

