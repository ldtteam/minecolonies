package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.compatibility.tinkers.TinkersWeaponHelper;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.guards.GuardGear;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.util.NamedDamageSource;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_ATTACK_PHYSICAL;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.GUARD_ATTACK_PROTECT;
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
            list.add(new GuardGear(ToolType.SHIELD, EntityEquipmentSlot.MAINHAND, 0, 0, SHIELD_LEVEL_RANGE, SHIELD_BUILDING_LEVEL_RANGE));
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
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.SWORD, 0, buildingGuards.getMaxToolLevel()) != -1;
    }

    @Override
    public void wearWeapon()
    {
        final int weaponSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.SWORD, 0, buildingGuards.getMaxToolLevel());

        if (weaponSlot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, weaponSlot);
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
        final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()),
          Items.SHIELD,
          -1);

        if (target != null && !target.isDead)
        {
            if (shieldSlot != -1)
            {
                worker.getCitizenItemHandler().setHeldItem(EnumHand.OFF_HAND, shieldSlot);
                worker.setActiveHand(EnumHand.OFF_HAND);

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

            worker.swingArm(EnumHand.MAIN_HAND);
            worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));

            double damageToBeDealt = getAttackDamage();

            if (worker.getHealth() <= worker.getMaxHealth() * 0.2D)
            {
                damageToBeDealt *= 2;
            }

            final DamageSource source = new NamedDamageSource(worker.getName(), worker);
            if (Configurations.gameplay.pvp_mode && target instanceof EntityPlayer)
            {
                source.setDamageBypassesArmor();
            }

            final int fireLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, worker.getHeldItem(EnumHand.MAIN_HAND));
            if (fireLevel > 0)
            {
                target.setFire(fireLevel * 80);
            }

            target.attackEntityFrom(source, (float) damageToBeDealt);
            target.setRevengeTarget(worker);
            worker.decreaseSaturationForContinuousAction();

            worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
        }
        return GUARD_ATTACK_PHYSICAL;
    }

    private int getAttackDamage()
    {
        if (worker.getCitizenData() != null)
        {
            int addDmg = 0;

            final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);

            if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
            {
                if (heldItem.getItem() instanceof ItemSword)
                {
                    addDmg += ((ItemSword) heldItem.getItem()).getAttackDamage() + BASE_PHYSICAL_DAMAGE;
                }
                else
                {
                    addDmg += TinkersWeaponHelper.getDamage(heldItem);
                }
                addDmg += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute()) / 2.5;
            }

            addDmg += getLevelDamage();
            return (int) ((addDmg) * Configurations.gameplay.knightDamageMult);
        }
        return (int) (BASE_PHYSICAL_DAMAGE * Configurations.gameplay.knightDamageMult);
    }

    @Override
    public void moveInAttackPosition()
    {
        worker.getNavigator().tryMoveToEntityLiving(target, getCombatMovementSpeed());
    }
}
