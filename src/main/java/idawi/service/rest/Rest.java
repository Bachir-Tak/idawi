package idawi.service.rest;

import idawi.Component;
import idawi.service.ServiceManager;
import idawi.service.ServiceManager.ensureStarted;

public class Rest {

	public static void main(String[] args) throws Throwable {
		Component a = new Component();
		a.lookup(ServiceManager.class).lookup(ensureStarted.class).f(RESTService.class);
		a.lookup(RESTService.class).startHTTPServer();
	}
}