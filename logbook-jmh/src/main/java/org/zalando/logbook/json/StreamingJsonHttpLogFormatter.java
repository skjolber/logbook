package org.zalando.logbook.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;

public class StreamingJsonHttpLogFormatter implements HttpLogFormatter {

	private static final String[] REPLACEMENT_CHARS;
	
	static {
		    REPLACEMENT_CHARS = new String[128];
		    for (int i = 0; i <= 0x1f; i++) {
		    	REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
		    }
		    REPLACEMENT_CHARS['"'] = "\\\"";
		    REPLACEMENT_CHARS['\\'] = "\\\\";
		    REPLACEMENT_CHARS['\t'] = "\\t";
		    REPLACEMENT_CHARS['\b'] = "\\b";
		    REPLACEMENT_CHARS['\r'] = "\\r";
		    REPLACEMENT_CHARS['\f'] = "\\f";
	  }
	
	@Override
	public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        
        String body = request.getBodyAsString();

        StringBuilder builder = new StringBuilder(body.length() + 2048);

        builder.append("{\"origin\":\"");
        if(request.getOrigin() == Origin.LOCAL) {
        	builder.append("local");
        } else {
        	builder.append("remote");
        }
        builder.append("\",\"type\":\"request\",\"correlation\":\"");
        builder.append(correlationId); 
        builder.append("\",\"protocol\":\"");
        encode(request.getProtocolVersion(), builder);
        builder.append("\",\"remote\":\"");
        encode(request.getRemote(), builder);
        builder.append("\",\"method\":\"");
        encode(request.getMethod(), builder);
        builder.append("\",\"uri\":\"");
        encode(request.getRequestUri(), builder);
        builder.append('"');

        writeHeaders(builder, request);

        writeBody(request, body, builder);

        builder.append('}');

        return builder.toString();
	}

	private void writeBody(HttpMessage message, String body, StringBuilder builder) {
		if(!body.isEmpty()) {
            builder.append(",\"body\":");

            final String contentType = message.getContentType();

            if (JsonMediaType.JSON2.test(contentType)) {
            	builder.append(body);
            } else {
                builder.append('"');
                encode(body, builder);
                builder.append('"');
            }
        }
	}

	@Override
	public String format(Correlation correlation, HttpResponse response) throws IOException {
		
        final String correlationId = correlation.getId();
        
        String body = response.getBodyAsString();

        StringBuilder builder = new StringBuilder(body.length() + 2048);

        builder.append("{\"origin\":\"");
        if(response.getOrigin() == Origin.LOCAL) {
        	builder.append("local");
        } else {
        	builder.append("remote");
        }
        builder.append("\",\"type\":\"response\",\"correlation\":\"");
        builder.append(correlationId); 
        builder.append("\",\"protocol\":\"");
        encode(response.getProtocolVersion(), builder);
        builder.append("\",\"duration\":");
        builder.append(correlation.getDuration().toMillis()); 
        builder.append(",\"status\":");
        builder.append(response.getStatus()); 

        writeHeaders(builder, response);
        writeBody(response, body, builder);
        
        builder.append('}');

        return builder.toString();		
	}

	private void writeHeaders(StringBuilder builder, HttpMessage httpMessage) {
        Map<String, List<String>> headers = httpMessage.getHeaders();
		
		if(!headers.isEmpty()) {
            builder.append(",\"headers\":{");
            
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                builder.append('"');
                encode(entry.getKey(), builder); 
                builder.append("\":[");
                List<String> headerValues = entry.getValue();
                if(!headerValues.isEmpty()) {
                    builder.append('"');
                    for(String value : entry.getValue()) {
                    	encode(value, builder); 
                        builder.append("\",\"");
                    }
                    builder.setLength(builder.length() - 2); // discard last comma and quote
                }
                builder.append("],");
			}
            builder.setCharAt(builder.length() - 1, '}'); // discard last comma
        }
	}

	private void encode(String value, StringBuilder builder) {
		int last = 0;
		int length = value.length();
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			if (c >= 128) {
				continue;
			}
			if(REPLACEMENT_CHARS[c] == null) {
				continue;
			}
			if (last < i) {
				builder.append(value, last, i);
			}
			builder.append(REPLACEMENT_CHARS[c]);
			last = i + 1;
		}
		if (last < length) {
			builder.append(value, last, length);
		}
	}
}
