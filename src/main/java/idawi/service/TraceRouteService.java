package idawi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import idawi.Component;
import idawi.InnerClassOperation;
import idawi.Service;
import idawi.messaging.MessageQueue;
import idawi.routing.Route;
import idawi.routing.TargetComponents;

/**
 * Sends an empty message on a queue that is created specifically for the peer
 * to bench.
 */

public class TraceRouteService extends Service {
	public TraceRouteService(Component node) {
		super(node);
		registerOperation(new traceroute());
	}

	public class traceroute extends InnerClassOperation {
		@Override
		public void impl(MessageQueue in) throws Throwable {
			var msg = in.poll_sync();
			reply(msg, msg.route);
		}

		@Override
		public String getDescription() {
			return "returns the route taken by the trigger message";
		}
	}

	public Route traceRoute(Component t, double timeout) {
		return (Route) component.bb().exec_rpc(t, TraceRouteService.traceroute.class, null);
	}

	public Map<Component, Route> traceRoute(Set<Component> targets, double timeout) {
		var map = new HashMap<Component, Route>();
		component.bb().exec(TraceRouteService.traceroute.class, null, new TargetComponents.Multicast(targets), true,
				null).returnQ.collect(timeout, timeout, c -> {
					var target = c.messages.last().route.initialEmission().transport.component;
					var route = (Route) c.messages.last().content;
					map.put(target, route);
					c.stop = c.messages.senders().equals(targets);
				});

		return map;
	}

	@Override
	public String getFriendlyName() {
		return "traceroute";
	}

}
