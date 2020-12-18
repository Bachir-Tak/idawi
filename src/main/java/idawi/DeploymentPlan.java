package idawi;

import java.io.Serializable;

public class DeploymentPlan implements Serializable {
	private final Graph<ComponentInfo> g = new Graph<>();

	public void addChild(ComponentInfo parent, ComponentInfo child) {
		g.add(parent, child);
	}

	public void removeChild(ComponentInfo parent, ComponentInfo child) {
		g.remove(parent, child);
	}
	

}
