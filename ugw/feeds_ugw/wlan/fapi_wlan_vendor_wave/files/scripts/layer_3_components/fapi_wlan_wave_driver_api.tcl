#!/bin/tclsh

source /opt/lantiq/wave/scripts/fapi_wlan_wave_ini.tcl

set driverApiHandler {}
set platform_log_level $env(log_level)
set wep ""

proc print2log {dbg_level dbg_string} {
	global platform_log_level

	if {$dbg_level == "INFO"} {
		if {$platform_log_level >= 2} {
			exec echo $dbg_string > /dev/console
		}
	} elseif {$dbg_level == "DBG"} {
		if {$platform_log_level >= 3} {
			exec echo $dbg_string > /dev/console
		}
	} else {
		exec echo $dbg_level $dbg_string > /dev/console
	}
}

################################################################
# TCL functions to be used by the fapi_wlan_wave_driver api.ini
################################################################

proc test {condition ret_if_true ret_if_false} {
	if {[expr $condition]} { 
		return $ret_if_true
	} else {
		return $ret_if_false
	}
}

proc WEP_Set {interface param value} {
	if {$value == ""} {
		return
	}
	
	if {$param == "WepEncryption"} {
		return "iwconfig $interface key [test $value==0 off on]"
	}  elseif {$param == "WepTxKeyIdx"} {
		return "iwconfig $interface key \[[expr $value+1]\] && iwconfig $interface key off"
	}
	
	set idx [string index $param "18"]
	incr idx
	if {[string first ' $value] == 0} {
		return "iwconfig $interface key \[$idx\] s:\"`printf \"%b\" $value`\""
	} else {
		return "iwconfig $interface key \[$idx\] $value"
	}
}

proc ESSID_set {interface value} {
	if {$value == ""} {
		return
	}
	return "iwconfig $interface essid \"`printf \"%b\" $value`\""
}

proc PeerAPMacs_Set {interface value} {
	set ret ""
	set PeerAPmacsList ""

	# Read wds status from driver.
	# Using the first "cut" to read the value after the ":" and the second "cut" to remove trailing spaces.
	if {![catch {set wdsStatus [eval "exec iwpriv $interface gBridgeMode | cut -d \":\" -f2 | cut -d \" \" -f1"]}]} {
		# The list of AP peers can be read from driver only if WDS is enabled.
		if {$wdsStatus == 1} {
			if {![catch {set val [eval "exec iwpriv $interface gPeerAPs"]}]} {
				set PeerAPmacsList [split $val]
			}
		}
	}
	# Go over the list of MAC from rc.conf and configure only those missing in driver.
	set PeerAPhost_apiList [split "$value"]
	foreach mac $PeerAPhost_apiList {
		if { [ lsearch $PeerAPmacsList $mac ] == -1 } {
			append ret "iwpriv $interface sAddPeerAP $mac;"
		}
	}
	return "$ret"
}

proc set_AP_Authentication {interface value} {
	set encryption ""
	if {$::wep == 0} {
		set encryption "off"
	} 
	if {$value == 1} {
		return "iwconfig $interface key open $encryption"
	} elseif {$value == 2} {
		return "iwconfig $interface key restricted $encryption"
	} elseif {$value == 3} {
		return "iwconfig $interface key open restricted $encryption"
	}
}

proc VapSTALimits_Set {interface value} {
	set value [eval "exec echo $value"]
	return "iwpriv $interface sVapSTALimits $value"
}
proc ProcGet {interface param} {
	exec cat /proc/net/mtlk/$interface/$param
}

proc ProcSet {interface param value} {
	return "echo $value > /proc/net/mtlk/$interface/$param"
}	

proc ProcMtlkGet {param} {
	exec cat /proc/net/mtlk/$param
}

proc ProcMtlkSet {param value} {
	return "echo $value > /proc/net/mtlk/$param"
}

proc ProcLogSet {param value} {
	return "echo 0 cdebug=$value > /proc/net/mtlk_log/$param" 
}	

proc ProcLogGet {param} {
	exec cat /proc/net/mtlk_log/$param	
}

proc IWPRIV_Set {interface param value} {
	return "iwpriv $interface s$param $value"
}

proc IWPRIV_Get {interface param} {
	return "iwpriv $interface g$param"
}

proc iwconfig {interface param value} {
	if {$value == ""} {
		set value auto
	}
	return "iwconfig $interface $param $value"
}
proc iwpriv {interface param value} {
	return "iwpriv $interface $param $value"
}
proc iwgetid {interface param} {
	return "iwgetid $interface $param"
}

# execute the command, run regexp on the response and return first match
proc parse {cmd reg} {
	if {$cmd != ""} {
		if {![catch {set ret [eval "exec $cmd"]}]} {
			if {[regexp $reg $ret all match] > 0} {
				return $match
			}			
		}
	}
	return ""
}
###################################################
#  Driver API functions
#
#
###################################################

proc DriverGet {interface param} {
  set param [string trim $param]
  set retVal [getParamExecuteCmd GET $param]
  if {$retVal != ""} {
		set retVal [eval $retVal]
		
  }  
  return $retVal

}

proc DriverSet {interface param value} {
	set param [string trim $param]
	set value [string trim $value]
	if {$value == ""} {
		return
	}
	set retVal [getParamExecuteCmd SET $param]
	if {$retVal != ""} {				
		if {[catch {set ret [eval $retVal]} err]} {		
			return "# failed to eval $retVal, err: $err"
		}				
		return $ret
	}  
	return $retVal
}


proc setDriverApiHandler {db_file} {
	global driverApiHandler
	# load the ini file
	set driverApiHandler [IniOpen $db_file]	
}


proc getParamExecuteCmd {section param} {
	global driverApiHandler	
	# get the parameter key from the command section
	set retVal [IniGetKey $driverApiHandler $section $param]
	if {$retVal != ""} {
			set retVal1 [IniGetKey $driverApiHandler $retVal $section]
			if {$retVal1 != ""} {
				return $retVal1
			}	
	}
	return $retVal;	
}


proc DriverSetAll {interface param_file_name} {
	# Need to define wep is global parameter since set_AP_Authentication checks this parameter value
	global wep
	
	print2log "DBG" "fapi_wlan_wave_driver_api.tcl: interface = $interface"

	set conf_param [open "/tmp/wlan_wave/set_driver_params_${interface}.sh" w]
	#for run conf_param.conf as sh script
	puts $conf_param {#!/bin/sh}
		
	set wep 0
	set ChannelBonding ""
	set UpperLowerChannelBonding ""
	set network_type ""
	set Country ""
	set EEPROMCountryValid 0
	set Key0 ""
	set Key1 ""
	set Key2 ""
	set Key3 ""
	set KeyIndex ""
	set Authentication 1
	set dot11dActive ""
	set channel ""
	set BridgeMode ""
	set MacCloningAddr ""
	set PeerAPMacs ""
	set WDSEnabled ""
	set AocsRestrictCh ""
	set ProxyArp ""
	set vapStaLimits ""
	set FwLoggerSeverity ""
	set nCoexAutoChanIntferEna ""
	set contents [exec cat $param_file_name]
			
	#Update parameters according to DB. 
	set contents [split $contents "\n"]
	foreach line $contents {		
		if {[regexp {(.*)=(.*)} $line all param value] == 1} {
			if {$value == ""} {
				continue
			}
			if {$param == "nCoexAutoChanIntferEna"} {
				set nCoexAutoChanIntferEna $value
				continue
			}

			if {$param == "vapStaLimits"} {
				set vapStaLimits $value
				continue
			}

			if {$param == "ProxyArp"} {
				set ProxyArp $value
				continue
			}

			if {$param == "WepEncryption"} {
				set wep $value
				continue
			}
			
			if {$param == "network_type"} {
				set network_type $value
				continue
			}
			
			if {$param == "Country"} {
				set Country $value
				continue
			}
			
			if {$param == "EEPROMCountryValid"} {
				set EEPROMCountryValid $value
				continue
			}
			
			if {$param == "WepKeys_DefaultKey0" } {
				set Key0 $value
				continue
			}

			if {$param == "WepKeys_DefaultKey1" } {
				set Key1 $value
				continue
			}

			if {$param == "WepKeys_DefaultKey2" } {
				set Key2 $value
				continue
			}

			if {$param == "WepKeys_DefaultKey3" } {
				set Key3 $value
				continue
			}

			if {$param == "WepTxKeyIdx" } {
				set KeyIndex $value
				continue
			}

			if {$param == "Authentication" } {
				set Authentication $value
				continue
			}

			if {$param == "dot11dActive"} {
				set dot11dActive $value
				continue
			}

			if {($param == "AclMode") && ($value != 0)} {
				puts $conf_param "iwpriv $interface sDelACL 00:00:00:00:00:00"
			}

			if {$param == "Channel"} {
				set channel $value
				continue
			}

			#if {$param == "BridgeMode"} {
			#	set BridgeMode $value
			#	continue
			#}

			if {$param == "MacCloningAddr"} {
				set MacCloningAddr $value
				continue
			}
			
			if {$param == "PeerAPkeyIdx"} {
				set  PeerAPkeyIdx $value
				continue
			}
			
			if {$param == "PeerAPMacs"} {
				set PeerAPMacs $value
				continue
			}
			
			if {$param == "WDSEnabled"} {
				set WDSEnabled $value
			}
			
			if {$param == "AocsRestrictCh"} {
                        	set AocsRestrictCh $value
				continue
			}
			
			if {$param == "UpperLowerChannelBonding"} {
				set UpperLowerChannelBonding $value
				continue
			}
			
			if {$param == "ChannelBonding"} {
				set ChannelBonding $value
				continue
			}

			if {$param == "FwLoggerSeverity"} {
				set FwLoggerSeverity $value
				continue
			}
			
			set ret [DriverSet $interface $param $value]
			if {$ret != ""} {
				puts $conf_param $ret
				#eval "exec $ret"
			}
		}
	}
	
	if {$Country != ""} {
		set ret [DriverSet $interface "Country" $Country]
		if {$ret != ""} {
			puts $conf_param $ret
		}
	}
	
	if {$ChannelBonding != ""} {
		set ret [DriverSet $interface "ChannelBonding" $ChannelBonding]
		if {$ret != ""} {
		        puts $conf_param $ret
		}
	}
	
	if {$UpperLowerChannelBonding != ""} {
		set ret [DriverSet $interface "UpperLowerChannelBonding" $UpperLowerChannelBonding]
		if {$ret != ""} {
		        puts $conf_param $ret
		}
	}
	
	# Disable restricted channels only if manual channel is set.
	if {$AocsRestrictCh != ""} {
		if {$channel != 0} {
			set AocsRestrictCh 0
		}
		set ret [DriverSet $interface AocsRestrictCh $AocsRestrictCh]
		if {$ret != ""} {
			puts $conf_param $ret
		}
	}

	# Channel can only be set after network_mode and Country were set
	if {$channel != ""} {
		set ret [DriverSet $interface "Channel" $channel]
		if {$ret != ""} {
		        puts $conf_param $ret
		}
	}
	
	# this is an ugly workaround, to make sure that we set WepEncryption after we done with the keys
	# write the keys:
	# 	before enable wep
	#	only if wep is enabled (if not HT - the Web doesn't allow to set wep if HT mode selected)
	#	i f {($network_mode != "12") && ($network_mode != "14") && ($network_mode != "20") && ($network_mode != "22") && ($network_mode != "23")}
	if {$wep != 0 || $WDSEnabled == 1} {
		set errorKey 0
		set ret [DriverSet $interface "WepKeys_DefaultKey0" $Key0]
			if {$ret != ""} {
				puts $conf_param $ret
			} else {
				set errorKey 1
			}
		if {$errorKey == 0 && $wep != 0} {
			set ret [DriverSet $interface "WepTxKeyIdx" $KeyIndex]
			if {$ret != ""} {
				puts $conf_param $ret
			}
			set ret [DriverSet $interface "WepEncryption" $wep]
			if {$ret != ""} {
				puts $conf_param $ret
			}
		}
	}
	
	if {$Authentication != ""} {
		set ret [DriverSet $interface "Authentication" $Authentication]
		if {$ret != ""} {
			puts $conf_param $ret
		}
	}
	#if {$BridgeMode != ""} {
		# If MAC Cloning is configured and MAC address is set, handle it to the driver
	#	if {$BridgeMode == "3"} {
	#		if {$MacCloningAddr != ""} {
	#			set ret [DriverSet $interface "MAC" $MacCloningAddr]
	#			if {$ret != ""} {
	#				puts $conf_param $ret
	#			}
	#		}
	#	}	
		# If L2NAT is configured, handle Local MAC to the driver
	#	if {$BridgeMode == "2"} {
	#		set L2NAT_LocMAC [exec ifconfig br-lan | awk {NR<2 {print $5}}]
	#		set ret [DriverSet $interface "L2NAT_LocMAC" $L2NAT_LocMAC]
	#		if {$ret != ""} {
	#			puts $conf_param $ret
	#		}
	#	}	
	#}
	
	#catch {set value [exec host_api get $$ sys BridgeMode]}	
	#if {$value != ""} {
	#	set ret [DriverSet $interface BridgeMode $value]
	#	if {$ret != ""} {
	#		puts $conf_param $ret
			#eval "exec $ret"
	#	}
	#}
	
	#puts $conf_param "echo DriverSET Param is DONE"
	
	#set wds parameters
	if {$WDSEnabled == 1} {
		set ret [DriverSet $interface "PeerAPkeyIdx" $PeerAPkeyIdx]
		if {$ret != ""} {
			puts $conf_param $ret
		}
		set ret [DriverSet $interface "PeerAPMacs" $PeerAPMacs]
		if {$ret != ""} {
			puts $conf_param $ret
		}
	}

	#set ProxyArp only if HS2.0 is enabled or OSEN mode
	#TODO: see how to replace the next line
	#catch {set value [exec host_api get $$ $indx Hs2Mode]}	
	#if {$value != 0} {
	#	set ret [DriverSet $interface ProxyArp $ProxyArp]
	#	if {$ret != ""} {
	#		puts $conf_param $ret
	#	}
	#}


	if {$vapStaLimits != ""} {
		set ret [DriverSet $interface vapStaLimits $vapStaLimits]
		if {$ret != ""} {
			puts $conf_param $ret
		}
	}
	
	if {$FwLoggerSeverity != ""} {
		set FwLoggerSeverity [concat $FwLoggerSeverity 2]
		set ret [DriverSet $interface FwLoggerSeverity $FwLoggerSeverity]
		if {$ret != ""} {
			puts $conf_param $ret
		}
	}

	if {$nCoexAutoChanIntferEna != ""} {
		if {[regsub -all {(')} $nCoexAutoChanIntferEna "" nCoexAutoChanIntferEna]} {
			set ret [DriverSet $interface nCoexAutoChanIntferEna "$nCoexAutoChanIntferEna"]
			if {$ret != ""} {
				puts $conf_param $ret
			}
		}
	}

	#THIS MUST BE LAST IN FILE ! - to support case of FW Crash on last API
	puts $conf_param "iwconfig $interface > /dev/null"

	close $conf_param
	
	catch {exec chmod +x /tmp/wlan_wave/set_driver_params_${interface}.sh }
}


# Create a script to get all params from the driver.
# Used for creating a list of the default values of the driver 
# (Call after insmod and before setting any params, after restore defaults)
proc DriverGetAll {interface conf_file} {
	
	# Name of default file to create(TODO: Remove hardcoded path)
	set def_file [open "/mnt/jffs2/drv.def" w]
	
	if {[file readable $conf_file] ==  1} {
		set fp [open $conf_file r]
		set contents [read $fp]
		close $fp
	} else {
		puts "echo Can not read file $conf_file"
		return ''
	}
	
	#for run conf_param.conf as sh script
	
	#Update parameters according to DB. 
	set contents [split $contents "\n"]
	set sed_cmd {sed -r s/\[^:\]*://}
	foreach line $contents {
		if {[regexp {(.*) = (.*)} $line all param value] == 1} {
			set ret [DriverGet $interface $param]
			if {$ret != ""} {
				# Ugly hack: ret contains either a sh cmd (e.g. iwpriv) or the value from parse 
				# exec the cmd and sed to remove iwpriv header, or use the value directly
				if {![catch {set val [eval "exec $ret | $sed_cmd"]}]} {
					puts $def_file "$param = [string trim $val]"
				} else {
					puts $def_file "$param = $ret"
				}
			} 
		}
	}

	# Get BridgeMode: a driver parameter but we hold it in the sys.conf
	set ret [DriverGet $interface BridgeMode]
	if {![catch {set val [eval "exec $ret | $sed_cmd"]}]} {
		puts $def_file "BridgeMode = [string trim $val]"
	}
	
	close $def_file
}


#usage for command line interface
if {[info exists ::argv]} {
	set driverApiHandler [IniOpen /opt/lantiq/wave/scripts/fapi_wlan_wave_driver_api.ini]
	if {[lindex $argv 0] == "DriverSetAll"} {
		DriverSetAll [lindex $argv 1] [lindex $argv 2]
	}
	if {[lindex $argv 0] == "DriverGetAll"} {		
		DriverGetAll [lindex $argv 1] [lindex $argv 2]
	}
	if {[lindex $argv 0] == "DriverParamSet"} {
		set ret [DriverSet [lindex $argv 1] [lindex $argv 2] [lindex $argv 3]]
		set exe_list [split $ret ";"]		
		foreach cmd $exe_list {
			if {$cmd != ""} {
				eval "exec $cmd"
			}
		}
	}			
} else {
	set driverApiHandler {}
}
