package com.minecolonies.api.items;

import com.minecolonies.api.colony.expeditions.ExpeditionTokenData;
import com.minecolonies.core.items.AbstractItemMinecolonies;
import net.minecraft.nbt.CompoundTag;

/**
 * Base class for the adventure token.
 */
public abstract class AbstractItemAdventureToken extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name       The name of this item
     * @param properties the properties.
     */
    public AbstractItemAdventureToken(final String name, final Properties properties)
    {
        super(name, properties);
    }

    /**
     * Create usage data for colony expeditions.
     *
     * @return the usage instance.
     */
    public abstract Usage<ExpeditionTokenData> forExpedition();

    /**
     * Interface for defining usage logic.
     *
     * @param <D> the type of information that this adventure token holds.
     */
    public interface Usage<D>
    {
        /**
         * Serialize this usage to compound data.
         *
         * @param data the incoming data.
         * @return the compound data.
         */
        CompoundTag serialize(final D data);

        /**
         * Deserialize this usage from compound data.
         *
         * @param compound the compound data.
         * @return the outgoing data.
         */
        D deserialize(final CompoundTag compound);
    }
}
