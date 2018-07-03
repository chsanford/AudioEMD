#! /bin/bash

#sudo apt-get install speex
#sudo apt-get install speex-*

sudo apt-get install build-essential cmake

mkdir codecs/codec2
svn co https://svn.code.sf.net/p/freetel/code/codec2/branches/0.8/ codecs/codec2

cd codecs/codec2
mkdir build_linux
cd build_linux
cmake ../

sudo make
sudo make install

cd ../..