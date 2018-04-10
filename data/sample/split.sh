#! /bin/bash

set -e

inputdir=$1
outputdir=$2
segment_time=$3
filetype=$4
name=$5

mkdir -p $outputdir

numfiles=$(ls -f $inputdir | wc -l)
numfiles=$((numfiles-2))
count=0

for f in $inputdir/*
do
        count=$((count+1))
        filename=$(basename "${f}")
        echo "Processing $count of $numfiles: $filename"
        echo "Command: ffmpeg -i $f -f segment -segment_time $segment_time -c copy $outputdir/$name\_$count\_%03d.$filetype -loglevel quiet"
        ffmpeg -i "$f" -f segment -segment_time $segment_time -c copy $outputdir/$name\_$count\_%03d.$filetype -loglevel quiet
done

