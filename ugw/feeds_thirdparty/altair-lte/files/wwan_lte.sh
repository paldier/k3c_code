#!/bin/sh /etc/rc.common

if [ ! "$ENVLOADED" ]; then
	if [ -r /etc/rc.conf ]; then
		. /etc/rc.conf 2> /dev/null
		ENVLOADED="1"
	fi
fi

query_modem ()
{
	echo "$1" | socat -t 1 /dev/ttyLTE0,raw,nonblock,cr,echo=0 stdio
}

stop ()
{
	killall udhcpc
	echo -n;
}

start ()
{
	if [ -f /tmp/cwan_status.txt ]; then
		cwi=0;
		c_flg=0;
		while [ $cwi -lt $cell_wan_Count ]; do
			eval [ '$'cwan_$cwi'_ena' = "1" ] && {
				c_flg=1;
				break
			}
			cwi=$((cwi+1))
		done

		if [ "$c_flg" = "1" ]; then
			eval c_ENA='$'cwan_$cwi'_ena'
			eval c_NAM='$'cwan_$cwi'_profName'
			eval c_APN='$'cwan_$cwi'_apn'
			eval c_USR='$'cwan_$cwi'_user'
			eval c_PAS='$'cwan_$cwi'_passwd'
			eval c_AUT='$'cwan_$cwi'_authType'
			eval c_PIN='$'cwan_$cwi'_usePIN'
			eval c_COD='$'cwan_$cwi'_PIN'
			eval c_DIA='$'cwan_$cwi'_dialNum'
			eval c_IDL='$'cwan_$cwi'_idleDisc'
			eval c_IDN='$'cwan_$cwi'_idleDiscTO'
			
			if [ -n "$c_IDL" -a "$c_IDL" = 1 ]; then
				local demand_dial=1
			fi
			if [ -n "$c_USR" ]; then
				local set_auth=1
			fi

			stop;
			
			# Configure PINCODE if used
			([ -n "$c_PIN" ] && [ "$c_PIN" = "1" ]) && {
				# send pin
				query_modem "AT+CPIN=$c_COD"
				# connect
				query_modem "at+cfun=1,0"
			}

			ppacmd addwan -i lte0
			ppacmd control --enable-lan --enable-wan
			ifconfig lte0 up
			udhcpc -i lte0 -s /etc/rc.d/udhcpc_lan.script -b
			route add -net 0.0.0.0 gw 10.10.10.10

			iptables -F
			iptables -X
			iptables -Z
			iptables -t nat -F
			iptables -t nat -X
			iptables -t nat -Z
			iptables -I INPUT -j ACCEPT
			iptables -I OUTPUT -j ACCEPT
			iptables -I FORWARD -j ACCEPT

			echo 1 > /proc/sys/net/ipv4/ip_forward

			iptables -t nat -o lte0 -j MASQUERADE -A POSTROUTING
			iptables -t nat -A PREROUTING -p udp --dport 4000:9000 -i lte0 -j DNAT --to 192.168.1.2
			iptables -t nat -A PREROUTING -p tcp --dport 4000:9000 -i lte0 -j DNAT --to 192.168.1.2

		fi
	fi
}

