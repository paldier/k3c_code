
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

#include "ugw_defs.h"
#include "ugw_structs.h"
#include "ugw_common.h"

#include "sl_test.h"
#include "sl_test_map.h"

#define TEST_OBJ_NAME	"Device.Firewall."
#define PARAM1_NAME	"Enable"
#define PARAM1_VAL	"False"
#define PARAM2_NAME	"Config"
#define PARAM2_VAL	"Advanced1"
#define PARAM3_NAME	"AdvancedLevel"
#define PARAM3_VAL	"test level"

#define PARAM_MAP_NAME "xParamMap"

void addNVToList(ObjList *pxObjList);
void addValuesToStruct(Firewall_t *pxStruct);
int32_t enumMapGet(name_value_t enum_nv[], int32_t iSize, char *trVal, int32_t *uiId, int32_t iSel);
void printStruct(x_LTQ_Param xParamDetail[], int32_t iSize, void * xStruct);
void printList(ObjList *pxObjList);
name_value_t name_val[100];
x_LTQ_Param param_map[100];

/* =============================================================================
 *  Function Name : addNVToList
 *  Description   : This API would create new object with specified object name, and
 *  			add specified parameters as paramlist in the object.
 * ============================================================================*/
void addNVToList(ObjList *pxObjList)
{
	ObjList	*pxNew = NULL;

	pxNew = HELP_OBJECT_SET(pxObjList, TEST_OBJ_NAME,
		0, OBJOPT_MODIFY, SOPT_OBJVALUE);
	HELP_PARAM_SET(pxNew, PARAM1_NAME, PARAM1_VAL, SOPT_OBJVALUE);
	HELP_PARAM_SET(pxNew, PARAM2_NAME, PARAM2_VAL, SOPT_OBJVALUE);
	HELP_PARAM_SET(pxNew, PARAM3_NAME, PARAM3_VAL, SOPT_OBJVALUE);

	return;
}

void addValuesToStruct(Firewall_t *pxStruct)
{
	pxStruct->bEnable = (!strcasecmp(PARAM1_VAL, "true"))?1:0;
	pxStruct->eConfig = FIREWALL_CONFIG_LOW;
	sprintf(pxStruct->sAdvancedLevel, "%s", PARAM3_VAL);
	/* since Id is running number in integer value range, the same can't be used
	 * directly to refer to bit in xParamMap. so calculate relative Id for this param
	 * in this object and use.
	 */
	pxStruct->xParamMap.nParamInc |= (1 << (DEVICE_FIREWALL_ENABLE - DEVICE_FIREWALL));
	pxStruct->xParamMap.nParamInc |= (1 << (DEVICE_FIREWALL_CONFIG - DEVICE_FIREWALL));
	pxStruct->xParamMap.nParamInc |= (1 << (DEVICE_FIREWALL_ADVANCEDLEVEL - DEVICE_FIREWALL));

	return;
}

/* =============================================================================
 *  Function Name : printStruct
 *  Description   : This API prints members of structure specified in third argument.
 * ============================================================================*/
void printStruct(x_LTQ_Param xParamDetail[], int32_t iSize, void * xStruct)
{
	int32_t iIndex;
	char sStr[100];
	Firewall_t *xStructTmp = (Firewall_t *)xStruct;

	memset(sStr, 0, sizeof(sStr));

	for(iIndex = 0;iIndex < iSize;iIndex++) {
		/* can also check xStruct->xParamMap to identify which parameters
		 * have values set in the structure.
		 */
		switch(xParamDetail[iIndex].uiId) {
			case DEVICE_FIREWALL_ADVANCEDLEVEL:
				printf("Description : %s\n", xStructTmp->sAdvancedLevel);
				break;
			case DEVICE_FIREWALL_CONFIG:
				iSize = sizeof(name_val)/sizeof(name_value_t);
				/* convert integer form of enum to TR-XXX enum string form. */
				if(enumMapGet(name_val, iSize, sStr, (int32_t *)&xStructTmp->eConfig, 2) != UGW_SUCCESS) {
					printf("Failed to identify corresponding string for enum int [%d]\n", (int32_t)xStructTmp->eConfig);
				}
				printf("Config : %s\n", sStr);
				break;
			case DEVICE_FIREWALL_ENABLE:
				printf("Enable : %s\n", (xStructTmp->bEnable == 1)?"true":"false");
				break;
		}
	}
	return;
}

/* =============================================================================
 *  Function Name : printList
 *  Description   : This API prints all parameters name and value of specified
 *  			object argument.
 * ============================================================================*/
void printList(ObjList *pxObjList)
{
	ObjList *pxTmpObj = NULL;
	ParamList *pxParam = NULL;
	FOR_EACH_OBJ(pxObjList, pxTmpObj) {
		FOR_EACH_PARAM(pxTmpObj, pxParam) {
			printf("[%s] : [%s]\n",
				pxParam->sParamName, pxParam->sParamValue);
		}
	}
	return;
}

int main(int32_t argc, char *argv[])
{
	Firewall_t xStruct;
	ObjList *pxObjList = NULL, *pxTmpObj = NULL;
	int32_t iSize = 0;

	memset(&xStruct, 0, sizeof(xStruct));

	if(argc != 2) {
		printf("Usage : ./test_struct2list <1/2>\n"
				"\t1 - convert list to struct\n"
				"\t2 - convert struct to list\n");
		exit(-1);
	}

	memcpy(name_val, FirewallConfig_nv, sizeof(FirewallConfig_nv));
	memcpy(param_map, x_LTQ_Firewall_param, sizeof(x_LTQ_Firewall_param));

	switch(atoi(argv[1])) {
		case 1:
			/* convert objlist to struct. */

			/* allocate memort for objlist. */
			pxObjList = HELP_CREATE_OBJ(SOPT_OBJVALUE);
			if(pxObjList == NULL) {
				break;
			}

			/* populate dummy values to objlist. */
			addNVToList(pxObjList);

			/* get the number of parameters. */
			iSize = sizeof(param_map) / sizeof(x_LTQ_Param);

			printf("-------------------------------\n"
				"    Parameter list (from list)    \n"
				"-------------------------------\n");
			printList(pxObjList);

			/* call API to convert objlist to struct. */
			FOR_EACH_OBJ(pxObjList, pxTmpObj) {
				help_objlistToStructConv(pxTmpObj, &xStruct, param_map, iSize);

				printf("-------------------------------\n"
					" Parameter list (from structure) \n"
					"-------------------------------\n");
				printStruct(param_map, iSize, &xStruct);
			}

			HELP_DELETE_OBJ(pxObjList, SOPT_OBJVALUE, FREE_OBJLIST);
			break;
		case 2 :
			/* convert struct to objlist. */

			/* allocate memort for objlist. */
			pxObjList = HELP_CREATE_OBJ(SOPT_OBJVALUE);
			if(pxObjList == NULL) {
				break;
			}
			pxTmpObj = HELP_OBJECT_SET(pxObjList, TEST_OBJ_NAME,
				0, OBJOPT_MODIFY, SOPT_OBJVALUE);

			/* populate dummy values to struct. */
			addValuesToStruct(&xStruct);

			/* get the number of parameters. */
			iSize = sizeof(param_map) / sizeof(x_LTQ_Param);

			printf("-------------------------------\n"
				" Parameter list (from structure) \n"
				"-------------------------------\n");
			printStruct(param_map, iSize, &xStruct);

			/* call API to convert struct to objlist. */
			help_structToObjlistConv(pxTmpObj, (void *)&xStruct, param_map, iSize);

			printf("-------------------------------\n"
				"    Parameter list (from list)    \n"
				"-------------------------------\n");
			printList(pxObjList);

			HELP_DELETE_OBJ(pxObjList, SOPT_OBJVALUE, FREE_OBJLIST);
			break;
	}
	
	return 0;
}
