# Pyfuscator



Pyfuscator is a tool for obfuscating Python code. It renames the variables, functions, and classes to make the code harder to reverse engineer while maintaining functionality.



## Technologies

- Java (JDK 11+)

- Python (3.8+)

- Maven



## Setup

1. Install JDK and Maven.

2. Install Python and ensure `pip` is available.



## Build and Run

### Building the project

1. Compile and package the Java code:

   - `mvn clean package`



### Running the project

1. Run the `.jar` file:

   - `java -jar target\obfuscator.jar --input <file> --output <file> [options]`



### Required Flags

- `--input <file>`: Specifies the input Python file.

- `--output <file>`: Specifies the output file.



### Optional Flags

- `--var-length <n>`: Sets the variable name length (default: 8).

- `--var-prefix <str>`: Sets the variable prefix (default: 'v').

- `--seed <n>`: Sets the random seed.

- `--preserve-builtins`: Prevents renaming of built-in functions (default).

- `--no-preserve-builtins`: Allows renaming of built-in functions.

- `--preserve-imports`: Prevents renaming of imports (default).

- `--keep-temp`: Keeps temporary files.

- `--verbose`: Enables verbose output.

- `--rename-functions`: Renames functions.

- `--rename-classes`: Renames classes.

- `--help, -h`: Displays help information.



## Run Python Component

1. Run the Python script:

   - `python path\to\script.py`



## What I Learned

- How to compile and run Java projects using Maven.

- How to integrate Python scripts into a Java-based project, including adding it to the final standalone .jar file.

- How to navigate AST (Abstract Syntax Tree) objects in the form of a JSON using a custom node class.

- A few different objects and methods in java (ex. consumer and stacks)

- Basics of how to manage the dependencies and project structure.



## Note

- Ensure all dependencies are installed before running the project. (Or use the release given for standalone jar/exe (going to add that later))
