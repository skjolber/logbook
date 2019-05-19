package org.zalando.logbook;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class LogbookBenchmark {

	@Benchmark
	public void jsonStreaming(LogbookState state) throws IOException {
		Logbook logbook = state.getLogstashStreamingJsonHttpLogFormatterLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}	

	@Benchmark
	public void json(LogbookState state) throws IOException {
		Logbook logbook = state.getAutoconfigurationLogstashLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}	
	
	@Benchmark
	public void plainStreaming(LogbookState state) throws IOException {
		Logbook logbook = state.getStreamingAutoconfigurationLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}
	
	@Benchmark
	public void noop(LogbookState state) throws IOException {
		Logbook logbook = state.getNoopHttpLogFormatterLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}
	
	@Benchmark
	public void plain(LogbookState state) throws IOException {
		Logbook logbook = state.getAutoconfigurationLogbook();
		
		logbook.process(state.getRequest()).write().process(state.getResponse()).write();
	}
	
	public static void main(String[] args) throws RunnerException {
	    Options options = new OptionsBuilder().include(LogbookBenchmark.class.getSimpleName())
	            .forks(1).build();
	    new Runner(options).run();
	}	
}
