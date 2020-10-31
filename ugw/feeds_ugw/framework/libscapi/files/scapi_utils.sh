#!/bin/sh 

if [ ! "$CONFIGLOADED" ]; then
        if [ -r /etc/rc.d/config.sh ]; then
                . /etc/rc.d/config.sh 2>/dev/null
                CONFIGLOADED="1"
        fi
fi


file="/opt/lantiq/etc/.certchk"

#create certificate for lighthttpd on first boot.
if [ ! -s $file ] ; then
	key=`scapiutil get_key`
fi


#check devices comming up with default mac or not.
old_mac=`scapiutil get_mac`
[ -n "$CONFIG_UBOOT_CONFIG_ETHERNET_ADDRESS" -a -n "$old_mac" ] && \
  [ "$old_mac" = "$CONFIG_UBOOT_CONFIG_ETHERNET_ADDRESS" ] && {
	local i=0;
	while [ $i -lt 5 ]; do
		echo -en "\033[J"; usleep 150000;
		echo -en "#######################################################\n";
		echo -en "#     DEVICE CONFIGURED WITH DEFAULT MAC ADDRESS!!    #\n";
		echo -en "# This may conflict with other devices. Please change #\n";
		echo -en "#     the MAC address for un-interrupted services.    #\n";
		echo -en "#######################################################\n";
		echo -en "\033[5A\033G"; usleep 300000;
		let i++
	done; echo -en "\n\n\n\n\n";
} || true
