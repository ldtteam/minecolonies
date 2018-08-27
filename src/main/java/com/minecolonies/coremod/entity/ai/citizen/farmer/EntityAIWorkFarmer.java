package com.minecolonies.coremod.entity.ai.citizen.farmer;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType; 
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.blocks.huts.BlockHutField;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Farmer AI class.
 * Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAIInteract<JobFarmer>
{
    /**
     * The standard delay the farmer should have.
     */
    private static final int STANDARD_DELAY = 40;

    /**
     * The smallest delay the farmer should have.
     */
    private static final int SMALLEST_DELAY = 1;

    /**
     * The bonus the farmer gains each update is level/divider.
     */
    private static final double DELAY_DIVIDER = 1;

    /**
     * The EXP Earned per harvest.
     */
    private static final double XP_PER_HARVEST = 0.5;

    /**
     * Changed after finished harvesting in order to dump the inventory.
     */
    private boolean shouldDumpInventory = false;

    /**
     * The offset to work at relative to the scarecrow.
     */
    @Nullable
    private BlockPos workingOffset;

    /**
     * The previous position which has been worked at.
     */
    @Nullable
    private BlockPos prevPos;

    /**
     * Variables used in handleOffset.
     */
    private int     totalDis;
    private int     dist;
    private boolean horizontal;

    /**
     * Constructor for the Farmer.
     * Defines the tasks the Farmer executes.
     *
     * @param job a farmer job to use.
     */
    public EntityAIWorkFarmer(@NotNull final JobFarmer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForFarming),
          new AITarget(FARMER_HOE, this::workAtField),
          new AITarget(FARMER_PLANT, this::workAtField),
          new AITarget(FARMER_HARVEST, this::workAtField)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingFarmer.class;
    }

    /**
     * Redirects the farmer to his building.
     *
     * @return the next state.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the farmer for farming.
     * Also requests the tools and checks if the farmer has sufficient fields.
     *
     * @return the next AIState
     */
    @NotNull
    private AIState prepareForFarming()
    {
        @Nullable final BuildingFarmer building = getWorkBuilding();
        if (building == null || building.getBuildingLevel() < 1)
        {
            return PREPARING;
        }

        building.syncWithColony(world);
        if (building.getFarmerFields().size() < getWorkBuilding().getBuildingLevel() && !building.assignManually())
        {
            searchAndAddFields();
        }

        if (building.hasNoFields())
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noFreeFields");
            worker.getCitizenData().getCitizenHappinessHandler().setNoFieldsToFarm(); 
            return PREPARING;
        }

        //If the farmer has no currentField and there is no field which needs work, check fields.
        if (building.getCurrentField() == null && building.getFieldToWorkOn(world) == null)
        {
            building.resetFields();
            return IDLE;
        }

        @Nullable final BlockPos currentField = building.getCurrentField();
        final TileEntity entity = world.getTileEntity(currentField);
        if (entity instanceof ScarecrowTileEntity && ((ScarecrowTileEntity) entity).needsWork())
        {
            if (((ScarecrowTileEntity) entity).getFieldStage() == ScarecrowTileEntity.FieldStage.PLANTED && checkIfShouldExecute((ScarecrowTileEntity) entity, this::shouldHarvest))
            {
                return FARMER_HARVEST;
            }
            else if (((ScarecrowTileEntity) entity).getFieldStage() == ScarecrowTileEntity.FieldStage.HOED)
            {
                return canGoPlanting((ScarecrowTileEntity) entity, building);
            }
            else if (((ScarecrowTileEntity) entity).getFieldStage() == ScarecrowTileEntity.FieldStage.EMPTY && checkIfShouldExecute((ScarecrowTileEntity) entity, this::shouldHoe))
            {
                return FARMER_HOE;
            }
            ((ScarecrowTileEntity) entity).nextState();
        }
        else
        {
            getWorkBuilding().setCurrentField(null);
        }
        return PREPARING;
    }

    /**
     * Returns the farmer's work building.
     *
     * @return building instance
     */
    public BuildingFarmer getWorkBuilding()
    {
        return getOwnBuilding(BuildingFarmer.class);
    }

    /**
     * Searches and adds a field that has not been taken yet for the farmer and then adds it to the list.
     */
    private void searchAndAddFields()
    {
        final Colony colony = worker.getCitizenColonyHandler().getColony();
        if (colony != null)
        {
            @Nullable final ScarecrowTileEntity newField = colony.getBuildingManager().getFreeField(worker.getCitizenData().getId(), world);

            if (newField != null && getWorkBuilding() != null)
            {
                newField.setOwner(worker.getCitizenData().getId());
                newField.setTaken(true);
                newField.markDirty();
                getWorkBuilding().addFarmerFields(newField.getPos());
            }
        }
    }

    /**
     * Handles the offset of the field for the farmer.
     * Checks if the field needs a certain operation checked with a given predicate.
     *
     * @param field     the field object.
     * @param predicate the predicate to test.
     * @return true if a harvestable crop was found.
     */
    private boolean checkIfShouldExecute(@NotNull final ScarecrowTileEntity field, @NotNull final Predicate<BlockPos> predicate)
    {
        if (workingOffset == null)
        {
            handleOffset(field);
        }

        BlockPos position = field.getPos().down().south(workingOffset.getZ()).east(workingOffset.getX());

        while (!predicate.test(position))
        {
            if (!handleOffset(field))
            {
                return false;
            }
            position = field.getPos().down().south(workingOffset.getZ()).east(workingOffset.getX());
        }
        return true;
    }

    /**
     * Checks if the farmer is ready to plant.
     *
     * @param currentField   the field to plant.
     * @param buildingFarmer the farmer building.
     * @return true if he is ready.
     */
    private AIState canGoPlanting(@NotNull final ScarecrowTileEntity currentField, @NotNull final BuildingFarmer buildingFarmer)
    {
        if (currentField.getSeed() == null)
        {
            chatSpamFilter.talkWithoutSpam("entity.farmer.noSeedSet");
            buildingFarmer.setCurrentField(null);
            worker.getCitizenData().getCitizenHappinessHandler().setNoFieldForFarmerModifier(currentField.getPos(), false); 
            return PREPARING;
        }
        worker.getCitizenData().getCitizenHappinessHandler().setNoFieldForFarmerModifier(currentField.getPos(), true); 

        final ItemStack seeds = currentField.getSeed();
        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(seeds.getItem(), seeds.getItemDamage());
        if (slot != -1)
        {
            return FARMER_PLANT;
        }

        if (walkToBuilding())
        {
            return PREPARING;
        }

        checkIfRequestForItemExistOrCreateAsynch(seeds);

        currentField.nextState();
        return PREPARING;
    }

    /**
     * Handles the offset of the field for the farmer.
     *
     * @param field the field object.
     * @return true if successful.
     */
    private boolean handleOffset(@NotNull final ScarecrowTileEntity field)
    {
        if (workingOffset == null)
        {
            workingOffset = new BlockPos(0, 0, 0);
            totalDis = 1;
            dist = 0;
            horizontal = true;
        }
        else
        {
            if (workingOffset.getZ() >= field.getWidthPlusZ() && workingOffset.getX() <= -field.getLengthMinusX())
            {
                workingOffset = null;
                return false;
            }
            else
            {
                if (totalDis == dist)
                {
                    horizontal = !horizontal;
                    dist = 0;
                    if (horizontal)
                    {
                        totalDis++;
                    }
                }
                if (horizontal)
                {
                    workingOffset = new BlockPos(workingOffset.getX(), 0, workingOffset.getZ() - Math.pow(-1, totalDis));
                }
                else
                {
                    workingOffset = new BlockPos(workingOffset.getX() - Math.pow(-1, totalDis), 0, workingOffset.getZ());
                }
                dist++;
            }
        }
        return true;
    }

    /**
     * Called to check when the InventoryShouldBeDumped.
     *
     * @return true if the conditions are met
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        if (shouldDumpInventory)
        {
            shouldDumpInventory = false;
            return true;
        }
        return false;
    }

    /**
     * This (re)initializes a field.
     * Checks the block above to see if it is a plant, if so, breaks it. Then tills.
     */
    private AIState workAtField()
    {
        @Nullable final BuildingFarmer buildingFarmer = getWorkBuilding();

        if (buildingFarmer == null || checkForToolOrWeapon(ToolType.HOE) || buildingFarmer.getCurrentField() == null)
        {
            return PREPARING;
        }
        @Nullable final BlockPos field = buildingFarmer.getCurrentField();
        final TileEntity entity = world.getTileEntity(field);
        if(entity instanceof ScarecrowTileEntity)
        {
            if (workingOffset != null)
            {
                final BlockPos position = field.down().south(workingOffset.getZ()).east(workingOffset.getX());
                // Still moving to the block
                if (walkToBlock(position.up()))
                {
                    return getState();
                }

                switch (getState())
                {
                    case FARMER_HOE:
                        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.hoeing"));

                        if (!hoeIfAble(position))
                        {
                            return getState();
                        }
                        break;
                    case FARMER_PLANT:
                        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.planting"));
                        if (!tryToPlant((ScarecrowTileEntity) entity, position))
                        {
                            return PREPARING;
                        }
                        break;
                    case FARMER_HARVEST:
                        worker.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.harvesting"));
                        if (!harvestIfAble(position))
                        {
                            return getState();
                        }
                        break;
                    default:
                        return PREPARING;
                }
                prevPos = position;
            }
            setDelay(getLevelDelay());

            if (!handleOffset((ScarecrowTileEntity) entity))
            {
                shouldDumpInventory = true;
                ((ScarecrowTileEntity) entity).nextState();
                prevPos = null;
                return IDLE;
            }
        }
        return getState();
    }

    /**
     * Checks if we can hoe, and does so if we can.
     *
     * @param position the position to check.
     * @return true if the farmer should move on.
     */
    private boolean hoeIfAble(final BlockPos position)
    {
        if (shouldHoe(position) && !checkForToolOrWeapon(ToolType.HOE))
        {
            if (mineBlock(position.up()))
            {
                equipHoe();
                worker.swingArm(worker.getActiveHand());
                world.setBlockState(position, Blocks.FARMLAND.getDefaultState());
                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Try to plant the field at a certain position.
     *
     * @param field    the field to try to plant.
     * @param position the position to try.
     * @return the next state to go to.
     */
    private boolean tryToPlant(final ScarecrowTileEntity field, final BlockPos position)
    {
        return !shouldPlant(position, field) || plantCrop(field.getSeed(), position);
    }

    /**
     * Checks if we can harvest, and does so if we can.
     *
     * @return true if we harvested or not supposed to.
     */
    private boolean harvestIfAble(final BlockPos position)
    {
        if (shouldHarvest(position))
        {
            worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
            if (Compatibility.isPamsInstalled())
            {
                harvestCrop(position.up());
                return true;
            }

            return mineBlock(position.up());
        }
        return true;
    }

    @Override
    protected int getLevelDelay()
    {
        return (int) Math.max(SMALLEST_DELAY, STANDARD_DELAY - (this.worker.getCitizenExperienceHandler().getLevel() * DELAY_DIVIDER));
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position the position to check.
     * @return true if should be hoed.
     */
    private boolean shouldHoe(@NotNull final BlockPos position)
    {
        return !BlockUtils.isBlockSeed(world, position.up())
                 && !(world.getBlockState(position.up()).getBlock() instanceof BlockHutField)
                 && (world.getBlockState(position).getBlock() == Blocks.DIRT || world.getBlockState(position).getBlock() == Blocks.GRASS);
    }

    /**
     * Sets the hoe as held item.
     */
    private void equipHoe()
    {
        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, getHoeSlot());
    }

    /**
     * Checks if the ground should be planted.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if the farmer should plant.
     */
    private boolean shouldPlant(@NotNull final BlockPos position, @NotNull final ScarecrowTileEntity field)
    {
        return !field.isNoPartOfField(world, position) && !(world.getBlockState(position.up()).getBlock() instanceof BlockCrops)
                 && !(world.getBlockState(position.up()).getBlock() instanceof BlockStem)
                 && !(world.getBlockState(position).getBlock() instanceof BlockHutField) && world.getBlockState(position).getBlock() == Blocks.FARMLAND;
    }

    /**
     * Plants the crop at a given location.
     *
     * @param item     the crop.
     * @param position the location.
     * @return true if succesful.
     */
    private boolean plantCrop(final ItemStack item, @NotNull final BlockPos position)
    {
        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(item.getItem(), item.getItemDamage());
        if (slot == -1)
        {
            return false;
        }

        @NotNull final IPlantable seed = (IPlantable) item.getItem();
        if ((seed == Items.MELON_SEEDS || seed == Items.PUMPKIN_SEEDS) && prevPos != null && !world.isAirBlock(prevPos.up()))
        {
            return true;
        }

        world.setBlockState(position.up(), seed.getPlant(world, position));
        new InvWrapper(getInventory()).extractItem(slot, 1, false);
        return true;
    }

    /**
     * Checks if the crop should be harvested.
     *
     * @param position the position to check.
     * @return true if should be harvested.
     */
    private boolean shouldHarvest(@NotNull final BlockPos position)
    {
        final IBlockState state = world.getBlockState(position.up());
        final Block block = state.getBlock();

        if (block == Blocks.PUMPKIN || block == Blocks.MELON_BLOCK)
        {
            return true;
        }

        if (block instanceof IGrowable && block instanceof BlockCrops && !(block instanceof BlockStem))
        {
            @NotNull final BlockCrops crop = (BlockCrops) block;
            return crop.isMaxAge(state);
        }

        return false;
    }

    /**
     * Harvest the crop (only if pams is installed).
     *
     * @param pos the position to harvest.
     */
    private void harvestCrop(@NotNull final BlockPos pos)
    {
        final ItemStack tool = worker.getHeldItemMainhand();

        final int fortune = ItemStackUtils.getFortuneOf(tool);
        final IBlockState state = world.getBlockState(pos);
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, pos, state, fortune);
        for (final ItemStack item : drops)
        {
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), item);
        }

        if (state.getBlock() instanceof BlockCrops)
        {
            final BlockCrops crops = (BlockCrops) state.getBlock();
            world.setBlockState(pos, crops.withAge(0));
        }

        worker.getCitizenExperienceHandler().addExperience(XP_PER_BLOCK);
    }

    /**
     * Get's the slot in which the hoe is in.
     *
     * @return slot number
     */
    private int getHoeSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getWorkBuilding().getMaxToolLevel());
    }

    /**
     * Returns the farmer's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public EntityCitizen getCitizen()
    {
        return worker;
    }

    @Override 
    protected boolean checkForToolOrWeapon(@NotNull final IToolType toolType) 
    { 
        final boolean needTool = super.checkForToolOrWeapon(toolType); 
        worker.getCitizenData().getCitizenHappinessHandler().setNeedsATool(toolType, needTool); 
        return needTool; 
    } 
}
