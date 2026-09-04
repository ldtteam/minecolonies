package com.minecolonies.core.gametest;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.workorders.WorkOrderBuilding;
import com.minecolonies.core.entity.ai.workers.builder.EntityAIStructureBuilder;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.List;

/**
 * End-to-end server-side checks for the core colony lifecycle.
 */
public final class MinecoloniesGameTests
{
    private MinecoloniesGameTests()
    {
    }

    public static void colonyLifecycle(final GameTestHelper helper)
    {
        final ServerLevel level = helper.getLevel();
        // A server-only lifecycle test needs a player object but no client connection.
        final ServerPlayer player = FakePlayerFactory.getMinecraft(level);
        final BlockPos relativeTownHallPos = new BlockPos(1, 1, 1);
        final BlockPos townHallPos = helper.absolutePos(relativeTownHallPos);

        // The empty test environment has no terrain. Give the citizen spawn
        // search a small, deterministic walkable platform beside the hall.
        for (int x = -8; x < 40; x++)
        {
            for (int z = -8; z < 40; z++)
            {
                helper.setBlock(new BlockPos(x, -1, z), Blocks.STONE.defaultBlockState());
            }
        }
        helper.setBlock(relativeTownHallPos, ModBlocks.blockHutTownHall.defaultBlockState());
        helper.assertBlockPresent(ModBlocks.blockHutTownHall, relativeTownHallPos);

        final BlockEntity blockEntity = level.getBlockEntity(townHallPos);
        helper.assertTrue(blockEntity instanceof TileEntityColonyBuilding,
          "town hall placement did not create a MineColonies building block entity");
        final TileEntityColonyBuilding townHall = (TileEntityColonyBuilding) blockEntity;
        townHall.setPackName("Minecolonies Original");
        townHall.setBlueprintPath("fundamentals/townhall1.blueprint");

        final IColony colony = IColonyManager.getInstance().createColony(
          level,
          townHallPos,
          player,
          "MC26.2 GameTest Colony",
          "Minecolonies Original");
        helper.assertTrue(colony != null, "colony creation returned null");

        final IBuilding building = colony.getServerBuildingManager().addNewBuilding(
          townHall,
          level);
        helper.assertTrue(building != null, "town hall building registration returned null");
        helper.assertTrue(colony.getServerBuildingManager().hasTownHall(),
          "registered colony does not expose a town hall");

        final BlockPos relativeSpawnAnchor = new BlockPos(3, 1, 3);
        final BlockPos spawnAnchor = helper.absolutePos(relativeSpawnAnchor);
        level.setChunkForced(townHallPos.getX() >> 4, townHallPos.getZ() >> 4, true);
        level.setChunkForced(spawnAnchor.getX() >> 4, spawnAnchor.getZ() >> 4, true);
        // GameTest structures receive their entity-ticking chunk tickets at
        // the end of the setup tick. Continue after that boundary so a fresh
        // universe cannot race the citizen manager's loaded-chunk guard.
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(WorldUtil.isEntityBlockLoaded(level, townHallPos),
              "town hall chunk is not entity-ticking: " + townHallPos);
            helper.assertTrue(WorldUtil.isEntityBlockLoaded(level, spawnAnchor),
              "spawn anchor chunk is not entity-ticking: " + spawnAnchor);
            helper.assertTrue(level.getBlockState(spawnAnchor).isAir(),
              "spawn anchor occupied: " + level.getBlockState(spawnAnchor));
            helper.assertTrue(level.getBlockState(spawnAnchor.above()).isAir(),
              "spawn anchor head occupied: " + level.getBlockState(spawnAnchor.above()));
            helper.assertTrue(!level.getBlockState(spawnAnchor.below()).isAir()
                || !level.getBlockState(spawnAnchor.below(2)).isAir(),
              "spawn anchor has no supporting platform: below=" + level.getBlockState(spawnAnchor.below())
                + ", below2=" + level.getBlockState(spawnAnchor.below(2)));
            helper.assertTrue(EntityUtils.getSpawnPoint(level, spawnAnchor) != null,
              "spawn-point search failed at anchor " + spawnAnchor);
            final ICitizenData citizen = colony.getCitizenManager().spawnOrCreateCivilian(
              null,
              level,
              List.of(townHallPos, spawnAnchor),
              true);
            helper.assertTrue(citizen != null, "citizen creation returned null");
            helper.assertTrue(colony.getCitizenManager().getCitizens().contains(citizen),
              "created citizen was not registered with the colony");

        // Place a second hut through the same block/entity path used by a
        // player, register it with the colony, and assign the live citizen to
        // its worker module. This exercises building placement and the job
        // assignment/network-facing colony state without relying on a client.
        final BlockPos relativeBuilderPos = new BlockPos(6, 1, 1);
        final BlockPos builderPos = helper.absolutePos(relativeBuilderPos);
        level.setChunkForced(builderPos.getX() >> 4, builderPos.getZ() >> 4, true);
        helper.assertTrue(WorldUtil.isBlockLoaded(level, builderPos),
          "builder chunk is not fully loaded: " + builderPos);
        helper.setBlock(relativeBuilderPos, ModBlocks.blockHutBuilder.defaultBlockState());
        helper.assertBlockPresent(ModBlocks.blockHutBuilder, relativeBuilderPos);
        final BlockEntity builderBlockEntity = level.getBlockEntity(builderPos);
        helper.assertTrue(builderBlockEntity instanceof TileEntityColonyBuilding,
          "builder placement did not create a MineColonies building block entity");
        final TileEntityColonyBuilding builderHut = (TileEntityColonyBuilding) builderBlockEntity;
        builderHut.setPackName("Minecolonies Original");
        builderHut.setBlueprintPath("fundamentals/builder1.blueprint");
        final IBuilding builder = colony.getServerBuildingManager().addNewBuilding(builderHut, level);
        helper.assertTrue(builder instanceof BuildingBuilder,
          "builder hut did not create a worker building: " + (builder == null ? "null" : builder.getClass().getName()));
        final BuildingBuilder builderBuilding = (BuildingBuilder) builder;
        helper.assertTrue(builderBuilding.getModule(BuildingModules.BUILDER_WORK).assignCitizen(citizen),
          "builder worker could not be assigned to the spawned citizen");
        helper.assertTrue(citizen.getWorkBuilding() == builder,
          "citizen work building was not synchronized after assignment");
        // The real builder AI must clear the pre-existing platform before it
        // places the schematic. Give it the same starter tool a level-zero
        // builder can use; materials themselves remain supplied by the test
        // resource policy below.
        citizen.getInventory().setStackInSlot(0, new ItemStack(Items.WOODEN_PICKAXE));
        citizen.getInventory().setStackInSlot(1, new ItemStack(Items.WOODEN_AXE));
        citizen.getInventory().setStackInSlot(2, new ItemStack(Items.WOODEN_SHOVEL));
        citizen.getInventory().setStackInSlot(3, new ItemStack(Items.WOODEN_HOE));
        citizen.getInventory().setStackInSlot(4, new ItemStack(Items.SHEARS));

        final WorkOrderBuilding workOrder = WorkOrderBuilding.create(WorkOrderType.BUILD, builder);
        colony.getWorkManager().addWorkOrder(workOrder, false);
        helper.assertTrue(workOrder.getID() > 0, "building work order did not receive an id");
        helper.assertTrue(colony.getWorkManager().getWorkOrder(workOrder.getID()) == workOrder,
          "building work order was not registered");
        workOrder.setClaimedBy(builderBuilding.getID());
        builderBuilding.setWorkOrder(workOrder);
        final boolean previousInfiniteBuilderResources = Constants.BUILDER_INF_RESOURECES;
        Constants.BUILDER_INF_RESOURECES = true;
        workOrder.setRequested(true);
        workOrder.loadBlueprint(level, ignored -> { });

        helper.assertTrue(citizen.getEntity().isPresent(), "spawned citizen has no live entity");
        helper.assertTrue(citizen.getEntity().get().getCitizenJobHandler().getWorkAI() != null,
          "assigned builder citizen has no worker AI");

        // Exercise the same NBT round-trip used by the persistent colony
        // manager.  This catches registry/component schema regressions that a
        // live in-memory colony would otherwise hide.
        final CompoundTag saved = colony.write(new CompoundTag(), level.registryAccess());
        final Colony restored = Colony.loadColony(saved, level, level.registryAccess());
        helper.assertTrue(restored != null, "colony NBT could not be loaded");
        helper.assertTrue(restored.getID() == colony.getID(), "colony id was not preserved across reload");
        helper.assertTrue(restored.getCenter().equals(colony.getCenter()), "colony center was not preserved across reload");
        helper.assertTrue(restored.getName().equals(colony.getName()), "colony name was not preserved across reload");
        helper.assertTrue(restored.getServerBuildingManager().hasTownHall(), "town hall was not preserved across reload");
        helper.assertTrue(restored.getCitizenManager().getCitizens().size() == colony.getCitizenManager().getCitizens().size(),
          "citizens were not preserved across reload");
        helper.assertTrue(restored.getServerBuildingManager().getBuildings().size() == colony.getServerBuildingManager().getBuildings().size(),
          "buildings were not preserved across reload");
        helper.assertTrue(restored.getWorkManager().getWorkOrders().size() == colony.getWorkManager().getWorkOrders().size(),
          "work orders were not preserved across reload");

        // Blueprint loading is queued by Structurize. Wait for it before
        // completing the test so the construction bounding box is proven to
        // be available, not merely that an order object was allocated.
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(workOrder.getBlueprint() != null, "builder work order blueprint did not load");
            helper.assertTrue(!workOrder.getBoundingBox().equals(com.minecolonies.api.util.constant.Constants.EMPTY_AABB),
              "builder work order did not calculate a construction area");
            helper.assertTrue(builderBuilding.getModule(BuildingModules.BUILDER_WORK).hasAssignedCitizen(),
              "builder worker assignment was lost before construction started");
            final Blueprint loadedBuilderBlueprint = workOrder.getBlueprint();
            helper.assertTrue(loadedBuilderBlueprint.getPrimaryBlockOffset() != null,
              "loaded builder schematic did not expose an anchor");
            final EntityAIStructureBuilder workerAI = (EntityAIStructureBuilder) citizen.getEntity().get().getCitizenJobHandler().getWorkAI();
            citizen.getEntity().ifPresent(entity -> {
                // Keep the synthetic worker on the flat test pad while the
                // production pathfinder evaluates the construction site.
                entity.setPos(builderBuilding.getID().getX() + 0.5D,
                  builderBuilding.getID().getY(),
                  builderBuilding.getID().getZ() + 0.5D);
            });
            // Let any earlier state-machine load attempt settle, then invoke
            // the production loader against the completed blueprint and
            // re-arm the request on the following server tick.
            helper.runAfterDelay(100, () -> {
                workerAI.loadStructure(workOrder, builderPos, false);
                helper.runAfterDelay(1, () -> workOrder.setRequested(true));
            });
        });
            helper.runAfterDelay(40000, () -> {
            try
            {
                final String workerState = citizen.getEntity()
                  .map(entity -> entity.getCitizenJobHandler().getWorkAI() == null
                    ? "no-worker-ai"
                    : entity.getCitizenJobHandler().getWorkAI().getStateAI().getState().toString())
                  .orElse("no-entity");
                final String workerHistory = citizen.getEntity()
                  .map(entity -> entity.getCitizenJobHandler().getWorkAI() == null
                    ? "no-worker-ai"
                    : entity.getCitizenJobHandler().getWorkAI().getStateAI().getHistory().getString())
                  .orElse("no-entity");
                final BlockPos progressLocal = builderBuilding.getProgress() == null ? null : builderBuilding.getProgress().getA();
                final BlockPos progressWorld = progressLocal == null || workOrder.getBlueprint() == null
                  ? null
                  : builderBuilding.getID().subtract(workOrder.getBlueprint().getPrimaryBlockOffset()).offset(progressLocal);
                final String progressState = progressWorld == null ? "null" : level.getBlockState(progressWorld).toString();
                final String heldItem = citizen.getEntity().map(entity -> entity.getMainHandItem().toString()).orElse("no-entity");
                helper.assertTrue(builderBuilding.getBuildingLevel() >= 1,
                  "builder worker did not complete the level-one construction; level=" + builderBuilding.getBuildingLevel()
                    + ", ai=" + workerState + ", workOrder=" + builderBuilding.getWorkOrder()
                    + ", requested=" + workOrder.isRequested() + ", claimedBy=" + workOrder.getClaimedBy()
                    + ", blueprint=" + (workOrder.getBlueprint() == null ? "null" : workOrder.getBlueprint().getFileName())
                    + ", primary=" + (workOrder.getBlueprint() == null ? "null" : workOrder.getBlueprint().getPrimaryBlockOffset())
                    + ", size=" + (workOrder.getBlueprint() == null ? "null" : workOrder.getBlueprint().getSizeX() + "x" + workOrder.getBlueprint().getSizeY() + "x" + workOrder.getBlueprint().getSizeZ())
                    + ", bbox=" + workOrder.getBoundingBox()
                    + ", progress=" + builderBuilding.getProgress()
                    + ", progressWorld=" + progressWorld + ", progressState=" + progressState + ", held=" + heldItem
                    + ", history=" + workerHistory
                    + ", citizenPos=" + citizen.getEntity().map(entity -> entity.blockPosition()).orElse(null));
                helper.assertTrue(!builderBuilding.hasWorkOrder(),
                  "completed construction still has a builder work order");
                helper.assertTrue(colony.getWorkManager().getWorkOrder(workOrder.getID()) == null,
                  "completed construction work order was not removed");
                helper.succeed();
            }
            finally
            {
                Constants.BUILDER_INF_RESOURECES = previousInfiniteBuilderResources;
            }
            });
        });
    }
}
