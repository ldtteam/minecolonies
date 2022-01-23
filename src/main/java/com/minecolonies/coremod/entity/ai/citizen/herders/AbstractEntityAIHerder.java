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
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Abstract class for all Citizen Herder AIs
 */
public abstract class AbstractEntityAIHerder<J extends AbstractJob<?, J>, B extends AbstractBuilding, T extends AnimalEntity> extends AbstractEntityAIInteract<J, B>
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
        if (getOwnBuilding().getSetting(AbstractBuilding.BREEDING).getValue() ||
                getOwnBuilding().getSetting(AbstractBuilding.FEEDING).getValue())
        {
            list.add(getRequestBreedingItems());
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

        final List<T> animals = new ArrayList<>(searchForAnimals());

        if (animals.isEmpty())
        {
            return DECIDE;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_DECIDING));

        int numOfBreedableAnimals = 0;
        int numOfFeedableAnimals = 0;
        for (final AnimalEntity entity : animals)
        {
            if (isBreedAble(entity))
            {
                numOfBreedableAnimals++;
            }
            else if (MAX_ENTITY_AGE / entity.getAge() <= 1 + getSecondarySkillLevel()/100.0)
            {
                numOfFeedableAnimals++;
            }
        }

        final boolean hasBreedingItem =
          InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()),
            (ItemStack stack) -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, getBreedingItem())) > 1;

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

    /**
     * Checks if we can breed this entity
     *
     * @param entity to check
     * @return true if breed able
     */
    protected boolean isBreedAble(final AnimalEntity entity)
    {
        return entity.getAge() == 0 && entity.canFallInLove();
    }

    /**
     * Whether or not this one can feed adults to breed children.
     * @return true if so.
     */
    protected boolean canBreedChildren()
    {
        return getOwnBuilding().getSetting(AbstractBuilding.BREEDING).getValue();
    }

    /**
     * Whether or not this one can feed children to speed up growth.
     * @return true if so.
     */
    protected boolean canFeedChildren()
    {
        return getOwnBuilding().getSetting(AbstractBuilding.FEEDING).getValue() &&
                getSecondarySkillLevel() >= LIMIT_TO_FEED_CHILDREN;
    }

    /**
     * Redirects the herder to their building.
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
     * Prepares the herder for herding
     *
     * @return The next {@link IAIState}.
     */
    private IAIState prepareForHerding()
    {
        for (final ToolType tool : getExtraToolsNeeded())
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
        }

        if (getOwnBuilding().getSetting(AbstractBuilding.BREEDING).getValue() ||
                getOwnBuilding().getSetting(AbstractBuilding.FEEDING).getValue())
        {
            final ItemStack breedingItem = getBreedingItem();
            checkIfRequestForItemExistOrCreateAsynch(breedingItem, breedingItem.getMaxStackSize(), breedingItem.getCount());
        }

        for (final ItemStack item : getExtraItemsNeeded())
        {
            checkIfRequestForItemExistOrCreateAsynch(item);
        }

        return DECIDE;
    }

    /**
     * Butcher some animals (Preferably Adults & not recently fed) that the herder looks after.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState butcherAnimals()
    {
        final List<T> animals = new ArrayList<>(searchForAnimals());

        if (!maxAnimals(animals))
        {
            return DECIDE;
        }

        if (!equipTool(Hand.MAIN_HAND, ToolType.AXE))
        {
            return START_WORKING;
        }

        final AnimalEntity animal = animals
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
        final List<T> animals = searchForAnimals();

        final AnimalEntity animalOne = animals
                                         .stream()
                                         .filter(this::isBreedAble)
                                         .findAny()
                                         .orElse(null);

        if (animalOne == null)
        {
            return DECIDE;
        }

        final AnimalEntity animalTwo = animals.stream().filter(animal ->
          {
              final float range = animal.distanceTo(animalOne);
              final boolean isAnimalOne = animalOne.equals(animal);
              return isBreedAble(animal) && range <= DISTANCE_TO_BREED && !isAnimalOne;
          }
        ).findAny().orElse(null);

        if (animalTwo == null)
        {
            return DECIDE;
        }

        if (!equipItem(Hand.MAIN_HAND, getBreedingItem()))
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BREEDING));

        breedTwoAnimals(animalOne, animalTwo);
        worker.decreaseSaturationForContinuousAction();
        return DECIDE;
    }

    /**
     * Breed some animals together.
     *
     * @return The next {@link IAIState}.
     */
    protected IAIState feedAnimals()
    {
        final List<T> animals = searchForAnimals();

        final AnimalEntity animalOne = animals
                                         .stream()
                                         .filter(entity -> entity.isBaby() && MAX_ENTITY_AGE / entity.getAge() <= 1 + getSecondarySkillLevel()/100.0)
                                         .findAny()
                                         .orElse(null);

        if (animalOne == null)
        {
            return DECIDE;
        }

        if (!equipItem(Hand.MAIN_HAND, getBreedingItem()))
        {
            return START_WORKING;
        }

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_FEEDING));

        if (!walkingToAnimal(animalOne))
        {
            // Values taken from vanilla.
            animalOne.ageUp((int)((float)(-animalOne.getAge() / 20) * 0.1F), true);
            worker.swing(Hand.MAIN_HAND);
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), getBreedingItem());
            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            animalOne.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
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
        final List<ItemEntity> items = searchForItemsInArea();

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
     * @return the {@link List} of animals in the area.
     */
    public List<T> searchForAnimals()
    {
        return WorldUtil.getEntitiesWithinBuilding(world, getAnimalClass(), getOwnBuilding(), null);
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
    public List<ItemEntity> searchForItemsInArea()
    {
        return WorldUtil.getEntitiesWithinBuilding(world, ItemEntity.class, getOwnBuilding(), null);
    }

    /**
     * Get the Animal's class from the none Abstract.
     *
     * @return the class of the animal to work with.
     */
    public abstract Class<T> getAnimalClass();

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
            return walkToBlock(new BlockPos(animal.position()));
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
                worker.swing(Hand.MAIN_HAND);
                InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), getBreedingItem());
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
    public boolean maxAnimals(final List<T> allAnimals)
    {
        final List<T> animals = allAnimals.stream()
                                  .filter(animalToButcher -> !animalToButcher.isBaby()).collect(Collectors.toList());
        if (animals.isEmpty())
        {
            return false;
        }

        final int numOfAnimals = animals.size();
        final int maxAnimals = getOwnBuilding().getBuildingLevel() * getMaxAnimalMultiplier();

        return numOfAnimals > maxAnimals;
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
        final int slot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), toolType,
          TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());

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
     * @param animal the {@link AnimalEntity} we are butchering
     */
    protected void butcherAnimal(@Nullable final AnimalEntity animal)
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BUTCHERING));
        if (animal != null && !walkingToAnimal(animal) && !ItemStackUtils.isEmpty(worker.getMainHandItem()))
        {
            worker.swing(Hand.MAIN_HAND);
            final FakePlayer fp = FakePlayerFactory.getMinecraft((ServerWorld) worker.getCommandSenderWorld());
            final DamageSource ds = DamageSource.playerAttack(fp);
            animal.hurt(ds, (float) getButcheringAttackDamage());
            worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
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
     * @return the BreedingItem stack.
     */
    public ItemStack getRequestBreedingItems()
    {
        final ItemStack breedingItem = getBreedingItem().copy();
        ItemStackUtils.setSize(breedingItem, breedingItem.getCount() * 8); // means that we can breed 8 animals before requesting again.
        return breedingItem;
    }

    /**
     * Get breeding item for animal.
     *
     * @return the {@link Item} needed for breeding the animal.
     */
    public abstract ItemStack getBreedingItem();
}
