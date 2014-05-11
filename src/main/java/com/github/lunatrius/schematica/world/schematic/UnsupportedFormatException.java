package com.github.lunatrius.schematica.world.schematic;

public class UnsupportedFormatException extends Exception {
	public UnsupportedFormatException(String format) {
		super(String.format("Unsupported format: %s", format));
	}
}
