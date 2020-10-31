#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

command=$1
driver_mode=$2
object_index=$3
wave_count=$4

insmod_device()
{
	cd /tmp	
	insmod $@
	# verify that insmod was successful
	if [ `lsmod | grep "mtlk " -c` -eq 0  ]
	then
		print2log ALERT "fapi_wlan_wave_init_driver.sh: driver insmod is failed"
		echo "fapi_wlan_wave_init_driver.sh: driver insmod is failed" >> $wave_init_failure
		exit
        fi

	cd - > /dev/null
}

rmmod_device()
{
	device=$1

	# In mtlkroot.ko insmod we pass to the driver the Netlink ID to reuse it on Netlink create.
	# Therefore, before removing driver, use iwpriv to read the ID that the kernel allocated, save it in file.
	mtlk_genl_family_id=`api_wrapper get iwpriv wlan0 gGenlFamilyId \| awk -F \":\" \'{print \\$2}\'`
	print2log DBG "fapi_wlan_wave_init_driver.sh:  after rmmod, mtlk_genl_family_id=$mtlk_genl_family_id"
	if [ "$mtlk_genl_family_id" != "" ]
	then
		echo $mtlk_genl_family_id > $GENL_FAMILY_ID_FILE
	fi

	rmmod $device	
	res=$?
	# verify that rmmod was successful
	count=0
	while [ $res != 0 ]
	do
		if [ $count -lt 3 ]
		then
			rmmod $device
			res=$?
			sleep 2
		else
			print2log ALERT "fapi_wlan_wave_init_driver.sh: rmmod of $device is failed"
			print2log INFO "fapi_wlan_wave_init_driver.sh: Kill drvhlpr process before rmmod of $device"
			break
		fi
		count$((count+1))
	done		
}

start_insmod_wls_driver() {	
	print2log DBG "fapi_wlan_wave_init_driver.sh: Start"

	fw_logger_severity=`get_conf_param FwLoggerSeverity $object_index wlan0`
	# Broadband CPU frequency is needed for GRX350 only
	# TODO: config.sh will probably be moved to: /opt/lantiq/etc/rc.d/config.sh or somewhere similar
	. /etc/config.sh
	[ "$CONFIG_IFX_MODEL_NAME" = "GRX350_GW_HE_VDSL_LTE" ] && cpu_freq="320000000"
	cpu_freq_cmd=""
	[ -n "$cpu_freq" ] && cpu_freq_cmd="cpu_freq=$cpu_freq"

	if [ $wave_count = 1 ]; then
		insmod_device mtlk.ko $driver_mode=1 $cpu_freq_cmd fw_logger_severity=$fw_logger_severity
	else
		insmod_device mtlk.ko $driver_mode=1,1 $cpu_freq_cmd fw_logger_severity=$fw_logger_severity
	fi
	awk '/mtlk/ {print $1" module loaded in address: " $6}' /proc/modules > /dev/console
	print2log DBG "fapi_wlan_wave_init_driver.sh: Done Start"
}

stop_insmod_wls_driver() {
	print2log DBG "fapi_wlan_wave_init_driver.sh: Stop"

	if [ `lsmod | grep "mtlk " -c` -gt 0  ]; then
		rmmod_device mtlk
	else
		print2log DBG "fapi_wlan_wave_init_driver.sh: mtlk driver wasn't loaded"
	fi
	print2log DBG "fapi_wlan_wave_init_driver.sh: Done Stop"
}

create_config_insmod_wls_driver()
{	
	return	
}

should_run_insmod_wls_driver()
{
	print2log DBG "fapi_wlan_wave_init_driver.sh: should_run"
	return_status=`check_failure wlan0`
}

case $command in
	start)
		start_insmod_wls_driver
		;;
	stop)
		stop_insmod_wls_driver
		;;
	create_config)
		create_config_insmod_wls_driver
		;;
	should_run)
		should_run_insmod_wls_driver
		;;
esac
$return_status
