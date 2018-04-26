package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuardsNew;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

public abstract class AbstractEntityAIGuardNew<J extends AbstractJobGuard> extends AbstractEntityAIInteract<J>
{
    /**
     * The priority we are currently at for getting a target.
     */
    private int currentPriority = -1;

    /**
     * Tools and Items needed by the worker.
     */
    public final List<ToolType>  toolsNeeded = new ArrayList<>();
    public final List<ItemStack> itemsNeeded = new ArrayList<>();

    /**
     * How many more ticks we have until next attack.
     */
    protected int currentAttackDelay = 0;

    /**
     * Physical Attack delay in ticks.
     */
    protected static final int PHYSICAL_ATTACK_DELAY = 40;

    /**
     * The current target for our guard.
     */
    protected Entity target = null;

    /**
     * Default vision range.
     */
    private static final int DEFAULT_VISION = 10;

    /**
     * Y search range.
     */
    private static final int Y_VISION = 15;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIGuardNew(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepare),
          new AITarget(DECIDE, this::decide),
          new AITarget(GUARD_SEARCH_TARGET, this::getTarget),
          new AITarget(GUARD_ATTACK_PROTECT, this::attackProtect)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Redirects the herder to their building.
     *
     * @return The next {@link AIState}.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        worker.setLatestStatus(new TextComponentTranslation(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_WORKER_GOINGTOHUT));
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
    private AIState prepare()
    {
        setDelay(20 * 5);

        for (final ToolType tool : toolsNeeded)
        {
            if (checkForToolOrWeapon(tool))
            {
                return getState();
            }
            else if (getOwnBuilding() != null)
            {
                InventoryFunctions.matchFirstInProviderWithSimpleAction(worker,
                  stack -> !ItemStackUtils.isEmpty(stack)
                             && ItemStackUtils.doesItemServeAsWeapon(stack)
                             && ItemStackUtils.hasToolLevel(stack, tool, 0, getOwnBuilding().getMaxToolLevel()),
                  worker::setHeldItem);
            }
        }

        for (final ItemStack item : itemsNeeded)
        {
            checkIfRequestForItemExistOrCreateAsynch(item);
        }

        return DECIDE;
    }

    /**
     * Decide what we should do next!
     *
     * @return the next AIState.
     */
    protected AIState decide()
    {
        for (final ToolType toolType : toolsNeeded)
        {
            if (getOwnBuilding() != null && !InventoryUtils.hasItemHandlerToolWithLevel(new InvWrapper(getInventory()),
              toolType,
              0,
              getOwnBuilding().getMaxToolLevel()))
            {
                return START_WORKING;
            }
        }

        for (final ItemStack item : itemsNeeded)
        {
            if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(getInventory()),
              item.getItem(),
              item.getCount()))
            {
                return START_WORKING;
            }
        }

        if (target == null)
        {
            return GUARD_SEARCH_TARGET;
        }

        if (!target.isDead)
        {
            return DECIDE;
        }
        else
        {
            target = null;
        }

        return DECIDE;
    }

    /**
     * Get a target for the guard.
     *
     * @return The next AIState to go to.
     */
    protected AIState getTarget()
    {
        setDelay(20);

        final AbstractBuildingGuardsNew building = (AbstractBuildingGuardsNew) getOwnBuilding();

        if (building != null && target == null)
        {
            final MobEntryView mobEntry = building.getMobsToAttack()
                                         .stream()
                                         .filter(view -> view.getPriority() == currentPriority)
                                         .findFirst()
                                         .orElse(null);

            if (mobEntry != null && mobEntry.getAttack())
            {
                target = world.findNearestEntityWithinAABB(mobEntry.getEntityEntry().getEntityClass(),
                  getSearchArea(),
                  worker);

                if (target != null)
                {
                    currentPriority = 0;
                    return DECIDE;
                }
            }

            if (currentPriority > 1)
            {
                currentPriority -= 1;
            }
            else
            {
                currentPriority = building.getMobsToAttack().size();
            }
        }
        return DECIDE;
    }

    /**
     * Check if the guard can protect himself with a shield
     * And if so, do it.
     *
     * @return The next AIState.
     */
    protected AIState attackProtect()
    {
        final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()),
          Items.SHIELD,
          0);

        if (shieldSlot != -1)
        {
            worker.setHeldItem(shieldSlot);
        }

        return DECIDE;
    }


    /**
     * Get the {@link AxisAlignedBB} we're searching for targets in.
     *
     * @return the {@link AxisAlignedBB}
     */
    protected AxisAlignedBB getSearchArea()
    {
        final AbstractBuildingGuardsNew building = (AbstractBuildingGuardsNew) getOwnBuilding();

        if (building != null)
        {
            final double x1 = worker.posX + (building.getBonusVision() + DEFAULT_VISION);
            final double x2 = worker.posX - (building.getBonusVision() + DEFAULT_VISION);
            final double y1 = worker.posY + Y_VISION;
            final double y2 = worker.posY - Y_VISION;
            final double z1 = worker.posZ + (building.getBonusVision() + DEFAULT_VISION);
            final double z2 = worker.posZ - (building.getBonusVision() + DEFAULT_VISION);

            return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        }

        return getOwnBuilding().getTargetableArea(world);
    }
}
