package com.minecolonies.coremod.colony.materials;

import net.minecraftforge.fml.common.registry.GameData;

/**
 * A MaterialException for not enough items.
 */
class QuantityNotFound extends RuntimeException
{

    public static final long serialVersionUID = -703925360745766939L;

    //todo: We need to change registry access
    @SuppressWarnings("deprecation")
    /**
     * Create a new Exception fo this type.
     *
     * @param location the location where this occured.
     * @param id       the material id
     * @param count    the number of items currently there
     * @param quantity the quantity needed/requested
     */
    public QuantityNotFound(final String location, final int id, final int count, final int quantity)
    {
        super(location + " doesn't contain enough items: " + count + " < " + quantity + ". For material: " + id
                + " Block: " + GameData.getBlockRegistry().getObjectById(id)
                + " Item: " + GameData.getItemRegistry().getObjectById(id));
    }
}
