#! /bin/bash

set -e

###########
#Denoisers:

#Build RNNoise in a subshell
(
  cd denoising_algs/rnnoise
  ./autogen.sh
  ./configure
  make
  #sudo make install
)


#no need to build NoNoise


#Build gstpeaq in a subshell
(
  cd comparison_algs/gstpeaq
  ./autogen.sh
  #./configure --with-gstreamer=1.0 --enable-man
  make
  #sudo make install
)
