#!/bin/sh /etc/rc.common
START=77

KERNEL_VERSION=`uname -r`
modules_dir=/lib/modules/${KERNEL_VERSION} 
pmcu_file_name=ltq_pmcu.ko 

# checks if the given module is already loaded
# : module_exist <module_name>
module_exist()
{
	#echo "module_exists: $1"
	ret_val=1
	output=$(lsmod | grep -q $1)
	if [ $? -eq 0 ];then
		ret_val=0
	fi
}

start() {
	if [ A"$CONFIG_PACKAGE_LTQ_PMCU" = "A1" ]; then
		if [ -e ${modules_dir}/${pmcu_file_name} ];then
			module_exist ${pmcu_file_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${pmcu_file_name};
			fi
		fi 
		#echo "Create PMCU device node"
		mknod /dev/pmcu c $(sed 's/:/ /g' /sys/class/misc/ltq_pmcu/dev)
	fi
}

stop() {
		echo "Remove PMCU kernel module"
		rmmod ${modules_dir}/${pmcu_file_name};
		echo "Remove PMCU device node"
		rm /dev/pmcu
}