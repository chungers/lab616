/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 * @author Mladen Turk
 * @version $Revision: 467206 $, $Date: 2006-10-24 04:45:46 +0200 (Tue, 24 Oct 2006) $
 */

#include "tcn.h"

TCN_IMPLEMENT_CALL(jlong, Thread, current)(TCN_STDARGS)
{
    UNREFERENCED_STDARGS;
    return (jlong)((unsigned long)apr_os_thread_current());
}
