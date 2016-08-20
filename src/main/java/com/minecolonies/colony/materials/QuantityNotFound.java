package com.minecolonies.colony.materials;

import net.minecraftforge.fml.common.registry.GameData;

/**
 * Created by Colton, fixed and moved by Isfirs.
 *
 * @author Colton
 * @author Isfirs
 */
class QuantityNotFound extends MaterialException
{
    QuantityNotFound(String location, int id, int count, int quantity)
    {
        super(location + " doesn't contain enough items: " + count + " < " + quantity + ". For material: " + id
              + " Block: " + GameData.getBlockRegistry().getObjectById(id)
              + " Item: " + GameData.getItemRegistry().getObjectById(id));
    }
}