package org.zalando.logbook;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class LogbookBenchmark {

	@Benchmark
	public Object autoconfigurationRequest(LogbookState state) throws IOException {
		Logbook logbook = state.getAutoconfigurationLogbook();
		
		return logbook.process(state.getRequest()).write();
	}
	
	@Benchmark
	public void autoconfigurationRequestResponse(LogbookState state) throws IOException {
		Logbook logbook = state.getAutoconfigurationLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}
	
	@Benchmark
	public Object autoconfigurationJsonRequest(LogbookState state) throws IOException {
		Logbook logbook = state.getAutoconfigurationJsonLogbook();
		
		return logbook.process(state.getRequest()).write();
	}
	
	@Benchmark
	public void autoconfigurationJsonRequestResponse(LogbookState state) throws IOException {
		Logbook logbook = state.getAutoconfigurationJsonLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}	
}
