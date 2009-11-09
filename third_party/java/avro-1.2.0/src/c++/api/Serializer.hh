/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef avro_Serializer_hh__
#define avro_Serializer_hh__

#include <boost/noncopyable.hpp>

#include "Writer.hh"
#include "ValidatingWriter.hh"

namespace avro {

/// Class that wraps a Writer or ValidatingWriter with an interface that uses
/// explicit put* names instead of putValue

template<class Writer>
class Serializer : private boost::noncopyable
{

  public:

    /// Constructor only works with Writer
    explicit Serializer(OutputStreamer &out) :
        writer_(out)
    {}

    /// Constructor only works with ValidatingWriter
    Serializer(const ValidSchema &schema, OutputStreamer &out) :
        writer_(schema, out)
    {}

    void putNull() {
        writer_.putValue(Null());
    }

    void putBool(bool val) {
        writer_.putValue(val);
    }

    void putInt(int32_t val) {
        writer_.putValue(val);
    }

    void putLong(int64_t val) {
        writer_.putValue(val);
    }

    void putFloat(float val) {
        writer_.putValue(val);
    }

    void putDouble(double val) {
        writer_.putValue(val);
    }

    void putBytes(const void *val, size_t size) {
        writer_.putBytes(val);
    }

    void putFixed(const uint8_t *val, size_t size) {
        writer_.putFixed(val, size);
    }

    void putString(const std::string &val) {
        writer_.putValue(val);
    }

    void beginRecord() {
        writer_.beginRecord();
    }

    void beginArrayBlock(int64_t size) {
        writer_.beginArrayBlock(size);
    }

    void endArray() {
        writer_.endArray();
    }

    void beginMapBlock(int64_t size) {
        writer_.beginMapBlock(size);
    }

    void endMap() {
        writer_.endMap();
    }

    void beginUnion(int64_t choice) {
        writer_.beginUnion(choice);
    }

    void beginEnum(int64_t choice) {
        writer_.beginEnum(choice);
    }

  private:

    Writer writer_;

};

} // namespace avro

#endif
