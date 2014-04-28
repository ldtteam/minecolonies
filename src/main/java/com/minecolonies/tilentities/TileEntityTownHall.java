package com.minecolonies.tilentities;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class TileEntityTownHall extends TileEntityHut
{
    private String       cityName;
    private String       ownerName;
    private BiomeGenBase biome;

    public TileEntityTownHall()
    {

    }

    public void setCityName(String cityName)
    {
        this.cityName = cityName;
    }

    public void onBlockAdded()
    {
        for(Object o : worldObj.loadedTileEntityList)
            if(o instanceof TileEntityHut)
            {
                TileEntityHut tileEntityHut = (TileEntityHut) o;
                tileEntityHut.findTownHall();
            }
    }

    public void setInfo(World w, String ownerName, int x, int z)
    {
        this.ownerName = ownerName;
        biome = w.getBiomeGenForCoords(x, z);
    }
}
