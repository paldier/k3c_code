include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=lantiq-swaccel
PKG_VERSION:=2.4.0.0
PKG_RELEASE:=1

include $(INCLUDE_DIR)/package.mk

SWACCEL_DEPEDENT_FILES:= $(if $(CONFIG_PACKAGE_kmod-lantiq_ppa_vrx318_d5_mod), \
				$(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx330/soc/ltqmips_ppe_drv.ko,)

define KernelPackage/lantiq-swaccel
  SUBMENU:=Lantiq
  TITLE:=Lantiq Software Acceleration Module
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Technologies AG
  KCONFIG:=CONFIG_LTQ_PPA_API_SW_FASTPATH=y
  FILES:=$(LINUX_DIR)/drivers/net/lantiq_ppa/ppa_api/ppa_api_sw_accel_mod.ko \
		$(LINUX_DIR)/drivers/net/lantiq_ppa/ppa_api/swa_stack_al.ko \
		$(LINUX_DIR)/drivers/net/lantiq_ppa/ppa_api/ppa_api.ko $(SWACCEL_DEPEDENT_FILES)
endef

define Build/Prepare
	echo -n
endef

define Build/Compile
	echo -n;
endef

$(eval $(call KernelPackage,lantiq-swaccel))
