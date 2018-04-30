package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.compatibility.tinkers.TinkersWeaponHelper;
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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
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
     * The pitch will be divided by this to calculate it for the arrow sound.
     */
    private static final double PITCH_DIVIDER = 1.0D;

    /**
     * The base pitch, add more to this to change the sound.
     */
    private static final double BASE_PITCH = 0.8D;

    /**
     * Random is multiplied by this to get a random sound.
     */
    private static final double PITCH_MULTIPLIER = 0.4D;

    /**
     * Quantity the worker should turn around all at once.
     */
    private static final double TURN_AROUND = 180D;

    /**
     * Normal volume at which sounds are played at.
     */
    private static final double BASIC_VOLUME = 1.0D;

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
    protected EntityLiving target = null;

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
          new AITarget(GUARD_ATTACK_PROTECT, this::attackProtect),
          new AITarget(GUARD_ATTACK_PHYSICAL, this::attackPhyisical)
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
                  worker::setMainHeldItem);
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
        setDelay(20);
        System.out.println("Decide1");
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

        System.out.println("Decide2");
        for (final ItemStack item : itemsNeeded)
        {
            if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(getInventory()),
              item.getItem(),
              item.getCount()))
            {
                return START_WORKING;
            }
        }

        System.out.println("Decide3");
        if (target == null)
        {
            System.out.println("Decide null");
            return GUARD_SEARCH_TARGET;
        }

        System.out.println("Decide4");
        if (target.isDead)
        {
            System.out.println("Decide dead");
            target = null;
        }

        System.out.println("Decide5");

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
                if (mobEntry.getEntityEntry().newInstance(world) instanceof EntityLiving)
                {
                    target = (EntityLiving) world.findNearestEntityWithinAABB(mobEntry.getEntityEntry().getEntityClass(),
                      getSearchArea(),
                      worker);
                }

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
          -1);

        if (shieldSlot != -1)
        {
            worker.setHeldItem(EnumHand.OFF_HAND, shieldSlot);
            worker.setActiveHand(EnumHand.OFF_HAND);
        }

        return GUARD_ATTACK_PHYSICAL;
    }

    protected AIState attackPhyisical()
    {

        if (target == null || target.isDead)
        {
            return DECIDE;
        }

        if (currentAttackDelay != 0)
        {
            currentAttackDelay--;
            return GUARD_ATTACK_PROTECT;
        }
        else
        {
            currentAttackDelay = PHYSICAL_ATTACK_DELAY;
        }

        if (getOwnBuilding() != null)
        {
            final int swordSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()),
              ToolType.SWORD,
              0,
              getOwnBuilding().getMaxToolLevel());

            if (swordSlot != -1)
            {
                worker.setHeldItem(EnumHand.MAIN_HAND, swordSlot);
                //worker.setActiveHand(EnumHand.MAIN_HAND);

                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

                worker.swingArm(EnumHand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) getRandomPitch());

                double damageToBeDealt = 3;

                if (worker.getHealth() <= 2)
                {
                    damageToBeDealt *= 2;
                }

                final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);

                if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
                {
                    if(heldItem.getItem() instanceof ItemSword)
                    {
                        damageToBeDealt += ((ItemSword) heldItem.getItem()).getAttackDamage();
                    }
                    else
                    {
                        damageToBeDealt += TinkersWeaponHelper.getDamage(heldItem);
                    }
                    damageToBeDealt += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute());
                }

                target.attackEntityFrom(new DamageSource(worker.getName()), (float) damageToBeDealt);
                target.setRevengeTarget(worker);

                worker.damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
        }
        return GUARD_ATTACK_PHYSICAL;
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

    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (worker.getRNG().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
