#
# Copyright (C) 2008 OpenWrt.org
# Copyright (C) 2014 lantiq.com 
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#

include $(TOPDIR)/rules.mk

PKG_NAME:=ltq-hwmon
PKG_VERSION:=1.0
PKG_RELEASE:=1
PKG_BIN_DIR=lib/modules/$(LINUX_VERSION)/

PKG_START_SCRIPT:=ltq_hwmon.sh

include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
  SECTION:=power
  CATEGORY:=Lantiq
  TITLE:=Power/Speed Monitor
  URL:=http://www.lantiq.com/
  MAINTAINER:=Lantiq Deutschland GmbH
  DEPENDS:=+kmod-hwmon-core \
           +kmod-i2c-core \
           +kmod-hwmon-ina2xx \
           +kmod-hwmon-xrx500-spdmon
  MENU:=1 
endef

define Package/$(PKG_NAME)/description
	XRX500 Speed Monitor.
	Power Monitoring with INA219.
	Additional I2C modules which needs to be enabled:
	SW-I2C: kmod-i2c-gpio, kmod-i2c-algo-bit
	HW-I2C: kmod-i2c-lantiq
endef

define Package/$(PKG_NAME)/config 
	menu "configuration" 
	depends on PACKAGE_$(PKG_NAME) 
	endmenu 
endef 

Build/Compile:=
 
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
