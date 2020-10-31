#
# Openwrt Makefile for rtdot1x
#

#### Includes ###################################
include $(TOPDIR)/rules.mk

#### Package Info ############################### 
PKG_NAME:=rtdot1xd
PKG_VERSION:=2.7.0.0
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)

include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
  SECTION:=net
  CATEGORY:=Network
  TITLE:=rtdot1xd userspace IEEE 802.1X Authenticator for RT_WIFI
  URL:=https://sourceforge.net/p/wive-ng/wive-ng-rtnl/ci/master/tree/user/802.1x/
  MAINTAINER:=Jouni Malinen <jkmaline@cc.hut.fi>
endef

define Package/$(PKG_NAME)/description
  RTDOT1XD - user space IEEE 802.1X Authenticator for RT_WIFI linux driver, Ralink Tech Corp.
endef

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/rtdot1xd $(1)/usr/sbin/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
