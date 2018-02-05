# Building
To build, you must have the CPLEX jar (free for academia) in `src/main/resources/cplex.jar`.
You must also have a sufficiently modern version of the java development toolkit (JDK) installed.

Run ./install_dependencies.sh to install all dependencies (build systems and audio software) for this project.

Then, run `mvn package`.

# Running
To run code from the Main class, run `mvn exec:java`.
