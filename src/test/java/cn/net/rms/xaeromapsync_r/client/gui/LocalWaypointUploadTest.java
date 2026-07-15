package cn.net.rms.xaeromapsync_r.client.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cn.net.rms.xaeromapsync_r.waypoint.PublicWaypoint;
import cn.net.rms.xaeromapsync_r.waypoint.WaypointVisibility;
import cn.net.rms.xaeromapsync_r.xaero.XaeroLocalWaypoint;
import org.junit.jupiter.api.Test;

final class LocalWaypointUploadTest {
	@Test
	void candidatePreservesSelectedLocalWaypointAndExplicitDimension() {
		XaeroLocalWaypoint local = new XaeroLocalWaypoint("Fort", "minecraft:the_nether", -12, 70, 34,
				"F", 0x123456, "bases");

		PublicWaypoint candidate = LocalWaypointUpload.createCandidate(local);

		assertNotNull(candidate.id());
		assertEquals("Fort", candidate.name());
		assertEquals("minecraft:the_nether", candidate.dimension());
		assertEquals(-12.0D, candidate.x());
		assertEquals(70.0D, candidate.y());
		assertEquals(34.0D, candidate.z());
		assertEquals("F", candidate.symbol());
		assertEquals(0x123456, candidate.color());
		assertEquals("bases", candidate.category());
		assertEquals(WaypointVisibility.PUBLIC, candidate.visibility());
	}

	@Test
	void candidateRejectsValuesThatCannotBeEncoded() {
		assertThrows(IllegalArgumentException.class, () -> LocalWaypointUpload.createCandidate(
				new XaeroLocalWaypoint("x".repeat(PublicWaypoint.MAX_NAME_LENGTH + 1), "minecraft:overworld",
						0, 64, 0, "X", 0, "default")));
		assertThrows(IllegalArgumentException.class, () -> LocalWaypointUpload.createCandidate(
				new XaeroLocalWaypoint("Home", "minecraft:overworld", 0, 64, 0, "x".repeat(17), 0, "default")));
	}
}
