package cn.net.rms.xaeromapsync_r.client.gui;

import cn.net.rms.xaeromapsync_r.XaeroMapsync_r;
import cn.net.rms.xaeromapsync_r.client.SharedMapClient;
import cn.net.rms.xaeromapsync_r.waypoint.WaypointVisibility;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public final class XaeroWaypointScreenIntegration {
	private static final String XAERO_WAYPOINT_SCREEN_CLASS = "xaero.common.gui.GuiWaypoints";
	private static final int BUTTON_HEIGHT = 20;
	private static final int BOTTOM_BAR_CLEARANCE = 78;
	private static final int HORIZONTAL_MARGIN = 6;
	private static final int MAX_ROW_WIDTH = 308;

	private XaeroWaypointScreenIntegration() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!XAERO_WAYPOINT_SCREEN_CLASS.equals(screen.getClass().getName())) {
				return;
			}
			addWaypointActions(client, screen, scaledWidth, scaledHeight);
		});
	}

	private static void addWaypointActions(net.minecraft.client.Minecraft client, Screen screen, int screenWidth,
			int screenHeight) {
		List<Button> actions = new ArrayList<>();
		Button status = actionButton(0, 0, 1, "screen.xaero-mapsync_r.share.status.not_shared", button -> {});
		status.active = false;
		actions.add(status);
		Button publicButton = actionButton(0, 0, 1, "screen.xaero-mapsync_r.share.public",
				button -> SharedMapClient.shareSelectedXaeroWaypoint(screen, WaypointVisibility.PUBLIC));
		actions.add(publicButton);
		Button teamButton = null;
		if (client.player != null && client.player.getTeam() != null) {
			teamButton = actionButton(0, 0, 1, "screen.xaero-mapsync_r.share.team",
					button -> SharedMapClient.shareSelectedXaeroWaypoint(screen, WaypointVisibility.TEAM));
			actions.add(teamButton);
		}
		Button unshareButton = actionButton(0, 0, 1, "screen.xaero-mapsync_r.share.remove",
				button -> SharedMapClient.unshareSelectedXaeroWaypoint(screen));
		actions.add(unshareButton);
		Button xaeroEditButton = xaeroEditButton(screen);
		Button lockedEditButton = xaeroEditButton == null ? null
				: new Button(xaeroEditButton.x, xaeroEditButton.y, xaeroEditButton.getWidth(), BUTTON_HEIGHT,
						xaeroEditButton.getMessage(), button -> {});
		if (lockedEditButton != null) {
			lockedEditButton.active = false;
		}

		int availableWidth = Math.max(3, screenWidth - HORIZONTAL_MARGIN * 2);
		int rowWidth = Math.min(Math.max(MAX_ROW_WIDTH, actions.size() * 96), availableWidth);
		int gap = rowWidth >= 152 ? 4 : 2;
		int buttonWidth = Math.max(1, (rowWidth - gap * (actions.size() - 1)) / actions.size());
		int usedWidth = buttonWidth * actions.size() + gap * (actions.size() - 1);
		int left = (screenWidth - usedWidth) / 2;
		int top = Math.max(4, screenHeight - BOTTOM_BAR_CLEARANCE);

		for (int index = 0; index < actions.size(); index++) {
			Button button = actions.get(index);
			button.x = left + index * (buttonWidth + gap);
			button.y = top;
			button.setWidth(buttonWidth);
			Screens.getButtons(screen).add(button);
		}
		Button finalTeamButton = teamButton;
		ScreenEvents.afterRender(screen).register((renderedScreen, matrices, mouseX, mouseY, tickDelta) ->
				updateStatus(status, publicButton, finalTeamButton, unshareButton, xaeroEditButton,
						lockedEditButton, renderedScreen));
	}

	private static void updateStatus(Button status, Button publicButton, Button teamButton, Button unshareButton,
			Button xaeroEditButton, Button lockedEditButton, Screen screen) {
		Optional<WaypointVisibility> visibility = SharedMapClient.selectedXaeroWaypointVisibility(screen);
		boolean selected = visibility.isPresent();
		boolean shared = visibility.filter(value -> value == WaypointVisibility.PUBLIC
				|| value == WaypointVisibility.TEAM).isPresent();
		String key = visibility.map(value -> value == WaypointVisibility.TEAM
				? "screen.xaero-mapsync_r.share.status.team"
				: value == WaypointVisibility.PUBLIC
						? "screen.xaero-mapsync_r.share.status.public"
						: "screen.xaero-mapsync_r.share.status.not_shared")
				.orElse("screen.xaero-mapsync_r.share.status.select");
		status.setMessage(new TranslatableComponent(key));
		publicButton.active = selected && !shared;
		if (teamButton != null) {
			teamButton.active = selected && !shared;
		}
		unshareButton.active = shared;
		if (xaeroEditButton != null && lockedEditButton != null) {
			if (shared) {
				Screens.getButtons(screen).remove(xaeroEditButton);
				if (!Screens.getButtons(screen).contains(lockedEditButton)) {
					Screens.getButtons(screen).add(lockedEditButton);
				}
			} else if (!Screens.getButtons(screen).contains(xaeroEditButton)) {
				Screens.getButtons(screen).remove(lockedEditButton);
				Screens.getButtons(screen).add(xaeroEditButton);
			}
		}
	}

	private static Button xaeroEditButton(Screen screen) {
		try {
			Field field = screen.getClass().getDeclaredField("editButton");
			field.setAccessible(true);
			return (Button) field.get(screen);
		} catch (ReflectiveOperationException | ClassCastException exception) {
			XaeroMapsync_r.LOGGER.warn("Unable to lock Xaero's edit button for shared waypoints", exception);
			return null;
		}
	}

	private static Button actionButton(int x, int y, int width, String translationKey, Button.OnPress onPress) {
		return new Button(x, y, width, BUTTON_HEIGHT, new TranslatableComponent(translationKey), onPress);
	}
}
