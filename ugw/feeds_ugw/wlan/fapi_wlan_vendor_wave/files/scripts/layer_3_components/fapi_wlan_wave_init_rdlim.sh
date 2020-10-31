#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

command=$1
interface_name=$2

start_init_rdlim() {
	print2log DBG "fapi_wlan_wave_init_rdlim.sh Start"

	print2log DBG "fapi_wlan_wave_init_rdlim.sh: $ETC_PATH/fapi_wlan_wave_rdlim.tcl $interface_name"
	$ETC_PATH/fapi_wlan_wave_rdlim.tcl $interface_name
	if [ $? != 0  ]; then
		print2log DBG "fapi_wlan_wave_init_rdlim.sh: fapi_wlan_wave_rdlim.tcl failed for $interface_name"
		echo "fapi_wlan_wave_init_rdlim.sh: fapi_wlan_wave_rdlim.tcl failed for $interface_name" >> ${WAVE_TMP}/wave_${interface_name}_start_failure
		exit 1
	fi
	# Workaround: create link in the tmp until fix drvhlpr and more for dual concurrent"
	# TODO: Needs a fix when we have a new drvhlpr that supports dual concurrent
	cp -s $CONFIGS_PATH/hw_wlan0.ini /tmp/HW.ini

	# For UGW-6.5, we comment the following section
	# Make sure defaults CoC related parameters are according to real HW
	#coc_power=`host_api get $$ $apIndex CoCPower` 
	#sw_num_antennas=`echo $coc_power | awk '{print $2}'` 
	#hw_Support_3TX=`host_api get $$ hw_$wlan Support_3TX` 
	#if [ $sw_num_antennas -gt 2 ] && [ $hw_Support_3TX -eq 0 ]
	#then
		#hw doesn't support 3X3, overwrite with 2X2
	#	coc_power_new=`echo $coc_power | awk '{print $1" 2 2"}'`
	#	host_api set $$ $apIndex CoCPower "$coc_power_new"
	#fi
	#config_save.sh
	print2log DBG "start fapi_wlan_wave_init_rdlim.sh: Done Start"
}

stop_init_rdlim() {
	return
}

create_config_init_rdlim() {
	return
}

should_run_init_rdlim() {
	print2log DBG "fapi_wlan_wave_init_rdlim.sh: should_run"
	return_status=`check_failure $interface_name`
	[ "$return_status" = "false" ] && return

	# Check if hw.ini file for the current interface already exists
	if [ -e $CONFIGS_PATH/hw_$interface_name.ini ]; then
		print2log DBG "fapi_wlan_wave_init_rdlim.sh:should_run: hw_$interface_name.ini already exists"
		return_status=false
	fi
}

case $command in
	start)
		start_init_rdlim
		;;
	stop)
		stop_init_rdlim.sh
		;;
	create_config)
		create_config_init_rdlim
		;;
	should_run)
		should_run_init_rdlim
		;;
esac
$return_status
