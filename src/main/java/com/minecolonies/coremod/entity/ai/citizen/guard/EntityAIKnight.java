package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.compatibility.tinkers.TinkersWeaponHelper;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.research.AdditionModifierResearchEffect;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_ATTACK_PHYSICAL;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_ATTACK_PROTECT;
import static com.minecolonies.api.research.util.ResearchConstants.MELEE_DAMAGE;
import static com.minecolonies.api.research.util.ResearchConstants.SHIELD_USAGE;
import static com.minecolonies.api.util.constant.GuardConstants.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIKnight extends AbstractEntityAIGuard<JobKnight>
{
    /**
     * Update interval for the guards attack ai
     */
    private final static int GUARD_ATTACK_INTERVAL = 8;

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
          new AITarget(GUARD_ATTACK_PROTECT, this::attackProtect, GUARD_ATTACK_INTERVAL),
          new AITarget(GUARD_ATTACK_PHYSICAL, this::attackPhysical, GUARD_ATTACK_INTERVAL)
        );
        toolsNeeded.add(ToolType.SWORD);

        for (final List<GuardGear> list : itemsNeeded)
        {
            list.add(new GuardGear(ToolType.SHIELD, EquipmentSlotType.MAINHAND, 0, 0, SHIELD_LEVEL_RANGE, SHIELD_BUILDING_LEVEL_RANGE));
        }
    }

    @Override
    public IAIState getAttackState()
    {
        return GUARD_ATTACK_PHYSICAL;
    }

    @Override
    protected int getAttackRange()
    {
        return MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    public boolean hasMainWeapon()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.SWORD, 0, buildingGuards.getMaxToolLevel()) != -1;
    }

    @Override
    public void wearWeapon()
    {
        final int weaponSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.SWORD, 0, buildingGuards.getMaxToolLevel());

        if (weaponSlot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, weaponSlot);
        }
    }

    /**
     * Calculates the Attack delay in Ticks for Knights
     */
    @Override
    protected int getAttackDelay()
    {
        return KNIGHT_ATTACK_DELAY_BASE;
    }

    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        final List<ItemStack> list = super.itemsNiceToHave();
        list.add(new ItemStack(Items.SHIELD, 1));
        return list;
    }

    /**
     * Check if the guard can protect himself with a shield
     * And if so, do it.
     *
     * @return The next IAIState.
     */
    protected IAIState attackProtect()
    {
        final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), Items.SHIELD);

        if (target != null && target.isAlive())
        {
            final UnlockAbilityResearchEffect
              effect = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(SHIELD_USAGE, UnlockAbilityResearchEffect.class);
            if (effect != null && shieldSlot != -1)
            {
                worker.getCitizenItemHandler().setHeldItem(Hand.OFF_HAND, shieldSlot);
                worker.setActiveHand(Hand.OFF_HAND);

                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.decreaseSaturationForContinuousAction();
            }
        }

        return GUARD_ATTACK_PHYSICAL;
    }

    /**
     * attackPhysical tries to launch an attack. Ticked every 8 Ticks
     */
    protected IAIState attackPhysical()
    {
        final IAIState state = preAttackChecks();
        if (state != getState())
        {
            worker.getNavigator().clearPath();
            worker.getMoveHelper().strafe(0, 0);
            setDelay(STANDARD_DELAY);
            return state;
        }

        moveInAttackPosition();
        reduceAttackDelay(GUARD_ATTACK_INTERVAL);
        if (currentAttackDelay > 0)
        {
            return GUARD_ATTACK_PROTECT;
        }

        if (!isInAttackDistance(target.getPosition()))
        {
            return getState();
        }

        if (getOwnBuilding() != null)
        {
            currentAttackDelay = getAttackDelay();
            worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

            worker.swingArm(Hand.MAIN_HAND);
            worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));

            double damageToBeDealt = getAttackDamage();

            if (worker.getHealth() <= worker.getMaxHealth() * 0.2D)
            {
                damageToBeDealt *= 2;
            }

            final DamageSource source = new EntityDamageSource(worker.getName().getFormattedText(), worker);
            if (MineColonies.getConfig().getCommon().pvp_mode.get() && target instanceof PlayerEntity)
            {
                source.setDamageBypassesArmor();
            }

            final int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, worker.getHeldItem(Hand.MAIN_HAND));
            if (fireLevel > 0)
            {
                target.setFire(fireLevel * 80);
            }

            target.attackEntityFrom(source, (float) damageToBeDealt);
            target.setRevengeTarget(worker);
            worker.decreaseSaturationForContinuousAction();

            worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
        }
        return GUARD_ATTACK_PHYSICAL;
    }

    private int getAttackDamage()
    {
        if (worker.getCitizenData() != null)
        {
            int addDmg = 0;

            final ItemStack heldItem = worker.getHeldItem(Hand.MAIN_HAND);

            if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
            {
                if (heldItem.getItem() instanceof SwordItem)
                {
                    addDmg += ((SwordItem) heldItem.getItem()).getAttackDamage() + BASE_PHYSICAL_DAMAGE;
                }
                else
                {
                    addDmg += TinkersWeaponHelper.getDamage(heldItem);
                }
                addDmg += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute()) / 2.5;
            }

            addDmg += getLevelDamage();

            final AdditionModifierResearchEffect
              effect = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(MELEE_DAMAGE, AdditionModifierResearchEffect.class);
            if (effect != null)
            {
                addDmg += effect.getEffect();
            }

            return (int) ((addDmg) * MineColonies.getConfig().getCommon().knightDamageMult.get());
        }
        return (int) (BASE_PHYSICAL_DAMAGE * MineColonies.getConfig().getCommon().knightDamageMult.get());
    }

    @Override
    public void moveInAttackPosition()
    {
        worker.getNavigator().tryMoveToEntityLiving(target, getCombatMovementSpeed());
    }
}
