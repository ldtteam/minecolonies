package com.minecolonies.tilentities;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.UUID;

public class TileEntityTownHall extends TileEntityHut
{
    private String            cityName;
    private ArrayList<UUID>   owners;
    private BiomeGenBase      biome;

    public TileEntityTownHall()
    {
        owners = new ArrayList<UUID>();
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

    public void setInfo(World w, UUID ownerName, int x, int z)
    {
        owners.add(ownerName);
        biome = w.getBiomeGenForCoords(x, z);
    }
}
