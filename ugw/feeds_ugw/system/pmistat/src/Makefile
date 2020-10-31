#
# Makefile for pmon
#

CFLAGS += -O2 -I$(KERNEL_INCLUDE_PATH) -DCONFIG_$(PLATFORM)

all: pmistat

ppacmd: pmistat.o

clean:
	rm -f *.o pmistat
