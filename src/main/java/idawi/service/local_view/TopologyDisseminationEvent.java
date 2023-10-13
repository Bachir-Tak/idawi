package idawi.service.local_view;

import idawi.Event;
import idawi.PointInTime;
import idawi.RuntimeEngine;
import idawi.service.local_view.LocalViewService.markLinkActive;

class TopologyDisseminationEvent extends Event<PointInTime> {

	private final LocalViewService c;

	public TopologyDisseminationEvent(double w, LocalViewService c) {
		super(new PointInTime(w));
		this.c = c;
	}

	@Override
	public void run() {
		c.routing().exec(LocalViewService.class, markLinkActive.class,
				c.g.randomLinks(c.disseminationSampleSize, RuntimeEngine.prng));
		RuntimeEngine.offer(new TopologyDisseminationEvent(when.time + c.disseminationInterval, c));
	}

};