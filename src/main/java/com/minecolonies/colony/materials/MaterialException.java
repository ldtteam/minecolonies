package com.minecolonies.colony.materials;

/**
 * Exception for material handling. If this exception is thrown then their is a problem with the Material System's counting.
 * Created: December 15, 2015
 *
 * @author Colton
 */
public class MaterialException extends RuntimeException
{
    public MaterialException(String message)
    {
        super(message);
    }
}
