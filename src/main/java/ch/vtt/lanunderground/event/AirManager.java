package ch.vtt.lanunderground.event;

import ch.vtt.lanunderground.renderer.OxygenHudRenderer;
import ch.vtt.lanunderground.renderer.OxygenWorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class AirManager {

    public static void register() {
        HudRenderCallback.EVENT.register(new OxygenHudRenderer());
        WorldRenderEvents.AFTER_ENTITIES.register(new OxygenWorldRenderer());
    }
}