package cn.net.rms.xaeromapsync_r.xaero;

import java.util.Objects;

public final class XaeroLocalWaypoint {
	private final String name;
	private final String dimension;
	private final int x;
	private final int y;
	private final int z;
	private final String symbol;
	private final int color;
	private final String category;

	public XaeroLocalWaypoint(String name, String dimension, int x, int y, int z, String symbol, int color, String category) {
		this.name = Objects.requireNonNull(name, "name");
		this.dimension = Objects.requireNonNull(dimension, "dimension");
		this.x = x;
		this.y = y;
		this.z = z;
		this.symbol = symbol;
		this.color = color;
		this.category = category;
	}

	public String name() {
		return name;
	}

	public String dimension() {
		return dimension;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int z() {
		return z;
	}

	public String symbol() {
		return symbol;
	}

	public int color() {
		return color;
	}

	public String category() {
		return category;
	}
}
