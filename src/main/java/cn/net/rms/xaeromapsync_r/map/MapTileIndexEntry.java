package cn.net.rms.xaeromapsync_r.map;

public final class MapTileIndexEntry {
	private final String dimension;
	private final int chunkX;
	private final int chunkZ;
	private final long contentHash;
	private final long revision;
	private final long updatedAtMillis;

	public MapTileIndexEntry(String dimension, int chunkX, int chunkZ, long contentHash, long revision, long updatedAtMillis) {
		this.dimension = dimension;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.contentHash = contentHash;
		this.revision = revision;
		this.updatedAtMillis = updatedAtMillis;
	}

	public String dimension() {
		return dimension;
	}

	public int chunkX() {
		return chunkX;
	}

	public int chunkZ() {
		return chunkZ;
	}

	public long contentHash() {
		return contentHash;
	}

	public long revision() {
		return revision;
	}

	public long updatedAtMillis() {
		return updatedAtMillis;
	}
}
