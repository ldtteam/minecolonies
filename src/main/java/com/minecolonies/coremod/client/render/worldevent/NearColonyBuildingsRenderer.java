package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SchematicRequestMessage;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.IAltersBuildingFootprint;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NearColonyBuildingsRenderer
{
    /**
     * The distance in which previews of nearby buildings are rendered
     */
    private static final double PREVIEW_RANGE = 25.0f;

    /**
     * The active blueprint pos distance shift needed for cache reset
     */
    private static final double CACHE_RESET_RANGE = PREVIEW_RANGE / 2.0F;

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
        if (lastCacheRebuild == null || !lastCacheRebuild.closerThan(activePosition, CACHE_RESET_RANGE))
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

        for (final IBuildingView buildingView : ctx.nearestColony.getBuildings())
        {
            if (buildingView.getBuildingType() == ModBuildings.postBox || buildingView.getBuildingType() == ModBuildings.stash)
            {
                continue;
            }

            final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) ctx.clientLevel.getBlockEntity(buildingView.getPosition());
            if (tileEntityColonyBuilding != null)
            {
                final Tuple<BlockPos, BlockPos> corners = tileEntityColonyBuilding.getInWorldCorners();
                BlockPos cornerA = corners.getA();
                BlockPos cornerB = corners.getB();

                if (activePosition.getX() >= cornerA.getX() - 1 && activePosition.getX() <= cornerB.getX() + 1
                      && activePosition.getY() >= cornerA.getY() - 1 && activePosition.getY() <= cornerB.getY() + 1
                      && activePosition.getZ() >= cornerA.getZ() - 1 && activePosition.getZ() <= cornerB.getZ() + 1)
                {
                    final BlockPos currentPosition = buildingView.getPosition();
                    if (blueprintCache.containsKey(currentPosition))
                    {
                        newCache.put(currentPosition, blueprintCache.get(currentPosition));
                        continue;
                    }

                    final BlockEntity tile = ctx.clientLevel.getBlockEntity(buildingView.getID());
                    String schematicName = buildingView.getSchematicName();
                    if (tile instanceof IBlueprintDataProvider)
                    {
                        if (!((IBlueprintDataProvider) tile).getSchematicName().isEmpty())
                        {
                            schematicName = ((IBlueprintDataProvider) tile).getSchematicName().replaceAll("\\d$", "");
                        }
                    }

                    final StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX,
                      buildingView.getStyle(),
                      schematicName + buildingView.getBuildingMaxLevel());

                    final String structureName = sn.toString();
                    final String md5 = Structures.getMD5(structureName);

                    final IStructureHandler wrapper = new LoadOnlyStructureHandler(ctx.clientLevel,
                      buildingView.getID(),
                      structureName,
                      new PlacementSettings(),
                      true);
                    if (!wrapper.hasBluePrint() || !wrapper.isCorrectMD5(md5))
                    {
                        if (alreadyRequestedStructures.contains(structureName))
                        {
                            continue;
                        }
                        alreadyRequestedStructures.add(structureName);

                        Log.getLogger().error("Couldn't find schematic: " + structureName + " requesting to server if possible.");
                        if (ServerLifecycleHooks.getCurrentServer() == null)
                        {
                            Network.getNetwork().sendToServer(new SchematicRequestMessage(structureName));
                        }
                        continue;
                    }

                    final Blueprint blueprint = wrapper.getBluePrint();
                    final Mirror mirror = buildingView.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE;
                    blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(buildingView.getRotation()), mirror, ctx.clientLevel);

                    final BlockPos primaryOffset = blueprint.getPrimaryBlockOffset();
                    final BlockPos boxStartPos = currentPosition.subtract(primaryOffset);
                    final BlockPos size = new BlockPos(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());
                    final BlockPos boxEndPos = boxStartPos.offset(size).subtract(new BlockPos(1, 1, 1));
                    blueprint.setRenderSource(buildingView.getID());

                    if (buildingView.getBuildingLevel() < buildingView.getBuildingMaxLevel())
                    {
                        newCache.put(currentPosition, new RenderData(blueprint, boxStartPos, boxEndPos));
                    }
                    else
                    {
                        newCache.put(currentPosition, new RenderData(null, boxStartPos, boxEndPos));
                    }
                }
            }
        }

        blueprintCache = newCache;
    }

    private static record RenderData(Blueprint blueprint, BlockPos boxStartPos, BlockPos boxEndPos)
    {
    }
}
