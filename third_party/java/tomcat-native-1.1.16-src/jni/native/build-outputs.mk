# DO NOT EDIT. AUTOMATICALLY GENERATED.

src/address.lo: src/address.c .make.dirs include/tcn.h include/tcn_api.h
src/bb.lo: src/bb.c .make.dirs include/tcn.h include/tcn_api.h
src/dir.lo: src/dir.c .make.dirs include/tcn.h include/tcn_api.h
src/error.lo: src/error.c .make.dirs include/tcn.h include/tcn_api.h
src/file.lo: src/file.c .make.dirs include/tcn.h include/tcn_api.h
src/info.lo: src/info.c .make.dirs include/tcn.h include/tcn_api.h
src/jnilib.lo: src/jnilib.c .make.dirs include/tcn.h include/tcn_api.h include/tcn_version.h
src/lock.lo: src/lock.c .make.dirs include/tcn.h include/tcn_api.h
src/misc.lo: src/misc.c .make.dirs include/tcn.h include/tcn_api.h
src/mmap.lo: src/mmap.c .make.dirs include/tcn.h include/tcn_api.h
src/multicast.lo: src/multicast.c .make.dirs include/tcn.h include/tcn_api.h
src/network.lo: src/network.c .make.dirs include/tcn.h include/tcn_api.h
src/os.lo: src/os.c .make.dirs include/tcn.h include/tcn_api.h
src/poll.lo: src/poll.c .make.dirs include/tcn.h include/tcn_api.h
src/pool.lo: src/pool.c .make.dirs include/tcn.h include/tcn_api.h
src/proc.lo: src/proc.c .make.dirs include/tcn.h include/tcn_api.h
src/shm.lo: src/shm.c .make.dirs include/tcn.h include/tcn_api.h
src/ssl.lo: src/ssl.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/sslcontext.lo: src/sslcontext.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/sslinfo.lo: src/sslinfo.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/sslnetwork.lo: src/sslnetwork.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/sslutils.lo: src/sslutils.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/stdlib.lo: src/stdlib.c .make.dirs include/tcn.h include/tcn_api.h
src/thread.lo: src/thread.c .make.dirs include/tcn.h include/tcn_api.h
src/user.lo: src/user.c .make.dirs include/tcn.h include/tcn_api.h

OBJECTS_all = src/address.lo src/bb.lo src/dir.lo src/error.lo src/file.lo src/info.lo src/jnilib.lo src/lock.lo src/misc.lo src/mmap.lo src/multicast.lo src/network.lo src/os.lo src/poll.lo src/pool.lo src/proc.lo src/shm.lo src/ssl.lo src/sslcontext.lo src/sslinfo.lo src/sslnetwork.lo src/sslutils.lo src/stdlib.lo src/thread.lo src/user.lo

os/unix/system.lo: os/unix/system.c .make.dirs include/tcn.h include/tcn_api.h
os/unix/uxpipe.lo: os/unix/uxpipe.c .make.dirs include/tcn.h include/tcn_api.h

OBJECTS_os_unix = os/unix/system.lo os/unix/uxpipe.lo

OBJECTS_unix = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_aix = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_beos = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_os2 = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_os390 = $(OBJECTS_all) $(OBJECTS_os_unix)

os/win32/ntpipe.lo: os/win32/ntpipe.c .make.dirs include/tcn.h include/tcn_api.h
os/win32/registry.lo: os/win32/registry.c .make.dirs include/tcn.h include/tcn_api.h
os/win32/system.lo: os/win32/system.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h

OBJECTS_os_win32 = os/win32/ntpipe.lo os/win32/registry.lo os/win32/system.lo

OBJECTS_win32 = $(OBJECTS_all) $(OBJECTS_os_win32)

HEADERS = $(top_srcdir)/include/ssl_private.h $(top_srcdir)/include/tcn.h $(top_srcdir)/include/tcn_api.h $(top_srcdir)/include/tcn_version.h

SOURCE_DIRS = src os/unix os/win32 $(EXTRA_SOURCE_DIRS)

BUILD_DIRS = os os/unix os/win32 src

.make.dirs: $(srcdir)/build-outputs.mk
	@for d in $(BUILD_DIRS); do test -d $$d || mkdir $$d; done
	@echo timestamp > $@
