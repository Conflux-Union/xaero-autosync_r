package cn.net.rms.xaeromapsync_r.client.gui;

import cn.net.rms.xaeromapsync_r.client.SharedMapClient;
import cn.net.rms.xaeromapsync_r.network.SharedMapNetworking;
import cn.net.rms.xaeromapsync_r.waypoint.PublicWaypoint;
import cn.net.rms.xaeromapsync_r.xaero.XaeroLocalWaypoint;
import cn.net.rms.xaeromapsync_r.xaero.XaeroLocalWaypointReadResult;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

final class LocalWaypointImportScreen extends Screen {
	private static final int SIDE_MARGIN = 12;
	private static final int CONTROL_HEIGHT = 20;
	private static final int LIST_TOP = 58;
	private final Screen parent;
	private List<XaeroLocalWaypoint> localWaypoints = List.of();
	private EditBox searchBox;
	private LocalWaypointList waypointList;
	private Button uploadButton;
	private Component status = TextComponent.EMPTY;
	private String validationError = "";

	LocalWaypointImportScreen(Screen parent) {
		super(new TranslatableComponent("screen.xaero-mapsync_r.import_local.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int contentWidth = Math.min(560, width - SIDE_MARGIN * 2);
		int left = (width - contentWidth) / 2;
		searchBox = new EditBox(font, left, 32, contentWidth, CONTROL_HEIGHT,
				new TranslatableComponent("screen.xaero-mapsync_r.import_local.search"));
		searchBox.setMaxLength(96);
		searchBox.setSuggestion(new TranslatableComponent("screen.xaero-mapsync_r.import_local.search").getString());
		searchBox.setResponder(query -> refreshList(query, true));
		addRenderableWidget(searchBox);

		int listBottom = Math.max(LIST_TOP + 24, height - 86);
		waypointList = new LocalWaypointList(minecraft, contentWidth, height, LIST_TOP, listBottom, 38,
				selected -> uploadButton.active = selected != null);
		waypointList.setLeftPos(left);
		addRenderableWidget(waypointList);

		int gap = 4;
		int buttonWidth = (contentWidth - gap * 2) / 3;
		uploadButton = addRenderableWidget(new Button(left, height - 27, buttonWidth, CONTROL_HEIGHT,
				new TranslatableComponent("screen.xaero-mapsync_r.import_local.upload"), button -> confirmUpload()));
		uploadButton.active = false;
		addRenderableWidget(new Button(left + buttonWidth + gap, height - 27, buttonWidth, CONTROL_HEIGHT,
				new TranslatableComponent("screen.xaero-mapsync_r.import_local.refresh"), button -> loadLocalWaypoints()));
		addRenderableWidget(new Button(left + (buttonWidth + gap) * 2, height - 27, buttonWidth, CONTROL_HEIGHT,
				new TranslatableComponent("gui.cancel"), button -> onClose()));

		loadLocalWaypoints();
		setInitialFocus(searchBox);
	}

	private void loadLocalWaypoints() {
		validationError = "";
		XaeroLocalWaypointReadResult result = SharedMapClient.readLocalWaypoints();
		localWaypoints = result.outcome() == XaeroLocalWaypointReadResult.Outcome.LOADED
				? new ArrayList<>(result.waypoints()) : List.of();
		status = result.outcome() == XaeroLocalWaypointReadResult.Outcome.LOADED
				? new TranslatableComponent(statusKey(result.outcome()), result.waypoints().size())
				: new TranslatableComponent(statusKey(result.outcome()));
		refreshList(searchBox.getValue(), false);
	}

	private static String statusKey(XaeroLocalWaypointReadResult.Outcome outcome) {
		switch (outcome) {
			case LOADED:
				return "screen.xaero-mapsync_r.import_local.status.loaded";
			case NOT_READY:
				return "screen.xaero-mapsync_r.import_local.status.not_ready";
			case FAILED:
				return "screen.xaero-mapsync_r.import_local.status.failed";
			default:
				return "screen.xaero-mapsync_r.import_local.status.unavailable";
		}
	}

	private void refreshList(String query, boolean preserveScroll) {
		if (waypointList == null) {
			return;
		}
		String normalized = query.trim().toLowerCase(Locale.ROOT);
		List<XaeroLocalWaypoint> filtered = localWaypoints.stream()
				.filter(waypoint -> normalized.isEmpty() || searchableText(waypoint).contains(normalized))
				.collect(Collectors.toList());
		waypointList.setWaypoints(filtered, preserveScroll);
	}

	private void confirmUpload() {
		XaeroLocalWaypoint selected = waypointList.selectedWaypoint();
		if (selected == null) {
			return;
		}
		try {
			PublicWaypoint candidate = LocalWaypointUpload.createCandidate(selected);
			validationError = "";
			minecraft.setScreen(new ConfirmScreen(confirmed -> {
				if (confirmed) {
					SharedMapNetworking.createWaypoint(candidate);
					minecraft.setScreen(parent);
				} else {
					minecraft.setScreen(this);
				}
			}, new TranslatableComponent("screen.xaero-mapsync_r.import_local.confirm.title"),
					new TranslatableComponent("screen.xaero-mapsync_r.import_local.confirm.message", candidate.name(),
							candidate.dimension(), (int) candidate.x(), (int) candidate.y(), (int) candidate.z())));
		} catch (RuntimeException exception) {
			validationError = exception.getMessage() == null ? "Invalid waypoint" : exception.getMessage();
		}
	}

	private static String searchableText(XaeroLocalWaypoint waypoint) {
		return String.join(" ", waypoint.name(), waypoint.dimension(), safe(waypoint.category()),
				Integer.toString(waypoint.x()), Integer.toString(waypoint.y()), Integer.toString(waypoint.z()))
				.toLowerCase(Locale.ROOT);
	}

	@Override
	public void tick() {
		searchBox.tick();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 12, 0xFFFFFF);
		int previewTop = Math.max(LIST_TOP + 26, height - 80);
		XaeroLocalWaypoint selected = waypointList.selectedWaypoint();
		if (selected == null) {
			drawCenteredClipped(poseStack, status, previewTop, 0xA0A0A0);
		} else {
			drawCenteredClipped(poseStack,
					new TranslatableComponent("screen.xaero-mapsync_r.import_local.preview.name", selected.name()),
					previewTop, 0xFFFFFF);
			drawCenteredClipped(poseStack,
					new TranslatableComponent("screen.xaero-mapsync_r.import_local.preview.location",
							selected.dimension(), selected.x(), selected.y(), selected.z(), safe(selected.category())),
					previewTop + 12, 0xA0A0A0);
		}
		if (!validationError.isEmpty()) {
			drawCenteredClipped(poseStack, new TextComponent(validationError), previewTop + 24, 0xFF5555);
		}
		super.render(poseStack, mouseX, mouseY, delta);
	}

	private void drawCenteredClipped(PoseStack poseStack, Component text, int y, int color) {
		int maxWidth = Math.max(40, width - SIDE_MARGIN * 2);
		String clipped = font.plainSubstrByWidth(text.getString(), maxWidth);
		drawCenteredString(poseStack, font, clipped, width / 2, y, color);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}
}
