package com.minecolonies.coremod.entity.ai.citizen.shepherd;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.BuildingShepherd;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Created by Asher on 3/9/17.
 */
public class EntityAIWorkShepherd extends AbstractEntityAISkill<JobShepherd>
{

    /**
     * Y range in which the shepherd detects other entities.
     */
    private static final double HEIGHT_DETECTION_RANGE = 10D;

    /**
     * Distance the shepherd starts searching.
     */
    protected static final int START_SEARCH_DISTANCE = 5;

    /**
     * Max distance for shepherd to search in
     */
    protected static final int MAX_SHEPHERD_DETECTION_RANGE = 25;

    /**
     * The distance the shepherd is searching entities in currently.
     */
    protected int currentSearchDistance = START_SEARCH_DISTANCE;

    /**
     * Max distance from Hut to detect sheep for shearing.
     */
    protected static final int MAX_DISTANCE_FROM_HUT = 25;

    /**
     * Experience given per sheep sheared.
     */
    protected static final double EXP_PER_SHEEP = 5.0;

    /**
     * Distance two sheep need to be inside to breed.
     */
    protected static final int DISTANCE_TO_BREED = 10;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    public EntityAIWorkShepherd(@NotNull final JobShepherd job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForShepherding),
          new AITarget(SHEPHERD_SEARCH_FOR_SHEEP, this::searchForSheep),
          new AITarget(SHEPHERD_WALK_TO_SHEEP, this::goToSheep),
          new AITarget(SHEPHERD_SHEAR_SHEEP, this::shearSheep),
          new AITarget(SHEPHERD_BREED_SHEEP, this::breedSheep)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Redirects the shepherd to his building.
     *
     * @return the next state.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.goingtohut"));
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the shepherd for fishing and
     * requests shear
     *
     * @return the next AIState
     */
    private AIState prepareForShepherding()
    {
        if (checkForToolOrWeapon(ToolType.SHEARS))
        {
            return getState();
        }
        if (job.getSheep().isEmpty())
        {
            return SHEPHERD_SEARCH_FOR_SHEEP;
        }
        return SHEPHERD_WALK_TO_SHEEP;
    }

    /**
     * If the job class has no sheep object the shepherd should search sheep.
     *
     * @return the next AIState the shepherd should switch to, after executing this method.
     */
    private AIState goToSheep()
    {
        job.removeDeadSheep();

        currentSearchDistance = START_SEARCH_DISTANCE;

        if (job.getSheep().isEmpty())
        {
            return SHEPHERD_SEARCH_FOR_SHEEP;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.goingtosheep"));

        final EntitySheep shearingSheep = job.getSheep().stream().filter(sheepie -> !sheepie.getSheared()).findFirst().orElse(null);

        int numOfBreedableSheep = job.getSheep().stream().filter(sheepie -> sheepie.getGrowingAge() == 0).toArray().length;

        if (shearingSheep != null && walkToSheep(shearingSheep))
        {
            return SHEPHERD_SHEAR_SHEEP;
        }
        else if (numOfBreedableSheep >= 2)
        {
            return SHEPHERD_BREED_SHEEP;
        }
        return SHEPHERD_SEARCH_FOR_SHEEP;
    }

    /**
     * Lets the shepherd walk to the sheep if the sheep in his job class already has been filled.
     *
     * @return true if the shepherd has arrived at the sheep.
     */
    private boolean walkToSheep(final EntitySheep sheep)
    {

        if (sheep != null)
        {
            return walkToBlock(sheep.getPosition());
        }
        else
        {
            return false;
        }
    }

    /**
     * Find sheep in area.
     *
     * @return the next AIState the shepherd should switch to, after executing this method.
     */
    private AIState searchForSheep()
    {
        job.removeDeadSheep();

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.searchforsheep"));

        if (currentSearchDistance > MAX_SHEPHERD_DETECTION_RANGE)
        {
            if (!job.getSheep().isEmpty())
            {
                return SHEPHERD_WALK_TO_SHEEP;
            }
            currentSearchDistance = START_SEARCH_DISTANCE;
        }

        job.setSheep(world.getEntitiesWithinAABB(
          EntitySheep.class,
          this.getTargetableArea(currentSearchDistance),
          sheep ->
          {
              final double range = worker.getWorkBuilding().getLocation().getDistance(sheep.getPosition().getX(),
                sheep.getPosition().getY(), sheep.getPosition().getZ());
              return range <= MAX_DISTANCE_FROM_HUT;
          }
        ));


        currentSearchDistance++;
        return SHEPHERD_SEARCH_FOR_SHEEP;
    }

    /**
     * Creates a simple area around the Shepherd used for AABB calculations for finding sheep
     *
     * @param range the range inwhich the area reaches
     * @return The AABB
     */
    private AxisAlignedBB getTargetableArea(final double range)
    {
        return this.worker.getEntityBoundingBox().expand(range, HEIGHT_DETECTION_RANGE, range);
    }

    /**
     * Shears a sheep, with a chance of dying it!
     *
     * @return the wanted AiState
     */
    private AIState shearSheep()
    {
        job.removeDeadSheep();

        final List<EntitySheep> sheepies = job.getSheep();

        if (sheepies.isEmpty())
        {
            return SHEPHERD_SEARCH_FOR_SHEEP;
        }

        if (checkForToolOrWeapon(ToolType.SHEARS))
        {
            return PREPARING;
        }

        final EntitySheep sheep = sheepies.stream().filter(sheepie -> !sheepie.getSheared()).findFirst().orElse(null);

        equipShears();

        if (worker.getHeldItemMainhand() != null && sheep != null)
        {
            final List<ItemStack> items = sheep.onSheared(worker.getHeldItemMainhand(),
              worker.worldObj,
              worker.getPosition(),
              net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, worker.getHeldItemMainhand()));

            dyeSheepChance(sheep);

            worker.getHeldItemMainhand().damageItem(1, worker);

            worker.addExperience(EXP_PER_SHEEP);

            for (final ItemStack item : items)
            {
                worker.getInventoryCitizen().addItemStackToInventory(item);
            }
        }
        return SHEPHERD_WALK_TO_SHEEP;
    }

    /**
     * Possibly dyes a sheep based on their Worker Hut Level
     */
    private void dyeSheepChance(final EntitySheep sheep)
    {
        if (worker.getWorkBuilding() != null)
        {
            final int chanceToDye = worker.getWorkBuilding().getBuildingLevel();

            final int rand = world.rand.nextInt(100);

            if (rand <= chanceToDye)
            {
                final int dyeInt = world.rand.nextInt(15);
                sheep.setFleeceColor(EnumDyeColor.byMetadata(dyeInt));
            }
        }
    }

    private AIState breedSheep()
    {
        job.removeDeadSheep();

        final List<EntitySheep> sheepies = job.getSheep();

        final EntitySheep sheepOne = sheepies.stream().filter(sheep -> sheep.getGrowingAge() == 0).findAny().orElse(null);

        if (sheepOne == null)
        {
            return PREPARING;
        }

        final EntitySheep sheepTwo = sheepies.stream().filter(sheep ->
          {
              final float range = sheep.getDistanceToEntity(sheepOne);
              final boolean isSheepOne = sheepOne.equals(sheep);
              return sheep.getGrowingAge() == 0 && range <= DISTANCE_TO_BREED && !isSheepOne;
          }
        ).findAny().orElse(null);

        if (sheepTwo == null)
        {
            return PREPARING;
        }

        if (!equipWheat())
        {
            return PREPARING;
        }

        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.breedingsheep"));

        breedTwoSheep(sheepOne, sheepTwo);

        return SHEPHERD_WALK_TO_SHEEP;
    }

    /**
     * Breed two sheep together!
     *
     * @param sheepOne the first sheep to breed.
     * @param sheepTwo the second sheep to breed.
     */
    private void breedTwoSheep(EntitySheep sheepOne, EntitySheep sheepTwo)
    {
        final EntityPlayer playerUsedForbreeding = worker.getColony().getMessageEntityPlayers().stream().findFirst().orElse(null);

        final List<EntitySheep> sheepToBreed = new ArrayList<>();
        sheepToBreed.add(sheepOne);
        sheepToBreed.add(sheepTwo);

        for (final EntitySheep sheep : sheepToBreed)
        {
            if (playerUsedForbreeding != null && !sheep.isInLove() && walkToSheep(sheep))
            {
                sheep.setInLove(playerUsedForbreeding);
                new InvWrapper(getInventory()).extractItem(getItemSlot(Items.WHEAT), 1, false);
            }
        }
    }

    /**
     * Sets the shears as held item.
     */
    private void equipShears()
    {
        worker.setHeldItem(getSheersSlot());
    }

    /**
     * Sets Wheat as helf item or returns false.
     */
    private boolean equipWheat()
    {
        if (!checkOrRequestItems(new ItemStack(Items.WHEAT, 2)))
        {
            worker.setHeldItem(getItemSlot(Items.WHEAT));
            return true;
        }
        return false;
    }

    /**
     * Get's the slot in which the Sheers are in.
     *
     * @return slot number
     */
    private int getSheersSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.SHEARS,
          TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());
    }

    /**
     * Gets the slot in which the inserted item is in. (if any)
     *
     * @param item The item to check for
     * @return slot number
     */
    private int getItemSlot(Item item)
    {
        return InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()), item, 0);
    }

    /**
     * Returns the shepherd's work building.
     *
     * @return building instance
     */
    @Override
    protected BuildingShepherd getOwnBuilding()
    {
        return (BuildingShepherd) worker.getWorkBuilding();
    }
}
