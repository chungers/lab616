#ifndef VARZ_H_
#define VARZ_H_

#include <string>
#include <vector>
#include <stdint.h>             // the normal place uint16_t is defined
#include <sys/types.h>          // the normal place u_int16_t is defined
#include <inttypes.h>           // a third place for uint16_t or u_int16_t

namespace lab616 {

// C99 format
typedef int32_t int32;
typedef uint32_t uint32;
typedef int64_t int64;
typedef uint64_t uint64;

struct VarzInfo {
  std::string name;           // the name of the varz
  std::string type;           // the type of the varz: int32, etc
  std::string description;    // the "help text" associated with the varz
  std::string filename;       // 'cleaned' version of filename holding the flag
  std::string current_value;
  std::string initial_value;
};


extern void GetAllVarzs(std::vector<VarzInfo>* OUTPUT);

// A varz lives in its own namespace.  It is purposefully
// named in an opaque way that people should have trouble typing
// directly.  The idea is that DEFINE puts the flag in the weird
// namespace, and DECLARE imports the flag from there into the current
// namespace.  The net result is to force people to use DECLARE to get
// access to a flag, rather than saying "extern bool VARZ_whatever;"
// or some such instead.  We want this so we can put extra
// functionality (like sanity-checking) in DECLARE if we want, and
// make sure it is picked up everywhere.
//
// We also put the type of the variable in the namespace, so that
// people can't DECLARE_VARZ_int32 something that they DEFINE_VARZ_bool'd
// elsewhere.
class VarzRegisterer {
 public:
  VarzRegisterer(const char* name, const char* type,
                 const char* help, const char* filename,
                 void* current_storage, void* defvalue_storage);
};

// Each Varz has variables associated with it: one  with the current value,
// and one with the initial / unset value.
// We have a third variable, which is where value is assigned; it's a
// constant.  This guarantees that VARZ_##value is initialized at
// static initialization time (e.g. before program-start) rather than
// than global construction time (which is after program-start but
// before main), at least when 'value' is a compile-time constant.  We
// use a small trick for the "default value" variable, and call it
// VARZ_no<name>.  This serves the second purpose of assuring a
// compile error if someone tries to define a varz named no<name>
// which is illegal (foo and nofoo both affect the "foo" varz).
#define DEFINE_VARZ(type, shorttype, name, value, help) \
  namespace vARZ##shorttype {                                      \
    static const type VARZ_nono##name = value;                   \
    type VARZ_##name = VARZ_nono##name;                          \
    type VARZ_no##name = VARZ_nono##name;                       \
    static ::lab616::VarzRegisterer o_##name(                   \
        #name, #type, help, __FILE__,                           \
        &VARZ_##name, &VARZ_no##name);                          \
  }                                                             \
  using vARZ##shorttype::VARZ_##name

#define DECLARE_VARZ(type, shorttype, name)       \
  namespace vARZ##shorttype {                     \
    extern type VARZ_##name;                      \
  }                                               \
  using vARZ##shorttype::VARZ_##name

// For DEFINE_bool, we want to do the extra check that the passed-in
// value is actually a bool, and not a string or something that can be
// coerced to a bool.  These declarations (no definition needed!) will
// help us do that, and never evaluate From, which is important.
// We'll use 'sizeof(IsBool(val))' to distinguish. This code requires
// that the compiler have different sizes for bool & double. Since
// this is not guaranteed by the standard, we check it with a
// compile-time assert (msg[-1] will give a compile-time error).
namespace vARZB {
struct CompileAssert {};
typedef CompileAssert expected_sizeof_double_neq_sizeof_bool[
                      (sizeof(double) != sizeof(bool)) ? 1 : -1];
template<typename From> double IsBoolFlag(const From& from);
bool IsBoolFlag(bool from);
}  // namespace vARZB

#define DECLARE_VARZ_bool(name)          DECLARE_VARZ(bool, B, name)
#define DEFINE_VARZ_bool(name, val, txt)                                       \
  namespace vARZB {                                                         \
    typedef CompileAssert VARZ_##name##_value_is_not_a_bool[              \
            (sizeof(::vARZB::IsBoolFlag(val)) != sizeof(double)) ? 1 : -1]; \
  }                                                                       \
  DEFINE_VARZ(bool, B, name, val, txt)

#define DECLARE_VARZ_int32(name)    DECLARE_VARZ(::lab616::int32, I, name)
#define DEFINE_VARZ_int32(name,val,txt)  DEFINE_VARZ(::lab616::int32, I, name, val, txt)

#define DECLARE_VARZ_int64(name)    DECLARE_VARZ(::lab616::int64, I64, name)
#define DEFINE_VARZ_int64(name,val,txt)  DEFINE_VARZ(::lab616::int64, I64, name, val, txt)

#define DECLARE_VARZ_uint64(name)        DECLARE_VARZ(::lab616::uint64, U64, name)
#define DEFINE_VARZ_uint64(name,val,txt) DEFINE_VARZ(::lab616::uint64, U64, name, val, txt)

#define DECLARE_VARZ_double(name)          DECLARE_VARZ(double, D, name)
#define DEFINE_VARZ_double(name, val, txt) DEFINE_VARZ(double, D, name, val, txt)

// Strings are trickier, because they're not a POD, so we can't
// construct them at static-initialization time (instead they get
// constructed at global-constructor time, which is much later).  To
// try to avoid crashes in that case, we use a char buffer to store
// the string, which we can static-initialize, and then placement-new
// into it later.  It's not perfect, but the best we can do.
#define DECLARE_VARZ_string(name)  namespace vARZS { extern std::string& VARZ_##name; } \
                              using vARZS::VARZ_##name

// We need to define a var named VARZ_no##name so people don't define
// --string and --nostring.  And we need a temporary place to put val
// so we don't have to evaluate it twice.  Two great needs that go
// great together!
// The weird 'using' + 'extern' inside the fLS namespace is to work around
// an unknown compiler bug/issue with the gcc 4.2.1 on SUSE 10.  See
//    http://code.google.com/p/google-gflags/issues/detail?id=20
#define DEFINE_VARZ_string(name, val, txt)                                     \
  namespace vARZS {                                                         \
    static union { void* align; char s[sizeof(std::string)]; } s_##name[2]; \
    const std::string* const VARZ_no##name = new (s_##name[0].s) std::string(val); \
    static ::lab616::VarzRegisterer o_##name(                \
      #name, "string", txt, __FILE__,                \
      s_##name[0].s, new (s_##name[1].s) std::string(*VARZ_no##name));   \
    extern std::string& VARZ_##name;                                     \
    using vARZS::VARZ_##name;                                              \
    std::string& VARZ_##name = *(reinterpret_cast<std::string*>(s_##name[0].s));   \
  }                                                                       \
  using vARZS::VARZ_##name




} // namespace lab616

#endif  // VARZ_H_
