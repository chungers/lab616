# DO NOT EDIT. AUTOMATICALLY GENERATED.

src/multicast.lo: src/multicast.c .make.dirs include/tcn.h include/tcn_api.h
src/poll.lo: src/poll.c .make.dirs include/tcn.h include/tcn_api.h
src/sslutils.lo: src/sslutils.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/lock.lo: src/lock.c .make.dirs include/tcn.h include/tcn_api.h
src/ssl.lo: src/ssl.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/mmap.lo: src/mmap.c .make.dirs include/tcn.h include/tcn_api.h
src/network.lo: src/network.c .make.dirs include/tcn.h include/tcn_api.h
src/sslnetwork.lo: src/sslnetwork.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/user.lo: src/user.c .make.dirs include/tcn.h include/tcn_api.h
src/os.lo: src/os.c .make.dirs include/tcn.h include/tcn_api.h
src/misc.lo: src/misc.c .make.dirs include/tcn.h include/tcn_api.h
src/dir.lo: src/dir.c .make.dirs include/tcn.h include/tcn_api.h
src/bb.lo: src/bb.c .make.dirs include/tcn.h include/tcn_api.h
src/info.lo: src/info.c .make.dirs include/tcn.h include/tcn_api.h
src/file.lo: src/file.c .make.dirs include/tcn.h include/tcn_api.h
src/error.lo: src/error.c .make.dirs include/tcn.h include/tcn_api.h
src/thread.lo: src/thread.c .make.dirs include/tcn.h include/tcn_api.h
src/sslinfo.lo: src/sslinfo.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/sslcontext.lo: src/sslcontext.c .make.dirs include/tcn.h include/tcn_api.h include/ssl_private.h
src/proc.lo: src/proc.c .make.dirs include/tcn.h include/tcn_api.h
src/address.lo: src/address.c .make.dirs include/tcn.h include/tcn_api.h
src/pool.lo: src/pool.c .make.dirs include/tcn.h include/tcn_api.h
src/jnilib.lo: src/jnilib.c .make.dirs include/tcn.h include/tcn_api.h include/tcn_version.h
src/stdlib.lo: src/stdlib.c .make.dirs include/tcn.h include/tcn_api.h
src/shm.lo: src/shm.c .make.dirs include/tcn.h include/tcn_api.h

OBJECTS_all = src/multicast.lo src/poll.lo src/sslutils.lo src/lock.lo src/ssl.lo src/mmap.lo src/network.lo src/sslnetwork.lo src/user.lo src/os.lo src/misc.lo src/dir.lo src/bb.lo src/info.lo src/file.lo src/error.lo src/thread.lo src/sslinfo.lo src/sslcontext.lo src/proc.lo src/address.lo src/pool.lo src/jnilib.lo src/stdlib.lo src/shm.lo

os/unix/uxpipe.lo: os/unix/uxpipe.c .make.dirs include/tcn.h include/tcn_api.h
os/unix/system.lo: os/unix/system.c .make.dirs include/tcn.h include/tcn_api.h

OBJECTS_os_unix = os/unix/uxpipe.lo os/unix/system.lo

OBJECTS_unix = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_aix = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_beos = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_os2 = $(OBJECTS_all) $(OBJECTS_os_unix)

OBJECTS_os390 = $(OBJECTS_all) $(OBJECTS_os_unix)

HEADERS = $(top_srcdir)/include/tcn_version.h $(top_srcdir)/include/ssl_private.h $(top_srcdir)/include/tcn_api.h $(top_srcdir)/include/tcn.h

SOURCE_DIRS = src os/unix $(EXTRA_SOURCE_DIRS)

BUILD_DIRS = os os/unix src

.make.dirs: $(srcdir)/build-outputs.mk
	@for d in $(BUILD_DIRS); do test -d $$d || mkdir $$d; done
	@echo timestamp > $@
