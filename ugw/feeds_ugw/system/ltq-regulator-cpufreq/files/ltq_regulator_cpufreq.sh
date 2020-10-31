#!/bin/sh /etc/rc.common
START=85

KERNEL_VERSION=`uname -r`
modules_dir=/lib/modules/${KERNEL_VERSION} 
consumer_file_name=ltq_regulator_cpufreq.ko 

start() {
		if [ -e ${modules_dir}/${consumer_file_name} ];then
			insmod ${modules_dir}/${consumer_file_name};
		else
			echo "${modules_dir}/${consumer_file_name} not found"
			exit 1
		fi 
}

stop() {
		rmmod ${modules_dir}/${consumer_file_name};
}