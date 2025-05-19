package com.ironwill;

import com.google.inject.Injector;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Ironwill",
	description = "Records boss/raids new collection items with KC and date"
)
public class IronwillPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IronwillConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Injector injector;

	private IronwillPanel panel;
	private NavigationButton navButton;

	private static final Pattern RAID_KC_REGEX =
			Pattern.compile("^Your completed (.+?) count is: (?<kc>\\d+)$");
	private static final Pattern BOSS_KC_REGEX =
			Pattern.compile("^Your (.+?) kill count is: (?<kc>\\d+)$");
	private static final Pattern COLLECTION_LOG_REGEX =
			Pattern.compile("^New item added to your collection log: (.+)$");
	private String lastBoss;
	private final Map<String, Integer> lastKcMap = new HashMap<>();
	private int kcTimestamp;

	private static final String ENTRY_DELIMITER = ";";
	private static final String FIELD_DELIMITER = ",";

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(IronwillPanel.class);
		panel.init();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Ironwill")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		loadEntries();
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	IronwillConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronwillConfig.class);
	}

    @Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM)
			return;

		String message = chatMessage.getMessage();
		Matcher raidMatcher = RAID_KC_REGEX.matcher(message);
		Matcher bossMatcher = BOSS_KC_REGEX.matcher(message);
		Matcher collectionMatcher = COLLECTION_LOG_REGEX.matcher(message);

		if (raidMatcher.matches())
		{
			String raidName = raidMatcher.group(1);
			int kc = Integer.parseInt(raidMatcher.group("kc"));
			lastKcMap.put(raidName, kc);
			lastBoss = raidMatcher.group(1);
			kcTimestamp = chatMessage.getTimestamp();
		}
		else if (bossMatcher.matches())
		{
			String bossName = bossMatcher.group(1);
			int kc = Integer.parseInt(bossMatcher.group("kc"));
			lastKcMap.put(bossName, kc);
			lastBoss = bossMatcher.group(1);
			kcTimestamp = chatMessage.getTimestamp();
		}
		if (collectionMatcher.matches() && (chatMessage.getTimestamp() - kcTimestamp < 10))
		{
			int itemId = client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0);
			int kc = lastKcMap.getOrDefault(lastBoss, 0);
			ItemComposition comp = itemManager.getItemComposition(itemId);
			String nameItem = comp.getName();
			LocalDate date = LocalDate.now();
			panel.addEntry(itemId, kc, nameItem, date);
			saveEntry(itemId, kc, nameItem, date);
		}
	}

	private void saveEntry(int itemId, int kc, String name, LocalDate date)
	{
		String newEntry = itemId + FIELD_DELIMITER +
				kc + FIELD_DELIMITER +
				name + FIELD_DELIMITER +
				date;

		String currentEntries = config.entries();
		config.entries(currentEntries + ENTRY_DELIMITER + newEntry);
	}

	private void loadEntries()
	{
		String entries = config.entries();
		if (entries == null || entries.isEmpty()) return;

		for (String entry : entries.split(ENTRY_DELIMITER))
		{
			if (entry.isEmpty()) continue;

			String[] parts = entry.split(FIELD_DELIMITER);
			if (parts.length != 4) continue;

			int itemId = Integer.parseInt(parts[0]);
			int kc = Integer.parseInt(parts[1]);
			String name = parts[2];
			LocalDate date = LocalDate.parse(parts[3]);
			panel.addEntry(itemId, kc, name, date);
		}
	}
}