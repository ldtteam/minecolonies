package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
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
    private static int maxAnimalMultiplier = 2;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIHerder(@NotNull final J job, int maxAnimalsMultiplier)
    {
        super(job);
        maxAnimalMultiplier = maxAnimalsMultiplier;
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForHerding),
          new AITarget(HERDER_DECIDE, this::decideWhatToDo),
          new AITarget(HERDER_BREED, this::breedAnimals),
          new AITarget(HERDER_BUTCHER, this::butcherAnimals)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 10;
    }

    /**
     * Decides what job the herder should switch to, breeding, butchering, or shearing (If Shepherd).
     *
     * @return the next AIState the shepherd should switch to, after executing this method.
     */
    public AIState decideWhatToDo()
    {
        setDelay(40);

        final List<T> animals = new ArrayList<>(getAnimals());

        if (animals.isEmpty())
        {
            setDelay(100);
            return HERDER_DECIDE;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.deciding"));

        final int numOfBreedableAnimals = animals.stream().filter(animal -> animal.getGrowingAge() == 0).toArray().length;

        if (maxAnimals())
        {
            return HERDER_BUTCHER;
        }
        else if (numOfBreedableAnimals >= NUM_OF_ANIMALS_TO_BREED)
        {
            return HERDER_BREED;
        }
        return HERDER_DECIDE;
    }

    /**
     * Redirects the herder to their building.
     *
     * @return the next state.
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
     * @return the next AIState
     */
    private AIState prepareForHerding()
    {
        if (checkForToolOrWeapon(ToolType.AXE) && checkOrRequestItems(new ItemStack(Items.WHEAT, 2))
              && (this instanceof EntityAIWorkShepherd && checkForToolOrWeapon(ToolType.SHEARS)))
        {
            return getState();
        }
        return HERDER_DECIDE;
    }

    /**
     * Butcher some animals (Preferably Adults) that the herder looks after.
     *
     * @return The next AIState
     */
    private AIState butcherAnimals()
    {
        setDelay(20);

        if (!maxAnimals())
        {
            return HERDER_DECIDE;
        }

        if (!equipTool(ToolType.AXE))
        {
            return PREPARING;
        }

        EntityAnimal animal = getAnimals().stream().filter(animalToButcher -> !animalToButcher.isChild()).findFirst().orElse(null);

        if (animal == null)
        {
            animal = getAnimals().stream().findFirst().orElse(null);
        }

        butcherAnimal(animal);

        if (!animal.isEntityAlive())
        {
            incrementActionsDone();
        }

        return HERDER_BUTCHER;
    }

    /**
     * Breed some animals together.
     *
     * @return The next AIState
     */
    private AIState breedAnimals()
    {
        setDelay(40);

        final List<T> animals = new ArrayList<>(getAnimals());

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

        if (!equipItem(new ItemStack(Items.WHEAT, 2)))
        {
            return PREPARING;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.breeding"));

        breedTwoAnimals(animalOne, animalTwo);

        return HERDER_DECIDE;
    }

    /**
     * Find animals in area.
     *
     * @return the next AIState the herder should switch to, after executing this method.
     */
    public List<T> searchForAnimals(Class<? extends T> clazz)
    {
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.searching"));

        if (this.getTargetableArea() != null)
        {
            return new ArrayList<>((world.getEntitiesWithinAABB(
              clazz,
              this.getTargetableArea()
            )));
        }
        return new ArrayList<>();
    }

    /**
     * Get the anmimals from the none Abstract.
     */
    public abstract List<T> getAnimals();

    /**
     * Creates a simple area around the Herder's Hut used for AABB calculations for finding animals.
     *
     * @return The AABB.
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
        final int y1 = getOwnBuilding().getLocation().getY() - 10;
        final int y3 = getOwnBuilding().getLocation().getY() + 10;

        return new AxisAlignedBB(x1, y1, z1, x3, y3, z3);
    }

    /**
     * Lets the herder walk to the animal.
     *
     * @return true if the herder has arrived at the animal.
     */
    public boolean walkToAnimal(final EntityAnimal animal)
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
     * @param animalOne the first animal to breed.
     * @param animalTwo the second animal to breed.
     */
    private void breedTwoAnimals(final EntityAnimal animalOne, final EntityAnimal animalTwo)
    {

        final List<EntityAnimal> animalsToBreed = new ArrayList<>();
        animalsToBreed.add(animalOne);
        animalsToBreed.add(animalTwo);

        for (final EntityAnimal animal : animalsToBreed)
        {
            if (!animal.isInLove() && !walkToAnimal(animal))
            {
                animal.setInLove(null);
                new InvWrapper(getInventory()).extractItem(getItemSlot(Items.WHEAT), 1, false);
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
            final int numOfAnimals = getAnimals().size();
            final int maxAnimals = getOwnBuilding().getBuildingLevel() * maxAnimalMultiplier;

            return numOfAnimals > maxAnimals;
        }
        return true;
    }

    /**
     * Sets the tool as held item.
     */
    public boolean equipTool(ToolType toolType)
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
    private int getToolSlot(ToolType toolType)
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
     * Sets the Item as held item or returns false.
     *
     * @return whether the item was equipped.
     */
    private boolean equipItem(ItemStack itemStack)
    {
        if (!checkOrRequestItems(itemStack))
        {
            worker.setHeldItem(getItemSlot(itemStack.getItem()));
            return true;
        }
        return false;
    }

    /**
     * Gets the slot in which the inserted item is in. (if any).
     *
     * @param item The item to check for.
     * @return slot number.
     */
    private int getItemSlot(final Item item)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()), item, 0);
    }

    /**
     * Butcher an animal.
     */
    private void butcherAnimal(final EntityAnimal animal)
    {

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.herder.butchering"));

        if (worker.getHeldItemMainhand() != null && animal != null)
        {
            new DamageSource(worker.getName());

            final BlockPos oldAnimalLocation = animal.getPosition();

            animal.attackEntityFrom(new DamageSource(worker.getName()), (float) BUTCHERING_ATTACK_DAMAGE);

            worker.getHeldItemMainhand().damageItem(1, animal);

            walkToBlock(oldAnimalLocation);
        }
    }
}
