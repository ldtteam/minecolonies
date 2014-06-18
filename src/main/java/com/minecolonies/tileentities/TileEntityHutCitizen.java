package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileEntityHutCitizen extends TileEntityHut
{
    private List<UUID> citizens = new ArrayList<UUID>();

    public TileEntityHutCitizen()
    {
        setMaxInhabitants(2);
    }

    @Override
    public String getName()
    {
        return "hutCitizen";
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if(worldObj.isRemote) return;

        if(citizens.size() < getMaxInhabitants() && this.getTownHall() != null)
        {
            addHomelessCitizen(this.getTownHall());
        }
    }

    private void addHomelessCitizen(TileEntityTownHall townhall)
    {
        List<UUID> citizenIDs = townhall.getCitizens();

        List<Entity> entityCitizens = Utils.getEntitiesFromUUID(worldObj, citizenIDs);
        if(entityCitizens != null)
        {
            for(Entity entity : entityCitizens)
            {
                if(entity instanceof EntityCitizen)
                {
                    EntityCitizen entityCitizen = (EntityCitizen) entity;
                    if(entityCitizen.getHomeHut() == null)
                    {
                        this.citizens.add(entityCitizen.getUniqueID());
                        entityCitizen.setHomeHut(this);
                        return;
                    }
                }
            }
        }
    }

    public void removeCitizen(EntityCitizen citizen)
    {
        for(UUID id : citizens)
        {
            if(id.equals(citizen.getUniqueID()))
            {
                citizens.remove(id);
                citizen.setHomeHut(null);
                return;
            }
        }
    }

    @Override
    public void breakBlock()
    {
        List<Entity> entityCitizens = Utils.getEntitiesFromUUID(worldObj, citizens);
        if(entityCitizens != null)
        {
            for(Entity entity : entityCitizens)
            {
                if(entity instanceof EntityCitizen)
                {
                    EntityCitizen citizen = (EntityCitizen) entity;
                    citizen.setHomeHut(null);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagList nbtTagCitizenList = new NBTTagList();
        for(UUID citizen : citizens)
        {
            NBTTagCompound nbtTagCitizenCompound = new NBTTagCompound();
            nbtTagCitizenCompound.setString("citizen", citizen.toString());
            nbtTagCitizenList.appendTag(nbtTagCitizenCompound);
        }
        compound.setTag("citizens", nbtTagCitizenList);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList nbtTagCitizenList = compound.getTagList("citizens", Constants.NBT.TAG_COMPOUND);
        this.citizens.clear();
        for(int i = 0; i < nbtTagCitizenList.tagCount(); i++)
        {
            NBTTagCompound nbtTagCitizenCompound = nbtTagCitizenList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(nbtTagCitizenCompound.getString("citizen"));
            citizens.add(uuid);
        }
    }
}
