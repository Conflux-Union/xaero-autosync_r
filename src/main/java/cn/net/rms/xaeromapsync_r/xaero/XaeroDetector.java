package cn.net.rms.xaeromapsync_r.xaero;

import net.fabricmc.loader.api.FabricLoader;

public final class XaeroDetector {
	private static XaeroStatus status = XaeroStatus.missing();

	private XaeroDetector() {
	}

	public static void detect() {
		FabricLoader loader = FabricLoader.getInstance();
		boolean minimapLoaded = loader.isModLoaded("xaerominimap");
		boolean worldMapLoaded = loader.isModLoaded("xaeroworldmap");
		status = new XaeroStatus(minimapLoaded, worldMapLoaded);
	}

	public static XaeroStatus status() {
		return status;
	}
}
