package com.minecolonies.core.entity.ai.workers.production.agriculture;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.Network;
import com.minecolonies.core.blocks.BlockScarecrow;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.blocks.MinecoloniesFarmland;
import com.minecolonies.core.colony.buildings.modules.FieldsModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobFarmer;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import com.minecolonies.core.items.ItemCrop;
import com.minecolonies.core.network.messages.client.CompostParticleMessage;
import com.minecolonies.core.util.AdvancementUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
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
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_FREE_FIELDS;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

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
     * The default delay the farmer should have.
     */
    private static final int DEFAULT_DELAY = 40;

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
    private static final VisibleCitizenStatus FARMING_ICON =
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
    private int cell = -1;

    /**
     * Constructor for the Farmer. Defines the tasks the Farmer executes.
     *
     * @param job a farmer job to use.
     */
    public EntityAIWorkFarmer(@NotNull final JobFarmer job)
    {
        super(job);
        super.registerTargets(
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
        if (shouldDumpInventory || job.getActionsDone() >= getActionRewardForCraftingSuccess())
        {
            shouldDumpInventory = false;
            return true;
        }
        return super.wantInventoryDumped();
    }

    @Override
    protected int getActionRewardForCraftingSuccess()
    {
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata((getState() == FARMER_PLANT || getState() == FARMER_HARVEST) ? RENDER_META_WORKING : "");
    }

    @Override
    protected IAIState decide()
    {
        IAIState state = super.decide();

        if (state == IDLE)
        {
            return PREPARING;
        }
        return state;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_BLOCKS_MINED;
    }

    /**
     * Prepares the farmer for farming. Also requests the tools and checks if the farmer has sufficient fields.
     *
     * @return the next IAIState
     */
    @NotNull
    private IAIState prepareForFarming()
    {
        worker.getCitizenData().setIdleAtJob(true);

        if (building == null || building.getBuildingLevel() < 1)
        {
            return PREPARING;
        }

        final FieldsModule module = building.getFirstModuleOccurance(FieldsModule.class);
        module.claimFields();

        if (module.getOwnedFields().size() == building.getMaxBuildingLevel())
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
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NO_FREE_FIELDS), ChatPriority.BLOCKING));
            }
            return IDLE;
        }

        module.resetCurrentField();
        final IField fieldToWork = module.getFieldToWorkOn();
        if (fieldToWork instanceof FarmField farmField)
        {
            worker.getCitizenData().setIdleAtJob(false);
            worker.getCitizenData().setVisibleStatus(FARMING_ICON);

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
     * @param farmField the field object.
     * @param predicate the predicate to test.
     * @return true if a harvestable crop was found.
     */
    private boolean checkIfShouldExecute(@NotNull final FarmField farmField, @NotNull final Predicate<BlockPos> predicate)
    {
        BlockPos position;
        do
        {
            workingOffset = nextValidCell(farmField);
            if (workingOffset == null)
            {
                return false;
            }

            position = farmField.getPosition().below().south(workingOffset.getZ()).east(workingOffset.getX());
        }
        while (!predicate.test(position));

        return true;
    }

    /**
     * Checks if the farmer is ready to plant.
     *
     * @param farmField the field to plant.
     * @return the next AI state.
     */
    private IAIState canGoPlanting(@NotNull final FarmField farmField)
    {
        if (farmField.getSeed() == null)
        {
            return PREPARING;
        }

        final ItemStack seeds = farmField.getSeed();
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
        farmField.nextState();
        return PREPARING;
    }

    /**
     * Checks if the ground should be hoed and the block above removed.
     *
     * @param position  the position to check.
     * @param farmField the field close to this position.
     * @return position of hoeable surface or null if there is not one
     */
    private BlockPos findHoeableSurface(@NotNull BlockPos position, @NotNull final FarmField farmField)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }
        final BlockState blockState = world.getBlockState(position);
        if (farmField.isNoPartOfField(world, position)
              || (world.getBlockState(position.above()).getBlock() instanceof CropBlock)
              || (world.getBlockState(position.above()).getBlock() instanceof BlockScarecrow)
              || (!blockState.is(BlockTags.DIRT) && !(blockState.getBlock() instanceof MinecoloniesFarmland) && !(blockState.getBlock() instanceof FarmBlock))
              ||  isRightFarmLandForCrop(farmField, blockState)
              || (world.getBlockState(position.above()).getBlock() instanceof MinecoloniesCropBlock)
        )
        {
            return null;
        }

        final BlockState aboveState = world.getBlockState(position.above());
        if (aboveState.canBeReplaced() && !(aboveState.getBlock() instanceof MinecoloniesCropBlock))
        {
            world.destroyBlock(position.above(), true);
        }

        if (!isRightFarmLandForCrop(farmField, blockState))
        {
            return position;
        }

        final BlockHitResult blockHitResult = new BlockHitResult(Vec3.ZERO, Direction.UP, position, false);
        final UseOnContext useOnContext = new UseOnContext(world,
          null,
          InteractionHand.MAIN_HAND,
          getInventory().getStackInSlot(InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.hoe.get(), TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel())),
          blockHitResult);
        final BlockState toolModifiedState = blockState.getToolModifiedState(useOnContext, ToolActions.HOE_TILL, true);
        if (toolModifiedState == null || !toolModifiedState.is(Blocks.FARMLAND))
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
        if (Math.abs(depth) > MAX_DEPTH || !WorldUtil.isBlockLoaded(world, position))
        {
            return null;
        }
        final BlockState curBlockState = world.getBlockState(position);
        @Nullable final Block curBlock = curBlockState.getBlock();
        if ((curBlockState.isSolid() && !(curBlock instanceof PumpkinBlock) && !(curBlock instanceof MelonBlock) && !(curBlock instanceof WebBlock)) || curBlockState.liquid())
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
     * Fetch the next available block within the field. Uses mathematical quadratic equations to determine the coordinates by an index. Considers max radii set in the field gui.
     *
     * @return the new offset position
     */
    protected BlockPos nextValidCell(FarmField farmField)
    {
        int ring, ringCell, x, z;
        Direction facing;

        if (workingOffset == null)
        {
            cell = -1;
        }

        do
        {
            if (++cell == getLargestCell(farmField))
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
          -z > farmField.getRadius(Direction.NORTH)
            || x > farmField.getRadius(Direction.EAST)
            || z > farmField.getRadius(Direction.SOUTH)
            || -x > farmField.getRadius(Direction.WEST)
        );

        return new BlockPos(x, 0, z);
    }

    protected int getLargestCell(FarmField farmField)
    {
        return (int) Math.pow(farmField.getMaxRadius() * 2D + 1D, 2);
    }

    /**
     * This (re)initializes a field. Checks the block above to see if it is a plant, if so, breaks it. Then tills.
     *
     * @return the next state to go into.
     */
    private IAIState workAtField()
    {
        final FieldsModule module = building.getFirstModuleOccurance(FieldsModule.class);
        if (checkForToolOrWeapon(ModEquipmentTypes.hoe.get()) || module.getCurrentField() == null)
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
                        if (!hoeIfAble(position, farmField))
                        {
                            return getState();
                        }
                    }
                    case FARMER_PLANT ->
                    {
                        if (!tryToPlant(farmField, position))
                        {
                            return PREPARING;
                        }
                    }
                    case FARMER_HARVEST ->
                    {
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
     * @param position  the position to check.
     * @param farmField the field close to this position.
     * @return true if the farmer should move on.
     */
    private boolean hoeIfAble(BlockPos position, final FarmField farmField)
    {
        position = findHoeableSurface(position, farmField);
        if (position != null && !checkForToolOrWeapon(ModEquipmentTypes.hoe.get()))
        {
            if (mineBlock(position.above()))
            {
                equipHoe();
                worker.swing(worker.getUsedItemHand());
                createCorrectFarmlandForSeed(farmField.getSeed(), position);
                worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);
                worker.decreaseSaturationForContinuousAction();
                worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(LAND_TILLED, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());

                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Create the correct farmland for a given seed.
     * @param seed the crop.
     * @param pos the position.
     */
    private void createCorrectFarmlandForSeed(final ItemStack seed, final BlockPos pos)
    {
        if (seed.getItem() instanceof ItemCrop itemCrop)
        {
            world.setBlockAndUpdate(pos, ((MinecoloniesCropBlock) itemCrop.getBlock()).getPreferredFarmland().defaultBlockState());
        }
        else
        {
            world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
        }
    }

    /**
     * Check if this is the right farm land for the specific crop.
     * @param farmField the field we're testing this for.
     * @param blockState the state we're testing this on.
     * @return true if so.
     */
    private boolean isRightFarmLandForCrop(final FarmField farmField, final BlockState blockState)
    {
        if (farmField.getSeed().getItem() instanceof ItemCrop itemCrop)
        {
            return blockState.getBlock() == ((MinecoloniesCropBlock) itemCrop.getBlock()).getPreferredFarmland();
        }
        else
        {
            return blockState.getBlock() instanceof FarmBlock;
        }
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
                worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(CROPS_HARVESTED, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());

                return true;
            }

            if (mineBlock(position.above()))
            {
                worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(CROPS_HARVESTED, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
                worker.getCitizenExperienceHandler().addExperience(XP_PER_HARVEST);
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBlockDropReception(final List<ItemStack> blockDrops)
    {
        super.onBlockDropReception(blockDrops);
        for (final ItemStack stack : blockDrops)
        {
            building.getModule(STATS_MODULE).incrementBy(ITEM_OBTAINED + ";" + stack.getItem().getDescriptionId(), stack.getCount());
        }
    }

    protected int getLevelDelay()
    {
        return (int) Math.max(SMALLEST_DELAY, DEFAULT_DELAY - ((getPrimarySkillLevel() / 2.0) * DELAY_DIVIDER));
    }

    /**
     * Try to plant the field at a certain position.
     *
     * @param farmField the field to try to plant.
     * @param position  the position to try.
     * @return the next state to go to.
     */
    private boolean tryToPlant(final FarmField farmField, BlockPos position)
    {
        position = findPlantableSurface(position, farmField);
        return position == null || plantCrop(farmField.getSeed(), position);
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
     * @param position  the position to check.
     * @param farmField the field close to this position.
     * @return position of plantable surface or null
     */
    private BlockPos findPlantableSurface(@NotNull BlockPos position, @NotNull final FarmField farmField)
    {
        position = getSurfacePos(position);
        if (position == null
              || farmField.isNoPartOfField(world, position)
              || world.getBlockState(position.above()).getBlock() instanceof CropBlock
              || world.getBlockState(position.above()).getBlock() instanceof StemBlock
              || world.getBlockState(position).getBlock() instanceof BlockScarecrow
              || !isRightFarmLandForCrop(farmField, world.getBlockState(position))
              || world.getBlockState(position.above()).getBlock() instanceof MinecoloniesCropBlock
        )
        {
            return null;
        }

        return position;
    }

    /**
     * Plants the crop at a given location.
     *
     * @param item     the crop.
     * @param position the location.
     * @return true if successful.
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

        if (item.getItem() instanceof BlockItem blockItem && (blockItem.getBlock() instanceof CropBlock || blockItem.getBlock() instanceof StemBlock || blockItem.getBlock() instanceof MinecoloniesCropBlock))
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

        if (block instanceof CropBlock)
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
                if (block instanceof CropBlock)
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
        else if (block instanceof MinecoloniesCropBlock minecoloniesCrop)
        {
            if (minecoloniesCrop.isMaxAge(state))
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
                minecoloniesCrop.attemptGrow(state, (ServerLevel) world, position.above());
                state = world.getBlockState(position.above());
                block = state.getBlock();
                if (block instanceof MinecoloniesCropBlock)
                {
                    minecoloniesCrop = (MinecoloniesCropBlock) block;
                }
                else
                {
                    return null;
                }
            }
            return minecoloniesCrop.isMaxAge(state) ? position : null;
        }
        return null;
    }

    @Override
    protected List<ItemStack> increaseBlockDrops(final List<ItemStack> drops)
    {
        final double increaseCrops = worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(FARMING);
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

        final double chance = worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(FARMING);

        final NonNullList<ItemStack> drops = NonNullList.create();
        state.getDrops(new LootParams.Builder((ServerLevel) world).withLuck(fortune)
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

        if (state.getBlock() instanceof final CropBlock crops)
        {
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
        return InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.hoe.get(), TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel());
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
