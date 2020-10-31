#!/bin/sh /etc/rc.common

[ -n "$CONFIG_NEW_FRAMEWORK" ] && {
	USE_PROCD=1
}
START=98

PMUTILROOT="/opt/lantiq/bin"
bin_dir=/opt/lantiq/bin 
ppmd_bin=ppmd 

start_service() {
	if [ A"$CONFIG_LTQ_CPUFREQ_ENABLED" = "A1" ]; then 
		if [ -f $PMUTILROOT/pm_util ]; then
			${bin_dir}/pm_util -yon&
		fi
	fi
	if [ A"$CONFIG_LTQ_DVS_ENABLED" = "A1" ]; then 
		if [ -f $PMUTILROOT/pm_util ]; then
			${bin_dir}/pm_util -xon&
		fi
	fi
	if [ A"$CONFIG_LTQ_SCALE_DOWN_ON_HIGH_TEMP" = "A1" ] ||
		[ A"$CONFIG_LTQ_TEMP_EMER_SHUT_DOWN" = "A1" ]; then 
			if [  "$(ps | grep -c ppmd)" -eq 1 ]; then
				echo "Power Policy Management Daemon running" 
				${bin_dir}/${ppmd_bin} -sp1000
				if [ -n "$CONFIG_NEW_FRAMEWORK" ]; then
					procd_open_instance
					procd_set_param command ${bin_dir}/${ppmd_bin} -o
					procd_set_param respawn
					procd_close_instance
				else
					${bin_dir}/${ppmd_bin} -o& 
				fi
			fi
	fi
}

start() {
	start_service;
}

stop() {
	if [ A"$CONFIG_LTQ_SCALE_DOWN_ON_HIGH_TEMP" = "A1" ] ||
		[ A"$CONFIG_LTQ_TEMP_EMER_SHUT_DOWN" = "A1" ]; then 
		if [ -e ${bin_dir}/${ppmd_bin} ];then
			echo "Power Policy Management Daemon disabled" 
			${bin_dir}/${ppmd_bin} -sp0
			if [ -n "$CONFIG_NEW_FRAMEWORK" ]; then
				procd_kill pm_util.sh ''
			else
				killall ${ppmd_bin}
			fi
		else
			echo "${bin_dir}/${ppmd_bin} not found"
			exit 1
		fi 
	fi

}
