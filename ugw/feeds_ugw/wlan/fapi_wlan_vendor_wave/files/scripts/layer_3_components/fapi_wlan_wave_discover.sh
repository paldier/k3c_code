#!/bin/sh

# This script counts PCI vendors, for enabling the feature of dynamically selecting dual PCI,
# or single PCI plus internal WLAN
# TODO: This script should be in the common layer - as the vendor PCI codes should not be known by Wave 
# TODO 2: Enable counting Wave500 PCI when merging to 5.1.0 branch. Currently PCI count is hardcoded 0 for wave devices

ATH_CODE="168c"
LTQ_CODE="1a30"
LTQ_CODE_WAVE500="1bef:0800"
MDTK_CODE_RT5592="1814"
MDTK_CODE_MT7603="14c3"


PCI_FILENAME="/tmp/lspci.txt"

ATH_HWNAME="9980"
ATH_VENDOR="QCA"
ATH_PREFIX="ath"

LTQ_HWNAME="wave"
LTQ_VENDOR="LANTIQ"
LTQ_PREFIX="wave"

MDTK_HWNAME_RT5592="5592"
MDTK_HWNAME_MT7603="7603"
MDTK_VENDOR="Ralink"
MDTK_PREFIX="ra"

QTN_HWNAME="QSR1000"
QTN_VENDOR="Quantenna"
QTN_PREFIX="qtn"

PCI_DEVICES_COUNT=0
AHB_WLAN_COUNT=0

lspci  > $PCI_FILENAME
PCI_DEVICES_COUNT=`cat $PCI_FILENAME | wc -l`
PCI_ATH_COUNT=`grep -c $ATH_CODE $PCI_FILENAME`
#PCI_LTQ_COUNT_WAVE300=`grep -c $LTQ_CODE $PCI_FILENAME`
#PCI_LTQ_COUNT_WAVE500=`grep -c $LTQ_CODE_WAVE500 $PCI_FILENAME`
#PCI_LTQ_COUNT=`expr $PCI_LTQ_COUNT_WAVE300 + $PCI_LTQ_COUNT_WAVE500`
PCI_LTQ_COUNT=0
PCI_MDTK_COUNT_RT5592=`grep -c $MDTK_CODE_RT5592 $PCI_FILENAME`
PCI_MDTK_COUNT_MT7603=`grep -c $MDTK_CODE_MT7603 $PCI_FILENAME`
PCI_MDTK_COUNT=`expr $PCI_MDTK_COUNT_RT5592 + $PCI_MDTK_COUNT_MT7603`
PCI_WLAN_COUNT=`expr $PCI_ATH_COUNT + $PCI_LTQ_COUNT + $PCI_MDTK_COUNT`
echo "fapi_wlan_wave_discover.sh: ALL  PCI DEVICES COUNT: $PCI_DEVICES_COUNT "
echo "fapi_wlan_wave_discover.sh: WLAN PCI DEVICES COUNT: $PCI_WLAN_COUNT "
#echo "fapi_wlan_wave_discover.sh: PCI_ATH COUNT: $PCI_ATH_COUNT "
#echo "fapi_wlan_wave_discover.sh: PCI_LTQ COUNT: $PCI_LTQ_COUNT "
#echo "fapi_wlan_wave_discover.sh: PCI_MDTK COUNT: $PCI_MDTK_COUNT "

if [ -e /sys/bus/platform/devices ]
then
	# Internal WLAN is identified as mtlk on AHB bus. This is "mtlk" before UGW-6.1, and "mtlk.0" in UGW-6.1
   AHB_WLAN_COUNT=`ls /sys/bus/platform/devices | grep mtlk -c`

	# If AHB wlan is supported on the bus, make sure it is supported in the driver too
	if [ $AHB_WLAN_COUNT -gt 0 ]
	then
		# Wave driver location differs between 6.1.1 and 6.5
		# Wave driver location in 6.5:
		WAVE_DRIVER_PATH=/opt/lantiq/lib/modules/`uname -r`/net/mtlk.ko
		# Wave driver location in 6.1.1:
		[ -e /lib/modules/3.10.12/net/mtlk.ko ] && WAVE_DRIVER_PATH=/lib/modules/`uname -r`/net/mtlk.ko
		AHB_WLAN_COUNT=`strings $WAVE_DRIVER_PATH | grep -c Ahb`
		if [ $AHB_WLAN_COUNT -gt 0 ]; then AHB_WLAN_COUNT=1; fi
	fi

	# If more than 1 PCI card detected, don't use internal WLAN
	if [ $PCI_WLAN_COUNT -gt 1 ] 
	then 
		AHB_WLAN_COUNT=0
	fi

	echo "fapi_wlan_wave_discover.sh: AHB_WLAN_COUNT: $AHB_WLAN_COUNT "
fi

# TODO: TOTAL_WLAN_COUNT should also take into account Quantenna RGMII
# otherwise script may terminate prematurely if Quantenna exists and internal WLAN isn't used.
# (probably not, because PCI WLAN count will be 0 in this configuration, but for correctness this should be fixed)
TOTAL_WLAN_COUNT=`expr $PCI_WLAN_COUNT + $AHB_WLAN_COUNT`
echo "fapi_wlan_wave_discover.sh: TOTAL WLAN COUNT: $TOTAL_WLAN_COUNT "

# Save information to a file to be used by Wave init script
#echo "PCI_WLAN_COUNT=$PCI_WLAN_COUNT" > /tmp/fapi_wlan_wave_discover.txt
#echo "PCI_LTQ_COUNT=$PCI_LTQ_COUNT" >> /tmp/fapi_wlan_wave_discover.txt
#echo "AHB_WLAN_COUNT=$AHB_WLAN_COUNT" >> /tmp/fapi_wlan_wave_discover.txt


