
/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/* ****************************************************************************** 
 *         File Name    : ugw_str_wrapper.c                                     *
 *         Description  : helper Library , this file contains wrapper API for   *
 *         		  standard string API's  				*
 * ******************************************************************************/

#include <stdio.h>
#include <string.h>
#include "ugw_defs.h"
#include "ugw_common.h" 

/* =============================================================================
* Function Name : HELP_STRDUP
* Description   : Function to create a string duplicate
* Returns       : 1. On success --> Duplicated string 2. On Failure --> NULL
============================================================================== */

OUT char * HELP_STRDUP(IN char * pcInStr)
{
	char *pxOutStr=NULL;
	pxOutStr = HELP_MALLOC(strlen(pcInStr));
	if (pxOutStr == NULL)
	{
		LOGF_LOG_ERROR("Memory alloc Failed\n");
		return NULL;
	}
	strncpy(pxOutStr, pcInStr , strlen(pcInStr));
	pxOutStr[strlen(pcInStr)] = '\0';
	return pxOutStr;
}

/* =============================================================================
* Function Name : HELP_STRDUP_FREE
* Description   : Function to free the duplicated string
* Returns       : 1. On success --> UGW_SUCCESS 2. On Failure --> -ve value depending on error
============================================================================== */
OUT int32_t  HELP_STRDUP_FREE(IN char * pcInStr)
{
	int32_t nRet = UGW_SUCCESS;
	nRet = HELP_FREE(pcInStr);
	return nRet;
}
