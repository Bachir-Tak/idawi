package idawi.knowledge_base;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import idawi.Component;
import toools.util.Date;

public abstract class Info implements Serializable {
	public double date;

	// how much the accuracy decreases per second
	public double decreaseRatio = 0.9;

	public Info(double date) {
		this.date = date;
	}

	public Info() {
		this(Date.time());
	}

	public double reliability(double now) {
		var duration = now - date;
		return Math.pow(decreaseRatio, duration);
	}

	public boolean isNewerThan(Info b) {
		return date > b.date;
	}

	public final boolean involves(Component d) {
		AtomicBoolean found = new AtomicBoolean();

		exposeComponent(c -> {
			found.set(c.equals(d));
			return found.get();
		});

		return found.get();
	}

	public abstract void exposeComponent(Predicate<Component> p);

	public final void forEachComponent(Consumer<Component> consumer) {
		exposeComponent(c -> {
			consumer.accept(c);
			return false;
		});
	}

	public int distanceFrom(Component c, PredecessorTable predecessors) {
		class A {
			int d = Integer.MAX_VALUE;
		}
		A a = new A();

		forEachComponent(d -> a.d = Math.min(a.d, predecessors.distance(c, d)));
		return a.d;
	}

	public double relevance(Component c, double now, PredecessorTable predecessors) {
		return reliability(now) * 1 / (1 + distanceFrom(c, predecessors));
	}

	public void update(Info i) {
		this.date = i.date;
		this.decreaseRatio = (decreaseRatio + i.decreaseRatio) / 2;
	}

	@Override
	public String toString() {
		return "info reliability: " + reliability(Date.time());
	}
}
