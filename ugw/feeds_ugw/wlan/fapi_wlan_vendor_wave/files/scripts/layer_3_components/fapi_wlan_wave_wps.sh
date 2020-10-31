#!/bin/sh

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

print2log DBG "fapi_wlan_wave_wps.sh: args: $*"

action=$1
interface_name=$2
wps_index=$3
wps_vendor_index=$4
param_5=$5
enrollee_uuid=$6

# File names
HOSTAPD_CONF="$CONFIGS_PATH/hostapd_${interface_name}.conf"
HOSTAPD_CLI="tmp/hostapd_cli"
WPS_CMD_SCRIPT="fapi_wlan_wave_wps.sh"
WPS_ACTION_STATFILE="$WAVE_TMP/wps_action"

# Since events are not yet supported in UGW, the following files left as a comment as a preparation for supporting drvhlpr events.
#WPS_START_TIME_STATFILE="$WAVE_TMP/wps_startup_time"
#This variable should be used by the display layer to figure out what the current WPS status is 
#WPS_CURRENT_STATUS_STATFILE="$WAVE_TMP/wps_current_status"
#WPS_LAST_CODE_STATFILE="$WAVE_TMP/wps_last_code"

# Constant variables
securityOpen=1
securityWEP=2
securityWPAPersonal=3

WPS_Idle_Blink=0

# See if this is not the first instance, if another instance is alive, abort the run
# Since we saw some cases where ps detected too many processes, we set that the ps would be printed to the screen in case on the error.
ps_result=`ps`
wps_is_running=`echo $ps_result | grep "\<$WPS_CMD_SCRIPT\>" | grep -vc grep`
if [ $wps_is_running ] && [ $wps_is_running -gt 2 ]; then
	print2log ERROR "fapi_wlan_wave_wps.sh: Aborted. $WPS_CMD_SCRIPT is already running"
	exit
fi

show_usage() {
	echo -e "Wave Wlan WPS control script"
	echo -e "This script is used to control the WPS Session state"
	echo -e "Syntax :"
	echo -e "fapi_wlan_wave_wps.sh action "
	echo -e "\nactions : "
	echo -e "conf_via_pin <PIN> : Configure a STA via PIN. "
	echo -e "\t PIN : PIN Number"
	echo -e "conf_via_pbc : Configure a STA using PBC"
	echo -e "\t MAC  : MAC Address of AP to connect to"
	echo -e "wps_generate_pin : Generate a new random PIN number for the AP"
	echo -e "wps_external_done : Save current WPS session settings to DB."
}

start_wps_led_blink() {
	# send WPS-SESSION-START event to the WLAN events script
	( . $HOSTAPD_EVENTS_SCRIPT $interface_name WPS-SESSION-START )
}

set_ap_configured() {
	print2log DBG "fapi_wlan_wave_wps.sh: Start set_ap_configured"
	wps_state=`get_conf_param WPSState $wps_vendor_index $interface_name`

	# If it is a non-configured AP
	if [ "$wps_state" = "$Unconfigured_AP" ]; then
		print2log DBG "fapi_wlan_wave_wps.sh: set_ap_configured. AP is non-configured, setting to configured"
		update_conf_out "Object_${wps_vendor_index}" "$ACCESSPOINT_WPS_VENDOR_OBJECT"
		update_conf_out "ConfigState_${wps_vendor_index}" "Configured"
		(. $ETC_PATH/fapi_wlan_wave_init_hostapd.sh stop $interface_name) && (. $ETC_PATH/fapi_wlan_wave_init_hostapd.sh create_config $interface_name $wps_index "WPSState" $wps_vendor_index) && (. $ETC_PATH/fapi_wlan_wave_init_hostapd.sh start $interface_name)
	else
		print2log DBG "fapi_wlan_wave_wps.sh: set_ap_configured. AP is in configured mode"
	fi
	print2log DBG "fapi_wlan_wave_wps.sh: End set_ap_configured"
}

update_wps_action() {
	print2log DBG "fapi_wlan_wave_wps.sh: update_wps_action. WPS action is: $action"
	echo "WPS_action = $action" > $WPS_ACTION_STATFILE
}

get_param() {
	string=`grep "^\<$1\>" $2 | sed 's/^[^=]*=[ ]*//'`
	echo $string
}

#########################################################
# The following functions are the actions available:
# conf_via_pbc
# conf_via_pin
# wps_generate_pin
# wps_external_done
#########################################################
conf_via_pbc() {
	print2log DBG "fapi_wlan_wave_wps.sh: Start conf_via_pbc"

	echo "The button 'Start PBC' was activated for $interface_name" > /dev/console

	start_wps_led_blink
	set_ap_configured
	update_wps_action
	$HOSTAPD_CLI -i $interface_name wps_pbc
	print2log DBG "fapi_wlan_wave_wps.sh: End conf_via_pbc"
}

conf_via_pin() {
	print2log DBG "fapi_wlan_wave_wps.sh: Start conf_via_pin"

	enrollee_mac=$param_5

	enrollee_pin=`get_conf_param EndpointPIN $wps_vendor_index $interface_name`
	[ -z "$enrollee_mac" ] && enrollee_mac=`get_conf_param AuthorizedMac $wps_vendor_index $interface_name`
	enrollee_type=2
	wps_pin_timeout=1800

	echo "The button 'Connect' for PIN connection was activated for $wlan with PIN=$enrollee_pin" and MAC=$enrollee_mac > /dev/console

	# If PIN wasn't received, show warnning and exit
	[ -z "$enrollee_pin" ] && print2log WARNNING "fapi_wlan_wave_wps.sh: PIN connection started but no PIN number received" && exit

	# Save PIN and MAC of STA in temp files
	echo "$enrollee_pin" > $WPS_PIN_TEMP_FILE
	echo "$enrollee_mac" > $WPS_MAC_TEMP_FILE

	start_wps_led_blink
	set_ap_configured
	update_wps_action

	# Check if web selection was to accept all incoming PIN connectios with correct PIN. In such case, no MAC and timeout are needed.
	if [ -z "$enrollee_mac" ]; then
		enrollee_uuid=any
		enrollee_mac=""
		wps_pin_timeout=""
	fi

	# Check if no uuid was received, need to read from pin_req file or generate new random
	if [ -z "$enrollee_uuid" ]; then
		# Get the MAC from the file and compare with requesting MAC
		# Get the MACs and uuids from the file
		pin_req_list=`cat $HOSTAPD_PIN_REQ | awk '{print $3"\n"$2}'`

		# Go over the list and compare MACs (MACs appear every odd line, corresponding uuid in following even line)
		# Going over the entire list, since STA can change its uuid and newest are added at the end of the file.
		i=0
		need_uuid=$NO
		for line in $pin_req_list; do
			let "res=i&1"
			if [ $res = 0 ]; then
				[ "$enrollee_mac" = "$line" ] && need_uuid=$YES
			else
				[ "$need_uuid" -eq "$YES" ] && enrollee_uuid=$line
			fi
			i=$((i+1))
		done

		# If MAC wasn't found in list, generate random uuid
		[ "$need_uuid" -eq "$NO" ] && enrollee_uuid=`uuidgen`
	fi

	print2log DBG "fapi_wlan_wave_wps.sh: $HOSTAPD_CLI -i$interface_name wps_pin $enrollee_uuid $enrollee_pin $wps_pin_timeout $enrollee_mac"
	$HOSTAPD_CLI -i$interface_name wps_pin $enrollee_uuid $enrollee_pin $wps_pin_timeout $enrollee_mac

	print2log DBG "fapi_wlan_wave_wps.sh: End conf_via_pin"
}

# Generate a new PIN number for the AP and return new value to the fapi_wlan_wave_out.conf
wps_generate_pin() {
	new_pin=`$HOSTAPD_CLI -i$interface_name wps_ap_pin random`

	# Write the new AP PIN to out.conf
	update_conf_out "PIN_${wps_vendor_index}" "$new_pin"
}

# Save the settings after WPS external registrar session from the hostapd conf file to the database.
# Notify SL that an update of the DB after external registrar is needed using ubus call with the parameters to update
wps_external_done() {
	print2log DBG "fapi_wlan_wave_wps.sh: Start wps_external_done"

	# Check hostapd configuration file exists.
	if [ ! -e $HOSTAPD_CONF ]; then
		print2log ERROR "fapi_wlan_wave_wps.sh: Aborted. $HOSTAPD_CONF file doesn't exist"
		exit 1
	fi
	# Read parameters from the hostapd conf file
	# Get ssid
	g_ssid=`get_param ssid $HOSTAPD_CONF`

	# Get security
	# Read wpa parameter to know if set to open/wep or wpa type of security
	s_security_mode=""
	g_wep_key=""
	g_passphrase=""

	g_wpa=`get_param wpa $HOSTAPD_CONF`
	[ -z "$g_wpa" ] && g_wpa=0

	# If wpa is 0, security is wep or open.
	if [ "$g_wpa" = "0" ]; then
		s_security_mode="None"
		# Check if WEP mode by searching wep_default_key in conf file.
		g_wep_default_key=`get_param wep_default_key $HOSTAPD_CONF`
		# Check wep key
		if [ "$g_wep_default_key" ]; then
			s_security_mode="WEP-64"
			g_wep_key=`get_param wep_key0 $HOSTAPD_CONF | sed 's/\"\([^\"]*\)\"/\1/'`
			# Check if key is 128
			wep_key_length=`echo $g_wep_key | wc -L`
			if [ $wep_key_length = 13 ] || [ $wep_key_length = 26 ]; then
				s_security_mode="WEP-128"
			fi
		fi
	else # Security is WPA 
		# Read wpa_pairwise and rsn pairwise and set wpa type.
		g_wpa_pairwise=`get_param wpa_pairwise $HOSTAPD_CONF`
		g_rsn_pairwise=`get_param rsn_pairwise $HOSTAPD_CONF`
		if [ "$g_wpa_pairwise" ] && [ "$g_wpa_pairwise" = "CCMP" ]; then
			wpa_type="WPA2"
		else
			wpa_type="WPA"
			if [ "$g_rsn_pairwise" ] && [ "$g_rsn_pairwise" = "CCMP" ]; then
				wpa_type="WPA-WPA2"
			fi
		fi

		# Read wpa_key_mgmt to check if in WPA-personal or WPA-enterparise security mode.
		g_wpa_key_mgmt=`get_param wpa_key_mgmt $HOSTAPD_CONF`
		key_management="Personal"
		if [ "$g_wpa_key_mgmt" ] && [ "$g_wpa_key_mgmt" = "WPA-EAP" ]; then
			key_management="Enterprise"
		fi
		s_security_mode="${wpa_type}-${key_management}"

		# Read the passphrase
		g_passphrase=`get_param wpa_passphrase $HOSTAPD_CONF`
	fi

	# Create ubus command for SL to update DB with new values
	# Source ugw_notify_defs.sh
	. /etc/ugw_notify_defs.sh
	nid=$NOTIFY_WIFI_WPS_NEW_AP_SETTINGS
	param_index=1

	ubus_call=$WAVE_TMP/ubus_call.sh

	ubus_command="ubus call servd notify '{\"nid\":$nid,\"type\":false,\"pn${param_index}\":\"ObjectName\",\"pv${param_index}\":\"$SSID_OBJECT\","
	param_index=$((param_index+1))
	ubus_command="${ubus_command}\"pn${param_index}\":\"SSID\",\"pv${param_index}\":\"$g_ssid\","
	param_index=$((param_index+1))
	ubus_command="${ubus_command}\"pn${param_index}\":\"Name\",\"pv${param_index}\":\"$interface_name\","
	param_index=$((param_index+1))
	ubus_command="${ubus_command}\"pn${param_index}\":\"ObjectName\",\"pv${param_index}\":\"$ACCESSPOINT_SECURITY_OBJECT\","
	param_index=$((param_index+1))
	ubus_command="${ubus_command}\"pn${param_index}\":\"ModeEnabled\",\"pv${param_index}\":\"$s_security_mode\","
	param_index=$((param_index+1))
	# If security is WPA/WPA2/mixed, set paddphrase
	if [ ! -z "$g_passphrase" ]; then
		ubus_command="${ubus_command}\"pn${param_index}\":\"KeyPassphrase\",\"pv${param_index}\":\"$g_passphrase\","
		param_index=$((param_index+1))
	fi
	# If security id WEP, set WEP key
	if [ ! -z "$g_wep_key" ]; then
		ubus_command="${ubus_command}\"pn${param_index}\":\"WEPKey\",\"pv${param_index}\":\"$g_wep_key\","
		param_index=$((param_index+1))
	fi
	ubus_command="${ubus_command}\"pn${param_index}\":\"ObjectName\",\"pv${param_index}\":\"$ACCESSPOINT_WPS_VENDOR_OBJECT\","
	param_index=$((param_index+1))
	ubus_command="${ubus_command}\"pn${param_index}\":\"ConfigState\",\"pv${param_index}\":\"Configured\"}'"

	# Make the script calling the ubus executable and execute it
	echo "$ubus_command" > $ubus_call
	chmod +x $ubus_call
	${ubus_call}
	rm -f $ubus_call

	print2log DBG "fapi_wlan_wave_wps.sh: End wps_external_done"
}

case $action in
	conf_via_pin)
		conf_via_pin
		;;
	conf_via_pbc)
		conf_via_pbc
		;;
	wps_generate_pin)
		wps_generate_pin
		;;
	wps_external_done)
		wps_external_done
		;;
	*)
		show_usage
		;;
esac
