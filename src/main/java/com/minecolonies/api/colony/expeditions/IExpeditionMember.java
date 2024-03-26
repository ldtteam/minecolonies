package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for expedition members.
 */
public interface IExpeditionMember<T extends ICivilianData>
{
    /**
     * Handler methods for calculating damage reduction based on available armor.
     *
     * @param civilianData the civilian data.
     * @param damageSource the damage source.
     * @param damage       the amount of incoming damage.
     * @return the calculated damage after absorption.
     */
    static float handleDamageReduction(final @NotNull ICivilianData civilianData, final DamageSource damageSource, final float damage)
    {
        final ItemStack head = civilianData.getInventory().getArmorInSlot(EquipmentSlot.HEAD);
        final ItemStack chest = civilianData.getInventory().getArmorInSlot(EquipmentSlot.CHEST);
        final ItemStack legs = civilianData.getInventory().getArmorInSlot(EquipmentSlot.LEGS);
        final ItemStack feet = civilianData.getInventory().getArmorInSlot(EquipmentSlot.FEET);

        final int armorPieces = (head.isEmpty() ? 0 : 1) + (chest.isEmpty() ? 0 : 1) + (legs.isEmpty() ? 0 : 1) + (feet.isEmpty() ? 0 : 1);
        if (armorPieces > 0)
        {
            final float dividedDamage = damage / armorPieces;

            float finalDamage = damage;
            if (!head.isEmpty() && head.getItem() instanceof ArmorItem armorItem && damageSource.getEntity() != null)
            {
                head.hurtAndBreak(Math.round(dividedDamage), (LivingEntity) damageSource.getEntity(), e -> {});
                finalDamage = CombatRules.getDamageAfterAbsorb(finalDamage, armorItem.getDefense(), armorItem.getToughness());
            }
            return finalDamage;
        }

        return damage;
    }

    /**
     * Get the id of the expedition member.
     *
     * @return the civilian id.
     */
    int getId();

    /**
     * Get the name of the expedition member.
     *
     * @return the name of the civilian.
     */
    String getName();

    /**
     * The health this member has.
     *
     * @return the current health.
     */
    float getHealth();

    /**
     * The max health this member has.
     *
     * @return the max health.
     */
    float getMaxHealth();

    /**
     * The damage this member can deal.
     *
     * @return the attack damage.
     */
    float getAttackDamage();

    /**
     * Hurt this expedition member.
     *
     * @param colony the colony where this member is from.
     * @param amount the amount to heal.
     */
    void heal(final IColony colony, final float amount);

    /**
     * Hurt this expedition member.
     *
     * @param colony       the colony where this member is from.
     * @param damageSource the incoming damage source.
     * @param amount       the amount of damage.
     */
    void hurt(final IColony colony, final DamageSource damageSource, final float amount);

    /**
     * Get whether this expedition member died during the expedition.
     *
     * @return true if so.
     */
    boolean isDead();

    /**
     * Attempt to resolve the civilian data for this expedition member.
     * May return null for multiple reasons.
     *
     * @param colony the colony where this member is from.
     * @return the civilian data, or null.
     */
    @Nullable
    T resolveCivilianData(final IColony colony);

    /**
     * Write this member to compound data.
     *
     * @param compound the compound tag.
     */
    void write(final CompoundTag compound);

    /**
     * Indicates to the colony the member should be removed because they died during the expedition.
     */
    void removeFromColony(final IColony colony);
}