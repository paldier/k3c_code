#!/bin/sh /etc/rc.common
START=82

KERNEL_VERSION=`uname -r`
modules_dir=/lib/modules/${KERNEL_VERSION} 
consumer_file_name=ltq_regulator_consumer.ko 

start() {
		if [ -e ${modules_dir}/${consumer_file_name} ];then
			insmod ${modules_dir}/${consumer_file_name};
		else
			echo "${modules_dir}/${consumer_file_name} not found"
			exit 1
		fi 
}

stop() {
		echo "Remove REGULATOR CONSUMER kernel module"
		rmmod ${modules_dir}/${consumer_file_name};
}