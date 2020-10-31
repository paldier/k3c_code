#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

print2log DBG "fapi_wlan_wave_set_driver_params.sh: args: $*"

command=$1
interface_name=$2
params_list_file=$3

timestamp "fapi_wlan_wave_set_driver_params.sh:$command:$interface_name:begin"

IWPRIV_SET ()
{
	api_wrapper set iwpriv $1 "s$2" $3
}

assignee ()
{
	if [ `expr $1 : $2` != 0 ]; then
		echo $3
	else
		echo $4
	fi
}

start_set_driver_params()
{
	print2log DBG "fapi_wlan_wave_set_driver_params.sh: Start"
	if [ ! -e ${WAVE_TMP}/set_driver_params_${interface_name}.sh ]; then
		print2log ALERT "${WAVE_TMP}/set_driver_params_${interface_name}.sh was not found."
		echo "${WAVE_TMP}/set_driver_params_${interface_name}.sh was not found." >> ${WAVE_TMP}/wave_${interface_name}_start_failure
		exit 1
	fi
	
	# DEBUG: TODO: REMOVE
	#cp ${WAVE_TMP}/set_driver_params_${interface_name}.sh ${WAVE_TMP}/set_driver_params_${interface_name}_$$
	api_wrapper file ${WAVE_TMP}/set_driver_params_${interface_name}.sh
	
	#TODO: how control commands order (such as wep). maybe differens queries
	print2log DBG "start fapi_wlan_wave_set_driver_params.sh: Done Start"
}

stop_set_driver_params()
{
	return
}

create_config_set_driver_params()
{
	print2log DBG "fapi_wlan_wave_set_driver_params.sh: create_config"
		
	$ETC_PATH/fapi_wlan_wave_driver_api.tcl DriverSetAll $interface_name $params_list_file
	# Check if debug mode of saving all the driver configurations is on.
	#Debug_save_conf=`host_api get $$ $interface_name Debug_save_conf`
	#if [ "$Debug_save_conf" = "$YES" ]; then
	#	Debug_save_conf_dest=`host_api get $$ $interface_name Debug_save_conf_dest`
	#	cat ${WAVE_TMP}/set_driver_params_${interface_name}.sh >> $Debug_save_conf_dest
	#fi
	print2log DBG "fapi_wlan_wave_set_driver_params.sh: Done create_config ${interface_name}"
}

should_run_set_driver_params()
{
	return_status=`check_failure $interface_name`
}


case $command in
	start)
		start_set_driver_params
	;;
	stop)
		stop_set_driver_params
	;;
	create_config)
		create_config_set_driver_params
	;;
	should_run)
		should_run_set_driver_params
	;;
	reconfigure)
		stop_set_driver_params
		create_config_set_driver_params
		start_set_driver_params
	;;
esac

timestamp "fapi_wlan_wave_set_driver_params.sh:$command:$interface_name:done"
$return_status
