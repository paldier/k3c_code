#
# Openwrt Makefile for csd
#
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=csd
PKG_VERSION:=1.4.0.2
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
  TITLE:=Lantiq Config Storage Database Manager
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:= +libezxml +libubox +libjson  +libubus +libugwhelper +libblobmsg-json +libscapi +openssl-util 
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Lantiq Config Storage Database Manager
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

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_DIR) $(1)/usr/include
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/capi/libcapi.so $(1)/usr/lib/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/capi/include/*.h $(1)/usr/include/
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/config
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/etc/csd
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/usr/sbin
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/usr/lib
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/etc/csd/
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/capi/libcapi.so $(1)/$(VENDOR_PATH)/usr/lib/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/csd $(1)/$(VENDOR_PATH)/usr/sbin
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/capi/test_api/capitest $(1)/$(VENDOR_PATH)/usr/sbin
	$(INSTALL_BIN) ./files/csd $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/csdutil $(1)/$(VENDOR_PATH)/usr/sbin/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
