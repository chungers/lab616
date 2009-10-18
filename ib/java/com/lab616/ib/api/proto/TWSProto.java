// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.lab616.ib.api.proto;

public final class TWSProto {
  private TWSProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public enum Method
      implements com.google.protobuf.ProtocolMessageEnum {
    tickGeneric(0, 0),
    tickOptionComputation(1, 1),
    tickPrice(2, 2),
    tickSize(3, 3),
    tickString(4, 4),
    realtimeBar(5, 5),
    updateMktDepth(6, 6),
    updateMktDepthL2(7, 7),
    currentTime(8, 8),
    historicalData(9, 9),
    ;
    
    
    public final int getNumber() { return value; }
    
    public static Method valueOf(int value) {
      switch (value) {
        case 0: return tickGeneric;
        case 1: return tickOptionComputation;
        case 2: return tickPrice;
        case 3: return tickSize;
        case 4: return tickString;
        case 5: return realtimeBar;
        case 6: return updateMktDepth;
        case 7: return updateMktDepthL2;
        case 8: return currentTime;
        case 9: return historicalData;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<Method>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<Method>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Method>() {
            public Method findValueByNumber(int number) {
              return Method.valueOf(number)
    ;        }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.lab616.ib.api.proto.TWSProto.getDescriptor().getEnumTypes().get(0);
    }
    
    private static final Method[] VALUES = {
      tickGeneric, tickOptionComputation, tickPrice, tickSize, tickString, realtimeBar, updateMktDepth, updateMktDepthL2, currentTime, historicalData, 
    };
    public static Method valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    private final int index;
    private final int value;
    private Method(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    static {
      com.lab616.ib.api.proto.TWSProto.getDescriptor();
    }
  }
  
  public static final class Field extends
      com.google.protobuf.GeneratedMessage {
    // Use Field.newBuilder() to construct.
    private Field() {}
    
    private static final Field defaultInstance = new Field();
    public static Field getDefaultInstance() {
      return defaultInstance;
    }
    
    public Field getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.lab616.ib.api.proto.TWSProto.internal_static_ib_Field_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.lab616.ib.api.proto.TWSProto.internal_static_ib_Field_fieldAccessorTable;
    }
    
    // optional double doubleValue = 1;
    public static final int DOUBLEVALUE_FIELD_NUMBER = 1;
    private boolean hasDoubleValue;
    private double doubleValue_ = 0D;
    public boolean hasDoubleValue() { return hasDoubleValue; }
    public double getDoubleValue() { return doubleValue_; }
    
    // optional int32 intValue = 2;
    public static final int INTVALUE_FIELD_NUMBER = 2;
    private boolean hasIntValue;
    private int intValue_ = 0;
    public boolean hasIntValue() { return hasIntValue; }
    public int getIntValue() { return intValue_; }
    
    // optional string stringValue = 3;
    public static final int STRINGVALUE_FIELD_NUMBER = 3;
    private boolean hasStringValue;
    private java.lang.String stringValue_ = "";
    public boolean hasStringValue() { return hasStringValue; }
    public java.lang.String getStringValue() { return stringValue_; }
    
    // optional int64 longValue = 4;
    public static final int LONGVALUE_FIELD_NUMBER = 4;
    private boolean hasLongValue;
    private long longValue_ = 0L;
    public boolean hasLongValue() { return hasLongValue; }
    public long getLongValue() { return longValue_; }
    
    // optional bool booleanValue = 5;
    public static final int BOOLEANVALUE_FIELD_NUMBER = 5;
    private boolean hasBooleanValue;
    private boolean booleanValue_ = false;
    public boolean hasBooleanValue() { return hasBooleanValue; }
    public boolean getBooleanValue() { return booleanValue_; }
    
    public final boolean isInitialized() {
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (hasDoubleValue()) {
        output.writeDouble(1, getDoubleValue());
      }
      if (hasIntValue()) {
        output.writeInt32(2, getIntValue());
      }
      if (hasStringValue()) {
        output.writeString(3, getStringValue());
      }
      if (hasLongValue()) {
        output.writeInt64(4, getLongValue());
      }
      if (hasBooleanValue()) {
        output.writeBool(5, getBooleanValue());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasDoubleValue()) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(1, getDoubleValue());
      }
      if (hasIntValue()) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, getIntValue());
      }
      if (hasStringValue()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(3, getStringValue());
      }
      if (hasLongValue()) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(4, getLongValue());
      }
      if (hasBooleanValue()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(5, getBooleanValue());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Field parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.lab616.ib.api.proto.TWSProto.Field prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private com.lab616.ib.api.proto.TWSProto.Field result;
      
      // Construct using com.lab616.ib.api.proto.TWSProto.Field.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new com.lab616.ib.api.proto.TWSProto.Field();
        return builder;
      }
      
      protected com.lab616.ib.api.proto.TWSProto.Field internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new com.lab616.ib.api.proto.TWSProto.Field();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.lab616.ib.api.proto.TWSProto.Field.getDescriptor();
      }
      
      public com.lab616.ib.api.proto.TWSProto.Field getDefaultInstanceForType() {
        return com.lab616.ib.api.proto.TWSProto.Field.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public com.lab616.ib.api.proto.TWSProto.Field build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private com.lab616.ib.api.proto.TWSProto.Field buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.lab616.ib.api.proto.TWSProto.Field buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        com.lab616.ib.api.proto.TWSProto.Field returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.lab616.ib.api.proto.TWSProto.Field) {
          return mergeFrom((com.lab616.ib.api.proto.TWSProto.Field)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.lab616.ib.api.proto.TWSProto.Field other) {
        if (other == com.lab616.ib.api.proto.TWSProto.Field.getDefaultInstance()) return this;
        if (other.hasDoubleValue()) {
          setDoubleValue(other.getDoubleValue());
        }
        if (other.hasIntValue()) {
          setIntValue(other.getIntValue());
        }
        if (other.hasStringValue()) {
          setStringValue(other.getStringValue());
        }
        if (other.hasLongValue()) {
          setLongValue(other.getLongValue());
        }
        if (other.hasBooleanValue()) {
          setBooleanValue(other.getBooleanValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                return this;
              }
              break;
            }
            case 9: {
              setDoubleValue(input.readDouble());
              break;
            }
            case 16: {
              setIntValue(input.readInt32());
              break;
            }
            case 26: {
              setStringValue(input.readString());
              break;
            }
            case 32: {
              setLongValue(input.readInt64());
              break;
            }
            case 40: {
              setBooleanValue(input.readBool());
              break;
            }
          }
        }
      }
      
      
      // optional double doubleValue = 1;
      public boolean hasDoubleValue() {
        return result.hasDoubleValue();
      }
      public double getDoubleValue() {
        return result.getDoubleValue();
      }
      public Builder setDoubleValue(double value) {
        result.hasDoubleValue = true;
        result.doubleValue_ = value;
        return this;
      }
      public Builder clearDoubleValue() {
        result.hasDoubleValue = false;
        result.doubleValue_ = 0D;
        return this;
      }
      
      // optional int32 intValue = 2;
      public boolean hasIntValue() {
        return result.hasIntValue();
      }
      public int getIntValue() {
        return result.getIntValue();
      }
      public Builder setIntValue(int value) {
        result.hasIntValue = true;
        result.intValue_ = value;
        return this;
      }
      public Builder clearIntValue() {
        result.hasIntValue = false;
        result.intValue_ = 0;
        return this;
      }
      
      // optional string stringValue = 3;
      public boolean hasStringValue() {
        return result.hasStringValue();
      }
      public java.lang.String getStringValue() {
        return result.getStringValue();
      }
      public Builder setStringValue(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasStringValue = true;
        result.stringValue_ = value;
        return this;
      }
      public Builder clearStringValue() {
        result.hasStringValue = false;
        result.stringValue_ = getDefaultInstance().getStringValue();
        return this;
      }
      
      // optional int64 longValue = 4;
      public boolean hasLongValue() {
        return result.hasLongValue();
      }
      public long getLongValue() {
        return result.getLongValue();
      }
      public Builder setLongValue(long value) {
        result.hasLongValue = true;
        result.longValue_ = value;
        return this;
      }
      public Builder clearLongValue() {
        result.hasLongValue = false;
        result.longValue_ = 0L;
        return this;
      }
      
      // optional bool booleanValue = 5;
      public boolean hasBooleanValue() {
        return result.hasBooleanValue();
      }
      public boolean getBooleanValue() {
        return result.getBooleanValue();
      }
      public Builder setBooleanValue(boolean value) {
        result.hasBooleanValue = true;
        result.booleanValue_ = value;
        return this;
      }
      public Builder clearBooleanValue() {
        result.hasBooleanValue = false;
        result.booleanValue_ = false;
        return this;
      }
    }
    
    static {
      com.lab616.ib.api.proto.TWSProto.getDescriptor();
    }
    
    static {
      com.lab616.ib.api.proto.TWSProto.internalForceInit();
    }
  }
  
  public static final class Event extends
      com.google.protobuf.GeneratedMessage {
    // Use Event.newBuilder() to construct.
    private Event() {}
    
    private static final Event defaultInstance = new Event();
    public static Event getDefaultInstance() {
      return defaultInstance;
    }
    
    public Event getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.lab616.ib.api.proto.TWSProto.internal_static_ib_Event_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.lab616.ib.api.proto.TWSProto.internal_static_ib_Event_fieldAccessorTable;
    }
    
    // required fixed64 timestamp = 1;
    public static final int TIMESTAMP_FIELD_NUMBER = 1;
    private boolean hasTimestamp;
    private long timestamp_ = 0L;
    public boolean hasTimestamp() { return hasTimestamp; }
    public long getTimestamp() { return timestamp_; }
    
    // required .ib.Method method = 2;
    public static final int METHOD_FIELD_NUMBER = 2;
    private boolean hasMethod;
    private com.lab616.ib.api.proto.TWSProto.Method method_ = com.lab616.ib.api.proto.TWSProto.Method.tickGeneric;
    public boolean hasMethod() { return hasMethod; }
    public com.lab616.ib.api.proto.TWSProto.Method getMethod() { return method_; }
    
    // repeated .ib.Field fields = 3;
    public static final int FIELDS_FIELD_NUMBER = 3;
    private java.util.List<com.lab616.ib.api.proto.TWSProto.Field> fields_ =
      java.util.Collections.emptyList();
    public java.util.List<com.lab616.ib.api.proto.TWSProto.Field> getFieldsList() {
      return fields_;
    }
    public int getFieldsCount() { return fields_.size(); }
    public com.lab616.ib.api.proto.TWSProto.Field getFields(int index) {
      return fields_.get(index);
    }
    
    // required string source = 4;
    public static final int SOURCE_FIELD_NUMBER = 4;
    private boolean hasSource;
    private java.lang.String source_ = "";
    public boolean hasSource() { return hasSource; }
    public java.lang.String getSource() { return source_; }
    
    public final boolean isInitialized() {
      if (!hasTimestamp) return false;
      if (!hasMethod) return false;
      if (!hasSource) return false;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (hasTimestamp()) {
        output.writeFixed64(1, getTimestamp());
      }
      if (hasMethod()) {
        output.writeEnum(2, getMethod().getNumber());
      }
      for (com.lab616.ib.api.proto.TWSProto.Field element : getFieldsList()) {
        output.writeMessage(3, element);
      }
      if (hasSource()) {
        output.writeString(4, getSource());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasTimestamp()) {
        size += com.google.protobuf.CodedOutputStream
          .computeFixed64Size(1, getTimestamp());
      }
      if (hasMethod()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(2, getMethod().getNumber());
      }
      for (com.lab616.ib.api.proto.TWSProto.Field element : getFieldsList()) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(3, element);
      }
      if (hasSource()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(4, getSource());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.lab616.ib.api.proto.TWSProto.Event parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.lab616.ib.api.proto.TWSProto.Event prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private com.lab616.ib.api.proto.TWSProto.Event result;
      
      // Construct using com.lab616.ib.api.proto.TWSProto.Event.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new com.lab616.ib.api.proto.TWSProto.Event();
        return builder;
      }
      
      protected com.lab616.ib.api.proto.TWSProto.Event internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new com.lab616.ib.api.proto.TWSProto.Event();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.lab616.ib.api.proto.TWSProto.Event.getDescriptor();
      }
      
      public com.lab616.ib.api.proto.TWSProto.Event getDefaultInstanceForType() {
        return com.lab616.ib.api.proto.TWSProto.Event.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public com.lab616.ib.api.proto.TWSProto.Event build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private com.lab616.ib.api.proto.TWSProto.Event buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.lab616.ib.api.proto.TWSProto.Event buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        if (result.fields_ != java.util.Collections.EMPTY_LIST) {
          result.fields_ =
            java.util.Collections.unmodifiableList(result.fields_);
        }
        com.lab616.ib.api.proto.TWSProto.Event returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.lab616.ib.api.proto.TWSProto.Event) {
          return mergeFrom((com.lab616.ib.api.proto.TWSProto.Event)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.lab616.ib.api.proto.TWSProto.Event other) {
        if (other == com.lab616.ib.api.proto.TWSProto.Event.getDefaultInstance()) return this;
        if (other.hasTimestamp()) {
          setTimestamp(other.getTimestamp());
        }
        if (other.hasMethod()) {
          setMethod(other.getMethod());
        }
        if (!other.fields_.isEmpty()) {
          if (result.fields_.isEmpty()) {
            result.fields_ = new java.util.ArrayList<com.lab616.ib.api.proto.TWSProto.Field>();
          }
          result.fields_.addAll(other.fields_);
        }
        if (other.hasSource()) {
          setSource(other.getSource());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                return this;
              }
              break;
            }
            case 9: {
              setTimestamp(input.readFixed64());
              break;
            }
            case 16: {
              int rawValue = input.readEnum();
              com.lab616.ib.api.proto.TWSProto.Method value = com.lab616.ib.api.proto.TWSProto.Method.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(2, rawValue);
              } else {
                setMethod(value);
              }
              break;
            }
            case 26: {
              com.lab616.ib.api.proto.TWSProto.Field.Builder subBuilder = com.lab616.ib.api.proto.TWSProto.Field.newBuilder();
              input.readMessage(subBuilder, extensionRegistry);
              addFields(subBuilder.buildPartial());
              break;
            }
            case 34: {
              setSource(input.readString());
              break;
            }
          }
        }
      }
      
      
      // required fixed64 timestamp = 1;
      public boolean hasTimestamp() {
        return result.hasTimestamp();
      }
      public long getTimestamp() {
        return result.getTimestamp();
      }
      public Builder setTimestamp(long value) {
        result.hasTimestamp = true;
        result.timestamp_ = value;
        return this;
      }
      public Builder clearTimestamp() {
        result.hasTimestamp = false;
        result.timestamp_ = 0L;
        return this;
      }
      
      // required .ib.Method method = 2;
      public boolean hasMethod() {
        return result.hasMethod();
      }
      public com.lab616.ib.api.proto.TWSProto.Method getMethod() {
        return result.getMethod();
      }
      public Builder setMethod(com.lab616.ib.api.proto.TWSProto.Method value) {
        if (value == null) {
          throw new NullPointerException();
        }
        result.hasMethod = true;
        result.method_ = value;
        return this;
      }
      public Builder clearMethod() {
        result.hasMethod = false;
        result.method_ = com.lab616.ib.api.proto.TWSProto.Method.tickGeneric;
        return this;
      }
      
      // repeated .ib.Field fields = 3;
      public java.util.List<com.lab616.ib.api.proto.TWSProto.Field> getFieldsList() {
        return java.util.Collections.unmodifiableList(result.fields_);
      }
      public int getFieldsCount() {
        return result.getFieldsCount();
      }
      public com.lab616.ib.api.proto.TWSProto.Field getFields(int index) {
        return result.getFields(index);
      }
      public Builder setFields(int index, com.lab616.ib.api.proto.TWSProto.Field value) {
        if (value == null) {
          throw new NullPointerException();
        }
        result.fields_.set(index, value);
        return this;
      }
      public Builder setFields(int index, com.lab616.ib.api.proto.TWSProto.Field.Builder builderForValue) {
        result.fields_.set(index, builderForValue.build());
        return this;
      }
      public Builder addFields(com.lab616.ib.api.proto.TWSProto.Field value) {
        if (value == null) {
          throw new NullPointerException();
        }
        if (result.fields_.isEmpty()) {
          result.fields_ = new java.util.ArrayList<com.lab616.ib.api.proto.TWSProto.Field>();
        }
        result.fields_.add(value);
        return this;
      }
      public Builder addFields(com.lab616.ib.api.proto.TWSProto.Field.Builder builderForValue) {
        if (result.fields_.isEmpty()) {
          result.fields_ = new java.util.ArrayList<com.lab616.ib.api.proto.TWSProto.Field>();
        }
        result.fields_.add(builderForValue.build());
        return this;
      }
      public Builder addAllFields(
          java.lang.Iterable<? extends com.lab616.ib.api.proto.TWSProto.Field> values) {
        if (result.fields_.isEmpty()) {
          result.fields_ = new java.util.ArrayList<com.lab616.ib.api.proto.TWSProto.Field>();
        }
        super.addAll(values, result.fields_);
        return this;
      }
      public Builder clearFields() {
        result.fields_ = java.util.Collections.emptyList();
        return this;
      }
      
      // required string source = 4;
      public boolean hasSource() {
        return result.hasSource();
      }
      public java.lang.String getSource() {
        return result.getSource();
      }
      public Builder setSource(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasSource = true;
        result.source_ = value;
        return this;
      }
      public Builder clearSource() {
        result.hasSource = false;
        result.source_ = getDefaultInstance().getSource();
        return this;
      }
    }
    
    static {
      com.lab616.ib.api.proto.TWSProto.getDescriptor();
    }
    
    static {
      com.lab616.ib.api.proto.TWSProto.internalForceInit();
    }
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_ib_Field_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_ib_Field_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_ib_Event_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_ib_Event_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014domain.proto\022\002ib\"l\n\005Field\022\023\n\013doubleVal" +
      "ue\030\001 \001(\001\022\020\n\010intValue\030\002 \001(\005\022\023\n\013stringValu" +
      "e\030\003 \001(\t\022\021\n\tlongValue\030\004 \001(\003\022\024\n\014booleanVal" +
      "ue\030\005 \001(\010\"a\n\005Event\022\021\n\ttimestamp\030\001 \002(\006\022\032\n\006" +
      "method\030\002 \002(\0162\n.ib.Method\022\031\n\006fields\030\003 \003(\013" +
      "2\t.ib.Field\022\016\n\006source\030\004 \002(\t*\301\001\n\006Method\022\017" +
      "\n\013tickGeneric\020\000\022\031\n\025tickOptionComputation" +
      "\020\001\022\r\n\ttickPrice\020\002\022\014\n\010tickSize\020\003\022\016\n\ntickS" +
      "tring\020\004\022\017\n\013realtimeBar\020\005\022\022\n\016updateMktDep" +
      "th\020\006\022\024\n\020updateMktDepthL2\020\007\022\017\n\013currentTim",
      "e\020\010\022\022\n\016historicalData\020\tB#\n\027com.lab616.ib" +
      ".api.protoB\010TWSProto"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_ib_Field_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_ib_Field_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_ib_Field_descriptor,
              new java.lang.String[] { "DoubleValue", "IntValue", "StringValue", "LongValue", "BooleanValue", },
              com.lab616.ib.api.proto.TWSProto.Field.class,
              com.lab616.ib.api.proto.TWSProto.Field.Builder.class);
          internal_static_ib_Event_descriptor =
            getDescriptor().getMessageTypes().get(1);
          internal_static_ib_Event_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_ib_Event_descriptor,
              new java.lang.String[] { "Timestamp", "Method", "Fields", "Source", },
              com.lab616.ib.api.proto.TWSProto.Event.class,
              com.lab616.ib.api.proto.TWSProto.Event.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  public static void internalForceInit() {}
}
