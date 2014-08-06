package com.minecolonies.tileentities;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;

import java.util.*;

import static net.minecraftforge.common.util.Constants.NBT;

public class TileEntityTownHall extends TileEntityHut
{
    private String     cityName = "ERROR(Wasn't placed by player)";
    private List<UUID> owners   = new ArrayList<UUID>();

    private int maxCitizens;
    private List<UUID> citizens = new ArrayList<UUID>();
    private List<Vec3> huts     = new ArrayList<Vec3>();

    private Map<Vec3, String> builderRequired = new HashMap<Vec3, String>();

    private List<Integer> entityIDs = new ArrayList<Integer>();

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
            if(o instanceof TileEntityHut)
            {
                TileEntityHut hut = (TileEntityHut) o;
                if(hut.getDistanceFrom(getPosition()) < Math.pow(Configurations.workingRangeTownhall, 2))
                {
                    hut.setTownHall(this);
                    huts.add(hut.getPosition());
                }
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
                        citizen.getWorkHut().unbindWorker(citizen);
                    }
                    citizen.setDead();
                }
            }
        }
        for(Vec3 pos : huts)
        {
            TileEntityHut hut = (TileEntityHut) Vec3Utils.getTileEntityFromVec(worldObj, pos);
            if(hut != null)
            {
                hut.setTownHall(null);
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
            for(Vec3 coords : huts)
            {
                NBTTagCompound compound = new NBTTagCompound();
                Vec3Utils.writeVecToNBT(compound, "hut", coords);
                nbtTagBuildingsList.appendTag(compound);
            }
            nbtTagCompound.setTag("huts", nbtTagBuildingsList);
        }
        if(!builderRequired.isEmpty())
        {
            NBTTagList nbtTagBuildingsList = new NBTTagList();
            for(Map.Entry<Vec3, String> entry : builderRequired.entrySet())
            {
                NBTTagCompound compound = new NBTTagCompound();
                Vec3Utils.writeVecToNBT(compound, "coords", entry.getKey());
                compound.setString("name", entry.getValue());
                nbtTagBuildingsList.appendTag(compound);
            }
            nbtTagCompound.setTag("builderRequired", nbtTagBuildingsList);
        }
        nbtTagCompound.setTag("citizens", nbtTagCitizenList);
        nbtTagCompound.setTag("owners", nbtTagOwnersList);

        if(!worldObj.isRemote && !this.getCitizens().isEmpty())
        {
            List<Entity> entities = Utils.getEntitiesFromUUID(worldObj, this.getCitizens());

            NBTTagList nbtTagList = new NBTTagList();
            for(Entity entity : entities)
            {
                NBTTagCompound nbtTagCompoundEntityID = new NBTTagCompound();
                nbtTagCompoundEntityID.setInteger("id", entity.getEntityId());
                nbtTagList.appendTag(nbtTagCompoundEntityID);
            }
            nbtTagCompound.setTag("EntityIDs", nbtTagList);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        this.maxCitizens = nbtTagCompound.getInteger("maxCitizens");
        this.cityName = nbtTagCompound.getString("cityName");

        NBTTagList nbtTagOwnersList = nbtTagCompound.getTagList("owners", NBT.TAG_COMPOUND);
        NBTTagList nbtTagCitizenList = nbtTagCompound.getTagList("citizens", NBT.TAG_COMPOUND);
        NBTTagList nbtTagBuildingsList = nbtTagCompound.getTagList("huts", NBT.TAG_COMPOUND);
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
            Vec3 hut;
            if(nbtTagBuildingCompound.getTag("hut") instanceof NBTTagIntArray)
            {
                int[] coords = nbtTagBuildingCompound.getIntArray("hut");
                hut = Vec3.createVectorHelper(coords[0], coords[1], coords[2]);
            }
            else
            {
                hut = Vec3Utils.readVecFromNBT(nbtTagBuildingCompound, "hut");
            }
            huts.add(hut);
        }
        for(int i = 0; i < nbtTagBuilderRequiredList.tagCount(); i++)
        {
            NBTTagCompound nbtTagBuilderRequiredCompound = nbtTagBuilderRequiredList.getCompoundTagAt(i);
            Vec3 coords;
            if(nbtTagBuilderRequiredCompound.getTag("coords") instanceof NBTTagIntArray)
            {
                int[] hut = nbtTagBuilderRequiredCompound.getIntArray("coords");
                coords = Vec3.createVectorHelper(hut[0], hut[1], hut[2]);
            }
            else
            {
                coords = Vec3Utils.readVecFromNBT(nbtTagBuilderRequiredCompound, "coords");
            }
            String name = nbtTagBuilderRequiredCompound.getString("name");
            builderRequired.put(coords, name);
        }

        if(nbtTagCompound.hasKey("EntityIDs"))
        {
            entityIDs.clear();
            NBTTagList nbtList = nbtTagCompound.getTagList("EntityIDs", NBT.TAG_COMPOUND);

            for(int i = 0; i < nbtList.tagCount(); i++)
            {
                NBTTagCompound tag = nbtList.getCompoundTagAt(i);
                entityIDs.add(tag.getInteger("id"));
            }
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
        for(Vec3 vec : huts)
        {
            list.add((TileEntityBuildable) Vec3Utils.getTileEntityFromVec(worldObj, vec));
        }
        return list;
    }

    public void addHut(Vec3 pos)
    {
        huts.add(pos);
    }

    public void removeHut(Vec3 pos)
    {
        for(Vec3 coords : huts)
        {
            if(Vec3Utils.equals(pos, coords))
            {
                huts.remove(coords);
                return;
            }
        }
    }

    public void addHutForUpgrade(String name, Vec3 pos)
    {
        builderRequired.put(pos, name);
    }

    public void removeHutForUpgrade(Vec3 coords)
    {
        for(Vec3 key : builderRequired.keySet())
        {
            if(Vec3Utils.equals(coords, key))
            {
                builderRequired.remove(key);
                return;
            }
        }
    }

    public Map<Vec3, String> getBuilderRequired()
    {
        return builderRequired;
    }

    public List<Vec3> getDeliverymanRequired()
    {
        List<Vec3> deliverymanRequired = new ArrayList<Vec3>();
        for(Entity entity : Utils.getEntitiesFromUUID(worldObj, citizens))
        {
            if(entity instanceof EntityWorker)
            {
                EntityWorker worker = (EntityWorker) entity;
                if(worker.getWorkHut() != null && !worker.hasItemsNeeded())
                {
                    deliverymanRequired.add(worker.getWorkHut().getPosition());
                }
            }
        }
        return deliverymanRequired;
    }

    public List<Integer> getEntityIDs()
    {
        return entityIDs;
    }
}
