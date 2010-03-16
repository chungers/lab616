CXX=g++
CXXFLAGS=-DIB_USE_STD_STRING -Wall -Wno-switch -g -O2
ROOT_DIR=./src/api
BASE_SRC_DIR=${ROOT_DIR}/PosixSocketClient
TEST_SRC_DIR=${ROOT_DIR}/PosixSocketClientTest
BUILD_DIR=./build
INCLUDES=-I${ROOT_DIR}/Shared/ -I${BASE_SRC_DIR}
TARGET=PosixSocketClientTest

$(TARGET):
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $(BUILD_DIR)/EClientSocketBase.o -c $(BASE_SRC_DIR)/EClientSocketBase.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $(BUILD_DIR)/EPosixClientSocket.o -c $(BASE_SRC_DIR)/EPosixClientSocket.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $(BUILD_DIR)/PosixTestClient.o -c $(TEST_SRC_DIR)/PosixTestClient.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $(BUILD_DIR)/Main.o -c $(TEST_SRC_DIR)/Main.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $@ \
		$(BUILD_DIR)/EClientSocketBase.o \
		$(BUILD_DIR)/EPosixClientSocket.o \
		$(BUILD_DIR)/PosixTestClient.o \
		$(BUILD_DIR)/Main.o

clean:
	rm -f $(TARGET) $(BUILD_DIR)/*.o

