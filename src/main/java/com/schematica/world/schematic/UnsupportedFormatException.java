package com.schematica.world.schematic;

class UnsupportedFormatException extends Exception
{
    public static final long serialVersionUID = -987325498762354L;
    public UnsupportedFormatException(String format)
    {
        super(String.format("Unsupported format: %s", format));
    }
}
