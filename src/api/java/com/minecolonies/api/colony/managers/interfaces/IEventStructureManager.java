package com.minecolonies.api.colony.managers.interfaces;

import com.ldtteam.structures.helpers.Structure;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IEventStructureManager
{
    boolean spawnTemporaryStructure(Structure structure, String schematicPath, BlockPos targetSpawnPoint, int eventID, int rotations, Mirror mirror);

    void loadBackupForEvent(int eventID);

    void readFromNBT(@NotNull NBTTagCompound compound);

    void writeToNBT(@NotNull NBTTagCompound compound);
}
