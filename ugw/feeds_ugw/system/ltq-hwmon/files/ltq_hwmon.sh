#!/bin/sh /etc/rc.common
START=10

KERNEL_VERSION=`uname -r`
modules_dir=/lib/modules/${KERNEL_VERSION} 
hwmon_file_name=hwmon.ko
hwmon_mod_name=hwmon
spdmon_file_name=spdmon.ko
spdmon_mod_name=spdmon
ina2xx_file_name=ina2xx.ko
ina2xx_mod_name=ina2xx
i2c_lantiq_file_name=i2c-lantiq.ko
i2c_lantiq_mod_name=i2c_lantiq 
i2c_dev_file_name=i2c-dev.ko
i2c_dev_mod_name=i2c_dev
i2c_core_file_name=i2c-core.ko
i2c_core_mod_name=i2c_core
i2c_algo_bit_file_name=i2c-algo-bit.ko
i2c_algo_bit_mod_name=i2c_algo_bit
of_i2c_file_name=of_i2c.ko
of_i2c_mod_name=of_i2c
i2c_gpio_file_name=i2c-gpio.ko
i2c_gpio_mod_name=i2c_gpio

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
	if [ A"$CONFIG_PACKAGE_KMOD_HWMON_CORE" = "A1" ]; then
		if [ -e ${modules_dir}/${hwmon_file_name} ];then
			module_exist ${hwmon_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${hwmon_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_HWMON_XRX500_SPDMON" = "A1" ]; then
		if [ -e ${modules_dir}/${spdmon_file_name} ];then
			module_exist ${spdmon_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${spdmon_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_CORE" = "A1" ]; then
		if [ -e ${modules_dir}/${i2c_core_file_name} ];then
			module_exist ${i2c_core_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${i2c_core_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_ALGO_BIT" = "A1" ]; then
		if [ -e ${modules_dir}/${i2c_algo_bit_file_name} ];then
			module_exist ${i2c_algo_bit_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${i2c_algo_bit_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_CORE" = "A1" ]; then
		if [ -e ${modules_dir}/${of_i2c_file_name} ] && 
			[ -e ${modules_dir}/${i2c_core_file_name} ] ;then
			module_exist ${of_i2c_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${of_i2c_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_GPIO" = "A1" ]; then
		if [ -e ${modules_dir}/${i2c_gpio_file_name} ];then
			module_exist ${i2c_gpio_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${i2c_gpio_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_LANTIQ" = "A1" ]; then
		if [ -e ${modules_dir}/${i2c_lantiq_file_name} ];then
			module_exist ${i2c_lantiq_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${i2c_lantiq_file_name};
			fi
		fi 
		if [ -e ${modules_dir}/${i2c_dev_file_name} ];then
			module_exist ${i2c_dev_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${i2c_dev_file_name};
			fi
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_HWMON_INA2XX" = "A1" ]; then
		if [ -e ${modules_dir}/${ina2xx_file_name} ];then
			module_exist ${ina2xx_mod_name}
			if [ ${ret_val} -ne 0 ];then
				insmod ${modules_dir}/${ina2xx_file_name};
			fi
		fi 
	fi
}

stop() {
	if [ A"$CONFIG_PACKAGE_KMOD_HWMON_XRX500_SPDMON" = "A1" ]; then
		module_exist ${spdmon_mod_name}
		if [ ${ret_val} -eq 0 ];then
			rmmod ${spdmon_mod_name};
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_HWMON_INA2XX" = "A1" ]; then
		module_exist ${ina2xx_mod_name}
		if [ ${ret_val} -eq 0 ];then
			rmmod ${ina2xx_mod_name};
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_HWMON_CORE" = "A1" ]; then
		module_exist ${hwmon_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${hwmon_mod_name};
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_LANTIQ" = "A1" ]; then
		module_exist ${i2c_lantiq_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${i2c_lantiq_mod_name};
		fi 
		module_exist ${i2c_dev_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${i2c_dev_mod_name};
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_GPIO" = "A1" ]; then
		module_exist ${i2c_gpio_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${i2c_gpio_mod_name};
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_ALGO_BIT" = "A1" ]; then
		module_exist ${i2c_algo_bit_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${i2c_algo_bit_mod_name};
		fi 
	fi
	if [ A"$CONFIG_PACKAGE_KMOD_I2C_CORE" = "A1" ]; then
		module_exist ${of_i2c_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${of_i2c_mod_name};
		fi 
		module_exist ${i2c_core_mod_name}
		if [ $? -eq 0 ];then
			rmmod ${i2c_core_mod_name};
		fi 
	fi
}
