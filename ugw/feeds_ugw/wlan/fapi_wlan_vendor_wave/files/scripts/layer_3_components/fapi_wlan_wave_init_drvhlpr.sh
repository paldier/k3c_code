#!/bin/sh
return_status=true

# Source config.sh to identify the platform
# TODO: 1. Fix this path when moved in UGW
# 2. It looks like this is redundant, part of init platform
## [ ! "$CONFIGLOADED" ] && [ -r /etc/config.sh ] && . /etc/config.sh 2>/dev/null && CONFIGLOADED="1"

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: args: $*"

command=$1
interface_name=$2
object_index=$3

start_init_drvhlpr() {
	print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: Start"
	cp -s $BINDIR/drvhlpr /tmp/drvhlpr_$interface_name
	$ETC_PATH/fapi_wlan_wave_drvhlpr.sh $interface_name &
	print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: Done"
}

stop_init_drvhlpr() {
	print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: Stop"
	drvhlpr_ps=`ps | grep "\<drvhlpr_$interface_name\> " | grep -v grep | awk '{print $1}'`
	for pid in $drvhlpr_ps; do kill $pid 2>/dev/null; done
	# Loop in order to wait drvhlpr for proper cleanup, give it time, but not forever
	count=0
	drvhlpr_ps=`ps | grep "\<drvhlpr_$interface_name\> " | grep -v grep | awk '{print $1}'`
	while [ -n "$drvhlpr_ps" ]; do
		if [ $count -gt 10 ]; then
			print2log ERROR "fapi_wlan_wave_init_drvhlpr.sh: In Stop, force drvhlpr kill after 10 sec"
			# After 10 sec force kill
			for pid in $drvhlpr_ps; do kill -9 $pid; done
		fi
		sleep 1
		drvhlpr_ps=`ps | grep "\<drvhlpr_$interface_name\> " | grep -v grep | awk '{print $1}'`
		count=$((count+1))
	done		
	print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: Done stop fapi_wlan_wave_init_drvhlpr"
}

create_config_init_drvhlpr()
{
	print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: create_config"
	drvhlpr_conf=$CONFIGS_PATH/drvhlpr_$interface_name.conf
	# Parameters not used anymore in drvhlpr are commented
	#wps_script_path=""
	#[ "$interface_name" = "wlan0" ] && wps_script_path=$ETC_PATH/mtlk_wps_event.sh
	wps_script_path=$ETC_PATH/mtlk_wps_event.sh
	wls_link_script_path=$ETC_PATH/fapi_wlan_wave_linkstat_event.sh
	data_link_script_path=$ETC_PATH/mtlk_data_link_status.sh
	#leds_conf_file=$ETC_PATH/leds.conf
	#drvhlpr_params=/tmp/drvhlpr_params.sh
	
	# Clean the drvhlpr_conf file
	cat /dev/null > $drvhlpr_conf
	
	rf_mgmt_enable=`get_conf_param RFMgmtEnable $object_index $interface_name`
	rf_mgmt_refresh_time=1000
	rf_mgmt_keep_alive_timeout=60
	rf_mgmt_averaging_alpha=40
	rf_mgmt_met_margin_threshold=1100
	debug_software_watchdog_enable=`get_conf_param Debug_SoftwareWatchdogEnable $object_index $interface_name`
	# The driver NonProcSecurityMode value is decreased by one from the web value
	#NonProcSecurityMode=`expr $NonProcSecurityMode - 1`
		
	#cp $leds_conf_file $drvhlpr_conf
	echo "wlan_link_on = led_on" >> $drvhlpr_conf
	echo "wlan_link_off = led_off" >> $drvhlpr_conf
	echo "wlan_link_connecting = led_pattern_3ms_interval" >> $drvhlpr_conf
	echo "wlan_link_scan = led_pattern_1s_interval" >> $drvhlpr_conf
	echo "wlan_link_data = led_pattern_3ms_interval" >> $drvhlpr_conf
	echo "security_on = led_on" >> $drvhlpr_conf
	echo "security_off = led_off" >> $drvhlpr_conf
	echo "security_wep = led_on" >> $drvhlpr_conf
	echo "wps_idle = led_off" >> $drvhlpr_conf
	echo "wps_in_process = led_pattern_3ms_interval" >> $drvhlpr_conf
	echo "wps_error_overlap_err = led_pattern_1ms_interval_end_5ms_off" >> $drvhlpr_conf
	echo "wps_error_timeout_err = led_on" >> $drvhlpr_conf
	echo "wps_error_stop_pin_err = led_on" >> $drvhlpr_conf
	echo "wps_error_stop_err = led_on" >> $drvhlpr_conf
	echo "security_wep_timeout = 120" >> $drvhlpr_conf
	echo "wps_error_timeout = 120" >> $drvhlpr_conf
	echo "wps_activated_timeout = 150" >> $drvhlpr_conf
	
	echo "network_type = $AP" >> $drvhlpr_conf
	# some parameters are only for physical AP
	#if [ "$network_type" = "$AP" ]
	#then
		echo "wps_script_path = $wps_script_path" >> $drvhlpr_conf
		echo "RFMgmtEnable = $rf_mgmt_enable" >> $drvhlpr_conf
		echo "RFMgmtRefreshTime = $rf_mgmt_refresh_time" >> $drvhlpr_conf
		echo "RFMgmtKeepAliveTimeout = $rf_mgmt_keep_alive_timeout" >> $drvhlpr_conf
		echo "RFMgmtAveragingAlpha = $rf_mgmt_averaging_alpha" >> $drvhlpr_conf
		echo "RFMgmtMetMarginThreshold = $rf_mgmt_met_margin_threshold" >> $drvhlpr_conf
	#fi

	echo "Debug_SoftwareWatchdogEnable = $debug_software_watchdog_enable" >> $drvhlpr_conf
	#echo "NonProcSecurityMode = $NonProcSecurityMode" >> $drvhlpr_conf
	echo "NonProcSecurityMode = 2" >> $drvhlpr_conf
	echo "interface = $interface_name" >> $drvhlpr_conf
	echo "arp_iface0 = eth0" >> $drvhlpr_conf
	arp_eth1=`grep eth1 /proc/net/dev`
	[ "$arp_eth1" ] && echo "arp_iface1 = eth1" >> $drvhlpr_conf

	echo "led_resolution = 1" >> $drvhlpr_conf
	echo "wls_link_script_path = $wls_link_script_path" >> $drvhlpr_conf
	echo "wls_link_status_script_path = $data_link_script_path" >> $drvhlpr_conf
	echo "recovery_script_path = $RECOVERY_SCRIPT_PATH" >> $drvhlpr_conf 

	print2log DBG "fapi_wlan_wave_init_drvhlpr.sh: Done create_config"
}

should_run_init_drvhlpr()
{
	return_status=`check_failure $interface_name`
}

case $command in
	start)
		start_init_drvhlpr 
		;;
	stop)
		stop_init_drvhlpr
		;;
	restart)
		stop_init_drvhlpr
		should_run_init_drvhlpr
		create_config_init_drvhlpr
		start_init_drvhlpr
		;;
	create_config)
		create_config_init_drvhlpr
		;;
	should_run)
		should_run_init_drvhlpr
		;;
esac
$return_status
