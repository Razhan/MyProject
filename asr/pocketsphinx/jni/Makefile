TARGET_SWIG_JAVA_OUTPUT=../src/main/java/edu/cmu/pocketsphinx/

android: swig_build ndk_cleanbuild

ndk_debugbuild:
	@echo NDK build using $(ANDROID_NDK)
	$(ANDROID_NDK)/ndk-build NDK_DEBUG=1

ndk_cleanbuild:
	@echo NDK build using $(ANDROID_NDK)
	$(ANDROID_NDK)/ndk-build -B

swig_build:
	@echo SWIG build
	swig-1.3 -java -package edu.cmu.pocketsphinx -outdir $(TARGET_SWIG_JAVA_OUTPUT) pocketsphinx.i


SPHINX_PATH=$(HOME)/Repos
POCKETSPHINX_PPATH=$(SPHINX_PATH)/pocketsphinx
SPHINXBASE_PATH=$(SPHINX_PATH)/sphinxbase
MAC_LIB_OUTPUT=../libs-mac-x64
CFLAGS := -g -Wall -DPIC -fPIC
CPPFLAGS := -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I$(SPHINXBASE_PATH)/include -I$(POCKETSPHINX_PPATH)/include

mac: swig_build libpocketsphinx_jni.jnilib

libpocketsphinx_jni.jnilib: pocketsphinx_wrap.o
	mkdir -p $(MAC_LIB_OUTPUT)
	$(CC) -dynamiclib -v -o $(MAC_LIB_OUTPUT)/$@ pocketsphinx_wrap.o  $(SPHINXBASE_PATH)/src/libsphinxbase/.libs/libsphinxbase.a $(SPHINXBASE_PATH)/src/libsphinxad/.libs/libsphinxad.a $(POCKETSPHINX_PPATH)/src/libpocketsphinx/.libs/libpocketsphinx.a -liconv
	-rm pocketsphinx_wrap.o
