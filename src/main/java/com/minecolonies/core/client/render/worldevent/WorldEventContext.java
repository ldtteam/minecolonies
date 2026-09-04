package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.structurize.client.rendertask.util.BufferSourceCompat;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.core.client.render.TileEntityColonySignRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldEventContext
{
    public static final WorldEventContext INSTANCE = new WorldEventContext();
    public static final float DEFAULT_LINE_WIDTH = 0.025F;
    public static final net.minecraft.client.renderer.rendertype.RenderType LINES_WITH_WIDTH =
        WorldRenderMacros.LINES_WITH_WIDTH;

    private WorldEventContext()
    {
    }

    public RenderLevelStageEvent stageEvent;
    public SubmitCustomGeometryEvent submitEvent;
    public BufferSourceCompat bufferSource;
    public PoseStack poseStack;
    public ClientLevel clientLevel;
    public LocalPlayer clientPlayer;
    public ItemStack mainHandItem;
    public int clientRenderDist;

    @Nullable
    public IColonyView nearestColony;

    boolean hasNearestColony()
    {
        return nearestColony != null;
    }

    public void renderWorldLastEvent(final RenderLevelStageEvent event)
    {
        stageEvent = event;
        submitEvent = null;
        bufferSource = WorldRenderMacros.getBufferSource();
        poseStack = event.getPoseStack();
        clientLevel = Minecraft.getInstance().level;
        clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null || clientLevel == null)
        {
            return;
        }

        mainHandItem = clientPlayer.getMainHandItem();
        clientRenderDist = Minecraft.getInstance().options.renderDistance().get();
        checkNearbyColony(clientLevel);

        final Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.mainCamera().position();
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x(), -cameraPosition.y(), -cameraPosition.z());

        renderWithinContext(event);

        bufferSource.endBatch();
        poseStack.popPose();
    }

    /**
     * Submit colony blueprint geometry while Minecraft is collecting the
     * feature nodes for the current frame. RenderLevelStageEvent is too late
     * for SubmitNodeCollector submissions on Minecraft 26.2.
     */
    public void submitBlueprints(final SubmitCustomGeometryEvent event)
    {
        submitEvent = event;
        stageEvent = null;
        bufferSource = null;
        poseStack = event.getPoseStack();
        clientLevel = Minecraft.getInstance().level;
        clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null || clientLevel == null)
        {
            submitEvent = null;
            return;
        }

        mainHandItem = clientPlayer.getMainHandItem();
        clientRenderDist = Minecraft.getInstance().options.renderDistance().get();
        checkNearbyColony(clientLevel);
        ColonyBlueprintRenderer.renderBlueprints(this);
        submitEvent = null;
    }

    private void renderWithinContext(final RenderLevelStageEvent event)
    {
        if (event instanceof RenderLevelStageEvent.AfterOpaqueFeatures)
        {
            ColonyBorderRenderer.render(this);
            ColonyWaypointRenderer.render(this);
            ColonyPatrolPointRenderer.render(this);
            GuardTowerRallyBannerRenderer.render(this);
            PathfindingDebugRenderer.render(this);
            ColonyBlueprintRenderer.renderBoxes(this);
            ItemOverlayBoxesRenderer.render(this);
            HighlightManager.render(this);
        }
        else if (event instanceof RenderLevelStageEvent.AfterTranslucentBlocks)
        {
            TileEntityColonySignRenderer.renderSignHover(this);
        }
    }

    public void checkNearbyColony(final Level level)
    {
        if (clientPlayer != null)
        {
            nearestColony = IColonyManager.getInstance().getClosestColonyView(level, clientPlayer.blockPosition());
        }
    }

    public void renderLineBoxWithShadow(final BlockPos pos, final int argbColor, final float lineWidth)
    {
        WorldRenderMacros.renderLineBox(
            poseStack,
            bufferSource,
            new AABB(pos),
            lineWidth,
            withHalfAlpha(argbColor),
            true);
        WorldRenderMacros.renderLineBox(
            poseStack,
            bufferSource,
            new AABB(pos),
            lineWidth,
            argbColor,
            false);
    }

    public void renderLineBox(final BlockPos position, final int argbColor, final float lineWidth)
    {
        WorldRenderMacros.renderLineBox(poseStack, bufferSource, new AABB(position), lineWidth, argbColor, false);
    }

    public void renderLineAABBWithShadow(final AABB aabb, final int argbColor, final float lineWidth)
    {
        WorldRenderMacros.renderLineBox(
            poseStack,
            bufferSource,
            aabb,
            lineWidth,
            withHalfAlpha(argbColor),
            true);
        WorldRenderMacros.renderLineBox(
            poseStack,
            bufferSource,
            aabb,
            lineWidth,
            argbColor,
            false);
    }

    public void renderLineAABB(final AABB aabb, final int argbColor, final float lineWidth)
    {
        WorldRenderMacros.renderLineBox(poseStack, bufferSource, aabb, lineWidth, argbColor, false);
    }

    public void pushPoseCameraToPos(final BlockPos position)
    {
        poseStack.pushPose();
        poseStack.translate(position.getX(), position.getY(), position.getZ());
    }

    public void popPose()
    {
        poseStack.popPose();
    }

    public void renderBlueprint(final BlueprintPreviewData data, final List<BlockPos> positions)
    {
        if (data == null || data.getBlueprint() == null || submitEvent == null)
        {
            return;
        }

        for (final BlockPos position : positions)
        {
            BlueprintHandler.getInstance().internalBackportDraw(data, position, submitEvent);
        }
    }

    public void renderLineBox(final Object ignoredRenderType,
        final BlockPos first,
        final BlockPos second,
        final int argbColor,
        final float lineWidth)
    {
        WorldRenderMacros.renderLineBox(
            poseStack,
            bufferSource,
            new AABB(first),
            lineWidth,
            argbColor,
            false);
    }

    public void renderDebugText(final BlockPos position, final List<String> text, final boolean forceWhite, final int mergeEvery)
    {
        WorldRenderMacros.renderDebugText(position, text, poseStack, forceWhite, mergeEvery, bufferSource);
    }

    private static int withHalfAlpha(final int argbColor)
    {
        return (ARGB.alpha(argbColor) / 2) << 24 | (argbColor & 0x00ffffff);
    }
}
