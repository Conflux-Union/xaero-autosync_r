package cn.net.rms.xaeromapsync_r.network;

import cn.net.rms.xaeromapsync_r.map.MapTileIndexEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public final class MapTileIndexSnapshotPayload {
	private static final int MAX_ENTRIES = 8192;
	private static final int MAX_DIMENSION_LENGTH = 256;
	private final long rootHash;
	private final List<MapTileIndexEntry> entries;

	public MapTileIndexSnapshotPayload(long rootHash, Collection<MapTileIndexEntry> entries) {
		if (entries.size() > MAX_ENTRIES) {
			throw new IllegalArgumentException("Map tile index snapshot contains too many entries: " + entries.size());
		}
		this.rootHash = rootHash;
		this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
	}

	public static MapTileIndexSnapshotPayload read(FriendlyByteBuf buffer) {
		long rootHash = buffer.readLong();
		int count = buffer.readVarInt();
		if (count < 0 || count > MAX_ENTRIES) {
			throw new IllegalArgumentException("Invalid map tile index entry count: " + count);
		}
		List<MapTileIndexEntry> entries = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			entries.add(new MapTileIndexEntry(
					buffer.readUtf(MAX_DIMENSION_LENGTH),
					buffer.readInt(),
					buffer.readInt(),
					buffer.readLong(),
					buffer.readVarLong(),
					buffer.readLong()));
		}
		return new MapTileIndexSnapshotPayload(rootHash, entries);
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(rootHash);
		buffer.writeVarInt(entries.size());
		for (MapTileIndexEntry entry : entries) {
			buffer.writeUtf(entry.dimension(), MAX_DIMENSION_LENGTH);
			buffer.writeInt(entry.chunkX());
			buffer.writeInt(entry.chunkZ());
			buffer.writeLong(entry.contentHash());
			buffer.writeVarLong(entry.revision());
			buffer.writeLong(entry.updatedAtMillis());
		}
	}

	public long rootHash() {
		return rootHash;
	}

	public List<MapTileIndexEntry> entries() {
		return entries;
	}
}
