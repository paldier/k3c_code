/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/* ****************************************************************************** 
 *         File Name    : ugw_common.c                                          *
 *         Description  : Common Library , which contains functions,defines,	*
 *			  structs,enums used across the modules like CAL,CAPI,	*
 *			  CSD,Servd,and Management Entities			*
 * ******************************************************************************/

#include <stdio.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <string.h>
#include <sys/time.h>
#include <libubus.h>
#include <libubox/blobmsg_json.h>
#include <libubox/blobmsg.h>
#include "ugw_proto.h"
#include "ugw_structs.h"
#include "ugw_defs.h"
#include "ugw_enums.h"
#include "ugw_objmsg.h"

void help_testutility(void);
/*  =============================================================================
 *   Function Name 	: help_testutility					*
 *   Description 	: Function to test supported functions 			*
 *  ============================================================================*/
void help_testutility(void)
{

	printf("######## Start : help_malloc function testing ######## \n ");

	char *ptr;
	char *ptr1=NULL;
	ptr = HELP_MALLOC(10);
	if(ptr == NULL)
	{
		printf("malloc failed\n");
		goto finish;
	}

	strncpy(ptr,"TESTING",10);

	printf("##### %s\n",ptr);

	printf("######## End : help_malloc function testing ########\n");

	printf("######## Start : help_realloc function testing ########\n");

	printf("%p\n",ptr);
	ptr = HELP_REALLOC(ptr,20);
	if(ptr == NULL)
		goto finish;

	printf("##### %s\n",ptr);
	
	strncpy((ptr+strlen(ptr)),"-TESTING",20);
	printf("##### %s\n",ptr);

	printf("######## End : help_realloc function testing ########\n");

	printf("######## Start : help_calloc function testing ########\n");
	
	ptr1 = HELP_CALLOC(1,20);
	if(ptr1 == NULL)
		goto finish;

	strncpy(ptr1,"TESTING-TESTING",20);

	printf("##### %s\n",ptr1);

	printf("######## End : help_calloc function testing ########\n");

	printf("######## Start : help_free function testing ########\n");

finish:
	HELP_DUMP_MEMINFO();
	HELP_FREE(ptr);
	HELP_FREE(ptr1);
	HELP_DUMP_MEMINFO();
	printf("######## End : help_free function testing ########\n");
}


int main(void)
{

	printf("Testing Started for HELP LIBRARY functions \n");
	help_testutility();
	printf("Testing Ended for HELP LIBRARY functions \n");
return 1;
}

