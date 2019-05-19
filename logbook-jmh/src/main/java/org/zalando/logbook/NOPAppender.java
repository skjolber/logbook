package org.zalando.logbook;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

final public class NOPAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent eventObject) {
    	if(eventObject == null || eventObject.getMessage() == null) {
    		throw new RuntimeException();
    	}
    }
}
