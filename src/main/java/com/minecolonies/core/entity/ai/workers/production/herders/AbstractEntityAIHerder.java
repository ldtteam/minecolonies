package com.minecolonies.core.entity.ai.workers.production.herders;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_USED;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

/**
 * Abstract class for all Citizen Herder AIs
 */
public abstract class AbstractEntityAIHerder<J extends AbstractJob<?, J>, B extends AbstractBuilding> extends AbstractEntityAIInteract<J, B>
{
    /**
     * How many animals per hut level the worker should max have.
     */
    private static final int ANIMAL_MULTIPLIER = 2;

    /**
     * Amount of animals needed to bread.
     */
    private static final int NUM_OF_ANIMALS_TO_BREED = 2;

    /**
     * Request this many sets of breeding items at once, to reduce courier workload.
     */
    private static final int EXTRA_BREEDING_ITEMS_REQUEST = 8;

    /**
     * Butchering attack damage.
     */
    protected static final int BUTCHERING_ATTACK_DAMAGE = 5;

    /**
     * Distance two animals need to be inside to breed.
     */
    private static final int DISTANCE_TO_BREED = 10;

    /**
     * Delays used to setDelay()
     */
    private static final int BUTCHER_DELAY  = 20;
    private static final int DECIDING_DELAY = 80;
    private static final int BREEDING_DELAY = 40;

    /**
     * Level limit to feed children.
     */
    public static final int LIMIT_TO_FEED_CHILDREN = 10;

    /**
     * Number of actions needed to dump inventory.
     */
    private static final int ACTIONS_FOR_DUMP = 10;

    /**
     * New born age.
     */
    private static final double MAX_ENTITY_AGE = AgeableMob.BABY_START_AGE;

    /**
     * Xp per action, like breed feed butcher
     */
    protected static final double XP_PER_ACTION = 0.5;

    /**
     * Chance to feed one animal
     */
    private static final double FEED_CHANCE = 0.1;

    /**
     * The current herding module we're working on
     */
    @Nullable
    protected AnimalHerdingModule current_module;

    /**
     * Selected breeding partners
     */
    private final List<Animal> animalsToBreed = new ArrayList<>();

    /**
     * Recently fed animals
     */
    private final Map<UUID, Long> fedRecently = new HashMap<>();

    /**
     * Prevents retrying breeding too quickly if last attempt failed
     */
    private int breedTimeOut = 0;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill.
     */
    public AbstractEntityAIHerder(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(PREPARING, this::prepareForHerding, TICKS_SECOND),
          new AITarget(DECIDE, this::decideWhatToDo, DECIDING_DELAY),
          new AITarget(HERDER_BREED, this::breedAnimals, BREEDING_DELAY),
          new AITarget(HERDER_BUTCHER, this::butcherAnimals, BUTCHER_DELAY),
          new AITarget(HERDER_PICKUP, this::pickupItems, TICKS_SECOND),
          new AITarget(HERDER_FEED, this::feedAnimal, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_FOR_DUMP;
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        final List<ItemStack> list = super.itemsNiceToHave();
        for (final AnimalHerdingModule module : building.getModulesByType(AnimalHerdingModule.class))
        {
            list.addAll(getRequestBreedingItems(module));
        }
        return list;
    }

    /**
     * Get the extra tools needed for this job.
     *
     * @return a list of tools or empty.
     */
    @NotNull
    public List<EquipmentTypeEntry> getExtraToolsNeeded()
    {
        final List<EquipmentTypeEntry> toolsNeeded = new ArrayList<>();
        toolsNeeded.add(ModEquipmentTypes.axe.get());
        return toolsNeeded;
    }

    /**
     * Get the extra items needed for this job.
     *
     * @return a list of items needed or empty.
     */
    @NotNull
    public List<ItemStack> getExtraItemsNeeded()
    {
        return new ArrayList<>();
    }

    /**
     * Decides what job the herder should switch to, breeding or Butchering.
     *
     * @return The next {@link IAIState} the herder should switch to, after executing this method.
     */
    public IAIState decideWhatToDo()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        if (breedTimeOut > 0)
        {
            breedTimeOut -= DECIDING_DELAY;
        }

        for (final AnimalHerdingModule module : building.getModulesByType(AnimalHerdingModule.class))
        {
            final List<? extends Animal> animals = searchForAnimals(module::isCompatible);
            if (animals.isEmpty())
            {
                continue;
            }

            current_module = module;

            int numOfBreedableAnimals = 0;
            for (final Animal entity : animals)
            {
                if (isBreedAble(entity))
                {
                    numOfBreedableAnimals++;
                }
            }

            final boolean hasBreedingItem =
              InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()),
                (ItemStack stack) -> ItemStackUtils.compareItemStackListIgnoreStackSize(module.getBreedingItems(), stack)) > 1;

            if (ColonyConstants.rand.nextDouble() < 0.1 && !searchForItemsInArea().isEmpty())
            {
                return HERDER_PICKUP;
            }
            else if (ColonyConstants.rand.nextDouble() < chanceToButcher(animals))
            {
                return HERDER_BUTCHER;
            }
            else if (canBreedChildren() && numOfBreedableAnimals >= NUM_OF_ANIMALS_TO_BREED && hasBreedingItem && breedTimeOut == 0)
            {
                return HERDER_BREED;
            }
            else if (ColonyConstants.rand.nextDouble() < FEED_CHANCE && hasBreedingItem)
            {
                return HERDER_FEED;
            }
        }

        return START_WORKING;
    }

    /**
     * Checks if we can breed this entity
     *
     * @param entity to check
     * @return true if breed able
     */
    protected static boolean isBreedAble(final Animal entity)
    {
        return entity.getAge() == 0 && (entity.isInLove() || entity.canFallInLove());
    }

    /**
     * Checks if we can feed this entity
     *
     * @param entity to check
     * @return true if feed able
     */
    protected boolean isFeedAble(final Animal entity)
    {
        return entity.isBaby() && MAX_ENTITY_AGE / entity.getAge() <= 1 + getSecondarySkillLevel() / 100.0;
    }

    /**
     * Whether or not this one can feed adults to breed children.
     *
     * @return true if so.
     */
    protected boolean canBreedChildren()
    {
        return building.getSetting(AbstractBuilding.BREEDING).getValue();
    }

    /**
     * Redirects the herder to their building.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the herder for herding
     *
     * @return The next {@link IAIState}.
     */
    private IAIState prepareForHerding()
    {
        if (current_module == null)
        {
            return DECIDE;
        }

        for (final EquipmentTypeEntry tool : getExtraToolsNeeded())
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
        }

        for (final ItemStack breedingItem : current_module.getBreedingItems())
        {
            checkIfRequestForItemExistOrCreateAsync(breedingItem, breedingItem.getCount() * EXTRA_BREEDING_ITEMS_REQUEST, breedingItem.getCount());
        }

        for (final ItemStack item : getExtraItemsNeeded())
        {
            checkIfRequestForItemExistOrCreateAsync(item);
        }

        return DECIDE;
    }

    /**
     * Butcher some animals (Preferably Adults and not recently fed) that the herder looks after.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState butcherAnimals()
    {
        if (current_module == null)
        {
            return DECIDE;
        }

        final List<? extends Animal> animals = searchForAnimals(current_module::isCompatible);

        if (!equipTool(InteractionHand.MAIN_HAND, ModEquipmentTypes.axe.get()))
        {
            return START_WORKING;
        }

        if (animals.isEmpty())
        {
            return DECIDE;
        }

        final BlockPos center = getCenterOfHerd(animals);

        // Butcher furthest animal
        animals.sort(Comparator.<Animal>comparingDouble(an -> an.blockPosition().distSqr(center)).reversed());

        Animal toKill = null;

        for (final Animal entity : animals)
        {
            if (!entity.isBaby() && !entity.isInLove())
            {
                if (toKill == null || !entity.level.canSeeSky(entity.blockPosition()) && toKill.level.canSeeSky(toKill.blockPosition()))
                {
                    toKill = entity;
                }
            }
        }

        if (toKill == null)
        {
            return DECIDE;
        }

        butcherAnimal(toKill);

        if (!toKill.isAlive())
        {
            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            incrementActionsDoneAndDecSaturation();
            fedRecently.remove(toKill.getUUID());
            return DECIDE;
        }

        return HERDER_BUTCHER;
    }

    /**
     * Calculates the center of a herd
     *
     * @param animals
     * @return blockpos center
     */
    private BlockPos getCenterOfHerd(final List<? extends Animal> animals)
    {
        if (animals.isEmpty())
        {
            return BlockPos.ZERO;
        }

        Vec3 avg = new Vec3(0, 0, 0);
        for (final Animal animal : animals)
        {
            avg = avg.add(animal.position());
        }

        return BlockPos.containing(avg.multiply(1.0 / animals.size(), 1.0 / animals.size(), 1.0 / animals.size()));
    }

    /**
     * Breed some animals together.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState breedAnimals()
    {
        if (current_module == null)
        {
            CitizenItemUtils.removeHeldItem(worker);
            return DECIDE;
        }

        if (breedTwoAnimals())
        {
            return getState();
        }

        final Predicate<Animal> predicate = ((Predicate<Animal>) current_module::isCompatible).and(AbstractEntityAIHerder::isBreedAble);
        final List<? extends Animal> breedables = searchForAnimals(predicate);

        if (breedables.size() < 2)
        {
            CitizenItemUtils.removeHeldItem(worker);
            breedTimeOut = TICKS_SECOND * 60;
            return DECIDE;
        }

        final BlockPos center = getCenterOfHerd(breedables);

        // Breed closest animal
        breedables.sort(Comparator.<Animal>comparingDouble(an -> an.blockPosition().distSqr(center)));

        final Animal animalOne = breedables.remove(0);

        Animal animalTwo = null;
        for (final Animal animal : breedables)
        {
            if (animal.distanceTo(animalOne) <= DISTANCE_TO_BREED && canMate(animalOne, animal))
            {
                animalTwo = animal;
                break;
            }
        }

        if (animalTwo == null)
        {
            CitizenItemUtils.removeHeldItem(worker);
            breedTimeOut = TICKS_SECOND * 20;
            return DECIDE;
        }

        if (!equipItem(InteractionHand.MAIN_HAND, current_module.getBreedingItems()))
        {
            CitizenItemUtils.removeHeldItem(worker);
            return START_WORKING;
        }


        animalsToBreed.add(animalOne);
        animalsToBreed.add(animalTwo);

        if (breedTwoAnimals())
        {
            return getState();
        }

        breedTimeOut = TICKS_SECOND * 60;
        CitizenItemUtils.removeHeldItem(worker);
        return IDLE;
    }

    /**
     * Helper to check if two animals can mate
     *
     * @param first
     * @param second
     * @return
     */
    private static boolean canMate(final Animal first, final Animal second)
    {
        if (!isBreedAble(first) || !isBreedAble(second))
        {
            return false;
        }

        final int oldloveFirst = first.getInLoveTime();
        final int oldloveSecond = second.getInLoveTime();

        first.setInLoveTime(5);
        second.setInLoveTime(5);
        final boolean result = first.canMate(second);
        first.setInLoveTime(oldloveFirst);
        second.setInLoveTime(oldloveSecond);

        return result;
    }

    /**
     * Breed some animals together.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState feedAnimal()
    {
        if (current_module == null)
        {
            return DECIDE;
        }

        if (!equipItem(InteractionHand.MAIN_HAND, current_module.getBreedingItems()))
        {
            return START_WORKING;
        }

        List<? extends Animal> animals = searchForAnimals(current_module::isCompatible);
        Animal toFeed = null;

        for (final Animal animal : animals)
        {
            if (worker.level.getGameTime() - fedRecently.getOrDefault(animal.getUUID(), 0L) > TICKS_SECOND * 60 * 5)
            {
                toFeed = animal;
                break;
            }
        }

        if (toFeed == null)
        {
            return DECIDE;
        }

        if (!walkingToAnimal(toFeed))
        {
            if (toFeed.isBaby() && getSecondarySkillLevel() >= LIMIT_TO_FEED_CHILDREN)
            {
                toFeed.ageUp((int) ((float) (-toFeed.getAge() / TICKS_SECOND) * 0.1F), true);
            }

            // Values taken from vanilla.
            worker.swing(InteractionHand.MAIN_HAND);
            building.getModule(STATS_MODULE).increment(ITEM_USED + ";" + worker.getMainHandItem().getItem().getDescriptionId());
            worker.getMainHandItem().shrink(1);
            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            worker.level.broadcastEntityEvent(toFeed, (byte) 18);
            toFeed.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            CitizenItemUtils.removeHeldItem(worker);
            fedRecently.put(toFeed.getUUID(), worker.level.getGameTime());

            return DECIDE;
        }

        worker.decreaseSaturationForContinuousAction();
        return getState();
    }

    /**
     * Allows the worker to pickup any stray items around Hut. Specifically useful when he possibly leaves Butchered drops OR with chickens (that drop feathers and etc)!
     *
     * @return The next {@link IAIState}.
     */
    private IAIState pickupItems()
    {
        final List<? extends ItemEntity> items = searchForItemsInArea();

        if (!items.isEmpty() && walkToBlock(items.get(0).blockPosition(), 1))
        {
            return getState();
        }

        incrementActionsDoneAndDecSaturation();

        return DECIDE;
    }

    /**
     * Find animals in area.
     *
     * @param predicate true if the animal is interesting.
     * @return a {@link Stream} of animals in the area.
     */
    public List<? extends Animal> searchForAnimals(final Predicate<Animal> predicate)
    {
        return WorldUtil.getEntitiesWithinBuilding(world, Animal.class, building, predicate);
    }

    public int getMaxAnimalMultiplier()
    {
        return ANIMAL_MULTIPLIER;
    }

    /**
     * Find items in hut area.
     *
     * @return the {@link List} of {@link ItemEntity} in the area.
     */
    public List<? extends ItemEntity> searchForItemsInArea()
    {
        return WorldUtil.getEntitiesWithinBuilding(world, ItemEntity.class, building, null);
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
     * Breed two animals together!
     *
     * @return true if still working on it
     */
    private boolean breedTwoAnimals()
    {
        for (final Iterator<Animal> it = animalsToBreed.iterator(); it.hasNext(); )
        {
            final Animal animal = it.next();
            if (animal.isInLove() || animal.isDeadOrDying())
            {
                it.remove();
            }
            else if (walkingToAnimal(animal))
            {
                break;
            }
            else
            {
                animal.setInLove(null);
                worker.swing(InteractionHand.MAIN_HAND);
                building.getModule(STATS_MODULE).increment(ITEM_USED + ";" + worker.getMainHandItem().getItem().getDescriptionId());
                worker.getMainHandItem().shrink(1);
                worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
                worker.decreaseSaturationForAction();
                it.remove();
            }
        }
        return !animalsToBreed.isEmpty();
    }

    /**
     * Returns true if animals list is above max. Returns false if animals list is within max.
     *
     * @param allAnimals the list of animals.
     * @return chance to butcher an animal
     */
    public double chanceToButcher(final List<? extends Animal> allAnimals)
    {
        final int maxAnimals = building.getBuildingLevel() * getMaxAnimalMultiplier();
        if (!building.getSetting(AbstractBuilding.BREEDING).getValue() && allAnimals.size() <= maxAnimals)
        {
            return 0;
        }

        int grownUp = 0;
        for (Animal animalToButcher : allAnimals)
        {
            if (!animalToButcher.isBaby())
            {
                grownUp++;
            }
        }

        if (grownUp <= 3)
        {
            return 0;
        }

        return 0.5 * (Math.pow(grownUp, 4) / Math.pow(maxAnimals, 4));
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
     * @param itemStacks the list of {@link ItemStack}s to equip one of.
     * @param hand       the hand to equip it in.
     * @return true if the item was equipped.
     */
    public boolean equipItem(final InteractionHand hand, final List<ItemStack> itemStacks)
    {
        for (final ItemStack itemStack : itemStacks)
        {
            if (checkIfRequestForItemExistOrCreateAsync(itemStack))
            {
                CitizenItemUtils.setHeldItem(worker, hand, getItemSlot(itemStack.getItem()));
                return true;
            }
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
     * Butcher an animal.
     *
     * @param animal the {@link Animal} we are butchering
     */
    protected void butcherAnimal(@Nullable final Animal animal)
    {
        if (animal != null && !walkingToAnimal(animal) && !ItemStackUtils.isEmpty(worker.getMainHandItem()))
        {
            worker.swing(InteractionHand.MAIN_HAND);
            final DamageSource ds = animal.level.damageSources().playerAttack(getFakePlayer());
            animal.hurt(ds, (float) getButcheringAttackDamage());
            CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);
        }
    }

    /**
     * Get the attack damage to be used.
     *
     * @return the attack damage.
     */
    public double getButcheringAttackDamage()
    {
        return BUTCHERING_ATTACK_DAMAGE;
    }

    /**
     * Gets an ItemStack of breedingItem for requesting, requests multiple items to decrease work for delivery man
     *
     * @param module the herding module.
     * @return the BreedingItem stacks.
     */
    public List<ItemStack> getRequestBreedingItems(final AnimalHerdingModule module)
    {
        final List<ItemStack> breedingItems = new ArrayList<>();

        // TODO: currently this will request some of all items, when really we should be happy with enough of *any* of
        //       these items ... but right now it doesn't matter anyway since these are currently all single item lists.
        for (final ItemStack stack : module.getBreedingItems())
        {
            final ItemStack requestable = stack.copy();
            ItemStackUtils.setSize(requestable, stack.getCount() * EXTRA_BREEDING_ITEMS_REQUEST);
            breedingItems.add(requestable);
        }

        return breedingItems;
    }
}
