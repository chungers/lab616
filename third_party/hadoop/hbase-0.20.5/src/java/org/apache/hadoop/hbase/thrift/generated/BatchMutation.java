/*
 * Copyright 2009 The Apache Software Foundation
 *
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
package org.apache.hadoop.hbase.thrift.generated;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.apache.thrift.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.protocol.*;

/**
 * A BatchMutation object is used to apply a number of Mutations to a single row.
 */
public class BatchMutation implements TBase, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("BatchMutation");
  private static final TField ROW_FIELD_DESC = new TField("row", TType.STRING, (short)1);
  private static final TField MUTATIONS_FIELD_DESC = new TField("mutations", TType.LIST, (short)2);

  public byte[] row;
  public static final int ROW = 1;
  public List<Mutation> mutations;
  public static final int MUTATIONS = 2;

  private final Isset __isset = new Isset();
  private static final class Isset implements java.io.Serializable {
  }

  public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {{
    put(ROW, new FieldMetaData("row", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.STRING)));
    put(MUTATIONS, new FieldMetaData("mutations", TFieldRequirementType.DEFAULT, 
        new ListMetaData(TType.LIST, 
            new StructMetaData(TType.STRUCT, Mutation.class))));
  }});

  static {
    FieldMetaData.addStructMetaDataMap(BatchMutation.class, metaDataMap);
  }

  public BatchMutation() {
  }

  public BatchMutation(
    byte[] row,
    List<Mutation> mutations)
  {
    this();
    this.row = row;
    this.mutations = mutations;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public BatchMutation(BatchMutation other) {
    if (other.isSetRow()) {
      this.row = other.row;
    }
    if (other.isSetMutations()) {
      List<Mutation> __this__mutations = new ArrayList<Mutation>();
      for (Mutation other_element : other.mutations) {
        __this__mutations.add(new Mutation(other_element));
      }
      this.mutations = __this__mutations;
    }
  }

  @Override
  public BatchMutation clone() {
    return new BatchMutation(this);
  }

  public byte[] getRow() {
    return this.row;
  }

  public void setRow(byte[] row) {
    this.row = row;
  }

  public void unsetRow() {
    this.row = null;
  }

  // Returns true if field row is set (has been asigned a value) and false otherwise
  public boolean isSetRow() {
    return this.row != null;
  }

  public void setRowIsSet(boolean value) {
    if (!value) {
      this.row = null;
    }
  }

  public int getMutationsSize() {
    return (this.mutations == null) ? 0 : this.mutations.size();
  }

  public java.util.Iterator<Mutation> getMutationsIterator() {
    return (this.mutations == null) ? null : this.mutations.iterator();
  }

  public void addToMutations(Mutation elem) {
    if (this.mutations == null) {
      this.mutations = new ArrayList<Mutation>();
    }
    this.mutations.add(elem);
  }

  public List<Mutation> getMutations() {
    return this.mutations;
  }

  public void setMutations(List<Mutation> mutations) {
    this.mutations = mutations;
  }

  public void unsetMutations() {
    this.mutations = null;
  }

  // Returns true if field mutations is set (has been asigned a value) and false otherwise
  public boolean isSetMutations() {
    return this.mutations != null;
  }

  public void setMutationsIsSet(boolean value) {
    if (!value) {
      this.mutations = null;
    }
  }

  public void setFieldValue(int fieldID, Object value) {
    switch (fieldID) {
    case ROW:
      if (value == null) {
        unsetRow();
      } else {
        setRow((byte[])value);
      }
      break;

    case MUTATIONS:
      if (value == null) {
        unsetMutations();
      } else {
        setMutations((List<Mutation>)value);
      }
      break;

    default:
      throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
    }
  }

  public Object getFieldValue(int fieldID) {
    switch (fieldID) {
    case ROW:
      return getRow();

    case MUTATIONS:
      return getMutations();

    default:
      throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
    }
  }

  // Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
  public boolean isSet(int fieldID) {
    switch (fieldID) {
    case ROW:
      return isSetRow();
    case MUTATIONS:
      return isSetMutations();
    default:
      throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
    }
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof BatchMutation)
      return this.equals((BatchMutation)that);
    return false;
  }

  public boolean equals(BatchMutation that) {
    if (that == null)
      return false;

    boolean this_present_row = true && this.isSetRow();
    boolean that_present_row = true && that.isSetRow();
    if (this_present_row || that_present_row) {
      if (!(this_present_row && that_present_row))
        return false;
      if (!java.util.Arrays.equals(this.row, that.row))
        return false;
    }

    boolean this_present_mutations = true && this.isSetMutations();
    boolean that_present_mutations = true && that.isSetMutations();
    if (this_present_mutations || that_present_mutations) {
      if (!(this_present_mutations && that_present_mutations))
        return false;
      if (!this.mutations.equals(that.mutations))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id)
      {
        case ROW:
          if (field.type == TType.STRING) {
            this.row = iprot.readBinary();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case MUTATIONS:
          if (field.type == TType.LIST) {
            {
              TList _list0 = iprot.readListBegin();
              this.mutations = new ArrayList<Mutation>(_list0.size);
              for (int _i1 = 0; _i1 < _list0.size; ++_i1)
              {
                Mutation _elem2;
                _elem2 = new Mutation();
                _elem2.read(iprot);
                this.mutations.add(_elem2);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
          break;
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();


    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.row != null) {
      oprot.writeFieldBegin(ROW_FIELD_DESC);
      oprot.writeBinary(this.row);
      oprot.writeFieldEnd();
    }
    if (this.mutations != null) {
      oprot.writeFieldBegin(MUTATIONS_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.mutations.size()));
        for (Mutation _iter3 : this.mutations)        {
          _iter3.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("BatchMutation(");
    boolean first = true;

    sb.append("row:");
    if (this.row == null) {
      sb.append("null");
    } else {
      sb.append(this.row);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("mutations:");
    if (this.mutations == null) {
      sb.append("null");
    } else {
      sb.append(this.mutations);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
    // check that fields of type enum have valid values
  }

}

