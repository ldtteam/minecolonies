package com.github.lunatrius.schematica;

import java.io.File;

public class FileFilterSchematic implements java.io.FileFilter {
	private final boolean directory;

	public FileFilterSchematic(boolean dir) {
		super();
		this.directory = dir;
	}

	@Override
	public boolean accept(File file) {
		if (this.directory) {
			return file.isDirectory();
		}

		return file.getName().toLowerCase().endsWith(".schematic");
	}
}
