# logbook-jmh
Module for benchmarking using the JMH framework.

## Introduction
Performance analysis is complicated due to the dynamic nature of the JVM Hotspot implementation. 

For example the JVM Hotspot is affected by the classes it loads and better optimize when there is only one implementation of an interface in use. 

In practice much of the dynamic nature can be handled by using so-called forks and warmups. 

## Getting started 
First disable CPU turbo / boost in BIOS, and close active programs competing for CPU resources.

Download a profiler like [VisualVM](https://visualvm.github.io/) for drilling down to method level during development.

### Performance analysis
A good starting point is to have a end-to-end benchmark, which touches the most commonly used code paths. Also add a (as close as possible) no-operation / passthrough benchmark to sanity-check the upper limit on your results.

Then analyze the source code, tweak the end-to-end and noop benchmarks and/or use a profiler to identify hotspots. 

### Writing a benchmark
Once a potential hotspot is identified, capture the initial state by writing a baseline benchmark. Then add alternative implementations and benchmarks. Add the benchmarks you want to compare to the same class file, so the visualizer presents them together. 

If missing, add unit tests, so you're sure to be comparing apples to apples.
 
### Running a benchmark
Each benchmark can be run as a standalone program (using main(..) method directly from your IDE. This is less accurate than running from the command line, but convenient during active development, especially for drilling down using a profiler like [VisualVM](https://visualvm.github.io/) or such. 

#### Command line
Build using command

```
mvn clean package
```

Then run all benchmarks using command

```
java -jar target/benchmark.jar -rf json
```

For individual benchmarks, run

```
java -jar target/benchmark.jar MyBenchmark -rf json
```

where `MyBenchmark` is the class name (no package qualifier necessary).