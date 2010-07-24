/*************************************************************************************************
 * Common symbols for command line utilities
 *                                                               Copyright (C) 2009-2010 FAL Labs
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


#ifndef _CMDCOMMON_H                     // duplication check
#define _CMDCOMMON_H

#include <kccommon.h>
#include <kcutil.h>
#include <kcdb.h>
#include <kcthread.h>
#include <kcfile.h>
#include <kccompress.h>
#include <kccompare.h>
#include <kcmap.h>

#if !defined(_KC_PREFIX)
#define _KC_PREFIX       "*"
#endif
#if !defined(_KC_INCLUDEDIR)
#define _KC_INCLUDEDIR   "*"
#endif
#if !defined(_KC_LIBDIR)
#define _KC_LIBDIR       "*"
#endif
#if !defined(_KC_BINDIR)
#define _KC_BINDIR       "*"
#endif
#if !defined(_KC_LIBEXECDIR)
#define _KC_LIBEXECDIR   "*"
#endif
#if !defined(_KC_APPINC)
#define _KC_APPINC       "*"
#endif
#if !defined(_KC_APPLIBS)
#define _KC_APPLIBS      "*"
#endif

namespace kc = kyotocabinet;


// constants
const int32_t THREADMAX = 64;            // maximum number of threads
const size_t RECBUFSIZ = 64;             // buffer size for a record
const size_t RECBUFSIZL = 1024;          // buffer size for a long record


// global variables
uint64_t g_rnd_x = 123456789;
uint64_t g_rnd_y = 362436069;
uint64_t g_rnd_z = 521288629;
uint64_t g_rnd_w = 88675123;


// function prototypes
int64_t myrand(int64_t range);
void iprintf(const char* format, ...);
void iputchar(char c);
void eprintf(const char* format, ...);
void printversion();
void printdata(const char* buf, int size, bool px);
bool getline(std::istream* is, std::string* str);
void splitstr(const std::string& str, char delim, std::vector<std::string>* elems);
std::string unitnumstr(int64_t num);
std::string unitnumstrbyte(int64_t num);


// get the random seed
void mysrand(int64_t seed) {
  g_rnd_x = seed;
  for (int32_t i = 0; i < 16; i++) {
    myrand(1);
  }
}


// get a random number
int64_t myrand(int64_t range) {
  uint64_t t = g_rnd_x ^ (g_rnd_x << 11);
  g_rnd_x = g_rnd_y;
  g_rnd_y = g_rnd_z;
  g_rnd_z = g_rnd_w;
  g_rnd_w = (g_rnd_w ^ (g_rnd_w >> 19)) ^ (t ^ (t >> 8));
  return (g_rnd_w & INT64_MAX) % range;
}


// print formatted information string and flush the buffer
inline void iprintf(const char* format, ...) {
  std::string msg;
  va_list ap;
  va_start(ap, format);
  kc::strprintf(&msg, format, ap);
  va_end(ap);
  std::cout << msg;
  std::cout.flush();
}


// print a character and flush the buffer
inline void iputchar(char c) {
  std::cout << c;
  std::cout.flush();
}


// print formatted error string and flush the buffer
inline void eprintf(const char* format, ...) {
  std::string msg;
  va_list ap;
  va_start(ap, format);
  kc::strprintf(&msg, format, ap);
  va_end(ap);
  std::cerr << msg;
  std::cerr.flush();
}


// print the versin information
inline void printversion() {
  iprintf("Kyoto Cabinet %s (%d.%d:%d) on %s\n",
          kc::VERSION, kc::LIBVER, kc::LIBREV, kc::FMTVER, kc::SYSNAME);
}


// print record data
inline void printdata(const char* buf, int size, bool px) {
  size_t cnt = 0;
  char numbuf[kc::NUMBUFSIZ];
  while (size-- > 0) {
    if (px) {
      if (cnt++ > 0) putchar(' ');
      std::sprintf(numbuf, "%02X", *(unsigned char*)buf);
      std::cout << numbuf;
    } else {
      std::cout << *buf;
    }
    buf++;
  }
}


// read a line from a file descriptor
inline bool getline(std::istream* is, std::string* str) {
  str->clear();
  bool hit = false;
  char c;
  while (is->get(c)) {
    hit = true;
    if (c == '\0' || c == '\r') continue;
    if (c == '\n') break;
    str->append(1, c);
  }
  return hit;
}


// convert a number into the string with the decimal unit
inline std::string unitnumstr(int64_t num) {
  if (num >= pow(1000.0, 6)) {
    return kc::strprintf("%.3Lf quintillion", (long double)num / pow(1000.0, 6));
  } else if (num >= pow(1000.0, 5)) {
    return kc::strprintf("%.3Lf quadrillion", (long double)num / pow(1000.0, 5));
  } else if (num >= pow(1000.0, 4)) {
    return kc::strprintf("%.3Lf trillion", (long double)num / pow(1000.0, 4));
  } else if (num >= pow(1000.0, 3)) {
    return kc::strprintf("%.3Lf billion", (long double)num / pow(1000.0, 3));
  } else if (num >= pow(1000.0, 2)) {
    return kc::strprintf("%.3Lf million", (long double)num / pow(1000.0, 2));
  } else if (num >= pow(1000.0, 1)) {
    return kc::strprintf("%.3Lf thousand", (long double)num / pow(1000.0, 1));
  }
  return kc::strprintf("%lld", (long long)num);
}


// convert a number into the string with the byte unit
inline std::string unitnumstrbyte(int64_t num) {
  if ((unsigned long long)num >= 1ULL << 60) {
    return kc::strprintf("%.3Lf EiB", (long double)num / (1ULL << 60));
  } else if ((unsigned long long)num >= 1ULL << 50) {
    return kc::strprintf("%.3Lf PiB", (long double)num / (1ULL << 50));
  } else if ((unsigned long long)num >= 1ULL << 40) {
    return kc::strprintf("%.3Lf TiB", (long double)num / (1ULL << 40));
  } else if ((unsigned long long)num >= 1ULL << 30) {
    return kc::strprintf("%.3Lf GiB", (long double)num / (1ULL << 30));
  } else if ((unsigned long long)num >= 1ULL << 20) {
    return kc::strprintf("%.3Lf MiB", (long double)num / (1ULL << 20));
  } else if ((unsigned long long)num >= 1ULL << 10) {
    return kc::strprintf("%.3Lf KiB", (long double)num / (1ULL << 10));
  }
  return kc::strprintf("%lld B", (long long)num);
}



#endif                                   // duplication check

// END OF FILE
