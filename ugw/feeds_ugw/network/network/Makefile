#
# Openwrt Makefile for libnetwork
#
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=libnetwork
PKG_VERSION:=1.13.0.1
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src

#PKG_CONFIG_DEPENDS:=\
#	CONFIG_FULL_PACKAGE

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq Network Manager
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:= +libugwhelper +libcal +csd +servd +system_fapi +ltq-config_common_features:ltq-config_common_features +uci
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Lantiq SL function for Network Management like LAN,WAN,etc.
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
	menu "$(PKG_NAME) features"
	depends on PACKAGE_$(PKG_NAME)
        source "$(SOURCE)/config/Config.in"
        endmenu
endef

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

# To Staging Dir
define Build/InstallDev
	$(INSTALL_DIR) $(1)/xml/
	$(INSTALL_DATA) ./files/db_xml/*_control.xml $(1)/xml/; \
	$(INSTALL_DATA) ./files/db_xml/*_data.xml $(1)/xml/;
endef

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_DIR) $(1)/etc/hotplug.d/iface
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/etc/init.d
	$(INSTALL_DIR) $(1)/opt/lantiq/www
	$(INSTALL_DIR) $(1)/opt/lantiq/servd/lib
	$(INSTALL_DIR) $(1)/opt/lantiq/xml
	$(INSTALL_DIR) $(1)/opt/lantiq/cli
	$(INSTALL_DIR) $(1)/opt/lantiq/cli/xmls
	$(INSTALL_DIR) $(1)/opt/lantiq/cli/map-files
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libnetwork.so $(1)/opt/lantiq/servd/lib/
	cd $(1)/usr/lib && ln -sf ../../opt/lantiq/servd/lib/libnetwork.so
	#$(INSTALL_BIN) $(PKG_BUILD_DIR)/sl_test $(1)/usr/sbin
	$(INSTALL_DATA) ./files/db_xml/*_control.xml $(1)/opt/lantiq/xml/; \
	$(INSTALL_DATA) ./files/db_xml/*_data.xml $(1)/opt/lantiq/xml/; \
	$(if $(CONFIG_FEATURE_MULTI_BRIDGE), \
		$(INSTALL_DATA) ./files/view_xml/vlanform.xml $(1)/opt/lantiq/www/; \
		$(INSTALL_DATA) ./files/view_xml/vlantable.xml $(1)/opt/lantiq/www/; \
		$(INSTALL_DATA) ./files/view_xml/bridgeform.xml $(1)/opt/lantiq/www/; \
		$(INSTALL_DATA) ./files/view_xml/bridgetable.xml $(1)/opt/lantiq/www/; \
		$(INSTALL_DATA) ./files/view_xml/multibridge_menu.json $(1)/opt/lantiq/www/; \
	)
	$(INSTALL_BIN) ./files/02-servd-notify $(1)/etc/hotplug.d/iface/
	$(INSTALL_BIN) ./files/host_update $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/*.sh $(1)/$(VENDOR_PATH)/etc/
	$(INSTALL_DATA) ./files/cli-xml/*.xml $(1)/opt/lantiq/cli/xmls/
	$(INSTALL_DATA) ./files/cli-map/*.map $(1)/opt/lantiq/cli/map-files/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
