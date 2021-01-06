package idawi.ui.cmd;

import java.util.Set;
import java.util.function.Consumer;

import idawi.Component;
import idawi.ComponentDescriptor;
import idawi.Message;
import idawi.RegistryService;
import idawi.service.PingPong;

public class tracerouteBackend extends CommandBackend {

	@Override
	public void runOnServer(Component n, Consumer<Object> out) throws Throwable {
		var parms = cmdline.findParameters();
		Set<ComponentDescriptor> to = n.lookupService(RegistryService.class).lookupByRegexp(parms.get(0));

		for (ComponentDescriptor t : to) {
			out.accept("ping " + t);
			Message pong = n.lookupService(PingPong.class).ping(t, 1000);

			if (pong == null) {
				out.accept("No pong received. :(");
				return;
			}

			Message ping = (Message) pong.content;
			out.accept("forward route:  " + ping.route);
			out.accept("backward route: " + pong.route);
			double time = pong.receptionDate - ((Message) pong.content).route.last().emissionDate;
			out.accept("time: " + time + "s");
		}
	}
}
