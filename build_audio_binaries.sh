#! /bin/bash

#This script compiles the dependencies pulled with download_audio_sources.sh
#The compiled software (and sources) can be removed with delete_audio_sources.sh

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
  #./configure --enable-man #This is called by ./autogen.sh
  make
  #sudo make install
)
