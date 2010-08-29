
#include <vector>
#include <iostream>
#include <fastflow/farm.hpp>

#include <boost/thread.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/asio.hpp>

#include <gmock/gmock.h>
#include <gtest/gtest.h>


using namespace ff;
using boost::asio::ip::tcp;

static boost::mutex cout_mutex;

const int kBufSz      = 1024;
const int kSockQ      = 32;
const int kMaxMsgSz   = 32;
const int kYes        = 1;
const char kDelimiter = '\n';

// thread opens socket to localhost on specified port and sends specified
//   # of msgs then exits
class ClientThread {
 public:
  // raii ctor
  ClientThread(int msgs,int port):msgs_(msgs) {
    done_ = false;
    socket_ = boost::shared_ptr<tcp::socket>(new tcp::socket(io_svc_));
    boost::system::error_code error = boost::asio::error::host_not_found;
    const tcp::endpoint ep = tcp::endpoint
        (boost::asio::ip::address::from_string("127.0.0.1"),port);
    std::cout << ep << std::endl;
    socket_->connect(ep);
    thread_ = boost::shared_ptr<boost::thread>
        (new boost::thread(boost::bind(&ClientThread::send_msgs, this)));
  }

  ~ClientThread() { }

  void send_msgs () {
    int i = 0;
    std::cout << "sending msgs!" << std::endl;
    while(!done_) {
      std::stringstream ss;
      ss << i << kDelimiter;
      const char* msg = ss.str().c_str();
      size_t mlen = strlen(msg);
      boost::asio::write(*socket_, boost::asio::buffer(msg, mlen));
      if (++i >= msgs_) {
        std::cout << "ClientThread: done.\n";
        done_ = true;
        socket_->close();
      }
    }
  }

  void join() { if (thread_) thread_->join(); }

 private:

  int msgs_;
  volatile bool done_;
  boost::shared_ptr<boost::thread> thread_;
  boost::shared_ptr<tcp::socket> socket_;

  static boost::asio::io_service io_svc_;
}; // ClientThread

// init static member
boost::asio::io_service ClientThread::io_svc_;

// union for dealing with sockaddrs
//
typedef union address {
  struct sockaddr sa;
  struct sockaddr_in sa_in;
  struct sockaddr_in6 sa_in6;
  struct sockaddr_storage sa_stor;
} address_t;

// Sits on server socket, and uses select to read incoming messages;
//   these are turned into 'tasks' which are pushed into the fastflow
//   'network'
//
class SelectReader : public ff_node {
 public:

  // ctor initializes fd sets
  SelectReader(int port):port_(port){
    FD_ZERO(&master_);
    FD_ZERO(&read_fds_);
    ready_ = false;
  }

  ~SelectReader() {}//todo

  // initializes svc; non-zero return is bad...
  int svc_init() {

    int result = 0;
    int rv;
    char prt[16];
    struct addrinfo hints, *ai, *p;

    memset(&hints, 0, sizeof hints);
    hints.ai_family   = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags    = AI_PASSIVE;
    snprintf(prt,sizeof(p), "%d",port_);

    if ((rv = getaddrinfo(NULL, prt, &hints, &ai)) != 0) {
      std::cerr <<  "SelectReader: " << gai_strerror(rv) << "\n";
      return rv;
    }

    // get socket and bind it
    for(p = ai; p != NULL; p = p->ai_next) {

      listener_ = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
      if (listener_ < 0) { continue; }

      setsockopt(listener_, SOL_SOCKET, SO_REUSEADDR, &kYes, sizeof(int));
      if (bind(listener_, p->ai_addr, p->ai_addrlen) < 0) {
        close(listener_);
        continue;
      }
      break;
    } // for
    freeaddrinfo(ai);

    // listen
    if ((result=listen(listener_, kSockQ)) < 0) {
      perror("listen");
    } else {
      // add the listener to the master set
      FD_SET(listener_, &master_);
      // store max FD
      fdmax_ = listener_;
    }

    printf("SelectReader: initialized(%d)\n",result);
    return result;
  }

  // we listen with select, construct and push 'tasks' into the fastflow
  //  network
  void * svc(void *t) {

    int nbytes;         // chars read
    char buf[kBufSz];   // buffer for client data
    int newfd;          // newly accept()ed socket descriptor
    int result;

    struct sockaddr_storage remoteaddr; // client address
    socklen_t addrlen;

    // main loop
    for(;;) {
      signal_readiness(); // notify blocked threads that we're ready

      read_fds_ = master_;
      if ((result=select(fdmax_+1, &read_fds_, NULL, NULL, NULL)) == -1) {
        perror("select");
        return NULL; //EOS
      }

      for(int i = 0; i <= fdmax_; i++) {   // check connections for data to read
        if (FD_ISSET(i, &read_fds_)) { // we have something to read
          if (i == listener_) {        // handle new connections
            addrlen = sizeof remoteaddr;
            if ((newfd = accept
                 (listener_, (sockaddr *)&remoteaddr, &addrlen)) < 0) {
              perror("accept");
            } else {
              FD_SET(newfd, &master_); // add to master set
              if (newfd > fdmax_) { fdmax_ = newfd; }
              conn_info((address*)&remoteaddr, newfd);
            }
          } else {                     // handle existing conns
            if ((nbytes = recv(i, buf, sizeof buf, 0)) <= 0) {
              if (nbytes != 0) {       // connection closed
                perror("recv");
              } else {
                printf("SelectReader: socket %d closed\n",i);
              }
              close(i);
              FD_CLR(i, &master_);   // remove from master set
              return NULL;           // only listen for 1 client..we're done
            } else {                 // we got some data from a client
              buf[nbytes]='\0';
              new_data(nbytes,buf);
            }
          } // END handle data from client
        } // END got new incoming connection
      } // END looping through file descriptors
    } // END for(;;)

    printf("SelectReader: done reading.\n");
    return NULL;
  }

  // calling thread will block until server is ready
  //   we need to prevent race condition with client
  void wait_til_ready() {
    boost::unique_lock<boost::mutex> lock(mutx_);
    while(!ready_) { cond_.wait(lock); }
  }

 private:

  // ready to go - let other threads know
  void signal_readiness() {
    if (ready_) return;
    printf("ready...");
    boost::lock_guard<boost::mutex> lock(mutx_);
    ready_ = true;
    cond_.notify_all();  //signal our readiness to handle incoming msgs
    printf(" signalled.\n");
  }

  // handle new data coming in
  void new_data(int nbytes, const char* buf) {
    static char remainder[kMaxMsgSz];
    int *t;
    int j = 0;
    int i = 0;
    char msgbuf[kMaxMsgSz];
    for (; i < nbytes; i++) {
      if ( kDelimiter == buf[i] ) {
        int rlen = (j==0) ? strlen(remainder) : 0; // remainder?
        int len = i-j;
        if (rlen>0) strncpy(msgbuf,remainder,rlen);
        int start = (rlen==0) ? 0 : rlen;
        strncpy(&(msgbuf[start]),&(buf[j]),len);
        msgbuf[len+rlen] = '\0';
        t = (int *)malloc(sizeof(int));
        *t = atoi(msgbuf);
        j = i;
        //printf("got %d\n",*t);
        ff_send_out(t);  // push message into farm
      }
    } // for

    int k = j+1;
    if (k < i) { // save 'remainder'
      strncpy(remainder,&(buf[k]),(i-k));
      //      printf("--> '%s'\n",remainder);
    } else remainder[0] = '\0';
  }

  // outputs some info about good con we've made
  void conn_info(const address *ad, int newfd) {
    char ip[INET6_ADDRSTRLEN];
    void * raddr = (ad->sa.sa_family == AF_INET)
        ? (void*)&(ad->sa_in.sin_addr)
        : (void*)&(ad->sa_in6.sin6_addr);
    const char *nm = inet_ntop
        (ad->sa.sa_family, raddr, ip, INET6_ADDRSTRLEN);
    std::cout << "SelectReader: connection from " << nm
              << " on FD#" << newfd << std::endl;
  }

  int              port_;      // port we listen on
  fd_set           master_;    // master file descriptor list
  fd_set           read_fds_;  // temp file descriptor list for select()
  int              fdmax_;     // maximum file descriptor number
  int              listener_;  // listening socket descriptor

  // we use a condition variable to manage race condition btw
  //  server & client
  boost::condition_variable cond_;
  boost::mutex     mutx_;
  bool             ready_;
};

// typical worker: does little ;^p
class Worker: public ff_node {
 public:
  Worker() : last_(-1), invoked_(0) {}

  void * svc(void * task) {
    int * t = (int *)task;
    ++invoked_;

    if (last_ != -1) {
      EXPECT_LT(last_, *t);  // In order
    }
    Debug(t);
    last_ = *t;
    return task;
  }

  int GetInvoked()
  { return invoked_; }

 private:
  int last_;
  int invoked_;

  void Debug(const int* t)
  {
    boost::mutex::scoped_lock l(cout_mutex);
    std::cout << "Worker " << ff_node::get_my_id()
              << " received task [" << *t << "]" << std::endl;
  }
};

// collector that just frees the malloc'ed memory
class Collector: public ff_node
{
 public:
  void * svc(void * task) {
    int * t = (int *)task;
    if (*t == -1) return NULL;
    else free(task);
    return task;
  }
};

class Emitter : public ff_node
{
 public:
  Emitter(int m) : messages_(m), k_(0) {}

  void* svc(void* task)
  {
    if (k_ < messages_) {
      int* t = (int *)malloc(sizeof(int));
      *t = ++k_;
      return t;
    }
    return NULL;
  }

  void* svc0(void* task)
  {
    for (int i = 0; i < messages_; ++i) {
      int* t = (int *)malloc(sizeof(int));
      *t = i;

      ff_send_out(t);

      boost::mutex::scoped_lock l(cout_mutex);
      std::cout << "Emitted " << *t << std::endl;
    }
    return NULL;
  }
 private:
  int messages_;
  int k_;
};


// Disabled because the test hangs randomly.
TEST(FastFlowPrototype, DISABLED_SingleTest)
{
  int nworkers = 4;               // how many workers will recv msgs

  Emitter em(200);
  Collector fc;                   // and a freeing collector
  ff_farm<> farm;                 // and a farm for it to live in
  farm.add_emitter(&em);          // add both to the farm

  std::vector<ff_node *> workers; // build a collection of workers
  for(int i =0; i < nworkers; i++) { workers.push_back(new Worker); }

  farm.add_workers(workers);      // add all workers to the farm

  farm.add_collector(&fc);

  //  ffTime(START_TIME);

  // farm.run();                     // launch the farm
  //  printf("main: started farm.\n");

  // farm.wait();

  sleep(1.0);

  EXPECT_EQ(0, farm.run_and_wait_end());

  // emit some stats
  std::cout << "DONE, time= " << farm.ffTime() << " (ms)\n";
  farm.ffStats(std::cout);
}

// Disabled because this test hangs .
TEST(FastFlowPrototype, DISABLED_ClientServerTest)
{
  int msgs = 1024;
  int port = 9999;

  int nworkers = 5;               // how many workers will recv msgs

  printf("main: sending #%d msgs to port <%d>\n",msgs,port);

  SelectReader sr(port);         // we create 'server'
  Collector fc;           // and a freeing collector
  ff_farm<> farm;                 // and a farm for it to live in
  farm.add_emitter(&sr);          // add both to the farm
  farm.add_collector(&fc);

  std::vector<ff_node *> workers; // build a collection of workers
  for(int i =0; i < nworkers; i++) { workers.push_back(new Worker); }

  farm.add_workers(workers);      // add all workers to the farm

  farm.run();                     // launch the farm
  printf("main: started farm.\n");

  sr.wait_til_ready();            // don't create client til srvr ready

  // create client which will send msgs via socket
  //
  printf("main: creating client...\n");
  ClientThread* client = new ClientThread(msgs,port);
  if (client != NULL) client->join();

  std::cout << "Farm wait.." << std::endl;

  farm.wait();                    // wait for farm to be done its workload

  // emit some stats
  std::cout << "DONE, time= " << farm.ffTime() << " (ms)\n";
  farm.ffStats(std::cout);
}
