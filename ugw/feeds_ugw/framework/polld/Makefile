include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=polld
PKG_VERSION:=1.0.0.3
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
	SECTION:=utils
	CATEGORY:=Lantiq
  	SUBMENU:=UGW Software
	TITLE:=PollD
	URL:=http://www.lantiq.com
	MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
 	DEPENDS:= +libhelper +libugwhelper +libcal +libubus +libubox +libblobmsg-json +ltq-voip-common
	MENU:=1
endef

define Package/$(PKG_NAME)/description
	Provides polling framework to the system
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
	ln -fs `pwd`/src/* $(PKG_BUILD_DIR)/
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/bin/
	$(INSTALL_DIR) $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/polld $(1)/etc/init.d/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/polld $(1)/$(VENDOR_PATH)/bin/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
