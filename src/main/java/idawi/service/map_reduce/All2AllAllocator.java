package idawi.service.map_reduce;

import java.util.HashSet;
import java.util.List;

import idawi.Component;
import idawi.routing.TargetComponents;

public class All2AllAllocator<R> implements Allocator<R> {
	@Override
	public void assign(List<Task<R>> tasks, List<Component> workers) {
		tasks.forEach(t -> t.to = new TargetComponents.Multicast(new HashSet<>(workers)));
	}
}