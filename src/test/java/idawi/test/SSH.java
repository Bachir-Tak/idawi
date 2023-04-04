package idawi.test;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import idawi.Component;
import idawi.deploy.DeployerService;
import idawi.deploy.DeployerService.RemoteDeploymentRequest;
import idawi.messaging.Message;
import toools.io.Cout;

public class SSH {
	public static void main(String[] args) throws Exception {
		new SSH().pingViaSSH();
	}

	@Test
	public void pingViaSSH() throws IOException {
		Cout.debugSuperVisible("Starting test");

		// creates a component in this JVM
		Component c1 = new Component();

		// and deploy another one in a separate JVM
		// they will communicate through standard streams
		var c2 = new Component("c2");
		var deployReq = new RemoteDeploymentRequest();
		deployReq.ssh.host = "musclotte.inria.fr";

		c1.lookup(DeployerService.class).deployRemotely(Set.of(deployReq),
				rsyncOut -> System.out.println("rsync: " + rsyncOut),
				rsyncErr -> System.err.println("rsync: " + rsyncErr), p -> System.out.println("ok"));

		// asks the master to ping the other component
		Message pong = c1.bb().ping(c2).poll_sync();
		System.out.println("pong: " + pong);

		// be sure it got an answer
		assertNotEquals(null, pong);

		// clean
		Component.componentsInThisJVM.clear();
	}
}
