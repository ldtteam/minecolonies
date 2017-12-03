package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
     * Amount of animals to keep per Hut Level.
     */
    private final int maxAnimalMultiplier;

    /**
     * Delays used to setDelay()
     */
    public static final int DELAY_TWENTY      = 20;
    public static final int DELAY_FOURTY      = 40;
    public static final int DELAY_ONE_HUNDRED = 100;

    /**
     * Number of actions needed to dump inventory.
     */
    private static final int ACTIONS_FOR_DUMP = 10;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill.
     * @param maxAnimalsMultiplier the number of max animals to add per hut level
     */
    public AbstractEntityAIHerder(@NotNull final J job, final int maxAnimalsMultiplier)
    {
        super(job);
        this.maxAnimalMultiplier = maxAnimalsMultiplier;
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
        list.add(new ItemStack(getBreedingItem(), 2));
        return list;
    }

    /**
     * Decides what job the herder should switch to, breeding or Butchering.
     *
     * @return The next {@link AIState} the herder should switch to, after executing this method.
     */
    public AIState decideWhatToDo()
    {
        setDelay(DELAY_FOURTY);

        final List<T> animals = new ArrayList<>(searchForAnimals());

        if (animals.isEmpty())
        {
            setDelay(DELAY_ONE_HUNDRED);
            return HERDER_DECIDE;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.deciding"));

        final int numOfBreedableAnimals = animals.stream().filter(animal -> animal.getGrowingAge() == 0).toArray().length;

        final boolean hasBreedingItem = worker.hasItemInInventory(getBreedingItem(), 0);

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
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.goingToHut"));
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
        itemsNeeded.add(new ItemStack(getBreedingItem(), 2));

        for (final ToolType tool : toolsNeeded)
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
        }

        for (final ItemStack item : itemsNeeded)
        {
            checkOrRequestItemsAsynch(false, item);
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
        setDelay(DELAY_TWENTY);

        if (!maxAnimals())
        {
            return HERDER_DECIDE;
        }

        if (!equipTool(ToolType.AXE))
        {
            return START_WORKING;
        }

        EntityAnimal animal = searchForAnimals().stream().filter(animalToButcher -> !animalToButcher.isChild()).findFirst().orElse(null);

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
        setDelay(DELAY_FOURTY);

        final List<T> animals = new ArrayList<>(searchForAnimals());

        final EntityAnimal animalOne = animals.stream().filter(animal -> animal.getGrowingAge() == 0).findAny().orElse(null);

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

        if (!equipItem(new ItemStack(getBreedingItem(), 2)))
        {
            return START_WORKING;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.breeding"));

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
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.searching"));

        if (this.getTargetableArea() != null)
        {
            return new ArrayList<>((world.getEntitiesWithinAABB(
              getAnimalClass(),
              this.getTargetableArea()
            )));
        }
        return new ArrayList<>();
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
        final Structures.StructureName sn =
          new Structures.StructureName(Structures.SCHEMATICS_PREFIX, getOwnBuilding().getStyle(), getOwnBuilding().getSchematicName() + getOwnBuilding().getBuildingLevel());

        final String structureName = sn.toString();

        final StructureWrapper wrapper = new StructureWrapper(world, structureName);
        wrapper.rotate(getOwnBuilding().getRotation(), world, getOwnBuilding().getLocation(), getOwnBuilding().isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);

        final BlockPos pos = getOwnBuilding().getLocation();
        wrapper.setPosition(pos);

        final int x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
        final int z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
        final int x3 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
        final int z3 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());
        final int y1 = getOwnBuilding().getLocation().getY() - 1;
        final int y3 = getOwnBuilding().getLocation().getY() + 2;

        return new AxisAlignedBB(x1, y1, z1, x3, y3, z3);
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
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.goingToAnimal"));
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
                new InvWrapper(getInventory()).extractItem(getItemSlot(getBreedingItem()), 1, false);
            }
        }
    }

    /**
     * Returns true if animals list is above max.
     * Returns false if animals list is within max.
     *
     * @return See Above ^^^.
     */
    public boolean maxAnimals()
    {
        if (getOwnBuilding() != null)
        {
            final int numOfAnimals = searchForAnimals().size();
            final int maxAnimals = getOwnBuilding().getBuildingLevel() * maxAnimalMultiplier;

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
        if (!checkOrRequestItemsAsynch(true, itemStack))
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

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.butchering"));

        if (!walkingToAnimal(animal) && worker.getHeldItemMainhand() != null && animal != null)
        {
            new DamageSource(worker.getName());

            worker.swingArm(EnumHand.MAIN_HAND);
            animal.attackEntityFrom(new DamageSource(worker.getName()), (float) BUTCHERING_ATTACK_DAMAGE);

            worker.getHeldItemMainhand().damageItem(1, animal);
        }
    }

    /**
     * Get breeding item for animal.
     *
     * @return the {@link Item} needed for breeding the animal.
     */
    public Item getBreedingItem()
    {
        if (getAnimalClass().equals(EntityChicken.class))
        {
            return Items.WHEAT_SEEDS;
        }
        if (getAnimalClass().equals(EntityPig.class))
        {
            return Items.CARROT;
        }
        else
        {
            return Items.WHEAT;
        }
    }
}
