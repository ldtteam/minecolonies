package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class NearColonyBuildingsRenderer
{
    /**
     * The distance in which previews of nearby buildings are rendered
     */
    private static final double PREVIEW_RANGE = 10.0f;

    /**
     * The cached map of blueprints of nearby buildings that are rendered.
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
     * Renders building bounding boxes into the client.
     * 
     * @param ctx rendering context
     */
    static void render(final WorldEventContext ctx)
    {
        if (RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() == null || !ctx.hasNearestColony()
            || !MinecoloniesAPIProxy.getInstance().getConfig().getClient().neighborbuildingrendering.get())
        {
            blueprintCache.clear();
            lastCacheRebuild = null;
            return;
        }

        if (ctx.mainHandItem.getItem() != ModItems.buildTool.get())
        {
            alreadyRequestedStructures.clear();
            return;
        }

        final BlockPos activePosition = RenderingCache.getOrCreateBlueprintPreviewData("blueprint").pos;
        if (lastCacheRebuild == null || Minecraft.getInstance().level.getGameTime() % 20 == 0)
        {
            rebuildCache(ctx);
            lastCacheRebuild = activePosition;
        }

        for (final Map.Entry<BlockPos, RenderData> nearbyBuilding : blueprintCache.entrySet())
        {
            final RenderData buildingData = nearbyBuilding.getValue();
            final BlockPos position = nearbyBuilding.getKey();
            if (buildingData.blueprint != null)
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

    private static void rebuildCache(final WorldEventContext ctx)
    {
        final BlockPos activePosition = RenderingCache.getOrCreateBlueprintPreviewData("blueprint").pos;
        final Map<BlockPos, RenderData> newCache = new HashMap<>();
        final Blueprint blueprint = RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint();
        final BlockPos zeroPos = activePosition.subtract(blueprint.getPrimaryBlockOffset());
        final AABB blueprintAABB = new AABB(zeroPos.offset(-1, -1, -1), zeroPos.offset(blueprint.getSizeX() , blueprint.getSizeY() , blueprint.getSizeZ() ));

        for (final IBuildingView buildingView : ctx.nearestColony.getBuildings())
        {
            if (buildingView.getBuildingType() == ModBuildings.postBox || buildingView.getBuildingType() == ModBuildings.stash)
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
                    if (blueprintCache.containsKey(currentPosition))
                    {
                        newCache.put(currentPosition, blueprintCache.get(currentPosition));
                        continue;
                    }

                    String schemPath = buildingView.getStructurePath();
                    schemPath = schemPath.replace(".blueprint", "");
                    schemPath = schemPath.substring(0, schemPath.length() - 1) + buildingView.getBuildingMaxLevel() + ".blueprint";

                    final String structurePack = buildingView.getStructurePack();

                    final Future<Blueprint> localBlueprint = StructurePacks.getBlueprintFuture(structurePack, schemPath);
                    pendingBlueprints.add(new PendingRenderData(localBlueprint, currentPosition,
                      new PlacementSettings(buildingView.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(buildingView.getRotation())),
                        buildingView.getBuildingLevel() >= buildingView.getBuildingMaxLevel()));

                }
                else if (blueprintCache.containsKey(currentPosition))
                {
                    blueprintCache.remove(currentPosition);
                }
            }
        }

        if (pendingBlueprints.isEmpty())
        {
            return;
        }

        final PendingRenderData data = pendingBlueprints.peek();
        if (data.blueprint() != null && data.blueprint.isDone())
        {
            try
            {
                pendingBlueprints.poll();
                final Blueprint localBlueprint = data.blueprint().get();
                if (localBlueprint == null)
                {
                    return;
                }
                localBlueprint.rotateWithMirror(data.settings().getRotation(), data.settings.getMirror(), ctx.clientLevel);

                final BlockPos primaryOffset = localBlueprint.getPrimaryBlockOffset();
                final BlockPos boxStartPos = data.pos.subtract(primaryOffset);
                final BlockPos size = new BlockPos(localBlueprint.getSizeX(), localBlueprint.getSizeY(), localBlueprint.getSizeZ());
                final BlockPos boxEndPos = boxStartPos.offset(size).subtract(new BlockPos(1, 1, 1));
                localBlueprint.setRenderSource(data.pos);

                if (data.isMax)
                {
                    newCache.put(null, new RenderData(null, boxStartPos, boxEndPos));
                }
                else
                {
                    newCache.put(data.pos, new RenderData(localBlueprint, boxStartPos, boxEndPos));
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
     * Holds blueprint renderdata.
     */
    private record RenderData(Blueprint blueprint, BlockPos boxStartPos, BlockPos boxEndPos)
    {

    }

    /**
     * Holds blueprint renderdata.
     */
    private record PendingRenderData(Future<Blueprint> blueprint, BlockPos pos, PlacementSettings settings, boolean isMax)
    {

    }
}
