#!/bin/sh

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

interface_name=$1

# The script to stop the Wave wlan can be executed only once, so if drvhlpr tries to stop the system while stop is already executing we need to prevent it.
# Using a temp file to verify the system is not already in the process of stop.
restart_in_progress=$WAVE_TMP/wlan_wave_restart_in_progress

/tmp/drvhlpr_$interface_name -p $CONFIGS_PATH/drvhlpr_$interface_name.conf </dev/console 1>/dev/console 2>&1
term_stat=$?
if [ $term_stat = 1 ]; then
	if [ -e $restart_in_progress ]; then
		echo "fapi_wlan_wave_drvhlpr.sh: Received stop from $interface_name, already in stop process - ignoring"
	else
		echo "fapi_wlan_wave_drvhlpr.sh: MAC assert executed stop for $interface_name" >> $restart_in_progress
		echo "fapi_wlan_wave_drvhlpr.sh: wave MAC HANG"
		[ -e /tmp/mtlk_info_dump.sh ] && sh -c /tmp/mtlk_info_dump.sh
		# Don't reboot the system in UGW - just restart the wls interfaces
		echo "fapi_wlan_wave_drvhlpr.sh: Restarting wireless interface"
		$ETC_PATH/fapi_wlan_wave_restart
		rm $restart_in_progress
	fi
elif [ $term_stat = 2 ]; then
	echo "fapi_wlan_wave_drvhlpr.sh: drvhlpr return rmmod"
else
	echo "fapi_wlan_wave_drvhlpr.sh: drvhlpr terminated with status $term_stat"
fi
