#
# Openwrt Makefile for liblanservices
#
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=liblanservices
PKG_VERSION:=1.2.6.1
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src

#PKG_CONFIG_DEPENDS:=\
#	CONFIG_FEATURE_LANSERVICES_NTP

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq LAN Services SL
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:= +libugwhelper +libcal +csd +servd +scapi +dnsmasq +system_fapi
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Lantiq SL for LAN Services Support like DHCPv4,DNS,NTP,RIPD etc.
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
endef

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

# To Staging Dir
define Build/InstallDev
	$(INSTALL_DIR) $(1)/xml
	$(INSTALL_DATA) ./files/db_xml/lan_services_*.xml $(1)/xml/
endef

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/etc/rc.d
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/etc/rc.d
	#$(INSTALL_DIR) $(1)/www
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/servd/lib
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/xml
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/cli
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/cli/xmls
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/cli/map-files
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/liblanservices.so $(1)/$(VENDOR_PATH)/servd/lib/
	cd $(1)/usr/lib && ln -sf ../../$(VENDOR_PATH)/servd/lib/liblanservices.so
	#$(INSTALL_BIN) $(PKG_BUILD_DIR)/sl_lanserv_test $(1)/usr/sbin
	$(INSTALL_DATA) ./files/db_xml/lan_services_*.xml $(1)/$(VENDOR_PATH)/xml/
	$(INSTALL_BIN) ./files/etc/*.sh $(1)/$(VENDOR_PATH)/etc/rc.d/
	#cd $(1)/etc/ && ln -sf ../$(VENDOR_PATH)/etc/resolv.conf
	cd $(1)/etc/ && ln -sf ../$(VENDOR_PATH)/etc/TZ
	#$(INSTALL_DATA) ./files/view_xml/*.xml $(1)/www/
	$(INSTALL_DATA) ./files/cli-xml/*.xml $(1)/$(VENDOR_PATH)/cli/xmls/
	$(INSTALL_DATA) ./files/cli-map/*.map $(1)/$(VENDOR_PATH)/cli/map-files/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
