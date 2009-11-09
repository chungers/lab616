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

#include "avro.h"
#include <string.h>
#include "dump.h"

static avro_status_t
socket_get_bytes (struct AVRO *avro, caddr_t addr, const int64_t len)
{
  apr_status_t apr_status;
  if ((avro->len - avro->used) < len || len < 0)
    {
      return AVRO_FAILURE;
    }
  if (len > 0)
    {
      /* TODO: len can overflow an apr_size_t */
      apr_size_t bytes_wanted = len;
      apr_size_t bytes_recvd = 0;
      while (bytes_recvd < bytes_wanted)
	{
	  apr_size_t bytes_requested = bytes_wanted - bytes_recvd;
	  apr_status = apr_socket_recv (avro->socket, addr, &bytes_requested);
	  if (apr_status != APR_SUCCESS)
	    {
	      return AVRO_FAILURE;
	    }
	  bytes_recvd += bytes_requested;
	}
    }
  return AVRO_OK;
}

static avro_status_t
socket_put_bytes (struct AVRO *avro, const char *addr, const int64_t len)
{
  apr_status_t apr_status;
  if ((avro->len - avro->used) < len || len < 0)
    {
      return AVRO_FAILURE;
    }
  if (len > 0)
    {
      /* TODO: len can overflow an apr_size_t */
      apr_size_t bytes_wanted = len;
      apr_size_t bytes_sent = 0;
      while (bytes_sent < bytes_wanted)
	{
	  apr_size_t bytes_requested = bytes_wanted - bytes_sent;
	  apr_status = apr_socket_send (avro->socket, addr, &bytes_requested);
	  if (apr_status != APR_SUCCESS)
	    {
	      return AVRO_FAILURE;
	    }
	  bytes_sent += bytes_requested;
	}
    }
  return AVRO_OK;
}

static const struct avro_ops avro_socket_ops = {
  socket_get_bytes,
  socket_put_bytes
};

avro_status_t
avro_create_socket (AVRO * avro, apr_pool_t * pool, apr_socket_t * socket,
		    avro_op op)
{
  if (!avro || !pool || !socket)
    {
      return AVRO_FAILURE;
    }
  avro->pool = pool;
  avro->a_op = op;
  avro->a_ops = (struct avro_ops *) &avro_socket_ops;
  avro->socket = socket;
  return AVRO_OK;
}

void
avro_dump_socket (AVRO * avro, FILE * fp)
{
  dump (fp, avro->addr, avro->used);
}
