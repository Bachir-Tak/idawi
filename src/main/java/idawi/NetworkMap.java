package idawi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import idawi.map.NetworkMapListener;

public class NetworkMap {
	private final Map<ComponentDescriptor, Set<ComponentDescriptor>> m = new HashMap<>();
	public final List<NetworkMapListener> listener = new ArrayList<>();

	public Set<ComponentDescriptor> get(ComponentDescriptor p) {
		return m.get(p);
	}

	public Set<ComponentDescriptor> peers() {
		return m.keySet();
	}

	public void add(ComponentDescriptor p) {
		m.put(p, new HashSet<>());
		listener.forEach(l -> l.newNode(p));
	}

	public void add(ComponentDescriptor a, ComponentDescriptor b) {
		if ( ! contains(a)) {
			add(a);
		}

		if ( ! contains(b)) {
			add(b);
		}

		if ( ! contains(a, b)) {
			m.get(a).add(b);
			m.get(b).add(a);
			listener.forEach(l -> l.newEdge(a, b));
		}
	}

	public boolean contains(ComponentDescriptor a, ComponentDescriptor b) {
		return contains(a) && get(a).contains(b);
	}

	public boolean contains(ComponentDescriptor a) {
		return m.containsKey(a);
	}

	public void remove(ComponentDescriptor a, ComponentDescriptor b) {
		if (contains(a, b)) {
			m.get(a).remove(b);
			m.get(b).remove(a);
			listener.forEach(l -> l.edgeRemoved(a, b));
		}
	}

	public static class Edge {
		public ComponentDescriptor a, b;
	}

	public void forEachEdge(Consumer<Edge> c) {
		for (ComponentDescriptor u : m.keySet()) {
			for (ComponentDescriptor v : m.get(u)) {
				if (u.toString().compareTo(v.toString()) < 0) {
					Edge e = new Edge();
					e.a = u;
					e.b = v;
					c.accept(e);
				}
			}
		}
	}

	public String toDot() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		toDot(pw);
		pw.flush();
		return sw.toString();
	}

	public void toDot(PrintWriter out) {
		out.println("digraph {");
		forEachEdge(e -> out.println(e.a + " -- " + e.b + ";"));
		out.println("}");
	}
}