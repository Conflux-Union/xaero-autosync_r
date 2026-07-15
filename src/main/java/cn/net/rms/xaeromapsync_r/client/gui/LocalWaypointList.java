package cn.net.rms.xaeromapsync_r.client.gui;

import cn.net.rms.xaeromapsync_r.xaero.XaeroLocalWaypoint;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

final class LocalWaypointList extends ObjectSelectionList<LocalWaypointList.Entry> {
	private final Consumer<XaeroLocalWaypoint> selectionListener;

	LocalWaypointList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight,
			Consumer<XaeroLocalWaypoint> selectionListener) {
		super(minecraft, width, height, top, bottom, itemHeight);
		setRenderBackground(false);
		setRenderTopAndBottom(false);
		this.selectionListener = selectionListener;
		setRenderSelection(true);
	}

	XaeroLocalWaypoint selectedWaypoint() {
		Entry selected = getSelected();
		return selected == null ? null : selected.waypoint;
	}

	void setWaypoints(List<XaeroLocalWaypoint> waypoints, boolean preserveScroll) {
		double previousScroll = getScrollAmount();
		XaeroLocalWaypoint previousSelection = selectedWaypoint();
		clearEntries();
		Entry selectedEntry = null;
		for (XaeroLocalWaypoint waypoint : waypoints) {
			Entry entry = new Entry(minecraft, waypoint);
			addEntry(entry);
			if (waypoint == previousSelection) {
				selectedEntry = entry;
			}
		}
		setSelected(selectedEntry);
		selectionListener.accept(selectedEntry == null ? null : selectedEntry.waypoint);
		setScrollAmount(preserveScroll ? Math.min(previousScroll, getMaxScroll()) : 0.0D);
	}

	@Override
	public int getRowWidth() {
		return Math.max(100, width - 18);
	}

	final class Entry extends ObjectSelectionList.Entry<Entry> {
		private final Minecraft minecraft;
		private final XaeroLocalWaypoint waypoint;

		private Entry(Minecraft minecraft, XaeroLocalWaypoint waypoint) {
			this.minecraft = minecraft;
			this.waypoint = waypoint;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button != 0) {
				return false;
			}
			LocalWaypointList.this.setSelected(this);
			selectionListener.accept(waypoint);
			return true;
		}

		@Override
		public void render(PoseStack poseStack, int index, int top, int left, int rowWidth, int rowHeight,
				int mouseX, int mouseY, boolean hovered, float delta) {
			if (hovered) {
				fill(poseStack, left - 2, top, left + rowWidth + 2, top + rowHeight - 2, 0x30FFFFFF);
			}
			minecraft.font.draw(poseStack, minecraft.font.plainSubstrByWidth(waypoint.name(), rowWidth - 8),
					left + 2, top + 4, 0xFFFFFF);
			String details = new TranslatableComponent("screen.xaero-mapsync_r.local.details",
					waypoint.x(), waypoint.y(), waypoint.z(), waypoint.dimension(), safe(waypoint.category())).getString();
			minecraft.font.draw(poseStack, minecraft.font.plainSubstrByWidth(details, rowWidth - 8),
					left + 2, top + 18, 0xA0A0A0);
		}

		@Override
		public Component getNarration() {
			return new TranslatableComponent("screen.xaero-mapsync_r.local.narration", waypoint.name(),
					waypoint.x(), waypoint.y(), waypoint.z(), waypoint.dimension(), safe(waypoint.category()));
		}
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}
}
