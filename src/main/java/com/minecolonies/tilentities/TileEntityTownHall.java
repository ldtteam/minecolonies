package com.minecolonies.tilentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;

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
        markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setString("cityName", cityName);
        NBTTagList nbtTagOwnersList = new NBTTagList();
        for(UUID owner : owners)
        {
            NBTTagCompound nbtTagOwnersCompound = new NBTTagCompound();
            nbtTagOwnersCompound.setString("owner", owner.toString());
            nbtTagOwnersList.appendTag(nbtTagOwnersCompound);
        }
        nbtTagCompound.setTag("owners", nbtTagOwnersList);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        this.cityName = nbtTagCompound.getString("cityName");
        NBTTagList nbtTagOwnersList = nbtTagCompound.getTagList("owners", Constants.NBT.TAG_COMPOUND);
        this.owners = new ArrayList<UUID>();
        for(int i = 0; i < nbtTagOwnersList.tagCount(); i++)
        {
            NBTTagCompound nbtTagOwnersCompound = nbtTagOwnersList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagOwnersCompound.getString("owner"));
            owners.add(i, uuid);
            System.out.println("UUID Size = " + this.owners.size());
            System.out.println("UUID name = " + this.owners.get(i).toString());
        }
    }

    public ArrayList<UUID> getOwners()
    {
        return owners;
    }
}
