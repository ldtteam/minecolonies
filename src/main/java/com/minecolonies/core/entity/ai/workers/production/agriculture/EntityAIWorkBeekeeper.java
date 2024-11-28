package com.minecolonies.core.entity.ai.workers.production.agriculture;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobBeekeeper;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Beekeeper AI class.
 */
public class EntityAIWorkBeekeeper extends AbstractEntityAIInteract<JobBeekeeper, BuildingBeekeeper>
{
    /**
     * Amount of animals needed to bread.
     */
    private static final int NUM_OF_ANIMALS_TO_BREED = 2;
    private static final int BEES_PER_LEVEL          = 3;

    /**
     * How many flowers there are required to start breeding.
     */
    private static final int NUM_OF_FLOWERS_TO_BREED = 2;

    /**
     * How many flowers the beekeeper would like to have in stock.
     */
    private static final int NUM_OF_WANTED_FLOWERS = 16;

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
    private static final int NO_HIVES_DELAY   = 100;
    private static final int NO_FLOWERS_DELAY = 100;
    private static final int BREEDING_DELAY   = 40;

    /**
     * If true, last harvest contained a honey bottle
     */
    private boolean lastHarvestedBottle = false;

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

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 5;
    }

    /**
     * Checks the beehives/beenests and their surroundings if the maximum number of bees is reached
     *
     * @param allBees all bees in the area around the beehives/beenests
     * @return true if the maximum nuber of bees is reached els false
     */
    private boolean hasMaxAnimals(final List<Bee> allBees)
    {
        final int numOfBeesInHive = getBeesInHives();
        final int numOfAnimals = allBees.size();
        final int maxAnimals = building.getBuildingLevel() * BEES_PER_LEVEL;

        return (numOfAnimals + numOfBeesInHive) >= maxAnimals;
    }

    /**
     * Get the number of bees in assigned hives.
     *
     * @return the number of bees in assigned hives.
     */
    private int getBeesInHives()
    {
        return building
                 .getHives()
                 .stream()
                 .map(world::getBlockEntity)
                 .filter(Objects::nonNull)
                 .map(BeehiveBlockEntity.class::cast)
                 .mapToInt(BeehiveBlockEntity::getOccupantCount)
                 .sum();
    }

    /**
     * Prepares the beekeeper for herding
     *
     * @return The next {@link IAIState}.
     */
    private IAIState prepareForHerding()
    {
        setDelay(DECIDING_DELAY);
        if (!building.getHarvestTypes().equals(BuildingBeekeeper.HONEY))
        {
            if (checkForToolOrWeapon(ModEquipmentTypes.shears.get()))
            {
                return getState();
            }
        }

        if (!building.getHarvestTypes().equals(BuildingBeekeeper.HONEYCOMB))
        {
            checkIfRequestForItemExistOrCreateAsync(new ItemStack(Items.GLASS_BOTTLE));
        }

        return DECIDE;
    }

    /**
     * Redirects the beekeeper to their building.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            setDelay(2);
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
        setDelay(DECIDING_DELAY + (99 / getSecondarySkillLevel() - 1));

        final Set<BlockPos> hives = building.getHives();

        if (hives.isEmpty())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NO_HIVES), ChatPriority.BLOCKING));
            setDelay(NO_HIVES_DELAY);
            return DECIDE;
        }

        ItemListModule flowersModule = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST));
        if (flowersModule.getList().isEmpty() && building.getSetting(BuildingBeekeeper.BREEDING).getValue())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COM_MINECOLONIES_COREMOD_BEEKEEPER_NOFLOWERS), ChatPriority.BLOCKING));
            setDelay(NO_FLOWERS_DELAY);
            return DECIDE;
        }

        for (BlockPos pos : hives)
        {
            if (!(world.getBlockState(pos).getBlock() instanceof BeehiveBlock))
            {
                building.removeHive(pos);
            }
        }

        final Optional<BlockPos> hive = building.getHives()
                                          .stream()
                                          .filter(pos -> BeehiveBlockEntity.getHoneyLevel(world.getBlockState(pos)) >= 5)
                                          .findFirst();

        if (hive.isPresent())
        {
            return BEEKEEPER_HARVEST;
        }

        final List<Bee> bees = searchForAnimals(world, building);

        final JobBeekeeper job = worker.getCitizenJobHandler().getColonyJob(JobBeekeeper.class);
        if (bees.isEmpty())
        {
            if (getBeesInHives() <= 0)
            {
                job.tickNoBees();
                if (job.checkForBeeInteraction())
                {
                    worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NO_BEES), ChatPriority.BLOCKING));
                }
            }
            else
            {
                job.resetCounter();
            }
            setDelay(NO_ANIMALS_DELAY);
            return DECIDE;
        }
        else
        {
            job.resetCounter();
        }

        if (isReadyForBreeding())
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

        final List<Bee> animals = searchForAnimals(world, building);

        final Animal animalOne = animals
                                   .stream()
                                   .filter(animal -> !animal.isBaby())
                                   .findAny()
                                   .orElse(null);

        if (animalOne == null)
        {
            return DECIDE;
        }

        final Animal animalTwo = animals.stream().filter(animal ->
          {
              final float range = animal.distanceTo(animalOne);
              final boolean isAnimalOne = animalOne.equals(animal);
              return animal.getAge() == 0 && range <= DISTANCE_TO_BREED && !isAnimalOne;
          }
        ).findAny().orElse(null);

        if (animalTwo == null)
        {
            return DECIDE;
        }

        if (!equipBreedItem(InteractionHand.MAIN_HAND))
        {
            return START_WORKING;
        }

        breedTwoAnimals(animalOne, animalTwo);

        incrementActionsDoneAndDecSaturation();
        return DECIDE;
    }

    /**
     * Harvest honey/honeycomb from full beehives.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState harvestHoney()
    {
        final List<BlockPos> hives = building
                                       .getHives()
                                       .stream()
                                       .filter(pos -> BeehiveBlockEntity.getHoneyLevel(world.getBlockState(pos)) >= 5)
                                       .collect(Collectors.toList());

        if (hives.isEmpty())
        {
            return DECIDE;
        }
        if (building.getHarvestTypes().equals(BuildingBeekeeper.HONEYCOMB) || (building.getHarvestTypes().equals(BuildingBeekeeper.BOTH) && lastHarvestedBottle))
        {
            if (!equipTool(InteractionHand.MAIN_HAND, ModEquipmentTypes.shears.get()))
            {
                return PREPARING;
            }
        }
        else
        {
            if (!equipItem(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE)))
            {
                return PREPARING;
            }
        }
        final BlockPos hive = hives.get(0);
        if (!world.getBlockState(hive).is(BlockTags.BEEHIVES))
        {
            building.removeHive(hive);
            return PREPARING;
        }
        if (walkToBlock(hive))
        {
            return getState();
        }

        worker.swing(InteractionHand.MAIN_HAND);
        final ItemStack itemStack = worker.getMainHandItem();
        if (!building.getHarvestTypes().equals(BuildingBeekeeper.HONEY) && ModEquipmentTypes.shears.get().checkIsEquipment(itemStack))
        {
            CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);

            for (ItemStack stackItem : Compatibility.getCombsFromHive(hive, world, getHoneycombsPerHarvest()))
            {
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(stackItem, worker.getItemHandlerCitizen());
            }
            world.setBlockAndUpdate(hive, world.getBlockState(hive).setValue(BlockStateProperties.LEVEL_HONEY, 0));
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_HARVEST);
            lastHarvestedBottle = false;
        }
        else if (!building.getHarvestTypes().equals(BuildingBeekeeper.HONEYCOMB) && itemStack.getItem() == Items.GLASS_BOTTLE)
        {
            int i;
            for (i = 0; i < getHoneyBottlesPerHarvest() && !itemStack.isEmpty(); i++)
            {
                itemStack.shrink(1);
            }
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(new ItemStack(Items.HONEY_BOTTLE, i), worker.getItemHandlerCitizen());
            world.setBlockAndUpdate(hive, world.getBlockState(hive).setValue(BlockStateProperties.LEVEL_HONEY, 0));
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_HARVEST);
            lastHarvestedBottle = true;
        }

        final int dex = getPrimarySkillLevel();
        if ((50 - (dex / 99. * 50.)) / 100 > worker.getRandom().nextDouble())
        {
            final List<Entity> bees =
              ((BeehiveBlockEntity) world.getBlockEntity(hive)).releaseAllOccupants(world.getBlockState(hive), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            bees.stream()
              .filter(entity -> entity instanceof Bee)
              .map(entity -> (Bee) entity)
              .filter(bee -> worker.position().distanceToSqr(bee.position()) <= 16.0D)
              .forEach(bee -> {
                  bee.setRemainingPersistentAngerTime(400 + worker.getRandom().nextInt(400));
                  bee.setLastHurtByMob(worker);
              });
        }
        incrementActionsDoneAndDecSaturation();

        return START_WORKING;
    }

    /**
     * Lets the herder walk to the animal.
     *
     * @param animal the animal to walk to.
     * @return true if the herder is walking to the animal.
     */
    public boolean walkingToAnimal(final Animal animal)
    {
        if (animal != null)
        {
            return walkToBlock(animal.blockPosition());
        }
        else
        {
            return false;
        }
    }

    /**
     * Determines if the beekeeper is ready to start breeding.
     *
     * @return whether the beekeeper is ready to start breeding.
     */
    private boolean isReadyForBreeding()
    {
        if (!building.getSetting(BuildingBeekeeper.BREEDING).getValue())
        {
            return false;
        }

        final ItemListModule flowersModule = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST));
        final List<Bee> bees = searchForAnimals(world, building);

        final int breedableAnimals = (int) bees.stream().filter(animal -> animal.getAge() == 0).count();

        boolean canBreed = !hasMaxAnimals(bees) && breedableAnimals >= NUM_OF_ANIMALS_TO_BREED;
        if (canBreed)
        {
            int flowerCount = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), (stack) -> flowersModule.isItemInList(new ItemStorage(stack)))
                                + InventoryUtils.getCountFromBuilding(building, flowersModule.getList());

            if (flowerCount < NUM_OF_FLOWERS_TO_BREED && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)))
            {
                worker.getCitizenData().createRequestAsync(new StackList(flowersModule.getList().stream()
                                                                           .map(ItemStorage::getItemStack)
                                                                           .peek((stack) -> stack.setCount(NUM_OF_WANTED_FLOWERS))
                                                                           .collect(Collectors.toList()),
                  RequestSystemTranslationConstants.REQUEST_TYPE_FLOWERS,
                  NUM_OF_WANTED_FLOWERS,
                  NUM_OF_FLOWERS_TO_BREED));
                return false;
            }

            return flowerCount >= NUM_OF_FLOWERS_TO_BREED;
        }

        return false;
    }

    /**
     * Breed two animals together!
     *
     * @param animalOne the first {@link Animal} to breed.
     * @param animalTwo the second {@link Animal} to breed.
     */
    private void breedTwoAnimals(final Animal animalOne, final Animal animalTwo)
    {
        final List<Animal> animalsToBreed = new ArrayList<>();
        animalsToBreed.add(animalOne);
        animalsToBreed.add(animalTwo);

        for (final Animal animal : animalsToBreed)
        {
            if (!animal.isInLove() && !walkingToAnimal(animal))
            {
                animal.setInLove(null);
                worker.swing(InteractionHand.MAIN_HAND);
                InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), worker.getMainHandItem());
            }
        }
    }

    /**
     * Sets the tool as held item.
     *
     * @param toolType the {@link EquipmentTypeEntry} we want to equip
     * @param hand     the hand to equip it in.
     * @return true if the tool was equipped.
     */
    public boolean equipTool(final InteractionHand hand, final EquipmentTypeEntry toolType)
    {
        if (getToolSlot(toolType) != -1)
        {
            CitizenItemUtils.setHeldItem(worker, hand, getToolSlot(toolType));
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
    private int getToolSlot(final EquipmentTypeEntry toolType)
    {
        final int slot = InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), toolType,
          TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel());

        if (slot == -1)
        {
            checkForToolOrWeapon(toolType);
        }
        return slot;
    }

    /**
     * Sets the {@link ItemStack} as held item or returns false.
     *
     * @param itemStack the {@link ItemStack} to equip.
     * @param hand      the hand to equip it in.
     * @return true if the item was equipped.
     */
    public boolean equipItem(final InteractionHand hand, final ItemStack itemStack)
    {
        if (checkIfRequestForItemExistOrCreateAsync(itemStack))
        {
            CitizenItemUtils.setHeldItem(worker, hand, getItemSlot(itemStack.getItem()));
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
    public boolean equipBreedItem(final InteractionHand hand)
    {
        if (checkIfRequestForTagExistOrCreateAsync(ItemTags.FLOWERS, NUM_OF_FLOWERS_TO_BREED))
        {
            ItemListModule flowersModule = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(BUILDING_FLOWER_LIST));
            CitizenItemUtils
              .setHeldItem(worker, hand, InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), stack -> flowersModule.isItemInList(new ItemStorage(stack))));
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
    public static List<Bee> searchForAnimals(final Level world, final BuildingBeekeeper ownBuilding)
    {
        if (ownBuilding == null)
        {
            return new ArrayList<>();
        }
        return ownBuilding
                 .getHives()
                 .stream()
                 .map(AABB::new)
                 .map(aabb -> aabb.inflate(HIVE_BEE_RADIUS))
                 .map(aabb -> world.getEntitiesOfClass(Bee.class, aabb))
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

    /**
     * Can be overridden in implementations to return the exact building type the worker expects.
     *
     * @return the building type associated with this AI's worker.
     */
    @Override
    public Class<BuildingBeekeeper> getExpectedBuildingClass()
    {
        return BuildingBeekeeper.class;
    }
}
