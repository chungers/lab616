// 2010 lab616.com

#ifndef IB_BRIDGE_H_
#define IB_BRIDGE_H_

namespace ib {
namespace bridge {

enum ApiVersion {
  V964_BETA,
  V964
};

class Bridge {
 public:
  virtual ~Bridge();
  virtual ApiVersion get_api_version() const;
};

}
}
#endif // IB_BRIDGE_H
