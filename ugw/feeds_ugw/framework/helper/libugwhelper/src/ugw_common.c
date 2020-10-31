 
/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/* ****************************************************************************** 
 *         File Name    : ugw_common.c                                          *
 *         Description  : helper Library , which contains functions used across *
 *         			multiple SL libraries.				*
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
#include <ugw_structs.h>
#include <ugw_defs.h>
#include <ugw_enums.h>
#include <ugw_objmsg.h>
#include "ugw_common.h"

static int nPolldTimeOut = 3;
int32_t enumMapGet(name_value_t enum_nv[], int32_t iSize, char *trVal, int32_t *uiId, int32_t iSel);

/* =============================================================================
 *  Function Name : enumMapGet
 *  Description   : This API based on value of fifth argument does one of,
 *  			a. for given Id in fourth argument find matching entry in
 *  			enum map from first argument and return corresponding
 *  			string value if match found.
 *  			b. for given string value in third argument find matching
 *  			entry in enum map from first argument and return
 *  			corresponding Id value if match found.
 * ============================================================================*/
int32_t enumMapGet(name_value_t enum_nv[], int32_t iSize, char *trVal, int32_t *uiId, int32_t iSel)
{
	int32_t iIndex;

	for(iIndex = 0; iIndex < iSize; iIndex++) {
		switch(iSel) {
			case 1:
				/* find matching uiId for string enum val. */
				if(!strcmp(enum_nv[iIndex].name, trVal)) {
					*uiId = enum_nv[iIndex].value;
					return UGW_SUCCESS;
				}
				break;
			case 2:
				/* find matching string enum val for uiId. */
				if(enum_nv[iIndex].value == *uiId) {
					strcpy(trVal, enum_nv[iIndex].name);
					return UGW_SUCCESS;
				}
				break;
		}
	}
	return UGW_FAILURE;
}

/* =============================================================================
 *  Function Name : help_objlistToStructConv
 *  Description   : This API converts object-parameters from first argument into
 *  			structure specified in second argument.
 *
 *  			Third argument provides following information,
 *  			for a given parameter name <==> offset of corresponding
 *  							parameter in structure.
 * ============================================================================*/
int32_t help_objlistToStructConv(ObjList *pxTmpObj, void *pxStruct, x_LTQ_Param xParamDetail[], int32_t iSize)
{
	int32_t iFlag;
	int32_t iIndex;
	int32_t nEnumVal;
	uint32_t mapOff = 0, xParamMap = 0;
	ParamList *pxParam = NULL;

	if(NULL == pxTmpObj || NULL == pxStruct || NULL == xParamDetail)
		return UGW_FAILURE;

	 /* scan the list to get the xParamMap's offset. */
	for(iIndex = 0;iIndex < iSize;iIndex++)	{
		if(0 == strncmp(PARAM_MAP_NAME,xParamDetail[iIndex].acParamName
					,strlen(PARAM_MAP_NAME))) {
			mapOff = xParamDetail[iIndex].unOffset;
			break;
		}
	}
	if(mapOff == 0) {
		/* xParamMap is to be at end of struct. */
		return UGW_SUCCESS;
	}

	FOR_EACH_PARAM(pxTmpObj, pxParam) {
		/* scan the list to get the required param offset and type info. */
		for(iIndex = 0;iIndex < iSize;iIndex++) {
			iFlag = 0;
			nEnumVal = 0;
			if(0 == strncmp(pxParam->sParamName,xParamDetail[iIndex].acParamName,
					strlen(pxParam->sParamName))) {
				/* if name matches get the type and offset to save the value. */
				switch(xParamDetail[iIndex].uiType) {
					case LTQ_PARAM_TYPE_OBJ_LIST:
					//case LTQ_PARAM_TYPE_OBJ:
						break;

					case LTQ_PARAM_TYPE_CHAR:
					case LTQ_PARAM_TYPE_UCHAR:
						*((char*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) = atoi(pxParam->sParamValue);
						iFlag = 1;
						break;

					case LTQ_PARAM_TYPE_SHORT_INT:
					case LTQ_PARAM_TYPE_USHORT_INT:
						*((unsigned short*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) =
							atoi(pxParam->sParamValue);
						iFlag = 1;
						break;

					case LTQ_PARAM_TYPE_INT:
					case LTQ_PARAM_TYPE_UINT:
						*((int32_t*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) = atoi(pxParam->sParamValue);
						iFlag = 1;
						break;

					case LTQ_PARAM_TYPE_ENUM:
						if(enumMapGet(xParamDetail[iIndex].nvLookUp, xParamDetail[iIndex].unLookUpSize,
							pxParam->sParamValue, &nEnumVal, 1) != UGW_SUCCESS) {
							LOGF_LOG_ERROR("Failed to identify corresponding int value for enum str [%s]\n", pxParam->sParamValue);
							return UGW_FAILURE;
						}
						*((int32_t*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) = nEnumVal;
						break;

					case LTQ_PARAM_TYPE_ULONG:
					case LTQ_PARAM_TYPE_LONG:
						*((long*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) =
							atol(pxParam->sParamValue);
						iFlag = 1;
						break;

					case LTQ_PARAM_TYPE_FLOAT:
						*((float*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) =
							atof(pxParam->sParamValue);
						iFlag = 1;
						break;

					case LTQ_PARAM_TYPE_STR:
						strcpy(((char*)(uint32_t)pxStruct + xParamDetail[iIndex].unOffset),pxParam->sParamValue);
						iFlag = 1;
						break;

					case LTQ_PARAM_TYPE_BOOL:
						*((bool*)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)) = (bool)(!strcasecmp(pxParam->sParamValue,"true")?1:0);
						iFlag = 1;
						break;

					default:
						continue;
						break;
				}
			}
			/* if the param is found and processed ,break the loop and check the next param.
			 * since Id is running number in integer value range, the same can't be used
			 * directly to refer to bit in xParamMap. so calculate relative Id for this param
			 * in this object and use.
			 */
			if(iFlag == 1){
				xParamMap |= (xParamDetail[iIndex].uiId - (xParamDetail[0].uiId - 1));
				break;
			}
		}
	}
	/* set parameter bit-map value. */
	*((int32_t *)((uint32_t)pxStruct + mapOff)) = xParamMap;

	return UGW_SUCCESS;
}

/* =============================================================================
 *  Function Name : help_objlistToStructConv
 *  Description   : This API converts structure specified in second argument into
 *  			object-parameters from first argument.
 *
 *  			Third argument provides following information,
 *  			for a given parameter name <==> offset of corresponding
 *  							parameter in structure.
 * ============================================================================*/
int32_t help_structToObjlistConv(ObjList *pxTmpObj, void *pxStruct, x_LTQ_Param xParamDetail[], int32_t iSize)
{
	int32_t iIndex;
	int32_t nEnumVal;
	long iParamBit = 0;
	uint32_t mapOff = 0; //, xParamMap = 0;
	char sParamValue[MAX_LEN_PARAM_VALUE] = {0};
	bool bTmp = 0;

	if(NULL == pxTmpObj || NULL == pxStruct || NULL == xParamDetail)
		return UGW_FAILURE;

	/* scan the list to get the required param offset and type info. */
	for(iIndex = 0;iIndex < iSize;iIndex++) {
		if(0 == strncmp(PARAM_MAP_NAME,xParamDetail[iIndex].acParamName,
				strlen(PARAM_MAP_NAME))) {
				mapOff = xParamDetail[iIndex].unOffset;
				break;
		}
	}
	if(mapOff == 0) {
		/* xParamMap is to be at end of struct. */
		return UGW_FAILURE;
	}

	/* scan the list to get the required param offset and type info. */
		for(iIndex = 0;iIndex < iSize;iIndex++) {
			nEnumVal = 0;
			/* since Id is running number in integer value range, the same can't be used
			 * directly to refer to bit in xParamMap. so calculate relative Id for this param
			 * in this object and use.
			 */
			iParamBit = (1 << (xParamDetail[iIndex].uiId - (xParamDetail[0].uiId - 1)));
			memset(sParamValue, 0, sizeof(sParamValue));
			if(*((uint32_t *)((uint32_t)pxStruct + mapOff)) & iParamBit) {
				switch(xParamDetail[iIndex].uiType) {
					case LTQ_PARAM_TYPE_OBJ_LIST:
					//case LTQ_PARAM_TYPE_OBJ:
						break;

					case LTQ_PARAM_TYPE_CHAR:
					case LTQ_PARAM_TYPE_UCHAR:
						sprintf(sParamValue, "%c", *((char *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_SHORT_INT:
						sprintf(sParamValue, "%hu", *((short *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_USHORT_INT:
						sprintf(sParamValue, "%hd", *((unsigned short *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_INT:
						sprintf(sParamValue, "%d", (int)*((int32_t *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_ENUM:
						nEnumVal = *((int32_t *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset));
						if(enumMapGet(xParamDetail[iIndex].nvLookUp, xParamDetail[iIndex].unLookUpSize,
							sParamValue, &nEnumVal, 2) != UGW_SUCCESS) {
							LOGF_LOG_ERROR("Failed to identify corresponding string value for enum int [%d]\n", nEnumVal);
							return UGW_FAILURE;
						}
						break;

					case LTQ_PARAM_TYPE_UINT:
						sprintf(sParamValue, "%u", *((uint32_t *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_ULONG:
						sprintf(sParamValue, "%lu", *((unsigned long *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_LONG:
						sprintf(sParamValue, "%ld", *((long *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_FLOAT:
						sprintf(sParamValue, "%f", *((float *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset)));
						break;

					case LTQ_PARAM_TYPE_STR:
						sprintf(sParamValue, "%s", (char *)(uint32_t)pxStruct + xParamDetail[iIndex].unOffset);
						break;

					case LTQ_PARAM_TYPE_BOOL:
						bTmp = *((bool *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset));
						sprintf(sParamValue, "%d", bTmp);
						break;

					default:
						continue;
						break;
				}
				HELP_PARAM_SET(pxTmpObj, xParamDetail[iIndex].acParamName, sParamValue, SOPT_OBJVALUE);
			}
		}
	return UGW_SUCCESS;
}
/* =============================================================================
 *  Function Name : help_structToObjlistMapping
 *  Description   : This API updates each object-parameters Value of the first 
 *		 	argument with corresponding structure params value specified 
 *			in second argument.
 *
 *                      Third argument provides following information,
 *                      for a given parameter name <==> offset of corresponding
 *                                                      parameter in structure.
 * ============================================================================*/
int32_t help_structToObjlistMapping(ObjList *pxTmpObj, void *pxStruct, x_LTQ_Param xParamDetail[])
{
        int32_t iIndex;
        ParamList *pxParam = NULL;
        bool bTmp;

        if(NULL == pxTmpObj || NULL == pxStruct || NULL == xParamDetail)
                return UGW_FAILURE;

          FOR_EACH_PARAM(pxTmpObj, pxParam) {
                        iIndex = GET_PARAM_ID(pxParam) - xParamDetail[0].uiId;
                        switch(xParamDetail[iIndex].uiType) {
                                case LTQ_PARAM_TYPE_OBJ_LIST:
                                //case LTQ_PARAM_TYPE_OBJ:
                                        break;

                                case LTQ_PARAM_TYPE_CHAR:
                                case LTQ_PARAM_TYPE_UCHAR:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%c", *((char *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_SHORT_INT:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%hu", *((short *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_USHORT_INT:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%hd", *((unsigned short *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_INT:
                                case LTQ_PARAM_TYPE_ENUM:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%d", (int)*((int32_t *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_UINT:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%u", *((uint32_t *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_ULONG:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%lu", *((unsigned long *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_LONG:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%ld", *((long *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_FLOAT:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%f", *((float *)((uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset)));
                                        break;

                                case LTQ_PARAM_TYPE_STR:
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%s", (char *)(uint32_t)pxStruct
                                                                        + xParamDetail[iIndex].unOffset);
                                        break;

                                case LTQ_PARAM_TYPE_BOOL:
                                        bTmp = *((bool *)((uint32_t)pxStruct + xParamDetail[iIndex].unOffset));
                                        snprintf(pxParam->sParamValue,MAX_LEN_PARAM_VALUE, "%d", bTmp);
                                        break;

                                default:
                                        continue;
                                        break;
                        }
        }
        return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name  : help_delTimerList                                         *
 *   Description    : Function used to free the timer linked list               *
 *  ============================================================================*/
void help_delTimerList(IN PolldTimeInfo **pxTimerList)
{
	PolldTimeInfo *pxTimerTmp = NULL, *pNxt = NULL;

	list_for_each_entry_safe(pxTimerTmp, pNxt, &((*pxTimerList)->xPlist), xPlist)
	{
		DELETE_TIMER_NODE(pxTimerTmp);
	}

	/*Delete head node of timer list*/
	DELETE_TIMER_NODE((*pxTimerList));
	*pxTimerList = NULL;
}

/*  =============================================================================
 *   Function Name  : help_fillTableValuePolld                                  *
 *   Description    : Function to construct objlist                             *
 *  ============================================================================*/
static int help_fillTableValuePolld(IN struct blob_attr *head, IN unsigned int len, OUT MsgHeader *pxObjList, IN const char *pcObjName, OUT PolldTimeInfo *pxTimerInfo)
{
	char sObjName[MAX_LEN_OBJNAME] = {0};
	uint16_t unOid = 0, unSid = 0, unParamId = 0;
	char sParamName[MAX_LEN_PARAM_NAME] = {0}, sParamValue[MAX_LEN_PARAM_VALUE] = {0};
	uint32_t unParamFlag = 0;
	uint32_t unObjOper = 0, unObjFlag = 0;
	struct blob_attr *attr = NULL;
	struct blobmsg_hdr *hdr = NULL;
	int nType = 0;
	static int nObjParamCount = 0;
	static int nMsgHeaderCount = 0;
	ObjList *pxObj = NULL;
	char *pcTmp = NULL;

	uint32_t unRequest_type = 0;
	uint32_t unOccurrence = 0;
	uint32_t unTimeOut = 0;
	static int nTimerCount = 0;
	PolldTimeInfo xpolldTimer;
	
	memset(&xpolldTimer, 0, sizeof(PolldTimeInfo));
	__blob_for_each_attr(attr, head, len)
	{
		hdr = blob_data(attr);
		nType = blobmsg_type(attr);
		switch(nType)
		{
			case BLOBMSG_TYPE_TABLE:
				if(strcmp((char *)hdr->name,"Mgh") == 0)
				{
					/* Reset required to parse new msg */
					nMsgHeaderCount=1;
					nObjParamCount=0;
					help_fillTableValuePolld(blobmsg_data(attr), blobmsg_data_len(attr),pxObjList,NULL, pxTimerInfo);	
				}
				else
				{
					/* Reset ObjList */
					nObjParamCount=0;
					help_fillTableValuePolld(blobmsg_data(attr), blobmsg_data_len(attr),pxObjList,(char *)hdr->name, pxTimerInfo);
				}
				break;
			case BLOBMSG_TYPE_STRING:
				pcTmp = blobmsg_get_string(attr);
				if(nObjParamCount == 4)
				{
					memset(&sParamName[0], 0, sizeof(sParamName));
					if(pcTmp != NULL)
						strncpy(sParamName, pcTmp, MAX_LEN_PARAM_NAME);
					/* In strncpy, if there is no null byte among the first N bytes of src, the result will not be null-terminated */
					sParamName[MAX_LEN_PARAM_NAME-1]='\0';
					nObjParamCount++;
				}
				if(nObjParamCount == 6)
				{
					memset(&sParamValue[0], 0, sizeof(sParamValue));
					if(pcTmp != NULL)
						strncpy(sParamValue,pcTmp,MAX_LEN_PARAM_VALUE);
					/* In strncpy, if there is no null byte among the first N bytes of src, the result will not be null-terminated */
					sParamValue[MAX_LEN_PARAM_VALUE-1]='\0';
					nObjParamCount++;
				}
				break;
			case BLOBMSG_TYPE_INT8:
				LOGF_LOG_INFO("%d\n", blobmsg_get_u8(attr));
				break;
			case BLOBMSG_TYPE_INT16:
				if (nObjParamCount == 0)
				{
					unSid = blobmsg_get_u16(attr);
					nObjParamCount++;
				}
				else if(nObjParamCount == 1)
				{
					unOid = blobmsg_get_u16(attr);
					nObjParamCount++;
				}
				else if(nObjParamCount == 5)
				{
					unParamId = blobmsg_get_u16(attr);
					nObjParamCount++;
				}
				break;
			case BLOBMSG_TYPE_INT32:
				if (nObjParamCount == 0)
				{
					if (nMsgHeaderCount == 1)
					{
						pxObjList->unMainOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 2)	
					{
						pxObjList->unSubOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
 					}
					else if (nMsgHeaderCount == 3)
					{
						pxObjList->unOwner = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 4)
					{
						/*reserved flag not used as of now */
						pxObjList->nFlag = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					} 
				}
				if (nObjParamCount == 2)
				{
 					unObjOper = blobmsg_get_u32(attr);
					nObjParamCount++;	
				}
				else if (nObjParamCount == 3)
				{
					unObjFlag = blobmsg_get_u32(attr);
					strncpy(sObjName,pcObjName,MAX_LEN_OBJNAME);
					/* In strncpy, if there is no null byte among the first N bytes of src, the result will not be null-terminated */
					sObjName[MAX_LEN_OBJNAME-1]='\0';
					//LOGF_LOG_INFO("ObjList %s:%d:%d:%d:%d\n",sObjName,unSid,unOid,unObjOper,unObjFlag);
					pxObj = help_addObjList(pxObjList->pObjType, sObjName, unSid, unOid, unObjOper, unObjFlag);
					nTimerCount = 4;
					nObjParamCount++;
				}
				else if (nObjParamCount == 7)
				{
					unParamFlag = blobmsg_get_u32(attr);
					// LOGF_LOG_INFO("ParamList  %s:%d:%s:%d\n",sParamName,unParamId,sParamValue,unParamFlag);
					help_addParamList(pxObj,sParamName,unParamId,sParamValue,unParamFlag);
 					/* Reset ParamList */
					nObjParamCount = 4;
					nTimerCount = 8;
				}
				else if(nTimerCount == 4)
				{
					/* Object level timer parsing */
					if(strcmp((char *)hdr->name, "rt") == 0) 
					{
						unRequest_type = blobmsg_get_u32(attr);
						nTimerCount ++;
					}
				}
				else if(nTimerCount == 5) 
				{
					if(strcmp((char *)hdr->name, "oc") == 0) 
					{
						unOccurrence = blobmsg_get_u32(attr);
						nTimerCount ++;
					}
				}
				else if(nTimerCount == 6) 
				{
					if(strcmp((char *)hdr->name, "to") == 0) 
					{
						unTimeOut = blobmsg_get_u32(attr);
						nTimerCount = 4;
						//LOGF_LOG_DEBUG("unRequest_type unOccurrence unTimeOut %d %d %d \n", unRequest_type, unOccurrence, unTimeOut);
						xpolldTimer.unRequest_type = unRequest_type;
						xpolldTimer.unOccurrence = unOccurrence;
						xpolldTimer.unTimeOut = unTimeOut;
						help_addTimerList(pxTimerInfo, &xpolldTimer);
					}
				}
				else if (nTimerCount == 8)
				{
					if(strcmp((char *)hdr->name, "rt") == 0)
					{
						unRequest_type = blobmsg_get_u32(attr);
						nTimerCount ++;
					}
				}
				else if(nTimerCount == 9)
				{
					unOccurrence = blobmsg_get_u32(attr);
					nTimerCount ++;
				}
				else if(nTimerCount == 10)
				{
					unTimeOut = blobmsg_get_u32(attr);
					/* Adding a new node to the timer list */
					xpolldTimer.unRequest_type = unRequest_type;
					xpolldTimer.unOccurrence = unOccurrence;
					xpolldTimer.unTimeOut = unTimeOut;
					nTimerCount = 8;
					help_addTimerList(pxTimerInfo, &xpolldTimer);
		                       //bzero(&xpolldTimerInfo, sizeof(xpolldTimerInfo));
				}
				break;
			case BLOBMSG_TYPE_INT64:
				LOGF_LOG_INFO("%"PRIu64"\n", blobmsg_get_u64(attr));
				break;
		}
	}
	return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name  : help_blobToObjListPolld                                   *
 *   Description    : Function to extract the blob msg and fill the caller      *
 *                    struct.                                                   *
 *  ============================================================================*/
int help_blobToObjListPolld(IN struct blob_attr *msg,OUT MsgHeader *pxMsg, OUT PolldTimeInfo *pxTimerInfo)
{
	int nRet = UGW_SUCCESS;

	if (IS_OBJLIST(pxMsg->unSubOper))
	{        
		nRet = help_fillTableValuePolld(blobmsg_data(msg), blobmsg_data_len(msg),pxMsg,NULL, pxTimerInfo);
		if(nRet != UGW_SUCCESS) 
		{
                	LOGF_LOG_ERROR("IPC MSG CREATION FAILED !!\n");
		}
	}
	else
	{
		/* when msg empty */
		LOGF_LOG_INFO("Message header created with wrong sub operation, ideally should not reach here...\n");
	}
	return nRet;
}

/*  =============================================================================
 *   Function Name  : polld_getValueCb                                          *
 *   Description    : Callback function of ubus_invoke api                      *
 *  ============================================================================*/
static void polld_getValueCb(IN struct ubus_request *pxResp, UNUSED_ARG IN int nType, IN struct blob_attr *pxMsg)
{
	LOGF_LOG_DEBUG("GET Response Received from Polld\n");
	if(pxMsg != NULL)
	{
		pxResp->priv = NULL;
	}
}

/*  =============================================================================
 *   Function Name  : ubusMsgToPolld	                 			*
 *   Description    : Function used to send ubus message to polld 		*
 *  ============================================================================*/
int ubusMsgToPolld(IN MainOper eOperation, IN MsgHeader *pxDynMsg, UNUSED_ARG OUT MsgHeader *pxOutputMsg, IN PolldTimeInfo *pxTimerList)
{
	unsigned int unId = 0;
	static struct blob_buf xBuf;
	static struct ubus_context *pxPolldCtx = NULL;
	PolldRespCode xRespCode;
	int nRet = UGW_SUCCESS;

	memset(&xBuf, 0x0 , sizeof(struct blob_buf));
	memset(&xRespCode, 0x0, sizeof(PolldRespCode));
	/* pack the name:id:value structure to blob msg */
	if (help_objListToBlobPolld(&xBuf, pxDynMsg, NO_ARG_VALUE, NO_ARG_VALUE, pxTimerList) != UGW_SUCCESS )
	{
                LOGF_LOG_ERROR("IPC MSG CREATION FAILED !!\n");
		return UGW_FAILURE;
	}

	/* call ubus api to send the msg.
	   In the callback response handler, write code to unpack the received msg.
	*/
	pxPolldCtx = help_ubusConnect();
	if (pxPolldCtx == NULL)
	{
		LOGF_LOG_CRITICAL("Failed to create UBUS context for Polld %d \n", ERR_RECEIVER_NOT_RUNNING);
		return UGW_FAILURE;
	}

	/*Specifying polld as lookup ID*/
    	if (ubus_lookup_id(pxPolldCtx, "polld", &unId)) 
	{
		LOGF_LOG_CRITICAL("UBUS_LOOPUP_ID Failed");
		return UGW_FAILURE;
	}

	LOGF_LOG_DEBUG("Sending Message to Polld...\n");

	if (eOperation == MOPT_GET || eOperation == MOPT_TIMERACTION)
	{
		nRet = ubus_invoke(pxPolldCtx, unId, "get", xBuf.head, polld_getValueCb, (void *)&xRespCode, nPolldTimeOut * 1000);
		if(nRet != UGW_SUCCESS)
		{
			LOGF_LOG_ERROR("Failed to send blob message ubus_invoke return value :%d \n", nRet);
			xRespCode.nRespCode = UGW_FAILURE;
		}
	}
	else {
		LOGF_LOG_ERROR("Only MOPT_GET/MOPT_TIMERACTION supported %d not supported\n", eOperation);
	}
	blob_buf_free(&xBuf);

	help_ubusDisconnect(pxPolldCtx);
	return nRet;
}

/*  =============================================================================
 *   Function Name  : help_objListToBlobPolld                 			*
 *   Description    : Function used to convert ObjList to blob format including *
 *		      Timer List						*
 *  ============================================================================*/
int help_objListToBlobPolld(OUT struct blob_buf *buf, IN MsgHeader *pxMsg, IN struct ubus_context *ctx, IN struct ubus_request_data *req, IN PolldTimeInfo *pxTimerList)
{
	int nRet = UGW_SUCCESS;
	void *ptbl;

	int nParamCount = 0;
	int nObjCount = 0;
	int nTimeCount = 0;
	PolldTimeInfo *pxTimerTmp = NULL;

	blobmsg_buf_init(buf);
	blobmsg_add_u32(buf,"objflag",pxMsg->unSubOper);

	ptbl = blobmsg_open_table(buf,"Mgh");
	blobmsg_add_u32(buf,"mo",pxMsg->unMainOper);
	blobmsg_add_u32(buf,"so",pxMsg->unSubOper);
	blobmsg_add_u32(buf,"ow",pxMsg->unOwner);
	blobmsg_add_u32(buf,"rf",pxMsg->nFlag);
	blobmsg_close_table(buf, ptbl);

	if (IS_OBJLIST(pxMsg->unSubOper))
	{
		ObjList *pxTmpObj,*pxObjList;
		ParamList *pxParam;

		pxObjList = pxMsg->pObjType;

		list_for_each_entry(pxTmpObj,&(pxObjList->xOlist),xOlist) 
		{
			if (buf->buflen >= UBUS_MSG_LENGTH)
			{
				if ( (ctx != NO_ARG_VALUE) && (req != NO_ARG_VALUE))
				{
					ubus_send_reply(ctx, req, buf->head);
					if (buf->buf)
					{
						free(buf->buf);
					}
					memset(buf,0x0,sizeof(struct blob_buf));
					blobmsg_buf_init(buf);
					blobmsg_add_u32(buf, "objflag", pxMsg->unSubOper);
					ptbl = blobmsg_open_table(buf, "Mgh");
					blobmsg_add_u32(buf, "mo", pxMsg->unMainOper);
					blobmsg_add_u32(buf, "so", pxMsg->unSubOper);
					blobmsg_add_u32(buf, "ow", pxMsg->unOwner);
					blobmsg_add_u32(buf, "rf", pxMsg->nFlag);
					blobmsg_close_table(buf, ptbl);
				}
				else
				{
					fprintf(stderr, "Your Msg May Not Be Sent Due To Overload \n");
				}
			}
			ptbl = blobmsg_open_table(buf, pxTmpObj->sObjName);
			blobmsg_add_u16(buf, "sid", pxTmpObj->unSid); 
			blobmsg_add_u16(buf, "oid", pxTmpObj->unOid);
			blobmsg_add_u32(buf, "oo", pxTmpObj->unObjOper);
			blobmsg_add_u32(buf, "of", pxTmpObj->unObjFlag);
			/* Timer at param level mapping */
			list_for_each_entry(pxParam,&(pxTmpObj->xParamList.xPlist), xPlist)
			{
				blobmsg_add_string(buf, "pn", pxParam->sParamName);
				blobmsg_add_u16(buf, "pi", pxParam->unParamId);
				blobmsg_add_string(buf, "pv", pxParam->sParamValue);
				blobmsg_add_u32(buf, "pf", pxParam->unParamFlag);	
				nTimeCount = 0;
				FOR_EACH_TIMER_ONLY(pxTimerList, pxTimerTmp) 
				{
					if(nParamCount == nTimeCount)
					{
						blobmsg_add_u32(buf, "rt", pxTimerTmp->unRequest_type);
						blobmsg_add_u32(buf, "oc", pxTimerTmp->unOccurrence);
						blobmsg_add_u32(buf, "to", pxTimerTmp->unTimeOut);
						//help_printTimerList(pxTimerTmp);
						break;
					}
					nTimeCount++;
				}
				nObjCount++;
				++nParamCount;
			}
			/* Timer at object level mapping */
			if(list_empty(&(pxTmpObj->xParamList.xPlist))) 	
			{
				LOGF_LOG_DEBUG("ParamList is NULL for requested object: %s \n", pxTmpObj->sObjName);
				nTimeCount = 0;
				FOR_EACH_TIMER_ONLY(pxTimerList, pxTimerTmp) 
				{
					if(nObjCount == nTimeCount) 
					{
						blobmsg_add_u32(buf, "rt", pxTimerTmp->unRequest_type);
						blobmsg_add_u32(buf, "oc", pxTimerTmp->unOccurrence);
						blobmsg_add_u32(buf, "to", pxTimerTmp->unTimeOut);
					}
					nTimeCount++;
				}
				nParamCount++;
				++nObjCount;
			}
			blobmsg_close_table(buf, ptbl);
		}
	}
	/* Print blob message contains message header and timer list in json format */
	//fprintf(stderr, "json: %s\n", blobmsg_format_json_indent(buf->head, true, 0));
	//HELP_PRINT_MSG(pxMsg);
	return nRet;
}

/*  =============================================================================
 *   Function Name  : help_createTimerList                 			*
 *   Description    : Function used to create timer linked-list    		*
 *  ============================================================================*/
PolldTimeInfo *help_createTimerList()
{
	PolldTimeInfo *pxTmp = NULL;
	pxTmp = HELP_MALLOC(sizeof(PolldTimeInfo));
	if(!pxTmp)
	{
		LOGF_LOG_CRITICAL("Malloc failed in creating polld head timer node %d \n", ENOMEM);
		goto End;
	}
	INIT_LIST_HEAD(&pxTmp->xPlist);
 End:
	return pxTmp;
}

/*  =============================================================================
 *   Function Name  : help_addTimerList                 			*
 *   Description    : Function used to add new Timer node at end   		*
 *  ============================================================================*/
int help_addTimerList(OUT PolldTimeInfo *pxTimerList, IN PolldTimeInfo *pxTimeInfo)
{
	uint32_t nCount = 0;
	int nRet = UGW_SUCCESS;
	PolldTimeInfo *pxTmp = NULL;

	/* For backward compatible, they will pass unReferenceCount = 0 */
	pxTimeInfo->unReferenceCount = (pxTimeInfo->unReferenceCount == 0) ? 1 : pxTimeInfo->unReferenceCount;

	LOGF_LOG_DEBUG("pxTimeInfo [unRequest_type: %u unOccurrence: %u unTimeOut: %u nReferenceCount: %u\n",
		pxTimeInfo->unRequest_type, pxTimeInfo->unOccurrence, pxTimeInfo->unTimeOut, pxTimeInfo->unReferenceCount);

	for (nCount=0 ; nCount < pxTimeInfo->unReferenceCount ; nCount++) {
		pxTmp = HELP_MALLOC(sizeof(PolldTimeInfo));
		if(!pxTmp)
		{
			LOGF_LOG_CRITICAL("Malloc failed for new timer node of polld %d \n", ENOMEM);
			nRet = UGW_FAILURE;
			goto End;
		}

		pxTmp->unRequest_type = pxTimeInfo->unRequest_type;
		pxTmp->unOccurrence = pxTimeInfo->unOccurrence;
		pxTmp->unTimeOut = pxTimeInfo->unTimeOut;

		list_add_tail(&(pxTmp->xPlist), &(pxTimerList->xPlist));
	}
 End:
	return nRet;
}

/*  =============================================================================
 *   Function Name  : help_printTimerList                 			*
 *   Description    : Function used to print timer List  			*
 *  ============================================================================*/
void help_printTimerList(IN PolldTimeInfo *pxTimerList)
{
	PolldTimeInfo *pxTimerTmp = NULL;
	FOR_EACH_TIMER_ONLY(pxTimerList ,pxTimerTmp)
	{
		fprintf(stderr, "[");
		fprintf(stderr,"Request_type: %u ", pxTimerTmp->unRequest_type);
		fprintf(stderr,"Occurance: %u ", pxTimerTmp->unOccurrence);
		fprintf(stderr,"Timeout: %u", pxTimerTmp->unTimeOut);
		fprintf(stderr, "]\n");
	}
}

/*!  \brief  This function sends the ubus message to Ubus registered daemons
  \param[IN] pxBlob Pointer to the blob message
  \param[IN] psUbusObject Name of the ubus registered daemon
  \param[IN] psUbusMethod Name of the method to be handled in the ubus registered daemon(psUbusObject)
  \return UGW_SUCCESS/UGW_FAILURE
 */
int32_t help_sendUbusMessage (IN struct blob_buf *pxBlob,
			       IN char *psUbusObject, IN char *psUbusMethod)
{
	uint32_t unId;
	int32_t nTimeOut = 30;
	static struct ubus_context *pxUbusCtx = NULL;
	RespCode xRespCode;
	int32_t nRet = UGW_SUCCESS;

	/* call ubus api to send the msg. 
	   In the callback response handler, write code to unpack the received msg.
	 */
	pxUbusCtx = help_ubusConnect();
	if (pxUbusCtx == NULL) {
		LOGF_LOG_CRITICAL("Failed to create UBUS context for Devm\n");
		nRet = UGW_FAILURE;
		goto end;
	}

	/* Get the Ubus ID for the Ubus Objects*/
	LOGF_LOG_DEBUG("Looking for ubus id for %s\n", psUbusObject);
	if ((nRet = ubus_lookup_id(pxUbusCtx, psUbusObject, &unId)) != UGW_SUCCESS) {
		LOGF_LOG_CRITICAL("UBUS_LOOPUP_ID Failed");
		goto end;
	} 

	/* Send a message to ubus*/
	LOGF_LOG_INFO("Sending Message to %s...\n", psUbusObject);
	if ((nRet = ubus_invoke(pxUbusCtx, unId, psUbusMethod, pxBlob->head, NULL,
	     (void *)&xRespCode, nTimeOut * 1000)) != UGW_SUCCESS) {
		LOGF_LOG_ERROR("Error : ubus_invoke failed with RespCode: sName = %s RespCode = %d for ID:%d and Method:%s..\n", 
					xRespCode.sName, xRespCode.RespCode, unId, psUbusMethod);
		LOGF_LOG_ERROR("Error : ubus_invoke failed with error code %d\n", nRet);
	}


end:
	if(pxUbusCtx != NULL) {
	help_ubusDisconnect(pxUbusCtx);
	}

	return nRet;

}

/*!  \brief  This function used to construct the param list using the blob message
  \param[IN] pxBlob Pointer to the blob message object
  \param[OUT] pxParamList ParamList used to construct blob message
  \return UGW_SUCCESS/UGW_FAILURE
 */
int32_t help_constructParamListFromBlob(IN struct blob_attr *pxBlob,
				  OUT ParamList *pxParam)
{
        struct blob_attr *attr;
        struct blob_attr *head;
        int nType;
        unsigned int len=0;
	char sVal[MAX_LEN_PARAM_VALUE] = { '\0' };

        head = blobmsg_data(pxBlob);
        len = blobmsg_data_len(pxBlob);

        __blob_for_each_attr(attr, head, len) {
                nType = blobmsg_type(attr);
                switch(nType)
                {
                        case BLOBMSG_TYPE_INT32:
				snprintf(sVal, MAX_LEN_PARAM_VALUE, "%d", blobmsg_get_u32(attr));
				HELP_ONLY_PARAM_SET_WITH_TYPE (pxParam, blobmsg_name (attr), sVal, PARAM_TYPE_INT32);
                                break;
                        case BLOBMSG_TYPE_STRING:
				HELP_ONLY_PARAM_SET_WITH_TYPE (pxParam, blobmsg_name (attr), blobmsg_get_string(attr), PARAM_TYPE_STRING);
                                break;
                }
        }

        return 0;

}
/*!  \brief  This function used to construct the blob message using the param list
  \param[OUT] pxBlob Pointer to the blob message object to beconstructed
  \param[IN] pxParamList ParamList used to construct blob message
  \return UGW_SUCCESS/UGW_FAILURE
 */
int32_t help_constructBlobMessageFromParamList(OUT struct blob_buf * pxBlob,
				  IN ParamList * pxParamList)
{
	ParamList *pxParam = NULL;
	int32_t nRet = UGW_SUCCESS, nBlobRet = UGW_SUCCESS;

	LOGF_LOG_INFO("Entered help_constructBlobMessageFromParamList\n");

	/*Initialize the pxBlob */
	if (blob_buf_init(pxBlob, 0) != UGW_SUCCESS) {
		LOGF_LOG_ERROR("blob message initialization has failed\n");
		nRet = UGW_FAILURE;
		goto end;
	}

	/* Construct the pxBlob from pxParamList */
	FOR_EACH_PARAM_ONLY(pxParamList, pxParam) {
		if (nBlobRet != UGW_SUCCESS) {
			LOGF_LOG_ERROR
			    ("Adding string to blob message has failed\n");
			nRet = UGW_FAILURE;
			goto end;
		}
		nBlobRet = UGW_SUCCESS;

		switch(pxParam->unParamFlag) {
		case PARAM_TYPE_INT32:
		    blobmsg_add_u32 (pxBlob, pxParam->sParamName, atoi(pxParam->sParamValue));
			break;
		case PARAM_TYPE_STRING:
		nBlobRet |=
		    blobmsg_add_string (pxBlob, pxParam->sParamName, pxParam->sParamValue);
			break;
		default:
			nRet = UGW_FAILURE;
			goto end;
		}
	}
end:
	LOGF_LOG_INFO("help_constructBlobMessageFromParamList returned with %d \n", nRet);
	return nRet;
}

/*!  \brief  This function used to convert wsd data to blob message
  \param[OUT] buf Pointer to the blob buffer
  \param[IN] pxWsdData WsdData structure pointer used to construct blob message
  \return UGW_SUCCESS/UGW_FAILURE
 */
int32_t help_WsdDataToBlob(struct blob_buf *buf, WsdData *pxWsdData)
{
        int nRet = UGW_SUCCESS;

        memset(buf, 0x0, sizeof(struct blob_buf));
        blobmsg_buf_init(buf);
        nRet = help_objListToBlob(buf, &pxWsdData->xMsgHdr, NO_ARG_VALUE, NO_ARG_VALUE);
        if (nRet == UGW_FAILURE) {
                LOGF_LOG_ERROR("IPC MSG CREATION FAILED !!\n");
                goto returnHandler;
        }

        blobmsg_add_u32(buf, "wsprotoid", pxWsdData->nWSProto);
        blobmsg_add_u8(buf, "action", pxWsdData->eWsdAction);
        blobmsg_add_u32(buf, "timer", pxWsdData->nTimer);
        blobmsg_add_u32(buf, "instance", pxWsdData->nInstance);

        nRet = UGW_SUCCESS;
  returnHandler:
        return nRet;
}

/*!  \brief  This function used to convert blob message to wsd data 
  \param[IN] msg blob attr pointer used to construct blob message to Wsd data
  \param[OUT] pxWsdData Pointer to the WsdData structure
  \return UGW_SUCCESS/UGW_FAILURE
 */
int32_t help_blobToWsdData(struct blob_attr *msg, WsdData *pxWsdData)
{

        struct blob_attr *attr;
        int nType = 0, nRet = UGW_SUCCESS;
        struct blobmsg_hdr *hdr;
        struct blob_attr *head = blobmsg_data(msg);
        unsigned int len = blobmsg_data_len(msg);

        HELP_CREATE_MSG(&pxWsdData->xMsgHdr, MOPT_GET, SOPT_OBJVALUE, OWN_SERVD, 1);
        nRet = help_blobToObjList(msg, &pxWsdData->xMsgHdr);
        if (nRet == UGW_FAILURE) {
                LOGF_LOG_ERROR("IPC MSG CREATION FAILED !!\n");
                HELP_DELETE_MSG(&pxWsdData->xMsgHdr);
                goto returnHandler;
        }

        __blob_for_each_attr(attr, head, len)
        {
                hdr = blob_data(attr);
                nType = blobmsg_type(attr);
                switch(nType)
                {
                        case BLOBMSG_TYPE_INT32:
                                if(strcmp((char *)hdr->name,"wsprotoid") == 0) {
                                        pxWsdData->nWSProto = blobmsg_get_u32(attr);
                                } else if(strcmp((char *)hdr->name,"timer") == 0) {
                                        pxWsdData->nTimer = blobmsg_get_u32(attr);
                                } else if(strcmp((char *)hdr->name,"instance") == 0) {
                                        pxWsdData->nInstance = blobmsg_get_u32(attr);
                                }
                                break;
                        case BLOBMSG_TYPE_INT8:
                                if(strcmp((char *)hdr->name,"action") == 0) {
                                        pxWsdData->eWsdAction = blobmsg_get_u8(attr);
                                }
                                break;
                        default:
                                break;
                }
        }

        nRet = UGW_SUCCESS;
  returnHandler:
        return nRet;
}

/*!     \brief  This function is to send ubus status messages to wsd and web.
        \param[in] unNotifyId Notify id to identify wsd proto
        \param[in] ucType sync/async notification
        \param[in] pcMessage pointer to the messages string
        \return on UGW_SUCCESS/UGW_FAILURE
*/
int32_t help_sendStatusToWeb(uint32_t unNotifyId, unsigned char ucType, char *pcMessage)
{
	NotifyObjData xNotifyData;
	struct blob_buf buf;
	int nRet = UGW_FAILURE;
	void *pParam;

	memset(&buf,0x0,sizeof(struct blob_buf));

	xNotifyData.unNotifyId = unNotifyId;
	xNotifyData.ucType = ucType;

	pParam = HELP_CREATE_PARAM(SOPT_OBJVALUE);
	HELP_ONLY_PARAM_SET(pParam, "message", pcMessage, SOPT_OBJVALUE);

	xNotifyData.pxParamList = pParam;

	nRet = help_notifyListToBlob(&buf, &xNotifyData);
	if(nRet == UGW_FAILURE) {
                LOGF_LOG_ERROR("IPC MSG CREATION FAILED !!\n");
		goto returnHandler;
	}

	nRet = help_sendUbusMessage(&buf, "wsd", "notify.status");
	if(nRet == UGW_FAILURE) {
                LOGF_LOG_ERROR("send Ubus status message to wsd failed");
                goto returnHandler;
        }

  returnHandler:
	HELP_DELETE_PARAM(xNotifyData.pxParamList, SOPT_OBJVALUE);
	blob_buf_free(&buf);
	return nRet;
}

