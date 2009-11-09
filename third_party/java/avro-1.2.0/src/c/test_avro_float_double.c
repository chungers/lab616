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

#include <apr.h>
#include <apr_pools.h>
#include <apr_buckets.h>
#include <apr_file_io.h>
#include <stdlib.h>
#include <time.h>
#include "avro.h"
#include "error.h"

int
main (void)
{
  apr_pool_t *pool;
  AVRO avro_in, avro_out;
  avro_status_t avro_status;
  char buf[1024];
  float f_in, f_out;
  double d_in, d_out;
  int i;

  avro_initialize ();

  srand (time (NULL));

  for (i = 0; i < 10; i++)
    {
      apr_pool_create (&pool, NULL);
      avro_status =
	avro_create_memory (&avro_in, pool, buf, sizeof (buf), AVRO_ENCODE);
      if (avro_status != AVRO_OK)
	{
	  err_quit ("Unable to create AVRO encoder");
	}

      f_in = rand () + (rand () / (RAND_MAX + 1.0));
      d_in = f_in * f_in;

      avro_status = avro_float (&avro_in, &f_in);
      if (avro_status != AVRO_OK)
	{
	  err_quit ("Unable to encode float value=%f", f_in);
	}

      avro_status = avro_double (&avro_in, &d_in);
      if (avro_status != AVRO_OK)
	{
	  err_quit ("Unable to encode double value=%f", d_in);
	}

      avro_status =
	avro_create_memory (&avro_out, pool, buf, sizeof (buf), AVRO_DECODE);
      if (avro_status != AVRO_OK)
	{
	  err_quit ("Unable to create AVRO decoder");
	}

      avro_status = avro_float (&avro_out, &f_out);
      if (avro_status != AVRO_OK)
	{
	  err_quit ("Unable to decode AVRO float");
	}

      avro_status = avro_getint64_raw (&avro_out, (int64_t *) & d_out);
      if (avro_status != AVRO_OK)
	{
	  err_quit ("Unable to decode AVRO double");
	}

      if (f_in != f_out)
	{
	  avro_dump_memory (&avro_in, stderr);
	  avro_dump_memory (&avro_out, stderr);
	  err_quit ("Error encoding decoding float %f != %f", f_in, f_out);
	}
      if (d_in != d_out)
	{
	  err_quit ("Error encoding decoding double %f != %f", d_in, d_out);
	}
      apr_pool_destroy (pool);
    }

  return EXIT_SUCCESS;
}
