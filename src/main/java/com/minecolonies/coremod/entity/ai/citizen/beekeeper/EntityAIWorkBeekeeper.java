package com.minecolonies.coremod.entity.ai.citizen.beekeeper;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.coremod.colony.jobs.JobBeekeeper;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_BEEKEEPER_HARVESTING;
import static com.minecolonies.api.util.constant.TranslationConstants.FLOWERS;

public class EntityAIWorkBeekeeper extends AbstractEntityAIInteract<JobBeekeeper>
{
    /**
     * Amount of animals needed to bread.
     */
    private static final int NUM_OF_ANIMALS_TO_BREED = 2;
    private static final int BEES_PER_LEVEL          = 3;

    /**
     * Experience given per beehive harvested.
     */
    private static final double EXP_PER_HARVEST = 5.0;

    /**
     * Distance two animals need to be inside to breed.
     */
    private static final int DISTANCE_TO_BREED = 10;

    /**
     * Distance around a hive wher bees wander.
     */
    private static final int HIVE_BEE_RADIUS = 22;

    /**
     * Delays used to setDelay()
     */
    private static final int DECIDING_DELAY   = 40;
    private static final int NO_ANIMALS_DELAY = 100;
    private static final int BREEDING_DELAY   = 40;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkBeekeeper(@NotNull JobBeekeeper job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(PREPARING, this::prepareForHerding, 1),
          new AITarget(DECIDE, this::decideWhatToDo, 1),
          new AITarget(HERDER_BREED, this::breedAnimals, 1),
          new AITarget(BEEKEEPER_HARVEST, this::harvestHoney, TICKS_SECOND)
        );
    }

    /**
     * Checks the beehives/beenests and their surroundings if the maximum number of bees is reached
     *
     * @param allBees all bees in the area around the beehives/beenests
     * @return true if the maximum nuber of bees is reached els false
     */
    private boolean hasMaxAnimals(final List<BeeEntity> allBees)
    {
        if (getOwnBuilding() != null)
        {
            final int numOfBeesInHive = getOwnBuilding(BuildingBeekeeper.class)
                                          .getHives()
                                          .stream()
                                          .map(world::getTileEntity)
                                          .filter(Objects::nonNull)
                                          .map(BeehiveTileEntity.class::cast)
                                          .mapToInt(BeehiveTileEntity::func_226971_j_)
                                          .sum();
            final int numOfAnimals = allBees.size();
            final int maxAnimals = getOwnBuilding().getBuildingLevel() * BEES_PER_LEVEL;

            return (numOfAnimals + numOfBeesInHive) > maxAnimals;
        }
        return true;
    }

    /**
     * Prepares the beekeeper for herding
     *
     * @return The next {@link IAIState}.
     */
    private IAIState prepareForHerding()
    {
        setDelay(DECIDING_DELAY);
        if (getOwnBuilding(BuildingBeekeeper.class).shouldHarvestHoneycombs())
        {
            if (checkForToolOrWeapon(ToolType.SHEARS))
            {
                return getState();
            }
        }

        checkIfRequestForItemExistOrCreateAsynch(new ItemStack(Items.GLASS_BOTTLE));
        checkIfRequestForTagExistOrCreateAsynch(ItemTags.field_226159_I_, 16, FLOWERS);

        return DECIDE;
    }

    /**
     * Redirects the beekeeper to their building.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_WORKER_GOINGTOHUT));
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Decides what job the beekeeper should switch to, breeding or harvesting.
     *
     * @return The next {@link IAIState} the beekeeper should switch to, after executing this method.
     */
    private IAIState decideWhatToDo()
    {
        setDelay(DECIDING_DELAY);

        final Optional<BlockPos> hive = getOwnBuilding(BuildingBeekeeper.class)
                                          .getHives()
                                          .stream()
                                          .filter(pos -> BeehiveTileEntity.func_226964_a_(world.getBlockState(pos)) >= 5)
                                          .findFirst();
        if (hive.isPresent())
        {
            return BEEKEEPER_HARVEST;
        }

        final List<BeeEntity> bees = new ArrayList<>(searchForAnimals());

        if (bees.isEmpty())
        {
            setDelay(NO_ANIMALS_DELAY);
            return DECIDE;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_DECIDING));

        final int breedableAnimals = (int) bees.stream()
                                             .filter(animal -> animal.getGrowingAge() == 0)
                                             .count();

        final boolean hasBreedingItem =
          InventoryUtils.hasItemInItemHandler((worker.getInventoryCitizen()),
            (ItemStack stack) -> stack.getItem().isIn(ItemTags.field_226159_I_));

        if (!hasMaxAnimals(bees) && breedableAnimals >= NUM_OF_ANIMALS_TO_BREED && hasBreedingItem)
        {
            return HERDER_BREED;
        }

        return START_WORKING;
    }

    /**
     * Breed some animals together.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState breedAnimals()
    {
        setDelay(BREEDING_DELAY);

        final List<BeeEntity> animals = searchForAnimals();

        final AnimalEntity animalOne = animals
                                         .stream()
                                         .filter(animal -> !animal.isChild())
                                         .findAny()
                                         .orElse(null);

        if (animalOne == null)
        {
            return DECIDE;
        }

        final AnimalEntity animalTwo = animals.stream().filter(animal ->
          {
              final float range = animal.getDistance(animalOne);
              final boolean isAnimalOne = animalOne.equals(animal);
              return animal.getGrowingAge() == 0 && range <= DISTANCE_TO_BREED && !isAnimalOne;
          }
        ).findAny().orElse(null);

        if (animalTwo == null)
        {
            return DECIDE;
        }

        if (!equipBreedItem(Hand.MAIN_HAND))
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BREEDING));

        breedTwoAnimals(animalOne, animalTwo);
        incrementActionsDoneAndDecSaturation();
        worker.getCitizenExperienceHandler().addExperience(1.0);
        return DECIDE;
    }

    /**
     * Haverst honey/honeycomb from full beehives.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState harvestHoney()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_STATUS_BEEKEEPER_HARVESTING));

        final List<BlockPos> hives = getOwnBuilding(BuildingBeekeeper.class)
                                 .getHives()
                                 .stream()
                                 .filter(pos -> BeehiveTileEntity.func_226964_a_(world.getBlockState(pos)) >= 5)
                                 .collect(Collectors.toList());

        if (hives.isEmpty())
        {
            return DECIDE;
        }
        if (getOwnBuilding(BuildingBeekeeper.class).shouldHarvestHoneycombs())
        {
            if (!equipTool(Hand.MAIN_HAND, ToolType.SHEARS))
            {
                return PREPARING;
            }
        }
        else
        {
            if (!equipItem(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE)))
            {
                return PREPARING;
            }
        }
        final BlockPos hive = hives.get(0);
        if (walkToBlock(hive))
        {
            return getState();
        }
        worker.swingArm(Hand.MAIN_HAND);
        final ItemStack itemStack = worker.getHeldItemMainhand();
        if (getOwnBuilding(BuildingBeekeeper.class).shouldHarvestHoneycombs())
        {
            if (ItemStackUtils.isTool(itemStack, ToolType.SHEARS))
            {
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(new ItemStack(Items.HONEYCOMB, getHoneycombsPerHarvest()), worker.getItemHandlerCitizen());
                world.setBlockState(hive, world.getBlockState(hive).with(BlockStateProperties.field_227036_ao_, 0));
                worker.getCitizenExperienceHandler().addExperience(EXP_PER_HARVEST);
            }
        }
        else
        {
            if (itemStack.getItem() == Items.GLASS_BOTTLE)
            {
                int i;
                for (i = 0; i < getHoneyBottlesPerHarvest() && !itemStack.isEmpty(); i++)
                {
                    itemStack.shrink(1);
                }
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(new ItemStack(Items.HONEY_BOTTLE, i), worker.getItemHandlerCitizen());
                world.setBlockState(hive, world.getBlockState(hive).with(BlockStateProperties.field_227036_ao_, 0));
                worker.getCitizenExperienceHandler().addExperience(EXP_PER_HARVEST);
            }
        }
        worker.getCitizenExperienceHandler().addExperience(1.0);
        incrementActionsDoneAndDecSaturation();

        return START_WORKING;
    }

    /**
     * Lets the herder walk to the animal.
     *
     * @param animal the animal to walk to.
     * @return true if the herder is walking to the animal.
     */
    public boolean walkingToAnimal(final AnimalEntity animal)
    {
        if (animal != null)
        {
            worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_GOINGTOANIMAL));
            return walkToBlock(animal.getPosition());
        }
        else
        {
            return false;
        }
    }

    /**
     * Breed two animals together!
     *
     * @param animalOne the first {@link AnimalEntity} to breed.
     * @param animalTwo the second {@link AnimalEntity} to breed.
     */
    private void breedTwoAnimals(final AnimalEntity animalOne, final AnimalEntity animalTwo)
    {
        final List<AnimalEntity> animalsToBreed = new ArrayList<>();
        animalsToBreed.add(animalOne);
        animalsToBreed.add(animalTwo);

        for (final AnimalEntity animal : animalsToBreed)
        {
            if (!animal.isInLove() && !walkingToAnimal(animal))
            {
                animal.setInLove(null);
                worker.swingArm(Hand.MAIN_HAND);
                InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), worker.getHeldItemMainhand());
            }
        }
    }

    /**
     * Sets the tool as held item.
     *
     * @param toolType the {@link ToolType} we want to equip
     * @param hand     the hand to equip it in.
     * @return true if the tool was equipped.
     */
    public boolean equipTool(final Hand hand, final ToolType toolType)
    {
        if (getToolSlot(toolType) != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(hand, getToolSlot(toolType));
            return true;
        }
        return false;
    }

    /**
     * Gets the slot in which the Tool is in.
     *
     * @param toolType this herders tool type.
     * @return slot number.
     */
    private int getToolSlot(final ToolType toolType)
    {
        if (getOwnBuilding() != null)
        {
            final int slot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), toolType,
              TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());

            if (slot == -1)
            {
                checkForToolOrWeapon(toolType);
            }
            return slot;
        }
        return -1;
    }

    /**
     * Sets the {@link ItemStack} as held item or returns false.
     *
     * @param itemStack the {@link ItemStack} to equip.
     * @param hand      the hand to equip it in.
     * @return true if the item was equipped.
     */
    public boolean equipItem(final Hand hand, final ItemStack itemStack)
    {
        if (checkIfRequestForItemExistOrCreateAsynch(itemStack))
        {
            worker.getCitizenItemHandler().setHeldItem(hand, getItemSlot(itemStack.getItem()));
            return true;
        }
        return false;
    }

    /**
     * Sets the {@link ItemStack} as held item or returns false.
     *
     * @param hand the hand to equip it in.
     * @return true if the item was equipped.
     */
    public boolean equipBreedItem(final Hand hand)
    {
        if (checkIfRequestForTagExistOrCreateAsynch(ItemTags.field_226159_I_, 2, FLOWERS))
        {
            worker.getCitizenItemHandler()
              .setHeldItem(hand, InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), stack -> stack.getItem().isIn(ItemTags.field_226159_I_)));
            return true;
        }
        return false;
    }

    /**
     * Gets the slot in which the inserted item is in. (if any).
     *
     * @param item The {@link Item} to check for.
     * @return slot number -1 if not in INV.
     */
    public int getItemSlot(final Item item)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), item);
    }

    /**
     * Find animals in area.
     *
     * @return the {@link List} of animals in the area.
     */
    public List<BeeEntity> searchForAnimals()
    {
        final BuildingBeekeeper ownBuilding = getOwnBuilding(BuildingBeekeeper.class);
        if (ownBuilding == null)
        {
            return new ArrayList<>();
        }
        return ownBuilding
                 .getHives()
                 .stream()
                 .map(AxisAlignedBB::new)
                 .map(aabb -> aabb.grow(HIVE_BEE_RADIUS))
                 .map(aabb -> world.getEntitiesWithinAABB(BeeEntity.class, aabb))
                 .flatMap(Collection::stream)
                 .collect(Collectors.toList());
    }

    private int getHoneyBottlesPerHarvest()
    {
        return 1;
    }

    private int getHoneycombsPerHarvest()
    {
        return 3;
    }
}
