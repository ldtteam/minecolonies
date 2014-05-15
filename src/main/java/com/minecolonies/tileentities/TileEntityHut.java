package com.minecolonies.tileentities;

import net.minecraft.block.Block;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityHut extends TileEntityBuildable
{
    private int maxInhabitants;
    private int maleInhabitants;
    private int femaleInhabitants;

    public TileEntityHut()
    {

    }

    public int getMaxInhabitants()
    {
        return maxInhabitants;
    }

    public void setMaxInhabitants(int maxInhabitants)
    {
        this.maxInhabitants = maxInhabitants;
    }

    public int getMaleInhabitants()
    {
        return maleInhabitants;
    }

    public void setMaleInhabitants(int maleInhabitants)
    {
        this.maleInhabitants = maleInhabitants;
    }

    public int getFemaleInhabitants()
    {
        return femaleInhabitants;
    }

    public void setFemaleInhabitants(int femaleInhabitants)
    {
        this.femaleInhabitants = femaleInhabitants;
    }

    protected Vec3 scanForBlockNearPoint(World world, Block block, int x, int y, int z, int rx, int ry, int rz)
    {
        Vec3 entityVec = Vec3.createVectorHelper(x, y, z);

        Vec3 closestVec = null;
        double minDistance = 999999999;

        for (int i = x - rx; i <= x + rx; i++)
        {
            for (int j = y - ry; j <= y + ry; j++)
            {
                for (int k = z - rz; k <= z + rz; k++)
                {
                    if (world.getBlock(i, j, k) == block)
                    {
                        Vec3 tempVec = Vec3.createVectorHelper(i, j, k);

                        if (closestVec == null || tempVec.distanceTo(entityVec) < minDistance)
                        {
                            closestVec = tempVec;
                            minDistance = closestVec.distanceTo(entityVec);
                        }
                    }
                }
            }
        }
        return closestVec;
    }
}
