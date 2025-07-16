package ch.vtt.lanunderground.event;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import ch.vtt.lanunderground.armor.BronzeOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.ElectricOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.SteelOxygenTankArmorMaterial;
import ch.vtt.lanunderground.utils.AtmosphereGenerator;
import ch.vtt.lanunderground.utils.AtmosphereRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AirManager {
    private static final ArrayList<MachineBlockEntity> atmosphereGenerators = new ArrayList<>();

    public static void register() {
        registerAtmosphereTracking();
        registerHudRenderer();
        registerWorldRenderer();
    }

    // 1. Registriere und tracke relevante Atmosphären-Generatoren
    private static void registerAtmosphereTracking() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((block, world) -> {
            if (isAtmosphereGenerator(block)) {
                atmosphereGenerators.add((MachineBlockEntity) block);
            }
        });

        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((block, world) -> {
            if (isAtmosphereGenerator(block)) {
                atmosphereGenerators.remove(block);
            }
        });
    }

    private static boolean isAtmosphereGenerator(Object block) {
        if (!(block instanceof MachineBlockEntity machine)) return false;
        String id = Registries.BLOCK.getId(machine.getCachedState().getBlock()).toString();
        return id.equals("modern_industrialization:bronze_atmosphere_generator") ||
                id.equals("modern_industrialization:steel_atmosphere_generator") ||
                id.equals("modern_industrialization:electric_atmosphere_generator");
    }

    // 2. HUD für Sauerstoff-Anzeige
    private static void registerHudRenderer() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (!(chestStack.getItem() instanceof ArmorItem armor)) return;

            if (
                armor.getMaterial() instanceof BronzeOxygenTankArmorMaterial ||
                armor.getMaterial() instanceof SteelOxygenTankArmorMaterial ||
                armor.getMaterial() instanceof ElectricOxygenTankArmorMaterial
            ) {
                renderOxygenHud(drawContext, chestStack);
            }
        });
    }

    private static void renderOxygenHud(DrawContext drawContext, ItemStack chest) {
        MinecraftClient client = MinecraftClient.getInstance();

        int maxOxygen = chest.getMaxDamage();
        int currentOxygen = maxOxygen - chest.getDamage();
        int oxygenWidth = (int) ((80 / (float) maxOxygen) * currentOxygen);

        // Hintergrund
        drawContext.fill(1, 1, 81, 10, 0xFF444444);
        // Füllung
        drawContext.fill(1, 1, 1 + oxygenWidth, 10, 0xFF0C47A6);
        // Textanzeige
        Text text = Text.of(currentOxygen + " mB");
        int textX = 41 - client.textRenderer.getWidth(text) / 2;
        drawContext.drawText(client.textRenderer, text, textX, 2, 0xFFFFFF, true);
    }

    // 3. Welt-Rendering für Atmosphärenbereich
    private static void registerWorldRenderer() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;

            if(player == null) return;
            String mainHandId = Registries.ITEM.getId(player.getMainHandStack().getItem()).toString();
            String offHandId = Registries.ITEM.getId(player.getOffHandStack().getItem()).toString();

            if(
                !mainHandId.equals("modern_industrialization:wrench") &&
                !offHandId.equals("modern_industrialization:wrench")
            ) return;

            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();

            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider.Immediate buffer = client.getBufferBuilders().getEntityVertexConsumers();

            for (MachineBlockEntity block : atmosphereGenerators) {
                renderAtmosphereBoundingBox(matrices, buffer, block, cameraPos);
                renderTerrainOutline(matrices, buffer, block, cameraPos);
            }

            buffer.draw();  // Zeichne alles am Ende
        });
    }

    private static void renderAtmosphereBoundingBox(MatrixStack matrices, VertexConsumerProvider buffer, MachineBlockEntity block, Vec3d cameraPos) {
        Vec3d center = Vec3d.ofCenter(block.getPos());
        int range = AtmosphereGenerator.getRange(block);
        Box box = new Box(center.add(-range, -range, -range), center.add(range, range, range))
                .offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer consumer = buffer.getBuffer(RenderLayer.getLines());
        drawBoxLines(consumer, matrices.peek(), box, 0f, 0f, 1f, 1f);  // Grün
    }

    private static void renderTerrainOutline(MatrixStack matrices, VertexConsumerProvider buffer, MachineBlockEntity block, Vec3d cameraPos) {
        VertexConsumer consumer = buffer.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();

        List<Vec3d> points = AtmosphereRenderer.getTerrainOutline(block);
        for (int i = 0; i < points.size(); i++) {
            Vec3d from = points.get(i).subtract(cameraPos);
            Vec3d to = points.get((i + 1) % points.size()).subtract(cameraPos);

            consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
                    .color(0f, 0f, 1f, 1f) // Grün
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();

            consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
                    .color(0f, 0f, 1f, 1f)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();
        }
    }

    private static void drawBoxLines(VertexConsumer consumer, MatrixStack.Entry entry, Box box, float r, float g, float b, float a) {
        Vec3d[] corners = {
                new Vec3d(box.minX, box.minY, box.minZ),
                new Vec3d(box.maxX, box.minY, box.minZ),
                new Vec3d(box.maxX, box.maxY, box.minZ),
                new Vec3d(box.minX, box.maxY, box.minZ),
                new Vec3d(box.minX, box.minY, box.maxZ),
                new Vec3d(box.maxX, box.minY, box.maxZ),
                new Vec3d(box.maxX, box.maxY, box.maxZ),
                new Vec3d(box.minX, box.maxY, box.maxZ)
        };

        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] edge : edges) {
            Vec3d from = corners[edge[0]];
            Vec3d to = corners[edge[1]];
            consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
                    .color(r, g, b, a)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();
            consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
                    .color(r, g, b, a)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();
        }
    }
}