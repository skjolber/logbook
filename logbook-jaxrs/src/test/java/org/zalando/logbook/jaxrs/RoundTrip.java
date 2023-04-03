package org.zalando.logbook.jaxrs;

import lombok.Value;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;

@Value
class RoundTrip {
    HttpRequest clientRequest;
    HttpResponse clientResponse;
    HttpRequest serverRequest;
    HttpResponse serverResponse;
}
