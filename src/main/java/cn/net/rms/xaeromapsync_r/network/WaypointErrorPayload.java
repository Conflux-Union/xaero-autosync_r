package cn.net.rms.xaeromapsync_r.network;

import net.minecraft.network.FriendlyByteBuf;

public final class WaypointErrorPayload {
	private final String message;

	public WaypointErrorPayload(String message) {
		if (message == null || message.isBlank()) {
			throw new IllegalArgumentException("Waypoint error message is required");
		}
		this.message = message;
	}

	public static WaypointErrorPayload read(FriendlyByteBuf buffer) {
		return new WaypointErrorPayload(buffer.readUtf(256));
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(message, 256);
	}

	public String message() {
		return message;
	}
}
