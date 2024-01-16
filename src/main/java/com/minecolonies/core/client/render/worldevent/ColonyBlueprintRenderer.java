package com.minecolonies.core.client.render.worldevent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.RotationMirror;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.client.ModKeyMappings;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.workorders.view.WorkOrderBuildingView;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.items.ModItems.buildTool;

/**
 * Renders blueprints in the colony area.
 */
public class ColonyBlueprintRenderer
{
    /**
     * An arbitrary not-null-but-invalid position.
     */
    private static final BlockPos INVALID_POS = BlockPos.ZERO.below(500);

    /**
     * Min distance for player to move before we recheck cache.
     */
    private static final double CACHE_RESET_RANGE = 12.5F;

    /**
     * The cached list of blueprints to be rendered.
     */
    private static Map<BlueprintCacheKey, List<BlockPos>> blueprintRenderCache = new HashMap<>();

    /**
     * The cached list of boxes to be rendered.
     */
    private static Map<BlockPos, BoxRenderData> boxRenderCache = new HashMap<>();

    /**
     * The cache of blueprint data.
     */
    private static final LoadingCache<BlueprintCacheKey, BlueprintPreviewData> blueprintDataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(2))
            .softValues()
            .build(CacheLoader.from(ColonyBlueprintRenderer::makeBlueprintPreview));

    private static BlockPos lastCacheRebuild = null;

    /**
     * True when blueprints should be rendered.  Toggled via hotkey.
     */
    private static boolean shouldRenderBlueprints = true;

    /**
     * Blueprints we're still loading.
     */
    private static final Map<BlockPos, PendingRenderData> pendingBoxes = new HashMap<>();

    /**
     * Rendering rules.  Order matters.
     */
    private static final List<IRenderBlueprintRule> renderRules = new ArrayList<>();

    static
    {
        renderRules.add(new NearBuildPreview());
        renderRules.add(new BuildGoggles());
    }

    /**
     * Invalidate the cache, because something significant has changed in colony data (e.g. more work orders).
     */
    public static void invalidateCache()
    {
        lastCacheRebuild = null;
    }

    /**
     * Indicates whether blueprint rendering is enabled or disabled.
     *
     * @return true when enabled.
     */
    public static boolean willRenderBlueprints()
    {
        return shouldRenderBlueprints;
    }

    /**
     * Renders blueprints into the client.
     *
     * @param ctx rendering context
     */
    static void renderBlueprints(final WorldEventContext ctx)
    {
        if (ModKeyMappings.TOGGLE_GOGGLES.get().consumeClick())
        {
            shouldRenderBlueprints = !shouldRenderBlueprints;

            ctx.clientPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundSource.NEUTRAL, 1.0F, shouldRenderBlueprints ? 0.75F : 0.25F);
        }

        if (!ctx.hasNearestColony())
        {
            blueprintRenderCache.clear();
            boxRenderCache.clear();
            lastCacheRebuild = null;
            return;
        }

        final List<IRenderBlueprintRule> activeRules = new ArrayList<>();
        for (final IRenderBlueprintRule rule : renderRules)
        {
            if (rule.isEnabled(ctx))
            {
                activeRules.add(rule);
            }
        }

        if (activeRules.isEmpty())
        {
            blueprintRenderCache.clear();
            boxRenderCache.clear();
            lastCacheRebuild = null;
            return;
        }

        final BlockPos activePosition = ctx.clientPlayer.blockPosition();
        if (lastCacheRebuild == null || !lastCacheRebuild.closerThan(activePosition, CACHE_RESET_RANGE))
        {
            rebuildCache(ctx, activeRules);
            lastCacheRebuild = activePosition;
        }

        if (Minecraft.getInstance().level.getGameTime() % 20 == 0)
        {
            processPendingBlueprints();
        }

        if (shouldRenderBlueprints)
        {
            for (final Map.Entry<BlueprintCacheKey, List<BlockPos>> entry : blueprintRenderCache.entrySet())
            {
                final BlueprintPreviewData data = blueprintDataCache.getUnchecked(entry.getKey());
                StructureClientHandler.renderStructureAtPosList(data, ctx.partialTicks, entry.getValue(), ctx.poseStack);
            }
        }
    }

    /**
     * Renders boxes into the client.  Must be called after {@link #renderBlueprints}.
     *
     * @param ctx rendering context
     */
    static void renderBoxes(final WorldEventContext ctx)
    {
        for (final Map.Entry<BlockPos, BoxRenderData> entry : boxRenderCache.entrySet())
        {
            final BoxRenderData buildingData = entry.getValue();

            if (buildingData.box().getPos1() != INVALID_POS)
            {
                ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource,
                        new AABB(buildingData.box().getPos1(), buildingData.box().getPos2().offset(1, 1, 1)),
                        0.08f, 0xFF0000FF, false);
            }

            buildingData.box().getAnchor().ifPresent(pos ->
            {
                if (ctx.clientPlayer.isShiftKeyDown())
                {
                    ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource,
                            new AABB(pos), 0.02f, 0xFFFF0000, true);
                }
            });

            if (ctx.nearestColony != null && buildingData.builder() != 0 && ctx.clientPlayer.isShiftKeyDown())
            {
                final ICitizenDataView citizen = ctx.nearestColony.getCitizen(buildingData.builder());
                if (citizen != null)
                {
                    final BlockPos pos = citizen.getStatusPosition();
                    if (pos != null)
                    {
                        ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource,
                                new AABB(pos), 0.02f, 0xFF00FF00, true);
                    }
                }
            }
        }

        ColonyWorldRenderMacros.endRenderLineBox(ctx.bufferSource);
    }

    private static void rebuildCache(final WorldEventContext ctx, final List<IRenderBlueprintRule> rules)
    {
        Collections.reverse(rules);   // so the first rule "wins"
        final Map<BlockPos, PendingRenderData> desired = new HashMap<>();
        for (final IRenderBlueprintRule rule : rules)
        {
            desired.putAll(rule.getDesiredBlueprints(ctx));
        }

        final Map<BlueprintCacheKey, List<BlockPos>> newBlueprints = new HashMap<>();
        final Map<BlockPos, BoxRenderData> newBoxes = new HashMap<>();
        for (final Map.Entry<BlockPos, PendingRenderData> entry : desired.entrySet())
        {
            if (entry.getValue().blueprint() != null && !entry.getValue().boxOnly())
            {
                final List<BlockPos> posList = newBlueprints.computeIfAbsent(entry.getValue().blueprint(), k -> new ArrayList<>());
                posList.add(entry.getKey());
            }

            final BoxRenderData newBox = tryLoadBox(entry.getValue());
            if (newBox != null)
            {
                newBoxes.put(entry.getKey(), newBox);
            }
            else
            {
                pendingBoxes.put(entry.getKey(), entry.getValue());

                final BoxRenderData oldBox = boxRenderCache.getOrDefault(entry.getKey(), null);
                if (oldBox != null)
                {
                    newBoxes.put(entry.getKey(), oldBox);
                }
            }
        }
        blueprintRenderCache = newBlueprints;
        boxRenderCache = newBoxes;
    }

    private static @Nullable BoxRenderData tryLoadBox(@NotNull final PendingRenderData data)
    {
        if (data.blueprint() == null)
        {
            if (data.boxOnly() && data.hasAnchor())     // render only the anchor
            {
                final BoxPreviewData box = new BoxPreviewData(INVALID_POS, INVALID_POS, Optional.of(data.pos()));
                return new BoxRenderData(box, 0);
            }
            return new BoxRenderData(null, 0);
        }

        final BlueprintPreviewData blueprintData = blueprintDataCache.getUnchecked(data.blueprint());
        final Blueprint localBlueprint = blueprintData.getBlueprint();
        if (localBlueprint == null)
        {
            return null;
        }

        if (blueprintData.isEmpty())
        {
            return new BoxRenderData(null, 0);
        }
        else
        {
            final BlockPos primaryOffset = localBlueprint.getPrimaryBlockOffset();
            final BlockPos boxStartPos = data.pos().subtract(primaryOffset);
            final BlockPos boxEndPos = boxStartPos.offset(localBlueprint.getSizeX() - 1, localBlueprint.getSizeY() - 1, localBlueprint.getSizeZ() - 1);
            final BoxPreviewData box = new BoxPreviewData(boxStartPos, boxEndPos, data.hasAnchor() ? Optional.of(data.pos()) : Optional.empty());

            return new BoxRenderData(box, data.builder());
        }
    }

    private static void processPendingBlueprints()
    {
        final Iterator<Map.Entry<BlockPos, PendingRenderData>> iterator = pendingBoxes.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<BlockPos, PendingRenderData> entry = iterator.next();
            final BoxRenderData box = tryLoadBox(entry.getValue());
            if (box != null)
            {
                if (box.box() != null || box.builder() != 0)
                {
                    boxRenderCache.put(entry.getKey(), box);
                }
                iterator.remove();
            }
        }
    }

    private static @NotNull BlueprintPreviewData makeBlueprintPreview(@NotNull final BlueprintCacheKey key)
    {
        final Future<Blueprint> blueprintFuture = StructurePacks.getBlueprintFuture(key.packName(), key.path());

        final BlueprintPreviewData blueprintPreviewData = new BlueprintPreviewData(false);
        blueprintPreviewData.setBlueprintFuture(blueprintFuture);
        blueprintPreviewData.setPos(BlockPos.ZERO);
        blueprintPreviewData.setRotationMirror(key.orientation());

        return blueprintPreviewData;
    }

    /**
     * Abstract rule for deciding which blueprints to render.
     */
    private interface IRenderBlueprintRule
    {
        /**
         * Quick check if this rule applies
         */
        boolean isEnabled(WorldEventContext ctx);

        /**
         * Report the complete list of locations that should render blueprints.
         */
        Map<BlockPos, PendingRenderData> getDesiredBlueprints(WorldEventContext ctx);
    }

    /**
     * Holds blueprint renderdata (pending load).
     */
    private record PendingRenderData(@Nullable BlueprintCacheKey blueprint, @NotNull BlockPos pos, int builder, boolean boxOnly, boolean hasAnchor)
    {
    }

    /**
     * Holds box renderdata (loaded).
     */
    private record BoxRenderData(@Nullable BoxPreviewData box, int builder)
    {
    }

    /**
     * Cache key for {@link #blueprintDataCache}.
     */
    private record BlueprintCacheKey(@NotNull String packName, @NotNull String path, RotationMirror orientation)
    {
    }

    /**
     * Render buildings at max level adjacent to the current build tool preview.
     */
    private static class NearBuildPreview implements IRenderBlueprintRule
    {
        @Override
        public boolean isEnabled(final WorldEventContext ctx)
        {
            return RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() != null &&
                    MinecoloniesAPIProxy.getInstance().getConfig().getClient().neighborbuildingrendering.get() &&
                    ctx.mainHandItem.getItem() == buildTool.get();
        }

        @Override
        public Map<BlockPos, PendingRenderData> getDesiredBlueprints(final WorldEventContext ctx)
        {
            final Map<BlockPos, PendingRenderData> desired = new HashMap<>();
            final BlockPos activePosition = RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getPos();
            final Blueprint blueprint = RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint();
            final BlockPos zeroPos = activePosition.subtract(blueprint.getPrimaryBlockOffset());
            final AABB blueprintAABB = new AABB(zeroPos, zeroPos.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1))
                    .inflate(2 + MinecoloniesAPIProxy.getInstance().getConfig().getClient().neighborbuildingrange.get());

            for (final IBuildingView buildingView : ctx.nearestColony.getBuildings())
            {
                if (buildingView.getBuildingType() == ModBuildings.postBox.get()
                      || buildingView.getBuildingType() == ModBuildings.stash.get()
                      || buildingView.getStructurePath().replace(".blueprint", "").isEmpty())
                {
                    continue;
                }
                final BlockPos currentPosition = buildingView.getPosition();
                if (ctx.clientLevel.getBlockEntity(currentPosition) instanceof final TileEntityColonyBuilding tileEntityColonyBuilding)
                {
                    final Tuple<BlockPos, BlockPos> corners = tileEntityColonyBuilding.getInWorldCorners();
                    BlockPos cornerA = corners.getA();
                    BlockPos cornerB = corners.getB();

                    if (blueprintAABB.intersects(new AABB(cornerA, cornerB)))
                    {
                        String schemPath = buildingView.getStructurePath();
                        schemPath = schemPath.replace(".blueprint", "");
                        schemPath = schemPath.substring(0, schemPath.length() - 1) + buildingView.getBuildingMaxLevel() + ".blueprint";

                        final String structurePack = buildingView.getStructurePack();
                        final BlueprintCacheKey key = new BlueprintCacheKey(structurePack, schemPath,
                                RotationMirror.of(BlockPosUtil.getRotationFromRotations(buildingView.getRotation()),
                                        buildingView.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE));

                        desired.put(currentPosition,
                                new PendingRenderData(key, currentPosition, 0,
                                    buildingView.getBuildingLevel() >= buildingView.getBuildingMaxLevel(),
                                    true));
                    }
                }
            }

            return desired;
        }
    }

    /**
     * Render work orders near the player wearing build goggles.
     */
    private static class BuildGoggles implements IRenderBlueprintRule
    {
        @Override
        public boolean isEnabled(final WorldEventContext ctx)
        {
            return ctx.clientPlayer.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.buildGoggles);
        }

        @Override
        public Map<BlockPos, PendingRenderData> getDesiredBlueprints(final WorldEventContext ctx)
        {
            // ideally we'd check based on the bounding box, but we don't know that until we load the blueprints
            final double range = MathUtils.square(MinecoloniesAPIProxy.getInstance().getConfig().getClient().buildgogglerange.get());

            // show work orders
            final Map<BlockPos, PendingRenderData> desired = new HashMap<>();
            for (final IWorkOrderView workOrder : ctx.nearestColony.getWorkOrders())
            {
                if (workOrder.getLocation().distSqr(ctx.clientPlayer.blockPosition()) < range)
                {
                    final int builder = getBuilderId(ctx.nearestColony, workOrder.getClaimedBy());
                    final BlueprintCacheKey key = new BlueprintCacheKey(workOrder.getPackName(), workOrder.getStructurePath(),
                            RotationMirror.of(BlockPosUtil.getRotationFromRotations(workOrder.getRotation()),
                                    workOrder.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE));
                    desired.put(workOrder.getLocation(),
                            new PendingRenderData(key, workOrder.getLocation(), builder,
                                workOrder.getWorkOrderType() == WorkOrderType.REMOVE,
                                workOrder instanceof WorkOrderBuildingView));
                }
            }

            // and also just the anchor pos for unbuilt non-work-orders, to help find lost huts
            for (final IBuildingView building : ctx.nearestColony.getBuildings())
            {
                if (!desired.containsKey(building.getPosition()) &&
                        building.getBuildingLevel() == 0 &&
                        building.getBuildingMaxLevel() > 0 &&
                        building.getPosition().distSqr(ctx.clientPlayer.blockPosition()) < range)
                {
                    desired.put(building.getPosition(),
                            new PendingRenderData(null, building.getPosition(), 0,
                                    true, true));
                }
            }

            return desired;
        }

        /**
         * Find the builder for this work order, if any.
         *
         * @param colony the colony.
         * @param builderPos the builder's building id.
         * @return the builder's id, or 0.
         */
        private int getBuilderId(final IColonyView colony, final BlockPos builderPos)
        {
            if (builderPos != null && !builderPos.equals(BlockPos.ZERO))
            {
                final IBuildingView builderView = colony.getBuilding(builderPos);
                if (builderView != null)
                {
                    final Set<Integer> builders = builderView.getAllAssignedCitizens();
                    if (!builders.isEmpty())
                    {
                        return builders.iterator().next();
                    }
                }
            }
            return 0;
        }
    }
}
