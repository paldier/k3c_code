#!/bin/sh /etc/rc.common
#
# Install icc driver node


START=84

drv_dev_icc_name=ltq_icc


start() {

		icc_major=`grep $drv_dev_icc_name /proc/devices |cut -d' ' -f1`
		[ ! -e /dev/$drv_dev_icc_name ] && mknod /dev/$drv_dev_icc_name c $icc_major 0;
		sleep 2;

}

