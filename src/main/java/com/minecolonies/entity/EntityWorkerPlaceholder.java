package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIWorkBuilder;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Schematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class EntityWorkerPlaceholder extends EntityWorker
{
    private Schematic schematic;

    public EntityWorkerPlaceholder(World world)
    {
        super(world);
    }

    @Override
    protected String getJobName()
    {
        return "Citizen";
    }

    @Override
    public int getTextureID()//TODO remove method once more textures are added
    {
        return 1;
    }

    @Override
    public boolean isNeeded()
    {
        return false;
    }
}
