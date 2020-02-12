package org.rtm.commons;

import java.util.Collection;

public interface Measurement<T extends Datapoint> {

	public OrderedIdentifier getIdentifier();

	public void setIdentifier(OrderedIdentifier id);

	public Collection<Datapoint> getData();

	public void setData(Collection<Datapoint> data);
}
