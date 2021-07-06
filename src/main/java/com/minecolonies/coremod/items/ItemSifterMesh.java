package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling Sifter Mesh.
 */
public class ItemSifterMesh extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the Sifter Mesh item.
     *
     * @param properties the properties.
     */
    public ItemSifterMesh(
        @NotNull final String name,
        final Properties properties)
    {
        super(name, properties.tab(ModCreativeTabs.MINECOLONIES));
    }
}
