#! /bin/bash

set -e

mkdir -p denoising_algs

(
  cd denoising_algs
  if [ ! -d rnnoise ]; then
    git clone https://github.com/xiph/rnnoise.git
  fi
  (
    cd rnnoise
    git pull
  )
  if [ ! -d noNoise ]; then
    git clone https://github.com/srikantpatnaik/noNoise.git
  fi
  (
    cd noNoise
    git pull
  )
)

mkdir -p comparison_algs

(
  cd comparison_algs
  if [ ! -d gstpeaq ]; then
    git clone https://github.com/HSU-ANT/gstpeaq.git
  fi
  (
    cd gstpeaq
    git pull
  )
)
