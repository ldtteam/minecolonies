package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MathUtils;
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
     * The cached map of blueprints that are rendered.
     */
    private static Map<BlockPos, RenderData> blueprintCache = new HashMap<>();

    private static BlockPos lastCacheRebuild = null;

    /**
     * Set of already requested structures.
     */
    public static Set<String> alreadyRequestedStructures = new HashSet<>();

    /**
     * Blueprints we're still loading.
     */
    private static Queue<PendingRenderData> pendingBlueprints = new LinkedList<>();

    /**
     * Rendering rules.  Order matters.
     */
    private static final List<IRenderBlueprintRule> renderRules = new ArrayList<>();

    static
    {
        renderRules.add(new NearBuildPreview());
    }

    /**
     * Renders blueprints into the client.
     *
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (!ctx.hasNearestColony())
        {
            blueprintCache.clear();
            alreadyRequestedStructures.clear();
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
            alreadyRequestedStructures.clear();
            lastCacheRebuild = null;
            return;
        }

        if (lastCacheRebuild == null || Minecraft.getInstance().level.getGameTime() % 20 == 0)
        {
            final BlockPos activePosition = ctx.clientPlayer.blockPosition();
            rebuildCache(ctx, activeRules);
            lastCacheRebuild = activePosition;
        }

        for (final Map.Entry<BlockPos, RenderData> entry : blueprintCache.entrySet())
        {
            final RenderData buildingData = entry.getValue();
            if (buildingData == null) { continue; }

            final BlockPos position = entry.getKey();
            if (buildingData.blueprint != null && buildingData.blueprint.getBlueprint() != null)
            {
                StructureClientHandler.renderStructureAtPos(buildingData.blueprint, ctx.partialTicks, position, ctx.poseStack);
            }

            WorldRenderMacros.renderLineBox(ctx.bufferSource.getBuffer(WorldRenderMacros.LINES_WITH_WIDTH),
                    ctx.poseStack,
                    buildingData.boxStartPos,
                    buildingData.boxEndPos,
                    0,
                    0,
                    255,
                    255,
                    0.08f);
        }
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
                    newCache.remove(data.pos());
                    continue;
                }
                final Blueprint localBlueprint = data.blueprint().get();
                if (localBlueprint == null)
                {
                    newCache.remove(data.pos());
                }
                else if (newCache.containsKey(data.pos()))
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
                    final BlockPos boxEndPos = boxStartPos.offset(size).subtract(new BlockPos(1, 1, 1));

                    localBlueprint.setRenderSource(data.pos());
                    if (data.boxOnly())
                    {
                        newCache.put(data.pos(), new RenderData(null, boxStartPos, boxEndPos));
                    }
                    else
                    {
                        newCache.put(data.pos(), new RenderData(blueprintPreviewData, boxStartPos, boxEndPos));
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

        blueprintCache = newCache;
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
    private record RenderData(BlueprintPreviewData blueprint, BlockPos boxStartPos, BlockPos boxEndPos)
    {
    }

    /**
     * Holds blueprint renderdata (pending load).
     */
    private record PendingRenderData(Future<Blueprint> blueprint, BlockPos pos, PlacementSettings settings, boolean boxOnly)
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
                if (buildingView.getBuildingType() == ModBuildings.postBox.get() || buildingView.getBuildingType() == ModBuildings.stash.get())
                {
                    continue;
                }
                final BlockPos currentPosition = buildingView.getPosition();

                final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) ctx.clientLevel.getBlockEntity(buildingView.getPosition());
                if (tileEntityColonyBuilding != null)
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
                                    buildingView.getBuildingLevel() >= buildingView.getBuildingMaxLevel());
                        });
                    }
                }
            }

            return desired;
        }
    }
}
