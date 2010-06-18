/*************************************************************************************************
 * C language binding
 *                                                      Copyright (C) 2009-2010 Mikio Hirabayashi
 * This file is part of Kyoto Cabinet.
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version
 * 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *************************************************************************************************/


#include "kcpolydb.h"
#include "myconf.h"
#include "kclangc.h"

using namespace kyotocabinet;

extern "C" {


/** The package version. */
const char* const KCVERSION = VERSION;


/** Special pointer for no operation by the visiting function. */
const char* const KCVISNOP = DB::Visitor::NOP;


/** Special pointer to remove the record by the visiting function. */
const char* const KCVISREMOVE = DB::Visitor::REMOVE;


/**
 * Allocate a region on memory.
 */
char* kcmalloc(size_t size) {
  _assert_(size > 0 && size <= MEMMAXSIZ);
  return new char[size];
}


/**
 * Release a region allocated in the library.
 */
void kcfree(char* ptr) {
  _assert_(true);
  delete[] ptr;
}


/**
 * Get the time of day in seconds.
 */
double kctime(void) {
  _assert_(true);
  return kyotocabinet::time();
}


/**
 * Convert a string to an integer.
 */
int64_t kcatoi(const char* str) {
  _assert_(str);
  return kyotocabinet::atoi(str);
}


/**
 * Convert a string with a metric prefix to an integer.
 */
int64_t kcatoix(const char* str) {
  _assert_(str);
  return kyotocabinet::atoix(str);
}


/**
 * Convert a string to a real number.
 */
double kcatof(const char* str) {
  _assert_(str);
  return kyotocabinet::atof(str);
}


/**
 * Get the hash value by MurMur hashing.
 */
uint64_t kchashmurmur(const void* buf, size_t size) {
  _assert_(buf && size <= MEMMAXSIZ);
  return kyotocabinet::hashmurmur(buf, size);
}


/**
 * Get the hash value by FNV hashing.
 */
uint64_t kchashfnv(const void* buf, size_t size) {
  _assert_(buf && size <= MEMMAXSIZ);
  return kyotocabinet::hashfnv(buf, size);
}


/**
 * Get the quiet Not-a-Number value.
 */
double kcnan() {
  _assert_(true);
  return kyotocabinet::nan();
}


/**
 * Get the positive infinity value.
 */
double kcinf() {
  _assert_(true);
  return kyotocabinet::inf();
}


/**
 * Check a number is a Not-a-Number value.
 */
int32_t kcchknan(double num) {
  _assert_(true);
  return kyotocabinet::chknan(num);
}


/**
 * Check a number is an infinity value.
 */
int32_t kcchkinf(double num) {
  _assert_(true);
  return kyotocabinet::chkinf(num);
}


/**
 * Get the readable string of an error code.
 */
const char* kcecodename(int32_t code) {
  _assert_(true);
  return FileDB::Error::codename((FileDB::Error::Code)code);
}


/**
 * Create a database object.
 */
KCDB* kcdbnew(void) {
  _assert_(true);
  return (KCDB*)new PolyDB;
}


/**
 * Destroy a database object.
 */
void kcdbdel(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  delete pdb;
}


/**
 * Open a database file.
 */
int32_t kcdbopen(KCDB* db, const char* path, uint32_t mode) {
  _assert_(db && path);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->open(path, mode);
}


/**
 * Close the database file.
 */
int32_t kcdbclose(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->close();
}


/**
 * Get the code of the last happened error.
 */
int32_t kcdbecode(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->error().code();
}


/**
 * Get the supplement message of the last happened error.
 */
const char* kcdbemsg(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->error().message();
}


/**
 * Accept a visitor to a record.
 */
int32_t kcdbaccept(KCDB* db, const char* kbuf, size_t ksiz,
                   KCVISITFULL fullproc, KCVISITEMPTY emptyproc, void* opq, int32_t writable) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  class VisitorImpl : public DB::Visitor {
  public:
    explicit VisitorImpl(KCVISITFULL fullproc, KCVISITEMPTY emptyproc, void* opq) :
      fullproc_(fullproc), emptyproc_(emptyproc), opq_(opq) {}
    const char* visit_full(const char* kbuf, size_t ksiz,
                           const char* vbuf, size_t vsiz, size_t* sp) {
      if (!fullproc_) return NOP;
      return fullproc_(kbuf, ksiz, vbuf, vsiz, sp, opq_);
    }
    const char* visit_empty(const char* kbuf, size_t ksiz, size_t* sp) {
      if (!emptyproc_) return NOP;
      return emptyproc_(kbuf, ksiz, sp, opq_);
    }
  private:
    KCVISITFULL fullproc_;
    KCVISITEMPTY emptyproc_;
    void* opq_;
  };
  VisitorImpl visitor(fullproc, emptyproc, opq);
  return pdb->accept(kbuf, ksiz, &visitor, writable);
}


/**
 * Iterate to accept a visitor for each record.
 */
int32_t kcdbiterate(KCDB* db, KCVISITFULL fullproc, void* opq, int32_t writable) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  class VisitorImpl : public DB::Visitor {
  public:
    explicit VisitorImpl(KCVISITFULL fullproc, void* opq) : fullproc_(fullproc), opq_(opq) {}
    const char* visit_full(const char* kbuf, size_t ksiz,
                           const char* vbuf, size_t vsiz, size_t* sp) {
      if (!fullproc_) return NOP;
      return fullproc_(kbuf, ksiz, vbuf, vsiz, sp, opq_);
    }
  private:
    KCVISITFULL fullproc_;
    void* opq_;
  };
  VisitorImpl visitor(fullproc, opq);
  return pdb->iterate(&visitor, writable);
}


/**
 * Set the value of a record.
 */
int32_t kcdbset(KCDB* db, const char* kbuf, size_t ksiz, const char* vbuf, size_t vsiz) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ && vbuf && vsiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->set(kbuf, ksiz, vbuf, vsiz);
}


/**
 * Add a record.
 */
int32_t kcdbadd(KCDB* db, const char* kbuf, size_t ksiz, const char* vbuf, size_t vsiz) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ && vbuf && vsiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->add(kbuf, ksiz, vbuf, vsiz);
}


/**
 * Append the value of a record.
 */
int32_t kcdbappend(KCDB* db, const char* kbuf, size_t ksiz, const char* vbuf, size_t vsiz) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ && vbuf && vsiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->append(kbuf, ksiz, vbuf, vsiz);
}


/**
 * Add a number to the numeric value of a record.
 */
int64_t kcdbincrint(KCDB* db, const char* kbuf, size_t ksiz, int64_t num) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->increment(kbuf, ksiz, num);
}


/**
 * Add a number to the numeric value of a record.
 */
double kcdbincrdouble(KCDB* db, const char* kbuf, size_t ksiz, double num) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->increment(kbuf, ksiz, num);
}


/**
 * Perform compare-and-swap.
 */
int32_t kcdbcas(KCDB* db, const char* kbuf, size_t ksiz,
                const char* nvbuf, size_t nvsiz, const char* ovbuf, size_t ovsiz) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->cas(kbuf, ksiz, nvbuf, nvsiz, ovbuf, ovsiz);
}


/**
 * Remove a record.
 */
int32_t kcdbremove(KCDB* db, const char* kbuf, size_t ksiz) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->remove(kbuf, ksiz);
}


/**
 * Retrieve the value of a record.
 */
char* kcdbget(KCDB* db, const char* kbuf, size_t ksiz, size_t* sp) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ && sp);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->get(kbuf, ksiz, sp);
}


/**
 * Retrieve the value of a record.
 */
int32_t kcdbgetbuf(KCDB* db, const char* kbuf, size_t ksiz, char* vbuf, size_t max) {
  _assert_(db && kbuf && ksiz <= MEMMAXSIZ && vbuf);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->get(kbuf, ksiz, vbuf, max);
}


/**
 * Remove all records.
 */
int32_t kcdbclear(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->clear();
}


/**
 * Synchronize updated contents with the file and the device.
 */
int32_t kcdbsync(KCDB* db, int32_t hard, KCFILEPROC proc, void* opq) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  class FileProcessorImpl : public FileDB::FileProcessor {
  public:
    explicit FileProcessorImpl(KCFILEPROC proc, void* opq) : proc_(proc), opq_(opq) {}
    bool process(const std::string& path, int64_t count, int64_t size) {
      if (!proc_) return true;
      return proc_(path.c_str(), count, size, opq_);
    }
  private:
    KCFILEPROC proc_;
    void* opq_;
  };
  FileProcessorImpl myproc(proc, opq);
  return pdb->synchronize(hard, &myproc);
}


/**
 * Create a copy of the database file.
 */
int32_t kcdbcopy(KCDB* db, const char* dest) {
  _assert_(db && dest);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->copy(dest);
}


/**
 * Begin transaction.
 */
int32_t kcdbbegintran(KCDB* db, int32_t hard) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->begin_transaction(hard);
}


/**
 * Try to begin transaction.
 */
int32_t kcdbbegintrantry(KCDB* db, int32_t hard) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->begin_transaction_try(hard);
}


/**
 * End transaction.
 */
int32_t kcdbendtran(KCDB* db, int32_t commit) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->end_transaction(commit);
}


/**
 * Dump records into a file.
 */
int32_t kcdbdumpsnap(KCDB* db, const char* dest) {
  _assert_(db && dest);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->dump_snapshot(dest);
}


/**
 * Load records from a file.
 */
int32_t kcdbloadsnap(KCDB* db, const char* src) {
  _assert_(db && src);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->load_snapshot(src);
}


/**
 * Get the number of records.
 */
int64_t kcdbcount(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->count();
}


/**
 * Get the size of the database file.
 */
int64_t kcdbsize(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return pdb->size();
}


/**
 * Get the path of the database file.
 */
char* kcdbpath(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  std::string path = pdb->path();
  size_t psiz = path.size();
  char* pbuf = new char[psiz+1];
  std::memcpy(pbuf, path.c_str(), psiz + 1);
  return pbuf;
}


/**
 * Get the miscellaneous status information.
 */
char* kcdbstatus(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  std::map<std::string, std::string> status;
  if (!pdb->status(&status)) return NULL;
  std::ostringstream obuf;
  std::map<std::string, std::string>::iterator it = status.begin();
  std::map<std::string, std::string>::iterator itend = status.end();
  while (it != itend) {
    obuf << it->first << "\t" << it->second << "\n";
    it++;
  }
  std::string sstr = obuf.str();
  size_t ssiz = sstr.size();
  char* sbuf = new char[ssiz+1];
  std::memcpy(sbuf, sstr.c_str(), ssiz + 1);
  return sbuf;
}


/**
 * Create a cursor object.
 */
KCCUR* kcdbcursor(KCDB* db) {
  _assert_(db);
  PolyDB* pdb = (PolyDB*)db;
  return (KCCUR*)pdb->cursor();
}


/**
 * Destroy a cursor object.
 */
void kccurdel(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  delete pcur;
}


/**
 * Accept a visitor to the current record.
 */
int32_t kccuraccept(KCCUR* cur, KCVISITFULL fullproc, void* opq,
                    int32_t writable, int32_t step) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  class VisitorImpl : public DB::Visitor {
  public:
    explicit VisitorImpl(KCVISITFULL fullproc, void* opq) : fullproc_(fullproc), opq_(opq) {}
    const char* visit_full(const char* kbuf, size_t ksiz,
                           const char* vbuf, size_t vsiz, size_t* sp) {
      if (!fullproc_) return NOP;
      return fullproc_(kbuf, ksiz, vbuf, vsiz, sp, opq_);
    }
  private:
    KCVISITFULL fullproc_;
    void* opq_;
  };
  VisitorImpl visitor(fullproc, opq);
  return pcur->accept(&visitor, writable, step);
}


/**
 * Set the value of the current record.
 */
int32_t kccursetvalue(KCCUR* cur, const char* vbuf, size_t vsiz, int32_t step) {
  _assert_(cur && vbuf && vsiz <= MEMMAXSIZ);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->set_value(vbuf, vsiz, step);
}


/**
 * Remove the current record.
 */
int32_t kccurremove(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->remove();
}


/**
 * Get the key of the current record.
 */
char* kccurgetkey(KCCUR* cur, size_t* sp, int32_t step) {
  _assert_(cur && sp);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->get_key(sp, step);
}


/**
 * Get the value of the current record.
 */
char* kccurgetvalue(KCCUR* cur, size_t* sp, int32_t step) {
  _assert_(cur && sp);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->get_value(sp, step);
}


/**
 * Get a pair of the key and the value of the current record.
 */
char* kccurget(KCCUR* cur, size_t* ksp, const char** vbp, size_t* vsp, int32_t step) {
  _assert_(cur && ksp && vbp && vsp);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->get(ksp, vbp, vsp, step);
}


/**
 * Jump the cursor to the first record.
 */
int32_t kccurjump(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->jump();
}


/**
 * Jump the cursor to a record.
 */
int32_t kccurjumpkey(KCCUR* cur, const char* kbuf, size_t ksiz) {
  _assert_(cur && kbuf && ksiz <= MEMMAXSIZ);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->jump(kbuf, ksiz);
}


/**
 * Step the cursor to the next record.
 */
int32_t kccurstep(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->step();
}


/**
 * Get the database object.
 */
KCDB* kccurdb(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return (KCDB*)pcur->db();
}


/**
 * Get the code of the last happened error.
 */
int32_t kccurecode(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->error().code();
}


/**
 * Get the supplement message of the last happened error.
 */
const char* kccuremsg(KCCUR* cur) {
  _assert_(cur);
  PolyDB::Cursor* pcur = (PolyDB::Cursor*)cur;
  return pcur->error().message();
}


}

// END OF FILE
