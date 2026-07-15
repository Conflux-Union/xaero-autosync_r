package cn.net.rms.xaeromapsync_r.xaero;

public final class XaeroStatus {
	private final boolean minimapLoaded;
	private final boolean worldMapLoaded;

	public XaeroStatus(boolean minimapLoaded, boolean worldMapLoaded) {
		this.minimapLoaded = minimapLoaded;
		this.worldMapLoaded = worldMapLoaded;
	}

	public static XaeroStatus missing() {
		return new XaeroStatus(false, false);
	}

	public boolean minimapLoaded() {
		return minimapLoaded;
	}

	public boolean worldMapLoaded() {
		return worldMapLoaded;
	}

	public boolean usable() {
		return minimapLoaded && worldMapLoaded;
	}

	public String message() {
		if (usable()) {
			return "Xaero's Minimap and World Map detected";
		}
		if (minimapLoaded) {
			return "Xaero's World Map is missing";
		}
		if (worldMapLoaded) {
			return "Xaero's Minimap is missing";
		}
		return "Xaero's Minimap and World Map are missing";
	}
}
