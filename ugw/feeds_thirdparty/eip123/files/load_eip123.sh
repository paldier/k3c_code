#!/bin/sh /etc/rc.common
#
# Install EIP-123 driver


START=86

drv_dev_base_name=umdevxs_c

start() {

		eip123_major=`grep $drv_dev_base_name /proc/devices |cut -d' ' -f1`
		[ ! -e /dev/$drv_dev_base_name ] && mknod /dev/$drv_dev_base_name c $eip123_major 0;
		if [ "$CONFIG_BUILD_IA_ONLY" = "1" ];then
			while [ 1 ]
			do
			/usr/sbin/tss
			done
		fi
}

