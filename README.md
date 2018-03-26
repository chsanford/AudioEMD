# Building
To build, you must have the CPLEX jar (free for academia) in `src/main/resources/cplex.jar`.
You must also have a sufficiently modern version of the java development toolkit (JDK) installed.

Run ./install_dependencies.sh to install all dependencies (build systems) for this project.  This script relies on package managers; if your system is not supported by the script, please inspect this file and manually install the dependencies.

To build the required audio software, run ./build_audio_binaries.sh.  This script relies on compilers and other software needed to build the audio binaries, but should be relatively platform-independent.


Then, run `mvn package`.

# Running
To run code from the Main class, run `mvn exec:java`.
