package com.github.lunatrius.schematica;

import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.File;

public class CommonProxy {
	public void registerKeybindings() {
	}

	public void registerEvents() {
	}

	public void createFolders() {
		Reference.schematicDirectory = Reference.config.schematicDirectory;

		if (!Reference.schematicDirectory.exists()) {
			if (!Reference.schematicDirectory.mkdirs()) {
				Reference.logger.info("Could not create schematic directory [%s]!", Reference.schematicDirectory.getAbsolutePath());
			}
		}
	}

	public File getDataDirectory() {
		return MinecraftServer.getServer().getFile(".");
	}

	public void setActiveSchematic(SchematicWorld world) {
	}

	public void setActiveSchematic(SchematicWorld world, EntityPlayer player) {
	}

	public SchematicWorld getActiveSchematic() {
		return null;
	}

	public SchematicWorld getActiveSchematic(EntityPlayer player) {
		return null;
	}
}
