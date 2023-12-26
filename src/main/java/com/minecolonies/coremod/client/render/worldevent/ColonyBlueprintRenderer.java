package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.coremod.colony.workorders.view.WorkOrderBuildingView;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

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
     * The cached map of blueprints that are rendered.
     */
    private static Map<BlockPos, RenderData> blueprintCache = new HashMap<>();

    private static BlockPos lastCacheRebuild = null;

    /**
     * Blueprints we're still loading.
     */
    private static final Queue<PendingRenderData> pendingBlueprints = new LinkedList<>();

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
     * Renders blueprints into the client.
     *
     * @param ctx rendering context
     */
    static void renderBlueprints(final WorldEventContext ctx)
    {
        if (!ctx.hasNearestColony())
        {
            blueprintCache.clear();
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
            blueprintCache.clear();
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

        for (final Map.Entry<BlockPos, RenderData> entry : blueprintCache.entrySet())
        {
            final RenderData buildingData = entry.getValue();
            if (buildingData == null) { continue; }

            final BlockPos position = entry.getKey();
            if (buildingData.blueprint != null && buildingData.blueprint.getBlueprint() != null)
            {
                BlueprintHandler.getInstance().draw(buildingData.blueprint, position, ctx.stageEvent);
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
        for (final Map.Entry<BlockPos, RenderData> entry : blueprintCache.entrySet())
        {
            final RenderData buildingData = entry.getValue();
            if (buildingData == null) { continue; }

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
        }

        ColonyWorldRenderMacros.endRenderLineBox(ctx.bufferSource);
    }

    private static void rebuildCache(final WorldEventContext ctx, final List<IRenderBlueprintRule> rules)
    {
        Collections.reverse(rules);   // so the first rule "wins"
        final Map<BlockPos, Supplier<PendingRenderData>> desired = new HashMap<>();
        for (final IRenderBlueprintRule rule : rules)
        {
            desired.putAll(rule.getDesiredBlueprints(ctx));
        }

        final Map<BlockPos, RenderData> newCache = new HashMap<>();
        for (final Map.Entry<BlockPos, Supplier<PendingRenderData>> entry : desired.entrySet())
        {
            if (blueprintCache.containsKey(entry.getKey()))
            {
                newCache.put(entry.getKey(), blueprintCache.get(entry.getKey()));
            }
            else
            {
                newCache.put(entry.getKey(), null);
                pendingBlueprints.add(entry.getValue().get());
            }
        }
        blueprintCache = newCache;
    }

    private static void processPendingBlueprints()
    {
        while (!pendingBlueprints.isEmpty())
        {
            final PendingRenderData data = pendingBlueprints.peek();
            if (data.blueprint() != null && !data.blueprint().isDone())
            {
                break;
            }
            try
            {
                pendingBlueprints.poll();
                if (data.blueprint() == null)
                {
                    if (data.boxOnly() && data.hasAnchor())     // render only the anchor
                    {
                        final BoxPreviewData box = new BoxPreviewData(INVALID_POS, INVALID_POS, Optional.of(data.pos()));
                        blueprintCache.put(data.pos(), new RenderData(null, box));
                    }
                    else
                    {
                        blueprintCache.remove(data.pos());
                    }
                    continue;
                }
                final Blueprint localBlueprint = data.blueprint().get();
                if (localBlueprint == null)
                {
                    blueprintCache.remove(data.pos());
                }
                else if (blueprintCache.containsKey(data.pos()))
                {
                    final BlueprintPreviewData blueprintPreviewData = new BlueprintPreviewData();
                    blueprintPreviewData.setBlueprint(localBlueprint);
                    blueprintPreviewData.setPos(data.pos());
                    blueprintPreviewData.rotate(data.settings().getRotation());
                    if (data.settings().getMirror() != Mirror.NONE)
                    {
                        blueprintPreviewData.mirror();
                    }

                    final BlockPos primaryOffset = localBlueprint.getPrimaryBlockOffset();
                    final BlockPos boxStartPos = data.pos().subtract(primaryOffset);
                    final BlockPos size = new BlockPos(localBlueprint.getSizeX(), localBlueprint.getSizeY(), localBlueprint.getSizeZ());
                    final BlockPos boxEndPos = boxStartPos.offset(size).offset(-1, -1, -1);
                    final BoxPreviewData box = new BoxPreviewData(boxStartPos, boxEndPos, data.hasAnchor() ? Optional.of(data.pos()) : Optional.empty());

                    localBlueprint.setRenderSource(data.pos());
                    if (data.boxOnly())
                    {
                        blueprintCache.put(data.pos(), new RenderData(null, box));
                    }
                    else
                    {
                        blueprintCache.put(data.pos(), new RenderData(blueprintPreviewData, box));
                    }

                    // only process one real blueprint per call
                    break;
                }
            }
            catch (final InterruptedException | ExecutionException ex)
            {
                //Noop
            }
        }
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
         * Report the complete list of locations that should render blueprints; if they're new then it will fetch the data.
         */
        Map<BlockPos, Supplier<PendingRenderData>> getDesiredBlueprints(WorldEventContext ctx);
    }

    /**
     * Holds blueprint renderdata (loaded).
     */
    private record RenderData(BlueprintPreviewData blueprint, BoxPreviewData box)
    {
    }

    /**
     * Holds blueprint renderdata (pending load).
     */
    private record PendingRenderData(Future<Blueprint> blueprint, BlockPos pos, PlacementSettings settings, boolean boxOnly, boolean hasAnchor)
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
        public Map<BlockPos, Supplier<PendingRenderData>> getDesiredBlueprints(final WorldEventContext ctx)
        {
            final Map<BlockPos, Supplier<PendingRenderData>> desired = new HashMap<>();
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
                        desired.put(currentPosition, () ->
                        {
                            String schemPath = buildingView.getStructurePath();
                            schemPath = schemPath.replace(".blueprint", "");
                            schemPath = schemPath.substring(0, schemPath.length() - 1) + buildingView.getBuildingMaxLevel() + ".blueprint";

                            final String structurePack = buildingView.getStructurePack();

                            final Future<Blueprint> localBlueprint = StructurePacks.getBlueprintFuture(structurePack, schemPath);
                            return new PendingRenderData(localBlueprint, currentPosition,
                                    new PlacementSettings(buildingView.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(buildingView.getRotation())),
                                    buildingView.getBuildingLevel() >= buildingView.getBuildingMaxLevel(),
                                    true);
                        });
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
        public Map<BlockPos, Supplier<PendingRenderData>> getDesiredBlueprints(final WorldEventContext ctx)
        {
            // ideally we'd check based on the bounding box, but we don't know that until we load the blueprints
            final double range = MathUtils.square(MinecoloniesAPIProxy.getInstance().getConfig().getClient().buildgogglerange.get());

            // show work orders
            final Map<BlockPos, Supplier<PendingRenderData>> desired = new HashMap<>();
            for (final IWorkOrderView workOrder : ctx.nearestColony.getWorkOrders())
            {
                if (workOrder.getLocation().distSqr(ctx.clientPlayer.blockPosition()) < range)
                {
                    desired.put(workOrder.getLocation(), () ->
                    {
                        final Future<Blueprint> localBlueprint = StructurePacks.getBlueprintFuture(workOrder.getPackName(), workOrder.getStructurePath());
                        return new PendingRenderData(localBlueprint, workOrder.getLocation(),
                                new PlacementSettings(workOrder.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE,
                                        BlockPosUtil.getRotationFromRotations(workOrder.getRotation())),
                                workOrder.getWorkOrderType() == WorkOrderType.REMOVE,
                                workOrder instanceof WorkOrderBuildingView);
                    });
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
                    desired.put(building.getPosition(), () ->
                            new PendingRenderData(null, building.getPosition(), new PlacementSettings(),
                                    true, true));
                }
            }

            return desired;
        }
    }
}
