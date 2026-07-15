package cn.net.rms.xaeromapsync_r.network;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public final class WaypointDeletePayload {
	private final UUID waypointId;
	private final long knownRevision;

	public WaypointDeletePayload(UUID waypointId, long knownRevision) {
		if (waypointId == null) {
			throw new IllegalArgumentException("Waypoint id is required");
		}
		this.waypointId = waypointId;
		this.knownRevision = knownRevision;
	}

	public static WaypointDeletePayload read(FriendlyByteBuf buffer) {
		return new WaypointDeletePayload(WaypointPayloadCodec.readUuid(buffer), buffer.readVarLong());
	}

	public void write(FriendlyByteBuf buffer) {
		WaypointPayloadCodec.writeUuid(buffer, waypointId);
		buffer.writeVarLong(knownRevision);
	}

	public UUID waypointId() {
		return waypointId;
	}

	public long knownRevision() {
		return knownRevision;
	}
}
