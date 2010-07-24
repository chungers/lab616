/*************************************************************************************************
 * Popular encoders and decoders
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


#include "cmdcommon.h"


// global variables
const char* g_progname;                  // program name


// function prototypes
int main(int argc, char** argv);
static void usage();
static int runhex(int argc, char** argv);
static int runcomp(int argc, char** argv);
static int runhash(int argc, char** argv);
static int runconf(int argc, char** argv);
static int32_t prochex(const char* file, bool dec);
static int32_t proccomp(const char* file, int32_t mode, bool dec);
static int32_t prochash(const char* file, int32_t mode);
static int32_t procconf(int32_t mode);


// main routine
int main(int argc, char** argv) {
  g_progname = argv[0];
  if (argc < 2) usage();
  int32_t rv = 0;
  if (!std::strcmp(argv[1], "hex")) {
    rv = runhex(argc, argv);
  } else if (!std::strcmp(argv[1], "comp")) {
    rv = runcomp(argc, argv);
  } else if (!std::strcmp(argv[1], "hash")) {
    rv = runhash(argc, argv);
  } else if (!std::strcmp(argv[1], "conf")) {
    rv = runconf(argc, argv);
  } else if (!std::strcmp(argv[1], "version") || !std::strcmp(argv[1], "--version")) {
    printversion();
    rv = 0;
  } else {
    usage();
  }
  return rv;
}


// print the usage and exit
static void usage() {
  eprintf("%s: popular encoders and decoders of Kyoto Cabinet\n", g_progname);
  eprintf("\n");
  eprintf("usage:\n");
  eprintf("  %s hex [-d] [file]\n", g_progname);
  eprintf("  %s comp [-def|-gz] [-d] [file]\n", g_progname);
  eprintf("  %s hash [-fnv|-path] [file]\n", g_progname);
  eprintf("  %s conf [-v|-i|-l|-p]\n", g_progname);
  eprintf("\n");
  std::exit(1);
}


// parse arguments of hex command
static int runhex(int argc, char** argv) {
  char* file = NULL;
  bool dec = false;
  for (int32_t i = 2; i < argc; i++) {
    if (argv[i][0] == '-') {
      if (!std::strcmp(argv[i], "-d")) {
        dec = true;
      } else {
        usage();
      }
    } else if (!file) {
      file = argv[i];
    } else {
      usage();
    }
  }
  int rv = prochex(file, dec);
  return rv;
}


// parse arguments of comp command
static int runcomp(int argc, char** argv) {
  char* file = NULL;
  int32_t mode = 0;
  bool dec = false;
  for (int32_t i = 2; i < argc; i++) {
    if (argv[i][0] == '-') {
      if (!std::strcmp(argv[i], "-def")) {
        mode = 1;
      } else if (!std::strcmp(argv[i], "-gz")) {
        mode = 2;
      } else if (!std::strcmp(argv[i], "-d")) {
        dec = true;
      } else {
        usage();
      }
    } else if (!file) {
      file = argv[i];
    } else {
      usage();
    }
  }
  int rv = proccomp(file, mode, dec);
  return rv;
}


// parse arguments of hash command
static int runhash(int argc, char** argv) {
  char* file = NULL;
  int32_t mode = 0;
  for (int32_t i = 2; i < argc; i++) {
    if (argv[i][0] == '-') {
      if (!std::strcmp(argv[i], "-fnv")) {
        mode = 1;
      } else if (!std::strcmp(argv[i], "-path")) {
        mode = 2;
      } else {
        usage();
      }
    } else if (!file) {
      file = argv[i];
    } else {
      usage();
    }
  }
  int rv = prochash(file, mode);
  return rv;
}


// parse arguments of conf command
static int runconf(int argc, char** argv) {
  int32_t mode = 0;
  for (int32_t i = 2; i < argc; i++) {
    if (argv[i][0] == '-') {
      if (!std::strcmp(argv[i], "-v")) {
        mode = 'v';
      } else if (!std::strcmp(argv[i], "-i")) {
        mode = 'i';
      } else if (!std::strcmp(argv[i], "-l")) {
        mode = 'l';
      } else if (!std::strcmp(argv[i], "-p")) {
        mode = 'p';
      } else {
        usage();
      }
    } else {
      usage();
    }
  }
  int rv = procconf(mode);
  return rv;
}


// perform hex command
static int32_t prochex(const char* file, bool dec) {
  const char* istr = file && *file == '@' ? file + 1 : NULL;
  std::istream *is;
  std::ifstream ifs;
  std::istringstream iss(istr ? istr : "");
  if (file) {
    if (istr) {
      is = &iss;
    } else {
      ifs.open(file, std::ios_base::in | std::ios_base::binary);
      if (!ifs) {
        eprintf("%s: %s: open error\n", g_progname, file);
        return 1;
      }
      is = &ifs;
    }
  } else {
    is = &std::cin;
  }
  if (dec) {
    char c;
    while (is->get(c)) {
      int32_t cc = (unsigned char)c;
      int32_t num = -1;
      if (cc >= '0' && cc <= '9') {
        num = cc - '0';
      } else if (cc >= 'a' && cc <= 'f') {
        num = cc - 'a' + 10;
      } else if (cc >= 'A' && cc <= 'F') {
        num = cc - 'A' + 10;
      }
      if (num >= 0) {
        if (is->get(c)) {
          cc = (unsigned char)c;
          if (cc >= '0' && cc <= '9') {
            num = num * 0x10 + cc - '0';
          } else if (cc >= 'a' && cc <= 'f') {
            num = num * 0x10 + cc - 'a' + 10;
          } else if (cc >= 'A' && cc <= 'F') {
            num = num * 0x10 + cc - 'A' + 10;
          }
          std::cout << (char)num;
        } else {
          std::cout << (char)num;
          break;
        }
      }
    }
    if (istr) std::cout << std::endl;
  } else {
    bool mid = false;
    char c;
    while (is->get(c)) {
      if (mid) std::cout << ' ';
      int32_t cc = (unsigned char)c;
      int32_t num = (cc >> 4);
      if (num < 10) {
        std::cout << (char)('0' + num);
      } else {
        std::cout << (char)('a' + num - 10);
      }
      num = (cc & 0x0f);
      if (num < 10) {
        std::cout << (char)('0' + num);
      } else {
        std::cout << (char)('a' + num - 10);
      }
      mid = true;
    }
    std::cout << std::endl;
  }
  return 0;
}


// perform comp command
static int32_t proccomp(const char* file, int32_t mode, bool dec) {
  const char* istr = file && *file == '@' ? file + 1 : NULL;
  std::istream *is;
  std::ifstream ifs;
  std::istringstream iss(istr ? istr : "");
  if (file) {
    if (istr) {
      is = &iss;
    } else {
      ifs.open(file, std::ios_base::in | std::ios_base::binary);
      if (!ifs) {
        eprintf("%s: %s: open error\n", g_progname, file);
        return 1;
      }
      is = &ifs;
    }
  } else {
    is = &std::cin;
  }
  std::ostringstream oss;
  char c;
  while (is->get(c)) {
    oss.put(c);
  }
  bool err = false;
  const std::string& ostr = oss.str();
  switch (mode) {
    default: {
      kc::Zlib::Mode zmode;
      switch (mode) {
        default: zmode = kc::Zlib::RAW; break;
        case 1: zmode = kc::Zlib::DEFLATE; break;
        case 2: zmode = kc::Zlib::GZIP; break;
      }
      if (dec) {
        size_t zsiz;
        char* zbuf = kc::Zlib::decompress(zmode, ostr.data(), ostr.size(), &zsiz);
        if (zbuf) {
          std::cout.write(zbuf, zsiz);
          delete[] zbuf;
        } else {
          eprintf("%s: decompression failed\n", g_progname);
          err = true;
        }
      } else {
        size_t zsiz;
        char* zbuf = kc::Zlib::compress(zmode, ostr.data(), ostr.size(), &zsiz);
        if (zbuf) {
          std::cout.write(zbuf, zsiz);
          delete[] zbuf;
        } else {
          eprintf("%s: compression failed\n", g_progname);
          err = true;
        }
      }
      break;
    }
  }
  return err ? 1 : 0;
}


// perform hash command
static int32_t prochash(const char* file, int32_t mode) {
  const char* istr = file && *file == '@' ? file + 1 : NULL;
  std::istream *is;
  std::ifstream ifs;
  std::istringstream iss(istr ? istr : "");
  if (file) {
    if (istr) {
      is = &iss;
    } else {
      ifs.open(file, std::ios_base::in | std::ios_base::binary);
      if (!ifs) {
        eprintf("%s: %s: open error\n", g_progname, file);
        return 1;
      }
      is = &ifs;
    }
  } else {
    is = &std::cin;
  }
  std::ostringstream oss;
  char c;
  while (is->get(c)) {
    oss.put(c);
  }
  const std::string& ostr = oss.str();
  switch (mode) {
    default: {
      uint64_t hash = kc::hashmurmur(ostr.data(), ostr.size());
      iprintf("%016llx\n", (unsigned long long)hash);
      break;
    }
    case 1: {
      uint64_t hash = kc::hashfnv(ostr.data(), ostr.size());
      iprintf("%016llx\n", (unsigned long long)hash);
      break;
    }
    case 2: {
      char name[kc::NUMBUFSIZ];
      uint32_t hash = kc::hashpath(ostr.data(), ostr.size(), name);
      iprintf("%s\t%08lx\n", name, (unsigned long)hash);
      break;
    }
  }
  return 0;
}


// perform conf command
static int32_t procconf(int32_t mode) {
  switch (mode) {
    case 'v': {
      iprintf("%s\n", kc::VERSION);
      break;
    }
    case 'i': {
      iprintf("%s\n", _KC_APPINC);
      break;
    }
    case 'l': {
      iprintf("%s\n", _KC_APPLIBS);
      break;
    }
    case 'p': {
      iprintf("%s\n", _KC_BINDIR);
      break;
    }
    default: {
      iprintf("VERSION: %s\n", kc::VERSION);
      iprintf("LIBVER: %d\n", kc::LIBVER);
      iprintf("LIBREV: %d\n", kc::LIBREV);
      iprintf("FMTVER: %d\n", kc::FMTVER);
      iprintf("SYSNAME: %s\n", kc::SYSNAME);
      iprintf("BIGEND: %d\n", kc::BIGEND);
      iprintf("CLOCKTICK: %d\n", kc::CLOCKTICK);
      iprintf("PAGESIZE: %d\n", kc::PAGESIZE);
      iprintf("TYPES: void*=%d short=%d int=%d long=%d long_long=%d size_t=%d"
              " float=%d double=%d long_double=%d\n",
              (int)sizeof(void*), (int)sizeof(short), (int)sizeof(int), (int)sizeof(long),
              (int)sizeof(long long), (int)sizeof(size_t),
              (int)sizeof(float), (int)sizeof(double), (int)sizeof(long double));
      if (std::strcmp(_KC_PREFIX, "*")) {
        iprintf("prefix: %s\n", _KC_PREFIX);
        iprintf("includedir: %s\n", _KC_INCLUDEDIR);
        iprintf("libdir: %s\n", _KC_LIBDIR);
        iprintf("bindir: %s\n", _KC_BINDIR);
        iprintf("libexecdir: %s\n", _KC_LIBEXECDIR);
        iprintf("appinc: %s\n", _KC_APPINC);
        iprintf("applibs: %s\n", _KC_APPLIBS);
      }
      break;
    }
  }
  return 0;
}



// END OF FILE
