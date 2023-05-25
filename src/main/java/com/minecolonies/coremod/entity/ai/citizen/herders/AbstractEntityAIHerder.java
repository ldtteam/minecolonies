package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

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
    private static final int BUTCHER_DELAY    = 20;
    private static final int DECIDING_DELAY   = 40;
    private static final int BREEDING_DELAY   = 40;

    /**
     * Level limit to feed children.
     */
    public static final int LIMIT_TO_FEED_CHILDREN = 10;

    /**
     * Number of actions needed to dump inventory.
     */
    private static final int ACTIONS_FOR_DUMP   = 10;

    /**
     * New born age.
     */
    private static final double MAX_ENTITY_AGE = -24000.0;

    /**
     * Xp per action, like breed feed butcher
     */
    protected static final double XP_PER_ACTION = 0.5;

    /**
     * The current herding module we're working on
     */
    @Nullable protected AnimalHerdingModule current_module;

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
          new AITarget(HERDER_FEED, this::feedAnimals, TICKS_SECOND)
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
        if (building.getSetting(AbstractBuilding.BREEDING).getValue() ||
                building.getSetting(AbstractBuilding.FEEDING).getValue())
        {
            for (final AnimalHerdingModule module : building.getModules(AnimalHerdingModule.class))
            {
                list.addAll(getRequestBreedingItems(module));
            }
        }
        return list;
    }

    /**
     * Get the extra tools needed for this job.
     *
     * @return a list of tools or empty.
     */
    @NotNull
    public List<ToolType> getExtraToolsNeeded()
    {
        final List<ToolType> toolsNeeded = new ArrayList<>();
        toolsNeeded.add(ToolType.AXE);
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

        for (final AnimalHerdingModule module : building.getModules(AnimalHerdingModule.class))
        {
            final List<? extends Animal> animals = searchForAnimals(module.getAnimalClass());
            if (animals.isEmpty())
            {
                continue;
            }

            current_module = module;
            worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_DECIDING));

            int numOfBreedableAnimals = 0;
            int numOfFeedableAnimals = 0;
            for (final Animal entity : animals)
            {
                if (isBreedAble(entity))
                {
                    numOfBreedableAnimals++;
                }
                else if (isFeedAble(entity))
                {
                    numOfFeedableAnimals++;
                }
            }

            final boolean hasBreedingItem =
                    InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()),
                            (ItemStack stack) -> ItemStackUtils.compareItemStackListIgnoreStackSize(module.getBreedingItems(), stack)) > 1;

            if (!searchForItemsInArea().isEmpty())
            {
                return HERDER_PICKUP;
            }
            else if (maxAnimals(animals))
            {
                return HERDER_BUTCHER;
            }
            else if (canBreedChildren() && numOfBreedableAnimals >= NUM_OF_ANIMALS_TO_BREED && hasBreedingItem)
            {
                return HERDER_BREED;
            }
            else if (canFeedChildren() && numOfFeedableAnimals > 0 && hasBreedingItem)
            {
                return HERDER_FEED;
            }
            return START_WORKING;
        }

        return DECIDE;
    }

    /**
     * Checks if we can breed this entity
     *
     * @param entity to check
     * @return true if breed able
     */
    protected boolean isBreedAble(final Animal entity)
    {
        return entity.getAge() == 0 && entity.canFallInLove();
    }

    /**
     * Checks if we can feed this entity
     *
     * @param entity to check
     * @return true if feed able
     */
    protected boolean isFeedAble(final Animal entity)
    {
        return entity.isBaby() && MAX_ENTITY_AGE / entity.getAge() <= 1 + getSecondarySkillLevel()/100.0;
    }

    /**
     * Whether or not this one can feed adults to breed children.
     * @return true if so.
     */
    protected boolean canBreedChildren()
    {
        return building.getSetting(AbstractBuilding.BREEDING).getValue();
    }

    /**
     * Whether or not this one can feed children to speed up growth.
     * @return true if so.
     */
    protected boolean canFeedChildren()
    {
        return building.getSetting(AbstractBuilding.FEEDING).getValue() &&
                getSecondarySkillLevel() >= LIMIT_TO_FEED_CHILDREN;
    }

    /**
     * Redirects the herder to their building.
     *
     * @return The next {@link IAIState}.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_WORKER_GOINGTOHUT));
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

        for (final ToolType tool : getExtraToolsNeeded())
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
        }

        if (building.getSetting(AbstractBuilding.BREEDING).getValue() ||
                building.getSetting(AbstractBuilding.FEEDING).getValue())
        {
            for (final ItemStack breedingItem : current_module.getBreedingItems())
            {
                checkIfRequestForItemExistOrCreateAsync(breedingItem, breedingItem.getMaxStackSize(), breedingItem.getCount());
            }
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

        final List<? extends Animal> animals = searchForAnimals(current_module.getAnimalClass());

        if (!maxAnimals(animals))
        {
            return DECIDE;
        }

        if (!equipTool(InteractionHand.MAIN_HAND, ToolType.AXE))
        {
            return START_WORKING;
        }

        final Animal animal = animals
                                      .stream()
                                      .filter(animalToButcher -> !animalToButcher.isBaby() && !animalToButcher.isInLove())
                                      .findFirst()
                                      .orElse(null);

        if (animal == null)
        {
            return DECIDE;
        }

        butcherAnimal(animal);

        if (!animal.isAlive())
        {
            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            incrementActionsDoneAndDecSaturation();
        }

        return HERDER_BUTCHER;
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
            return DECIDE;
        }

        final List<? extends Animal> breedables = searchForAnimals(current_module.getAnimalClass()).stream()
                .filter(this::isBreedAble).toList();

        final Animal animalOne = breedables.stream().findAny().orElse(null);

        if (animalOne == null)
        {
            return DECIDE;
        }

        final Animal animalTwo = breedables.stream().filter(animal ->
          {
              final float range = animal.distanceTo(animalOne);
              final boolean isAnimalOne = animalOne.equals(animal);
              return range <= DISTANCE_TO_BREED && !isAnimalOne;
          }
        ).findAny().orElse(null);

        if (animalTwo == null)
        {
            return DECIDE;
        }

        if (!equipItem(InteractionHand.MAIN_HAND, current_module.getBreedingItems()))
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BREEDING));

        breedTwoAnimals(animalOne, animalTwo);
        worker.decreaseSaturationForContinuousAction();
        worker.getCitizenItemHandler().removeHeldItem();
        return DECIDE;
    }

    /**
     * Breed some animals together.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState feedAnimals()
    {
        if (current_module == null)
        {
            return DECIDE;
        }

        final Animal animalOne = searchForAnimals(current_module.getAnimalClass()).stream()
                                         .filter(this::isFeedAble)
                                         .findAny()
                                         .orElse(null);

        if (animalOne == null)
        {
            return DECIDE;
        }

        if (!equipItem(InteractionHand.MAIN_HAND, current_module.getBreedingItems()))
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_FEEDING));

        if (!walkingToAnimal(animalOne))
        {
            // Values taken from vanilla.
            animalOne.ageUp((int)((float)(-animalOne.getAge() / 20) * 0.1F), true);
            worker.swing(InteractionHand.MAIN_HAND);
            worker.getMainHandItem().shrink(1);
            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            animalOne.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            worker.getCitizenItemHandler().removeHeldItem();
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

        if (!items.isEmpty() && walkToBlock(items.get(0).blockPosition()))
        {
            return getState();
        }

        incrementActionsDoneAndDecSaturation();

        return DECIDE;
    }

    /**
     * Find animals in area.
     *
     * @param clazz the animal class.
     * @return a {@link Stream} of animals in the area.
     */
    public <T extends Animal> List<? extends T> searchForAnimals(final Class<T> clazz)
    {
        return WorldUtil.getEntitiesWithinBuilding(world, clazz, building, null);
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
            worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_GOINGTOANIMAL));
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
                worker.getMainHandItem().shrink(1);
                worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            }
        }
    }

    /**
     * Returns true if animals list is above max. Returns false if animals list is within max.
     *
     * @param allAnimals the list of animals.
     * @return if amount of animals is over max.
     */
    public boolean maxAnimals(final List<? extends Animal> allAnimals)
    {
        final List<? extends Animal> animals = allAnimals.stream()
                .filter(animalToButcher -> !animalToButcher.isBaby()).toList();
        if (animals.isEmpty())
        {
            return false;
        }

        final int numOfAnimals = animals.size();
        final int maxAnimals = building.getBuildingLevel() * getMaxAnimalMultiplier();

        return numOfAnimals > maxAnimals;
    }

    /**
     * Sets the tool as held item.
     *
     * @param toolType the {@link ToolType} we want to equip
     * @param hand     the hand to equip it in.
     * @return true if the tool was equipped.
     */
    public boolean equipTool(final InteractionHand hand, final ToolType toolType)
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
        final int slot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), toolType,
          TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxToolLevel());

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
                worker.getCitizenItemHandler().setHeldItem(hand, getItemSlot(itemStack.getItem()));
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
        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BUTCHERING));
        if (animal != null && !walkingToAnimal(animal) && !ItemStackUtils.isEmpty(worker.getMainHandItem()))
        {
            worker.swing(InteractionHand.MAIN_HAND);
            final FakePlayer fp = FakePlayerFactory.getMinecraft((ServerLevel) worker.getCommandSenderWorld());
            final DamageSource ds = animal.level.damageSources().playerAttack(fp);
            animal.hurt(ds, (float) getButcheringAttackDamage());
            worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);
        }
    }

    /**
     * Get the attack damage to be used.
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
            ItemStackUtils.setSize(requestable, stack.getCount() * 8); // means that we can breed 8 animals before requesting again.
            breedingItems.add(requestable);
        }

        return breedingItems;
    }
}
