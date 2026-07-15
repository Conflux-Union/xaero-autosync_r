package cn.net.rms.xaeromapsync_r;

import cn.net.rms.xaeromapsync_r.config.SharedMapConfig;
import cn.net.rms.xaeromapsync_r.network.SharedMapNetworking;
import cn.net.rms.xaeromapsync_r.server.SharedMapServer;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XaeroMapsync_r implements ModInitializer {
	public static final String MOD_ID = "xaero-mapsync_r";
	public static final String MOD_NAME = "Xaero Map Sync";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SharedMapConfig.register();
		SharedMapNetworking.registerServerReceivers();
		SharedMapServer.register();
		LOGGER.info("{} initialized", MOD_NAME);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
