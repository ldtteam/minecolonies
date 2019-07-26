package com.minecolonies.coremod.entity.citizenhandlers;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICitizenItemHandler
{
    /**
     * Citizen will try to pick up a certain item.
     *
     * @param entityItem the item he wants to pickup.
     */
    void tryPickupEntityItem(@NotNull EntityItem entityItem);

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
    void setHeldItem(EnumHand hand, int slot);

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
     *
     * @param damage amount of damage.
     */
    void damageItemInHand(EnumHand hand, int damage);

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
    EntityItem entityDropItem(@NotNull ItemStack itemstack);

    /**
     * Updates the armour damage after being hit.
     *
     * @param damage damage dealt.
     */
    void updateArmorDamage(double damage);
}
