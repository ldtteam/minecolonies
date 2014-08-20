package com.minecolonies.util;

import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Vec3Utils
{
    public static double distanceTo(Vec3 vec, double x, double y, double z)
    {
        return vec.distanceTo(Vec3.createVectorHelper(x, y, z));
    }

    public static boolean equals(Vec3 vec, double x, double y, double z)
    {
        return equals(vec, Vec3.createVectorHelper(x, y, z));
    }

    public static boolean equals(Vec3 vec1, Vec3 vec2)
    {
        return vec1.xCoord == vec2.xCoord && vec1.yCoord == vec2.yCoord && vec1.zCoord == vec2.zCoord;
    }
}
