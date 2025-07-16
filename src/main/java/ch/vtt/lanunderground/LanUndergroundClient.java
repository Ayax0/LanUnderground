package ch.vtt.lanunderground;

import ch.vtt.lanunderground.event.AirManager;
import net.fabricmc.api.ClientModInitializer;

public class LanUndergroundClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AirManager.register();
    }
}
