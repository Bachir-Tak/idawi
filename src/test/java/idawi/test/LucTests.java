package idawi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import idawi.Component;
import idawi.OperationParameterList;
import idawi.Service;
import idawi.deploy.DeployerService;
import idawi.deploy.DeployerService.ExtraJVMDeploymentRequest;
import idawi.messaging.Message;
import idawi.routing.BlindBroadcasting;
import idawi.routing.TargetComponents;
import idawi.service.DemoService;
import idawi.service.web.WebService;
import idawi.transport.SharedMemoryTransport;
import toools.io.Cout;
import toools.net.NetUtilities;

public class LucTests {

	public static void main(String[] args) {
		var a = new StringBuilder("toto");
		var b = new StringBuilder("toto");
		System.out.println(a.equals(b));
		System.out.println(a.hashCode());
	}

	public static void main2(String[] args) throws Throwable {
		Cout.debugSuperVisible("Starting test main2");

		// trigger the creation of a component from its description
		Component c1 = new Component("c1");

		// a shortcut for creating a component from a description
		Component c2 = new Component("c2");

		// connect those 2 components
		c1.lookup(SharedMemoryTransport.class).connectTo(c2);

		// ask c1 to ping c2
		Message pong = c1.bb().ping(c2).poll_sync();
		System.out.println(pong);
		Component.stopPlatformThreads();
	}

	@Test
	public void twoComponentsConversation() {
		Cout.debugSuperVisible("Starting test twoComponentsConversation");
		// trigger the creation of a component from its description
		Component c1 = new Component("c1");

		// a shortcut for creating a component from a description
		Component c2 = new Component("c2");

		// connect those 2 components
		c1.lookup(SharedMemoryTransport.class).connectTo(c2);

		// ask c1 to ping c2
		Message pong = c1.bb().ping(c2).poll_sync();

		// be sure c1 got an answer
		assertNotEquals(null, pong);

		// clean
		Component.componentsInThisJVM.clear();
	}

	@Test
	public void manyMessages() throws IOException {
		Cout.debugSuperVisible("Starting test manyMessages");
		Component c1 = new Component();

		var req = new ExtraJVMDeploymentRequest();
		req.target = new Component("c2d");
		c1.lookup(DeployerService.class).deployInNewJVM(req, msg -> System.out.println(msg));

		for (int i = 0; i < 100; ++i) {
			Message pong = c1.bb().ping(req.target).poll_sync();

			// be sure c1 got an answer
			assertNotEquals(null, pong);
		}

		// clean
		Component.componentsInThisJVM.clear();
	}

	@Test
	public void operationSignatures() throws Throwable {
		Cout.debugSuperVisible("Starting test operationSignatures");
		Component c1 = new Component("c1");
		Component c2 = new Component("c2");
		c1.lookup(SharedMemoryTransport.class).connectTo(c2);

		Cout.debugSuperVisible(1);

		assertEquals(5, (int) c1.lookup(BlindBroadcasting.class).exec_rpc(c2, DemoService.stringLength.class, "salut"));
		Cout.debugSuperVisible(2);

		assertEquals(53, (int) c1.lookup(BlindBroadcasting.class).exec(c2, DemoService.countFrom1toN.class, 100).returnQ
				.c().collectNResults(100).get(53));
		Cout.debugSuperVisible(3);

		assertEquals(7, (int) c1.bb().exec(c2, DemoService.countFromAtoB.class, new DemoService.Range(0, 13)).returnQ
				.c().collectNResults(13).get(7));
		Cout.debugSuperVisible(4);

		// assertEquals(7, c2.DemoService.countFromAtoB(0, 13).get(7).content);

		Component.componentsInThisJVM.clear();
	}

	@Test
	public void waitingFirst() {
		Cout.debugSuperVisible("Starting test waitingFirst");
		var root = new Component("root");
		List<Component> others = root.lookup(DeployerService.class).deployInThisJVM("c1", "c2");

		Set<Component> ss = new HashSet<>(others.stream().map(c -> c).toList());

		Component first = root.bb().exec(DemoService.waiting.class, null, new TargetComponents.Multicast(ss), true,
				new OperationParameterList(1)).returnQ.c().collectWhile(c -> !c.messages.isEmpty()).messages
				.get(0).route.initialEmission.transport.component;

		System.out.println(first);
//		assertEquals(7, (Double) );
		Component.componentsInThisJVM.clear();
	}

	@Test
	public void pingViaTCP() throws IOException {
		Cout.debugSuperVisible("Starting test pingViaTCP");

		// creates a component in this JVM
		Component master = new Component("master");

		// and deploy another one in a separate JVM
		// they will communicate through standard streams
		var req = new ExtraJVMDeploymentRequest();
		req.target = new Component("other_peer");
		master.lookup(DeployerService.class).deployInNewJVM(req, fdbck -> System.out.println(fdbck));

		// asks the master to ping the other component
		Message pong = new Service(master).component.bb().ping(req.target).poll_sync();
		System.out.println("***** " + pong.route);

		// be sure it got an answer
		assertNotEquals(null, pong);

		// clean
		Component.componentsInThisJVM.clear();
	}

	@Test
	public void signature() {
		Cout.debugSuperVisible("Starting signature test");
		Component c1 = new Component("c1");
		Component c2 = new Component("c2");
		c1.lookup(SharedMemoryTransport.class).connectTo(c2);

		var rom = c1.bb().exec(c2, DemoService.stringLength.class, new OperationParameterList("hello"));
		var c = rom.returnQ.collect(5, 5, cc -> {
			cc.stop = !cc.messages.resultMessages().isEmpty();
		});

		var l = c.messages;
		int len = (int) l.resultMessages().contents().get(0);
		assertEquals(5, len);

		// clean
		Component.componentsInThisJVM.clear();
	}

	@Test
	public void rest() throws IOException {
		Cout.debugSuperVisible("Starting REST test");
		Component c1 = new Component();
		var ws = c1.lookup(WebService.class);
		ws.startHTTPServer();
		NetUtilities.retrieveURLContent("http://localhost:" + ws.getPort() + "/api/" + c1);
		// clean
		Component.componentsInThisJVM.clear();
	}

	@Test
	public void multihop() {
		Cout.debugSuperVisible("Starting test multihop");
		List<Component> l = new ArrayList<>();

		for (int i = 0; i < 10; ++i) {
			l.add(new Component());
		}

		SharedMemoryTransport.chain(l, SharedMemoryTransport.class);
		var first = new Service(l.get(0));
		var last = l.get(l.size() - 1);
		Message pong = first.component.bb().ping(last).poll_sync();
		System.out.println(pong.route);
		assertNotEquals(pong, null);

		// clean
		Component.componentsInThisJVM.clear();
	}

}