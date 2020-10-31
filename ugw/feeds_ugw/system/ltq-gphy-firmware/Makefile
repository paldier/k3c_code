#
# Openwrt feed Makefile for ltq-gphy-firmware
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=ltq-gphy-firmware
PKG_VERSION:=2.1.1
PKG_BASE_NAME:=ltq_fw_PHY_IP-$(PKG_VERSION)
PKG_SOURCE:=$(PKG_BASE_NAME).tar.gz
PKG_RELEASE:=1
PKG_SOURCE_URL:=http://localhost
PKG_BUILD_DIR:=$(KERNEL_BUILD_DIR)/$(PKG_BASE_NAME)
PKG_BUILD_DEPENDS:=open_uboot

include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/package.mk

ifeq ($(DUMP),)
  STAMP_CONFIGURED:=$(strip $(STAMP_CONFIGURED))_$(shell $(SH_FUNC) grep '_SW_PORT2_MII' $(TOPDIR)/.config | md5s)
endif

GPHY_FIRMWARE_IMAGE	:=gphy_firmware.img
MKIMAGE			:=$(STAGING_DIR_HOST)/bin/mkimage
LZMA			:=$(STAGING_DIR_HOST)/bin/lzma
PLATFORM_NAME		:=$(subst lantiq_,,$(subst platform_,,$(SUBTARGET)))


ifeq ("$(PLATFORM_NAME)","vr9")
  PLATFORM_NAME:=xrx200
endif
ifeq ("$(PLATFORM_NAME)","ar10")
  PLATFORM_NAME:=xrx300
endif
ifeq ("$(PLATFORM_NAME)","xrx330")
  PLATFORM_NAME:=xrx300
endif

ifeq ("$(PLATFORM_NAME)","xrx200")
  XWAY_NAME		:=xRx2xx
  ifeq ($(PROFILE),EASY220W2)
    FIRMWARE_1=ltq_fw_PHY11G_IP_1v6_$(XWAY_NAME)_A22_*.bin
    FIRMWARE_1_STRING="VR9 V1.2 GPHY GE"

    FIRMWARE_2=ltq_fw_PHY22F_IP_1v5_$(XWAY_NAME)_A22_*.bin
    FIRMWARE_2_STRING="VR9 V1.2 GPHY FE"
  else ifeq ($(PROFILE),easy220_v2)
    FIRMWARE_1=ltq_fw_PHY11G_IP_1v6_$(XWAY_NAME)_A22_*.bin
    FIRMWARE_1_STRING="VR9 V1.2 GPHY GE"
    
    FIRMWARE_2=ltq_fw_PHY22F_IP_1v5_$(XWAY_NAME)_A22_*.bin
    FIRMWARE_2_STRING="VR9 V1.2 GPHY FE"
  else
    ifeq ("$(CONFIG_UBOOT_CONFIG_VR9_SW_PORT2_MII)","y")
      FIRMWARE_1=ltq_fw_PHY22F_IP_1v5_$(XWAY_NAME)_A22_*.bin
      FIRMWARE_1_STRING="VR9 V1.2 GPHY FE"

      FIRMWARE_2=ltq_fw_PHY22F_IP_1v4_$(XWAY_NAME)_A14_*.bin
      FIRMWARE_2_STRING="VR9 V1.1 GPHY FE"
    else
      FIRMWARE_1=ltq_fw_PHY11G_IP_1v6_$(XWAY_NAME)_A22_*.bin
      FIRMWARE_1_STRING="VR9 V1.2 GPHY GE"

      FIRMWARE_2=ltq_fw_PHY11G_IP_1v4_$(XWAY_NAME)_A14_*.bin
      FIRMWARE_2_STRING="VR9 V1.1 GPHY GE"
    endif
  endif
endif

ifeq ("$(PLATFORM_NAME)","xrx300")
  XWAY_NAME		:=xRx3xx
  FIRMWARE_1=ltq_fw_PHY11G_IP_1v6_$(XWAY_NAME)_A21_*.bin
  FIRMWARE_1_STRING="VR9 V1.2 GPHY GE"

  FIRMWARE_2=ltq_fw_PHY22F_IP_1v5_$(XWAY_NAME)_A21_*.bin
  FIRMWARE_2_STRING="VR9 V1.2 GPHY FE"
endif

ifeq ("$(PLATFORM_NAME)","xrx500")
  XWAY_NAME     :=xRx5xx
  ifeq ("$(CONFIG_UBOOT_CONFIG_GRX500_A21)","y")
    FIRMWARE_1=ltq_fw_PHY11G_IP_1v0_$(XWAY_NAME)_A21_*.bin
    FIRMWARE_1_STRING="GRX500 V1.1 GPHY BE"
  else
    FIRMWARE_1=ltq_fw_PHY11G_IP_1v6_$(XWAY_NAME)_A11_*.bin
    FIRMWARE_1_STRING="GRX500 V1.1 GPHY BE"
  endif
endif

define Package/$(PKG_NAME)
  SECTION:=firmware
  CATEGORY:=Lantiq
  TITLE:=GPHY Firmware
  URL:=http://www.lantiq.com/
  MAINTAINER:=Lantiq Deutschland GmbH
  DEPENDS:=@(!UBOOT_CONFIG_VR9_GPHY_FW_EMBEDDED) +@BUSYBOX_CONFIG_UNLZMA +common-utils
  MENU:=1
endef

define mkimage_extra_checks
$(if $(CONFIG_UBOOT_CONFIG_LTQ_IMAGE_EXTRA_CHECKS), \
	-B $(CONFIG_UBOOT_CONFIG_VENDOR_NAME) \
	-V $(CONFIG_UBOOT_CONFIG_BOARD_NAME) \
	-b $(CONFIG_UBOOT_CONFIG_BOARD_VERSION) \
	-c $(CONFIG_UBOOT_CONFIG_CHIP_NAME) \
	-p $(CONFIG_UBOOT_CONFIG_CHIP_VERSION) \
	-s $(CONFIG_UBOOT_CONFIG_SW_VERSION) \
)
endef

define Package/$(PKG_NAME)/description
	Lantiq GPHY Firmware
endef

define Build/Configure
endef


define Build/Compile
	@echo $(PLATFORM_NAME)----------------------------------platform name
	cd $(PKG_BUILD_DIR); \
	[ -d $(XWAY_NAME) ] && cd $(XWAY_NAME); \
	$(KERNEL_BUILD_DIR)/u-boot-*/tools/mkimage -A mips -T firmware -C none -n $(FIRMWARE_1_STRING) $(call mkimage_extra_checks) -d $(FIRMWARE_1) tmp1.img; \
	$(KERNEL_BUILD_DIR)/u-boot-*/tools/mkimage -A mips -T firmware -C none -n $(FIRMWARE_2_STRING) $(call mkimage_extra_checks) -d $(FIRMWARE_2) tmp2.img; \
	cat tmp2.img >> tmp1.img; \
	$(LZMA) e tmp1.img  tmp1.lzma; \
	$(KERNEL_BUILD_DIR)/u-boot-*/tools/mkimage -A mips -T multi -C lzma -n "GPHY Firmware" $(call mkimage_extra_checks) -d tmp1.lzma $(GPHY_FIRMWARE_IMAGE)
endef

define Package/$(PKG_NAME)/install
	mkdir -p $(TARGET_IMG_DIR)
	cp -f $(PKG_BUILD_DIR)/$(XWAY_NAME)/$(GPHY_FIRMWARE_IMAGE) $(TARGET_IMG_DIR)/
	$(if $(CONFIG_NEW_FRAMEWORK),\
		$(INSTALL_DIR) $(1)/etc/; \
		$(INSTALL_BIN) ./files/load_gphy_firmware.sh $(1)/etc/load_gphy_firmware_preinit.sh; \
		$(SED) 's/start/gphy_fw_load/g' -e '/START.*=/d' -e \
			's/.*rc.common.*/#\!\/bin\/sh/g' -e '$$$$agphy_fw_load;\n' \
			$(1)/etc/load_gphy_firmware_preinit.sh
	,\
		$(INSTALL_DIR) $(1)/etc/init.d; \
		$(INSTALL_BIN) ./files/load_gphy_firmware.sh $(1)/etc/init.d/; \
	)
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
