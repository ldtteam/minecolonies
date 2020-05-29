package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICitizenItemHandler
{
    /**
     * Citizen will try to pick up a certain item.
     *
     * @param ItemEntity the item he wants to pickup.
     */
    void tryPickupItemEntity(@NotNull ItemEntity ItemEntity);

    /**
     * Removes the currently held item.
     */
    void removeHeldItem();

    /**
     * Sets the currently held item.
     *
     * @param hand what hand we're setting
     * @param slot from the inventory slot.
     */
    void setHeldItem(Hand hand, int slot);

    /**
     * Sets the currently held for mainHand item.
     *
     * @param slot from the inventory slot.
     */
    void setMainHeldItem(int slot);

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * Will not break the block.
     *
     * @param blockPos Block position.
     */
    void hitBlockWithToolInHand(@Nullable BlockPos blockPos);

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * If breakBlock is true then it will break the block (different sound and
     * particles), and damage the tool in the citizens hand.
     *
     * @param blockPos   Block position.
     * @param breakBlock if we want to break this block.
     */
    void hitBlockWithToolInHand(@Nullable BlockPos blockPos, boolean breakBlock);

    /**
     * Damage the current held item.
     * @param hand hand the item is in.
     * @param damage amount of damage.
     */
    void damageItemInHand(Hand hand, int damage);

    /**
     * Pick up all items in a range around the citizen.
     */
    void pickupItems();

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * This will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param blockPos Block position.
     */
    void breakBlockWithToolInHand(@Nullable BlockPos blockPos);

    /**
     * Handles the dropping of items from the entity.
     *
     * @param itemstack to drop.
     * @return the dropped item.
     */
    ItemEntity entityDropItem(@NotNull ItemStack itemstack);

    /**
     * Updates the armour damage after being hit.
     *
     * @param damage damage dealt.
     */
    void updateArmorDamage(double damage);

    /**
     * Apply mending to the armour.
     * @return the remaining xp.
     * @param localXp the xp to add.
     */
    double applyMending(final double localXp);
}
