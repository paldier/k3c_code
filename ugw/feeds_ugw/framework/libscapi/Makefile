#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=libscapi
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
  SECTION:=lib
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq Common Adaption Layer
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:= +libhelper
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Lantiq System Config APIs
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
#export $(PKG_CONFIG_DEPENDS)

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,./files/interface_config.sh $(TOPDIR)/.config $(PKG_BUILD_DIR)/interfaces.cfg)
endef

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/include
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/*.so $(1)/usr/lib
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/*.h $(1)/usr/include
	$(INSTALL_DIR) $(1)/usr/docs
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/scapi_proto.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_error.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ulogging.h $(1)/usr/docs/
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/etc/preinit.d
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/mem $(1)/usr/sbin/ 
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/utils/scapiutil $(1)/usr/sbin/ 
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/etc/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/interfaces.cfg $(1)/$(VENDOR_PATH)/etc/
	$(INSTALL_BIN) ./files/scapi_utils.sh $(1)/etc/preinit.d/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/*.so $(1)/usr/lib
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
