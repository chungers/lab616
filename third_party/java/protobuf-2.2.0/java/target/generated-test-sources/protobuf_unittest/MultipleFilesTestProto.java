// Generated by the protocol buffer compiler.  DO NOT EDIT!

package protobuf_unittest;

public final class MultipleFilesTestProto {
  private MultipleFilesTestProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registry.add(protobuf_unittest.MultipleFilesTestProto.extensionWithOuter);
  }
  public static final int EXTENSION_WITH_OUTER_FIELD_NUMBER = 1234567;
  public static
    com.google.protobuf.GeneratedMessage.GeneratedExtension<
      protobuf_unittest.UnittestProto.TestAllExtensions,
      java.lang.Integer> extensionWithOuter;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_protobuf_unittest_MessageWithNoOuter_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_protobuf_unittest_MessageWithNoOuter_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_protobuf_unittest_MessageWithNoOuter_NestedMessage_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_protobuf_unittest_MessageWithNoOuter_NestedMessage_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n-com/google/protobuf/multiple_files_tes" +
      "t.proto\022\021protobuf_unittest\032\036google/proto" +
      "buf/unittest.proto\"\277\002\n\022MessageWithNoOute" +
      "r\022C\n\006nested\030\001 \001(\01323.protobuf_unittest.Me" +
      "ssageWithNoOuter.NestedMessage\0220\n\007foreig" +
      "n\030\002 \003(\0132\037.protobuf_unittest.TestAllTypes" +
      "\022E\n\013nested_enum\030\003 \001(\01620.protobuf_unittes" +
      "t.MessageWithNoOuter.NestedEnum\0228\n\014forei" +
      "gn_enum\030\004 \001(\0162\".protobuf_unittest.EnumWi" +
      "thNoOuter\032\032\n\rNestedMessage\022\t\n\001i\030\001 \001(\005\"\025\n",
      "\nNestedEnum\022\007\n\003BAZ\020\003*#\n\017EnumWithNoOuter\022" +
      "\007\n\003FOO\020\001\022\007\n\003BAR\020\0022c\n\022ServiceWithNoOuter\022" +
      "M\n\003Foo\022%.protobuf_unittest.MessageWithNo" +
      "Outer\032\037.protobuf_unittest.TestAllTypes:D" +
      "\n\024extension_with_outer\022$.protobuf_unitte" +
      "st.TestAllExtensions\030\207\255K \001(\005B\032B\026Multiple" +
      "FilesTestProtoP\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_protobuf_unittest_MessageWithNoOuter_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_protobuf_unittest_MessageWithNoOuter_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_protobuf_unittest_MessageWithNoOuter_descriptor,
              new java.lang.String[] { "Nested", "Foreign", "NestedEnum", "ForeignEnum", },
              protobuf_unittest.MessageWithNoOuter.class,
              protobuf_unittest.MessageWithNoOuter.Builder.class);
          internal_static_protobuf_unittest_MessageWithNoOuter_NestedMessage_descriptor =
            internal_static_protobuf_unittest_MessageWithNoOuter_descriptor.getNestedTypes().get(0);
          internal_static_protobuf_unittest_MessageWithNoOuter_NestedMessage_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_protobuf_unittest_MessageWithNoOuter_NestedMessage_descriptor,
              new java.lang.String[] { "I", },
              protobuf_unittest.MessageWithNoOuter.NestedMessage.class,
              protobuf_unittest.MessageWithNoOuter.NestedMessage.Builder.class);
          protobuf_unittest.MultipleFilesTestProto.extensionWithOuter =
            com.google.protobuf.GeneratedMessage.newGeneratedExtension(
              protobuf_unittest.MultipleFilesTestProto.getDescriptor().getExtensions().get(0),
              java.lang.Integer.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          protobuf_unittest.UnittestProto.getDescriptor(),
        }, assigner);
  }
  
  public static void internalForceInit() {}
}