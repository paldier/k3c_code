#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh
print2log DBG "fapi_wlan_wave_set_mac_address.sh: args: $*"

command=$1
interface_name=$2
vap_mac=$3

start_set_mac_address() {
	print2log DBG "fapi_wlan_wave_set_mac_address.sh: Start"

	# If vap_mac value was not received, calculate the MAC for the interface.
	if [ -z "$vap_mac" ]; then
		phy_ap=${interface_name%%.*}
		
		# First interface mac is based on board mac in uboot + offset 16 (dec base)
		# Second interface mac is based on board mac in uboot + offset 24 (dec base)
		# Check the interface_name of the physical interface, 0=First interface, 1=second interface.
		phy_index=${phy_ap##wlan}
		index0_inc=16
		index1_inc=24
		eval wlan_inc=\${index${phy_index}_inc}
		
		# Get the MAC of the platform
		board_mac=`upgrade mac_get 0`

		if [ -z "$board_mac" ]; then
			print2log ALERT "fapi_wlan_wave_set_mac_address: No MAC is defined for the platform in u-boot"
			echo "fapi_wlan_wave_set_mac_address: No MAC is defined for the platform in u-boot" >> ${WAVE_TMP}/wave_${interface_name}_start_failure
			exit 1
		fi

		print2log DBG "fapi_wlan_wave_set_mac_address.sh: Board MAC = $board_mac"
		
		# Divide the board MAC address to the first 3 bytes and the last 3 byte (which we are going to increment).
		board_mac1=0x`echo $board_mac | cut -c 1-2`
		board_mac23=`echo $board_mac | cut -c 4-8`
		board_mac46=0x`echo $board_mac | sed s/://g | cut -c 7-12`

		# Increment the last byte by the the proper incrementation according to the physical interface (wlan0 or wlan1)
		board_mac46=$((board_mac46+wlan_inc))

		# Find out if this is physical AP or VAP.
		is_vap=`check_is_vap $interface_name`
		if [ "$is_vap" = "true" ]; then
			vap_incr_index=`echo $interface_name | cut -d "." -f 2`
			vap_incr_index=$((vap_incr_index+1))
		else
			vap_incr_index=0
		fi
		
		# If it is AP, verify MAC ends with 0 or 8 only.
		if [ "$is_vap" = "false" ]; then
			suffix=$((board_mac46&7))
			if [ $suffix != 0 ]; then
				print2log ALERT "######################################################################################"
				print2log ALERT "######### fapi_wlan_wave_set_mac_address: MAC of $phy_ap is wrong. Must end with 0 or 8 ##"
				print2log ALERT "######### Number of supported VAPs may be limited due to this error ##################"
				print2log ALERT "######################################################################################"
				# Uncomment the following two lines if you want to disable wlan bringup with unaligned MACs
				# $ETC_PATH/mtlk_insmod_wls_driver.sh stop
				# exit 1
			fi
		fi

		# For VAP, use MAC of physical AP incremented by the index of the interface name+1 (wlan0.0 increment wlan0 by 0+1, wlan1.2 increment wlan1 by 2+1).
		board_mac46=$((board_mac46+$vap_incr_index))
		
		# Generate the new MAC.
		vap_mac4=$((board_mac46/65536))
		board_mac46=$((board_mac46-vap_mac4*65536))
		vap_mac5=$((board_mac46/256))
		board_mac46=$((board_mac46-vap_mac5*256))
		vap_mac6=$board_mac46
		# If the 4th byte is greater than FF (255) set it to 00.
		[ $vap_mac4 -ge 256 ] && vap_mac4=0
		
		vap_mac=`printf '%02X:%s:%02X:%02X:%02X' $board_mac1 $board_mac23 $vap_mac4 $vap_mac5 $vap_mac6`
		print2log DBG "fapi_wlan_wave_set_mac_address.sh: New VAP MAC = $vap_mac"
	fi
	# Set new MAC
	api_wrapper set ifconfig $interface_name hw ether $vap_mac down
	if [ $? != 0 ]; then
		print2log ALERT "fapi_wlan_wave_set_mac_address.sh: Setting MAC and starting $interface_name failed."
		echo "fapi_wlan_wave_set_mac_address.sh: Setting MAC and starting $interface_name failed" >> ${WAVE_TMP}/wave_${interface_name}_start_failure
		exit 1
	else
		echo "new_vap_mac=$vap_mac" > ${WAVE_TMP}/${interface_name}_mac
	fi
	
	print2log DBG "fapi_wlan_wave_set_mac_address.sh: Finish"
}

stop_set_mac_address() {
	return
}

create_config_set_mac_address() {
	return
}

should_run_set_mac_address() {
	return_status=`check_failure $interface_name`
}

case $command in
	start)
		start_set_mac_address
		;;
	stop)
		stop_set_mac_address
		;;
	create_config)
		create_config_set_mac_address
		;;
	should_run)
		should_run_set_mac_address
		;;
esac
$return_status
