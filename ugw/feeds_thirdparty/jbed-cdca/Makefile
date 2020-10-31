#
# Copyright 2013-2014 Myriad Group AG. All Rights Reserved.
#
# This is proprietry software. See file LICENSE for more information.
#

include $(TOPDIR)/rules.mk

PKG_NAME:=jbed6
PKG_VERSION:=UGW-6.1-20141030-233372-eval
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/jbed6_MIPS_Linux
PKG_SOURCE:=Jbed6_Linux_MIPS_$(PKG_VERSION).tar.gz
PKG_SOURCE_URL:=http://localhost
PKG_MD5SUM:=ae3e483f6eaee91fb233928f6bafc403
PKG_MYRIAD_DIR:=/opt/myriad/
PKG_CAFFEINE_DIR:=/opt/caffeine/
PKG_CAT:=zcat

include $(INCLUDE_DIR)/package.mk

define Package/jbed6
  SECTION:=utils
  CATEGORY:=Utilities
  TITLE:=Myriad Java6 Virtual Machine (subset2)
  URL:=http://myriadgroup.com/
  DEPENDS:=+librt
endef

define Build/Compile
	:
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/$(PKG_MYRIAD_DIR)/bin
	$(INSTALL_DIR) $(1)/$(PKG_MYRIAD_DIR)/lib
	$(INSTALL_DIR) $(1)/$(PKG_MYRIAD_DIR)/lib/ext
	$(INSTALL_DIR) $(1)/$(PKG_MYRIAD_DIR)/lib/harmony
	$(INSTALL_DIR) $(1)/$(PKG_MYRIAD_DIR)/lib/security
	$(INSTALL_DIR) $(1)/$(PKG_CAFFEINE_DIR)
	$(INSTALL_BIN)  $(PKG_BUILD_DIR)/../bin/LICENSE                       $(1)/$(PKG_MYRIAD_DIR)/bin/
	$(INSTALL_BIN)  $(PKG_BUILD_DIR)/../bin/jbed6_J6Sa                    $(1)/$(PKG_MYRIAD_DIR)/bin/
	$(INSTALL_BIN)  $(PKG_BUILD_DIR)/../bin/java                          $(1)/$(PKG_MYRIAD_DIR)/bin/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/romresources.jar              $(1)/$(PKG_MYRIAD_DIR)/lib/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/nls_messages.jar              $(1)/$(PKG_MYRIAD_DIR)/lib/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/charset.jar                   $(1)/$(PKG_MYRIAD_DIR)/lib/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/icu4j_charset_51_2.jar        $(1)/$(PKG_MYRIAD_DIR)/lib/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/xercesImpl_2.11.0.jar         $(1)/$(PKG_MYRIAD_DIR)/lib/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/content-types.properties      $(1)/$(PKG_MYRIAD_DIR)/lib/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/security/java.policy          $(1)/$(PKG_MYRIAD_DIR)/lib/security/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/security/java.security        $(1)/$(PKG_MYRIAD_DIR)/lib/security/
	$(INSTALL_DATA) $(PKG_BUILD_DIR)/../lib/security/java.security.readme $(1)/$(PKG_MYRIAD_DIR)/lib/security/
	$(CP) ./files/*.zip $(1)/opt/caffeine/
endef

$(eval $(call BuildPackage,jbed6))

