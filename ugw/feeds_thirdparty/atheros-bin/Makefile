#
# Copyright (C) 2007 OpenWrt.org
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=atheros-bin
PKG_VERSION:=7.1.1_ga_20160914T181412
PKG_RELEASE:=1
PKG_SOURCE:=$(PKG_NAME)-$(PKG_VERSION).tar.gz
PKG_SOURCE_URL:=http://localhost
PKG_MD5SUM:=fe5eca6f236f96b8960ae5ca9e699b52

UGW_MODEL_NAME:=$(shell echo $(CONFIG_BUILD_SUFFIX)|tr a-z A-Z)

include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
	SECTION:=utils
	CATEGORY:=Lantiq
	TITLE:= Atheros Binaries
	DEPENDS:= @!atheros_USE_CUSTOM_SOURCE_DIR +libpthread +librt
	URL:=http://www.lantiq.com
	MAINTAINER:=Lantiq Deutschland GMBH
	MENU:=1
endef

define Package/$(PKG_NAME)/description
 This package contains atheros binaries
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/rc.d
	$(INSTALL_DIR) $(1)/lib/modules/$(LINUX_VERSION)/net
	$(INSTALL_DIR) $(1)/sbin
	$(INSTALL_DIR) $(1)/bin
	$(INSTALL_DIR) $(1)/etc
	$(INSTALL_DIR) $(1)/usr
	cp -rf $(PKG_BUILD_DIR)/ugw_models/$(UGW_MODEL_NAME)/* $(1)/
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
