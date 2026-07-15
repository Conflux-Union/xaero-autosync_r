package cn.net.rms.xaeromapsync_r.xaero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class XaeroLocalWaypointReadResult {
	public enum Outcome {
		LOADED,
		NOT_READY,
		UNAVAILABLE,
		FAILED
	}

	private final Outcome outcome;
	private final List<XaeroLocalWaypoint> waypoints;
	private final String message;

	private XaeroLocalWaypointReadResult(Outcome outcome, List<XaeroLocalWaypoint> waypoints, String message) {
		this.outcome = outcome;
		this.waypoints = Collections.unmodifiableList(new ArrayList<>(waypoints));
		this.message = message;
	}

	static XaeroLocalWaypointReadResult loaded(List<XaeroLocalWaypoint> waypoints) {
		return new XaeroLocalWaypointReadResult(Outcome.LOADED, waypoints, "Xaero local waypoints loaded");
	}

	public static XaeroLocalWaypointReadResult notReady(String message) {
		return new XaeroLocalWaypointReadResult(Outcome.NOT_READY, List.of(), message);
	}

	static XaeroLocalWaypointReadResult unavailable(String message) {
		return new XaeroLocalWaypointReadResult(Outcome.UNAVAILABLE, List.of(), message);
	}

	static XaeroLocalWaypointReadResult failed(String message) {
		return new XaeroLocalWaypointReadResult(Outcome.FAILED, List.of(), message);
	}

	public Outcome outcome() {
		return outcome;
	}

	public List<XaeroLocalWaypoint> waypoints() {
		return waypoints;
	}

	public String message() {
		return message;
	}

	public boolean retryable() {
		return outcome == Outcome.NOT_READY;
	}
}
