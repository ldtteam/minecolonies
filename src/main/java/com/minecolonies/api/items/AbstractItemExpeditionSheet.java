package com.minecolonies.api.items;

import com.minecolonies.core.items.AbstractItemMinecolonies;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for the expedition sheet item.
 */
public abstract class AbstractItemExpeditionSheet extends AbstractItemMinecolonies
{
    /**
     * Container class for expedition sheet information.
     *
     * @param colonyId     the id of the colony this expedition is for.
     * @param expeditionId the id of the expedition instance.
     */
    public record ExpeditionSheetInfo(int colonyId, int expeditionId) {}

    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name       The name of this item
     * @param properties the properties.
     */
    public AbstractItemExpeditionSheet(final String name, final Properties properties)
    {
        super(name, properties);
    }

    /**
     * Get the expedition sheet info on a given item stack.
     *
     * @param stack the item stack to check for.
     * @return the expedition sheet info or null.
     */
    @Nullable
    public abstract ExpeditionSheetInfo getExpeditionSheetInfo(final ItemStack stack);

    /**
     * Generate an item stack for the given expedition.
     *
     * @param expeditionSheetInfo the sheet info.
     * @return the generated item stack.
     */
    @NotNull
    public abstract ItemStack createItemStackForExpedition(final ExpeditionSheetInfo expeditionSheetInfo);
}
