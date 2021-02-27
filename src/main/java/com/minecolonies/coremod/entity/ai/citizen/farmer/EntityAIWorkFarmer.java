package com.minecolonies.coremod.entity.ai.citizen.farmer;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.tileentities.ScarecrowFieldStage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteraction;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.CompostParticleMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.FARMING;
import static com.minecolonies.api.util.constant.CitizenConstants.BLOCK_BREAK_SOUND_RANGE;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Farmer AI class. Created: December 20, 2014
 */
public class EntityAIWorkFarmer extends AbstractEntityAICrafting<JobFarmer, BuildingFarmer>
{
    /**
     * Return to chest after this amount of stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

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
     * Farming icon
     */
    private final static VisibleCitizenStatus FARMING_ICON =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/farmer.png"), "com.minecolonies.gui.visiblestatus.farmer");

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
     * Constructor for the Farmer. Defines the tasks the Farmer executes.
     *
     * @param job a farmer job to use.
     */
    public EntityAIWorkFarmer(@NotNull final JobFarmer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> START_WORKING, 10),
          new AITarget(PREPARING, this::prepareForFarming, TICKS_SECOND),
          new AITarget(FARMER_HOE, this::workAtField, 5),
          new AITarget(FARMER_PLANT, this::workAtField, 5),
          new AITarget(FARMER_HARVEST, this::workAtField, 5)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingFarmer> getExpectedBuildingClass()
    {
        return BuildingFarmer.class;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING)
        {
            return nextState;
        }

        if (job.getTaskQueue().isEmpty())
        {
            return PREPARING;
        }

        if (job.getCurrentTask() == null)
        {
            return PREPARING;
        }


        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    /**
     * Prepares the farmer for farming. Also requests the tools and checks if the farmer has sufficient fields.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState prepareForFarming()
    {
        @Nullable final BuildingFarmer building = getOwnBuilding();
        if (building == null || building.getBuildingLevel() < 1)
        {
            return PREPARING;
        }

        if (!job.getTaskQueue().isEmpty())
        {
            return START_WORKING;
        }
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        building.syncWithColony(world);
        if (building.getFarmerFields().size() < getOwnBuilding().getBuildingLevel() && !building.assignManually())
        {
            searchAndAddFields();
        }

        if (building.getFarmerFields().size() == getOwnBuilding().getMaxBuildingLevel())
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(building.getColony(), AdvancementTriggers.MAX_FIELDS::trigger);
        }

        final int amountOfCompostInBuilding = InventoryUtils.getCountFromBuilding(getOwnBuilding(), this::isCompost);
        final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost);

        if (amountOfCompostInBuilding + amountOfCompostInInv <= 0)
        {
            if (getOwnBuilding().requestFertilizer() && !getOwnBuilding().hasWorkerOpenRequestsOfType(Objects.requireNonNull(worker.getCitizenData()),
              TypeToken.of(StackList.class)))
            {
                final List<ItemStack> compostAbleItems = new ArrayList<>();
                compostAbleItems.add(new ItemStack(ModItems.compost, 1));
                compostAbleItems.add(new ItemStack(Items.BONE_MEAL, 1));
                worker.getCitizenData().createRequestAsync(new StackList(compostAbleItems, FERTLIZER, STACKSIZE, 1));
            }
        }
        else if (amountOfCompostInInv <= 0 && amountOfCompostInBuilding > 0)
        {
            needsCurrently = new Tuple<>(this::isCompost, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        if (building.hasNoFields())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            }
            worker.getCitizenData().setIdleAtJob(true);
            return PREPARING;
        }
        worker.getCitizenData().setIdleAtJob(false);

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
            if (((ScarecrowTileEntity) entity).getFieldStage() == ScarecrowFieldStage.PLANTED && checkIfShouldExecute((ScarecrowTileEntity) entity, this::shouldHarvest))
            {
                return FARMER_HARVEST;
            }
            else if (((ScarecrowTileEntity) entity).getFieldStage() == ScarecrowFieldStage.HOED)
            {
                return canGoPlanting((ScarecrowTileEntity) entity, building);
            }
            else if (((ScarecrowTileEntity) entity).getFieldStage() == ScarecrowFieldStage.EMPTY && checkIfShouldExecute((ScarecrowTileEntity) entity,
              pos -> this.shouldHoe(pos, (ScarecrowTileEntity) entity)))
            {
                return FARMER_HOE;
            }
            ((ScarecrowTileEntity) entity).nextState();
        }
        else
        {
            getOwnBuilding().setCurrentField(null);
        }
        return PREPARING;
    }

    /**
     * Check if itemStack can be used as compost.
     *
     * @param itemStack the stack to check.
     * @return true if so.
     */
    private boolean isCompost(final ItemStack itemStack)
    {
        if (itemStack.getItem() == ModItems.compost)
        {
            return true;
        }
        return itemStack.getItem() == Items.BONE_MEAL;
    }

    /**
     * Searches and adds a field that has not been taken yet for the farmer and then adds it to the list.
     */
    private void searchAndAddFields()
    {
        final IColony colony = worker.getCitizenColonyHandler().getColony();
        if (colony != null)
        {
            @Nullable final AbstractScarecrowTileEntity newField = colony.getBuildingManager().getFreeField(worker.getCitizenData().getId(), world);

            if (newField != null && getOwnBuilding() != null)
            {
                newField.setOwner(worker.getCitizenData().getId());
                newField.setTaken(true);
                newField.markDirty();
                getOwnBuilding().addFarmerFields(newField.getPosition());
            }
        }
    }

    /**
     * Handles the offset of the field for the farmer. Checks if the field needs a certain operation checked with a given predicate.
     *
     * @param field     the field object.
     * @param predicate the predicate to test.
     * @return true if a harvestable crop was found.
     */
    private boolean checkIfShouldExecute(@NotNull final ScarecrowTileEntity field, @NotNull final Predicate<BlockPos> predicate)
    {
        BlockPos position;
        do
        {
            workingOffset = nextValidCell(field);
            if (workingOffset == null)
            {
                return false;
            }

            position = field.getPos().down().south(workingOffset.getZ()).east(workingOffset.getX());
        }
        while (!predicate.test(position));

        return true;
    }

    /**
     * Checks if the farmer is ready to plant.
     *
     * @param currentField   the field to plant.
     * @param buildingFarmer the farmer building.
     * @return true if he is ready.
     */
    private IAIState canGoPlanting(@NotNull final ScarecrowTileEntity currentField, @NotNull final BuildingFarmer buildingFarmer)
    {
        if (currentField.getSeed() == null || currentField.getSeed().isEmpty())
        {
            worker.getCitizenData()
              .triggerInteraction(new PosBasedInteraction(new TranslationTextComponent(NO_SEED_SET, currentField.getPos()),
                ChatPriority.BLOCKING,
                new TranslationTextComponent(NO_SEED_SET),
                currentField.getPos()));
            buildingFarmer.setCurrentField(null);
            worker.getCitizenData().setIdleAtJob(true);
            return PREPARING;
        }
        worker.getCitizenData().setIdleAtJob(false);

        final ItemStack seeds = currentField.getSeed().copy();
        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(seeds.getItem());
        if (slot != -1)
        {
            return FARMER_PLANT;
        }

        if (walkToBuilding())
        {
            return PREPARING;
        }

        seeds.setCount(seeds.getMaxStackSize());
        checkIfRequestForItemExistOrCreateAsynch(seeds, seeds.getMaxStackSize(), 1);
        currentField.nextState();
        return PREPARING;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if should be hoed.
     */
    private boolean shouldHoe(@NotNull final BlockPos position, @NotNull final ScarecrowTileEntity field)
    {

        return !field.isNoPartOfField(world, position) && !(world.getBlockState(position.up()).getBlock() instanceof CropsBlock)
                 && !(world.getBlockState(position.up()).getBlock() instanceof BlockScarecrow)
                 && (world.getBlockState(position).getBlock().isIn(Tags.Blocks.DIRT) || world.getBlockState(position).getBlock() instanceof GrassBlock);
    }

    /**
     * The current index within the current field
     */
    private int cell = -1;

    /**
     * Fetch the next available block within the field. Uses mathemajical quadratic equations to determine the coordinates by an index. Considers max radii set in the field gui.
     *
     * @return the new offset position
     */
    protected BlockPos nextValidCell(AbstractScarecrowTileEntity field)
    {
        int ring, ringCell, x, z;
        Direction facing;

        if (workingOffset == null)
        {
            cell = -1;
        }

        do
        {
            if (++cell == getLargestCell())
            {
                return null;
            }
            ring = (int) Math.floor((Math.sqrt(cell + 1) + 1) / 2.0);
            ringCell = cell - (int) (4 * Math.pow(ring - 1, 2) + 4 * (ring - 1));
            facing = Direction.byHorizontalIndex(Math.floorDiv(ringCell, 2 * ring));


            if (facing.getAxis() == Direction.Axis.Z)
            {
                x = (facing == Direction.NORTH ? -1 : 1) * (ring - (ringCell % (2 * ring)));
                z = (facing == Direction.NORTH ? -1 : 1) * ring;
            }
            else
            {
                x = (facing == Direction.WEST ? -1 : 1) * ring;
                z = (facing == Direction.EAST ? -1 : 1) * (ring - (ringCell % (2 * ring)));
            }
        }
        while (
          -z > field.getRadius(Direction.NORTH)
            || x > field.getRadius(Direction.EAST)
            || z > field.getRadius(Direction.SOUTH)
            || -x > field.getRadius(Direction.WEST)
        );

        return new BlockPos(x, 0, z);
    }

    protected int getLargestCell()
    {
        return (int) Math.pow(ScarecrowTileEntity.getMaxRange() * 2 + 1, 2);
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
     * This (re)initializes a field. Checks the block above to see if it is a plant, if so, breaks it. Then tills.
     *
     * @return the next state to go into.
     */
    private IAIState workAtField()
    {
        @Nullable final BuildingFarmer buildingFarmer = getOwnBuilding();

        if (buildingFarmer == null || checkForToolOrWeapon(ToolType.HOE) || buildingFarmer.getCurrentField() == null)
        {
            return PREPARING;
        }

        worker.getCitizenData().setVisibleStatus(FARMING_ICON);
        @Nullable final BlockPos field = buildingFarmer.getCurrentField();
        final TileEntity entity = world.getTileEntity(field);
        if (entity instanceof ScarecrowTileEntity)
        {
            final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) entity;
            if (workingOffset != null)
            {
                if (scarecrow.getOwnerId() != worker.getCivilianID())
                {
                    buildingFarmer.freeField(buildingFarmer.getCurrentField());
                    buildingFarmer.setCurrentField(null);
                    return getState();
                }

                final BlockPos position = field.down().south(workingOffset.getZ()).east(workingOffset.getX());

                // Still moving to the block
                if (walkToBlock(position.up()))
                {
                    return getState();
                }

                switch ((AIWorkerState) getState())
                {
                    case FARMER_HOE:
                        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.hoeing"));

                        if (!hoeIfAble(position, scarecrow))
                        {
                            return getState();
                        }
                        break;
                    case FARMER_PLANT:
                        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.planting"));
                        if (!tryToPlant(scarecrow, position))
                        {
                            return PREPARING;
                        }
                        break;
                    case FARMER_HARVEST:
                        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.harvesting"));
                        if (!harvestIfAble(position))
                        {
                            return getState();
                        }
                        break;
                    default:
                        return PREPARING;
                }
                prevPos = position;
                setDelay(getLevelDelay());
            }

            workingOffset = nextValidCell(scarecrow);
            if (workingOffset == null)
            {
                shouldDumpInventory = true;
                scarecrow.nextState();
                prevPos = null;
                return IDLE;
            }
        }
        else
        {
            return IDLE;
        }
        return getState();
    }

    /**
     * Checks if we can hoe, and does so if we can.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return true if the farmer should move on.
     */
    private boolean hoeIfAble(final BlockPos position, final ScarecrowTileEntity field)
    {
        if (shouldHoe(position, field) && !checkForToolOrWeapon(ToolType.HOE))
        {
            if (mineBlock(position.up()))
            {
                equipHoe();
                worker.swingArm(worker.getActiveHand());
                world.setBlockState(position, Blocks.FARMLAND.getDefaultState());
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
                worker.decreaseSaturationForContinuousAction();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    /**
     * Checks if we can harvest, and does so if we can.
     *
     * @param position the block to harvest.
     * @return true if we harvested or not supposed to.
     */
    private boolean harvestIfAble(final BlockPos position)
    {
        if (shouldHarvest(position))
        {
            if (Compatibility.isPamsInstalled())
            {
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
                harvestCrop(position.up());
                return true;
            }

            if (mineBlock(position.up()))
            {
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    protected int getLevelDelay()
    {
        return (int) Math.max(SMALLEST_DELAY, STANDARD_DELAY - ((getPrimarySkillLevel() / 2.0) * DELAY_DIVIDER));
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
     * Sets the hoe as held item.
     */
    private void equipHoe()
    {
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, getHoeSlot());
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
        return !field.isNoPartOfField(world, position) && !(world.getBlockState(position.up()).getBlock() instanceof CropsBlock)
                 && !(world.getBlockState(position.up()).getBlock() instanceof StemBlock)
                 && !(world.getBlockState(position).getBlock() instanceof BlockScarecrow) && world.getBlockState(position).getBlock() instanceof FarmlandBlock;
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
        if (item == null || item.isEmpty())
        {
            return false;
        }
        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(item.getItem());
        if (slot == -1)
        {
            return false;
        }

        if (item.getItem() instanceof BlockItem && (((BlockItem) item.getItem()).getBlock() instanceof CropsBlock || ((BlockItem) item.getItem()).getBlock() instanceof StemBlock))
        {
            @NotNull final Item seed = item.getItem();
            if ((seed == Items.MELON_SEEDS || seed == Items.PUMPKIN_SEEDS) && prevPos != null && !world.isAirBlock(prevPos.up()))
            {
                return true;
            }

            world.setBlockState(position.up(), ((BlockItem) item.getItem()).getBlock().getDefaultState());
            worker.decreaseSaturationForContinuousAction();
            getInventory().extractItem(slot, 1, false);
        }
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
        BlockState state = world.getBlockState(position.up());
        Block block = state.getBlock();

        if (block == Blocks.PUMPKIN || block == Blocks.MELON)
        {
            return true;
        }

        if (isCrop(block))
        {
            @NotNull CropsBlock crop = (CropsBlock) block;
            if (crop.isMaxAge(state))
            {
                return true;
            }
            final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost);
            if (amountOfCompostInInv == 0)
            {
                return false;
            }

            if (InventoryUtils.shrinkItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost))
            {
                Network.getNetwork().sendToPosition(new CompostParticleMessage(position.up()),
                  new PacketDistributor.TargetPoint(position.getX(), position.getY(), position.getZ(), BLOCK_BREAK_SOUND_RANGE, world.getDimensionKey()));
                crop.grow(world, position.up(), state);
                state = world.getBlockState(position.up());
                block = state.getBlock();
                if (isCrop(block))
                {
                    crop = (CropsBlock) block;
                }
            }
            return crop.isMaxAge(state);
        }

        return false;
    }

    /**
     * Check if a block is a crop.
     *
     * @param block the block.
     * @return true if so.
     */
    public boolean isCrop(final Block block)
    {
        return block instanceof CropsBlock;
    }

    @Override
    protected List<ItemStack> increaseBlockDrops(final List<ItemStack> drops)
    {
        if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(FARMING) > 0)
        {
            return drops;
        }

        final List<ItemStack> newDrops = new ArrayList<>();
        for (final ItemStack stack : drops)
        {
            final ItemStack drop = stack.copy();
            if (worker.getRandom().nextDouble() < worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(FARMING))
            {
                drop.setCount(drop.getCount() * 2);
            }
            newDrops.add(drop);
        }

        return newDrops;
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
        final BlockState state = world.getBlockState(pos);

        final double chance = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(FARMING);

        final NonNullList<ItemStack> drops = NonNullList.create();
        state.getDrops(new LootContext.Builder((ServerWorld) world).withLuck(fortune)
                         .withLuck(fortune)
                         .withParameter(LootParameters.field_237457_g_, worker.getPositionVec())
                         .withParameter(LootParameters.TOOL, tool)
                         .withParameter(LootParameters.THIS_ENTITY, getCitizen()));
        for (final ItemStack item : drops)
        {
            final ItemStack drop = item.copy();
            if (worker.getRandom().nextDouble() < chance)
            {
                drop.setCount(drop.getCount() * 2);
            }
            InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), drop);
        }

        if (state.getBlock() instanceof CropsBlock)
        {
            final CropsBlock crops = (CropsBlock) state.getBlock();
            world.setBlockState(pos, crops.withAge(0));
        }

        this.incrementActionsDone();
        worker.decreaseSaturationForContinuousAction();
    }

    /**
     * Get's the slot in which the hoe is in.
     *
     * @return slot number
     */
    private int getHoeSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());
    }

    /**
     * Returns the farmer's worker instance. Called from outside this class.
     *
     * @return citizen object
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }
}
