package idawi.transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import idawi.Component;
import idawi.RuntimeEngine;
import idawi.service.LocationService;
import idawi.service.SimulatedLocationService;
import idawi.service.local_view.Network;
import jdotgen.EdgeProps;
import jdotgen.GraphvizDriver;
import jdotgen.VertexProps;
import jdotgen.VertexProps.Shape;

public class Topologies {


	public static void chain(List<Component> l, Class<? extends TransportService> t,
			BiFunction<TransportService, TransportService, Boolean> bidi) {
		int n = l.size();

		for (int i = 1; i < n; ++i) {
			var a = l.get(i - 1).need(t);
			var b = l.get(i).need(t);
			Network.link(a, b, bidi.apply(a, b));
		}
	}

	public static void chainRandomly(Collection<Component> components, int n, Random r,
			Class<? extends TransportService> t, BiFunction<TransportService, TransportService, Boolean> bidi) {

		var l = new ArrayList<>(components);

		for (int i = 0; i < n; ++i) {
			Collections.shuffle(l, r);
			chain(l, t, bidi);
		}
	}

	public static void random(List<Component> l, int nbEdges, Class<? extends TransportService> t,
			BiFunction<TransportService, TransportService, Boolean> bidi) {
		chain(l, t, bidi);
		nbEdges -= l.size() - 1;

		while (nbEdges > 0) {
			final var a = l.get(ThreadLocalRandom.current().nextInt(l.size()));
			final var at = a.need(t);
			var b = a;
			var outN = at.outLinks().map(sl -> sl.dest.component).toList();

			while (b == a || outN.contains(b)) {
				b = l.get(ThreadLocalRandom.current().nextInt(l.size()));
			}

			var bt = b.need(t);
			Network.link(at, bt, bidi.apply(at, bt));
		}
	}

	public static void gnp(Collection<Component> components, double p, Class<? extends TransportService> t, boolean bidi, double timeIncrement) {
		int i =0;
		for (var a : components) {
			for (var b : components) {
				if (a != b && RuntimeEngine.prng.nextDouble() < p
						&& !a.need(t).outLinks().anyMatch(l -> l.dest.component.equals(b))) {
					RuntimeEngine.offer(i += timeIncrement, "add link GNP",
							() -> Network.link(a, b, WiFiDirect.class, bidi));
					//Network.link(a, b, t, false);
				}
			}
		}
	}

	public static void randomTree(List<Component> l, Class<? extends TransportService> t) {
		int n = l.size();

		for (int i = 1; i < n; ++i) {
			var from = l.get(i);
			var to = l.get(ThreadLocalRandom.current().nextInt(i));
			Network.link(from, to, t, true);
		}
	}

	public static List<Component> pick(List<Component> l, int n, Random r) {
		List<Component> out = new ArrayList<>();

		while (out.size() < n) {
			Component t = l.get(r.nextInt(l.size()));
			out.add(t);
		}

		return out;
	}

	public static void clique(Collection<Component> components, Class<? extends TransportService> t) {
		couples(components, t, (a, b) -> true);
	}

	public static void couples(Collection<Component> components, Class<? extends TransportService> t,
			BiPredicate<Component, Component> p) {
		for (var a : components) {
			for (var b : components) {
				if (a != b && p.test(a, b)) {
					Network.link(a, b, t, false);
				}
			}
		}
	}

	public static void reconnectAccordingToDistance(ArrayList<Component> components) {
		// remove invalid links
		for (var c : components) {
			var ta = c.need(SharedMemoryTransport.class);
			var linksToKill = c.localView().links().stream()
					.filter(l -> l.matches(ta, null) && distance(c, l.dest.component) > ta.emissionRange).toList();
			// ta.
		}

		// create new links
		for (var a : components) {
			for (var b : components) {
				if (a != b) {
					final var d = distance(a, b);
					var ta = a.need(SharedMemoryTransport.class);

					if (d < ta.emissionRange) {
						var tb = b.need(ta.getClass());

						if (tb != null) {
							if (ta.component.localView().links().stream().noneMatch(ll -> ll.matches(ta, tb))) {
								Network.link(ta, tb, false);
							}
						}
					}
				}
			}
		}
	}

	private static double distance(Component a, Component b) {
		return a.need(LocationService.class).getLocation()
				.distanceFrom(b.need(SimulatedLocationService.class).getLocation());
	}

	public static GraphvizDriver toDot(Collection<Component> components, OutLinks links) {
		return graphViz(components, links, c -> c);
	}

	public static GraphvizDriver graphViz(Collection<Component> components, OutLinks links,
			Function<Component, Object> id) {
		return new GraphvizDriver() {
			Set<Link> s = new HashSet<>();

			@Override
			protected void findVertices(VertexProps v, Validator f) {
				for (var c : components) {
					v.id = id.apply(c);
					v.shape = Shape.circle;
					f.f();
				}
			}

			@Override
			protected void findEdges(EdgeProps e, Validator f) {
				for (var l : links) {
					if (!s.contains(l) && components.contains(l.src.component)
							&& components.contains(l.dest.component)) {
						// search for the reverse link
						var ol = links.stream().filter(ll -> ll.matches(l.dest, l.src)).findAny().orElse(null);

						if (ol == null) {
							e.directed = true;
						} else {
							s.add(ol);
							e.directed = false;
						}

						e.from = id.apply(l.src.component);
						e.to = id.apply(l.dest.component);
						e.label = new HashSet<>(List.of(l.src.getClass(), l.dest.getClass()));
						f.f();
					}
				}
			}
		};
	}
}
