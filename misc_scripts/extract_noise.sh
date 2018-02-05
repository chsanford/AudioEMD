noisypath=$1
cleanpath=$2
noisepath=$3
inversepath=inversetemp

numfiles=$(ls -f $noisypath | wc -l)
numfiles=$((numfiles-2))
count=0

mkdir $inversepath

for f in $noisypath/*
do
        count=$((count+1))
        filename=$(basename "${f}")
        echo "Processing $count of $numfiles: $filename"

        sox -v -1 $cleanpath/$filename $inversepath/$filename
        sox -m $noisypath/$filename $inversepath/$filename $noisepath/$filename
done

rm -rf $inversepath