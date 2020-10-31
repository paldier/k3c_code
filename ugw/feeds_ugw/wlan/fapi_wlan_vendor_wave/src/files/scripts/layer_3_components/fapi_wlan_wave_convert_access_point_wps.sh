#!/bin/sh

# Source fapi_wlan_wave_in.conf
. ${IN_CONF}

param_name=$1
object_index=$2
interface_name=$3
vendor_object_index=$4
device_info_index=$5
[ -z "$vendor_object_index" ] && vendor_object_index=$object_index
[ -z "$device_info_index" ] && device_info_index=$object_index

value=""

case "$param_name" in
	"WPSState")
		eval enabled_value=\${Enable_${object_index}}
		eval config_state_value=\${ConfigState_${vendor_object_index}}
		Enable=$(printf "%b" "$enabled_value")
		ConfigState=$(printf "%b" "$config_state_value")
		if [ -z "$Enable" ] && [ -n "$ConfigState" ]; then
			Enable=`grep wps_state= $CONFIGS_PATH/hostapd_${interface_name}.conf`
			Enable=${Enable##*=}
			if [ $Enable -gt 0 ]; then
				Enable="true"
			else
				Enable="false"
			fi
		elif [ -n "$Enable" ] && [ -z "$ConfigState" ]; then
			ConfigState=`grep wps_state= $CONFIGS_PATH/hostapd_${interface_name}.conf`
			if [ "$ConfigState" = "2" ]; then
				ConfigState="Configured"
			else
				ConfigState="Unconfigured"
			fi
		fi
		if [ "$Enable" = "false" ]; then
			value=0
		elif [ "$ConfigState" = "Unconfigured" ]; then
			value=1
		elif [ "$ConfigState" = "Configured" ]; then
			value=2
		fi
		;;
	"WPSAction")
		eval wps_action_value=\${WPSAction_${vendor_object_index}}
		value=$(printf "%b" "$wps_action_value")
		;;
	"PIN")
		eval pin_value=\${PIN_${vendor_object_index}}
		value=$(printf "%b" "$pin_value")
		;;
	"WPS2ConfigMethodsEnabled")
		eval config_method_value=\${AWPS2ConfigMethodsEnabled_${vendor_object_index}}
		conf_methods=$(printf "%b" "$config_method_value")
		# Replace commas with spaces
		value="${conf_methods//,/ }"
		;;
	"UUID")
		eval uuid_value=\${UUID_${vendor_object_index}}
		value=$(printf "%b" "$uuid_value")
		;;
	"DeviceName")
		eval device_name_value=\${DeviceName_${vendor_object_index}}
		value=$(printf "%b" "$device_name_value")
		;;
	"Manufacturer")
		eval manufacturer_value=\${Manufacturer_${device_info_index}}
		value=$(printf "%b" "$manufacturer_value")
		;;
	"ModelName")
		eval model_name_value=\${ModelName_${device_info_index}}
		value=$(printf "%b" "$model_name_value")
		;;
	"ModelNumber")
		eval model_num_value=\${ModelNumber_${device_info_index}}
		value=$(printf "%b" "$model_num_value")
		;;
	"SerialNumber")
		eval serial_num_value=\${SerialNumber_${device_info_index}}
		value=$(printf "%b" "$serial_num_value")
		;;
	"FriendlyName")
		eval friendly_name_value=\${FriendlyName_${vendor_object_index}}
		value=$(printf "%b" "$friendly_name_value")
		;;
	"ManufacturerUrl")
		eval manufacturer_url_value=\${ManufacturerUrl_${vendor_object_index}}
		value=$(printf "%b" "$manufacturer_url_value")
		;;
	"EndpointPIN")
		eval endpoint_pin_value=\${EndpointPIN_${vendor_object_index}}
		value=$(printf "%b" "$endpoint_pin_value")
		;;
	"AuthorizedMac")
		eval authorized_mac_value=\${AuthorizedMac_${vendor_object_index}}
		value=$(printf "%b" "$authorized_mac_value")
		;;
esac

echo "$value"
