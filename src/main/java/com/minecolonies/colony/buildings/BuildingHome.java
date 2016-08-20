package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHomeBuilding;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.util.ServerUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingHome extends AbstractBuildingHut
{
    private List<CitizenData> residents = new ArrayList<>();

    private static final String TAG_RESIDENTS = "residents";
    private static final String CITIZEN       = "Citizen";

    public BuildingHome(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return CITIZEN;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 4;
    }

    @Override
    public int getMaxInhabitants()
    {
        return 2;
    }

    @Override
    public void setBuildingLevel(int level)
    {
        super.setBuildingLevel(level);
        getColony().calculateMaxCitizens();
    }

    @Override
    public void onDestroyed()
    {
        residents.stream().filter(citizen -> citizen != null).forEach(citizen -> citizen.setHomeBuilding(null));

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
            // 'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    /**
     * Looks for a homeless citizen to add to the current building. Calls
     * {@link #addResident(CitizenData)}
     */
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

    /**
     * Adds the citizen to the building
     *
     * @param citizen
     *            Citizen to add
     */
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

    /**
     * Returns whether the citizen has this as home or not
     *
     * @param citizen
     *            Citizen to check
     * @return True if citizen lives here, otherwise false
     */
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
                // Bypass addResident (which marks dirty)
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

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        final EntityPlayer owner = ServerUtils.getPlayerFromUUID(getColony().getPermissions().getOwner());

        if (newLevel == 1)
        {
            owner.triggerAchievement(ModAchievements.achievementBuildingColonist);
        }
        else if (newLevel >= this.getMaxBuildingLevel())
        {
            owner.triggerAchievement(ModAchievements.achievementUpgradeColonistMax);
        }
    }

    public static class View extends AbstractBuildingHut.View
    {
        private List<Integer> residents = new ArrayList<>();

        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public List<Integer> getResidents()
        {
            return Collections.unmodifiableList(residents);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHomeBuilding(this);
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
