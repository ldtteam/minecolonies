package com.minecolonies.tileentities;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.UUID;

public class TileEntityTownHall extends TileEntityHut
{
    private String          cityName;
    private ArrayList<UUID> owners;
    private BiomeGenBase    biome;//TODO do we plan on useing this?

    private ArrayList<UUID>  citizens;
    private int              maxCitizens;
    private ArrayList<int[]> huts; //Stores XYZ's

    private ArrayList<int[]> builderRequired; //Stores XYZ's

    public TileEntityTownHall()
    {
        setHutName("Townhall");
        owners = new ArrayList<UUID>();
        citizens = new ArrayList<UUID>();
        maxCitizens = com.minecolonies.lib.Constants.DEFAULTMAXCITIZENS;
        huts = new ArrayList<int[]>();
        builderRequired = new ArrayList<int[]>();
    }

    public void setCityName(String cityName)
    {
        this.cityName = cityName;
    }

    public void onBlockAdded()
    {
        for(Object o : worldObj.loadedTileEntityList)
            if(o instanceof TileEntityHut && Utils.getDistanceToClosestTownHall(worldObj, xCoord, yCoord, zCoord) < Configurations.workingRangeTownhall)
            {
                TileEntityHut tileEntityHut = (TileEntityHut) o;
                if(tileEntityHut.getTownHall() == null)
                {
                    tileEntityHut.setTownHall(this);
                    huts.add(new int[]{((TileEntityHut) o).xCoord, ((TileEntityHut) o).yCoord, ((TileEntityHut) o).zCoord});
                }
            }
    }

    public void setInfo(World w, UUID ownerName, int x, int z)
    {
        owners.add(ownerName);
        biome = w.getBiomeGenForCoords(x, z);
    }

    @Override
    public void updateEntity()
    {
        int respawnInterval = Configurations.citizenRespawnInterval * 20;
        respawnInterval -= (60 * getBuildingLevel());

        if(worldObj.getWorldInfo().getWorldTime() % respawnInterval == 0)
        {
            if(getCitizens().size() < getMaxCitizens())
            {
                Vec3 spawnPoint = Utils.scanForBlockNearPoint(worldObj, Blocks.air, xCoord, yCoord, zCoord, 1, 0, 1);
                if(spawnPoint == null)
                    spawnPoint = Utils.scanForBlockNearPoint(worldObj, Blocks.snow_layer, xCoord, yCoord, zCoord, 1, 0, 1);

                if(spawnPoint != null)
                {
                    EntityCitizen ec = spawnCitizen(spawnPoint.xCoord, spawnPoint.yCoord, spawnPoint.zCoord);
                    if(ec != null)
                    {
                        addCitizen(ec);
                        if(getMaxCitizens() == getCitizens().size())
                        {
                            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(worldObj, owners), "tile.blockHutTownhall.messageMaxSize");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("maxCitizens", maxCitizens);
        nbtTagCompound.setString("cityName", cityName);
        NBTTagList nbtTagOwnersList = new NBTTagList();
        for(UUID owner : owners)
        {
            NBTTagCompound nbtTagOwnersCompound = new NBTTagCompound();
            nbtTagOwnersCompound.setString("owner", owner.toString());
            nbtTagOwnersList.appendTag(nbtTagOwnersCompound);
        }
        NBTTagList nbtTagCitizenList = new NBTTagList();
        for(UUID citizens : getCitizens())
        {
            NBTTagCompound nbtTagCitizenCompound = new NBTTagCompound();
            nbtTagCitizenCompound.setString("citizen", citizens.toString());
            nbtTagCitizenList.appendTag(nbtTagCitizenCompound);
        }
        if(!huts.isEmpty())
        {
            NBTTagList nbtTagBuildingsList = new NBTTagList();
            for(int[] coords : huts)
            {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setIntArray("hut", coords);
                nbtTagBuildingsList.appendTag(compound);
            }
            nbtTagCompound.setTag("huts", nbtTagBuildingsList);
        }
        if(!builderRequired.isEmpty())
        {
            NBTTagList nbtTagBuildingsList = new NBTTagList();
            for(int[] coords : builderRequired)
            {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setIntArray("builderRequired", coords);
                nbtTagBuildingsList.appendTag(compound);
            }
            nbtTagCompound.setTag("builderRequireds", nbtTagBuildingsList);
        }
        nbtTagCompound.setTag("citizens", nbtTagCitizenList);
        nbtTagCompound.setTag("owners", nbtTagOwnersList);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        this.maxCitizens = nbtTagCompound.getInteger("maxCitizens");
        this.cityName = nbtTagCompound.getString("cityName");
        NBTTagList nbtTagOwnersList = nbtTagCompound.getTagList("owners", Constants.NBT.TAG_COMPOUND);
        NBTTagList nbtTagCitizenList = nbtTagCompound.getTagList("citizens", Constants.NBT.TAG_COMPOUND);
        NBTTagList nbtTagBuildingsList = nbtTagCompound.getTagList("huts", Constants.NBT.TAG_INT_ARRAY);
        NBTTagList nbtTagBuildersNeededList = nbtTagCompound.getTagList("builderRequireds", Constants.NBT.TAG_COMPOUND);
        this.owners = new ArrayList<UUID>();
        this.citizens = new ArrayList<UUID>();
        this.huts = new ArrayList<int[]>();
        this.builderRequired = new ArrayList<int[]>();
        for(int i = 0; i < nbtTagOwnersList.tagCount(); i++)
        {
            NBTTagCompound nbtTagOwnersCompound = nbtTagOwnersList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagOwnersCompound.getString("owner"));
            owners.add(i, uuid);
        }
        for(int i = 0; i < nbtTagCitizenList.tagCount(); i++)
        {
            NBTTagCompound nbtTagCitizenCompound = nbtTagCitizenList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagCitizenCompound.getString("citizen"));
            citizens.add(i, uuid);
        }
        for(int i = 0; i < nbtTagBuildingsList.tagCount(); i++)
        {
            NBTTagCompound nbtTagBuildingCompound = nbtTagBuildingsList.getCompoundTagAt(i);
            int[] hut = nbtTagBuildingCompound.getIntArray("hut");
            huts.add(i, hut);
        }
        for(int i = 0; i < nbtTagBuildersNeededList.tagCount(); i++)
        {
            NBTTagCompound nbtTagBuildingsListCompoundTag = nbtTagBuildingsList.getCompoundTagAt(i);
            int[] hut = nbtTagBuildingsListCompoundTag.getIntArray("builderRequired");
            builderRequired.add(i, hut);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        this.readFromNBT(packet.func_148857_g());
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket()
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.writeToNBT(nbtTagCompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbtTagCompound);
    }

    @Override
    public TileEntityTownHall getTownHall()
    {
        return this;
    }

    public ArrayList<UUID> getOwners()
    {
        return owners;
    }

    public String getCityName()
    {
        return cityName;
    }

    public void addCitizen(EntityCitizen citizen)
    {
        citizen.setTownHall(this);
        citizens.add(citizen.getUniqueID());
    }

    public void removeCitizen(EntityCitizen citizen)
    {
        if(citizens.contains(citizen.getUniqueID())) citizens.remove(citizen.getUniqueID());
    }

    public ArrayList<UUID> getCitizens()
    {
        return citizens;
    }

    public int getMaxCitizens()
    {
        return maxCitizens;
    }

    public void setMaxCitizens(int maxCitizens)
    {
        this.maxCitizens = maxCitizens;
    }

    public void addCitizenToTownhall(EntityCitizen entityCitizen)
    {
        if(getCitizens() != null) this.addCitizen(entityCitizen);
    }

    public EntityCitizen spawnCitizen(double x, double y, double z)
    {
        if(worldObj.isRemote) return null;

        EntityCitizen ec = new EntityCitizen(worldObj);
        ec.setPosition(x, y, z);
        worldObj.spawnEntityInWorld(ec);
        return ec;
    }

    public ArrayList<TileEntityBuildable> getHuts()
    {
        ArrayList<TileEntityBuildable> list = new ArrayList<TileEntityBuildable>();
        for(int[] i : huts)
        {
            list.add((TileEntityBuildable)worldObj.getTileEntity(i[0], i[1], i[2]));
        }
        return list;
    }

    public void addHut(int x, int y, int z)
    {
        huts.add(new int[]{x, y, z});
    }

    public void addHutForUpgrade(int x, int y, int z)
    {
        builderRequired.add(new int[] {x, y, z});
    }

    public ArrayList<int[]> getBuilderRequired()
    {
        return builderRequired;
    }
}
