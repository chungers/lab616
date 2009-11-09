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

#ifndef avro_ValidatingReader_hh__
#define avro_ValidatingReader_hh__

#include <stdint.h>
#include <vector>
#include <boost/noncopyable.hpp>

#include "Reader.hh"
#include "Validator.hh"
#include "AvroTraits.hh"

namespace avro {

class ValidSchema;
class InputStreamer;

/// As an avro object is being parsed from binary data to its C++
/// representation, this parser will walk the parse tree and ensure that the
/// correct type is being asked for.  If the user attempts to parse a type that
/// does not match what the schema says, an exception will be thrown.  
///
/// The ValidatingReader object can also be used to tell what the next type is,
/// so that callers can dynamically discover the contents.  It also tells
/// the attribute names of the objects or their fields, if they exist.
///

class ValidatingReader : private boost::noncopyable
{

  public:

    ValidatingReader(const ValidSchema &schema, InputStreamer &in);

    template<typename T>
    void getValue(T &val) {
        checkSafeToGet(type_to_avro<T>::type);
        reader_.getValue(val);
        validator_.advance();
    }

    void getBytes(std::vector<uint8_t> &val) {
        checkSafeToGet(AVRO_BYTES);
        validator_.advance();
        reader_.getBytes(val);
    }

    void getFixed(uint8_t *val, size_t size) {
        checkSafeToGet(AVRO_FIXED);
        checkSizeExpected(size);
        validator_.advance();
        reader_.getFixed(val, size);
    }

    void getFixed(std::vector<uint8_t> &val, size_t size) {
        checkSafeToGet(AVRO_FIXED);
        checkSizeExpected(size);
        validator_.advance();
        reader_.getFixed(val, size);
    }


    void getRecord();

    int64_t getArrayBlockSize();

    int64_t getUnion();

    int64_t getEnum();

    int64_t getMapBlockSize();

    Type nextType() const{
        return validator_.nextTypeExpected();
    }

    bool getCurrentRecordName(std::string &name) const {
        return validator_.getCurrentRecordName(name);
    }

    bool getNextFieldName(std::string &name) const {
        return validator_.getNextFieldName(name);
    }

  private:

    int64_t getCount();

    void checkSafeToGet(Type type) const {
        if(validator_.nextTypeExpected() != type) {
            throw Exception("Type does not match");
        }
    }

    void checkSizeExpected(int size) const {
        if(validator_.nextSizeExpected() != size) {
            throw Exception("Wrong size of for fixed");
        }
    }

    Validator validator_;
    Reader reader_;
};

} // namespace avro

#endif
