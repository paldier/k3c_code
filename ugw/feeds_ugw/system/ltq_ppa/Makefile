include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk
SUBTARGET_SUFFIX:=$(shell echo $(subst .,_,$(subst -,_,$(subst /,_,$(SUBTARGET)))) | cut -d_ -f2-)

PKG_NAME:=lantiq_ppa
PKG_VERSION:=1.0
PKG_RELEASE:=1

include $(INCLUDE_DIR)/package.mk

define KernelPackage/lantiq_ppa_xrx200
 SUBMENU:=Lantiq
 TITLE:=LTQ PPA Support for xRX200
 DEPENDS:=@TARGET_lantiq_xrx200
 KCONFIG:= \
	CONFIG_LTQ_PPA_XRX200=y
endef

define KernelPackage/lantiq_ppa_xrx300
 SUBMENU:=Lantiq
 TITLE:=LTQ PPA Support for xRX300
 DEPENDS:=@TARGET_lantiq_xrx300
 KCONFIG:= \
	CONFIG_LTQ_PPA_XRX300=y
endef


define KernelPackage/lantiq_ppa_xrx330
 SUBMENU:=Lantiq
 TITLE:=LTQ PPA Support for xRX330
 DEPENDS:=@TARGET_lantiq_xrx330
 KCONFIG:= \
	CONFIG_LTQ_PPA_XRX330=y
endef

define KernelPackage/lantiq_ppa
 SUBMENU:=Lantiq
 TITLE:=LTQ PPA Support
 DEPENDS:=@TARGET_lantiq
 KCONFIG:= \
	CONFIG_LTQ_PPA=y \
	CONFIG_LTQ_PPA_API=y \
	CONFIG_LTQ_PPA_API_DIRECTPATH=y \
	CONFIG_LTQ_PPA_API_DIRECTPATH_BRIDGING=y \
	CONFIG_LTQ_PPA_API_PROC=y \
	CONFIG_LTQ_PPA_DATAPATH=y
endef

define KernelPackage/lantiq_ppa/description
 Kernel support for PPA acceleration
endef

define KernelPackage/lantiq_ppa_mod
 SUBMENU:=Lantiq
 TITLE:=LTQ PPA Support
 DEPENDS:=@TARGET_lantiq
 KCONFIG:= \
	CONFIG_LTQ_PPA=y \
	CONFIG_LTQ_PPA_API \
	CONFIG_LTQ_PPA_API_DIRECTPATH=y \
	CONFIG_LTQ_PPA_API_DIRECTPATH_BRIDGING=y \
	CONFIG_LTQ_PPA_API_PROC \
	CONFIG_LTQ_PPA_DATAPATH

 $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
 FILES:= \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/ppa_api/ppa_api.$(LINUX_KMOD_SUFFIX) \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/ppa_api/ppa_api_proc.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_mod/description
 Kernel support for PPA acceleration
endef

define KernelPackage/lantiq_ppa_a5_builtin
  $(call KernelPackage/lantiq_ppa)
  TITLE+=(A5 mode - built-in)
  DEPENDS+=@!PACKAGE_kmod-lantiq_ppa_a5_mod @!PACKAGE_kmod-lantiq_atm_builtin @TARGET_lantiq_ar9||TARGET_lantiq_xrx200||TARGET_lantiq_xrx300 @!TARGET_lantiq_xrx330 @!TARGET_lantiq_xrx300_EASY388_VRX318 @!TARGET_lantiq_xrx300_EASY388_BOND +kmod-atm_stack
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_A5=y
endef

define KernelPackage/lantiq_ppa_a5_builtin/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA A5 Mode - built-in
endef


define KernelPackage/lantiq_ppa_a5_mod
  $(call KernelPackage/lantiq_ppa_mod)
  TITLE+=(A5 mode - pluggable module)
  DEPENDS+=@!PACKAGE_kmod-fs-nfs-root @!PACKAGE_kmod-lantiq_atm_builtin @TARGET_lantiq_ar9||TARGET_lantiq_xrx200||TARGET_lantiq_xrx300 @!TARGET_lantiq_xrx330 @!TARGET_lantiq_xrx300_EASY388_VRX318 +kmod-atm_stack
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_A5=y

  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/a5/ppa_datapath_$(SUBTARGET_SUFFIX)_a5.$(LINUX_KMOD_SUFFIX) \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/a5/ppa_hal_$(SUBTARGET_SUFFIX)_a5.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_a5_mod/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA A5 Mode - pluggable module
endef

define KernelPackage/lantiq_ppa_grx500_mpe
 $(call KernelPackage/lantiq_ppa_mod)
 TITLE+=for GRX500
 DEPENDS+=@TARGET_lantiq_xrx500 +PACKAGE_kmod-lantiq-swaccel:kmod-lantiq-swaccel
 KCONFIG+= \
        CONFIG_LTQ_PPA_MPE_HAL \
        
  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
        $(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/ltq_mpe_hal_drv.$(LINUX_KMOD_SUFFIX) \
        $(LINUX_DIR)/drivers/net/lantiq_ppa/ppa_api/ppa_api_tmplbuf.ko 
 
endef

define KernelPackage/lantiq_ppa_grx500_mpe/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA GRX500 - pluggable module
endef

define KernelPackage/lantiq_ppa_mpe_ip97
 SUBMENU:=Lantiq
 TITLE:=LTQ PPA MPE IP97 Support for xRX500
 DEPENDS:=@TARGET_lantiq_xrx500
 KCONFIG:= \
	CONFIG_LTQ_PPA_MPE_IP97=y
endef

define KernelPackage/lantiq_ppa_mpe_ip97/description
  PPA GRX500 MPE IP97 Support
endef

define KernelPackage/lantiq_ppa_grx500_mpe/install
	$(if $(CONFIG_NEW_FRAMEWORK), \
		$(INSTALL_DIR) $(1)/lib/firmware; \
		$(INSTALL_BIN) $(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx500/mpe_fw_be.img $(1)/lib/firmware/; \
	, \
		$(INSTALL_DIR) $(1)/opt/lantiq/bin/; \
		$(INSTALL_DIR) $(1)/etc/hotplug.d/firmware; \
		$(INSTALL_BIN) $(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx500/mpe_fw_be.img $(1)/opt/lantiq/bin/; \
		$(INSTALL_BIN) $(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx500/20-mpe.hotplug $(1)/etc/hotplug.d/firmware/; \
	)
endef


define KernelPackage/lantiq_ppa_grx500
 $(call KernelPackage/lantiq_ppa_mod)
 TITLE+=for GRX500
 DEPENDS+=@TARGET_lantiq_xrx500
 KCONFIG+= \
        CONFIG_LTQ_PPA_GRX500=y \
        CONFIG_LTQ_PPA_PAE_HAL 
        
  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
        $(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/ltq_pae_hal.$(LINUX_KMOD_SUFFIX) \
        $(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/ltq_tmu_hal_drv.$(LINUX_KMOD_SUFFIX) \
        $(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/ltq_directpath_datapath.$(LINUX_KMOD_SUFFIX) \
        $(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/ltq_dp_test.$(LINUX_KMOD_SUFFIX)
 
endef

define KernelPackage/lantiq_ppa_grx500/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA GRX500 - pluggable module
endef

define KernelPackage/lantiq_ppa_d5_builtin
  $(call KernelPackage/lantiq_ppa)
  TITLE+=(D5 mode - built-in)
  DEPENDS+=@!PACKAGE_kmod-lantiq_ppa_d5_mod @!PACKAGE_kmod-lantiq_eth_drv_builtin @TARGET_lantiq_ar9||TARGET_lantiq_xrx200 @!TARGET_lantiq_xrx330 @!TARGET_lantiq_xrx300_EASY388_VRX318 @!TARGET_lantiq_xrx300_EASY388_BOND
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_D5=y
endef

define KernelPackage/lantiq_ppa_d5_builtin/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA D5 Mode - built-in
endef


define KernelPackage/lantiq_ppa_d5_mod
  $(call KernelPackage/lantiq_ppa_mod)
  TITLE+=(D5 mode - pluggable module)
  DEPENDS+=@!PACKAGE_kmod-fs-nfs-root @!PACKAGE_kmod-lantiq_eth_drv_builtin @TARGET_lantiq_ar9||TARGET_lantiq_xrx200||TARGET_lantiq_xrx300 @!TARGET_lantiq_xrx300_EASY388_VRX318
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_D5=y

  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/d5/ppa_datapath_$(SUBTARGET_SUFFIX)_d5.$(LINUX_KMOD_SUFFIX) \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/d5/ppa_hal_$(SUBTARGET_SUFFIX)_d5.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_d5_mod/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA D5 Mode - pluggable module
endef



define KernelPackage/lantiq_ppa_e5_builtin
  $(call KernelPackage/lantiq_ppa)
  TITLE+=(E5 mode - built-in)
  DEPENDS+=@!PACKAGE_kmod-lantiq_ppa_e5_mod @!PACKAGE_kmod-lantiq_eth_drv_builtin @TARGET_lantiq_ar9||@TARGET_lantiq_xrx200 @!TARGET_lantiq_xrx330 @!TARGET_lantiq_xrx300_EASY388_VRX318 @!TARGET_lantiq_xrx300_EASY388_BOND +kmod-atm_stack
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_E5=y
endef

define KernelPackage/lantiq_ppa_e5_builtin/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA E5 Mode - built-in
endef



define KernelPackage/lantiq_ppa_e5_mod
  $(call KernelPackage/lantiq_ppa_mod)
  TITLE+=(E5 mode - pluggable module)
  DEPENDS+=@!PACKAGE_kmod-fs-nfs-root @!PACKAGE_kmod-lantiq_eth_drv_builtin @TARGET_lantiq_ar9||@TARGET_lantiq_xrx200||@TARGET_lantiq_xrx300 @!TARGET_lantiq_xrx300_EASY388_VRX318 +kmod-atm_stack
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_E5=y

  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/e5/ppa_datapath_$(SUBTARGET_SUFFIX)_e5.$(LINUX_KMOD_SUFFIX) \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/$(SUBTARGET_SUFFIX)/e5/ppa_hal_$(SUBTARGET_SUFFIX)_e5.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_e5_mod/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA E5 Mode - pluggable module
endef


define KernelPackage/lantiq_ppa_vrx318_d5_builtin
  $(call KernelPackage/lantiq_ppa)
  TITLE+=(D5 mode - built-in)
  DEPENDS+=@!PACKAGE_kmod-lantiq_ppa_vrx318_d5_mod @!PACKAGE_kmod-lantiq_eth_drv_builtin @TARGET_lantiq_xrx330||TARGET_lantiq_xrx300_EASY388_VRX318||TARGET_lantiq_xrx300_EASY388_BOND
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_D5=y
endef

define KernelPackage/lantiq_ppa_vrx318_d5_builtin/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA D5 Mode - built-in
endef


define KernelPackage/lantiq_ppa_vrx318_d5_mod
  $(call KernelPackage/lantiq_ppa_mod)
  TITLE+=(D5 mode - module)
  DEPENDS+=@!PACKAGE_kmod-fs-nfs-root @!PACKAGE_kmod-lantiq_eth_drv_builtin @!PACKAGE_kmod-lantiq_ppa_d5_mod @TARGET_lantiq_xrx330||TARGET_lantiq_xrx300_EASY388_VRX318||@TARGET_lantiq_xrx300_EASY388_BOND
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_D5=y \
	CONFIG_IFX_DMA_DESCRIPTOR_NUMBER=64

  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/xrx330/soc/ltqmips_ppe_drv.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_vrx318_d5_mod/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA D5 Mode - pluggable module
endef


define KernelPackage/lantiq_ppa_vrx318_e1_builtin
  $(call KernelPackage/lantiq_ppa)
  TITLE+=(E1 mode - built-in)
  DEPENDS+=@!PACKAGE_kmod-lantiq_ppa_vrx318_e1_mod @!PACKAGE_kmod-lantiq_eth_drv_builtin @TARGET_lantiq_xrx330||TARGET_lantiq_xrx300_EASY388_VRX318||TARGET_lantiq_xrx300_EASY388_BOND +kmod-atm_stack +kmod-lantiq_ppa_vrx318_d5_builtin
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_E5=n \
	CONFIG_LTQ_PPA_E1=y
endef

define KernelPackage/lantiq_ppa_vrx318_e1_builtin/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA E1 Mode - built-in
endef


define KernelPackage/lantiq_ppa_vrx318_e1_mod
  $(call KernelPackage/lantiq_ppa_vrx318_d5_mod)
  TITLE+=(E1 mode)
  DEPENDS+=@!PACKAGE_kmod-fs-nfs-root @!PACKAGE_kmod-lantiq_ppa_e5_mod @!PACKAGE_kmod-lantiq_ppa_e5_builtin @TARGET_lantiq_xrx330||@TARGET_lantiq_xrx300_EASY388_VRX318||@TARGET_lantiq_xrx300_EASY388_BOND +kmod-atm_stack +kmod-lantiq_ppa_vrx318_d5_mod
  KCONFIG += \
	CONFIG_LTQ_PPA_E5=n \
	CONFIG_LTQ_PPA_E1=y

  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/xrx330/smartphy/ltqmips_vrx318_e1.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_vrx318_e1_mod/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA E1 Mode - pluggable module
endef


define KernelPackage/lantiq_ppa_vrx318_a1_builtin
  $(call KernelPackage/lantiq_ppa)
  TITLE+=(A1 mode - built-in)
  DEPENDS+=@!PACKAGE_kmod-lantiq_ppa_vrx318_a1_mod @!PACKAGE_kmod-lantiq_atm_builtin @TARGET_lantiq_xrx330||TARGET_lantiq_xrx300_EASY388_VRX318||TARGET_lantiq_xrx300_EASY388_BOND +kmod-atm_stack +kmod-lantiq_ppa_vrx318_d5_builtin
  KCONFIG += \
	CONFIG_IFX_EXTRA_CFG=y \
	CONFIG_LTQ_PPA_A5=n \
	CONFIG_LTQ_PPA_A1=y
endef

define KernelPackage/lantiq_ppa_vrx318_a1_builtin/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA A1 Mode - built-in
endef


define KernelPackage/lantiq_ppa_vrx318_a1_mod
  $(call KernelPackage/lantiq_ppa_vrx318_d5_mod)
  TITLE+=(A1 mode - module)
  DEPENDS+=@!PACKAGE_kmod-fs-nfs-root @!PACKAGE_kmod-lantiq_atm_builtin @!PACKAGE_kmod-lantiq_ppa_a5_mod @!PACKAGE_kmod-lantiq_ppa_a5_builtin @TARGET_lantiq_xrx330||TARGET_lantiq_xrx300_EASY388_VRX318||@TARGET_lantiq_xrx300_EASY388_BOND +kmod-atm_stack +kmod-lantiq_ppa_vrx318_d5_mod
  KCONFIG += \
	CONFIG_LTQ_PPA_A1=y \
	CONFIG_LTQ_PPA_A5=n

  $(if $(wildcard $(LINUX_DIR)/drivers/net/lantiq_ppa),PPAPATH:="lantiq_ppa",PPAPATH:="lantiq_ppa/src")
  FILES += \
	$(LINUX_DIR)/drivers/net/$(PPAPATH)/platform/xrx330/smartphy/ltqmips_vrx318_a1.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/lantiq_ppa_vrx318_a1_mod/description
  $(call KernelPackage/lantiq_ppa/description)
  PPA A1 Mode - pluggable module
endef


define KernelPackage/lantiq_directlink_support
  SUBMENU:=Lantiq
  TITLE:=Direct Link Support
  DEPENDS:=@TARGET_lantiq_xrx330 +PACKAGE_kmod-lantiq_ppa_vrx318_d5_mod:kmod-lantiq_ppa_vrx318_d5_mod
  KCONFIG:= \
        CONFIG_ACCL_11AC=m

  FILES:=\
	$(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx330/soc/directlink/ltqmips_dtlk.ko
endef

define KernelPackage/lantiq_directlink_support/description
  Direct Link Support
endef

define KernelPackage/lantiq_directlink_support/install
	$(INSTALL_DIR) $(1)/$(MODULES_SUBDIR)/
	$(if $(CONFIG_PACKAGE_kmod-smvp), \
		$(INSTALL_BIN) $(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx330/soc/directlink/dlrx_fw_smp.ko $(1)/$(MODULES_SUBDIR)/dlrx_fw.ko; \
	, \
		$(INSTALL_BIN) $(LINUX_DIR)/drivers/net/lantiq_ppa/platform/xrx330/soc/directlink/dlrx_fw.ko $(1)/$(MODULES_SUBDIR)/; \
	)
endef

define KernelPackage/lantiq_directlink_support_xrx500
  SUBMENU:=Lantiq
  TITLE:=Direct Link Support for xrx500
  DEPENDS:=@TARGET_lantiq_xrx500 \
			+PACKAGE_kmod-lantiq_ppa_grx500_mpe:kmod-lantiq_ppa_grx500_mpe
  KCONFIG:= \
        CONFIG_ACCL_11AC=m

  FILES:=\
	$(LINUX_DIR)/drivers/net/ethernet/lantiq/directlink/ltqmips_dtlk.ko \
	$(LINUX_DIR)/drivers/net/ethernet/lantiq/directlink/dlrx/dlrx_fw/dlrx_fw.ko
endef

define KernelPackage/lantiq_directlink_support_xrx500/description
  Direct Link Support for xrx500 platforms
endef

#define KernelPackage/lantiq_ppa_bridge_sessions
#  SUBMENU:=Lantiq
#  TITLE:=Learn bridge sessions in PPA
#  KCONFIG += \
#	CONFIG_LTQ_BRIDGE_LEARNING=y \
#	CONFIG_BRIDGE_NETFILTER=y

#endef

#define KernelPackage/lantiq_ppa_bridge_sessions/description
#  Learn bridge sessions in PPA - Enable this to learn bridge sessions in PPA
#endef

define KernelPackage/lantiq_ppa_gre
  SUBMENU:=Lantiq
  TITLE:=Support GRE Acceleration in PPA
  DEPENDS:=+kmod-gre +kmod-gre6
  KCONFIG += \
	CONFIG_LTQ_BRIDGE_LEARNING=y \
	CONFIG_BRIDGE_NETFILTER=y
endef

define KernelPackage/lantiq_ppa_gre/description
  GRE/EoGRE Acceleration in PPA - Enable this to support GRE/EoGRE in PPA
endef

define Build/Prepare
	echo -n;
endef

define Build/Compile
	echo -n;
endef

$(eval $(call KernelPackage,lantiq_ppa_xrx200))
$(eval $(call KernelPackage,lantiq_ppa_xrx300))
$(eval $(call KernelPackage,lantiq_ppa_xrx330))
$(eval $(call KernelPackage,lantiq_ppa_a5_builtin))
$(eval $(call KernelPackage,lantiq_ppa_a5_mod))
$(eval $(call KernelPackage,lantiq_ppa_grx500))
$(eval $(call KernelPackage,lantiq_ppa_grx500_mpe))
$(eval $(call KernelPackage,lantiq_ppa_mpe_ip97))
$(eval $(call KernelPackage,lantiq_ppa_d5_builtin))
$(eval $(call KernelPackage,lantiq_ppa_d5_mod))
$(eval $(call KernelPackage,lantiq_ppa_e5_builtin))
$(eval $(call KernelPackage,lantiq_ppa_e5_mod))
$(eval $(call KernelPackage,lantiq_ppa_vrx318_d5_builtin))
$(eval $(call KernelPackage,lantiq_ppa_vrx318_d5_mod))
$(eval $(call KernelPackage,lantiq_ppa_vrx318_e1_builtin))
$(eval $(call KernelPackage,lantiq_ppa_vrx318_e1_mod))
$(eval $(call KernelPackage,lantiq_ppa_vrx318_a1_builtin))
$(eval $(call KernelPackage,lantiq_ppa_vrx318_a1_mod))
$(eval $(call KernelPackage,lantiq_directlink_support))
$(eval $(call KernelPackage,lantiq_directlink_support_xrx500))
#$(eval $(call KernelPackage,lantiq_ppa_bridge_sessions))
$(eval $(call KernelPackage,lantiq_ppa_gre))
