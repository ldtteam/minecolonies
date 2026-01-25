package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.inventory.InventoryCitizen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for expedition members.
 */
public interface IExpeditionMember<T extends ICivilianData>
{
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
     * Get the health this member has.
     *
     * @return the current health.
     */
    float getHealth();

    /**
     * Set the new health for this member.
     *
     * @param health the new health.
     */
    void setHealth(float health);

    /**
     * Get the max health this member has.
     *
     * @return the max health.
     */
    float getMaxHealth();

    /**
     * Get whether this expedition member died during the expedition.
     *
     * @return true if so.
     */
    boolean isDead();

    /**
     * Get the weapon this member is carrying.
     *
     * @return the weapon item stack.
     */
    ItemStack getPrimaryWeapon();

    /**
     * Get the inventory for this expedition member.
     *
     * @return the inventory.
     */
    InventoryCitizen getInventory();

    /**
     * Attempt to resolve the civilian data for this expedition member.
     * May return null for multiple reasons.
     *
     * @param colony the colony where this member is from.
     * @return the civilian data, or null.
     */
    @Nullable T resolveCivilianData(final IColony colony);

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
