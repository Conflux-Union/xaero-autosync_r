package cn.net.rms.xaeromapsync_r.network;

import cn.net.rms.xaeromapsync_r.waypoint.PublicWaypoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public final class WaypointSnapshotPayload {
	private final long fromRevision;
	private final List<PublicWaypoint> waypoints;

	public WaypointSnapshotPayload(long fromRevision, Collection<PublicWaypoint> waypoints) {
		if (waypoints.size() > WaypointPayloadCodec.MAX_SNAPSHOT_WAYPOINTS) {
			throw new IllegalArgumentException("Waypoint snapshot contains too many entries: " + waypoints.size());
		}
		List<PublicWaypoint> copy = new ArrayList<>(waypoints.size());
		for (PublicWaypoint waypoint : waypoints) {
			waypoint.validate();
			copy.add(waypoint);
		}
		this.fromRevision = fromRevision;
		this.waypoints = Collections.unmodifiableList(copy);
	}

	public static WaypointSnapshotPayload read(FriendlyByteBuf buffer) {
		long fromRevision = buffer.readVarLong();
		int count = buffer.readVarInt();
		if (count < 0 || count > WaypointPayloadCodec.MAX_SNAPSHOT_WAYPOINTS) {
			throw new IllegalArgumentException("Invalid waypoint snapshot entry count: " + count);
		}
		List<PublicWaypoint> waypoints = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			waypoints.add(WaypointPayloadCodec.readSnapshotWaypoint(buffer));
		}
		return new WaypointSnapshotPayload(fromRevision, waypoints);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarLong(fromRevision);
		buffer.writeVarInt(waypoints.size());
		for (PublicWaypoint waypoint : waypoints) {
			WaypointPayloadCodec.writeSnapshotWaypoint(buffer, waypoint);
		}
	}

	public long fromRevision() {
		return fromRevision;
	}

	public List<PublicWaypoint> waypoints() {
		return waypoints;
	}
}
