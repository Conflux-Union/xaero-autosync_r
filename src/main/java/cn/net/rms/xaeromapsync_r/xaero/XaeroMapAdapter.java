package cn.net.rms.xaeromapsync_r.xaero;

import cn.net.rms.xaeromapsync_r.map.MapTile;

public interface XaeroMapAdapter {
	enum ApplyResult {
		APPLIED,
		RETRY_LATER,
		UNAVAILABLE
	}

	boolean isAvailable();

	boolean apply(MapTile tile);

	default ApplyResult applyResult(MapTile tile) {
		if (apply(tile)) return ApplyResult.APPLIED;
		return isAvailable() ? ApplyResult.RETRY_LATER : ApplyResult.UNAVAILABLE;
	}
}
