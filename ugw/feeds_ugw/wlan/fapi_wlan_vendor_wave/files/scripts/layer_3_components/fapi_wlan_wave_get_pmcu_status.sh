#!/bin/sh

interface_name=$1

if [ -z "$interface_name" ]; then
	echo "ERROR: please provide wlan interface, exit"
	exit 1
fi

# Get PMCU status:
kernel_pmcu=`cat  /sys/devices/system/cpu/cpu0/cpufreq/ltq_stats/ltq_control | awk '{print $3}'`

driver_pmcu=`iwpriv $interface_name gPCoCPower | awk '{print $3}'`

echo "PMCU status: kernel = $kernel_pmcu, driver ($wlan) = $driver_pmcu"
