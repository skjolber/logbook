package org.zalando.logbook.jmh;

import org.zalando.logbook.Correlation;
import org.zalando.logbook.Precorrelation;

public class DefaultPrecorrelation implements Precorrelation {

	private String id;
	private Correlation correlation;
	
	public DefaultPrecorrelation(String id, Correlation correlation) {
		super();
		this.id = id;
		this.correlation = correlation;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Correlation correlate() {
		return correlation;
	}

}
