package cn.net.rms.xaeromapsync_r.server.dirty;

import cn.net.rms.xaeromapsync_r.XaeroMapsync_r;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;

public final class DirtyChunkStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Map<String, DirtyChunkRecord> records = new LinkedHashMap<>();
	private long currentTick;
	private boolean paused;

	public DirtyChunkStore() {
		ServerTickEvents.END_SERVER_TICK.register(server -> advance());
	}

	public synchronized void markDirty(String dimension, BlockPos pos) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		String key = key(dimension, chunkX, chunkZ);
		DirtyChunkRecord record = records.computeIfAbsent(key, ignored -> new DirtyChunkRecord(dimension, chunkX, chunkZ, currentTick));
		record.markColumn(pos.getX() & 15, pos.getZ() & 15, currentTick);
	}

	public synchronized void load(MinecraftServer server) {
		Path path = path(server);
		records.clear();
		if (!Files.exists(path)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			DirtyChunkFile file = GSON.fromJson(reader, DirtyChunkFile.class);
			if (file == null || file.records == null) {
				return;
			}
			for (DirtyRecordFile recordFile : file.records) {
				if (recordFile == null || recordFile.dimension == null) {
					continue;
				}
				DirtyChunkRecord record = new DirtyChunkRecord(recordFile.dimension, recordFile.chunkX, recordFile.chunkZ, currentTick);
				DirtyActivityState state = recordFile.state == null ? DirtyActivityState.ACTIVE : recordFile.state;
				record.restore(state, recordFile.firstDirtyTick, recordFile.lastDirtyTick, recordFile.dirtyColumns);
				records.put(key(recordFile.dimension, recordFile.chunkX, recordFile.chunkZ), record);
			}
			XaeroMapsync_r.LOGGER.info("Loaded {} dirty chunks", records.size());
		} catch (IOException | RuntimeException exception) {
			XaeroMapsync_r.LOGGER.warn("Failed to load dirty chunks at {}", path, exception);
		}
	}

	public synchronized void save(MinecraftServer server) {
		Path path = path(server);
		Path tempPath = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			DirtyChunkFile file = new DirtyChunkFile();
			file.records = new DirtyRecordFile[records.size()];
			int index = 0;
			for (DirtyChunkRecord record : records.values()) {
				DirtyRecordFile recordFile = new DirtyRecordFile();
				recordFile.dimension = record.dimension();
				recordFile.chunkX = record.chunkX();
				recordFile.chunkZ = record.chunkZ();
				recordFile.state = record.state();
				recordFile.firstDirtyTick = record.firstDirtyTick();
				recordFile.lastDirtyTick = record.lastDirtyTick();
				recordFile.dirtyColumns = record.dirtyColumnsAsLongArray();
				file.records[index++] = recordFile;
			}
			try (Writer writer = Files.newBufferedWriter(tempPath)) {
				GSON.toJson(file, writer);
			}
			Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException exception) {
			XaeroMapsync_r.LOGGER.warn("Failed to save dirty chunks at {}", path, exception);
		}
	}

	public synchronized int flushStableDirtyChunks() {
		if (paused) {
			return 0;
		}
		int flushed = 0;
		Iterator<Map.Entry<String, DirtyChunkRecord>> iterator = records.entrySet().iterator();
		while (iterator.hasNext()) {
			DirtyChunkRecord record = iterator.next().getValue();
			if (record.state() == DirtyActivityState.STABLE) {
				iterator.remove();
				flushed++;
			}
		}
		return flushed;
	}

	public synchronized int totalCount() {
		return records.size();
	}

	public synchronized String stateSummary() {
		int quiet = 0;
		int active = 0;
		int storm = 0;
		int cooldown = 0;
		int stable = 0;
		for (DirtyChunkRecord record : records.values()) {
			switch (record.state()) {
				case QUIET:
					quiet++;
					break;
				case ACTIVE:
					active++;
					break;
				case STORM:
					storm++;
					break;
				case COOLDOWN:
					cooldown++;
					break;
				case STABLE:
					stable++;
					break;
				default:
					break;
			}
		}
		return "paused=" + paused + ",quiet=" + quiet + ",active=" + active + ",storm=" + storm + ",cooldown=" + cooldown + ",stable=" + stable;
	}

	public synchronized void setPaused(boolean paused) {
		this.paused = paused;
	}

	private synchronized void advance() {
		currentTick++;
		for (DirtyChunkRecord record : records.values()) {
			record.advance(currentTick);
		}
	}

	private static String key(String dimension, int chunkX, int chunkZ) {
		return dimension + ":" + ChunkPos.asLong(chunkX, chunkZ);
	}

	private static Path path(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve("xaero-mapsync_r").resolve("dirty_chunks.json");
	}

	private static final class DirtyChunkFile {
		private DirtyRecordFile[] records;
	}

	private static final class DirtyRecordFile {
		private String dimension;
		private int chunkX;
		private int chunkZ;
		private DirtyActivityState state;
		private long firstDirtyTick;
		private long lastDirtyTick;
		private long[] dirtyColumns;
	}
}
