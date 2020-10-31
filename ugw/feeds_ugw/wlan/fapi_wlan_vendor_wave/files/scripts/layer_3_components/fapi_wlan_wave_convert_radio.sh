#!/bin/sh

# Source fapi_wlan_wave_in.conf
. ${IN_CONF}

param_name=$1
object_index=$2
interface_name=$3
vendor_object_index=$4
[ -z "$vendor_object_index" ] && vendor_object_index=$object_index

value=""

case "$param_name" in
	"WLSEnabled")
		eval enabled_value=\${Enable_${object_index}}
		Enable=$(printf "%b" "$enabled_value")
		if [ "$Enable" = "true" ]; then
			value="1"
		elif [ "$Enable" = "false" ]; then
			value="0"
		fi
		;;
	"AutoCocEnabled")
		eval auto_coc_value=\${AutoCocEnabled_${vendor_object_index}}
		Enable=$(printf "%b" "$auto_coc_value")
		if [ "$Enable" = "true" ]; then
			value="1"
		elif [ "$Enable" = "false" ]; then
			value="0"
		fi
		;;
	"NumOfAntennas")
		eval num_of_antennas_value=\${NumOfAntennas_${vendor_object_index}}
		value=$(printf "%b" "$num_of_antennas_value")
		;;
	"CoCPower")
		# Current CoC values are saved in a separate conf file
		# Parameters in the conf file are:
		# AutoCocEnabled
		# NumOfAntennas
		
		. ${COC_CONF}_${interface_name}
		eval coc_power_value=\${AutoCocEnabled_${vendor_object_index}}
		eval num_of_antennas_value=\${NumOfAntennas_${vendor_object_index}}
		eval enabled_value=\${Enable_${object_index}}
	
		AutoCocEnabled_db=$(printf "%b" "$coc_power_value")
		if [ "$AutoCocEnabled_db" = "true" ]; then
			AutoCocEnabled_db=1
		elif [ "$AutoCocEnabled_db" = "false" ]; then
			AutoCocEnabled_db=0
		fi
		NumOfAntennas_db=$(printf "%b" "$num_of_antennas_value")
		Enable=$(printf "%b" "$enabled_value")
		
		# If a parameter is missing (not modified), use value from conf file
		# If both parameters are missing, return empty value.
		if [ -z "$AutoCocEnabled_db" ]; then
			AutoCocEnabled_db=$AutoCocEnabled
		elif [ -z "$NumOfAntennas_db" ]; then
			NumOfAntennas_db=$NumOfAntennas
		fi
		# If AutoCoC is enabled, don't set number of antennas
		[ "$AutoCocEnabled_db" = "1" ] && NumOfAntennas_db=""
		# If the radio is disabled, set number of antennas to 0
		[ "$Enable" = "false" ] && AutoCocEnabled_db=0 && NumOfAntennas_db=0
		value="$AutoCocEnabled_db $NumOfAntennas_db $NumOfAntennas_db"
		;;
	"Country")
		eval country_value=\${RegulatoryDomain_${object_index}}
		value=$(printf "%b" "$country_value")
		;;
	"Channel")
		# Current Channel value is saved in a separate conf file
		# Parameter in the conf file is:
		# Channel
		
		[ -e "${CHANNEL_CONF}_${interface_name}" ] && . ${CHANNEL_CONF}_${interface_name}
		eval auto_channel_enable_value=\${AutoChannelEnable_${object_index}}
		eval channel_value=\${Channel_${object_index}}
		AutoChannelEnable=$(printf "%b" "$auto_channel_enable_value")
		Channel=$(printf "%b" "$channel_value")
		
		# If channel is empty, use current channel value.
		[ -z "$Channel" ] && Channel=$Channel_db
		# If AutoChannelEnable is enabled, return 0.
		# If AutoChannelEnable is disabled, return channel value.
		# If AutoChannelEnable is empty and channel is not empty, return channel value.
		if [ "$AutoChannelEnable" = "true" ]; then
			value="0"
		elif [ "$AutoChannelEnable" = "false" ]; then
			value="$Channel"
		elif [ -z "$AutoChannelEnable" ] && [ -n "$Channel" ]; then
			value="$Channel"
		fi
		;;
	"Channel_db")
		eval channel_value=\${Channel_${object_index}}
		value=$(printf "%b" "$channel_value")
		;;
	"ChannelBonding")
		eval ch_bandwidth_value=\${OperatingChannelBandwidth_${object_index}}
		OperatingChannelBandwidth=$(printf "%b" "$ch_bandwidth_value")
		if [ "$OperatingChannelBandwidth" = "20MHz" ]; then
			value=0
		elif [ "$OperatingChannelBandwidth" = "40MHz" ]; then
			value=1
		elif [ "$OperatingChannelBandwidth" = "Auto" ]; then
			value=1
		fi
		;;
	"UpperLowerChannelBonding")
		eval secondary_value=\${ExtensionChannel_${object_index}}
		ExtensionChannel=$(printf "%b" "$secondary_value")
		if [ "$ExtensionChannel" = "AboveControlChannel" ]; then
			value=0
		elif [ "$ExtensionChannel" = "BelowControlChannel" ]; then
			value=1
		fi
		;;
	"DTIMPeriod")
		eval dtim_value=\${DTIMPeriod_${object_index}}
		value=$(printf "%b" "$dtim_value")
		;;
	"BeaconPeriod")
		eval beacon_value=\${BeaconPeriod_${object_index}}
		value=$(printf "%b" "$beacon_value")
		;;
	"PowerSelection")
		eval power_value=\${TransmitPower_${object_index}}
		TransmitPower=$(printf "%b" "$power_value")
		case "$TransmitPower" in
		"12")
			value=-9
			;;
		"25")
			value=-6
			;;
		"50")
			value=-3
			;;
		"100")
			value=0
			;;
		esac
		;;
	"RTSThreshold")
		eval rts_value=\${RTSThreshold_${object_index}}
		value=$(printf "%b" "$rts_value")
		;;
	"RFMgmtEnable")
		value="4"
		# TODO (11ac)
		;;
	"ShortCyclicPrefixTx")
		eval gi_value=\${GuardInterval_${object_index}}
		GuardInterval=$(printf "%b" "$gi_value")
		if [ "$GuardInterval" = "400nsec" ]; then
			value=1
		elif [ "$GuardInterval" = "800nsec" ]; then
			value=0
		fi
		;;
	"pCoCEna")
		eval pcoc_value=\${WavePowerCocEnabled_${vendor_object_index}}
		WavePowerCocEnabled=$(printf "%b" "$pcoc_value")
		if [ "$WavePowerCoc" = "true" ]; then
			value=1
		elif [ "$WavePowerCoc" = "false" ]; then
			value=0
		fi
		;;
	"n2040CoexEna")
		eval coex_value=\${OperatingChannelBandwidth_${object_index}}
		OperatingChannelBandwidth=$(printf "%b" "$coex_value")
		if [ "$OperatingChannelBandwidth" = "Auto" ]; then
			value=1
		elif [ -n "$OperatingChannelBandwidth" ]; then
			value=0
		fi
		;;
	"n2040CoexThreshold")
		eval coex_threshold_value=\${CoexChannelSwitchThreshold_${vendor_object_index}}
		value=$(printf "%b" "$coex_threshold_value")
		;;
	"nObssScanInt")
		eval coex_scan_value=\${CoexScanInterval_${vendor_object_index}}
		value=$(printf "%b" "$coex_scan_value")
		;;
	"hRadarEna")
		eval radar_detect_value=\${IEEE80211hEnabled_${object_index}}
		IEEE80211hEnabled=$(printf "%b" "$radar_detect_value")
		if [ "$IEEE80211hEnabled" = "true" ]; then
			value=1
		elif [ "$IEEE80211hEnabled" = "false" ]; then
			value=0
		fi
		;;
	"CoexActTrshold")
		eval coex_act_threshold_value=\${CoexChannelSwitchThreshold_${vendor_object_index}}
		value=$(printf "%b" "$coex_act_threshold_value")
		;;
	"Debug_SoftwareWatchdogEnable")
		eval complete_recover_value=\${WaveCompleteRecoveryEnabled_${vendor_object_index}}
		WaveCompleteRecoveryEnabled=$(printf "%b" "$complete_recover_value")
		if [ "$WaveCompleteRecoveryEnabled" = "true" ]; then
			value=1
		elif [ "$WaveCompleteRecoveryEnabled" = "false" ]; then
			value=0
		fi
		;;
	"FrequencyBand")
		eval frequecy_value=\${OperatingFrequencyBand_${object_index}}
		OperatingFrequencyBand=$(printf "%b" "$frequecy_value")
		if [ "$OperatingFrequencyBand" = "2.4GHz" ]; then
			value=1
		elif [ "$OperatingFrequencyBand" = "5GHz" ]; then
			value=0
		elif [ "$OperatingFrequencyBand" = "Both" ]; then
			value=2
		fi
		;;
	"UpDebugLevel")
		eval driver_debug_value=\${WaveDriverDebugLevelConsole_${vendor_object_index}}
		value=$(printf "%b" "$driver_debug_value")
		;;
	"RTLoggerEnabled")
		eval rtlogger_value=\${WaveRTLoggerEnabled_${vendor_object_index}}
		WaveRTLoggerEnabled=$(printf "%b" "$rtlogger_value")
		if [ "$WaveRTLoggerEnabled" = "true" ]; then
			value=1
		elif [ "$WaveRTLoggerEnabled" = "false" ]; then
			value=0
		fi
		;;
	"FwRecovery")
		# FW recovery parameters are saved in a separate conf file
		# Parameters in the conf file are:
		# fast_recovery_enabled (0/1)
		# fast_recovery_limit
		# full_recovery_enabled (0/1)
		# full_recovery_limit
		# complete_recovery_enabled (0/1)
		# recovery_timer
		# recovery_num_of_dumps
		
		. ${RECOVERY_CONF}_${interface_name}
		# Space only at the end - this avoid space at value read (empty value gives space)
		if [ "$fast_recovery_enabled" = "0" ]; then
			value="${value}${fast_recovery_enabled} "
		else
			value="${value}${fast_recovery_limit} "
		fi
		
		if [ "$full_recovery_enabled" = "0" ]; then
			value="${value}${full_recovery_enabled} "
		else
			value="${value}${full_recovery_limit} "
		fi
		
		value="${value}${complete_recovery_enabled} ${recovery_timer} ${recovery_num_of_dumps}"
		;;
	"FwLoggerSeverity")
		eval fw_logger_severity_value=\${WaveFwLoggerSeverity_${vendor_object_index}}
		value=$(printf "%b" "$fw_logger_severity_value")
		;;
	"FWDebugEnabled")
		eval fw_debug_value=\${WaveFWDebugEnabled_${vendor_object_index}}
		WaveFWDebugEnabled=$(printf "%b" "$fw_debug_value")
		if [ "$WaveFWDebugEnabled" = "true" ]; then
			value=1
		elif [ "$WaveFWDebugEnabled" = "false" ]; then
			value=0
		fi
		;;
	"ShortPreamble")
		eval preamble_value=\${PreambleType_${object_index}}
		PreambleType=$(printf "%b" "$preamble_value")
		case "$PreambleType" in
			"short")
				value=1
				;;
			"long")
				value=2
				;;
			"auto")
				value=3
				;;
		esac
		;;
	"LongRetryLimit")
		eval long_retry_value=\${LongRetryLimit_${object_index}}
		value=$(printf "%b" "$long_retry_value")
		;;
	"NetworkMode")
		eval standards_value=\${OperatingStandards_${object_index}}
		OperatingStandards=$(printf "%b" "$standards_value")
		# Remove commas
		OperatingStandards=${OperatingStandards//,/}
		case "$OperatingStandards" in
			"a")
				value=10
				;;
			"an"|"na")
				value=14
				;;
			"b")
				value=17
				;;
			"g")
				value=18
				;;
			"bg"|"gb")
				value=19
				;;
			"gn"|"ng")
				value=22
				;;
			"bgn"|"bng"|"gbn"|"gnb"|"nbg"|"ngb")
				value=23
				;;
			"n")
				value=20
				# When 11n is set, read frequency from the driver
				frequency=`iwconfig $interface_name`
				frequency=${frequency##*Frequency=}
				frequency=${frequency%%.*}
				[ "$frequency" = "5" ] && value=12				
				;;
		esac
		;;
	"fast_recovery_enabled")
		eval fast_recovery_enabled_value=\${WaveFastRecoveryEnabled_${object_index}}
		value=$(printf "%b" "$fast_recovery_enabled_value")
		;;
	"fast_recovery_limit")
		eval fast_recovery_limit_value=\${WaveFastRecoveryLimit_${object_index}}
		value=$(printf "%b" "$fast_recovery_limit_value")
		;;
	"full_recovery_enabled")
		eval full_recovery_enabled_value=\${WaveFullRecoveryEnabled_${object_index}}
		value=$(printf "%b" "$full_recovery_enabled_value")
		;;
	"full_recovery_limit")
		eval full_recovery_limit_value=\${WaveFullRecoveryLimit_${object_index}}
		value=$(printf "%b" "$full_recovery_limit_value")
		;;
	"complete_recovery_enabled")
		eval complete_recovery_enabled_value=\${WaveCompleteRecoveryEnabled_${object_index}}
		value=$(printf "%b" "$complete_recovery_enabled_value")
		;;
	"recovery_timer")
		eval recovery_timer_value=\${WaveRecoveryTimer_${object_index}}
		value=$(printf "%b" "$recovery_timer_value")
		;;
	"recovery_num_of_dumps")
		eval num_of_dumps_value=\${WaveRecoveryNumOfDumps_${object_index}}
		value=$(printf "%b" "$num_of_dumps_value")
		;;
esac

echo "$value"
