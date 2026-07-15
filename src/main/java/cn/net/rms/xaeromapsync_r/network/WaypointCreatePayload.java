package cn.net.rms.xaeromapsync_r.network;

import cn.net.rms.xaeromapsync_r.waypoint.PublicWaypoint;
import net.minecraft.network.FriendlyByteBuf;

public final class WaypointCreatePayload {
	private final PublicWaypoint waypoint;

	public WaypointCreatePayload(PublicWaypoint waypoint) {
		waypoint.validate();
		this.waypoint = waypoint;
	}

	public static WaypointCreatePayload read(FriendlyByteBuf buffer) {
		return new WaypointCreatePayload(WaypointPayloadCodec.readMutationWaypoint(buffer));
	}

	public void write(FriendlyByteBuf buffer) {
		WaypointPayloadCodec.writeMutationWaypoint(buffer, waypoint);
	}

	public PublicWaypoint waypoint() {
		return waypoint;
	}
}
