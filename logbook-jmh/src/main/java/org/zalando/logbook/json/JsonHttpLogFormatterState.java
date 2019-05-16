package org.zalando.logbook.json;

import java.time.Duration;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.jmh.DefaultCorrelation;
import org.zalando.logbook.jmh.DefaultPrecorrelation;
import org.zalando.logbook.json.JsonHttpLogFormatter;

@State(Scope.Benchmark)
public class JsonHttpLogFormatterState {

    private final HttpLogFormatter unit = new JsonHttpLogFormatter();

	private HttpResponse response;
	private Correlation correlation;
	
	private HttpRequest request;
	private Precorrelation precorrelation; 
	
	@Setup(Level.Trial)
    public void setUp() {
        response = MockHttpResponse.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");
        correlation = new DefaultCorrelation("id", Duration.ofMillis(100));
        
        request = MockHttpRequest.create()
                .withContentType("application/json")
                .withBodyAsString("{\"name\":\"Bob\"}");
        precorrelation = new DefaultPrecorrelation("id", null);
    }
	
	public HttpLogFormatter getUnit() {
		return unit;
	}
	
	public Correlation getCorrelation() {
		return correlation;
	}
	
	public Precorrelation getPrecorrelation() {
		return precorrelation;
	}
	
	public HttpRequest getRequest() {
		return request;
	}
	
	public HttpResponse getResponse() {
		return response;
	}
}
