/*
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 as published
 *  by the Free Software Foundation.
 *
 *  Copyright (C) 2015 victor yeo <s.yeo.ee@lantiq.com>
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/ioctl.h>
#include <string.h>

static int PMISTAT_MINOR = 0;
static int PMISTAT_MAJOR = 240;

typedef struct {
	int address;
	int value;
} ioctl_arg;

/* pmi register offset */
#define PMI_COUNTER00	0x0
#define PMI_COUNTER01	0x4
#define PMI_COUNTER02	0x8
#define PMI_COUNTER03 	0xC
#define PMI_COUNTER04 	0x10
#define PMI_F0_MASK0 	0x20
#define PMI_F0_MASK1	0x24
#define PMI_F0_PATN0	0x28 
#define PMI_F0_PATN1	0x2C
#define PMI_F0_RCNF0 	0x30
#define PMI_F0_RCNF1	0x34 
#define PMI_COUNTER10 	0x40
#define PMI_COUNTER11 	0x44
#define PMI_COUNTER12 	0x48
#define PMI_COUNTER13	0x4C 
#define PMI_COUNTER14 	0x50
#define PMI_F1_MASK0 	0x60
#define PMI_F1_MASK1  	0x64
#define PMI_F1_PATN0 	0x68
#define PMI_F1_PATN1 	0x6C
#define PMI_F1_RCNF0  	0x70
#define PMI_F1_RCNF1  	0x74
#define PMI_CONTROL 	0x80
#define PMI_INT_CTL 	0x90

#define TYPE 0xF0
#define IOCTL_PMI_READ 				_IOR(TYPE, 0, ioctl_arg *)
#define IOCTL_PMI_WRITE				_IOW(TYPE, 1, ioctl_arg *)
#define IOCTL_PMI_SRAM_TEST			_IO(TYPE, 2)
#define IOCTL_PMI_SRAM_INTR_TEST	_IO(TYPE, 3)
#define IOCTL_PMI_SRAM_DDR_TEST		_IO(TYPE, 4)

typedef struct {
	int PMI_COUNTER0_0;	
	int PMI_COUNTER0_1;	
	int PMI_COUNTER0_2;	
	int PMI_COUNTER0_3;	
	int PMI_COUNTER0_4;
	int PMI_F0_MASK_0;
} PMISTAT_INFO;

static char *devname = "/dev/pmi-ngi";
static char *Progname = "pmistat";
static char *Version = "1.0.1";
static PMISTAT_INFO pcinfo;
static int fd;

int open_pmi_device(void)
{
	int ret = -1;
	
	if (access(devname, 0) != 0) {
		printf("Major number %d\n", PMISTAT_MAJOR);
		if (mknod(devname, S_IFCHR+0644, makedev(PMISTAT_MAJOR,PMISTAT_MINOR)) != 0) {
            perror("mknod(2)");
        	printf("mknod failed.\n"  );
			return -1;
		}
	}

	if ((ret = open(devname, O_RDWR)) < 0) {
		perror("open(2)");
        printf("open device failed.\n"  );
		return -2;
	}
	return ret;
}

int set_pmistat(char *argv[], int *i_p)
{
	int ret;
	ioctl_arg mode;
	int rdata;
	char * cp;	
	int int_cp;
	int group = 0;
	int mcmd = 0;
	int MInitId = 0;
	int SInitId = 0;
	int i_v = *i_p;
	int offset;
	
	fd = open_pmi_device();
	
	while( 1 ) {
		if (argv[i_v] == NULL)
			break;
		printf( "argv = '%s' %d\n", argv[i_v], i_v);
		
		cp = argv[i_v];
		if (strncasecmp(cp, "g", 1) == 0) {
			cp =cp+2;
			int_cp = atoi(cp);
			printf( "cp : %s %d\n", cp, int_cp );
			if (int_cp == 0)
				group = 0;
			else 
				group = 1;
		} else if (strncasecmp(cp, "m", 1) == 0) {
			printf( "cp : %s\n", cp );
			cp = cp+2;
			if (strncasecmp(cp, "wrnp", 4) == 0)
				mcmd = 2;
			else if (strncasecmp(cp, "rd", 2) == 0) 
				mcmd = 0;
			else if (strncasecmp(cp, "wr", 2) == 0)
				mcmd = 1; 
		} else {
			printf( "cp : %s\n", cp );
			MInitId = (int)strtoul(cp, NULL, 16);
			
			i_v++;
			if (argv[i_v] == NULL)
				break;
			cp = argv[i_v];
			printf( "cp : %s\n", cp );
			SInitId = (int)strtoul(cp, NULL, 16);
		}
		
		i_v++;
    }
	
	printf("group %d mcmd %d MInitId 0x%x SInitId 0x%x\n", group, mcmd, MInitId, SInitId);
	
	if (group == 0) {
		offset = 0x0;
	} else {
		offset = 0x40;
	}
	
	if (mcmd == 1) { // monitor write events
		// control mask makes only first control byte and bit 6 valid (MCmd and SCmdAccept).
		mode.address = offset + PMI_F0_MASK0;
		mode.value = 0xff40;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// id mask makes only MinitID valid
 		mode.address = offset + PMI_F0_MASK1;
		mode.value = 0x1f00;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// write events (MCmd=WR, SCmdAccept=1, MRespAccept=1, SResp=DVA; not all used)
		mode.address = offset + PMI_F0_PATN0;
		mode.value = 0x0261;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// ID is given
		mode.address = offset + PMI_F0_PATN1;
		mode.value = 0x0000 + (MInitId << 8) + SInitId;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// config block added to corresponding mask block should add up to ffff
		mode.address = offset + PMI_F0_RCNF0;
		mode.value = 0x00bf;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// config block added to corresponding mask block should add up to ffff
		mode.address = offset + PMI_F0_RCNF1;
		mode.value = 0xe0ff;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
	} else if (mcmd == 2) { // monitor write np events
		// control mask makes only first control byte and bit 6 valid (MCmd and SCmdAccept).
		mode.address = offset + PMI_F0_MASK0;
		mode.value = 0x002f;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// id mask makes only MinitID valid
 		mode.address = offset + PMI_F0_MASK1;
		mode.value = 0x001f;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// write events (MCmd=WR, SCmdAccept=1, MRespAccept=1, SResp=DVA; not all used)
		mode.address = offset + PMI_F0_PATN0;
		mode.value = 0x2062;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// ID is given
		mode.address = offset + PMI_F0_PATN1;
		mode.value = 0x0000 + (MInitId << 8) + SInitId;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// config block added to corresponding mask block should add up to ffff
		mode.address = offset + PMI_F0_RCNF0;
		mode.value = 0xffd0;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// config block added to corresponding mask block should add up to ffff
		mode.address = offset + PMI_F0_RCNF1;
		mode.value = 0xffe0;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");	
	} else { // monitor read events
		// control mask makes only bits 5 and 3-0 valid (MRespAccept and SResp)
		mode.address = offset + PMI_F0_MASK0;
		mode.value = 0x002f;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// id mask makes only SinitID valid
 		mode.address = offset + PMI_F0_MASK1;
		mode.value = 0x001f;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// read events (MCmd=RD, SCmdAccept=1, MRespAccept=1, SResp=DVA; not all used)
		mode.address = offset + PMI_F0_PATN0;
		mode.value = 0x0462;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// ID is given
		mode.address = offset + PMI_F0_PATN1;
		mode.value = 0x0000 + (MInitId << 8) + SInitId;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// config block added to corresponding mask block should add up to ffff
		mode.address = offset + PMI_F0_RCNF0;
		mode.value = 0xffd0;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
		// config block added to corresponding mask block should add up to ffff
		mode.address = offset + PMI_F0_RCNF1;
		mode.value = 0xffe0;
		if (ioctl(fd, IOCTL_PMI_WRITE, &mode) != 0)
			printf("ioctl error\n");
 	}
	
	/*mode.address = 0x0;
	mode.value = 0xff;
    ioctl(fd, IOCTL_PMI_WRITE, &mode);
	mode.value  = 0x0;
    ioctl(fd, IOCTL_PMI_READ, &mode);
	printf("0x%x\n",mode.address);
	printf("0x%x\n",mode.value);*/
	
	return ret;
}

int read_pmistat(char * argv)
{
	int ret = 0;
	ioctl_arg mode;
	int rdata;
	int counter;
	int i;
	
	fd = open_pmi_device();
	
	if (strcmp(argv, "all")==0) {
		for (i = 0; i < 5; i++) {
			mode.address = 0x0 + 4*i;
			if (ioctl(fd, IOCTL_PMI_READ, &mode) != 0)
				printf("ioctl error\n");
			if (i%2 == 0)
				printf("counter 0%d: 0x%02x\t", i, mode.value);
			else 
				printf("counter 0%d: 0x%02x\n", i, mode.value);
		}
		for (i = 0; i < 5; i++) {
			mode.address = 0x40 + 0x0 + 4*i;
			if (ioctl(fd, IOCTL_PMI_READ, &mode) != 0)
				printf("ioctl error\n");
			if (i%2 == 0)
				printf("counter 1%d: 0x%02x\n", i, mode.value);
			else 
				printf("counter 1%d: 0x%02x\t", i, mode.value);
		}
		printf("\n");
		return ret;
	}
	
	counter = atoi(argv);
	printf("counter %d\n", counter);
	
	if (counter < 10)
		mode.address = counter*4;
	else
		mode.address = 24 + counter*4;
	printf("address 0x%x\n", mode.address );
    if (ioctl(fd, IOCTL_PMI_READ, &mode) != 0)
		printf("ioctl error\n");
	else
		printf("value: 0x%x\n",mode.value);
	
	return ret;
}

int run_pmistat(char * argv)
{
	int fd;
	int ret;
	ioctl_arg mode;
	int rdata;

	printf( "Entering run_pmistat()...\n" );

	fd = open_pmi_device();
	
	ret = read(fd, &pcinfo, sizeof(pcinfo));
	printf("Data\t"
		"0x%x 0x%x 0x%x 0x%x 0x%x 0x%x\n",
		pcinfo.PMI_COUNTER0_0,
		pcinfo.PMI_COUNTER0_1,
		pcinfo.PMI_COUNTER0_2,
		pcinfo.PMI_COUNTER0_3,
		pcinfo.PMI_COUNTER0_4,
		pcinfo.PMI_F0_MASK_0);

	/*mode.address = 0x0;
	mode.value = 0xff;
    ioctl(fd, IOCTL_PMI_WRITE, &mode);
	mode.value  = 0x0;
    ioctl(fd, IOCTL_PMI_READ, &mode);
	printf("0x%x\n",mode.address);
	printf("0x%x\n",mode.value);*/

	mode.address = 0x80;
    if (ioctl(fd, IOCTL_PMI_READ, &mode) != 0)
		printf("ioctl error\n");
	else 
		printf("Control: 0x%x\n",mode.value);
		

	mode.address = 0x90;
	if (ioctl(fd, IOCTL_PMI_READ, &mode) != 0)
		printf("ioctl error\n");
	else
		printf("Interrupt: 0x%x\n",mode.value);

	if (strcmp(argv, "all")==0) {
		/* run a test */
		ret = ioctl(fd, IOCTL_PMI_SRAM_TEST);
		if (ret == 9)
			printf("SRAM test passed\n");
		else
			printf("SRAM test failed\n");
		
		ret = ioctl(fd, IOCTL_PMI_SRAM_INTR_TEST);
		if (ret == 0)
			printf("SRAM interrupt test passed\n");
		else
			printf("SRAM interrupt test failed\n");
	} else if (strcmp(argv, "1")==0) {
		/* run a test */
		ret = ioctl(fd, IOCTL_PMI_SRAM_TEST);
		if (ret == 9)
			printf("SRAM test passed\n");
		else
			printf("SRAM test failed\n");		
	} else if (strcmp(argv, "2")==0) {
		/* run a test */
		ret = ioctl(fd, IOCTL_PMI_SRAM_INTR_TEST);
		if (ret == 0)
			printf("SRAM test passed\n");
		else
			printf("SRAM test failed\n");		
	}
	
	return ret;
}

void syntax()
{
    printf("Syntax: %s [-d device_number]\n", Progname);
    printf("Options:\n");
    printf("  -h                 Display this (help) information\n");
    printf("  --help             Display this (help) information\n");
    printf("  -d device_number   Set device number\n");
	printf("  -s g=[0:1] m=[rd:wr:wrnp] [MInitID] [SInitID]\t"
		   "				Set Mask, Pattern and Configuration registers\n");
	printf("SRAM - 0x0     DDR  - 0xD6\n");	
	printf("LN30 - 0x07    LN06 - 0x0C\n");
	printf("LN10 - 0x13    LX20 - 0x0E\n");
	printf("EX70 - 0x0D\n");
	printf("  -c [xx]            Read counter 00:04 to 10:14 value\n");
	printf("  -r [x]             Run SRAM Test cases\n"
		   "0-SRAM counter test\n"
		   "1-SRAM interrupt test\n");
    printf("\n");
}

void parse_args1(int argc, char *argv[])
{
	int i;
	char *str;
	
	//printf("argc: %d\n",argc);
	if (argc >= 2) {
		for (i=1; i<argc; i++) {
			str = argv[i];
			//printf("str %s\n", str);
			if (*str == '-') {
				while (*++str) {
					//printf("option: %c\n",*str);
					if (strcmp(str, "d") == 0) {
						if (i == argc - 1) 
							syntax();
						else {
							i++;
							printf("major device number %d\n", atoi(argv[i]));
							PMISTAT_MAJOR = atoi(argv[i]);
						}
					} else if (strcmp(str, "s") == 0) {
						if (i == argc - 1) 
							syntax();
						else {
							i++;
							set_pmistat(argv, &i);
						}
					} else if (strcmp(str, "c") == 0) {
						if (i == argc - 1) 
							read_pmistat("all");
						else {
							i++;
							read_pmistat(argv[i]);
						}
					} else if (strcmp(str, "r") == 0) {
						if (i == argc - 1)
							run_pmistat("all");
						else {
							i++;
							run_pmistat(argv[i]);
						}
					} else if (strcmp(str, "h") == 0)
						syntax();
					else if (strcmp(str, "-help") == 0)
						syntax();						
				}
			}
		}
	} 
	else if (argc == 1)
		syntax();
}

int main(int argc, char *argv[])
{
	printf( "# %s version %s\n", Progname, Version );
	
	parse_args1(argc, argv);
	
	close(fd);
	
	return 0;
}
