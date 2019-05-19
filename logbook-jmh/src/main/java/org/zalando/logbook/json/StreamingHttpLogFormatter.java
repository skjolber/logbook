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
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.RequestURI;

public class StreamingHttpLogFormatter implements HttpLogFormatter {

	@Override
	public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        final String correlationId = precorrelation.getId();
        
        String body = request.getBodyAsString();

        StringBuilder builder = new StringBuilder(body.length() + 2048);

        builder.append("Incoming Request: ");
        builder.append(correlationId);
        builder.append('\n');
        
        builder.append(request.getMethod());
        builder.append(' ');
        RequestURI.reconstruct(request, builder);
        builder.append(' ');
        builder.append(request.getProtocolVersion());
        builder.append('\n');
        
        writeHeaders(builder, request);

        return builder.toString();
	}

	@Override
	public String format(Correlation correlation, HttpResponse response) throws IOException {
		
        final String correlationId = correlation.getId();
        
        String body = response.getBodyAsString();

        StringBuilder builder = new StringBuilder(body.length() + 2048);

        builder.append("Outgoing Response: ");
        builder.append(correlationId);
        builder.append("\nDuration: ");
        builder.append(correlation.getDuration().toMillis());
        builder.append("ms\n");
        
        builder.append(response.getProtocolVersion());
        builder.append(' ');
        builder.append(response.getStatus());
        final String reasonPhrase = response.getReasonPhrase();
        if(reasonPhrase != null) {
        	builder.append(' ');
        	builder.append(reasonPhrase);
        }
        
        builder.append('\n');

        writeHeaders(builder, response);

        if (!body.isEmpty()) {
        	builder.append('\n');
            builder.append(body);
            builder.append('\n');
        }
        
        return builder.toString();		
	}

	private void writeHeaders(StringBuilder builder, HttpMessage httpMessage) {
        Map<String, List<String>> headers = httpMessage.getHeaders();
		
		if(!headers.isEmpty()) {
            for (Entry<String, List<String>> entry : headers.entrySet()) {
                builder.append(entry.getKey()); 
                builder.append(':');
                List<String> headerValues = entry.getValue();
                if(!headerValues.isEmpty()) {
                    for(String value : entry.getValue()) {
                        builder.append(value); 
                        builder.append(", ");
                    }
                    builder.setLength(builder.length() - 2); // discard last comma
                }
                builder.append('\n');
			}
        }
	}


}
