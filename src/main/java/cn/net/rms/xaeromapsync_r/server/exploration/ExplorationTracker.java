package cn.net.rms.xaeromapsync_r.server.exploration;

import cn.net.rms.xaeromapsync_r.server.SharedMapServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class ExplorationTracker {
	private static final int SCAN_INTERVAL_TICKS = 20;
	private static int ticksUntilScan = SCAN_INTERVAL_TICKS;

	private ExplorationTracker() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(ExplorationTracker::tick);
	}

	private static void tick(MinecraftServer server) {
		ticksUntilScan--;
		if (ticksUntilScan > 0) {
			return;
		}
		ticksUntilScan = SCAN_INTERVAL_TICKS;
		int radius = Math.max(0, server.getPlayerList().getViewDistance() - 1);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			scanPlayer(player, radius);
		}
	}

	private static void scanPlayer(ServerPlayer player, int radius) {
		ServerLevel level = player.getLevel();
		ChunkPos center = player.chunkPosition();
		String dimension = level.dimension().location().toString();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int chunkX = center.x + dx;
				int chunkZ = center.z + dz;
				if (level.getChunkSource().hasChunk(chunkX, chunkZ)) {
					SharedMapServer.exploredChunks().markExplored(dimension, chunkX, chunkZ);
				}
			}
		}
	}
}
