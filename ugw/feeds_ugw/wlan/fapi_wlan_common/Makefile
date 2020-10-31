#
# Openwrt Makefile for fapi_wlan_common
#

#### Includes ###################################
include $(TOPDIR)/rules.mk

#### Package Info ############################### 
PKG_NAME:=fapi_wlan_common
PKG_VERSION:=1.0.0.1
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=net
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq WLAN Service Library
  URL:=http://www.lantiq.com
  MAINTAINER:=LANTIQ DEUTSCHLAND GMBH
  DEPENDS:= +libhelper
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Lantiq Common FAPI layer
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

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libfapiwlancommon.so $(1)/usr/lib/
	#$(INSTALL_BIN) $(PKG_BUILD_DIR)/test_wlan $(1)/usr/sbin
	$(INSTALL_DIR) $(STAGING_DIR)/usr/docs
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/include
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libfapiwlancommon.so $(1)/usr/lib/
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
