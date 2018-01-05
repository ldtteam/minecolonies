package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Abstract class for all Citizen Herder AIs
 */
public abstract class AbstractEntityAIHerder<J extends AbstractJob, T extends EntityAnimal> extends AbstractEntityAIInteract<J>
{

    /**
     * Tools and Items needed by the worker.
     */
    public final List<ToolType>  toolsNeeded = new ArrayList<>();
    public final List<ItemStack> itemsNeeded = new ArrayList<>();

    /**
     * Amount of animals needed to bread.
     */
    public static final int NUM_OF_ANIMALS_TO_BREED = 2;

    /**
     * Butchering attack damage.
     */
    private static final int BUTCHERING_ATTACK_DAMAGE = 5;

    /**
     * Distance two animals need to be inside to breed.
     */
    private static final int DISTANCE_TO_BREED = 10;

    /**
     * Delays used to setDelay()
     */
    public static final int BUTCHER_DELAY    = 20;
    public static final int DECIDING_DELAY   = 40;
    public static final int BREEDING_DELAY   = 40;
    public static final int NO_ANIMALS_DELAY = 100;

    /**
     * Number of actions needed to dump inventory.
     */
    private static final int ACTIONS_FOR_DUMP = 10;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill.
     */
    public AbstractEntityAIHerder(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForHerding),
          new AITarget(HERDER_DECIDE, this::decideWhatToDo),
          new AITarget(HERDER_BREED, this::breedAnimals),
          new AITarget(HERDER_BUTCHER, this::butcherAnimals),
          new AITarget(HERDER_PICKUP, this::pickupItems)
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
        list.add(getBreedingItems());
        return list;
    }

    /**
     * Decides what job the herder should switch to, breeding or Butchering.
     *
     * @return The next {@link AIState} the herder should switch to, after executing this method.
     */
    public AIState decideWhatToDo()
    {
        setDelay(DECIDING_DELAY);

        final List<T> animals = new ArrayList<>(searchForAnimals());

        if (animals.isEmpty())
        {
            setDelay(NO_ANIMALS_DELAY);
            return HERDER_DECIDE;
        }

        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_DECIDING));

        final int numOfBreedableAnimals = ((int) animals.stream().filter(animal -> animal.getGrowingAge() == 0).count());

        final boolean hasBreedingItem =
          InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()),
            (ItemStack stack) -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, getBreedingItem()));

        if (!searchForItemsInArea().isEmpty())
        {
            return HERDER_PICKUP;
        }
        else if (maxAnimals())
        {
            return HERDER_BUTCHER;
        }
        else if (numOfBreedableAnimals >= NUM_OF_ANIMALS_TO_BREED && hasBreedingItem)
        {
            return HERDER_BREED;
        }
        return START_WORKING;
    }

    /**
     * Redirects the herder to their building.
     *
     * @return The next {@link AIState}.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_GOINGTOHUT));
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the herder for herding
     *
     * @return The next {@link AIState}.
     */
    private AIState prepareForHerding()
    {
        toolsNeeded.add(ToolType.AXE);
        itemsNeeded.add(getBreedingItems());

        for (final ToolType tool : toolsNeeded)
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
        }

        for (final ItemStack item : itemsNeeded)
        {
            checkIfRequestForItemExistOrCreateAsynch(item);
        }

        return HERDER_DECIDE;
    }

    /**
     * Butcher some animals (Preferably Adults) that the herder looks after.
     *
     * @return The next {@link AIState}.
     */
    private AIState butcherAnimals()
    {
        setDelay(BUTCHER_DELAY);

        if (!maxAnimals())
        {
            return HERDER_DECIDE;
        }

        if (!equipTool(ToolType.AXE))
        {
            return START_WORKING;
        }

        EntityAnimal animal = searchForAnimals()
                                .stream()
                                .filter(animalToButcher -> !animalToButcher.isChild())
                                .findFirst()
                                .orElse(null);

        if (animal == null)
        {
            animal = searchForAnimals().stream().findFirst().orElse(null);
        }

        butcherAnimal(animal);

        if (animal != null && !animal.isEntityAlive())
        {
            incrementActionsDone();
        }

        return HERDER_BUTCHER;
    }

    /**
     * Breed some animals together.
     *
     * @return The next {@link AIState}.
     */
    private AIState breedAnimals()
    {
        setDelay(BREEDING_DELAY);

        final List<T> animals = searchForAnimals();

        final EntityAnimal animalOne = animals
                                         .stream()
                                         .filter(animal -> animal.getGrowingAge() == 0)
                                         .findAny()
                                         .orElse(null);

        if (animalOne == null)
        {
            return HERDER_DECIDE;
        }

        final EntityAnimal animalTwo = animals.stream().filter(animal ->
          {
              final float range = animal.getDistanceToEntity(animalOne);
              final boolean isAnimalOne = animalOne.equals(animal);
              return animal.getGrowingAge() == 0 && range <= DISTANCE_TO_BREED && !isAnimalOne;
          }
        ).findAny().orElse(null);

        if (animalTwo == null)
        {
            return HERDER_DECIDE;
        }

        if (!equipItem(getBreedingItems()))
        {
            return START_WORKING;
        }

        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BREEDING));

        breedTwoAnimals(animalOne, animalTwo);

        return HERDER_DECIDE;
    }

    /**
     * Allows the worker to pickup any stray items around Hut.
     * Specifically useful when he possibly leaves Butchered
     * drops OR with chickens (that drop feathers and etc)!
     *
     * @return The next {@link AIState}.
     */
    private AIState pickupItems()
    {
        final List<EntityItem> items = new ArrayList<>(searchForItemsInArea());

        for (final EntityItem item : items)
        {
            walkToBlock(item.getPosition());
        }

        incrementActionsDone();

        return HERDER_DECIDE;
    }

    /**
     * Find animals in area.
     *
     * @return the {@link List} of animals in the area.
     */
    public List<T> searchForAnimals()
    {

        if (this.getTargetableArea() != null)
        {
            return new ArrayList<>((world.getEntitiesWithinAABB(
              getAnimalClass(),
              this.getTargetableArea()
            )));
        }
        return new ArrayList<>();
    }

    public int getMaxAnimalMultiplier()
    {
        return 2;
    }

    /**
     * Find items in hut area.
     *
     * @return the {@link List} of {@link EntityItem} in the area.
     */
    public List<EntityItem> searchForItemsInArea()
    {
        if (this.getTargetableArea() != null)
        {
            return new ArrayList<>((world.getEntitiesWithinAABB(
              EntityItem.class,
              this.getTargetableArea()
            )));
        }
        return new ArrayList<>();
    }

    /**
     * Get the Animal's class from the none Abstract.
     */
    public abstract Class<T> getAnimalClass();

    /**
     * Creates a simple area around the Herder's Hut used for AABB calculations for finding animals.
     *
     * @return The {@link AxisAlignedBB} of the Hut Area
     */
    private AxisAlignedBB getTargetableArea()
    {
        if (getOwnBuilding() == null)
        {
            return null;
        }

        return getOwnBuilding().getTargetableArea(world);
    }

    /**
     * Lets the herder walk to the animal.
     *
     * @return true if the herder is walking to the animal.
     */
    public boolean walkingToAnimal(final EntityAnimal animal)
    {

        if (animal != null)
        {
            worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_GOINGTOANIMAL));
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
     * @param animalOne the first {@link EntityAnimal} to breed.
     * @param animalTwo the second {@link EntityAnimal} to breed.
     */
    private void breedTwoAnimals(final EntityAnimal animalOne, final EntityAnimal animalTwo)
    {

        final List<EntityAnimal> animalsToBreed = new ArrayList<>();
        animalsToBreed.add(animalOne);
        animalsToBreed.add(animalTwo);

        for (final EntityAnimal animal : animalsToBreed)
        {
            if (!animal.isInLove() && !walkingToAnimal(animal))
            {
                //noinspection ConstantConditions
                animal.setInLove(null);
                worker.swingArm(EnumHand.MAIN_HAND);
                InventoryUtils.removeStackFromItemHandler(new InvWrapper(worker.getInventoryCitizen()), getBreedingItem());
            }
        }
    }

    /**
     * Returns true if animals list is above max.
     * Returns false if animals list is within max.
     *
     * @return if amount of animals is over max.
     */
    public boolean maxAnimals()
    {
        if (getOwnBuilding() != null)
        {
            final int numOfAnimals = searchForAnimals().size();
            final int maxAnimals = getOwnBuilding().getBuildingLevel() * getMaxAnimalMultiplier();

            return numOfAnimals > maxAnimals;
        }
        return true;
    }

    /**
     * Sets the tool as held item.
     *
     * @param toolType the {@link ToolType} we want to equip
     * @return true if the tool was equipped.
     */
    public boolean equipTool(final ToolType toolType)
    {
        if (getToolSlot(toolType) != -1)
        {
            worker.setHeldItem(getToolSlot(toolType));
            return true;
        }
        return false;
    }

    /**
     * Gets the slot in which the Tool is in.
     *
     * @return slot number.
     */
    private int getToolSlot(final ToolType toolType)
    {
        if (getOwnBuilding() != null)
        {
            final int slot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), toolType,
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
     * @return true if the item was equipped.
     */
    public boolean equipItem(final ItemStack itemStack)
    {
        if (checkIfRequestForItemExistOrCreateAsynch(itemStack))
        {
            worker.setHeldItem(getItemSlot(itemStack.getItem()));
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
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()), item, 0);
    }

    /**
     * Butcher an animal.
     *
     * @param animal the {@link EntityAnimal} we are butchering
     */
    private void butcherAnimal(final EntityAnimal animal)
    {

        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BUTCHERING));

        if (!walkingToAnimal(animal) && worker.getHeldItemMainhand() != null && animal != null)
        {
            new DamageSource(worker.getName());

            worker.swingArm(EnumHand.MAIN_HAND);
            animal.attackEntityFrom(new DamageSource(worker.getName()), (float) BUTCHERING_ATTACK_DAMAGE);

            worker.getHeldItemMainhand().damageItem(1, animal);
        }
    }

    /**
     * Gets an ItemStack of breedingItem for 2 animals.
     *
     * @return the BreedingItem stack.
     */
    public ItemStack getBreedingItems()
    {
        final ItemStack breedingItem = getBreedingItem().copy();
        ItemStackUtils.setSize(breedingItem, 2);
        return breedingItem;
    }

    /**
     * Get breeding item for animal.
     *
     * @return the {@link Item} needed for breeding the animal.
     */
    public abstract ItemStack getBreedingItem();
}
