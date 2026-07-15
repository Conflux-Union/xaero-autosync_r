package cn.net.rms.xaeromapsync_r.client.gui;

import cn.net.rms.xaeromapsync_r.waypoint.PublicWaypoint;
import cn.net.rms.xaeromapsync_r.waypoint.WaypointVisibility;
import cn.net.rms.xaeromapsync_r.xaero.XaeroLocalWaypoint;
import java.util.UUID;

final class LocalWaypointUpload {
	private static final int MAX_SYMBOL_LENGTH = 16;
	private static final int MAX_CATEGORY_LENGTH = 64;

	private LocalWaypointUpload() {
	}

	static PublicWaypoint createCandidate(XaeroLocalWaypoint localWaypoint) {
		validateOptionalLength("symbol", localWaypoint.symbol(), MAX_SYMBOL_LENGTH);
		validateOptionalLength("category", localWaypoint.category(), MAX_CATEGORY_LENGTH);
		PublicWaypoint candidate = new PublicWaypoint(
				UUID.randomUUID(), null, null, localWaypoint.name(), localWaypoint.dimension(),
				localWaypoint.x(), localWaypoint.y(), localWaypoint.z(), localWaypoint.symbol(),
				localWaypoint.color(), localWaypoint.category(), WaypointVisibility.PUBLIC,
				0L, false, 0L, 0L);
		candidate.validate();
		return candidate;
	}

	private static void validateOptionalLength(String field, String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			throw new IllegalArgumentException("Waypoint " + field + " must be at most " + maxLength + " characters");
		}
	}
}
