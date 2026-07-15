package cn.net.rms.xaeromapsync_r.network;

import cn.net.rms.xaeromapsync_r.waypoint.PublicWaypoint;
import net.minecraft.network.FriendlyByteBuf;

public final class WaypointUpdatePayload {
	private final PublicWaypoint waypoint;
	private final long knownRevision;

	public WaypointUpdatePayload(PublicWaypoint waypoint, long knownRevision) {
		waypoint.validate();
		this.waypoint = waypoint;
		this.knownRevision = knownRevision;
	}

	public static WaypointUpdatePayload read(FriendlyByteBuf buffer) {
		PublicWaypoint waypoint = WaypointPayloadCodec.readMutationWaypoint(buffer);
		return new WaypointUpdatePayload(waypoint, buffer.readVarLong());
	}

	public void write(FriendlyByteBuf buffer) {
		WaypointPayloadCodec.writeMutationWaypoint(buffer, waypoint);
		buffer.writeVarLong(knownRevision);
	}

	public PublicWaypoint waypoint() {
		return waypoint;
	}

	public long knownRevision() {
		return knownRevision;
	}
}
