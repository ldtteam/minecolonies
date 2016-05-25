package com.schematica.handler.client;

import com.schematica.Settings;
import com.schematica.client.world.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RenderTickHandler {
    public static final RenderTickHandler INSTANCE = new RenderTickHandler();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private RenderTickHandler() {}

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent event) {
        final SchematicWorld schematic = Settings.instance.schematic;

        Settings.instance.movingObjectPosition = schematic != null ? rayTrace(schematic, 1.0f) : null;
    }

    private MovingObjectPosition rayTrace(final SchematicWorld schematic, final float partialTicks) {
        final Entity renderViewEntity = this.minecraft.getRenderViewEntity();
        if (renderViewEntity == null) {
            return null;
        }

        final double blockReachDistance = this.minecraft.playerController.getBlockReachDistance();

        final double posX = renderViewEntity.posX;
        final double posY = renderViewEntity.posY;
        final double posZ = renderViewEntity.posZ;

        renderViewEntity.posX -= schematic.position.x;
        renderViewEntity.posY -= schematic.position.y;
        renderViewEntity.posZ -= schematic.position.z;

        final Vec3 vecPosition = renderViewEntity.getPositionEyes(partialTicks);
        final Vec3 vecLook = renderViewEntity.getLook(partialTicks);
        final Vec3 vecExtendedLook = vecPosition.addVector(vecLook.xCoord * blockReachDistance, vecLook.yCoord * blockReachDistance, vecLook.zCoord * blockReachDistance);

        renderViewEntity.posX = posX;
        renderViewEntity.posY = posY;
        renderViewEntity.posZ = posZ;

        return schematic.rayTraceBlocks(vecPosition, vecExtendedLook, false, false, true);
    }
}
