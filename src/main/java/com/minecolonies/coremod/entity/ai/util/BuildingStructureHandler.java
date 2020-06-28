package com.minecolonies.coremod.entity.ai.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.AbstractStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.RUN_AWAY_SPEED;

/**
 * Represents a build task for the StructureIterator AI.
 * <p>
 * It internally uses a structure it transparently loads.
 */
public class BuildingStructureHandler<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> extends AbstractStructureHandler
{
    /**
     * Amount of xp the builder gains for placing a block.
     */
    private static final double XP_EACH_BLOCK = 0.1D;

    /**
     * The structure AI handling this task.
     */
    private final AbstractEntityAIStructure<J, B> structureAI;

    /**
     * The total number of stages.
     */
    private final Stage[] stages;

    /**
     * The building associated with this placement.
     */
    private IBuilding building;

    /**
     * The current structure stage.
     */
    private int stage;

    /**
     * The minecolonies AI specific creative structure placer.
     * @param world the world.
     * @param worldPos the pos it is placed at.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     * @param entityAIStructure the AI handling this structure.
     */
    public BuildingStructureHandler(final World world, final BlockPos worldPos, final String structureName, final PlacementSettings settings, final AbstractEntityAIStructure<J, B> entityAIStructure, final Stage[] stages)
    {
        super(world, worldPos, structureName, settings);
        setupBuilding();
        this.structureAI= entityAIStructure;
        this.stages = stages;
        this.stage = 0;
    }

    /**
     * The minecolonies AI specific creative structure placer.
     * @param world the world.
     * @param worldPos the pos it is placed at.
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     * @param entityAIStructure the AI handling this structure.
     */
    public BuildingStructureHandler(final World world, final BlockPos worldPos, final Blueprint blueprint, final PlacementSettings settings, final AbstractEntityAIStructure<J, B> entityAIStructure, final Stage[] stages)
    {
        super(world, worldPos, blueprint, settings);
        setupBuilding();
        this.structureAI= entityAIStructure;
        this.stages = stages;
        this.stage = 0;
    }

    /**
     * Setup the building to register things to.
     */
    private void setupBuilding()
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(getWorld(), getWorldPos());
        if (colony != null)
        {
            this.building = colony.getBuildingManager().getBuilding(getWorldPos());
        }
    }

    /**
     * Get the current stage we're in.
     *
     * @return the current Stage.
     */
    @Nullable
    public Stage getStage()
    {
        if (this.stage >= stages.length)
        {
            return null;
        }
        return stages[stage];
    }

    /**
     * Go to the next stage.
     */
    public boolean nextStage()
    {
        return ++this.stage < stages.length;
    }

    /**
     * Set the current stage from memory.
     * @param stage the stage to set.
     */
    public void setStage(final Stage stage)
    {
        for (int i = 0; i < stages.length; i++)
        {
            if (stages[i] == stage)
            {
                this.stage = i;
                return;
            }
        }
    }

    @Override
    public void prePlacementLogic(final BlockPos worldPos, final BlockState blockState)
    {
        WorkerUtil.faceBlock(worldPos, structureAI.getWorker());
        //Move out of the way when placing blocks
        final ItemStack item = BlockUtils.getItemStackFromBlockState(blockState);
        structureAI.getWorker().setItemStackToSlot(EquipmentSlotType.MAINHAND, item == null ? ItemStackUtils.EMPTY : item);

        if (MathHelper.floor(structureAI.getWorker().getPosX()) == worldPos.getX()
              && MathHelper.abs(worldPos.getY() - (int) structureAI.getWorker().getPosY()) <= 1
              && MathHelper.floor(structureAI.getWorker().getPosZ()) == worldPos.getZ()
              && structureAI.getWorker().getNavigator().noPath())
        {
            structureAI.getWorker().getNavigator().moveAwayFromXYZ(worldPos, RUN_AWAY_SPEED, 1);
        }

        structureAI.getWorker().swingArm(Hand.MAIN_HAND);
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return structureAI.getWorker().getInventoryCitizen();
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> list, final boolean placement)
    {
        final BlockState state = getBluePrint().getBlockState(pos);
        final BlockPos worldPos = getProgressPosInWorld(pos);
        if (building != null)
        {
            building.registerBlockPosition(getBluePrint().getBlockState(pos), worldPos, this.getWorld());
        }

        if (placement)
        {
            structureAI.getWorker().getCitizenExperienceHandler().addExperience(XP_EACH_BLOCK);

            for (final ItemStack stack : list)
            {
                structureAI.reduceNeededResources(stack);
            }
        }

        if (state.getBlock() == ModBlocks.blockWayPoint)
        {
            structureAI.getWorker().getCitizenColonyHandler().getColony().addWayPoint(worldPos, state);
        }
    }

    @Override
    public boolean hasRequiredItems(@NotNull final List<ItemStack> requiredItems)
    {
        final List<ItemStack> itemList = new ArrayList<>();
        for (final ItemStack stack : requiredItems)
        {
            for (final ToolType toolType : ToolType.values())
            {
                if (ItemStackUtils.isTool(stack, toolType))
                {
                    if (structureAI.checkForToolOrWeapon(toolType))
                    {
                        return false;
                    }
                }
            }
            itemList.add(structureAI.getTotalAmount(stack));
        }

        return AbstractEntityAIStructure.hasListOfResInInvOrRequest(structureAI, itemList, itemList.size() > 1);
    }

    @Override
    public boolean isCreative()
    {
        return MineColonies.getConfig().getCommon().builderInfiniteResources.get();
    }

    @Override
    public int getStepsPerCall()
    {
        return 1;
    }

    @Override
    public int getMaxBlocksCheckedPerCall()
    {
        return 250;
    }

    @Override
    public boolean isStackFree(@Nullable final ItemStack itemStack)
    {
        return itemStack == null
                 ||itemStack.isEmpty()
                 || itemStack.getItem().isIn(ItemTags.LEAVES)
                 || itemStack.getItem() == new ItemStack(ModBlocks.blockDecorationPlaceholder, 1).getItem();
    }

    @Override
    public boolean allowReplace()
    {
        return getStage() != null && getStage() != Stage.CLEAR;
    }

    @Override
    public ItemStack getHeldItem()
    {
        return structureAI.getWorker().getHeldItemMainhand();
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos blockPos)
    {
        return structureAI.getSolidSubstitution(blockPos);
    }

    @Override
    public boolean replaceWithSolidBlock(final BlockState blockState)
    {
        return !blockState.getMaterial().isSolid() || structureAI.shallReplaceSolidSubstitutionBlock(blockState.getBlock(), blockState);
    }

    @Override
    public boolean fancyPlacement()
    {
        return true;
    }

    @Override
    public boolean shouldBlocksBeConsideredEqual(final BlockState state1, final BlockState state2)
    {
        final Block block1 = state1.getBlock();
        final Block block2 = state2.getBlock();

        if (block1 == Blocks.FLOWER_POT || block2 == Blocks.FLOWER_POT)
        {
            return block1 == block2;
        }

        if ((state1.getBlock() == com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution && state2.getMaterial().isSolid())
              || (state2.getBlock() == com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution && state1.getMaterial().isSolid()))
        {
            return true;
        }

        return block1 == Blocks.GRASS_BLOCK && block2 == Blocks.DIRT || block2 == Blocks.GRASS_BLOCK && block1 == Blocks.DIRT;
    }

    /**
     * The different stages a StructureIterator building process can be in.
     */
    public enum Stage
    {
        CLEAR,
        BUILD_SOLID,
        CLEAR_WATER,
        DECORATE,
        SPAWN,
        REMOVE
    }
}
