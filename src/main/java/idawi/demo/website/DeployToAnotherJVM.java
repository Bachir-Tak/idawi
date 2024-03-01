package idawi.demo.website;

import java.io.IOException;

import idawi.Component;
import idawi.deploy.DeployerService;
import idawi.deploy.DeployerService.ExtraJVMDeploymentRequest;

public class DeployToAnotherJVM {
	public static void main(String[] args) throws IOException {
		// creates a component in this JVM
		var a = new Component();

		// we'll put another component in a different JVM
		var req = new ExtraJVMDeploymentRequest();
		req.target = new Component();

		a.service(DeployerService.class, true).deployInNewJVM(req, feedback -> System.out.println(feedback));
	}
}
