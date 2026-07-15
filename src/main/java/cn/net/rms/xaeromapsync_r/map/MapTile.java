package cn.net.rms.xaeromapsync_r.map;

import java.util.Arrays;

public final class MapTile {
	private final String dimension;
	private final int chunkX;
	private final int chunkZ;
	private final int[] heights;
	private final long contentHash;

	public MapTile(String dimension, int chunkX, int chunkZ, int[] heights, long contentHash) {
		this.dimension = dimension;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.heights = Arrays.copyOf(heights, heights.length);
		this.contentHash = contentHash;
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

	public int[] heights() {
		return Arrays.copyOf(heights, heights.length);
	}

	public long contentHash() {
		return contentHash;
	}
}
