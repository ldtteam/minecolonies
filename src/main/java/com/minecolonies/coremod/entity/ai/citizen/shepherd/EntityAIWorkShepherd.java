package com.minecolonies.coremod.entity.ai.citizen.shepherd;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.BuildingShepherd;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

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
          new AITarget(SHEPHERD_SHEAR_SHEEP, this::shearSheep)
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
        if (job.getSheep() == null)
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
        currentSearchDistance = START_SEARCH_DISTANCE;
        if (job.getSheep() == null)
        {
            return SHEPHERD_SEARCH_FOR_SHEEP;
        }
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.goingtosheep"));

        if (walkToSheep())
        {
            return getState();
        }
        return SHEPHERD_SHEAR_SHEEP;
    }

    /**
     * Lets the shepherd walk to the sheep if the sheep in his job class already has been filled.
     *
     * @return true if the shepherd has arrived at the sheep.
     */
    private boolean walkToSheep()
    {
        return !job.getSheep().isEmpty() && walkToBlock(job.getSheep().get(0).getPosition());
    }

    /**
     * Find sheep in area.
     *
     * @return the next AIState the shepherd should switch to, after executing this method.
     */
    private AIState searchForSheep()
    {
        if (job.getSheep() != null && !job.getSheep().isEmpty())
        {
            return SHEPHERD_WALK_TO_SHEEP;
        }
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.shepherd.searchforsheep"));

        if (currentSearchDistance > MAX_SHEPHERD_DETECTION_RANGE)
        {
            currentSearchDistance = START_SEARCH_DISTANCE;
        }

        if (worker.getPosition().getDistance(worker.getWorkBuilding().getLocation().getX()
          ,worker.getWorkBuilding().getLocation().getY(),worker.getWorkBuilding().getLocation().getZ()) > MAX_DISTANCE_FROM_HUT )
        {
            return START_WORKING;
        }

        job.setSheep(world.getEntitiesWithinAABB(
          EntitySheep.class,
          this.getTargetableArea(currentSearchDistance),
          sheep ->
          {
              final double range = worker.getWorkBuilding().getLocation().getDistance(sheep.getPosition().getX(),
                sheep.getPosition().getY(), sheep.getPosition().getZ());
              return !sheep.getSheared() && range <= MAX_DISTANCE_FROM_HUT;
          }
        ));

        worker.addExperience(EXP_PER_SHEEP);

        if (!job.getSheep().isEmpty())
        {
            return SHEPHERD_WALK_TO_SHEEP;
        }
        else
        {
            currentSearchDistance++;
        }
        return SHEPHERD_SEARCH_FOR_SHEEP;
    }

    private AxisAlignedBB getTargetableArea(final double range)
    {
        return this.worker.getEntityBoundingBox().expand(range, HEIGHT_DETECTION_RANGE, range);
    }

    private AIState shearSheep()
    {
        final List<EntitySheep> sheepies = job.getSheep();

        if (sheepies == null || sheepies.isEmpty())
        {
            return SHEPHERD_SEARCH_FOR_SHEEP;
        }

        if (checkForToolOrWeapon(ToolType.SHEARS))
        {
            return PREPARING;
        }

        final EntitySheep sheep = sheepies.get(0);

        equipShears();

        if (worker.getHeldItemMainhand() != null)
        {
            final List<ItemStack> items = sheep.onSheared(worker.getHeldItemMainhand(),
              worker.worldObj,
              worker.getPosition(),
              net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, worker.getHeldItemMainhand()));

            dyeSheepChance(sheep);

            worker.getHeldItemMainhand().damageItem(1, worker);

            for (final ItemStack item : items)
            {
                worker.getInventoryCitizen().addItemStackToInventory(item);
            }
        }

        if (sheep.getSheared())
        {
            sheepies.remove(sheep);
            job.setSheep(sheepies);

            if (job.getSheep().isEmpty())
            {
                return SHEPHERD_SEARCH_FOR_SHEEP;
            }
            return SHEPHERD_WALK_TO_SHEEP;
        }
        return SHEPHERD_SHEAR_SHEEP;
    }

    /**
     *
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

    /**
     * Sets the shears as held item.
     */
    private void equipShears()
    {
        worker.setHeldItem(getSheersSlot());
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
