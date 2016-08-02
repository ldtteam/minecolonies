package com.schematica.world.schematic;

class UnsupportedFormatException extends Exception
{
    public UnsupportedFormatException(String format)
    {
        super(String.format("Unsupported format: %s", format));
    }
}
