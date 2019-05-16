# logbook-jmh
Module for benchmarking using the JMH framework.

## Introduction
Benchmarking can be complicated, due to the dynamic nature of the JVM Hotspot implementation. Use warmups to compansate for this.

Note especially that the JVM Hotspot is affected by the classes it loads. So, for instance, it is able to better optimize when there is only one implementation of an interface in use. Configure using forks (one is sufficient) so that each benchmark runs alone. 

Add the benchmarks you wish to compare to the same class, so they're presented together in the visualizer.

## Running 
First disable CPU turbo / boost in BIOS, and close active programs competing for CPU resources.

### Command line
Build using command

```
mvn clean package
```

Then run all benchmarks using command

```
java -jar benchmarks.jar -rf json
```

For individual benchmarks, run

```
java -jar benchmarks.jar MyBenchmark -rf json
```

where `MyBenchmark` is the class name (no package qualifier necessary).

### From IDE
Each benchmark can be run as a standalone program (using main(..) method. This is less accurate, but convenient during active development.