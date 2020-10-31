#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

print2log DBG "fapi_wlan_wave_init_hostapd.sh: args: $*"

command=$1
interface_name=$2
object_index=$3
params_list=$4
vendor_object_index=$5
device_info_index=$6

# Temporary assistive conf files
tmp_hostapd="${WAVE_TMP}/temp_hostapd.conf"
new_params_hostapd="${WAVE_TMP}/new_params_hostapd.conf"
	
timestamp "fapi_wlan_wave_init_hostapd.sh:$command:$interface_name:begin"

start_hostapd() {
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Start"
	timestamp "fapi_wlan_wave_init_hostapd.sh:start_func:$interface_name:begin"
	
	# If hostapd is already running, do run it again
	hostapd_count=`ps | grep "\<hostapd_$interface_name\> " | grep -vc grep`			
	[ $hostapd_count != 0 ] && return

	# Create links for hostapd related files
	cp -s $BINDIR/hostapd /tmp/hostapd
	cp -s $BINDIR/hostapd.eap_user /tmp/hostapd.eap_user
	cp -s $BINDIR/hostapd_cli /tmp/hostapd_cli
	cp -s $CONFIGS_PATH/hostapd_$interface_name.conf /tmp/hostapd_$interface_name.conf
	cp -s $BINDIR/hostapd /tmp/hostapd_$interface_name
	# Execute the hostapd
	/tmp/hostapd_$interface_name $CONFIGS_PATH/hostapd_$interface_name.conf -e /tmp/hostapd_ent_$interface_name -B > /dev/console 2>/dev/console

	# Print message if hostapd start failed and exit execution.
	if [ $? -ne 0 ]; then
		print2log ALERT "fapi_wlan_wave_init_hostapd.sh: hostapd failed to start for $interface_name"
		exit
	fi
	
	# Send out INIT event in order to initialize WLAN station LEDs
	( . $HOSTAPD_EVENTS_SCRIPT $interface_name WLAN-MODE-INIT ap )
	
	# Currently, hostapd_cli is used only for WPS external registrar events.
	# Optimization: Read WPS state from the config file instead of from host_api to reduce overhead
	wps_on=`get_wps_on $interface_name`
	[ "$wps_on" != "$NO" ] && hostapd_cli -i$interface_name -a$HOSTAPD_EVENTS_SCRIPT -B

	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Done Start"
	timestamp "fapi_wlan_wave_init_hostapd.sh:start_func:$interface_name:done"
}

stop_hostapd() {
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Stop"
	timestamp "fapi_wlan_wave_init_hostapd.sh:stop_func:$interface_name:begin"
	
	brutal_kill=$NO
	
	hostapd_count=`ps | grep "\<hostapd_$interface_name\> " | grep -vc grep`
	hostapd_cli_count=`ps | grep "\<hostapd_cli -i$interface_name\> " | grep -vc grep`
	if [ $hostapd_cli_count != 0 ]; then
		hostapd_cli_ps=`ps | grep "\<hostapd_cli -i$interface_name\> " | grep -v grep | awk '{print $1}'`
		for ps in $hostapd_cli_ps; do
			print2log DBG "fapi_wlan_wave_init_hostapd.sh: executing kill on hostapd_cli process $ps"
			kill $ps
		done
	fi
	if [ $hostapd_count != 0 ]; then
		hostapd_ps=`ps | grep "\<hostapd_$interface_name\> " | grep -v grep | awk '{print $1}'`
		for ps in $hostapd_ps; do
			print2log DBG "fapi_wlan_wave_init_hostapd.sh: executing regular kill on hostapd_$interface_name process $ps"
			kill $ps
			# give 1 sec delay to avoid false execution of "kill -9"
			sleep 1
		done
		# Repeating the loop in case hostapd was not down
		hostapd_ps=`ps | grep "\<hostapd_$interface_name\> " | grep -v grep | awk '{print $1}'`
		for ps in $hostapd_ps; do
			print2log DBG "fapi_wlan_wave_init_hostapd.sh: executing kill -9 on hostapd_$interface_name process $ps"
			brutal_kill=$YES
			kill -9 $ps
		done
		if [ "$brutal_kill" = "$YES" ]; then
			rm -f /var/run/hostapd/$interface_name
		fi
	fi
	[ -e $WPS_PIN_TEMP_FILE ] && rm $WPS_PIN_TEMP_FILE
	[ -e $WPS_MAC_TEMP_FILE ] && rm $WPS_MAC_TEMP_FILE
	# Wait until hostapd is completely dead
	hostapd_count=`ps | grep "\<hostapd_$interface_name\> " | grep -vc grep`
	while [ $hostapd_count != 0 ]; do
		sleep 1
		hostapd_count=`ps | grep "\<hostapd_$interface_name\> " | grep -vc grep`
	done
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Done stop"
	timestamp "fapi_wlan_wave_init_hostapd.sh:stop_func:$interface_name:done"
}

stop_radio_hostapd() {
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Stop radio"
	timestamp "fapi_wlan_wave_init_hostapd.sh:stop_radio_func:$interface_name:begin"
	
	brutal_kill=$NO
	
	hostapd_count=`ps | grep "\<hostapd_$interface_name\>" | grep -vc grep`
	hostapd_cli_count=`ps | grep "\<hostapd_cli -i$interface_name\>" | grep -vc grep`
	if [ $hostapd_cli_count != 0 ]; then
		hostapd_cli_ps=`ps | grep "\<hostapd_cli -i$interface_name\>" | grep -v grep | awk '{print $1}'`
		for ps in $hostapd_cli_ps; do
			print2log DBG "fapi_wlan_wave_init_hostapd.sh: executing kill on hostapd_cli process $ps"
			kill $ps
		done
	fi
	if [ $hostapd_count != 0 ]; then
		hostapd_ps=`ps | grep "\<hostapd_$interface_name\>" | grep -v grep | awk '{print $1}'`
		for ps in $hostapd_ps; do
			print2log DBG "fapi_wlan_wave_init_hostapd.sh: executing regular kill on radio related hostapd_$interface_name process $ps"
			kill $ps
			# give 1 sec delay to avoid false execution of "kill -9"
			sleep 1
		done
		# Repeating the loop in case hostapd was not down
		hostapd_ps=`ps | grep "\<hostapd_$interface_name\>" | grep -v grep | awk '{print $1}'`
		for ps in $hostapd_ps; do
			print2log DBG "fapi_wlan_wave_init_hostapd.sh: executing kill -9 on radio related hostapd_$interface_name process $ps"
			brutal_kill=$YES
			kill -9 $ps
		done
		if [ "$brutal_kill" = "$YES" ]; then
			rm -f /var/run/hostapd/${interface_name}*
		fi
	fi
	[ -e $WPS_PIN_TEMP_FILE ] && rm $WPS_PIN_TEMP_FILE
	[ -e $WPS_MAC_TEMP_FILE ] && rm $WPS_MAC_TEMP_FILE
	# Wait until hostapd is completely dead
	hostapd_count=`ps | grep "\<hostapd_$interface_name\>" | grep -vc grep`
	while [ $hostapd_count != 0 ]; do
		sleep 1
		hostapd_count=`ps | grep "\<hostapd_$interface_name\>" | grep -vc grep`
	done
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Done stop radio"
	timestamp "fapi_wlan_wave_init_hostapd.sh:stop_radio_func:$interface_name:done"
}

reconfigure_hostapd() {
	# WARNING: Reconfiguring with -HUP does not work in every case - e.g. WEP cannot be configured with this method (due to hostapd limitation)
	PID=`ps | awk -v wlan=$interface_name '{if ($5 == "/tmp/hostapd_"wlan)  print $1}'`
	kill -HUP $PID
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Done Reconfigure "
}


write_param_wlhsop()
{
	prefix=$1
	#ignore $2 and $3
	cpid=$4

	print2log HS20 "write_param_wlhsop: cpid=$cpid"
	#Get all params in order:
	eval val=\${${prefix}_${cpid}_osuProvServerUri}
	# Special chars for osuIconType
	val=$(printf "%b" "$val")
	echo "osu_server_uri=$val" >> $tmp_hostapd
	eval val=\${${prefix}_${cpid}_osuProvFriendName}
	# Special chars for osuIconType
	val=$(printf "%b" "$val")
	echo "osu_friendly_name=$val" >> $tmp_hostapd
	eval val=\${${prefix}_${cpid}_osuProvFriendName2}
	if [ -n "$val" ]; then
		# Special chars for osuIconType
		val=$(printf "%b" "$val")
		echo "osu_friendly_name=$val" >> $tmp_hostapd
	fi
	eval val=\${${prefix}_${cpid}_osuProvNai}
	if [ -n "$val" ]; then
		# Special chars for osuIconType
		val=$(printf "%b" "$val")
		echo "osu_nai=$val" >> $tmp_hostapd
	fi
	eval val=\${${prefix}_${cpid}_osuProvMethod}
	echo "osu_method_list=$val" >> $tmp_hostapd
	eval val=\${${prefix}_${cpid}_osuProvIcon}
	echo "osu_icon=$val" >> $tmp_hostapd
	eval val=\${${prefix}_${cpid}_osuProvIcon2}
	if [ -n "$val" ]; then
		echo "osu_icon=$val" >> $tmp_hostapd
	fi
	eval val=\${${prefix}_${cpid}_osuProvServiceDesc}
	# Special chars for osuIconType
	val=$(printf "%b" "$val")
	echo "osu_service_desc=$val" >> $tmp_hostapd
	eval val=\${${prefix}_${cpid}_osuProvServiceDesc2}
	if [ -n "$val" ]; then
		# Special chars for osuIconType
		val=$(printf "%b" "$val")
		echo "osu_service_desc=$val" >> $tmp_hostapd
	fi
}

write_param_wlhsoi()
{
	prefix=$1
	#ignore $2 and $3
	cpid=$4

	print2log HS20 "write_param_wlhsoi: cpid=$cpid"
	#Get all params in order:
	eval val=\${${prefix}_${cpid}_osuIconWidth}
	osu_icons_val="${val}"
	eval val=\${${prefix}_${cpid}_osuIconHeight}
	osu_icons_val="${osu_icons_val}:${val}"
	eval val=\${${prefix}_${cpid}_osuIconCode}
	osu_icons_val="${osu_icons_val}:${val}"
	eval val=\${${prefix}_${cpid}_osuIconType}
	# Special chars for osuIconType
	val=$(printf "%b" "$val")
	osu_icons_val="${osu_icons_val}:${val}"
	eval val=\${${prefix}_${cpid}_osuIconName}
	osu_icons_val="${osu_icons_val}:${val}"
	eval val=\${${prefix}_${cpid}_osuIconFile}
	osu_icons_val="${osu_icons_val}:${val}"
	eval val=\${${prefix}_${cpid}_osuIconFile}
	# Special chars for osuIconFile
	val=$(printf "%b" "$val")
	osu_icons_val="${osu_icons_val}:${val}"
	print2log HS20 "write_conf_osu_icons: hs20_icon=$osu_icons_val"
	echo "hs20_icon=$osu_icons_val" >> $tmp_hostapd
}

# Special action: concatenate to a vector ',' as a seperator
# keep parameter in vector, write it when finished
domain_name_indexes=""
write_param_wlhsdn()
{
	prefix=$1
	rc_conf_name=$2
	hostapd_param=$3
	cpid=$4

	count=`echo $domain_name_indexes | wc -w`
	print2log HS20 "write_conf_domain_name: cpid=$cpid, count=$count"
	eval val=\${${prefix}_${cpid}_${rc_conf_name}}
	# handle special chars
	val=$(printf "%b" "$val")
	print2log HS20 "val=$val"
	if [ -n "$val" ]; then
		print2log HS20 "conf param: $hostapd_param=$val"
		if [ "$count" = "1" ]; then
			print2log HS20 "val=$val"
			echo "$hostapd_param=$val" >> $tmp_hostapd
		else
			#concatenate names:
			wlhsdn_vector="${wlhsdn_vector},${val}"
			# if it is the last index then write to conf file
			last_index=`echo $domain_name_indexes | awk '{print $NF}'`
			print2log HS20 "last_index=$last_index" > /dev/console
			if [ "$cpid" = "$last_index" ]; then
				print2log HS20 "last index, $hostapd_param=$wlhsdn_vector"
				echo "$hostapd_param=$wlhsdn_vector" >> $tmp_hostapd
			fi
		fi
	fi
}


# TODO: change wlhs2 to other name for "IP Address Type"
# perform bitwise operations to glue two values togheter
write_param_wlhs2()
{
	prefix=$1
	rc_conf_name1=$2
	hostapd_param=$3
	cpid=$4
	rc_conf_name2=ipv6AddrType

	print2log HS20 "write_param_wlhs2: cpid=$cpid"
	eval val1=\${${prefix}_${cpid}_${rc_conf_name1}}
	eval val2=\${${prefix}_${cpid}_${rc_conf_name2}}
	print2log HS20 "val1=$val1, val2=$val2"
	if [ -n "$val1" ]; then
		#(ipv4_type & 0x3f) << 2 | (ipv6_type & 0x3)
		let "masked=$val1&63"
		echo "masked=$masked"
		let "shifted=$masked<<2"
		echo "shifted=$shifted"
		let "masked_6=$val2&3"
		echo "masked_6=$masked_6"
		let "val=$shifted | $masked_6"
		echo "val=$val"	
		val=`echo $val | awk '{printf "0%-X\n", $0}'`
		print2log HS20 "conf param: $hostapd_param=$val"
		echo "$hostapd_param=$val" >> $tmp_hostapd
	fi
}

write_param_wlhsnr()
{
	prefix=$1
	rc_conf_name=$2
	hostapd_param=$3
	cpid=$4
	rc_conf_eap1=naiRealmEap1
	rc_conf_eap2=naiRealmEap2
	#clear params:
	Eap1=""
	Eap2=""

	print2log HS20 "write_param_wlhsnr: cpid=$cpid"
	eval val1=\${${prefix}_${cpid}_${rc_conf_name}}
	eval val2=\${${prefix}_${cpid}_${rc_conf_eap1}}
	eval val3=\${${prefix}_${cpid}_${rc_conf_eap2}}

	if [ -n "$val1" ]; then
		realm_name=$val1
		realm_name=$(printf "%b" "$realm_name")
		realm_eap1=$val2
		realm_eap2=$val3
		print2log HS20 "ugw values: realm_name=$realm_name, realm_eap1=$realm_eap1, realm_eap2=$realm_eap2"
		if [ "$realm_eap1" = "0" ]; then
			Eap1=,13[5:6]
		elif [ "$realm_eap1" = "1" ]; then
			Eap1=,21[2:4][5:7]
		fi
		if [ "$realm_eap2" = "0" ]; then
			Eap2=,13[5:6]
		elif [ "$realm_eap2" = "1" ]; then
			Eap2=,21[2:4][5:7]
		fi
		nai_realm="0,$realm_name$Eap1$Eap2"
		print2log HS20 "write conf param: nai_realm=$nai_realm"
		echo "nai_realm=$nai_realm" >> $tmp_hostapd
	fi
}

get_cpid_list()
{
	total_cpids=$1
	prefix=$2
	ap_index=$3
	cpid_list=""
	cpid=0
	let pcpeId_search=$ap_index+1
	# add '='
	total_cpids="$total_cpids"=
# TODO: use parameter directly, can't use rc.conf directly. Or use status oper
	count=`cat $CONFIGS_PATH/rc.conf | sed -n 's/'"$total_cpids"'//p'`
	eval count=$count
	print2log HS20 "get_cpid_list: total_cpids=$total_cpids, prefix=$prefix, ap_index=$ap_index, pcpeId_search=$pcpeId_search, ount=$count"
	
	while [ "$cpid" -lt "$count" ]
	do
		 print2log HS20 "while: cpid=$cpid, test ${prefix}_${cpid}_pcpeId"
		 eval pcpid_data=\${${prefix}_${cpid}_pcpeId}
		 print2log HS20 "pcpid_data=$pcpid_data"
		 if [ "$pcpid_data" = "$pcpeId_search" ]; then
		 	print2log HS20 "cpid=$cpid, cpid_list=$cpid_list"
		 	if [ -n "$cpid_list" ]; then
		 		cpid_list="${cpid_list} ${cpid}"
		 	else
		 		cpid_list="${cpid}"
		 	fi
		 fi
		 let cpid=$cpid+1
	done
	echo "$cpid_list"
}

write_param_wlhsrc()
{
	prefix=$1
	rc_conf_name=$2
	hostapd_param=$3
	cpid=$4
	let pcpeId_search=$ap_index+1
	eval pcpid_data=\${${prefix}_${cpid}_pcpeId}
	print2log HS20 "pcpid_data=$pcpid_data"
	if [ "$pcpid_data" = "$pcpeId_search" ]; then
		# can get the data
		print2log HS20 "write_param: cpid=$cpid"
		eval val=\${${prefix}_${cpid}_${rc_conf_name}}
		print2log HS20 "val=$val"
		if [ -n "$val" ]; then
			val=`echo $val | sed 's/-//g'`
			print2log HS20 "conf param: $hostapd_param=$val"
			echo "$hostapd_param=$val" >> $tmp_hostapd
		fi
	fi
}


write_param()
{
	prefix=$1
	rc_conf_name=$2
	hostapd_param=$3
	cpid=$4

	print2log HS20 "write_param: cpid=$cpid"
	eval val=\${${prefix}_${cpid}_${rc_conf_name}}
	print2log HS20 "val=$val"
	if [ -n "$val" ]; then
		print2log HS20 "conf param: $hostapd_param=$val"
		if [ "$hostapd_param" = "osu_method_list" ] && [ "$val" = "2" ]; then
			print2log HS20 "conf param: osu_method_list, map 2->1 0"
			echo "$hostapd_param=1 0" >> $tmp_hostapd
		elif [ "$hostapd_param" = "venue_name" ] || [ "$hostapd_param" = "hs20_oper_friendly_name" ]; then
			val=$(printf "%b" "$val")
			print2log HS20 "special chars for $hostapd_param, val=$val"
			echo "$hostapd_param=$val" >> $tmp_hostapd
		else
			echo "$hostapd_param=$val" >> $tmp_hostapd
		fi
	fi
}


write_conf()
{
	prefix=$1
	rc_conf_name=$2
	hostapd_param=$3
	cpid_list="$4"
	special=$5
	print2log HS20 "write_conf: prefix=$prefix, rc_conf_name=$rc_conf_name, hostapd_param=$hostapd_param, cpid_list=$cpid_list"
	
	for cpid in $cpid_list
	do
		echo "call write_param${special}" > /dev/console
		write_param${special} $prefix $rc_conf_name $hostapd_param $cpid
	done
}

# Find the wps_rf_bands according to the frequenct of the interface
get_wps_rf_bands() {

	wps_rf_bands=a
	network_mode=`iwpriv $interface_name gNetworkMode`
	network_mode=${network_mode##*:}
	[ $network_mode -gt 14 ] && wps_rf_bands=g
}

# Use 2 temporary conf files:
# 1) conf file that is a copy of the current conf file (if exists).
# 2) conf file containing the updated parameters
# The function will find which parameters were changed or need to be added and these parameters with the new values will be written to the conf file (2).
# When a parameter needs to be modified, the parameter name will be added to a list of modified parameters (hostapd names).
# After the list was created, a single grep command will be used to remove all the modified parameters from the conf file (1).
# Tho concatenation of conf file (1) and conf file (2) after all the modifications will replace the currently existing conf file.
create_config_hostapd() {
	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Create config"
	timestamp "fapi_wlan_wave_init_hostapd.sh:create_config_func:$interface_name:begin"
	
	# The list of the modified hostapd parameters
	modified_params=""
	# The hostapd conf file can be created from fresh or modified.
	# If hostapd configuration file doesn't exist for this interface, add preliminary common parameters
	# If hostapd configuration file exists, create a copy of the existing conf and modify it
	if [ ! -e "$CONFIGS_PATH/hostapd_${interface_name}.conf" ]; then
		echo "logger_syslog_level=3" > $tmp_hostapd
		echo "logger_stdout=-1" >> $tmp_hostapd
		echo "logger_stdout_level=2" >> $tmp_hostapd
		echo "ctrl_interface=/var/run/hostapd" >> $tmp_hostapd
		echo "ctrl_interface_group=0" >> $tmp_hostapd
		echo "interface=$interface_name" >> $tmp_hostapd
		echo "driver=mtlk" >> $tmp_hostapd
		echo "macaddr_acl=0" >> $tmp_hostapd
		echo "bridge=br-lan" >> $tmp_hostapd
		echo "ignore_broadcast_ssid=0" >> $tmp_hostapd
		echo "eapol_key_index_workaround=0" >> $tmp_hostapd
		echo "auth_algs=1" >> $tmp_hostapd
		# Generate new UUID for the interface and save it in a temp file
		uuid=`uuidgen`
		echo "uuid=$uuid" >> $tmp_hostapd
		echo "uuid=$uuid" > ${WAVE_TMP}/UUID_${interface_name}
	else
		cp $CONFIGS_PATH/hostapd_${interface_name}.conf $tmp_hostapd
	fi

	# Create a new empty temp file to which the updated parameters will be written
	cat /dev/null > $new_params_hostapd
	
	# Try to read the values of all the received scripts parameters that can be configured in the hostapd conf file from the fapi_wlan_wave_in.conf file.
	# Parameters that are not in the fapi_wlan_wave_in.conf file will be empty and won't be modified/set.
	for param in $params_list; do
		val=`get_conf_param $param $object_index $interface_name $vendor_object_index $device_info_index`
		eval $param='$val'
	done
	
	# Set the parameters that were modified
	# If a parameter was modified:
	# 1) Add the parameter name (hostapd name) to the modified parameters list.
	# 2) Write the new value to the new parameters temp conf file.
	
	# General parameters
	[ -n "$ESSID" ] && modified_params="$modified_params ssid" && echo "ssid=$ESSID" >> $new_params_hostapd
	
	# Security parameters
	# When changing a parameter in the security object, all the security object is received and all the security parameters are re-written to the conf.
	if [ -n "$SecurityMode" ]; then
		# Mark all the security parameters as modified
		modified_params="$modified_params wpa ieee80211w wpa_pairwise rsn_pairwise wpa_key_mgmt ieee8021x eap_server wep_default_key wep_key0 wpa_passphrase wpa_group_rekey wpa_gmk_rekey eap_reauth_period auth_server_addr auth_server_port auth_server_shared_secret"	

		wpa=""
		ieee80211w=""
		wpa_pairwise=""
		rsn_pairwise=""
		wpa_key_mgmt=""
		ieee8021x=""
		eap_server=""
		wep_default_key=""
		security_type=""
		case "$SecurityMode" in
			"None")
				eap_server=1
				security_type="open"
				;;
			"WEP-64"|"WEP-128")
				eap_server=1
				wep_default_key=0
				security_type="wep"
				;;
			"WPA-Personal")
				wpa=1
				wpa_pairwise="TKIP"
				wpa_key_mgmt="WPA-PSK"
				eap_server=1
				security_type="wpa-personal"
				;;
			"WPA2-Personal")
				wpa=2
				ieee80211w=1
				wpa_pairwise="CCMP"
				wpa_key_mgmt="WPA-PSK"
				eap_server=1
				security_type="wpa-personal"
				;;
			"WPA-WPA2-Personal")
				wpa=3
				wpa_pairwise="TKIP"
				rsn_pairwise="CCMP"
				wpa_key_mgmt="WPA-PSK"
				eap_server=1
				security_type="wpa-personal"
				;;
			"WPA-Enterprise")
				wpa=1
				wpa_pairwise="TKIP"
				wpa_key_mgmt="WPA-EAP"
				ieee8021x=1
				eap_server=0
				security_type="wpa-enterprise"
				;;
			"WPA2-Enterprise")
				wpa=2
				ieee80211w=1
				wpa_pairwise="CCMP"
				wpa_key_mgmt="WPA-EAP"
				ieee8021x=1
				eap_server=0
				security_type="wpa-enterprise"
				;;
			"WPA-WPA2-Enterprise")
				wpa=3
				wpa_pairwise="TKIP"
				rsn_pairwise="CCMP"
				wpa_key_mgmt="WPA-EAP"
				ieee8021x=1
				eap_server=0
				security_type="wpa-enterprise"
				;;
			# TODO: add osen
			#$osen)
			#	;;
		esac
		[ -n "$wpa" ] && echo "wpa=$wpa" >> $new_params_hostapd
		[ -n "$ieee80211w" ] && echo "ieee80211w=$ieee80211w" >> $new_params_hostapd
		[ -n "$wpa_pairwise" ] && echo "wpa_pairwise=$wpa_pairwise" >> $new_params_hostapd
		[ -n "$rsn_pairwise" ] && echo "rsn_pairwise=$rsn_pairwise" >> $new_params_hostapd
		[ -n "$wpa_key_mgmt" ] && echo "wpa_key_mgmt=$wpa_key_mgmt" >> $new_params_hostapd
		[ -n "$ieee8021x" ] && echo "ieee8021x=$ieee8021x" >> $new_params_hostapd
		[ -n "$eap_server" ] && echo "eap_server=$eap_server" >> $new_params_hostapd
		[ -n "$wep_default_key" ] && echo "wep_default_key=$wep_default_key" >> $new_params_hostapd
	fi
	[ -n "$WEPKey" ] && [ "$security_type" = "wep" ] && echo "wep_key0=$WEPKey" >> $new_params_hostapd
	if [ -n "$KeyPassphrase" ]; then
		if [ "$security_type" = "wpa-personal" ] || [ "$security_type" = "wpa-enterprise" ]; then
			echo "wpa_passphrase=$KeyPassphrase" >> $new_params_hostapd
		fi
	fi
	if [ -n "$RekeyingInterval" ]; then
		if [ "$security_type" = "wpa-personal" ] || [ "$security_type" = "wpa-enterprise" ]; then
			echo "wpa_group_rekey=$RekeyingInterval" >> $new_params_hostapd
			echo "wpa_gmk_rekey=$RekeyingInterval" >> $new_params_hostapd
			echo "eap_reauth_period=$RekeyingInterval" >> $new_params_hostapd
		fi
	fi
	if [ "$security_type" = "wpa-enterprise" ]; then
		[ -n "$RadiusServerIPAddr" ] && echo "auth_server_addr=$RadiusServerIPAddr" >> $new_params_hostapd
		[ -n "$RadiusServerPort" ] && echo "auth_server_port=$RadiusServerPort" >> $new_params_hostapd
		[ -n "$RadiusSecret" ] && echo "auth_server_shared_secret=$RadiusSecret" >> $new_params_hostapd
	fi
	
	# WPS parameters
	if [ -n "$WPSState" ]; then
		modified_params="$modified_params wps_state pbc_in_m1 ap_setup_locked wps_cred_processing wps_pin_requests wps_rf_bands upnp_iface os_version device_type"
		echo "wps_state=$WPSState" >> $new_params_hostapd
		if [ "$WPSState" != "0" ]; then
			echo "pbc_in_m1=1" >> $new_params_hostapd
			echo "ap_setup_locked=0" >> $new_params_hostapd
			echo "wps_cred_processing=2" >> $new_params_hostapd
			echo "wps_pin_requests=/var/run/hostapd.pin-req" >> $new_params_hostapd
			echo "upnp_iface=br-lan" >> $new_params_hostapd
			echo "os_version=01020300" >> $new_params_hostapd
			echo "device_type=6-0050F204-1" >> $new_params_hostapd
			get_wps_rf_bands
			echo "wps_rf_bands=$wps_rf_bands" >> $new_params_hostapd
		fi
	fi
	[ -n "$PIN" ] && modified_params="$modified_params PIN" && echo "ap_pin=$PIN" >> $new_params_hostapd
	[ -n "$Manufacturer" ] && modified_params="$modified_params manufacturer" && echo "manufacturer=$Manufacturer" >> $new_params_hostapd
	[ -n "$ModelName" ] && modified_params="$modified_params model_name" && echo "model_name=$ModelName" >> $new_params_hostapd
	[ -n "$DeviceName" ] && modified_params="$modified_params device_name" && echo "device_name=$DeviceName" >> $new_params_hostapd
	[ -n "$ModelNumber" ] && modified_params="$modified_params model_number" && echo "model_number=$ModelNumber" >> $new_params_hostapd
	[ -n "$SerialNumber" ] && modified_params="$modified_params serial_number" && echo "serial_number=$SerialNumber" >> $new_params_hostapd
	[ -n "$WPS2ConfigMethodsEnabled" ] && modified_params="$modified_params config_methods" && echo "config_methods=$WPS2ConfigMethodsEnabled" >> $new_params_hostapd
	[ -n "$FriendlyName" ] && modified_params="$modified_params friendly_name" && echo "friendly_name=$FriendlyName" >> $new_params_hostapd
	[ -n "$ManufacturerUrl" ] && modified_params="$modified_params manufacturer_url" && echo "manufacturer_url=$ManufacturerUrl" >> $new_params_hostapd

	# Hotspot2.0 parameters
	# TODO: currently, hotspot is disabled
	if [ ! -e "$CONFIGS_PATH/hostapd_${interface_name}.conf" ]; then
		echo "hs20=0" >> $tmp_hostapd
		#echo "tdls_prohibit=1" >> $tmp_hostapd
		#echo "interworking=1" >> $tmp_hostapd
		#echo "manage_p2p=1" >> $tmp_hostapd
		#echo "allow_cross_connection=0" >> $tmp_hostapd
		#echo "hs20_deauth_req_timeout=60" >> $tmp_hostapd
	fi
	[ -n "$AccessNetType" ] && modified_params="$modified_params access_network_type" && echo "access_network_type=$AccessNetType" >> $new_params_hostapd
	[ -n "$QosMap" ] && modified_params="$modified_params qos_map_set" && echo "qos_map_set=$QosMap" >> $new_params_hostapd
	[ -n "$InternetConnectivity" ] && modified_params="$modified_params internet" && echo "internet=$InternetConnectivity" >> $new_params_hostapd
	[ -n "$venueGroup" ] && modified_params="$modified_params venue_group" && echo "venue_group=$venueGroup" >> $new_params_hostapd
	[ -n "$venueType" ] && modified_params="$modified_params venue_type" && echo "venue_type=$venueType" >> $new_params_hostapd
	[ -n "$HESSID" ] && modified_params="$modified_params hessid" && echo "hessid=$HESSID" >> $new_params_hostapd
	[ -n "$NetAuthType" ] && modified_params="$modified_params network_auth_type" && echo "network_auth_type=$NetAuthType" >> $new_params_hostapd
	[ -n "$ThreeGpp" ] && modified_params="$modified_params anqp_3gpp_cell_net" && echo "anqp_3gpp_cell_net=$ThreeGpp" >> $new_params_hostapd
	[ -n "$DgafDisabled" ] && modified_params="$modified_params disable_dgaf" && echo "disable_dgaf=$DgafDisabled" >> $new_params_hostapd
	[ -n "$AnqpDomainId" ] && modified_params="$modified_params anqp_domain_id" && echo "anqp_domain_id=$AnqpDomainId" >> $new_params_hostapd
	[ -n "$OperatingClass" ] && modified_params="$modified_params hs20_operating_class" && echo "hs20_operating_class=$OperatingClass" >> $new_params_hostapd
	[ -n "$OsuSsid" ]&& modified_params="$modified_params osu_ssid" && echo "osu_ssid=$OsuSsid" >> $new_params_hostapd
	[ -n "$GasComebackDelay" ] && modified_params="$modified_params gas_comeback_delay" && echo "gas_comeback_delay=$GasComebackDelay" >> $new_params_hostapd
	
	# TODO: add HS2.0 special parameters handling.
	
	# Remove all the modified parameters from the conf file
	[ -n "$modified_params" ] && remove_params_from_conf "$modified_params" $tmp_hostapd
	# Write the modified/new parameters to the updated conf file
	cat $new_params_hostapd >> $tmp_hostapd
	rm $new_params_hostapd
	# Update the original hostapd conf file
	mv $tmp_hostapd $CONFIGS_PATH/hostapd_$interface_name.conf

	print2log DBG "fapi_wlan_wave_init_hostapd.sh: Done create config"
	timestamp "fapi_wlan_wave_init_hostapd.sh:create_config_func:$interface_name:done"
}

should_run_hostapd()
{
	return_status=`check_failure $interface_name`
}

case "$command" in
	start)
		start_hostapd
		;;
	stop)
		stop_hostapd
		;;
	stop_radio)
		stop_radio_hostapd
		;;
	reload)
		# Reload existing hostapd configuration without recreating config file
		stop_hostapd
		start_hostapd
		;;
	create_config)
		create_config_hostapd
		;;
	should_run)
		should_run_hostapd
		;;
esac

timestamp "fapi_wlan_wave_init_hostapd.sh:$command:$interface_name:done"
$return_status
