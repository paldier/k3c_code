#
# Openwrt Makefile for libmanagementserver
#
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=libmanagementserver
PKG_VERSION:=1.1.0.0
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src
#PKG_BUILD_DEPENDS:=PACKAGE_ltq-devm:ltq-devm

#PKG_CONFIG_DEPENDS:=\
#	CONFIG_FULL_PACKAGE

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq Management Server
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:= +libugwhelper +libcal +csd +servd
  #DEPENDS:= +libugwhelper +libcal +csd +servd +ltq-devm
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Lantiq SL function for Management Server.
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
	$(INSTALL_DATA) ./files/db_xml/id_added_xmls/ManagementServer_*.xml $(1)/xml/
endef

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_DIR) $(1)/etc
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/www
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/servd/lib
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/xml
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libmanagementserver.so $(1)/$(VENDOR_PATH)/servd/lib/
	cd $(1)/usr/lib && ln -sf ../../$(VENDOR_PATH)/servd/lib/libmanagementserver.so
	$(INSTALL_DATA) ./files/db_xml/id_added_xmls/ManagementServer_*.xml $(1)/$(VENDOR_PATH)/xml/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
