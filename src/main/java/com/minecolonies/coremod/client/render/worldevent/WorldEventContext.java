package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Main class for handling world rendering.
 * Also holds all possible values which may be needed during rendering.
 */
public class WorldEventContext
{
    private static WorldEventContext instance;

    /**
     * In chunks
     */
    private RenderLevelStageEvent stageEvent;
    private int                   clientRenderDist;
    private BufferSource          bufferSource;
    private PoseStack             poseStack;
    private float                 partialTicks;
    private ClientLevel           clientLevel;
    private LocalPlayer           clientPlayer;
    private ItemStack             mainHandItem;
    @Nullable
    private IColonyView           nearestColony;

    private WorldEventContext()
    {
        // singleton
    }

    /**
     * Get the instance of the world event context.
     *
     * @return the world event context.
     */
    public static synchronized WorldEventContext getInstance()
    {
        if (instance == null)
        {
            instance = new WorldEventContext();
        }
        return instance;
    }

    /**
     * Get the render level stage event.
     *
     * @return the render level stage event.
     */
    public RenderLevelStageEvent getStageEvent()
    {
        return stageEvent;
    }

    /**
     * Get the client render distance.
     *
     * @return the client render distance.
     */
    public int getClientRenderDist()
    {
        return clientRenderDist;
    }

    /**
     * Get the buffer source.
     *
     * @return the buffer source.
     */
    public BufferSource getBufferSource()
    {
        return bufferSource;
    }

    /**
     * Get the pose stack.
     *
     * @return the pose stack.
     */
    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    /**
     * Get the partial ticks.
     *
     * @return the partial ticks.
     */
    public float getPartialTicks()
    {
        return partialTicks;
    }

    /**
     * Get the client level.
     *
     * @return the client level.
     */
    public ClientLevel getClientLevel()
    {
        return clientLevel;
    }

    /**
     * Get the player.
     *
     * @return the player.
     */
    public LocalPlayer getClientPlayer()
    {
        return clientPlayer;
    }

    /**
     * Get the main hand item, if any.
     *
     * @return the main hand item or an empty stack.
     */
    public ItemStack getMainHandItem()
    {
        return mainHandItem;
    }

    /**
     * Get the nearest colony, if any.
     *
     * @return the nearest colony or null.
     */
    public IColonyView getNearestColony()
    {
        return nearestColony;
    }

    public void renderWorldLastEvent(final RenderLevelStageEvent event)
    {
        stageEvent = event;
        bufferSource = WorldRenderMacros.getBufferSource();
        poseStack = event.getPoseStack();
        partialTicks = event.getPartialTick();
        clientLevel = Minecraft.getInstance().level;
        clientPlayer = Minecraft.getInstance().player;
        mainHandItem = clientPlayer != null ? clientPlayer.getMainHandItem() : ItemStack.EMPTY;
        nearestColony = IColonyManager.getInstance().getClosestColonyView(clientLevel, clientPlayer.blockPosition());
        clientRenderDist = Minecraft.getInstance().options.renderDistance().get();

        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)
        {
            ColonyBorderRenderer.render(this); // renders directly (not into bufferSource)
            ColonyBlueprintRenderer.renderBlueprints(this);
            ColonyWaypointRenderer.render(this);
            ColonyPatrolPointRenderer.render(this);
            GuardTowerRallyBannerRenderer.render(this);
            PathfindingDebugRenderer.render(this);

            bufferSource.endBatch();
        }
        else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)
        {
            ColonyBlueprintRenderer.renderBoxes(this);
            ItemOverlayBoxesRenderer.render(this);
            HighlightManager.render(this);

            bufferSource.endBatch();
        }

        poseStack.popPose();
    }

    public boolean hasNearestColony()
    {
        return nearestColony != null;
    }
}
