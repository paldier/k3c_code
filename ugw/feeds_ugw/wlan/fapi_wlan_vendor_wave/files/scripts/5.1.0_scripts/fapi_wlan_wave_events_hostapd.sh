#!/bin/sh

script_name="fapi_wlan_wave_events_hostapd.sh"

[ ! "$LIB_COMMON_SOURCED" ] && . /tmp/fapi_wlan_wave_lib_common.sh
[ ! "$LIB_WPS_SOURCED" ] && . /tmp/fapi_wlan_wave_lib_wps.sh
# [ -e $LED_VARS_FILE ] && . $LED_VARS_FILE

interface_name=$1
name=$2
param3=$3
param4=$4

# Find the interface index and the radio index
interface_index=`find_index_from_interface_name $interface_name`
radio_name=${interface_name%%.*}
radio_index=`find_index_from_interface_name $radio_name`

print2log $radio_index DEBUG "$script_name $*"

conf_via_external()
{
	# Define local parameters
	local interface_name

	interface_name=$1

	print2log $radio_index DEBUG "$script_name: conf_via_external start"
	wps_external_done $interface_name
	print2log $radio_index DEBUG "$script_name: conf_via_external done"
}

wps_pin_needed()
{
	# Define local parameters
	local interface_name sta_uuid sta_mac allowed_mac

	interface_name=$1
	sta_uuid=$2
	sta_mac=$3

	print2log $radio_index DEBUG "$script_name: Start wps_pin_needed for MAC=$sta_mac with uuid=$sta_uuid"
	# Change MAC to uppercase
	sta_mac=`echo $sta_mac | tr '[:lower:]' '[:upper:]'`

	# Compare between MAC in requesting sta and MAC listed as allowed in rc.conf (converted to uppercase).
	allowed_mac=`cat $WPS_MAC_TEMP_FILE | tr '[:lower:]' '[:upper:]'`

	# If requesting STA's MAC is allowed, perform pin connection
	if [ "$allowed_mac" = "$sta_mac" ]
	then
		print2log $radio_index DEBUG "$script_name: connecting STA via PIN"
		wps_connect_via_pin $interface_index $interface_name $radio_name 0 $sta_mac $sta_uuid
	else
		print2log $radio_index DEBUG "$script_name: STA $sta_mac tried PIN connection but wasn't allowed"
	fi
	print2log $radio_index DEBUG "$script_name: Done wps_pin_needed"
}

wps_in_progress()
{
	# Define local parameters
	local interface_name

	interface_name=$1

	# First, set status to Idle and only after that set to In Progress.
	# Notify the web and the DB that status is "Idle"
	build_wlan_notification "wsd" "NOTIFY_WPS_STATUS" "message:Idle"
	build_wlan_notification "servd" "NOTIFY_WIFI_WPS_STATUS" "Name:${interface_name} Status:Idle"
	# Notify the web and the DB that status is "In Progress"
	build_wlan_notification "wsd" "NOTIFY_WPS_STATUS" "message:In_Progress"
	build_wlan_notification "servd" "NOTIFY_WIFI_WPS_STATUS" "Name:${interface_name} Status:In_Progress"
#	# multicolor WPS LED: fast blink amber
#	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_amber" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_amber 0 400 $wps_amber,200 0,100 )
#	# unicolor WPS LED: blink green
#	elif [ -n "$led_wps_count" -a -n "$wps_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_blink $led_wps_green 500 500 )
#	# no WPS LED, but multicolor WLAN0 LED: fast blink amber
#	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_amber" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_amber 0 400 $wlan0_amber,200 0,100 )
#	# no WPS LED, but unicolor WLAN0 LED: blink green
#	elif [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_blink $led_wlan0_green 500 500 )
#	fi
}

#wps_auth_started()
# {
#	# multicolor WPS LED: slow blink green and amber
#	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_amber" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green -1 $wps_green,1000 $wps_amber,1000 )
#	# unicolor WPS LED: slow blink green
#	elif [ -n "$led_wps_count" -a -n "$wps_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_blink $led_wps_green 1000 1000 )
#	# no WPS LED, but multicolor WLAN0 LED: slow blink green and amber
#	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_amber" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green -1 $wlan0_green,1000 $wlan0_amber,1000 )
#	# no WPS LED, but unicolor WLAN0 LED: slow blink green
#	elif [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_blink $led_wlan0_green 1000 1000 )
#	fi
#}

wps_error()
{
	# Define local parameters
	local interface_name

	interface_name=$1

	# Notify the web and the DB that status is "Timeout"
	build_wlan_notification "wsd" "NOTIFY_WPS_STATUS" "message:Timeout"
	build_wlan_notification "servd" "NOTIFY_WIFI_WPS_STATUS" "Name:${interface_name} Status:Timeout"
#	# multicolor WPS LED: fast blink red for 2 minutes, then stay green
#	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 600 $wps_red,100 0,100 )
#	# unicolor WPS LEDs: fast blink red for 2 minutes, then stay green
#	elif [ -n "$wps_green" -a -n "$wps_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 1 0,120000 )
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_blink $led_wps_red 100 100 120 )
#	# unicolor WPS LED (green only): fast blink green for 2 minutes, then stay green
#	elif [ -n "$wps_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 600 $wps_green,100 0,100 )
#	# no WPS LED, but multicolor WLAN0 LED: fast blink red for 2 minutes, then stay green
#	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 600 $wlan0_red,100 0,100 )
#	# no WPS LED, but unicolor WLAN0 LEDs: fast blink red for 2 minutes, then stay green
#	elif [ -n "$wlan0_green" -a -n "$wlan0_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 1 0,120000 )
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_blink $led_wlan0_red 100 100 120 )
#	# no WPS LED, but unicolor WLAN0 LED (green only): fast blink green for 2 minutes, then stay green
#	elif [ -n "$wlan0_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 600 $wlan0_green,100 0,100 )
#	fi
}

wps_session_overlap()
{
	# Define local parameters
	local interface_name

	interface_name=$1

	# Notify the web and the DB that status is "Overlap"
	build_wlan_notification "wsd" "NOTIFY_WPS_STATUS" "message:Overlap"
	build_wlan_notification "servd" "NOTIFY_WIFI_WPS_STATUS" "Name:${interface_name} Status:Overlap"
#	# multicolor WPS LED: blink red for 2 minutes, then stay green
#	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 80 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 0,500 )
#	# unicolor WPS LEDs:  blink red for 2 minutes, then stay green
#	elif [ -n "$wps_green" -a -n "$wps_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 1 0,120000 )
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_red 0 80 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 0,500 )
#	# unicolor WPS LED (green only): fast blink green for 2 minutes, then stay green
#	elif [ -n "$wps_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 600 $wps_green,100 0,100 )
#	# no WPS LED, but multicolor WLAN0 LED: fast blink red for 2 minutes, then stay green
#	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 80 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 0,500 )
#	# no WPS LED, but unicolor WLAN0 LEDs: fast blink red for 2 minutes, then stay green
#	elif [ -n "$wlan0_green" -a -n "$wlan0_red" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 1 0,120000 )
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_red 0 80 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 0,500 )
#	# no WPS LED, but unicolor WLAN0 LED (green only): fast blink green for 2 minutes, then stay green
#	elif [ -n "$wlan0_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 600 $wlan0_green,100 0,100 )
#	fi
}

wps_success()
{
	# Define local parameters
	local interface_name

	interface_name=$1

	# Notify the web and the DB that status is "Success"
	build_wlan_notification "wsd" "NOTIFY_WPS_STATUS" "message:Success"
	build_wlan_notification "servd" "NOTIFY_WIFI_WPS_STATUS" "Name:${interface_name} Status:Success"
#	# WPS LED: slow blink green 5 times, then stay green
#	if [ -n "$led_wps_count" -a -n "$wps_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 5 $wps_green,1000 0,1000 )
#	# no WPS LED, but WLAN0 LED: slow blink green 5 times, then stay green
#	elif [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wlan0_green $wlan0_green 5 $wlan0_green,1000 0,1000 )
#	fi
}

sta_connection()
{
	# Define local parameters
	local interface_name connection_type mac_address \
	autentication_state downlink_rate uplink_rate signal_strength retransmissions

	interface_name=$1
	connection_type=$2
	mac_address=$3


	if [ "$connection_type" = "connect" ]
	then
		# Find the downlink, uplink, signal strength and retransmissins information
		mtdump_output=`${BINDIR}/mtdump $interface_name PeerFlowStatus $mac_address -f ${BINDIR}/mtdump.mtidlc_PeerFlowStat`
		downlink_rate=${mtdump_output%%: Last transmit rate*}
		downlink_rate=${downlink_rate##*received}
		downlink_rate=`echo $downlink_rate`
		downlink_rate=${downlink_rate/./}
		downlink_rate=$((downlink_rate*100))
		# Downlink minimum value is 1000
		[ $downlink_rate -eq 0 ] && downlink_rate=1000

		uplink_rate=${mtdump_output%%: Last receive rate*}
		uplink_rate=${uplink_rate##*rate}
		uplink_rate=`echo $uplink_rate`
		uplink_rate=${uplink_rate/./}
		uplink_rate=$((uplink_rate*100))
		# Uplink minimum value is 1000
		[ $uplink_rate -eq 0 ] && uplink_rate=1000

		mtdump_output=`mtdump w PeerFlowStatus $mac_address`
		ants=${mtdump_output#*Short-term RSSI average per antenna}
		signal_strength=`echo $ants | awk '{print $1}'`
		[ $signal_strength -lt -120 ] && signal_strength=-128
		signal_strength2=`echo $ants | awk '{print $4}'`
		[ $signal_strength2 -lt -120 ] && signal_strength2=-128
		signal_strength3=`echo $ants | awk '{print $7}'`
		[ $signal_strength3 -lt -120 ] && signal_strength3=-128
		signal_strength4=`echo $ants | awk '{print $10}'`
		[ $signal_strength4 -lt -120 ] && signal_strength4=-128
		retransmissions=0

		build_wlan_notification "servd" "NOTIFY_WIFI_DEVICE_ASSOCIATED" "Name:${interface_name} Status:Connected MACAddress:$mac_address AuthenticationState:true LastDataDownlinkRate:$downlink_rate LastDataUplinkRate:$uplink_rate SignalStrength:$signal_strength X_LANTIQ_COM_Vendor_SignalStrength2:$signal_strength2 X_LANTIQ_COM_Vendor_SignalStrength3:$signal_strength3 X_LANTIQ_COM_Vendor_SignalStrength4:$signal_strength4 Retransmissions:$retransmissions"
	elif [ "$connection_type" = "disconnect" ]
	then
		build_wlan_notification "servd" "NOTIFY_WIFI_DEVICE_ASSOCIATED" "Name:${interface_name} Status:Disconnected MACAddress:$mac_address"
	fi
}
#timestamp=`date -Iseconds`
# Write to file only when CLI WPS session started.
# CLI WPS command create CLI_WPS_IN_PROCESS file when WPS session start, delete it when finish .
# It also mv wave_wlan_hostapd_events.log to wave_wlan_hostapd_events_prev.log for debug history.
#if [ -e $CLI_WPS_IN_PROCESS ]
#then
#	echo "[$timestamp] $*" >> /tmp/wave_wlan_hostapd_events.log
#fi

#if [ -e /tmp/WPS_STATE ]; then
#	source /tmp/WPS_STATE
#else
#	WPS_STATE=WPS_OFF
#fi

# WPS state machine
case $name in
	"WPS-NEW-AP-SETTINGS")
		conf_via_external $interface_name
	;;
	"WPS-PIN-NEEDED")
		wps_pin_needed $interface_name $param3 $param4
	;;
	#"WLAN-MODE-INIT")
		# WLAN LED: flashing green for WLAN network activity
	#	if [ "$interface_name" = "wlan0" -a -n "$led_wlan0_green" ]; then
	#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_netif_activity_trigger $led_wlan0_green $interface_name )
	#	elif [ "$interface_name" = "wlan1" -a -n "$led_wlan1_green" ]; then
	#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_netif_activity_trigger $led_wlan1_green $interface_name )
	#	fi
	#	# Initialize WPS and APMODE LEDs (only for WLAN-MODE-INIT of wlan0)
	#	if [ "$interface_name" = "wlan0" ]; then
	#		# check if WPS is enabled
	#		WPS_ON=`get_wps_on $ap_index $interface_name`
	#		if [ "$WPS_ON" = "$YES" ]; then
	#			WPS_STATE="WPS_IDLE"
 	#		else
	#			WPS_STATE="WPS_OFF"
 	#		fi
	#		if [ -n "$led_wps_green" ]; then
	#			if [ "$WPS_STATE" != "WPS_OFF" ]; then
	#				( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_sequence $led_wps_green $wps_green 0 )
	#			else
	#				( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_off $led_wps_green )
	#			fi
	#		fi
	#		if [ -n "$led_apmode_green" ]; then
	#			if [ "$param3" = "sta" ]; then
	#				( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_off $led_apmode_green )
	#			else
	#				( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_on $led_apmode_green )
	#			fi
	#		fi
	#	fi
	#;;
	"WPS-SESSION-START")
	#	if [ "$WPS_STATE" != "WPS_OFF" ]; then
	#		WPS_STATE="WPS_STARTED"
			wps_in_progress $interface_name
	#	fi
	;;
	#"CTRL-EVENT-EAP-STARTED")
	#	if [ "$WPS_STATE" = "WPS_STARTED" ]; then
	#		WPS_STATE=$param3
	#		wps_auth_started
	#	fi
	#;;
	"WPS-REG-SUCCESS")
	#	WPS_STATE=$param3
		wps_success $interface_name
	;;
	"AP-STA-CONNECTED" | "CONNECTED")
	#	if [ "$WPS_STATE" = "$param3" ]; then
	#		WPS_STATE="WPS_IDLE"
	#	fi
	#	# WLAN0 LED: flashing green for WLAN0 network activity
	#	if [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
	#		( . $ETC_PATH/wave_wlan_gpio_ctrl.sh led_netif_activity_trigger $led_wlan0_green wlan0 )
	#	fi
		sta_connection $interface_name connect $param3
	;;
	"WPS-TIMEOUT")
	#	WPS_STATE="WPS_IDLE"
		wps_error $interface_name
	;;
	"AP-STA-DISCONNECTED")
	#	if [ "$WPS_STATE" = "$param3" ]; then
	#		WPS_STATE="WPS_IDLE"
	#		wps_error
	#	fi
		sta_connection $interface_name disconnect $param3
	;;
	"WPS-OVERLAP-DETECTED")
	#	WPS_STATE="WPS_IDLE"
		wps_session_overlap $interface_name
	;;
	*)
		echo "wave_wlan_hostapd_events: $name"
	;;
esac
#echo "WPS_STATE=$WPS_STATE" > /tmp/WPS_STATE
