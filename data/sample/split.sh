#! /bin/bash

set -e

inputdir=$1
outputdir=$2
segment_time=$3
name=$4

mkdir -p $outputdir

numfiles=$(ls -f $inputdir | wc -l)
numfiles=$((numfiles-2))
count=0

for f in $inputdir/*
do
        count=$((count+1))
        filename=$(basename "${f}")
        echo "Processing $count of $numfiles: $filename"
        echo "Command: ffmpeg -i $f -f segment -segment_time $segment_time -acodec pcm_s16le -ac 1 -ar 48000 $outputdir/$name\_$count\_%03d.wav -loglevel quiet"
        ffmpeg -i "$f" -f segment -segment_time $segment_time -acodec pcm_s16le -ac 1 -ar 48000 $outputdir/$name\_$count\_%03d.wav -loglevel quiet
done

