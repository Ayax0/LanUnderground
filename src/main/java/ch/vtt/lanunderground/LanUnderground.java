package ch.vtt.lanunderground;

import ch.vtt.lanunderground.entity.SpawnManager;
import ch.vtt.lanunderground.event.AirManagerServer;
import ch.vtt.lanunderground.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanUnderground implements ModInitializer {
	public static final String MOD_ID = "lanunderground";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		SpawnManager.registerSpawnRules();

		new AirManagerServer().register();
	}
}