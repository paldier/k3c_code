#!/bin/sh

return_status=true
# Defines
[ ! "$WAVE_INIT_PLATFORM" ] && . ${WAVE_TMP}/fapi_wlan_wave_init_platform.sh

command=$1

start_wave_wlan_count()
{
	print2log DBG "start wave_wlan_count"
	
	# wlan_count is actual number of wlan cards in system
	# Wireless can be either PCI or AHB
	pci_count=`cat /proc/bus/pci/devices | grep 1a30 -c`
	ahb_count=`ls /sys/bus/platform/devices | grep wave -c`
	wave_count=$((pci_count+ahb_count))
	print2log DBG "wave_wlan_count=$wave_count"
	if [ "$wave_count" = "0" ]
	then
		print2log ALERT "fapi_wlan_wave_count.sh: No Wave wlan cards were found!!!"
		echo "fapi_wlan_wave_count.sh: No Wave wlan cards were found!!!" >> $wave_init_failure
	fi
	
	# Save the number of interfaces found in /tmp folder
	echo "$wave_count" > ${WAVE_TMP}/wave_wlan_count

	print2log DBG "finish fapi_wlan_wave_count.sh"
}

stop_wave_wlan_count()
{
	return
}

create_config_wave_wlan_count()
{
	return
}

should_run_wave_wlan_count()
{
	print2log DBG "fapi_wlan_wave_count.sh: should_run"
	return_status=`check_failure wlan0`
}

case $command in
	start)
		start_wave_wlan_count
		;;
	stop)
		stop_wave_wlan_count
		;;
	create_config)
		create_config_wave_wlan_count
		;;
	should_run)
		should_run_wave_wlan_count
		;;
esac

$return_status
