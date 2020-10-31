#
# Openwrt Makefile for icc
#

#### Includes ###################################
include $(TOPDIR)/rules.mk

#### Package Info ###############################
PKG_NAME:=icc_user
PKG_VERSION:=1.0.0
PKG_BASE_NAME:=icc_user
PKG_START_SCRIPT:=load_icc.sh

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  TITLE:=icc user node creation
  DEPENDS:=@(TARGET_lantiq_xrx500||TARGET_lantiq_xrx500_4kec)
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Technologies AG
endef

define Package/$(PKG_NAME)/description
	icc creation
endef

#### Target Rules ###############################
define Build/Compile
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/init.d
	# Startup script
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
