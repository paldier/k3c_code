/*****************************************************************************

  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany 

  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.

 ******************************************************************************/

/***************************************************************************** *
 *     File Name  : cal.c                                                      *
 *     Description: cal - common adaptation layer provides APIs that could be  *
 *                  used for getting or setting values in the target xml file  *
 *                  These APIs are called from various management entities and *
 *                  or service libraries to update the target database.        *
 *                                                                             *
 ******************************************************************************/
#include "cal.h"

#ifndef LOG_LEVEL
uint16_t LOGLEVEL = SYS_LOG_DEBUG + 1;
#else
uint16_t LOGLEVEL = LOG_LEVEL + 1;
#endif

#ifndef LOG_TYPE
uint16_t LOGTYPE = SYS_LOG_TYPE_FILE;
#else
uint16_t LOGTYPE = LOG_TYPE;
#endif

static int ubusMsgToServd(IN MainOper eOperation, IN MsgHeader * pxDynMsg, OUT MsgHeader * pxOutputMsg);
static void cal_getValueCb(struct ubus_request *pxResp, int nType, struct blob_attr *pxMsg);
static void cal_setValueCb(struct ubus_request *pxResp, int nType, struct blob_attr *pxMsg);

/* =============================================================================
 *  Function Name : cal_getValue
 *  Description   : API to get the values corresponding to an object or parameter
 *                  from the DOM Tree
 *                  This API checks if any of the parameters is dyanamic in nature 
 *                  and calls get from SL to update such parameters.
 * ============================================================================*/
int cal_getValue(INOUT MsgHeader * pxMsg)
{
    int nRet = UGW_SUCCESS;
    bool bDynFlag = 0;
    bool bDevice = 0;
    uint32_t unMsgSize = 0;

    ObjList *pxTmpObj = NULL;

    FOR_EACH_OBJ(pxMsg->pObjType, pxTmpObj) {
        if(IS_OWN_TR69(pxMsg->unOwner) && (strncmp(GET_OBJ_NAME(pxTmpObj), "Device.", MAX_LEN_OBJNAME) == 0)
           && (list_empty(&(pxTmpObj->xParamList.xPlist)) == 1)) {
            bDevice = 1;
            break;
        }
    }

    /* When DevM wants to fetch the information regarding the parameters,
    for which changeFlag is set, it does a getValue from DB followed by a getValue.
    In this case, the IDs are already available in the ObjList and value needs
    to fetched only from SLs. So, CSD path is skipped here for optimization */
    if(IS_OWN_TR69(pxMsg->unOwner) && IS_DYNAMIC_SET(pxMsg->unSubOper)) {

        MsgHeader xDynMsgList;
        HELP_CREATE_MSG(&xDynMsgList, pxMsg->unMainOper, pxMsg->unSubOper, pxMsg->unOwner, pxMsg->nFlag);
        nRet = ubusMsgToServd(MOPT_GET, pxMsg, &xDynMsgList);
        HELP_DELETE_OBJ(pxMsg->pObjType, pxMsg->unSubOper, EMPTY_OBJLIST); 
        HELP_COPY_OBJ(pxMsg->pObjType, xDynMsgList.pObjType, SOPT_OBJVALUE, COPY_COMPLETE_OBJ);
        HELP_DELETE_MSG(&xDynMsgList);
        goto end;
    } else {
        nRet = capi_getValue(pxMsg);
    }
    if(nRet != UGW_SUCCESS) {
        LOGF_LOG_ERROR("CAPI GET Failed. Return Value : %d\n", nRet);
    } else {
        if(IS_SOPT_OBJACSATTR(pxMsg->unSubOper) || IS_SOPT_OBJNAME(pxMsg->unSubOper) || IS_SOPT_OBJATTR(pxMsg->unSubOper)) {
            return UGW_SUCCESS;
        }

        if(!(IS_OWN_SERVD(pxMsg->unOwner))) {
            if(bDevice) {
                LOGF_LOG_INFO("Input Object is \"DEVICE.\", Received from TR-069. Its ok to return stale values\n");
                goto end;
            }
            /* If the owner is servD, no need to check if the parameter is dynamic or not */
            MsgHeader xDynMsgList;
            ObjList *pxLoopObj = NULL;

            unMsgSize = pxMsg->unMsgSize;
            HELP_CREATE_MSG(&xDynMsgList, pxMsg->unMainOper, pxMsg->unSubOper, pxMsg->unOwner, pxMsg->nFlag);

            pxLoopObj = HELP_CREATE_OBJ(pxMsg->unSubOper);
            if(pxLoopObj == NULL) {
                LOGF_LOG_CRITICAL("Failed to create ObjectList pointer\n");
                nRet = UGW_FAILURE;
                HELP_DELETE_MSG(&xDynMsgList);
                goto end;
            }

            if(IS_DYNAMIC_SET(pxMsg->unSubOper)) {
                HELP_COPY_OBJ(xDynMsgList.pObjType, pxMsg->pObjType, SOPT_OBJVALUE, COPY_COMPLETE_OBJ);
                HELP_DELETE_OBJ(pxMsg->pObjType, pxMsg->unSubOper, EMPTY_OBJLIST);
                bDynFlag = 1;
            } else {
                HELP_COPY_OBJ(pxLoopObj, pxMsg->pObjType, pxMsg->unSubOper, COPY_COMPLETE_OBJ);
                pxTmpObj = NULL;
                FOR_EACH_OBJ(pxLoopObj, pxTmpObj) {
                    /* If owner is TR69 and the object is "Device." and parameter list is empty, dont fetch live values */
                    if(IS_DYNAMIC_SET(pxTmpObj->unObjFlag)) {
                        LOGF_LOG_DEBUG("Dynamic Flag is set on Object %s\n", GET_OBJ_NAME(pxTmpObj));
                        HELP_MOVEOBJLIST(xDynMsgList.pObjType, pxMsg->pObjType, GET_OBJ_NAME(pxTmpObj), pxMsg->unSubOper);
                        bDynFlag = 1;
                    }
                }
            }

            /* If dynamic objects are present, send it to servd */
            if(bDynFlag) {
                LOGF_LOG_INFO("Dynamic List is Created. Sending to ServD..\n");
                nRet = ubusMsgToServd(MOPT_GET, &xDynMsgList, pxMsg);
		if( nRet == ERR_UBUS_TIME_OUT || nRet == ERR_SL_TIME_OUT )
		{
			/* empty objlist incase timeout failure */
            		HELP_DELETE_OBJ(pxMsg->pObjType,SOPT_OBJVALUE, EMPTY_OBJLIST);
		}
                else if(nRet != UGW_SUCCESS) {
                    LOGF_LOG_ERROR("Get Dynamic Values from SL Failed\n");
                    /* If SL returned error, merge the DynList back to Input List and return */
                    HELP_COPY_OBJ(pxMsg->pObjType, xDynMsgList.pObjType, SOPT_OBJVALUE, COPY_COMPLETE_OBJ);
                }
            } else {
                LOGF_LOG_DEBUG("Dynamic List is Empty\n");
            }
            HELP_DELETE_MSG(&xDynMsgList);
            HELP_DELETE_OBJ(pxLoopObj, pxMsg->unSubOper, FREE_OBJLIST);
        }
        pxMsg->unMsgSize = unMsgSize;
    }
 end:
    LOGF_LOG_DEBUG("CAL GET Return : %d\n", nRet);
    return nRet;
}

/* =============================================================================
 *  Function Name : cal_updatePeriodicValues
 *  Description   : API to get the dynamic values from SL and write them to the DOM Tree
 * ============================================================================*/
int cal_updatePeriodicValues(INOUT MsgHeader * pxMsg)
{
    int nRet = UGW_SUCCESS;
    MsgHeader xOutPutMsg;

    memset(&xOutPutMsg, 0, sizeof(xOutPutMsg));
    LOGF_LOG_DEBUG("Get the Values Required for Periodic Update\n");
    nRet = capi_getValue(pxMsg);

    if(nRet != UGW_SUCCESS) {
        LOGF_LOG_ERROR("CAPI GET Failed. Return Value : %d\n", nRet);
        goto end;
    } else {
        /* Creating the separate MSG to store output/return object(s) */
        HELP_CREATE_MSG(&xOutPutMsg, pxMsg->unMainOper, pxMsg->unSubOper, pxMsg->unOwner, pxMsg->nFlag);

        nRet = ubusMsgToServd(pxMsg->unMainOper, pxMsg, &xOutPutMsg);
        if(nRet != UGW_SUCCESS) {
            LOGF_LOG_ERROR("Get Dynamic Values from SL Failed\n");
            HELP_DELETE_MSG(&xOutPutMsg);
            goto end;
        }
        /* In case of success, I want to return the latest/updated object(s) in existing MSG */
        HELP_DELETE_OBJ(pxMsg->pObjType, pxMsg->unSubOper, EMPTY_OBJLIST);
        HELP_COPY_OBJ(pxMsg->pObjType, xOutPutMsg.pObjType, pxMsg->unSubOper, COPY_COMPLETE_OBJ);
        HELP_DELETE_MSG(&xOutPutMsg);
#ifdef DEBUG
        fprintf(stderr, "=============== Returned List From SL===============\n");
        HELP_PRINT_MSG(pxMsg);
#endif
        LOGF_LOG_DEBUG("CAPI_SET called to SET to DB... \n");
        nRet = capi_setValue(pxMsg);
        if(nRet != UGW_SUCCESS) {
            LOGF_LOG_ERROR("CAPI_SET Failed... RetCode : %d\n", nRet);
        }
    }

 end:
    LOGF_LOG_DEBUG("CAL Update Periodic Values - Return : %d\n", nRet);
    return nRet;
}

/* =============================================================================
 *  Function Name : cal_setValue
 *  Description   : API to set values for objects/parameters in the DOM tree
 *                  It does parameter Validation and ACL checks before updating
 *                  to database
 * ============================================================================*/
int cal_setValue(IN MsgHeader * pxMsg)
{
    int nRet = UGW_SUCCESS;

    LOGF_LOG_DEBUG("SET Request from Owner - %d\n", pxMsg->unOwner);
    if(!(IS_OWN_SERVD(pxMsg->unOwner))) {
        /* Get only IDs updated in the list and dont pollute the rest of the objectList */
        pxMsg->unSubOper |= SOPT_VALIDATION;

        /* Call CAPI get so that, the IDs could be retrived */
        LOGF_LOG_DEBUG("CAPI_GET called for getting IDs and to perform validation on requested object\n");
        nRet = capi_setValue(pxMsg);
        if(nRet != UGW_SUCCESS) {
            LOGF_LOG_ERROR("CAPI_SET Returned Error : %s\n", (nRet == ERR_VALIDATION_FAILED) ? "Validation Failure" : "CAPI Set Failure");
            goto ret;
        }

        if(IS_SOPT_OBJACSATTR(pxMsg->unSubOper) || IS_SOPT_OBJATTR(pxMsg->unSubOper)) {
            return UGW_SUCCESS;
        }

        MsgHeader xOutPutMsg;
        HELP_CREATE_MSG(&xOutPutMsg, pxMsg->unMainOper, pxMsg->unSubOper, pxMsg->unOwner, pxMsg->nFlag);
        /* Send the ObjList to be set to the system to servd */
        LOGF_LOG_INFO("Apply Configuration to System. Sending to ServD\n");
        nRet = ubusMsgToServd(MOPT_SET, pxMsg, &xOutPutMsg);
        if(nRet != UGW_SUCCESS) {
            MsgHeader xAddFailedList;
            ObjList *pxLoopObj = NULL;
            ObjList *pxTmpObj = NULL;
            bool bAddFlag = 0;

            LOGF_LOG_ERROR("Servd returned failure - return code : %d \n Revert added objects(if any)\n", nRet);
            pxLoopObj = HELP_CREATE_OBJ(pxMsg->unSubOper);
            if(pxLoopObj == NULL) {
                LOGF_LOG_CRITICAL("Failed to create ObjectList pointer\n");
                nRet = UGW_FAILURE;
                goto ret;
            }
            HELP_COPY_OBJ(pxLoopObj, pxMsg->pObjType, pxMsg->unSubOper, COPY_COMPLETE_OBJ);

            HELP_CREATE_MSG(&xAddFailedList, pxMsg->unMainOper, pxMsg->unSubOper, pxMsg->unOwner, pxMsg->nFlag);
            xAddFailedList.unOwner = OWN_SERVD;

            /* If the transaction failed, check if there is an add request received. 
               If ADD request is received, the DB is already updated with a new instance. 
               Delete this instance */
            FOR_EACH_OBJ(pxLoopObj, pxTmpObj) {
                if(IS_OBJOPT_ADD(pxTmpObj->unObjOper)) {
                    LOGF_LOG_DEBUG("ADD Flag is set on Object %s\n", GET_OBJ_NAME(pxTmpObj));
                    pxTmpObj->unObjOper = OBJOPT_DEL;
                    HELP_COPY_OBJ(xAddFailedList.pObjType, pxTmpObj, pxMsg->unSubOper, COPY_SINGLE_OBJ);
                    bAddFlag = 1;
                }
            }
            if(bAddFlag) {
                int nRetVal = UGW_SUCCESS;
                if((nRetVal = capi_setValue(&xAddFailedList)) != UGW_SUCCESS) {
                    LOGF_LOG_ERROR("CAPI failed to delete stale entries %d\n", nRetVal);
                }
            }

            /* clean existing content */
            HELP_DELETE_MSG(&xAddFailedList);
            HELP_DELETE_OBJ(pxLoopObj, SOPT_OBJVALUE, FREE_OBJLIST);

            HELP_DELETE_OBJ(pxMsg->pObjType, SOPT_OBJVALUE, EMPTY_OBJLIST);
            HELP_COPY_OBJ(pxMsg->pObjType, xOutPutMsg.pObjType, pxMsg->unSubOper, COPY_COMPLETE_OBJ);
            HELP_DELETE_MSG(&xOutPutMsg);
        } else {
            HELP_DELETE_MSG(&xOutPutMsg);
        }
    } else {
        /* call the API to update the configuration to System Config File */
        LOGF_LOG_DEBUG("CAPI_SET called to SET to DB... \n");
        nRet = capi_setValue(pxMsg);
        if(nRet != UGW_SUCCESS) {
            LOGF_LOG_ERROR("CAPI_SET Failed... RetCode : %d\n", nRet);
        }
    }
 ret:
    LOGF_LOG_DEBUG("CAL_SET Return : %d\n", nRet);

    return nRet;
}

static void cal_setValueCb(struct ubus_request *pxResp, int nType, struct blob_attr *pxMsg)
{
    LOGF_LOG_DEBUG("SET Response Received from Servd\n");
    if(help_blobToObjList(pxMsg, pxResp->priv) != UGW_SUCCESS) {
        if(nType) {
            LOGF_LOG_DEBUG("SetValue CallBack with Type - %d \n", nType);
        }
        if(pxMsg != NULL) {
            pxResp->priv = NULL;
        }
    }
}

static void cal_getValueCb(struct ubus_request *pxResp, int nType, struct blob_attr *pxMsg)
{
    LOGF_LOG_DEBUG("GET Response Received from Servd\n");
    /* Extract the objList from the blob response */
    if(help_blobToObjList(pxMsg, pxResp->priv) != UGW_SUCCESS) {
        if(nType) {
            LOGF_LOG_ERROR("constructing strcuture from blob failed - %d \n", nType);
        }
    }
}

static int ubusMsgToServd(IN MainOper eOperation, IN MsgHeader * pxInputMsg, OUT MsgHeader * pxOutputMsg)
{
    int nRet = UGW_SUCCESS;

    if(eOperation == MOPT_GET) {
        nRet = help_sendMsgToServer(pxInputMsg, cal_getValueCb, "servd", "get", pxOutputMsg);
    } else if(eOperation == MOPT_SET) {
        if(IS_MOPT_FWUPGRADE(pxInputMsg->unMainOper)) {
            nRet = help_sendMsgToServer(pxInputMsg, cal_setValueCb, "servd", "modify.fwupgrade", pxOutputMsg);
        } else {
            nRet = help_sendMsgToServer(pxInputMsg, cal_setValueCb, "servd", "modify", pxOutputMsg);
        }
    } else if(eOperation == MOPT_TIMERACTION) {
        nRet = help_sendMsgToServer(pxInputMsg, cal_getValueCb, "servd", "timeraction", pxOutputMsg);
    }

    return nRet;
}

/* =============================================================================
 *  Function Name : cal_getValueFromDb
 *  Description   : API to get the values corresponding to an object or parameter
 *                  from the DOM Tree
 * ============================================================================*/
int cal_getValueFromDb(INOUT MsgHeader * pxMsg)
{
    int nRet = UGW_SUCCESS;

    nRet = capi_getValue(pxMsg);
    if(nRet != UGW_SUCCESS) {
        LOGF_LOG_ERROR("Get From DB returned failure code : %d\n", nRet);
    }

    return nRet;
}

/* =============================================================================
 *  Function Name : cal_setValueToDb
 *  Description   : API to set values for objects/parameters in the DOM tree
 * ============================================================================*/
int cal_setValueToDb(IN MsgHeader * pxObjList)
{
    int nRet = UGW_SUCCESS;

    /* call the API to update the configuration to System Config File */
    nRet = capi_setValue(pxObjList);
    if(nRet != UGW_SUCCESS) {
        LOGF_LOG_ERROR("Setting to DB Failed. RetCode : %d\n", nRet);
    }

    return nRet;
}

/*============================================================================
 *   Funcion Name: servd_sendMsgToPolld
 *   Description : API to post ubus message with Objlist and timer list to polld
=============================================================================*/
int cal_sendMsgToPolld(IN MsgHeader * pxMsg, IN PolldTimeInfo * pxTimerList)
{
    int nRet = UGW_SUCCESS;
    ObjList *pxObj = NULL;

    ParamList *pxParam = NULL;
    PolldTimeInfo *pTimerTmp = NULL;
    int nCount = 0;
    int nTimerCount = 0;

#ifdef DEBUG
    help_printTimerList(pxTimerList);
    HELP_PRINT_MSG(pxMsg);
#endif
    /* Calculating number of timers */
    FOR_EACH_TIMER_ONLY(pxTimerList, pTimerTmp) {
        nTimerCount++;
    }

    FOR_EACH_OBJ(pxMsg->pObjType, pxObj) {
        if(list_empty(&(pxObj->xParamList.xPlist))) {
            /* Check for paramater less object */
            LOGF_LOG_DEBUG("ParamList is NULL for %s \n", pxObj->sObjName);
            nCount++;
        }
        FOR_EACH_PARAM(pxObj, pxParam) {
            nCount++;
        }
    }
    if(nTimerCount == nCount) {
        LOGF_LOG_DEBUG("Parameters or Parameter less object count: %d TimerCount: %d \n", nCount, nTimerCount);
        nRet = ubusMsgToPolld(pxMsg->unMainOper, pxMsg, pxMsg, pxTimerList);
        if(nRet != UGW_SUCCESS) {
            LOGF_LOG_ERROR("Sending ubus msg to polld failed \n");
            goto end;
        }
    } else {
        LOGF_LOG_ERROR("Requested number of paramaters : %d and timers : %d  are not same .. \n", nCount, nTimerCount);
        nRet = UGW_FAILURE;
        goto end;
    }
 end:
    return nRet;
}
