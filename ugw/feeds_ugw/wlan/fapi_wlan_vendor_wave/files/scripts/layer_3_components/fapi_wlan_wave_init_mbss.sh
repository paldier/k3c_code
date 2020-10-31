#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

command=$1
interface_name=$2

# Physical AP to which the VAP belongs.
phy_ap=${interface_name%%.*}
# VAP index of the VAP.
vap_index=${interface_name##*.}

start_init_mbss() {
	print2log DBG "fapi_wlan_wave_init_mbss.sh: Start"
	print2log DBG "fapi_wlan_wave_init_mbss.sh: phy_ap = $phy_ap"
	print2log DBG "fapi_wlan_wave_init_mbss.sh: vap_index $vap_index"
	
	api_wrapper set iwpriv $phy_ap sAddVap $vap_index 
	res=$?
	# verify that VAP was added successful
	count=0
	while [ $res != 0 ]; do
		if [ $count -lt 3 ]; then
			sleep 1
			api_wrapper set iwpriv $phy_ap sAddVap $vap_index
			res=$?
		else
			print2log ALERT "fapi_wlan_wave_init_mbss.sh: add of $interface_name to $phy_ap failed"
			echo "fapi_wlan_wave_init_mbss.sh: add of $interface_name to $phy_ap failed" >> ${WAVE_TMP}/wave_${interface_name}_start_failure
			exit 1
		fi
		count=$((count+1))
	done
	
	print2log DBG "fapi_wlan_wave_init_mbss.sh: BSS $interface_name created. Done Start"
}

stop_init_mbss() {
	print2log DBG "fapi_wlan_wave_init_mbss.sh: Start stop_init_mbss"
	print2log DBG "fapi_wlan_wave_init_mbss.sh: vap_index $vap_index"
	api_wrapper set iwpriv $phy_ap sDelVap $vap_index
	res=$?
	# verify that VAP was removed successful
	count=0
	while [ $res != 0 ]; do
		if [ $count -lt 4 ]; then
			sleep 1
			api_wrapper set iwpriv $phy_ap sDelVap $vap_index
			res=$?
		else
			print2log ALERT "Removing VAP $interface_name failed by driver after 5 tries"
			exit
		fi
		count=$((count+1))
	done
	
	print2log DBG "fapi_wlan_wave_init_mbss.sh: BSS $interface_name removed"
}

create_config_init_mbss() {
	return
}

should_run_init_mbss() {
	print2log DBG "fapi_wlan_wave_init_mbss.sh: should_run"
	return_status=`check_failure $interface_name`
	[ "$return_status" = "false" ] && return

	# Check if the physical AP failed and if so, don't start the VAP.
	if [ -e $wave_start_failure ]; then
		if [ `cat $wave_start_failure | grep "$phy_ap failed:" -c` -gt 0 ]; then
			print2log ALERT "fapi_wlan_wave_init_mbss.sh: add of $interface_name to $phy_ap failed. Since $phy_ap failed before that."
			echo "fapi_wlan_wave_init_mbss.sh: add of $interface_name to $phy_ap failed. Since $phy_ap failed before that." >> ${WAVE_TMP}/wave_${interface_name}_start_failure
			return_status=false
		fi
	fi
}


case $command in
	start)
		start_init_mbss
	;;
	stop)
		stop_init_mbss
	;;
	create_config)
		create_config_init_mbss
	;;
	should_run)
		should_run_init_mbss
	;;
esac

$return_status
