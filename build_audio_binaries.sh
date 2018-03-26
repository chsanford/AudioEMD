#! /bin/bash

set -e

###########
#Denoisers:

#TODO download RNNoise

#Build RNNoise in a subshell
(
cd denoising_algs/rnnoise
./autogen.sh
./configure
make
sudo make install
)

