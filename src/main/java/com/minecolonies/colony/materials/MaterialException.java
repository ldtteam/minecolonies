package com.minecolonies.colony.materials;

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


