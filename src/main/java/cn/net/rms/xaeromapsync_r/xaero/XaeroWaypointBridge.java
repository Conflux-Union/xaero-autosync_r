package cn.net.rms.xaeromapsync_r.xaero;

import java.util.List;

interface XaeroWaypointBridge {
	Target currentTarget() throws ReflectiveOperationException;

	SelectedWaypoint selectedWaypoint(Object screen) throws ReflectiveOperationException;

	Object create(WaypointValues values) throws ReflectiveOperationException;

	WaypointValues read(Object waypoint) throws ReflectiveOperationException;

	void update(Object waypoint, WaypointValues values) throws ReflectiveOperationException;

	void save(Object world) throws ReflectiveOperationException;

	final class Target {
		private final Object world;
		private final List<Object> waypoints;

		Target(Object world, List<Object> waypoints) {
			this.world = world;
			this.waypoints = waypoints;
		}

		Object world() {
			return world;
		}

		List<Object> waypoints() {
			return waypoints;
		}
	}

	final class SelectedWaypoint {
		private final Object nativeWaypoint;
		private final Object world;
		private final WaypointValues values;
		private final String category;
		private final String dimension;

		SelectedWaypoint(Object nativeWaypoint, Object world, WaypointValues values, String category, String dimension) {
			this.nativeWaypoint = nativeWaypoint;
			this.world = world;
			this.values = values;
			this.category = category;
			this.dimension = dimension;
		}

		Object nativeWaypoint() {
			return nativeWaypoint;
		}

		Object world() {
			return world;
		}

		WaypointValues values() {
			return values;
		}

		String category() {
			return category;
		}

		String dimension() {
			return dimension;
		}
	}
}
