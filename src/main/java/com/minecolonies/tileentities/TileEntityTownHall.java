package com.minecolonies.tileentities;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;

import java.util.*;

import static net.minecraftforge.common.util.Constants.NBT;

public class TileEntityTownHall extends TileEntityHut
{
    private String     cityName = "ERROR(Wasn't placed by player)";
    private List<UUID> owners   = new ArrayList<UUID>();

    private int maxCitizens;
    private List<UUID>  citizens = new ArrayList<UUID>();
    private List<int[]> huts     = new ArrayList<int[]>(); //Stores XYZ's

    private Map<int[], String> builderRequired = new HashMap<int[], String>(); //Stores XYZ's //TODO make this a Vec3

    public TileEntityTownHall()
    {
        maxCitizens = Constants.DEFAULTMAXCITIZENS;
    }

    @Override
    public String getName()
    {
        return "Townhall";
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
                TileEntityHut hut = (TileEntityHut) o;
                hut.setTownHall(this);
                huts.add(new int[]{hut.xCoord, hut.yCoord, hut.zCoord});
            }
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
    public void breakBlock()
    {
        for(Object o : worldObj.loadedEntityList)
        {
            if(o instanceof EntityCitizen)
            {
                EntityCitizen citizen = (EntityCitizen) o;
                if(this.getCitizens().contains(citizen.getUniqueID()))
                {
                    this.removeCitizen(citizen);
                    if(citizen.getHomeHut() != null)
                    {
                        citizen.getHomeHut().removeCitizen(citizen);
                    }
                    if(citizen.getWorkHut() != null)
                    {
                        citizen.removeFromWorkHut();
                    }
                    citizen.setDead();
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
        for(UUID citizen : getCitizens())
        {
            NBTTagCompound nbtTagCitizenCompound = new NBTTagCompound();
            nbtTagCitizenCompound.setString("citizen", citizen.toString());
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
            for(Map.Entry<int[], String> entry : builderRequired.entrySet())
            {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setIntArray("coords", entry.getKey());
                compound.setString("name", entry.getValue());
                nbtTagBuildingsList.appendTag(compound);
            }
            nbtTagCompound.setTag("builderRequired", nbtTagBuildingsList);
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

        NBTTagList nbtTagOwnersList = nbtTagCompound.getTagList("owners", NBT.TAG_COMPOUND);
        NBTTagList nbtTagCitizenList = nbtTagCompound.getTagList("citizens", NBT.TAG_COMPOUND);
        NBTTagList nbtTagBuildingsList = nbtTagCompound.getTagList("huts", NBT.TAG_INT_ARRAY);
        NBTTagList nbtTagBuilderRequiredList = nbtTagCompound.getTagList("builderRequired", NBT.TAG_COMPOUND);
        this.owners.clear();
        this.citizens.clear();
        this.huts.clear();
        this.builderRequired.clear();
        for(int i = 0; i < nbtTagOwnersList.tagCount(); i++)
        {
            NBTTagCompound nbtTagOwnersCompound = nbtTagOwnersList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagOwnersCompound.getString("owner"));
            owners.add(uuid);
        }
        for(int i = 0; i < nbtTagCitizenList.tagCount(); i++)
        {
            NBTTagCompound nbtTagCitizenCompound = nbtTagCitizenList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagCitizenCompound.getString("citizen"));
            citizens.add(uuid);
        }
        for(int i = 0; i < nbtTagBuildingsList.tagCount(); i++)
        {
            NBTTagCompound nbtTagBuildingCompound = nbtTagBuildingsList.getCompoundTagAt(i);
            int[] hut = nbtTagBuildingCompound.getIntArray("hut");
            huts.add(hut);
        }
        for(int i = 0; i < nbtTagBuilderRequiredList.tagCount(); i++)
        {
            NBTTagCompound nbtTagBuilderRequiredCompound = nbtTagBuilderRequiredList.getCompoundTagAt(i);
            int[] coords = nbtTagBuilderRequiredCompound.getIntArray("coords");
            String name = nbtTagBuilderRequiredCompound.getString("name");
            builderRequired.put(coords, name);
        }
    }

    public void addOwner(UUID ownerName)
    {
        owners.add(ownerName);
    }

    @Override
    public TileEntityTownHall getTownHall()
    {
        return this;
    }

    public List<UUID> getOwners()
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
        for(UUID id : citizens)
        {
            if(citizen.getUniqueID().equals(id))
            {
                citizens.remove(id);
                return;
            }
        }
    }

    public List<UUID> getCitizens()
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

    public List<TileEntityBuildable> getHuts()
    {
        List<TileEntityBuildable> list = new ArrayList<TileEntityBuildable>();
        for(int[] i : huts)
        {
            list.add((TileEntityBuildable) worldObj.getTileEntity(i[0], i[1], i[2]));
        }
        return list;
    }

    public void addHut(int x, int y, int z)
    {
        huts.add(new int[]{x, y, z});
    }

    public void removeHut(int x, int y, int z)
    {
        for(int[] coords : huts)
        {
            if(Arrays.equals(new int[]{x, y, z}, coords))
            {
                huts.remove(coords);
                return;
            }
        }
    }

    public void addHutForUpgrade(String name, int x, int y, int z)
    {
        builderRequired.put(new int[]{x, y, z}, name);
    }

    public void removeHutForUpgrade(int[] coords)
    {
        for(int[] key : builderRequired.keySet())
        {
            if(Arrays.equals(coords, key))
            {
                builderRequired.remove(key);
                return;
            }
        }
    }

    public Map<int[], String> getBuilderRequired()
    {
        return builderRequired;
    }
}
