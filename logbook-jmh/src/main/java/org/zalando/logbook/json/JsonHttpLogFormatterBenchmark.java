package org.zalando.logbook.json;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class JsonHttpLogFormatterBenchmark {

	@Benchmark
	public void benchmarkFormatResponse(JsonHttpLogFormatterState state) throws IOException {
		state.getUnit().format(state.getCorrelation(), state.getResponse());
	}
	
	@Benchmark
	public void benchmarkFormatRequest(JsonHttpLogFormatterState state) throws IOException {
		state.getUnit().format(state.getPrecorrelation(), state.getRequest());
	}
	
}
