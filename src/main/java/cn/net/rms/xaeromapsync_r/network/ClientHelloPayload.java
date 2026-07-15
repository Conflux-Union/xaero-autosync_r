package cn.net.rms.xaeromapsync_r.network;

import cn.net.rms.xaeromapsync_r.config.SharedMapConfig;
import cn.net.rms.xaeromapsync_r.config.SharedMapProtocolDefaults;
import net.minecraft.network.FriendlyByteBuf;

public final class ClientHelloPayload {
	private final int protocolVersion;
	private final int mapFormatVersion;
	private final String xaeroAdapterVersion;
	private final String compression;
	private final int maxPacketBytes;

	public ClientHelloPayload(int protocolVersion, int mapFormatVersion, String xaeroAdapterVersion, String compression, int maxPacketBytes) {
		this.protocolVersion = protocolVersion;
		this.mapFormatVersion = mapFormatVersion;
		this.xaeroAdapterVersion = xaeroAdapterVersion;
		this.compression = compression;
		this.maxPacketBytes = maxPacketBytes;
	}

	public static ClientHelloPayload current() {
		return new ClientHelloPayload(
				SharedMapConfig.protocolVersion(),
				SharedMapConfig.mapFormatVersion(),
				SharedMapProtocolDefaults.XAERO_ADAPTER_VERSION,
				SharedMapConfig.compression(),
				SharedMapConfig.maxPacketBytes());
	}

	public static ClientHelloPayload read(FriendlyByteBuf buffer) {
		return new ClientHelloPayload(
				buffer.readVarInt(),
				buffer.readVarInt(),
				buffer.readUtf(64),
				buffer.readUtf(32),
				buffer.readVarInt());
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(protocolVersion);
		buffer.writeVarInt(mapFormatVersion);
		buffer.writeUtf(xaeroAdapterVersion);
		buffer.writeUtf(compression);
		buffer.writeVarInt(maxPacketBytes);
	}

	public int protocolVersion() {
		return protocolVersion;
	}

	public int mapFormatVersion() {
		return mapFormatVersion;
	}

	public String xaeroAdapterVersion() {
		return xaeroAdapterVersion;
	}

	public String compression() {
		return compression;
	}

	public int maxPacketBytes() {
		return maxPacketBytes;
	}

	public boolean isCompatible() {
		return protocolVersion == SharedMapConfig.protocolVersion()
				&& mapFormatVersion == SharedMapConfig.mapFormatVersion();
	}
}
