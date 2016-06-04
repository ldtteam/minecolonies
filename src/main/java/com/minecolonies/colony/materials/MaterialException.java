package com.minecolonies.colony.materials;

import net.minecraftforge.fml.common.registry.GameData;

/**
 * Exception for material handling. If this exception is thrown then their is a problem with the Material System's counting.
 * Created: December 15, 2015
 *
 * @author Colton
 */
class MaterialException extends RuntimeException
{
    MaterialException(String message)
    {
        super(message);
    }
}

class QuantityNotFound extends MaterialException
{
    QuantityNotFound(String location, Material material, int count, int quantity)
    {
        super(location + " doesn't contain enough items: " + count + " < " + quantity + ". For material: " + material.toString());
    }
}