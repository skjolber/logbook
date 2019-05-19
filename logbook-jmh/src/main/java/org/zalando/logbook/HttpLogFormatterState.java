package org.zalando.logbook;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.json.JsonHttpLogFormatter;
import org.zalando.logbook.json.StreamingHttpLogFormatter;
import org.zalando.logbook.json.StreamingJsonHttpLogFormatter;

@State(Scope.Benchmark)
public class HttpLogFormatterState {

	private NoopHttpLogFormatter noopHttpLogFormatter = new NoopHttpLogFormatter();
	
	private JsonHttpLogFormatter jsonHttpLogFormatter = new JsonHttpLogFormatter();
	private HttpLogFormatter defaultHttpLogFormatter = new DefaultHttpLogFormatter();
	
	private StreamingJsonHttpLogFormatter streamingJsonHttpLogFormatter = new StreamingJsonHttpLogFormatter();
	private StreamingHttpLogFormatter streamingHttpLogFormatter = new StreamingHttpLogFormatter();
	
	public JsonHttpLogFormatter getJsonHttpLogFormatter() {
		return jsonHttpLogFormatter;
	}

	public StreamingJsonHttpLogFormatter getStreamingJsonHttpLogFormatter() {
		return streamingJsonHttpLogFormatter;
	}

	public HttpLogFormatter getDefaultHttpLogFormatter() {
		return defaultHttpLogFormatter;
	}

	public StreamingHttpLogFormatter getStreamingHttpLogFormatter() {
		return streamingHttpLogFormatter;
	}

	public NoopHttpLogFormatter getNoopHttpLogFormatter() {
		return noopHttpLogFormatter;
	}
}
