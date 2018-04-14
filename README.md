# Building
To build, you must have the CPLEX jar (free for academia) in `src/main/resources/cplex.jar`.
You must also have a sufficiently modern version of the java development toolkit (JDK) installed.

Run ./install_dependencies.sh to install all dependencies (build systems) for this project.  This script relies on package managers; if your system is not supported by the script, please inspect this file and manually install the dependencies.  You may need to run this script with root permissions.  It has been tested only on the Ubuntu 17.10 operating system; other operating systems may not be supported.  To obtain the required audio software (source files), run ./download_audio_sources.sh.  Then, to build the required audio software, run ./build_audio_binaries.sh.  This script relies on compilers and other software needed to build the audio binaries, but should be relatively platform-independent.

After installing required dependencies and building required software, run `mvn package` to build.

# Running
## Codec Experiments
To run a typical progressive sampling experiment, where the program learns directly from a directory of samples, run `mvn exec:java -Dexec.args="codec ps-directory SAMPLE/DIRECTORY"`. 

To precompute criterion data about a directory of samples, run `mvn exec:java -Dexec.args="codec fill-csv SAMPLE/DIRECTORY OUTPUT/CSV"`. 

To run progressive sampling on the CSV computed in the above procedure and output information into a new CSV, run `mvn exec:java -Dexec.args="codec ps-csv INPUT/CSV OUTPUT/CSV"`.

Ensure all data are in signed 16 bit little endian mono wav at 48000 hz.
