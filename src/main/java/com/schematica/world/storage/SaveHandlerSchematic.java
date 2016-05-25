package com.schematica.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

public class SaveHandlerSchematic implements ISaveHandler {
    @Override
    public WorldInfo loadWorldInfo() {
        return null;
    }

    @Override
    public void checkSessionLock() throws MinecraftException {}

    @Override
    public IChunkLoader getChunkLoader(final WorldProvider provider) {
        return null;
    }

    @Override
    public void saveWorldInfoWithPlayer(final WorldInfo info, final NBTTagCompound compound) {}

    @Override
    public void saveWorldInfo(final WorldInfo info) {}

    @Override
    public IPlayerFileData getPlayerNBTManager() {
        return null;
    }

    @Override
    public void flush() {}

    @Override
    public File getWorldDirectory() {
        return null;
    }

    @Override
    public File getMapFileFromName(final String name) {
        return null;
    }

    @Override
    public String getWorldDirectoryName() {
        return null;
    }
}
