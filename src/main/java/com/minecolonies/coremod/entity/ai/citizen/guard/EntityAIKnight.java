package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.compatibility.tinkers.TinkersWeaponHelper;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIKnight extends AbstractEntityAIGuard<JobKnight>
{
    /**
     * This guard's minimum distance for attack.
     */
    private static final double MAX_DISTANCE_FOR_ATTACK = 3;

    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 30;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIKnight(@NotNull final JobKnight job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_ATTACK_PROTECT, this::attackProtect),
          new AITarget(GUARD_ATTACK_PHYSICAL, this::attackPhysical)
        );
        toolsNeeded.add(ToolType.SWORD);

        for (final List<GuardGear> list: itemsNeeded)
        {
            list.add(new GuardGear(ToolType.SHIELD, EntityEquipmentSlot.MAINHAND, 0, 0, SHIELD_LEVEL_RANGE, SHIELD_BUILDING_LEVEL_RANGE));
        }
    }

    @Override
    protected int getAttackRange()
    {
        return (int) MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            return BASE_RELOAD_TIME / (worker.getCitizenData().getLevel() + 1);
        }
        return BASE_RELOAD_TIME;
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        final List<ItemStack> list = super.itemsNiceToHave();
        list.add(new ItemStack(Items.SHIELD, 1));
        return list;
    }

    @Override
    protected AIState decide()
    {
        final AIState superState = super.decide();

        if ((superState != DECIDE && superState != PREPARING) || target == null)
        {
            return superState;
        }

        return GUARD_ATTACK_PHYSICAL;
    }

    /**
     * Check if the guard can protect himself with a shield
     * And if so, do it.
     *
     * @return The next AIState.
     */
    protected AIState attackProtect()
    {
        setDelay(2);
        final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()),
          Items.SHIELD,
          -1);

        if (shieldSlot != -1
              && target != null
              && !target.isDead)
        {
            worker.getCitizenItemHandler().setHeldItem(EnumHand.OFF_HAND, shieldSlot);
            worker.setActiveHand(EnumHand.OFF_HAND);

            worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

            if (worker.getDistance(target) > getAttackRange())
            {
                worker.isWorkerAtSiteWithMove(target.getPosition(), getAttackRange());
            }
        }

        return GUARD_ATTACK_PHYSICAL;
    }

    protected AIState attackPhysical()
    {
        if (worker.getRevengeTarget() != null
              && !worker.getRevengeTarget().isDead
              && worker.getDistance(worker.getRevengeTarget()) < getAttackRange())
        {
            target = worker.getRevengeTarget();
        }

        if (target == null || target.isDead)
        {
            incrementActionsDone();
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            return DECIDE;
        }
        else if (worker.getDistance(target) > getAttackRange() * 5 && !worker.canEntityBeSeen(target))
        {
            target = null;
            return DECIDE;
        }

        if (currentAttackDelay != 0)
        {
            currentAttackDelay--;
            return GUARD_ATTACK_PROTECT;
        }
        else
        {
            currentAttackDelay = getAttackDelay();
        }

        if (getOwnBuilding() != null)
        {

            if (worker.getDistance(target) > getAttackRange())
            {
                worker.isWorkerAtSiteWithMove(target.getPosition(), getAttackRange());
                return GUARD_ATTACK_PHYSICAL;
            }

            final int swordSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()),
              ToolType.SWORD,
              0,
              getOwnBuilding().getMaxToolLevel());

            if (swordSlot != -1)
            {
                worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, swordSlot);

                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

                worker.swingArm(EnumHand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));

                double damageToBeDealt = BASE_PHYSICAL_DAMAGE;

                if (worker.getHealth() <= DOUBLE_DAMAGE_THRESHOLD)
                {
                    damageToBeDealt *= 2;
                }

                final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);

                if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
                {
                    if (heldItem.getItem() instanceof ItemSword)
                    {
                        damageToBeDealt += ((ItemSword) heldItem.getItem()).getAttackDamage();
                    }
                    else
                    {
                        damageToBeDealt += TinkersWeaponHelper.getDamage(heldItem);
                    }
                    damageToBeDealt += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute());
                }

                final DamageSource source = new DamageSource(worker.getName());
                if (Configurations.gameplay.pvp_mode && target instanceof EntityPlayer)
                {
                    source.setDamageBypassesArmor();
                }

                target.attackEntityFrom(source, (float) damageToBeDealt);
                target.setRevengeTarget(worker);

                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
        }
        return GUARD_ATTACK_PHYSICAL;
    }

}
