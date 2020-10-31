#!/bin/sh /etc/rc.common
START=76

KERNEL_VERSION=`uname -r`
modules_dir=/lib/modules/${KERNEL_VERSION} 
hwmon_file_name=hwmon.ko 
hwmon_mod_name=hwmon
temp_file_name=ltq_temp.ko 
temp_mod_name=ltq_temp

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

		if [ -e ${modules_dir}/${hwmon_file_name} ];then
			module_exist ${hwmon_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${hwmon_file_name};
			fi
		fi 

		if [ -e ${modules_dir}/${temp_file_name} ];then
			module_exist ${temp_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${temp_file_name};
			fi
		fi 
}

stop() {
		echo "Remove Lantiq Temperature Sensor"
		rmmod ${modules_dir}/${temp_file_name};
		rmmod ${modules_dir}/${hwmon_file_name};
}