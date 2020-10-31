#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>
#include <errno.h>

#include <ltq_api_include.h>


/* Test cases : 1. Normal functionality
 *              2. Buffer overflow
 *              3. NULL invalidation
 */


int main(){
	printf("Hello World\n");
	int nRet = UGW_FAILURE;
	char* pcRet = NULL;

	/**************** TESTING STRNCPY *******************/

	printf("@@@@ HELP_STRNCPY TEST CASES@@@@\n");
	/* Checking normal functionality */
	char buffer1[6] = "Test";

	pcRet = HELP_STRNCPY(buffer1, "Hello", sizeof(buffer1));
	if( pcRet == NULL){
		printf("Failed API\n");
	}
	else{
		if(!strcmp(pcRet, "Hello")){
			printf("Normal functionality --> Pass\n");
		}
		else
			printf("Normal functionality --> Fail. Expected = %s. Got = %s\n", "Hello World", buffer1);
	}

	/* Checking buffer overflow. Buffer size is 6. "Hellowx" size is
	 * 7 including '\0'*/
	pcRet = HELP_STRNCPY(buffer1, "Hellox", sizeof(buffer));
	if( pcRet == NULL){
		printf("Buffer overflow --> Pass\n");
	}
	else{
		printf("Buffer overflow --> Fail");
	}

	/* Checking NULL invalidation */
	pcRet = HELP_STRNCPY(buffer1, NULL, sizeof(buffer1));
	if( pcRet == NULL){
		printf("NULL Invalidation --> Pass\n");
	}
	else{
		printf("NULL Invalidation --> Fail\n");
	}


	/**************** TESTING STRNCAT *******************/

	printf("@@@@ HELP_STRNCAT TEST CASES@@@@\n");
	char buffer2[7] = "He";

	/* "HeHell" is of size 7. Target size is enough */
	pcRet = HELP_STRNCAT(buffer, " Hell", sizeof(buffer));
	if(pcRet < 0){
		printf("Failed API\n");
	}
	else{
		if( !strcmp(buffer, "Test Hello World")){
			printf("Normal functionality --> Pass\n");
		}
		else
			printf("Normal functionality --> Fail. Expected = %s. Got = %s\n", "Hello World", buffer);
	}

	HELP_STRNCPY(buffer2, "He", sizeof(buffer));

	/* Negative test case. Causes buffer overflow. "HeHello" is of
	 * size 8. Target buffer is of size 7 */
	pcRet = HELP_STRNCAT(buffer, "Hello", sizeof(buffer));
	if( pcRet == NULL){
		printf("Buffer overflow --> Pass\n");
	}
	else{
		printf("Buffer overflow --> Fail\n");
	}

	/* Checking NULL invalidation */
	pcRet = HELP_STRNCAT(buffer, NULL, sizeof(buffer));
	if( pcRet == NULL){
		printf("NULL invalidation --> Pass\n");
	}
	else
		printf("NULL invalidation --> Fail\n");

	/**************** TESTING STRCHR *******************/

	printf("@@@@ HELP_STRCHR TEST CASES@@@@\n");

	char buffer3[7] = "Hello";

	pcRet = HELP_STRCHR(buffer3, 'l');

	if( pcRet == NULL){
		printf("Failed API\n");
	}
	else{
		if( !strcmp(pcRet, "llo")){
			printf("Normal functionality --> Pass\n");
		}
		else
			printf("Normal functionality --> Fail. Expected = %s. Got = %s\n", "llo", pcRet);
	}

	/**************** TESTING STRSTR *******************/

	printf("@@@@ HELP_STRSTR TEST CASES @@@@\n");

	char buffer4[32] = "Hello World";

	pcRet = HELP_STRSTR(buffer, "World");

	if( pcRet == NULL){
		printf("Failed API\n");
	}
	else{
		if( !strcmp(pcRet, "World")){
			printf("Normal functionality --> Pass\n");
		}
		else
			printf("Normal functionality --> Fail. Expected = %s. Got = %s\n", "World", pcRet);
	}

}
