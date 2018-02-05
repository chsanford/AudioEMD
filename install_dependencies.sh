#! /bin/bash

set -e

####################
#Broad Dependencies:

#Attempt to install these from the command line.

packages="autoconf make sox maven"

#Package manager:
#Default to the false command, which will signal an error.
pkman=false

#Try to select package manager based on OS.
if [[ "$OSTYPE" == 'linux-gnu' ]]; then
  pkman="sudo apt-get install"
elif [[ "$unamestr" == "freebsd"* ]]; then
  pkman="pkg"
elif [[ "$unamestr" == "darwin"* ]]; then
  pkman="brew install"
fi

echo $pkman $packages
$pkman $packages

###########
#Denoisers:

#TODO download RNNoise

#Build RNNoise in a subshell
(
cd denoising-algs/rnnoise
./autogen.sh
./configure
make
make install
)

