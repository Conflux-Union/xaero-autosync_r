package cn.net.rms.xaeromapsync_r.map;

import cn.net.rms.xaeromapsync_r.XaeroMapsync_r;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public final class MapTileDebugRenderer {
	private MapTileDebugRenderer() {
	}

	public static int renderLoadedPlayerChunks(MinecraftServer server) {
		Set<String> rendered = new HashSet<>();
		int count = 0;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerLevel level = player.getLevel();
			ChunkPos pos = player.chunkPosition();
			String key = level.dimension().location() + ":" + pos.x + ":" + pos.z;
			if (rendered.add(key) && renderIfLoaded(level, pos.x, pos.z) != null) {
				count++;
			}
		}
		return count;
	}

	public static int renderAndIndexLoadedPlayerChunks(MinecraftServer server, MapTileIndexStore indexStore) {
		Set<String> rendered = new HashSet<>();
		int count = 0;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerLevel level = player.getLevel();
			ChunkPos pos = player.chunkPosition();
			String key = level.dimension().location() + ":" + pos.x + ":" + pos.z;
			if (!rendered.add(key)) {
				continue;
			}
			MapTile tile = renderIfLoaded(level, pos.x, pos.z);
			if (tile != null) {
				indexStore.upsert(tile);
				count++;
			}
		}
		return count;
	}

	public static MapTile renderIfLoaded(ServerLevel level, int chunkX, int chunkZ) {
		if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
			return null;
		}
		ChunkAccess chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
		if (chunk == null) {
			return null;
		}
		int[] heights = new int[256];
		for (int localZ = 0; localZ < 16; localZ++) {
			for (int localX = 0; localX < 16; localX++) {
				int worldX = (chunkX << 4) + localX;
				int worldZ = (chunkZ << 4) + localZ;
				heights[localZ * 16 + localX] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
			}
		}
		long hash = MapTileHasher.hashHeights(heights);
		MapTile tile = new MapTile(level.dimension().location().toString(), chunkX, chunkZ, heights, hash);
		XaeroMapsync_r.LOGGER.debug("Rendered debug tile {} {} {} hash={}", tile.dimension(), tile.chunkX(), tile.chunkZ(), tile.contentHash());
		return tile;
	}
}
