package ch.vtt.lanunderground.renderer;

import ch.vtt.lanunderground.LanUnderground;
import ch.vtt.lanunderground.armor.BronzeOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.ElectricOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.SteelOxygenTankArmorMaterial;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class OxygenHudRenderer implements HudRenderCallback {
    private static final Identifier WATER_FLOW  = new Identifier("minecraft", "block/water_flow");
    private static final Identifier BLOCK_ATLAS = new Identifier("minecraft", "textures/atlas/blocks.png");
    private static final Identifier OXYGEN_BAR  = new Identifier(LanUnderground.MOD_ID, "textures/gui/oxygen_bar.png");
    private static final int BAR_WIDTH  = 80;
    private static final int BAR_HEIGHT = 10;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private Sprite waterSprite;

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (client.player == null || client.world == null) return;

        // Tank‑Check
        ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ArmorItem armor)) return;
        if (!(
                armor.getMaterial() instanceof BronzeOxygenTankArmorMaterial ||
                armor.getMaterial() instanceof SteelOxygenTankArmorMaterial ||
                armor.getMaterial() instanceof ElectricOxygenTankArmorMaterial
        )) return;

        // Oxygen‑Berechnung
        int oxygen   = chest.getMaxDamage() - chest.getDamage();
        int maxO2    = chest.getMaxDamage();
        int filledPx = (int)((oxygen / (float)maxO2) * BAR_WIDTH);

        // Position des Balkens
        int x = 5;
        int y = 5;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, OXYGEN_BAR);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.drawTexture(
            OXYGEN_BAR,
            x - 2,
            y - 2,
            0,
            0,
            0,
            84,
            14,
            84,
            14
        );

        // Lazy‑load des Sprites
        if (waterSprite == null) {
            waterSprite = client.getSpriteAtlas(BLOCK_ATLAS).apply(WATER_FLOW);
        }

        // Shader + Atlas + Färbung
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, BLOCK_ATLAS);
        RenderSystem.setShaderColor(0.11f, 0.33f, 0.52f, 1.0f);

        context.enableScissor(x, y, x + filledPx, y + BAR_HEIGHT);
        context.drawSprite(
            x,
            y,
            1,
            BAR_WIDTH,
            BAR_HEIGHT,
            waterSprite
        );
        context.disableScissor();

        // Shader‑Farbe zurücksetzen
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        Text text = Text.of(oxygen + " mB");
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 2);
        context.drawText(
            client.textRenderer,
            text,
            x + 40 - client.textRenderer.getWidth(text) / 2,
            y + 1,
            0xFFFFFF,
            true
        );
        context.getMatrices().pop();
    }
}
