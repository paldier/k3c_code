#!/bin/sh

# This script sets platform specific settings for the UGW platforms.

# TODO: config.sh will probably be moved to: /opt/lantiq/etc/rc.d/config.sh or somewhere similar
[ ! "$CONFIGLOADED" ] && [ -r /etc/config.sh ] && . /etc/config.sh 2>/dev/null && CONFIGLOADED="1"

# Folders and files parameters
export ETC_PATH=/opt/lantiq/wave/scripts
export IMAGES_PATH=/opt/lantiq/wave/images
export DRIVER_PATH=/opt/lantiq/lib/modules/$(uname -r)/net
export CONFIGS_PATH=/ramdisk/flash
export BINDIR=/opt/lantiq/bin
export WAVE_TMP=/tmp/wlan_wave
export IN_CONF=$WAVE_TMP/fapi_wlan_wave_in.conf
export OUT_CONF=$WAVE_TMP/fapi_wlan_wave_out.conf
export RECOVERY_SCRIPT_PATH=$ETC_PATH/fapi_wlan_wave_fw_recovery_notify
export RECOVERY_CONF=$WAVE_TMP/fapi_wlan_wave_recovery_conf
export COC_CONF=$WAVE_TMP/fapi_wlan_wave_coc_conf
export CHANNEL_CONF=$WAVE_TMP/fapi_wlan_wave_channel_conf
export STA_LIMITS_CONF=$WAVE_TMP/fapi_wlan_wave_sta_limits_conf
export PPA_STATUS_CONF=$WAVE_TMP/fapi_wlan_wave_ppa_status

# Scripts debug parameters
export log_level=1
export log_path="/dev/console"
# Uncomment to turn on timestamp based profiling:
#export WLAN_PROFILING=1

export AP=2
export VAP=3
export STA=0

#STATIC=1
export DHCPC=0

export BAND_5G=0
export BAND_24G=1
export BOTH_BAND=2

export ENABLE=1
export DISABLE=0
export YES=1
export NO=0

export MacCloning=3
export L2NAT=2
export FourAddresses=1
export ThreeAddresses=0

export open=1
export wep=2
export WPA_Personal=3
export WPA_Enterprise=4
export osen=8

export Enrollee=0
export Wirless_registrar=1
export Configured_AP=2
export Unconfigured_AP=1
export Ap_Proxy_registrar=1

export SSID_CHANGED=1
export SSID_NOT_CHANGED=0

export IF_UP=1
export IF_DOWN=0

export HOSTAPD_EVENTS_SCRIPT=$ETC_PATH/fapi_wlan_wave_hostapd_events.sh
export HOSTAPD_PIN_REQ=/var/run/hostapd.pin-req
export WPS_PIN_TEMP_FILE=$WAVE_TMP/wps_current_pin
export WPS_MAC_TEMP_FILE=$WAVE_TMP/wps_current_mac
export LED_VARS_FILE=$WAVE_TMP/wlan_leds_scan_results
export CLI_WPS_IN_PROCESS=$WAVE_TMP/cli_wps_in_process

export wave_init_failure=$WAVE_TMP/wave_init_failure
export wave_start_failure=$WAVE_TMP/wave_start_failure
export wave_init_success=$WAVE_TMP/wave_init_success

export WAVE_VENDOR_NAME=LANTIQ

export GENL_FAMILY_ID_FILE=$WAVE_TMP/mtlk_genl_family_id_file

# HS2.0 parameters:
export HS20_MODE_DISABLED=0
export HS20_MODE_ENABLED=1
export HS20_MODE_OSEN=2

# TR181 objects names
export TR181_VENDOR="X_LANTIQ_COM_Vendor"
export WIFI_OBJECT="Device.WiFi"
export RADIO_OBJECT="Device.WiFi.Radio"
export RADIO_VENDOR_OBJECT="${RADIO_OBJECT}.${TR181_VENDOR}"
export RADIO_STATS_OBJECT="Device.WiFi.Radio.Stats"
export NEIGHBORING_OBJECT="Device.WiFi.NeighboringWiFiDiagnostic"
export NEIGHBORING_RESULT_OBJECT="Device.WiFi.NeighboringWiFiDiagnostic.Result"
export SSID_OBJECT="Device.WiFi.SSID"
export SSID_STATS_OBJECT="Device.WiFi.SSID.Stats"
export ACCESSPOINT_OBJECT="Device.WiFi.AccessPoint"
export ACCESSPOINT_VENDOR_OBJECT="${ACCESSPOINT_OBJECT}.${TR181_VENDOR}"
export ACCESSPOINT_SECURITY_OBJECT="Device.WiFi.AccessPoint.Security"
export ACCESSPOINT_WPS_OBJECT="Device.WiFi.AccessPoint.WPS"
export ACCESSPOINT_WPS_VENDOR_OBJECT="${ACCESSPOINT_VENDOR_OBJECT}.WPS"
export ACCESSPOINT_ASSOCIATED_DEVICES_OBJECT="Device.WiFi.AccessPoint.AssociatedDevice"
export ACCESSPOINT_ASSOCIATED_DEVICES_STATS_OBJECT="Device.WiFi.AccessPoint.AssociatedDevice.Stats"
export ACCESSPOINT_AC_OBJECT="Device.WiFi.AccessPoint.AC"
export ACCESSPOINT_AC_STATS_OBJECT="Device.WiFi.AccessPoint.AC.Stats"
export HS20_OBJECT="Device.WiFi.AccessPoint.HS20"
export DEVICE_INFO_OBJECT="Device.DeviceInfo"

# Start the led manager on the dongle
#LEDMAN=0

# Dongle has no pushbutton for WPS
#WPS_PB=1

# Don't use lower memory usage - use default net queues
#LOWER_MEM_USAGE=1

# Don't mangle multicast packets (this is done on STAR to reduce CPU utilization)
#MULTICAST_MANGLING=0

# GPIO pins used for output LEDs
#GPIO_LEDS=0,3
# GPIO pins used for pushbuttons
#GPIO_PUSHBUTTONS=1,13

# TODO: This info is also available from HW.ini so maybe remove here, and use host_api get to read
# 1 = trigger on PBC release
export pbc_wps_trigger=1

print2log() {
    case $1 in
	INFO)
		[ $log_level -ge 2 ] && echo "$2" > $log_path
		;;
	DBG)
		[ $log_level -ge 3 ] && echo "$2" > $log_path
		;;
	*)
		echo $1 $2 > /dev/console
		;;
    esac
}

# timestamp function for profiling.
# Results added to $WAVE_TMP/wlanprofiling.log
timestamp() {
	[ -z "$WLAN_PROFILING" ] && return
	SECS=`date +%s`
	[ -n $1 ] && PREFIX="[$1]"
	echo ${PREFIX}${SECS} >> $WAVE_TMP/wlanprofiling.log
}

# Get WPS on/off state: 0=off, 1=on
get_wps_on()
{
	interface_name=$1

	wps_state=`awk -F '=' '/wps_state/ {print $2}' $CONFIGS_PATH/hostapd_$interface_name.conf`
	if [ -n "$wps_state" ]; then
		wps_on=$NO
		[ $wps_state != 0 ] && wps_on=$YES
	fi
	echo $wps_on
}

# Converts ascii to hex
ascii2hex() {
	ascii_X=$1
	ascii_LEN=${#ascii_X}
	i=0
	while [ $i -lt $ascii_LEN ]; do
		ascii_char=${ascii_X:$i:1}
		printf '\\x%02x' "'$ascii_char" | sed 's/00/20/'
		i=$((i+1))
	done
}

# Check if the wlan interface is ready (eeprom/calibration file exist and ifconfig is working)
# If the interface is up, return 0, else return 1.
check_if_is_ready() {
	interface_name=$1
	parent_if=${interface_name%%.*}
	# Check if the hw_wlan.ini file exists for this interface, file is created if eeprom/calibration file exist.
	if [ ! -e $CONFIGS_PATH/hw_$parent_if.ini ]; then
		print2log DBG "fapi_wlan_wave_init_platform.sh: hw_$parent_if.ini is missing"
		echo 1
		return
	fi
	# Check if the wlan interface already exists.
	ifconfig_status=`ifconfig $interface_name`
	if [ $? -ne 0 ]; then
		print2log DBG "fapi_wlan_wave_init_platform.sh: $interface_name interface not ready."
		echo 1
		return
	fi
	echo 0
}

# Check if failure files exist
check_failure() {
	interface_name=$1
	if [ -e $wave_init_failure ] || [ -e $wave_start_failure ]; then
		echo false
	else
		echo true
	fi
}

error_message() {
	error_type=$1
	[ "$error_type" = "FW_CRASH" ] && echo "ERROR API execution: $error_type error" > /dev/console
}

# wrap_file
# validate complete file configuration
# Assume:
#	dummy API at the end of file. It gives the time to get fw crash indication from driver on file send.
# Detect FW Crash and resend file if found one.
# params format:
#	$1=script
# stages before send file:
#	entry param: $WAVE_TMP/set_driver_params_<interface name>.sh
#	extract interface name: remove file extension ".sh" and all text before interface name.
#	Source $RECOVERY_CONF to get the FW recovery parameters.
#	read FW Crash count before
#	execute file $WAVE_TMP/set_driver_params_X.sh
#	test for FW Crash: read FW Crash count and compare to previous value
wrap_file() {
	file=$1
	ret=0
	count_errors=0

	# Remove all that is before interface name, delete file extension (.sh)
	# example: $WAVE_TMP/set_driver_params_wlan0.3.sh gives wlan0.3
	interface_name=${file##*_}
	interface_name=${index%.*}

	# Extract the ohysical interface name
	parent_if=${interface_name%%.*}

	# Source conf file for FW recovery parameters
	#. $WAVE_TMP/FW_recovery_params
	# read FW Crash count before
	eval fw_fails_before=\${numFwFails_${interface_name}}

	#execute file and read if error on last command:
	$file
	eval fw_fails=\${numFwFails_${interface_name}}

	if [ "$fw_fails" != "$fw_fails_before" ]; then
		# Error occurred - initialize params needed for retries
		eval fast_recovery_limit=\${fastRecoveryLimit_${interface_name}}
		eval full_recovery_limit=\${fullRecovrylimit_${interface_name}}
		api_resend_max=$((fast_recovery_limit+full_recovery_limit))

		# Is retry allowed?
		[ "$api_resend_max" = "0" ] && error_message "UNKNOWN" && return 1
	fi

	while [ "$fw_fails" != "$fw_fails_before" ]; do
		count_errors=$((count_errors+1))
		if [ $count_errors -gt $api_resend_max ]; then
			# Max Count ERROR
			error_message FW_CRASH
			ret=1
			break
		fi
		eval fw_fails_before=\${numFwFails_${interface_name}}

		#execute file again:
		$file

		eval fw_fails_before=\${numFwFails_${interface_name}}
	done
	return $ret
}

# wrap_param: handle param get/set requests
# Assume:
#	- no double quote on comamnd in the scripts
# $*= set/get command...
# stages before send command (example: ifconfig command):
#	set ifconfig wlan0 down - params
#	"ifconfig wlan0 down"	 - after remove type
wrap_param() {
	##**********************************************#
	# TIME CRITICAL AREA							#
	TYPE=$1
	# Remove TYPE
	shift
	# Run command and test for error:
	ret=`eval "$*"`
	error=$?

	if [ "$error" != "0" ]; then
		##**********************************************#
		# NON-TIME CRITICAL AREA						#
		# Send to handle error and API status
		ret=`wrap_error_handler $*`
		error=$?
	fi

	##**********************************************#
	# TIME CRITICAL AREA							#
	# Return data from API call:
	[ "$TYPE" = "get" ] && [ "$error" = "0" ] && [ ! -z "$ret" ] && echo "$ret"
	return $error
}

# wrap_error_handler - handler API error
# Note- it regards wlan0 to always exist ! if command is not interface related, it uses wlan0 to get config 
wrap_error_handler() {
	count_errors=0
	ret_error=0

	#################################################################
	# PRE-STATE														#
	#################################################################
	# One time initialization to do when error occurred:
	error_cause=NO_ERROR
	interface_name=$2
	# Extract the physical interface name
	parent_if=${interface_name%%.*}

	# Detect if interface given or a generic API, by checking if arg2 contains wlanX, 
	# then get FW Crash value before exec API, save it and compare to after API sent
	per_interface=`echo $parent_if | grep wlan`
	if [ -z "$per_interface" ]; then
		# parent_if is empty - set it to wlan0
		parent_if=wlan0
		eval fw_fails_before=\${numFwFails_${parent_if}}
		num_interfaces=$numOfInterfaces
		[ "$num_interfaces" = "2" ] && fw_fails_before_if1=$numFwFails_wlan1
	else
		eval fw_fails_before=\${numFwFails_${parent_if}}
	fi
	# Read max retries (max=fast+full recovery limits), exit if equal current resends
	eval fast_recovery_limit=\${fastRecoveryLimit_${parent_if}}
	eval full_recovery_limit=\${fullRecovrylimit_${parent_if}}
	api_resend_max=$((fast_recovery_limit+full_recovery_limit))

	# Config to first state
	sm_state=SEND_API
	if [ "$api_resend_max" = "0" ]; then
		sm_state=DONE_ERROR_HANDLING
		ret_error=1
	fi

	#################################################################
	# STATE MACHINE - FW RECOVERY ERROR HANDLING					#
	#################################################################
	while [ true ]; do
		case $sm_state in
		SEND_API)
			count_errors=$((count_errors+1))
			ret=`eval "$*"`
			error=$?
			# Config to next state according to command status
			sm_state=DONE_ERROR_HANDLING
			[ $error -gt 0 ] && sm_state=RE_SEND_API
			;;
		RE_SEND_API)
			count_errors=$((count_errors+1))
			ret=`eval "$*"`
			error=$?
			# Config to next state according to command status
			sm_state=DONE_ERROR_HANDLING
			[ $error -gt 0 ] && sm_state=FW_CRASH_DETECT
			;;
		FW_CRASH_DETECT)
			fw_crash=0
			## TEST: ################################
			#let inc=$inc+1
			#host_api set $$ $parent_if numFwFails $inc
			## TEST END #############################
			eval fw_fails=\${numFwFails_${parent_if}}
			[ "$fw_fails_before" != "$fw_fails" ] && fw_crash=1

			if [ ! -z "$fw_fails_before_if1" ]; then
				fw_fails_if1=$numFwFails_wlan1
				[ "$fw_fails_before_if1" != "$fw_fails_if1" ] && fw_crash=1
			fi

			# Config to next state according to command status
			sm_state=MAX_RETRY_DETECT
			if [ "$fw_crash" = "0" ]; then
				# Configuration error detected. Exit, make sure to report error type before
				error_cause=CONFIGURATION
				ret_error=1
				sm_state=DONE_ERROR_HANDLING
			fi
			;;
		MAX_RETRY_DETECT)
			# Save last read fails for next iteration
			fw_fails_before=$fw_fails
			fw_fails_before_if1=$fw_fails_if1
			# Config to next state according to command status
			sm_state=SEND_API
			if [ $count_errors -gt $api_resend_max ]; then
				# Exit, make sure to report error type before
				error_cause=FW_CRASH
				ret_error=1
				sm_state=DONE_ERROR_HANDLING
			fi
			;;
		DONE_ERROR_HANDLING)
			# Report status
			error_message $error_cause
			break
			;;
		esac
	done

	# Return data from API call:
	echo "$ret"
	return $ret_error
}

# General on wrapper flow:
# command format: <type> <command> [<wlan interface> <processing>]
#   type=get/set/file, command=iwpriv/ifconfig etc. or set_driver_params_wlanX.sh
#   command=iwpriv/ifconfig etc. or set_driver_params_wlanX.sh [<wlanX/X>]
#   interface=wlanX
#   processing= | grep "string", etc. (processing only for 'get' type)
# stages over command handling:
#   handle=api_wrapper: "set ifconfig wlan0 down", save type
#          remove type from command line and send command to wrap_param().
#   handle=wrap_param: "ifconfig wlan0 down", get index from interface for use by handler,
#          exec command and handle error accordingly.
api_wrapper() {
	#keep type for later use
	TYPE=$1
	#remove first parameter - type
	shift
	#execute handler according to command type
	if [ "$TYPE" = "file" ]; then
		wrap_file $*
	else
		wrap_param $TYPE $*
	fi
	ret=$?
	return $ret
}

check_internal_switch() {
	# Check if the wireless is connected to internal switch.
	# When internal switch doesn't exist (grx500 platforms), don't call the switch_cli command.
	# TODO: We lack a better method of detecting this, look at platform type.
	if [ "$CONFIG_IFX_CONFIG_CPU" = "GRX500" ]; then
		echo false
	else
		echo true
	fi
}

# Run switch_cli command if supported by the platform
safe_switch_cli () {
	has_internal_switch=`check_internal_switch`
	[ "$has_internal_switch" = "true" ] && switch_cli $@
}

ppa_add_if() {
	interface_name=$1

	print2log DBG "fapi_wlan_wave_init_platform.sh: in ppa_add_if $interface_name"

	# Check if PPA is hooked, if not, don't add interface to PPA
	ppa_not_init=`cat /proc/ppa/api/hook | grep "not init" -c`
	print2log DBG "fapi_wlan_wave_init_platform.sh: in ppa_add_if, ppa_not_init=$ppa_not_init"
	if [ "$ppa_not_init" != "0" ]; then
		print2log DBG "PPA is not initialized. Skip adding $interface_name interface to PPA"
		return
	fi
	api_wrapper set iwpriv $interface_name sIpxPpaEnabled 1
	ppacmd addlan -i $interface_name
	# TODO: Does the switch API need to be done for all interfaces, or only interfaces in the HW PPA (i.e. portid > 0)?
	nPortId=`ppacmd getportid -i $interface_name | sed 's/The.* is //'`
	if [ "$nPortId" -gt "0" ]; then
		nPortId=$((nPortId+4))
		safe_switch_cli IFX_ETHSW_PORT_CFG_SET nPortId=$nPortId bLearningMAC_PortLock=1
	fi
}

ppa_del_if() {
	interface_name=$1

	ppacmd getportid -i $interface_name > /dev/null
	if [ $? -eq 0 ]; then
		nPortId=`ppacmd getportid -i $interface_name | sed 's/The.* is //'`
		if [ "$nPortId" -gt "0" ]; then
			nPortId=$((nPortId+4))
			safe_switch_cli IFX_ETHSW_PORT_CFG_SET nPortId=$nPortId bLearningMAC_PortLock=0
		fi
		ppacmd dellan -i $interface_name
	fi
}

# Check if an interface is VAP by searchig dot in the interface name.
check_is_vap() {
	interface_name=$1
	dot_location=`expr index $interface_name .`

	if [ $dot_location -gt 0 ]; then
		echo true
	else
		echo false
	fi
}

# Parse ifconfig result to extract the interfaces names
get_interfaces_from_ifconfig() {
	ifconfig_res=$1
	ifconfig_radio_vaps=""

	# The ifconfig_res file has each interface found in a separate line.
	# The interface name is the first word in the line
	while read cur_interface other_info; do
		ifconfig_radio_vaps="$ifconfig_radio_vaps $cur_interface"
	done < $ifconfig_res
	echo "$ifconfig_radio_vaps"
}

# Find the index of a param in the fapi_wlan_wave_in.conf
# Gets 2 arguments:
# param_name: the name of the parameter to find its index
# param_value: the expected value for the searched parameter
map_param_index() {
	param_name=$1
	param_value=$2

	index=0
	found="no"

	# Source fapi_wlan_wave_in.conf
	. ${IN_CONF}

	current_param=${param_name}_${index}
	eval current_value=\$$current_param
	current_value=$(printf "%b" "$current_value")
	if [ "$current_value" = "$param_value" ]; then
		found="yes"
	else
		index=$((index+1))
	fi

	while [ "$current_value" ] && [ "$found" = "no" ]; do
		current_param=${param_name}_${index}
		eval current_value=\$$current_param
		current_value=$(printf "%b" "$current_value")
		if [ "$current_value" = "$param_value" ]; then
			found="yes"
		else
			index=$((index+1))
		fi
	done

	# If parameter wasn't found, return empty value.
	[ "$found" = "no" ] && index=""

	echo $index
}

# Get the next available object index from the fapi_wlan_wave_in.conf
# The in.conf file is always written in order, so the last onject index will be in the last line.
get_next_object_index() {
	# Read the last line in the file
	last_line=`tail -n 1 ${IN_CONF}`
	last_index=${last_line%%=*}
	last_index=${last_index##*_}
	if [ -z "$last_index" ]; then
		next_index=0
	else
		next_index=$((last_index+1))
	fi

	echo $next_index
}

# Get the value of a parameter from the fapi_wlan_wave_in.conf
# The parameter name is the scripts parameter name and the returned value is a scripts parameter value.
# The function will source the fapi_wlan_wave_in.conf and will call the proper conversion script according to the object name.
# The specific object name is extracted from the full Object in the conf file (Device...).
get_conf_param() {
	param_name=$1
	object_index=$2
	interface_name=$3
	object2_index=$4
	object3_index=$5

	# Source the fapi_wlan_wave_in.conf
	. ${IN_CONF}

	# Select the corresponding conversion script according to the object in the conf file
	eval object_name=\${Object_${object_index}}
	object_name=$(printf "%b" "$object_name")
	case "$object_name" in
		"$RADIO_OBJECT"|"$RADIO_VENDOR_OBJECT") convert_script="fapi_wlan_wave_convert_radio.sh" ;;
		"$SSID_OBJECT") convert_script="fapi_wlan_wave_convert_ssid.sh" ;;
		"$ACCESSPOINT_OBJECT"|"$ACCESSPOINT_VENDOR_OBJECT") convert_script="fapi_wlan_wave_convert_access_point.sh" ;;
		"$ACCESSPOINT_SECURITY_OBJECT") convert_script="fapi_wlan_wave_convert_access_point_security.sh" ;;
		"$ACCESSPOINT_WPS_OBJECT"|"$ACCESSPOINT_WPS_VENDOR_OBJECT"|"$DEVICE_INFO_OBJECT") convert_script="fapi_wlan_wave_convert_access_point_wps.sh" ;;
		"$ACCESSPOINT_AC_OBJECT") convert_script="fapi_wlan_wave_convert_access_point_ac.sh" ;;
		"$HS20_OBJECT") convert_script="fapi_wlan_wave_convert_access_point_hs20.sh" ;;
	esac

	# Read the parameter value by calling the conversion script of the current object.
	param_value=`(. ${ETC_PATH}/${convert_script}) $param_name $object_index $interface_name $object2_index $object3_index`

	echo "$param_value"
}

# Write output parameters to fapi_wlan_wave_out.conf
update_conf_out() {
	param=$1
	value="$2"

	echo "$param=$value" >> ${OUT_CONF}
}

# Remove parameters from the a conf file by creating a grep string command and execute it to update the conf file.
# The created grep command removes all the parameters in the params_list from the conf file and writes the result to the conf file.
# Example of the final grep command: grep -wv "param_1\|param_2\|param_3" hostapd.conf > temp_hostapd.conf
remove_params_from_conf() {
	params_list=$1
	conf_file=$2

	tmp_conf_file=${conf_file}_tmp
	grep_cmd="grep -wv \""
	for param in $params_list
	do
		grep_cmd=${grep_cmd}${param}\\\|
	done

	grep_cmd=${grep_cmd%\\\|}
	grep_cmd="${grep_cmd}\" $conf_file > $tmp_conf_file"
	eval $grep_cmd
	mv $tmp_conf_file $conf_file
}

# Check if the only parameters that were modified are CoCPower related.
# CoCPower related TR181 parameters are: (Radio) Enable and (Radio.Vendor) NumOfAntennas.
check_only_coc_power_changed(){
	res="yes"

	# Read the in.conf file line by line.
	# If a parameters that is not CoCPower related was found, return "no"
	while read conf_line; do
		param=${conf_line%%_*}
		if [ "$param" != "Object" ] && [ "$param" != "Enable" ] && [ "$param" != "NumOfAntennas" ]; then
			res="no"
			break
		fi
	done < ${IN_CONF}
	echo "$res"
}

# Configure driver parameters for a specific object
configure_driver_params() {
	interface_name=$1
	object_index=$2
	params_list=$3
	params_list_file=$4
	object2_index=$5

	# Prepare params_list_file to be used by the driver_api.tcl
	cat /dev/null > $params_list_file

	# Try to read the values of all the scripts parameters that can be configured in the driver from the fapi_wlan_wave_in.conf file.
	# Parameters that are not in the fapi_wlan_wave_in.conf file will be empty and won't be set.
	for param in $params_list; do
		val=`get_conf_param $param $object_index $interface_name $object2_index`
		echo "$param=$val" >> $params_list_file
	done

	# Configure Radio parameters
	(. $ETC_PATH/fapi_wlan_wave_set_driver_params.sh reconfigure $interface_name $params_list_file)
}

# Find first unused VAP index - looks for holes in existing allocation, or adds at end
find_next_vap_name() {
	local radio_name radio_vaps next new
	radio_name=$1
	radio_vaps=`ifconfig -a |  awk -v radio_name=$radio_name '{if (match($1,radio_name".") == 1) {sub(radio_name".", "", $1); print  $1}}' | sort`
	new=0
	for next in $radio_vaps; do
		if [ $next -gt $new ]; then
			break
		fi
		let new=$next+1
	done
	echo "${radio_name}.$new"
}

# Check if an expected parameter is recieved
# If the input parameter is empty, print ALERT and exit execution
check_input_param() {
	script_name=$1
	param_name=$2
	param=$3

	[ -z "$param" ] && print2log ALERT "$script_name: $param_name is missing. exit execution" && exit 1
}

# Read the needed parameters from the fapi_wlan_wave_in.conf and save in a temp conf file
save_params() {
	object_index=$1
	interface_name=$2
	params_list=$3
	conf_name=${4}_${interface_name}

	modified_params=""

	for param in $params_list; do
		val=`get_conf_param $param $object_index $interface_name`
		if [ -n "$val" ]; then
			modified_params="$modified_params $param"
			echo "$param=$val" >> ${conf_name}_new
		fi
	done

	if [ -n "$modified_params" ]; then
		[ -e "$conf_name" ] && remove_params_from_conf "$modified_params" $conf_name
		cat ${conf_name}_new >> $conf_name
		rm -f ${conf_name}_new
	fi
}

# Read the FW recovery parameters from the fapi_wlan_wave_in.conf and save in a temp conf file
save_recovery_params() {
	radio_vendor_index=$1
	interface_name=$2

	fw_recovery_params="fast_recovery_enabled
	fast_recovery_limit
	full_recovery_enabled
	full_recovery_limit
	complete_recovery_enabled
	recovery_timer
	recovery_num_of_dumps"

	save_params $radio_vendor_index $interface_name "$fw_recovery_params" $RECOVERY_CONF
}

# Read the CoC parameters from the fapi_wlan_wave_in.conf and save in a temp conf file
save_coc_params() {
	radio_vendor_index=$1
	interface_name=$2

	coc_params="AutoCocEnabled
	NumOfAntennas"

	save_params $radio_vendor_index $interface_name "$coc_params" $COC_CONF
}

# Read the channel number from the fapi_wlan_wave_in.conf and save in a temp conf file
save_channel_params() {
	radio_index=$1
	interface_name=$2

	channel_params="Channel_db"

	save_params $radio_index $interface_name "$channel_params" $CHANNEL_CONF
}

# Read the VAP STA limits values from the fapi_wlan_wave_in.conf and save in a temp conf file
# If an index is missing, value -1 is used for index
save_sta_limits_params() {
	accesspoint_index=$1
	accesspoint_vendor_index=$2
	interface_name=$3

	vap_limits_ap_params="maxSta"

	vap_limits_ap_vendor_params="minSta"

	[ "$accesspoint_index" != "-1" ] && save_params $accesspoint_index $interface_name "$vap_limits_ap_params" $STA_LIMITS_CONF
	[ "$accesspoint_vendor_index" != "-1" ] && save_params $accesspoint_vendor_index $interface_name "$vap_limits_ap_vendor_params" $STA_LIMITS_CONF
}
FAPI_WLAN_WAVE_INIT_PLATFORM="1"
