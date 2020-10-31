#
# Openwrt feed for secure boot
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=secure_boot
PKG_BASE_NAME:=ltq_secureboot_test
PKG_VERSION:=1.0.0
PKG_SOURCE:=$(PKG_BASE_NAME)-$(PKG_VERSION).tar.gz
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_BASE_NAME)-$(PKG_VERSION)
PKG_SOURCE_URL:=https://localhost/

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Utilities
  TITLE:=secure boot test keys Lantiq XRX500 platforms
  URL:=http://www.lantiq.com
  DEPENDS:=@TARGET_lantiq_xrx500||TARGET_lantiq_xrx500_4kec @UBOOT_CONFIG_LTQ_SECURE_BOOT 
  MENU:=1
endef

define Package/$(PKG_NAME)/description
  This package contains pre-compiled binaries for secure boot
endef

define Package/$(PKG_NAME)/install
	cp -f $(PKG_BUILD_DIR)/keys/otp.bin $(BIN_DIR)/otp.bin 
endef

define Build/Compile
	sed -i '/.*CONFIG_UBOOT_CONFIG_SIGNTOOL.*/d' $(TOPDIR)/.config ;
	echo "CONFIG_UBOOT_CONFIG_SIGNTOOL=\"$(PKG_BUILD_DIR)/signtool\"" >> $(TOPDIR)/.config;
	sed -i '/.*CONFIG_UBOOT_CONFIG_PROD_UNIQUE_KEY.*/d' $(TOPDIR)/.config ;
	echo "CONFIG_UBOOT_CONFIG_PROD_UNIQUE_KEY=\"$(PKG_BUILD_DIR)/keys/CRkey.bin\"" >> $(TOPDIR)/.config;
	sed -i '/.*CONFIG_UBOOT_CONFIG_PRIVATE_KEY.*/d' $(TOPDIR)/.config ;
	echo "CONFIG_UBOOT_CONFIG_PRIVATE_KEY=\"$(PKG_BUILD_DIR)/keys/ecdsa_keypair_D.der\"" >> $(TOPDIR)/.config;
	sed -i '/.*CONFIG_UBOOT_CONFIG_CERTIFICATION.*/d' $(TOPDIR)/.config ;
	echo "CONFIG_UBOOT_CONFIG_CERTIFICATION_SUPPORT=y" >> $(TOPDIR)/.config;
	echo "CONFIG_UBOOT_CONFIG_CERTIFICATION=\"$(PKG_BUILD_DIR)/keys/cert1.bin\"" >> $(TOPDIR)/.config;
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
