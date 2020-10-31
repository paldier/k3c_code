#!/bin/sh /etc/rc.common
#
# Install EIP-97 driver


START=83

KERNEL_VERSION=`uname -r`
KERNEL_MAJOR=`uname -r | cut -f1,2 -d. | sed -e 's/\.//'`

# check for Linux 2.6 or higher
if [ $KERNEL_MAJOR -ge 26 ]; then
	MODEXT=.ko
fi

modules_dir=/lib/modules/${KERNEL_VERSION}
drv_obj_crypt=ltq_crypto$MODEXT
drv_obj_crypt_test=ltq_module_test$MODEXT


start() {
	[ -e ${modules_dir}/${drv_obj_crypt} ] && {
		insmod ${modules_dir}/${drv_obj_crypt};
	}
	[ -e ${modules_dir}/${drv_obj_crypt_test} ] && {
		insmod ${modules_dir}/${drv_obj_crypt_test};
	}
}

