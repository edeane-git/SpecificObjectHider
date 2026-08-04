package com.SpecificObjectHider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
@PluginDescriptor(
		name = "Specific Object Hider",
		description = "Hides all interaction options for specific object instances.",
		tags = {"hide", "objects", "options", "custom"}
)
public class SpecificObjectHiderPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "specificobjecthider";
	private static final String HIDDEN_OBJECTS_KEY = "hiddenObjectsList";

	@Inject
	private Client client;

	@Inject
	private SpecificObjectHiderConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Gson gson;

	@Getter
	private Set<HiddenObject> hiddenObjects = new HashSet<>();

	private SpecificObjectHiderPanel panel;
	private NavigationButton navButton;
	private boolean panelAdded = false;

	@Provides
	SpecificObjectHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpecificObjectHiderConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		loadHiddenObjects();
		panel = new SpecificObjectHiderPanel(this);

		navButton = NavigationButton.builder()
				.tooltip("Specific Object Hider")
				.icon(createThanosAnvilIcon())
				.priority(5)
				.panel(panel)
				.build();

		if (config.showPanel())
		{
			clientToolbar.addNavigation(navButton);
			panelAdded = true;
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (panelAdded)
		{
			clientToolbar.removeNavigation(navButton);
			panelAdded = false;
		}
		hiddenObjects.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (event.getKey().equals("showPanel"))
		{
			if (config.showPanel() && !panelAdded)
			{
				clientToolbar.addNavigation(navButton);
				panelAdded = true;
			}
			else if (!config.showPanel() && panelAdded)
			{
				clientToolbar.removeNavigation(navButton);
				panelAdded = false;
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.showHideMenu()) return;

		if (isObjectAction(event.getType()))
		{
			boolean alreadyHasHideOption = Arrays.stream(client.getMenuEntries())
					.anyMatch(e -> e.getOption().equals("Hide all options") && e.getIdentifier() == event.getIdentifier());

			if (!alreadyHasHideOption)
			{
				client.createMenuEntry(-1)
						.setOption("Hide all options")
						.setTarget(event.getTarget())
						.setIdentifier(event.getIdentifier())
						.setParam0(event.getActionParam0())
						.setParam1(event.getActionParam1())
						.setType(MenuAction.RUNELITE)
						.onClick(e -> hideObject(e, event.getTarget()));
			}
		}
	}

	private void hideObject(MenuEntry entry, String rawTarget)
	{
		WorldPoint wp = WorldPoint.fromScene(client, entry.getParam0(), entry.getParam1(), client.getPlane());
		String cleanName = Text.removeTags(rawTarget);

		HiddenObject obj = new HiddenObject(entry.getIdentifier(), wp.getX(), wp.getY(), wp.getPlane(), cleanName);

		hiddenObjects.remove(obj);
		hiddenObjects.add(obj);

		saveHiddenObjects();
		panel.updatePanel();
	}

	public void toggleObject(HiddenObject object)
	{
		object.setDisabled(!object.isDisabled());
		saveHiddenObjects();
		panel.updatePanel();
	}

	public void toggleAll(boolean disable)
	{
		for (HiddenObject obj : hiddenObjects)
		{
			obj.setDisabled(disable);
		}
		saveHiddenObjects();
		panel.updatePanel();
	}

	public void deleteObject(HiddenObject object)
	{
		hiddenObjects.remove(object);
		saveHiddenObjects();
		panel.updatePanel();
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) return;
		filterMenuEntries();
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		filterMenuEntries();
	}

	private void filterMenuEntries()
	{
		if (hiddenObjects.isEmpty()) return;

		MenuEntry[] entries = client.getMenuEntries();
		java.util.List<MenuEntry> filtered = new ArrayList<>();
		boolean modified = false;

		for (MenuEntry entry : entries)
		{
			if (entry.getType() != null && isObjectAction(entry.getType().getId()))
			{
				WorldPoint wp = WorldPoint.fromScene(client, entry.getParam0(), entry.getParam1(), client.getPlane());
				HiddenObject check = new HiddenObject(entry.getIdentifier(), wp.getX(), wp.getY(), wp.getPlane(), "");

				for (HiddenObject hidden : hiddenObjects)
				{
					if (hidden.equals(check) && !hidden.isDisabled())
					{
						modified = true;
						check = null;
						break;
					}
				}

				if (check == null) continue;
			}
			filtered.add(entry);
		}

		if (modified)
		{
			client.setMenuEntries(filtered.toArray(new MenuEntry[0]));
		}
	}

	private boolean isObjectAction(int actionId)
	{
		return actionId == MenuAction.GAME_OBJECT_FIRST_OPTION.getId() ||
				actionId == MenuAction.GAME_OBJECT_SECOND_OPTION.getId() ||
				actionId == MenuAction.GAME_OBJECT_THIRD_OPTION.getId() ||
				actionId == MenuAction.GAME_OBJECT_FOURTH_OPTION.getId() ||
				actionId == MenuAction.GAME_OBJECT_FIFTH_OPTION.getId() ||
				actionId == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT.getId() ||
				actionId == MenuAction.EXAMINE_OBJECT.getId();
	}

	private void saveHiddenObjects()
	{
		String json = gson.toJson(hiddenObjects);
		configManager.setConfiguration(CONFIG_GROUP, HIDDEN_OBJECTS_KEY, json);
	}

	private void loadHiddenObjects()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, HIDDEN_OBJECTS_KEY);
		if (json != null && !json.isEmpty())
		{
			Set<HiddenObject> loaded = gson.fromJson(json, new TypeToken<Set<HiddenObject>>() {}.getType());
			if (loaded != null) hiddenObjects.addAll(loaded);
		}
	}

	private BufferedImage createThanosAnvilIcon()
	{
		BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 1. Solid Left Side of Anvil
		g2.setColor(new Color(110, 115, 125));
		g2.fillRect(6, 20, 10, 5);
		g2.fillRect(8, 14, 8, 6);
		g2.fillRect(4, 10, 12, 5);

		g2.setColor(new Color(160, 165, 175));
		g2.fillRect(4, 10, 12, 1);

		// 2. Fading/Dissolving Right Side
		g2.setColor(new Color(110, 115, 125, 160));
		g2.fillRect(16, 10, 5, 5);
		g2.fillRect(16, 20, 4, 5);

		g2.setColor(new Color(110, 115, 125, 80));
		g2.fillRect(21, 10, 4, 4);

		// 3. Floating Ashes & Embers
		Color ashGrey = new Color(170, 175, 185, 200);
		Color magicGold = new Color(255, 200, 50, 230);

		g2.setColor(ashGrey);
		g2.fillRect(22, 7, 2, 2);
		g2.fillRect(26, 12, 2, 2);
		g2.fillRect(24, 18, 2, 2);
		g2.fillRect(28, 8, 1, 1);

		g2.setColor(magicGold);
		g2.fillRect(20, 6, 2, 2);
		g2.fillRect(25, 15, 2, 2);
		g2.fillRect(27, 21, 2, 2);
		g2.fillRect(29, 11, 1, 1);

		g2.dispose();
		return img;
	}
}