package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHomeBuilding;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.EnumGUI;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingHome extends BuildingHut
{
    private List<CitizenData> residents = new ArrayList<CitizenData>();

    private static final String TAG_RESIDENTS = "residents";

    public BuildingHome(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Citizen"; }

    @Override
    public int getMaxBuildingLevel(){ return 4; }

    @Override
    public int getMaxInhabitants(){ return 2; }

    @Override
    public void setBuildingLevel(int level)
    {
        super.setBuildingLevel(level);
        getColony().calculateMaxCitizens();
    }

    @Override
    public int getGuiId() { return EnumGUI.CITIZEN.getID(); }

    @Override
    public void onDestroyed()
    {
        for (CitizenData citizen : residents)
        {
            if (citizen != null)
            {
                citizen.setHomeBuilding(null);
            }
        }

        super.onDestroyed();
    }

    @Override
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        if (residents.size() < getMaxInhabitants())
        {
            //  'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    public void addHomelessCitizens()
    {
        for (CitizenData citizen : getColony().getCitizens().values())
        {
            if (citizen.getHomeBuilding() == null)
            {
                addResident(citizen);

                if (residents.size() >= getMaxInhabitants())
                {
                    break;
                }
            }
        }
    }

    private void addResident(CitizenData citizen)
    {
        residents.add(citizen);
        citizen.setHomeBuilding(this);

        markDirty();
    }

    @Override
    public void removeCitizen(CitizenData citizen)
    {
        if (residents.contains(citizen))
        {
            citizen.setHomeBuilding(null);
            residents.remove(citizen);
        }
    }

    public boolean hasResident(CitizenData citizen)
    {
        return residents.contains(citizen);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        residents.clear();

        int[] residentIds = compound.getIntArray(TAG_RESIDENTS);
        for (int citizenId : residentIds)
        {
            CitizenData citizen = getColony().getCitizen(citizenId);
            if (citizen != null)
            {
                //  Bypass addResident (which marks dirty)
                residents.add(citizen);
                citizen.setHomeBuilding(this);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (!residents.isEmpty())
        {
            int[] residentIds = new int[residents.size()];
            for (int i = 0; i < residents.size(); ++i)
            {
                residentIds[i] = residents.get(i).getId();
            }
            compound.setIntArray(TAG_RESIDENTS, residentIds);
        }
    }

    public static class View extends BuildingHut.View
    {
        private List<Integer> residents = new ArrayList<Integer>();

        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public List<Integer> getResidents() { return Collections.unmodifiableList(residents); }

        public com.blockout.views.Window getWindow(int guiId)
        {
            if (guiId == EnumGUI.CITIZEN.getID())
            {
                return new WindowHomeBuilding(this);
            }

            return null;
        }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);

            int numResidents = buf.readInt();
            for (int i = 0; i < numResidents; ++i)
            {
                residents.add(buf.readInt());
            }
        }
    }

    @Override
    public void serializeToView(ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(residents.size());
        for (CitizenData citizen : residents)
        {
            buf.writeInt(citizen.getId());
        }
    }
}
