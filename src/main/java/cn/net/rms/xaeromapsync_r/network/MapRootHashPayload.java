package cn.net.rms.xaeromapsync_r.network;

import net.minecraft.network.FriendlyByteBuf;

public final class MapRootHashPayload {
	private final long knownRootHash;

	public MapRootHashPayload(long knownRootHash) {
		this.knownRootHash = knownRootHash;
	}

	public static MapRootHashPayload read(FriendlyByteBuf buffer) {
		return new MapRootHashPayload(buffer.readLong());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(knownRootHash);
	}

	public long knownRootHash() {
		return knownRootHash;
	}
}
