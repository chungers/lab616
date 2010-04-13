# //cpp-ib/test/ib/mocks/CMakeLists.txt

build_tests()

# Process variables in headers with values set in top level cmake file:
configure_file(
  MockEWrapper.h.in
  ${GEN_DIR}/ib/mocks/MockEWrapper.h
)
install(FILES ${GEN_DIR}/ib/mocks/MockEWrapper.h DESTINATION ${INCLUDE_DIR})

configure_file(
  MockEClient.h.in
  ${GEN_DIR}/ib/mocks/MockEClient.h
)
install(FILES ${GEN_DIR}/ib/mocks/MockEClient.h DESTINATION ${INCLUDE_DIR})

		
