include $(TOPDIR)/rules.mk

FILE:= $(TOPDIR)/ugw_version
UGW_VERSION:=$(shell cat ${FILE})
PKG_NAME:=utmproxy
PKG_RELEASE:=1
PKG_BUILD_DIR:=utmproxy
DEBUG=0

ifeq ($(UGW_VERSION), 6.1)
	ifeq ($(DEBUG), 1)
    		PKG_VERSION:=0.3.debug.UGW$(UGW_VERSION)
    		PKG_MDSUM:=7ca9e1969beb71635021f5e788f35855
    		PKG_SOURCE:=hitmanpro.utm-$(PKG_VERSION).tar.gz
    		PKG_SOURCE_URL:=http://localhost
	else ifeq ($(DEBUG), 0)
    		PKG_VERSION:=0.5.UGW$(UGW_VERSION)
    		PKG_MDSUM:=8d98a6e9ca2c8bc747498fa6f019ca16
    		PKG_SOURCE:=hitmanpro.utm-$(PKG_VERSION).tar.gz
    		PKG_SOURCE_URL:=http://localhost
	endif
else ifeq ($(UGW_VERSION), 5.4)
    PKG_VERSION:=0.1.UGW$(UGW_VERSION)
    PKG_MDSUM:=3f1de0e6bb0f3d576bd2b5a53b278099
    PKG_SOURCE:=hitmanpro.utm-$(PKG_VERSION).tar.gz
    PKG_SOURCE_URL:=http://localhost
endif

PKG_CAT:=zcat

include $(INCLUDE_DIR)/package.mk

define Package/utmproxy
  SECTION:=utils
  CATEGORY:=Utilities
  TITLE:=Lantiq Cloud Security powered by HitmanPro.UTM
  URL:=http://hitmanpro.com
  DEPENDS=+librt
endef

define Build/Compile
	:
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/bin/
	$(INSTALL_DIR) $(1)/opt/HitmanPro.UTM/
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_DIR) $(1)/mnt/data
	touch $(1)/mnt/data/activation.conf
	#touch $(1)/opt/HitmanPro.UTM/utmproxy
	ln -s /mnt/data/activation.conf $(1)/etc/activation.conf
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/../bin/utmproxy $(1)/opt/HitmanPro.UTM/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../bin/LICENSE $(1)/opt/HitmanPro.UTM/
	ln -s /opt/HitmanPro.UTM/utmproxy $(1)/usr/bin/utmproxy
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/../etc/init.d/utmproxy.sh $(1)/etc/init.d/
endef

$(eval $(call BuildPackage,utmproxy))
