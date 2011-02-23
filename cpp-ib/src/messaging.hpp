#ifndef MESSAGING_H_
#define MESSAGING_H_

#include <glog/logging.h>
#include <zmq.hpp>
#include <vector>

namespace lab616 {
namespace messaging {

inline static bool frame(zmq::socket_t & socket, const std::string & string, bool last) {
  zmq::message_t message(string.size());
  memcpy(message.data(), string.data(), string.size());
  bool rc = socket.send(message, last ? 0 : ZMQ_SNDMORE);
  return (rc);
}

inline static bool frame(zmq::socket_t & socket, const std::string & string) {
  return frame(socket, string, false);
}

inline static bool last(zmq::socket_t & socket, const std::string & string) {
  return frame(socket, string, true);
}

template <typename T>
inline static bool frame(zmq::socket_t & socket, const T& data, bool last) {
  zmq::message_t message(sizeof(T));
  memcpy(message.data(), reinterpret_cast<const void*>(&data), sizeof(T));
  bool rc = socket.send(message, last ? 0 : ZMQ_SNDMORE);
  return (rc);
}

template <typename T>
inline static bool frame(zmq::socket_t & socket, const T& data) {
  return frame(socket, data, false);
}

template <typename T>
inline static bool last(zmq::socket_t & socket, const T& data) {
  return frame(socket, data, true);
}

template <typename T>
inline static bool receive(zmq::socket_t & socket, T& output) {
  zmq::message_t message;
  socket.recv(&message);
  memcpy(reinterpret_cast<void *>(&output), message.data(), sizeof(T));
  int64_t more;           //  Multipart detection
  size_t more_size = sizeof (more);
  socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
  VLOG(20) << '[' << output << '/' << more << ']' << std::endl;
  return more;
}

inline static bool receive(zmq::socket_t & socket, std::string* output) {
  zmq::message_t message;
  socket.recv(&message);
  output->assign(static_cast<char*>(message.data()), message.size());
  int64_t more;           //  Multipart detection
  size_t more_size = sizeof (more);
  socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
  VLOG(20) << '"' << *output << '/' << more << '"' << std::endl;
  return more;
}

/**
 * Message class
 * - Simple class for sending multi-part messages.
 * - Call add() repeatedly and followed by a send().
 */
class Message {
 public:
  Message() { }
  ~Message() {
    std::vector<zmq::message_t*>::iterator m;
    for (m = m_messages.begin(); m != m_messages.end(); ++m) {
      delete *m;
    }
  }

  void add(const std::string& string) {
    zmq::message_t* message = new zmq::message_t(string.size());
    memcpy(message->data(), string.data(), string.size());
    m_messages.push_back(message);
  }

  template <typename T> void add(const T& data) {
    zmq::message_t* message = new zmq::message_t(sizeof(T));
    memcpy(message->data(), reinterpret_cast<const void*>(&data), sizeof(T));
    m_messages.push_back(message);
  }

  void send(zmq::socket_t& socket) {
    for (unsigned i = 0; i < m_messages.size(); ++i) {
      zmq::message_t* m = m_messages[i];
      if (i == (m_messages.size() - 1)) {
        VLOG(30) << "L" << i << ": " << m->data() << std::endl;
        socket.send(*m, 0);
      } else {
        VLOG(30) << "F" << i << ": " << m->data() << std::endl;
        socket.send(*m, ZMQ_SNDMORE);
      }
    }
  }

 private:
  std::vector<zmq::message_t*> m_messages;

}; // Message

} // messaging
} // lab616


#endif // MESSAGING_H_
