#! /bin/bash

set -e

####################
#Broad Dependencies:

#Attempt to install these from the command line.

packages="autoconf make sox maven git2cl gtk-doc-tools gstreamer1.0 libgstreamer-plugins-base1.0-dev"

#peaq may also want these:
#git2cl gtk-doc-tools w3-dtd-mathml libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev gstreamer1.0-tools gstreamer1.0-plugins-base

#Package manager:
#Default to the false command, which will signal an error.
pkman=false

unamestr=$(uname -s)
echo $unamestr

#Try to select package manager based on OS.
#if [[ "$OSTYPE" == "linux-gnu" ]]; then
case "${unamestr}" in
  Linux*) pkman="apt-get install";;
  FreeBSD*) pkman="pkg";;
  Darwin*) pkman="brew install";;
  CYGWIN*) pkman=false;; #TODO
  MINGW*) pkman=false;;
esac

echo $pkman $packages
$pkman $packages

##############################
#Inconsistently Named Packages

if [[ "$OSTYPE" == "linux-gnu" ]]; then
  packages=""
elif [[ "$unamestr" == "freebsd"* ]]; then
  packages=""
elif [[ "$unamestr" == "darwin"* ]]; then
  packages=""
fi

if [[ ! $packages == "" ]]; then
  echo $pkman $packages
  $pkman $packages
fi
