/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

#ifndef JSON_TOKENIZER_H
#define JSON_TOKENIZER_H

#include <wchar.h>
#include "json_schema.h"

/* Tokens which are not part of the schema */
enum json_tokens
{
  TK_SPACE = 42424242,
  TK_ILLEGAL
};

struct Token
{
  char *z;
  double d;
  int b;
};
typedef struct Token Token;

int json_get_token (const wchar_t * z, const size_t len, int *tokenType,
		    double *number);

#endif
