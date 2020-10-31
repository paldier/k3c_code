/*****************************************************************************

  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany 

  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.

 ******************************************************************************/
#define  _GNU_SOURCE
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <getopt.h>
#include <string.h>
#include "list.h"
#include "ugw_proto.h"
#include "cal.h"

#define DEBUG 1
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

#define MAX_LEN_FILE_NAME 	80
#define MAX(x, y) 			(((x) > (y)) ? (x) : (y))

static int test_readFile(FILE * filePointer, char *pcLineRead)
{
    char cEndOfFile;
    int ret = 1;
    size_t nBytes = (MAX(MAX_LEN_OBJNAME, MAX_LEN_PARAM_NAME) + MAX_LEN_PARAM_VALUE + 1);

    cEndOfFile = getc(filePointer);
    fseek(filePointer, -1, 1);
    getline(&pcLineRead, &nBytes, filePointer);
    while(pcLineRead[0] == '#' || (strcmp(pcLineRead, "\n") == 0)) {
        cEndOfFile = getc(filePointer);
        if(cEndOfFile == EOF) {
            ret = 0;
            goto ret1;
        }
        fseek(filePointer, -1, 1);
        getline(&pcLineRead, &nBytes, filePointer);
    }
 ret1:
    return ret;
}

static int test_FillObject(ObjList ** pxObj, ObjList * pxObjectList, const MainOper eOper)
{
    char *pcToken;
    const char sDelimColon[2] = ":";
    const char sDelimNewLine[2] = "\n";
    size_t nLen = 0;
    int nRet = 0;
    char *pcOid = NULL;
/*
	char *pcSid = NULL;
*/
    char *pcObjName = NULL;
    ObjOper eSubOper = OBJOPT_NOOBJOPER;
    pcToken = strtok(NULL, sDelimColon);
    if((pcToken != NULL) && (strcmp(pcToken, " ") != 0)) {
        pcObjName = (char *)HELP_MALLOC(MAX_LEN_OBJNAME);
        if(pcObjName == NULL) {
            LOGF_LOG_ERROR("Malloc Failed for Obj Name %s \n", pcToken);
            nRet = UGW_FAILURE;
            goto ret1;
        }
        memset(pcObjName, 0x0, MAX_LEN_OBJNAME);
        strncpy(pcObjName, pcToken, MAX_LEN_OBJNAME);
        nLen = strlen(pcObjName);
        if(!(strcmp((pcObjName + nLen - 1), "\n"))) {
            strcpy(pcObjName + nLen - 1, "");
        }
        nLen = strlen(pcObjName);
        if(strcmp((pcObjName + nLen - 1), ".") != 0) {
            strncat(pcObjName, ".", MAX_LEN_OBJNAME);
        }
        if(eOper != MOPT_SET) {
            goto pcObj_fill;
        }
    }
/*
	pcToken=strtok(NULL,sDelimColon);
	if(pcToken!=NULL && strcmp(pcToken,"\n")!=0 && strcmp(pcToken," ")!=0) {
		pcSid = (char *) malloc (sizeof(char)*MAX_LEN_ID);
		if(pcSid == NULL) {
			LOGF_LOG_ERROR("Malloc Failed for SID \n");
			nRet = UGW_FAILURE;
			goto ret1;
		}
		strncpy(pcSid,pcToken,MAX_LEN_ID);  
		nLen = strlen(pcSid);           
		if(!(strcmp((pcSid+nLen-1),"\n"))) {
			strcpy(pcSid+nLen-1,"");
		}
		if(eOper != MOPT_SET) { 
			goto pcObj_fill;
		}
	}
*/
    pcToken = strtok(NULL, sDelimColon);
    if(pcToken != NULL && strcmp(pcToken, "\n") != 0 && strcmp(pcToken, " ") != 0) {
        pcOid = (char *)HELP_MALLOC(MAX_LEN_ID);
        if(pcOid == NULL) {
            LOGF_LOG_CRITICAL("Memory allocation for storing OID : %s failed\n", pcToken);
            nRet = UGW_FAILURE;
            goto ret1;
        }
        memset(pcOid, 0x0, MAX_LEN_ID);
        strncpy(pcOid, pcToken, MAX_LEN_ID);
        nLen = strlen(pcOid);
        if(!(strcmp((pcOid + nLen - 1), "\n"))) {
            strcpy(pcOid + nLen - 1, "");
        }
        if(eOper != MOPT_SET) {
            goto pcObj_fill;
        }
    }
    if((pcObjName == NULL) && (pcOid == NULL)) {
        LOGF_LOG_ERROR("Please enter atleast any one of the folowing : objectname : sid : oid \n");
        nRet = UGW_FAILURE;
        goto ret1;
    }
    if(eOper == MOPT_SET) {
        pcToken = strtok(NULL, sDelimNewLine);
        if(pcToken != NULL && strcmp(pcToken, "\n") != 0 && strcmp(pcToken, " ") != 0) {
            if(strcasecmp(pcToken, "ADD") == 0)
                eSubOper = OBJOPT_ADD;
            else if(strcasecmp(pcToken, "DELETE") == 0)
                eSubOper = OBJOPT_DEL;
            else if(strcasecmp(pcToken, "MODIFY") == 0)
                eSubOper = OBJOPT_MODIFY;
        } else {
            LOGF_LOG_ERROR("Enter operation for object %s \n", pcObjName);
            nRet = UGW_FAILURE;
            goto ret1;
        }
    }

 pcObj_fill:
    if(eOper == MOPT_SET) {
        *pxObj = HELP_OBJECT_SET(pxObjectList, pcObjName, NO_ARG_VALUE, eSubOper, SOPT_OBJVALUE);
    } else if(eOper == MOPT_GET) {
        *pxObj = HELP_OBJECT_GET(pxObjectList, pcObjName, SOPT_OBJVALUE);
    }

    if(*pxObj == NULL) {
        nRet = UGW_FAILURE;
        LOGF_LOG_ERROR("Helper Library %s returned null value for object %s\n", (eOper == MOPT_SET ) ? "HELP_OBJECT_SET" : "HELP_OBJECT_GET", pcObjName);
        goto ret1;
    }
 ret1:
    HELP_FREE(pcObjName);
//      FREE(pcSid);
    HELP_FREE(pcOid);
    return nRet;
}

static int test_FillParam(ObjList ** pxObj, const MainOper eOper)
{
    char *pcToken;
    const char sDelimColon[2] = ":";
    const char sDelimNewLine[2] = "\n";
    size_t nLen = 0;
    int nRet = 0;
    char *pcPid = NULL;
    char *pcParamName = NULL;
    char *pcParamValue = NULL;

    pcToken = strtok(NULL, sDelimColon);
    if(pcToken != NULL && strcmp(pcToken, "\n") != 0 && strcmp(pcToken, " ") != 0) {
        pcParamName = (char *)HELP_MALLOC(MAX_LEN_PARAM_NAME);
        if(pcParamName == NULL) {
            LOGF_LOG_CRITICAL("Memory Allocation Failed for storing Parameter Name : %s \n", pcToken);
            nRet = UGW_FAILURE;
            goto ret1;
        }
        memset(pcParamName, 0x0, MAX_LEN_PARAM_NAME);
        strncpy(pcParamName, pcToken, MAX_LEN_PARAM_NAME);
        if(eOper == MOPT_GET) {
            nLen = strlen(pcParamName);
            if(!(strcmp((pcParamName + nLen - 1), "\n"))) {
                strcpy(pcParamName + nLen - 1, "");
            }
            goto param_fill;
        }
    }
    pcToken = strtok(NULL, sDelimColon);
    if(pcToken != NULL && strcmp(pcToken, "\n") != 0 && strcmp(pcToken, " ") != 0) {
        pcPid = (char *)HELP_MALLOC(MAX_LEN_ID);
        if(pcPid == NULL) {
            LOGF_LOG_CRITICAL("Memory Allocation Failed for storing Parameter ID : %s \n", pcToken);
            nRet = UGW_FAILURE;
            goto ret1;
        }
        memset(pcPid, 0x0, MAX_LEN_ID);
        strncpy(pcPid, pcToken, MAX_LEN_ID);
    }
    if(eOper == MOPT_SET) {
        pcToken = strtok(NULL, sDelimNewLine);
        if(pcToken != NULL && strcmp(pcToken, "\n") != 0 && strcmp(pcToken, " ") != 0) {
            pcParamValue = (char *)HELP_MALLOC(MAX_LEN_PARAM_VALUE);
            if(pcParamValue == NULL) {
                LOGF_LOG_CRITICAL("Memory Allocation Failed for storing Parameter Value : %s \n", pcToken);
                nRet = UGW_FAILURE;
                goto ret1;
            }
            memset(pcParamValue, 0x0, MAX_LEN_PARAM_VALUE);
            strncpy(pcParamValue, pcToken, MAX_LEN_PARAM_VALUE);
        } else {
            LOGF_LOG_ERROR("Empty Parameter Value Encountered for %s  !! Enter the value\n", pcParamName);
            nRet = UGW_FAILURE;
            goto ret1;
        }
    }
    if(pcParamName == NULL && pcPid == NULL) {
        LOGF_LOG_ERROR("Either of Parameter Name or Param ID is mandatory\n");
        nRet = UGW_FAILURE;
        goto ret1;
    }
 param_fill:
    if(eOper == MOPT_SET) {
        HELP_PARAM_SET(*pxObj, pcParamName, pcParamValue, SOPT_OBJVALUE);
    } else if(eOper == MOPT_GET) {
        HELP_PARAM_GET(*pxObj, pcParamName, SOPT_OBJVALUE);
    }
 ret1:
    HELP_FREE(pcParamName);
    HELP_FREE(pcPid);
    HELP_FREE(pcParamValue);
    return nRet;
}

static int test_populateStruct(const char *pcFile, const MainOper eOper, MsgHeader * pxObjectList)
{
    FILE *filePointer = NULL;
    char *pcToken = NULL;
    char *pcLineRead = NULL;
    const char sDelimColon[2] = ":";
    size_t nBytes = (MAX(MAX_LEN_OBJNAME, MAX_LEN_PARAM_NAME) + MAX_LEN_PARAM_VALUE + 1);
    int nRet = 0;

    filePointer = fopen(pcFile, "r");
    if(filePointer == NULL) {
        LOGF_LOG_CRITICAL("Unable to open file : %s!!!\n", pcFile);
        if(filePointer != NULL) {
            fclose(filePointer);
        }
        return UGW_FAILURE;
    }

    pcLineRead = (char *)HELP_MALLOC(sizeof(char) * (nBytes + 1));
    if(pcLineRead == NULL) {
        LOGF_LOG_CRITICAL("Malloc failed for buffer to read the line\n");
        fclose(filePointer);
        return UGW_FAILURE;
    }
    memset(pcLineRead, 0, nBytes + 1);

    ObjList *pxObj = NULL;
    while(test_readFile(filePointer, pcLineRead)) {
        pcToken = strtok(pcLineRead, sDelimColon);
        if(pcToken != NULL) {
            if(strcmp(pcToken, "object") == 0) {
                nRet = test_FillObject(&pxObj, pxObjectList->pObjType, eOper);
                if(nRet != UGW_SUCCESS) {
                    goto ret;
                }
            }
            if(strcmp(pcToken, "param") == 0) {
                nRet = test_FillParam(&pxObj, eOper);
                if(nRet != UGW_SUCCESS) {
                    goto ret;
                }

            }
        }
    }

 ret:
    HELP_FREE(pcLineRead);
    // HELP_FREE(pxObj);
    fclose(filePointer);
    return nRet;
}

static int test_parseArgs(int nArgc, char *pcArgv[], MainOper * eOper, char *pcFile, Owner * OwnerField)
{
    int nRet = UGW_SUCCESS;
    int nRetGetOpt;

    if(nArgc < 3) {
        LOGF_LOG_ERROR("Insufficient input arguments \n");
        nRet = UGW_FAILURE;
        goto usage;
    }

    while((nRetGetOpt = getopt(nArgc, pcArgv, "g:s:c:")) != -1) {
        switch (nRetGetOpt) {
            case 'c':
                if(optarg) {
                    if(strcmp(optarg, "TR69") == 0)
                        *OwnerField = OWN_TR69;
                    else if(strcmp(optarg, "WEB") == 0)
                        *OwnerField = OWN_WEB;
                    else if(strcmp(optarg, "SERVD") == 0)
                        *OwnerField = OWN_SERVD;
                    else if(strcmp(optarg, "CLI") == 0)
                        *OwnerField = OWN_CLI;
                    else if(strcmp(optarg, "POLLD") == 0)
                        *OwnerField = OWN_POLLD;
                    else if(strcmp(optarg, "OTHER") == 0)
                        *OwnerField = OWN_OTHER;
                    else {
                        LOGF_LOG_ERROR("Invalid Owner field : %s\n", optarg);
                        nRet = UGW_FAILURE;
                        goto usage;
                    }
                } else
                    goto usage;
                break;
            case 'g':
                *eOper = MOPT_GET;
                if(optarg)
                    strcpy(pcFile, optarg);
                else
                    goto usage;
                break;
            case 's':
                *eOper = MOPT_SET;
                if(optarg) {
                    strcpy(pcFile, optarg);
                } else
                    goto usage;
                break;
                break;
            default:
                LOGF_LOG_ERROR("Invalid Input %d\n", nRetGetOpt);
                nRet = UGW_FAILURE;
                goto usage;
        }
    }
    if(pcFile != NULL) {
        if(access(pcFile, F_OK) == -1) {
            LOGF_LOG_ERROR("File %s does not exist\n", pcFile);
            HELP_FREE(pcFile);
            exit(0);
        }
    } else {
        goto usage;
    }

    return nRet;
 usage:
#ifdef DEBUG
    fprintf(stderr, "usage : caltest -g | -s <filename> -c <caller/invoker of the operation> \n where \n g - get, s - set, c - invoker of the operation");
    fprintf(stderr, " filename is where the information for which object/parameter get or set needs to be performed\n");
    fprintf(stderr, " invoker/caller is either of TR69 | WEB | CLI | SERVD | POLLD| OTHER\n");
#endif
    HELP_FREE(pcFile);
    exit(0);
}

int main(int argc, char *argv[])
{
    int nRetVal = 0;
    MainOper eMainOp = MOPT_SET;
    MsgHeader xObjectList;
    char *pcFileName = NULL;
    Owner OwnerField = OWN_OTHER;

    pcFileName = (char *)HELP_MALLOC(MAX_LEN_FILE_NAME);
    if(pcFileName == NULL) {
        LOGF_LOG_CRITICAL("Memory Allocation failure for storing Filename\n");
        exit(0);
    }
    nRetVal = test_parseArgs(argc, argv, &eMainOp, pcFileName, &OwnerField);
    if(nRetVal != UGW_SUCCESS) {
        goto ret;
    }

    HELP_CREATE_MSG(&xObjectList, eMainOp, SOPT_OBJVALUE, OwnerField, 1);

    nRetVal = test_populateStruct(pcFileName, eMainOp, &xObjectList);
#ifdef DEBUG
    fprintf(stderr, "**********RECEIVED ObjList from the User*****\n");
    HELP_PRINT_MSG(&xObjectList);
    fprintf(stderr, "\n********************\n");
#endif
    if(nRetVal != UGW_SUCCESS) {
        goto ret;
    }

    switch (eMainOp) {
        case MOPT_GET:
            if(OwnerField == OWN_POLLD) {
                nRetVal = cal_updatePeriodicValues(&xObjectList);
            } else {
                nRetVal = cal_getValue(&xObjectList);
            }
            if(nRetVal != UGW_SUCCESS) {
                fprintf(stderr, "cal_getValue Failed: Returned code :%d\n", nRetVal);
                goto ret;
            } else {
                fprintf(stderr, "cal_getValue returned success\n");
            }
            break;
        case MOPT_SET:
            nRetVal = cal_setValue(&xObjectList);
            if(nRetVal != UGW_SUCCESS) {
                fprintf(stderr, "CAL_SET Returned Error : %d: %s\n", nRetVal, (nRetVal == ERR_VALIDATION_FAILED) ? "Validation Failure" : "CAL Set Failure");
                goto ret;
            } else {
                fprintf(stderr, "cal_setValue success : Returned ObjList from CAL to User\n");
            }
            break;
        default:
            LOGF_LOG_ERROR("Received Invalid Option for Operation : %d \n", eMainOp);
            nRetVal = UGW_FAILURE;
            goto ret;
    }
 ret:

    fprintf(stderr, "\n**********RETURNED RESPONSE AT ME**********\n");
    HELP_PRINT_MSG(&xObjectList);
    fprintf(stderr, "\n********************\n");

    HELP_FREE(pcFileName);
    HELP_DELETE_MSG(&xObjectList);

    return nRetVal;
}
