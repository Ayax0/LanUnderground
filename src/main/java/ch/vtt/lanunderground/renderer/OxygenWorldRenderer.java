package ch.vtt.lanunderground.renderer;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import ch.vtt.lanunderground.utils.AtmosphereRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class OxygenWorldRenderer implements WorldRenderEvents.AfterEntities {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ArrayList<MachineBlockEntity> atmosphereGenerators = new ArrayList<>();

    private final int[][] edges = {
        {0, 1}, {1, 2}, {2, 3}, {3, 0},
        {4, 5}, {5, 6}, {6, 7}, {7, 4},
        {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };

    public OxygenWorldRenderer() {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((block, world) -> {
            if (isAtmosphereGenerator(block))
                atmosphereGenerators.add((MachineBlockEntity) block);
        });

        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((block, world) -> {
            if (isAtmosphereGenerator(block))
                atmosphereGenerators.remove((MachineBlockEntity) block);
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, block) -> {
            if(!(block instanceof MachineBlockEntity machine)) return;
            atmosphereGenerators.remove(machine);
        });
    }

    @Override
    public void afterEntities(WorldRenderContext context) {
        if (client.player == null || client.world == null) return;

        String mainHandId = Registries.ITEM.getId(client.player.getMainHandStack().getItem()).toString();
        String offHandId = Registries.ITEM.getId(client.player.getOffHandStack().getItem()).toString();
        if(!(
            mainHandId.equals("modern_industrialization:wrench") ||
            offHandId.equals("modern_industrialization:wrench")
        )) return;

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate buffer = client.getBufferBuilders().getEntityVertexConsumers();

        for(MachineBlockEntity machine : atmosphereGenerators) {
            int range = getRange(machine);
            int color = isMachineActive(machine) ? 0xff00ff00 : 0xffff0000;

            Vec3d center = Vec3d.ofCenter(machine.getPos());
            Box box = new Box(
                center.add(-range, -range, -range),
                center.add(range, range, range)
            ).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
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

            VertexConsumer consumer = buffer.getBuffer(RenderLayer.getLines());
            MatrixStack.Entry entry = matrices.peek();

            for(int[] edge : edges) {
                Vec3d from = corners[edge[0]];
                Vec3d to = corners[edge[1]];
                consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
                    .color(color)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();
                consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
                    .color(color)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();
            }

            List<Vec3d> points = AtmosphereRenderer.getTerrainOutline(machine);
            for (int i = 0; i < points.size(); i++) {
                Vec3d from = points.get(i).subtract(cameraPos);
                Vec3d to = points.get((i + 1) % points.size()).subtract(cameraPos);
                consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
                    .color(color)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();

                consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
                    .color(color)
                    .normal(entry.getNormalMatrix(), 0, 1, 0)
                    .next();
            }

            buffer.draw();
        }
    }

    private boolean isAtmosphereGenerator(Object block) {
        if (!(block instanceof MachineBlockEntity machine)) return false;
        String id = Registries.BLOCK.getId(machine.getCachedState().getBlock()).toString();
        return id.equals("modern_industrialization:bronze_atmosphere_generator") ||
            id.equals("modern_industrialization:steel_atmosphere_generator") ||
            id.equals("modern_industrialization:electric_atmosphere_generator");
    }

    private boolean isMachineActive(MachineBlockEntity machine) {
        AtomicBoolean isActive = new AtomicBoolean(false);
        machine.forComponentType(IsActiveComponent.class, comp -> {
            if(comp.isActive) {
                isActive.set(true);
                return;
            }
        });
        return isActive.get();
    }

    private int getRange(MachineBlockEntity machine) {
        String id = Registries.BLOCK.getId(machine.getCachedState().getBlock()).toString();
        return switch (id) {
            case "modern_industrialization:bronze_atmosphere_generator" -> 16;
            case "modern_industrialization:steel_atmosphere_generator" -> 32;
            case "modern_industrialization:electric_atmosphere_generator" -> 64;
            default -> 0;
        };
    }
}
