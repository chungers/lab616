#include <inttypes.h>
#include <stdio.h>     // for snprintf
#include <stdarg.h> // For va_list and related operations
#include <string.h>
#include <assert.h>
#include <iostream>    // for cerr
#include <string>
#include <map>
#include <vector>
#include <utility>     // for pair<>
#include <algorithm>

#include <boost/algorithm/string.hpp>
#include <boost/thread/mutex.hpp>

#include "common.hpp"
#include "varz.hpp"

#ifndef PATH_SEPARATOR
#define PATH_SEPARATOR  '/'
#endif

#ifndef PRId32
# define PRId32 "d"
#endif
#ifndef PRId64
# define PRId64 "lld"
#endif
#ifndef PRIu64
# define PRIu64 "llu"
#endif

namespace lab616 {
using namespace std;

// --------------------------------------------------------------------
// VarzValue
//    This represent the value a single varz might have.  The major
//    functionality is to convert from a string to an object of a
//    given type, and back.  Thread-compatible.
// --------------------------------------------------------------------

class VarzRegistry;
class VarzValue {
 public:
  VarzValue(void* valbuf, const char* type);
  ~VarzValue();

  string ToString() const;

 private:
  friend class VarzHolder;
  friend class VarzRegistry;     // checks value_buffer_ for varzs_by_ptr_ map

  enum ValueType {FV_BOOL, FV_INT32, FV_INT64, FV_UINT64, FV_DOUBLE, FV_STRING};

  const char* TypeName() const;
  bool Equal(const VarzValue& x) const;
  VarzValue* New() const;   // creates a new one with default value
  void CopyFrom(const VarzValue& x);

  void* value_buffer_;          // points to the buffer holding our data
  ValueType type_;              // how to interpret value_

  VarzValue(const VarzValue&);   // no copying!
  void operator=(const VarzValue&);
};


// This could be a templated method of VarzValue, but doing so adds to the
// size of the .o.  Since there's no type-safety here anyway, macro is ok.
#define VALUE_AS(type)  *reinterpret_cast<type*>(value_buffer_)
#define OTHER_VALUE_AS(fv, type)  *reinterpret_cast<type*>(fv.value_buffer_)
#define SET_VALUE_AS(type, value)  VALUE_AS(type) = (value)

VarzValue::VarzValue(void* valbuf, const char* type) : value_buffer_(valbuf) {
  if      (strcmp(type, "bool") == 0)  type_ = FV_BOOL;
  else if (strcmp(type, "int32") == 0)  type_ = FV_INT32;
  else if (strcmp(type, "int64") == 0)  type_ = FV_INT64;
  else if (strcmp(type, "uint64") == 0)  type_ = FV_UINT64;
  else if (strcmp(type, "double") == 0)  type_ = FV_DOUBLE;
  else if (strcmp(type, "string") == 0)  type_ = FV_STRING;
  else assert(false); // Unknown typename
}

VarzValue::~VarzValue() {
  switch (type_) {
    case FV_BOOL: delete reinterpret_cast<bool*>(value_buffer_); break;
    case FV_INT32: delete reinterpret_cast<int32*>(value_buffer_); break;
    case FV_INT64: delete reinterpret_cast<int64*>(value_buffer_); break;
    case FV_UINT64: delete reinterpret_cast<uint64*>(value_buffer_); break;
    case FV_DOUBLE: delete reinterpret_cast<double*>(value_buffer_); break;
    case FV_STRING: delete reinterpret_cast<string*>(value_buffer_); break;
  }
}

const char* VarzValue::TypeName() const {
  switch (type_) {
    case FV_BOOL:   return "bool";
    case FV_INT32:  return "int32";
    case FV_INT64:  return "int64";
    case FV_UINT64: return "uint64";
    case FV_DOUBLE: return "double";
    case FV_STRING: return "string";
    default: assert(false); return "";  // unknown type
  }
}

bool VarzValue::Equal(const VarzValue& x) const {
  if (type_ != x.type_)
    return false;
  switch (type_) {
    case FV_BOOL:   return VALUE_AS(bool) == OTHER_VALUE_AS(x, bool);
    case FV_INT32:  return VALUE_AS(int32) == OTHER_VALUE_AS(x, int32);
    case FV_INT64:  return VALUE_AS(int64) == OTHER_VALUE_AS(x, int64);
    case FV_UINT64: return VALUE_AS(uint64) == OTHER_VALUE_AS(x, uint64);
    case FV_DOUBLE: return VALUE_AS(double) == OTHER_VALUE_AS(x, double);
    case FV_STRING: return VALUE_AS(string) == OTHER_VALUE_AS(x, string);
    default: assert(false); return false;  // unknown type
  }
}

VarzValue* VarzValue::New() const {
  switch (type_) {
    case FV_BOOL:   return new VarzValue(new bool(false), "bool");
    case FV_INT32:  return new VarzValue(new int32(0), "int32");
    case FV_INT64:  return new VarzValue(new int64(0), "int64");
    case FV_UINT64: return new VarzValue(new uint64(0), "uint64");
    case FV_DOUBLE: return new VarzValue(new double(0.0), "double");
    case FV_STRING: return new VarzValue(new string, "string");
    default: assert(false); return NULL;  // unknown type
  }
}

void VarzValue::CopyFrom(const VarzValue& x) {
  assert(type_ == x.type_);
  switch (type_) {
    case FV_BOOL:   SET_VALUE_AS(bool, OTHER_VALUE_AS(x, bool));      break;
    case FV_INT32:  SET_VALUE_AS(int32, OTHER_VALUE_AS(x, int32));    break;
    case FV_INT64:  SET_VALUE_AS(int64, OTHER_VALUE_AS(x, int64));    break;
    case FV_UINT64: SET_VALUE_AS(uint64, OTHER_VALUE_AS(x, uint64));  break;
    case FV_DOUBLE: SET_VALUE_AS(double, OTHER_VALUE_AS(x, double));  break;
    case FV_STRING: SET_VALUE_AS(string, OTHER_VALUE_AS(x, string));  break;
    default: assert(false);  // unknown type
  }
}

string VarzValue::ToString() const {
  char intbuf[64];    // enough to hold even the biggest number
  switch (type_) {
    case FV_BOOL:
      return VALUE_AS(bool) ? "true" : "false";
    case FV_INT32:
      snprintf(intbuf, sizeof(intbuf), "%"PRId32, VALUE_AS(int32));
      return intbuf;
    case FV_INT64:
      snprintf(intbuf, sizeof(intbuf), "%"PRId64, VALUE_AS(int64));
      return intbuf;
    case FV_UINT64:
      snprintf(intbuf, sizeof(intbuf), "%"PRIu64, VALUE_AS(uint64));
      return intbuf;
    case FV_DOUBLE:
      snprintf(intbuf, sizeof(intbuf), "%.17g", VALUE_AS(double));
      return intbuf;
    case FV_STRING:
      return VALUE_AS(string);
    default:
      assert(false);
      return "";  // unknown type
  }
}

// --------------------------------------------------------------------
// VarzHolder
//    This represents a single varz, including its name, description,
//    default value, and current value.  Mostly this serves as a
//    struct, though it also knows how to register itself.
//       All VarzHolders are owned by a (exactly one)
//    VarzRegistry.  If you wish to modify fields in this class, you
//    should acquire the VarzRegistry lock for the registry that owns
//    this varz.
// --------------------------------------------------------------------

class VarzHolder {
 public:
  // Note: we take over memory-ownership of current_val and default_val.
  VarzHolder(const char* name, const char* help, const char* filename,
                  VarzValue* current_val, VarzValue* default_val);
  ~VarzHolder();

  const char* name() const { return name_; }
  const char* help() const { return help_; }
  const char* filename() const { return file_; }
  const char* CleanFileName() const;  // nixes irrelevant prefix such as homedir
  string current_value() const { return current_->ToString(); }
  string initial_value() const { return initvalue_->ToString(); }
  const char* type_name() const { return initvalue_->TypeName(); }

  void FillVarzInfo(struct VarzInfo* result);

 private:
  // for SetVarzLocked() and setting varzs_by_ptr_
  friend class VarzRegistry;

  // This copies all the non-const members: modified, processed, initvalue, etc.
  void CopyFrom(const VarzHolder& src);

  const char* const name_;     // Varz name
  const char* const help_;     // Help message
  const char* const file_;     // Which file did this come from?

  VarzValue* initvalue_;        // Default value for varz
  VarzValue* current_;         // Current value for varz

  VarzHolder(const VarzHolder&);   // no copying!
  void operator=(const VarzHolder&);
};



// --------------------------------------------------------------------
// VarzRegistry
//    A VarzRegistry singleton object holds all varz objects indexed
//    by their names so that if you know a varz's name (as a C
//    string), you can access or set it.  If the function is named
//    FooLocked(), you must own the registry lock before calling
//    the function; otherwise, you should *not* hold the lock, and
//    the function will acquire it itself if needed.
// --------------------------------------------------------------------

struct StringCmp {  // Used by the VarzRegistry map class to compare char*'s
  bool operator() (const char* s1, const char* s2) const {
    return (strcmp(s1, s2) < 0);
  }
};

class VarzRegistry {
 public:
  VarzRegistry() { }

  // Store a varz in this registry.  Takes ownership of the given pointer.
  void RegisterVarz(VarzHolder* varz);

  // Returns the varz object for the specified name, or NULL if not found.
  VarzHolder* FindVarzLocked(const char* name);

  // Returns the varz object whose current-value is stored at varz_ptr.
  // That is, for whom current_->value_buffer_ == varz_ptr
  VarzHolder* FindVarzViaPtrLocked(const void* varz_ptr);

  // Set the value of a varz.  If the varz was successfully set to
  // value, set msg to indicate the new varz value, and return true.
  // Otherwise, set msg to indicate the error, leave varz unchanged,
  // and return false.  msg can be NULL.
  bool SetVarzLocked(VarzHolder* varz, const char* value, string* msg);

  static VarzRegistry* GlobalRegistry();   // returns a singleton registry

 private:
  friend void lab616::GetAllVarzs(vector<VarzInfo>*);

  // The map from name to varz, for FindVarzLocked().
  typedef map<const char*, VarzHolder*, StringCmp> VarzMap;
  typedef VarzMap::iterator VarzIterator;
  typedef VarzMap::const_iterator VarzConstIterator;
  VarzMap varzs_;

  // The map from current-value pointer to varz, fo FindVarzViaPtrLocked().
  typedef map<const void*, VarzHolder*> VarzPtrMap;
  VarzPtrMap varzs_by_ptr_;

  static VarzRegistry* global_registry_;   // a singleton registry

  // Disallow
  VarzRegistry(const VarzRegistry&);
  VarzRegistry& operator=(const VarzRegistry&);
};

static boost::mutex global_registry_lock_;

// Global singleton.
VarzRegistry* VarzRegistry::global_registry_ = NULL;

// This method is synchronized. However, this should not be a problem
// because 1. this is called only when a new VARZ is defined, which is
// is done only once per varz, and 2. this does not affect the storage
// or access of the actual varzs when the program is running.  Program
// code directly writes to the address of the defined variable (void*)
// so there's no access of registry of any type, and finally 3. when
// the registry is queried to provide the global state.  This is done
// only periodically by a probing caller, so it should not interfere
// too much in terms of the performance, since memory access of the
// varz occurs far more often than the access via the registry by a
// sampling application.
VarzRegistry* VarzRegistry::GlobalRegistry() {
  boost::mutex::scoped_lock lock(global_registry_lock_);
  if (!global_registry_) {
    global_registry_ = new VarzRegistry;
  }
  return global_registry_;
}

void VarzRegistry::RegisterVarz(VarzHolder* varz) {
  using namespace boost;
  //unique_lock<mutex> acquire_lock(global_registry_lock_);

  pair<VarzIterator, bool> ins =
    varzs_.insert(pair<const char*, VarzHolder*>(varz->name(), varz));
  if (ins.second == false) {   // means the name was already in the map
    if (strcmp(ins.first->second->filename(), varz->filename()) != 0) {
      ReportError(DIE, "ERROR: varz '%s' was defined more than once "
                  "(in files '%s' and '%s').\n",
                  varz->name(),
                  ins.first->second->filename(),
                  varz->filename());
    } else {
      ReportError(DIE, "ERROR: something wrong with varz '%s' in file '%s'.  "
                  "One possibility: file '%s' is being linked both statically "
                  "and dynamically into this executable.\n",
                  varz->name(),
                  varz->filename(), varz->filename());
    }
  }
  // Also add to the varzs_by_ptr_ map.
  varzs_by_ptr_[varz->current_->value_buffer_] = varz;
}

VarzHolder* VarzRegistry::FindVarzLocked(const char* name) {
  VarzConstIterator i = varzs_.find(name);
  if (i == varzs_.end()) {
    return NULL;
  } else {
    return i->second;
  }
}

VarzHolder* VarzRegistry::FindVarzViaPtrLocked(const void* varz_ptr) {
  VarzPtrMap::const_iterator i = varzs_by_ptr_.find(varz_ptr);
  if (i == varzs_by_ptr_.end()) {
    return NULL;
  } else {
    return i->second;
  }
}


// --------------------------------------------------------------------
// VarzRegisterer
//    This class exists merely to have a global constructor (the
//    kind that runs before main(), that goes and initializes each
//    varz that's been declared.  Note that it's very important we
//    don't have a destructor that deletes varz_, because that would
//    cause us to delete current_storage/initvalue_storage as well,
//    which can cause a crash if anything tries to access the varz
//    values in a global destructor.
// --------------------------------------------------------------------

VarzRegisterer::VarzRegisterer(const char* name, const char* type,
                               const char* help, const char* filename,
                               void* current_storage, void* initvalue_storage) {
  if (help == NULL)
    help = "";
  // VarzValue expects the type-name to not include any namespace
  // components, so we get rid of those, if any.
  if (strchr(type, ':'))
    type = strrchr(type, ':') + 1;
  // This is the cool part: objects that wraps the storage (via the void*
  // pointers) of the actual variables.  This guarantees that we are accessing
  // the values at where it was first defined.
  VarzValue* current = new VarzValue(current_storage, type);
  VarzValue* initvalue = new VarzValue(initvalue_storage, type);
  // Importantly, varz_ will never be deleted, so storage is always good.
  VarzHolder* varz = new VarzHolder(name, help, filename,
                                    current, initvalue);
  VarzRegistry::GlobalRegistry()->RegisterVarz(varz);   // default registry
}


VarzHolder::VarzHolder(const char* name, const char* help,
                                 const char* filename,
                                 VarzValue* current_val, VarzValue* default_val)
    : name_(name), help_(help), file_(filename),
      initvalue_(default_val), current_(current_val) {
}

VarzHolder::~VarzHolder() {
  delete current_;
  delete initvalue_;
}

const char* VarzHolder::CleanFileName() const {
  vector<string> parts;
  string full_path(filename());
  boost::split(parts, full_path, boost::is_any_of("/"));
  return parts.back().c_str();
}

void VarzHolder::FillVarzInfo(
    VarzInfo* result) {
  result->name = name();
  result->type = type_name();
  result->description = help();
  result->current_value = current_value();
  result->initial_value = initial_value();
  result->filename = CleanFileName();
}

void VarzHolder::CopyFrom(const VarzHolder& src) {
  // Note we only copy the non-const members; others are fixed at construct time
  if (!current_->Equal(*src.current_)) current_->CopyFrom(*src.current_);
  if (!initvalue_->Equal(*src.initvalue_))
    initvalue_->CopyFrom(*src.initvalue_);
}

// --------------------------------------------------------------------
// GetAllVarzs()
//    The main way the VarzRegistry class exposes its data.  This
//    returns, as strings, all the info about all the varzs in
//    the main registry, sorted first by filename they are defined
//    in, and then by varzname.
// --------------------------------------------------------------------

struct FilenameVarznameCmp {
  bool operator()(const VarzInfo& a,
                  const VarzInfo& b) const {
    int cmp = strcmp(a.filename.c_str(), b.filename.c_str());
    if (cmp == 0)
      cmp = strcmp(a.name.c_str(), b.name.c_str());  // secondary sort key
    return cmp < 0;
  }
};

void GetAllVarzs(vector<VarzInfo>* OUTPUT) {
  // Note that we do not place a lock on the registry when trying
  // to access its state.  This is ok because varzs are just a snapshot
  // of the current state of the system and there is no need to incur the
  // expense of locking in the name of consistency.

  VarzRegistry* const registry = VarzRegistry::GlobalRegistry();

  for (VarzRegistry::VarzConstIterator i = registry->varzs_.begin();
       i != registry->varzs_.end(); ++i) {
    VarzInfo fi;
    i->second->FillVarzInfo(&fi);
    OUTPUT->push_back(fi);
  }

  // Now sort the varzs, first by filename they occur in, then alphabetically
  sort(OUTPUT->begin(), OUTPUT->end(), FilenameVarznameCmp());
}


} // namespace lab616
