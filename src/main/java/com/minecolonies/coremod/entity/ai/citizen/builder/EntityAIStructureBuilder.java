package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructure<JobBuilder>
{
    /**
     * How often should intelligence factor into the builders skill modifier.
     */
    private static final int    INTELLIGENCE_MULTIPLIER       = 2;

    /**
     * How often should strength factor into the builders skill modifier.
     */
    private static final int    STRENGTH_MULTIPLIER           = 1;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int    ACTIONS_UNTIL_DUMP            = 1024;



    /**
     * Position where the Builders constructs from.
     */
    @Nullable
    private              BlockPos workFrom       = null;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
          /**
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING),
          new AITarget(this::checkIfCanceled, IDLE),
          new AITarget(this::checkIfExecute, this::getState),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(BUILDER_REQUEST_MATERIALS, this::requestMaterials)
        );
        worker.setSkillModifier(INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                                  + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    private boolean checkIfCanceled()
    {
        final WorkOrderBuild wo = job.getWorkOrder();

        if (wo == null)
        {
            cancelTask();
            return true;
        }

        return false;
    }

    /**
     * Resets the builders current task.
     */
    public void cancelTask()
    {
        super.resetTask();
        job.setWorkOrder(null);
        workFrom = null;
        job.setStructure(null);
    }

    private boolean checkIfExecute()
    {
        setDelay(1);

        if (!job.hasWorkOrder())
        {
            return true;
        }

        final WorkOrderBuild wo = job.getWorkOrder();

        if (job.getColony().getBuilding(wo.getBuildingLocation()) == null && !(wo instanceof WorkOrderBuildDecoration))
        {
            job.complete();
            return true;
        }

        if (!job.hasStructure())
        {
            initiate();
        }

        return false;
    }

    private void initiate()
    {
        if (!job.hasStructure())
        {
            workFrom = null;
            loadStructure();

            final WorkOrderBuild wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                  String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)",
                    worker.getColony().getID(),
                    worker.getCitizenData().getId(), job.getWorkOrderId()));
                return;
            }

            if (wo instanceof WorkOrderBuildDecoration)
            {
                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                  "entity.builder.messageBuildStart",
                  job.getStructure().getName());
            }
            else
            {
                final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return;
                }

                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                  "entity.builder.messageBuildStart",
                  job.getStructure().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
        }
    }

    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return AIState.START_BUILDING;
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @param depth    the iteration depth.
     * @return returns BlockPos position with air above.
     */
    @Nullable
    private BlockPos getFloor(@NotNull BlockPos position, int depth)
    {
        if (depth > 50)
        {
            return null;
        }
        //If the position is floating in Air go downwards
        if (!EntityUtils.solidOrLiquid(world, position))
        {
            return getFloor(position.down(), depth + 1);
        }
        //If there is no air above the block go upwards
        if (!EntityUtils.solidOrLiquid(world, position.up()))
        {
            return position;
        }
        return getFloor(position.up(), depth + 1);
    }

    private AIState requestMaterials()
    {
        if (job.getStructure() == null)
        {
            //fix for bad structures
            job.complete();
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && !job.getWorkOrder().isRequested())
        {
            while (job.getStructure().findNextBlock())
            {
                if (job.getStructure().doesStructureBlockEqualWorldBlock())
                {
                    continue;
                }

                @Nullable final Block block = job.getStructure().getBlock();

                @NotNull final ItemStack itemstack = new ItemStack(block, 1);

                final Block worldBlock = BlockPosUtil.getBlock(world, job.getStructure().getBlockPosition());

                if (block != null
                      && block != Blocks.AIR
                      && worldBlock != Blocks.BEDROCK
                      && !(worldBlock instanceof AbstractBlockHut)
                      && !isBlockFree(block, 0))
                {
                    @NotNull final IBlockState blockState = job.getStructure().getBlockState();
                    if (blockState instanceof BlockBed && blockState.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.FOOT))
                    {
                        continue;
                    }
                    else if (blockState instanceof BlockDoor && blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
                    {
                        continue;
                    }

                    final AbstractBuilding building = getOwnBuilding();
                    if (building instanceof BuildingBuilder)
                    {
                        ((BuildingBuilder) building).addNeededResource(block, 1);
                    }
                }
            }
            job.getStructure().reset();
            incrementBlock();
        }

        job.getWorkOrder().setRequested(true);
        return AIState.BUILDER_STRUCTURE_STEP;
    }

    private boolean incrementBlock()
    {
        //method returns false if there is no next block (structures finished)
        return job.getStructure().incrementBlock();
    }



    //TODO handle resources
    private void spawnEntity(@Nullable final Entity entity)
    {
        if (entity != null)
        {
            final BlockPos pos = job.getStructure().getOffsetPosition();

            if (entity instanceof EntityHanging)
            {
                @NotNull final EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.posX += pos.getX();
                entityHanging.posY += pos.getY();
                entityHanging.posZ += pos.getZ();
                //also sets position based on tile
                entityHanging.setPosition(
                  entityHanging.getHangingPosition().getX(),
                  entityHanging.getHangingPosition().getY(),
                  entityHanging.getHangingPosition().getZ());

                entityHanging.setWorld(world);
                entityHanging.dimension = world.provider.getDimension();

                world.spawnEntityInWorld(entityHanging);
            }
            else if (entity instanceof EntityMinecart)
            {
                @Nullable final EntityMinecart minecart = (EntityMinecart) entity;
                minecart.posX += pos.getX();
                minecart.posY += pos.getY();
                minecart.posZ += pos.getZ();

                minecart.setWorld(world);
                minecart.dimension = world.provider.getDimension();

                world.spawnEntityInWorld(minecart);
            }
        }
    }



    private AIState findNextBlockSolid()
    {
        //method returns false if there is no next block (structures finished)
        if (!job.getStructure().findNextBlockSolid())
        {
            job.getStructure().reset();
            incrementBlock();
            return AIState.BUILDER_DECORATION_STEP;
        }
        return this.getState();
    }

    private AIState findNextBlockNonSolid()
    {
        //method returns false if there is no next block (structures finished)
        if (!job.getStructure().findNextBlockNonSolid())
        {
            job.getStructure().reset();
            incrementBlock();
            return AIState.BUILDER_COMPLETE_BUILD;
        }
        return this.getState();
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }

    /**
     * Can be overriden by implementations to specify which tools are useful for the worker.
     * When dumping he will keep these.
     *
     * @param stack the stack to decide on
     * @return if should be kept or not.
     */
    @Override
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }
}
