package com.minecolonies.coremod.entity.ai.citizen.farmer;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.buildings.workerbuildings.IField;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.colony.buildings.modules.FieldModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.FarmField;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteraction;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.CompostParticleMessage;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.FARMING;
import static com.minecolonies.api.util.constant.CitizenConstants.BLOCK_BREAK_SOUND_RANGE;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_FREE_FIELDS;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_SEED_SET;

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
     * The maximum depth to search for a surface
     */
    private static final int MAX_DEPTH = 5;

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
     * The current index within the current field
     */
    private int      cell = -1;

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

    /**
     * Called to check when the InventoryShouldBeDumped.
     *
     * @return true if the conditions are met
     */
    @Override
    protected boolean wantInventoryDumped()
    {
        if (shouldDumpInventory || (job != null && job.getActionsDone() >= getActionRewardForCraftingSuccess()))
        {
            shouldDumpInventory = false;
            return true;
        }
        return super.wantInventoryDumped();
    }

    /**
     * Prepares the farmer for farming. Also requests the tools and checks if the farmer has sufficient fields.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState prepareForFarming()
    {
        if (building == null || building.getBuildingLevel() < 1)
        {
            return PREPARING;
        }

        if (!job.getTaskQueue().isEmpty() || getActionsDoneUntilDumping() <= job.getActionsDone())
        {
            return START_WORKING;
        }
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        final FieldModule module = building.getFirstModuleOccurance(FieldModule.class);
        module.claimFields();

        if (module.getFields().size() == building.getMaxBuildingLevel())
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(building.getColony(), AdvancementTriggers.MAX_FIELDS::trigger);
        }

        final int amountOfCompostInBuilding = InventoryUtils.hasBuildingEnoughElseCount(building, this::isCompost, 1);
        final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost);

        if (amountOfCompostInBuilding + amountOfCompostInInv <= 0)
        {
            if (building.requestFertilizer() && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)))
            {
                final List<ItemStack> compostAbleItems = new ArrayList<>();
                compostAbleItems.add(new ItemStack(ModItems.compost, 1));
                compostAbleItems.add(new ItemStack(Items.BONE_MEAL, 1));
                worker.getCitizenData().createRequestAsync(new StackList(compostAbleItems, RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER, STACKSIZE, 1));
            }
        }
        else if (amountOfCompostInInv <= 0 && amountOfCompostInBuilding > 0)
        {
            needsCurrently = new Tuple<>(this::isCompost, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        if (module.hasNoFields())
        {
            if (worker.getCitizenData() != null)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslatableComponent(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            }
            worker.getCitizenData().setIdleAtJob(true);
            return PREPARING;
        }
        worker.getCitizenData().setIdleAtJob(false);

        // Get the next field to work on, if any.
        final IField fieldToWork = module.getFieldToWorkOn();
        if (fieldToWork instanceof FarmField farmField)
        {
            if (farmField.getFieldStage() == FarmField.Stage.PLANTED && checkIfShouldExecute(farmField, pos -> this.findHarvestableSurface(pos) != null))
            {
                return FARMER_HARVEST;
            }
            else if (farmField.getFieldStage() == FarmField.Stage.HOED)
            {
                return canGoPlanting(farmField);
            }
            else if (farmField.getFieldStage() == FarmField.Stage.EMPTY && checkIfShouldExecute(farmField, pos -> this.findHoeableSurface(pos, farmField) != null))
            {
                return FARMER_HOE;
            }
            farmField.nextState();
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
     * Handles the offset of the field for the farmer. Checks if the field needs a certain operation checked with a given predicate.
     *
     * @param field     the field object.
     * @param predicate the predicate to test.
     * @return true if a harvestable crop was found.
     */
    private boolean checkIfShouldExecute(@NotNull final FarmField field, @NotNull final Predicate<BlockPos> predicate)
    {
        BlockPos position;
        do
        {
            workingOffset = nextValidCell(field);
            if (workingOffset == null)
            {
                return false;
            }

            position = field.getPosition().below().south(workingOffset.getZ()).east(workingOffset.getX());
        }
        while (!predicate.test(position));

        return true;
    }

    /**
     * Checks if the farmer is ready to plant.
     *
     * @param currentField the field to plant.
     * @return true if he is ready.
     */
    private IAIState canGoPlanting(@NotNull final FarmField currentField)
    {
        if (currentField.getPlant() == null)
        {
            worker.getCitizenData()
              .triggerInteraction(new PosBasedInteraction(new TranslatableComponent(NO_SEED_SET, currentField.getPosition()),
                ChatPriority.BLOCKING,
                new TranslatableComponent(NO_SEED_SET),
                currentField.getPosition()));

            worker.getCitizenData().setIdleAtJob(true);
            return PREPARING;
        }
        worker.getCitizenData().setIdleAtJob(false);

        final ItemStack seeds = new ItemStack(currentField.getPlant());
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
        checkIfRequestForItemExistOrCreateAsync(seeds, seeds.getMaxStackSize(), 1);
        currentField.nextState();
        return PREPARING;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return position of hoeable surface or null if there is not one
     */
    private BlockPos findHoeableSurface(@NotNull BlockPos position, @NotNull final FarmField field)
    {
        position = getSurfacePos(position);
        if (position == null
              || field.isNoPartOfField(world, position)
              || (world.getBlockState(position.above()).getBlock() instanceof CropBlock)
              || (world.getBlockState(position.above()).getBlock() instanceof BlockScarecrow)
              || !world.getBlockState(position).is(BlockTags.DIRT)
        )
        {
            return null;
        }
        return position;
    }

    /**
     * Finds the position of the surface near the specified position
     *
     * @param position the location to begin the search
     * @return the position of the surface block or null if it can't be found
     */
    private BlockPos getSurfacePos(final BlockPos position)
    {
        return getSurfacePos(position, 0);
    }

    /**
     * Finds the position of the surface near the specified position
     *
     * @param position the location to begin the search
     * @param depth    the depth of the search for the surface
     * @return the position of the surface block or null if it can't be found
     */
    private BlockPos getSurfacePos(final BlockPos position, final Integer depth)
    {
        if (Math.abs(depth) > MAX_DEPTH)
        {
            return null;
        }
        final BlockState curBlockState = world.getBlockState(position);
        @Nullable final Block curBlock = curBlockState.getBlock();
        if ((curBlockState.getMaterial().isSolid() && !(curBlock instanceof PumpkinBlock) && !(curBlock instanceof MelonBlock) && !(curBlock instanceof WebBlock))
              || curBlockState.getMaterial().isLiquid())
        {
            if (depth < 0)
            {
                return position;
            }
            return getSurfacePos(position.above(), depth + 1);
        }
        else
        {
            if (depth > 0)
            {
                return position.below();
            }
            return getSurfacePos(position.below(), depth - 1);
        }
    }

    /**
     * Fetch the next available block within the field. Uses mathemajical quadratic equations to determine the coordinates by an index. Considers max radii set in the field gui.
     *
     * @return the new offset position
     */
    protected BlockPos nextValidCell(FarmField field)
    {
        int ring;
        int ringCell;
        int x;
        int z;
        Direction facing;

        if (workingOffset == null)
        {
            cell = -1;
        }

        do
        {
            if (++cell == getLargestCell(field))
            {
                return null;
            }
            ring = (int) Math.floor((Math.sqrt(cell + 1D) + 1) / 2.0);
            ringCell = cell - (int) (4 * Math.pow(ring - 1D, 2) + 4 * (ring - 1));
            facing = Direction.from2DDataValue(Math.floorDiv(ringCell, 2 * ring));


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

    protected int getLargestCell(FarmField field)
    {
        return (int) Math.pow(field.getMaxRadius() * 2 + 1D, 2);
    }

    /**
     * This (re)initializes a field. Checks the block above to see if it is a plant, if so, breaks it. Then tills.
     *
     * @return the next state to go into.
     */
    private IAIState workAtField()
    {
        final FieldModule module = building.getFirstModuleOccurance(FieldModule.class);
        if (checkForToolOrWeapon(ToolType.HOE) || module.getCurrentField() == null)
        {
            return PREPARING;
        }

        worker.getCitizenData().setVisibleStatus(FARMING_ICON);
        final IField field = module.getCurrentField();
        if (field instanceof FarmField farmField)
        {
            if (workingOffset != null)
            {
                final BlockPos position = farmField.getPosition().below().south(workingOffset.getZ()).east(workingOffset.getX());

                // Still moving to the block
                if (walkToBlock(position.above()))
                {
                    return getState();
                }

                switch ((AIWorkerState) getState())
                {
                    case FARMER_HOE ->
                    {
                        worker.getCitizenStatusHandler().setLatestStatus(new TranslatableComponent("com.minecolonies.coremod.status.hoeing"));
                        if (!hoeIfAble(position, farmField))
                        {
                            return getState();
                        }
                    }
                    case FARMER_PLANT ->
                    {
                        worker.getCitizenStatusHandler().setLatestStatus(new TranslatableComponent("com.minecolonies.coremod.status.planting"));
                        if (!tryToPlant(farmField, position))
                        {
                            return PREPARING;
                        }
                    }
                    case FARMER_HARVEST ->
                    {
                        worker.getCitizenStatusHandler().setLatestStatus(new TranslatableComponent("com.minecolonies.coremod.status.harvesting"));
                        if (!harvestIfAble(position))
                        {
                            return getState();
                        }
                    }
                    default ->
                    {
                        return PREPARING;
                    }
                }
                prevPos = position;
                setDelay(getLevelDelay());
            }

            workingOffset = nextValidCell(farmField);
            if (workingOffset == null)
            {
                shouldDumpInventory = true;
                farmField.nextState();
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
    private boolean hoeIfAble(BlockPos position, final FarmField field)
    {
        BlockPos hoePosition = findHoeableSurface(position, field);
        if (hoePosition != null && !checkForToolOrWeapon(ToolType.HOE))
        {
            if (mineBlock(hoePosition.above()))
            {
                equipHoe();
                worker.swing(worker.getUsedItemHand());
                world.setBlockAndUpdate(hoePosition, Blocks.FARMLAND.defaultBlockState());
                worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);
                worker.decreaseSaturationForContinuousAction();
                worker.getCitizenColonyHandler().getColony().getStatisticsManager().increment(LAND_TILLED);

                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if we can harvest, and does so if we can.
     *
     * @param position the block to harvest.
     * @return true if we harvested or not supposed to.
     */
    private boolean harvestIfAble(BlockPos position)
    {
        position = findHarvestableSurface(position);
        if (position != null)
        {
            if (Compatibility.isPamsInstalled())
            {
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
                harvestCrop(position.above());
                worker.getCitizenColonyHandler().getColony().getStatisticsManager().increment(CROPS_HARVESTED);

                return true;
            }

            if (mineBlock(position.above()))
            {
                worker.getCitizenColonyHandler().getColony().getStatisticsManager().increment(CROPS_HARVESTED);
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
    private boolean tryToPlant(final FarmField field, BlockPos position)
    {
        BlockPos plantPosition = findPlantableSurface(position, field);
        return plantPosition == null || plantCrop(new ItemStack(field.getPlant()), plantPosition);
    }

    /**
     * Sets the hoe as held item.
     */
    private void equipHoe()
    {
        worker.getCitizenItemHandler().setHeldItem(InteractionHand.MAIN_HAND, getHoeSlot());
    }

    /**
     * Checks if the ground should be planted.
     *
     * @param position the position to check.
     * @param field    the field close to this position.
     * @return position of plantable surface or null
     */
    private BlockPos findPlantableSurface(@NotNull BlockPos position, @NotNull final FarmField field)
    {
        BlockPos plantPosition = getSurfacePos(position);
        if (plantPosition == null
              || field.isNoPartOfField(world, plantPosition)
              || (world.getBlockState(plantPosition.above()).getBlock() instanceof CropBlock)
              || (world.getBlockState(plantPosition.above()).getBlock() instanceof StemBlock)
              || (world.getBlockState(plantPosition).getBlock() instanceof BlockScarecrow)
              || !(world.getBlockState(plantPosition).getBlock() instanceof FarmBlock)
        )
        {
            return null;
        }
        return plantPosition;
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

        if (item.getItem() instanceof BlockItem && (((BlockItem) item.getItem()).getBlock() instanceof CropBlock || ((BlockItem) item.getItem()).getBlock() instanceof StemBlock))
        {
            @NotNull final Item seed = item.getItem();
            if ((seed == Items.MELON_SEEDS || seed == Items.PUMPKIN_SEEDS) && prevPos != null && !world.isEmptyBlock(prevPos.above()))
            {
                return true;
            }

            world.setBlockAndUpdate(position.above(), ((BlockItem) item.getItem()).getBlock().defaultBlockState());
            worker.decreaseSaturationForContinuousAction();
            getInventory().extractItem(slot, 1, false);
        }
        return true;
    }

    /**
     * Checks if the crop should be harvested.
     *
     * @param position the position to check.
     * @return position of harvestable block or null
     */
    private BlockPos findHarvestableSurface(@NotNull BlockPos position)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }
        BlockState state = world.getBlockState(position.above());
        Block block = state.getBlock();

        if (block == Blocks.PUMPKIN || block == Blocks.MELON)
        {
            return position;
        }

        if (isCrop(block))
        {
            @NotNull CropBlock crop = (CropBlock) block;
            if (crop.isMaxAge(state))
            {
                return position;
            }
            final int amountOfCompostInInv = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost);
            if (amountOfCompostInInv == 0)
            {
                return null;
            }

            if (InventoryUtils.shrinkItemCountInItemHandler(worker.getInventoryCitizen(), this::isCompost))
            {
                Network.getNetwork().sendToPosition(new CompostParticleMessage(position.above()),
                  new PacketDistributor.TargetPoint(position.getX(), position.getY(), position.getZ(), BLOCK_BREAK_SOUND_RANGE, world.dimension()));
                crop.growCrops(world, position.above(), state);
                state = world.getBlockState(position.above());
                block = state.getBlock();
                if (isCrop(block))
                {
                    crop = (CropBlock) block;
                }
                else
                {
                    return null;
                }
            }
            return crop.isMaxAge(state) ? position : null;
        }
        return null;
    }

    /**
     * Check if a block is a crop.
     *
     * @param block the block.
     * @return true if so.
     */
    public boolean isCrop(final Block block)
    {
        return block instanceof CropBlock;
    }

    @Override
    protected List<ItemStack> increaseBlockDrops(final List<ItemStack> drops)
    {
        final double increaseCrops = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(FARMING);
        if (increaseCrops == 0)
        {
            return drops;
        }

        final List<ItemStack> newDrops = new ArrayList<>();
        for (final ItemStack stack : drops)
        {
            final ItemStack drop = stack.copy();
            if (worker.getRandom().nextDouble() < increaseCrops)
            {
                drop.setCount(drop.getCount() * 2);
            }
            newDrops.add(drop);
        }

        return newDrops;
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    /**
     * Harvest the crop (only if pams is installed).
     *
     * @param pos the position to harvest.
     */
    private void harvestCrop(@NotNull final BlockPos pos)
    {
        final ItemStack tool = worker.getMainHandItem();

        final int fortune = ItemStackUtils.getFortuneOf(tool);
        final BlockState state = world.getBlockState(pos);

        final double chance = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(FARMING);

        final NonNullList<ItemStack> drops = NonNullList.create();
        state.getDrops(new LootContext.Builder((ServerLevel) world).withLuck(fortune)
                         .withLuck(fortune)
                         .withParameter(LootContextParams.ORIGIN, worker.position())
                         .withParameter(LootContextParams.TOOL, tool)
                         .withParameter(LootContextParams.THIS_ENTITY, getCitizen()));
        for (final ItemStack item : drops)
        {
            final ItemStack drop = item.copy();
            if (worker.getRandom().nextDouble() < chance)
            {
                drop.setCount(drop.getCount() * 2);
            }
            InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), drop);
        }

        if (state.getBlock() instanceof CropBlock)
        {
            final CropBlock crops = (CropBlock) state.getBlock();
            world.setBlockAndUpdate(pos, crops.getStateForAge(0));
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
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxToolLevel());
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

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata((getState() == FARMER_PLANT || getState() == FARMER_HARVEST) ? RENDER_META_WORKING : "");
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
        if (nextState != START_WORKING && nextState != IDLE)
        {
            return nextState;
        }

        if (wantInventoryDumped())
        {
            // Wait to dump before continuing.
            return getState();
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
}
