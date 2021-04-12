package idawi.service;

import java.util.Set;

import idawi.AsMethodOperation.OperationID;
import idawi.Component;
import idawi.ComponentAddress;
import idawi.ComponentDescriptor;
import idawi.MessageList;
import idawi.Service;
import toools.util.Date;

/**
 * Sends an empty message on a queue that is created specifically for the peer
 * to bench.
 */

public class ConnectionBencher extends Service {
	public ConnectionBencher(Component peer) {
		super(peer);
		registerOperation(null, in -> {
		});
	}

	@Override
	public String getFriendlyName() {
		return "bench connection speed";
	}

	public double benchLinkTo2(ComponentDescriptor peer, double timeout) {
		var to = new ComponentAddress(Set.of(peer));
		MessageList msg = exec(to, new OperationID(id, null), null).returnQ.collect();
		return Date.time() - msg.last().route.last().emissionDate;
	}
}
