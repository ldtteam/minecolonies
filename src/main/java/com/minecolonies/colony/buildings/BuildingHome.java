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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingHome extends AbstractBuildingHut
{
    private static final String            TAG_RESIDENTS = "residents";
    private static final String            CITIZEN       = "Citizen";
    @NotNull
    private              List<CitizenData> residents     = new ArrayList<>();

    public BuildingHome(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound)
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

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CITIZEN;
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (!residents.isEmpty())
        {
            @NotNull int[] residentIds = new int[residents.size()];
            for (int i = 0; i < residents.size(); ++i)
            {
                residentIds[i] = residents.get(i).getId();
            }
            compound.setIntArray(TAG_RESIDENTS, residentIds);
        }
    }

    @Override
    public void onDestroyed()
    {
        residents.stream().filter(citizen -> citizen != null).forEach(citizen -> citizen.setHomeBuilding(null));

        super.onDestroyed();
    }

    @Override
    public void removeCitizen(@NotNull CitizenData citizen)
    {
        if (residents.contains(citizen))
        {
            citizen.setHomeBuilding(null);
            residents.remove(citizen);
        }
    }

    @Override
    public void onWorldTick(@NotNull TickEvent.WorldTickEvent event)
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

    @Override
    public int getMaxInhabitants()
    {
        return 2;
    }

    /**
     * Looks for a homeless citizen to add to the current building. Calls
     * {@link #addResident(CitizenData)}
     */
    public void addHomelessCitizens()
    {
        for (@NotNull CitizenData citizen : getColony().getCitizens().values())
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
     * @param citizen Citizen to add
     */
    private void addResident(@NotNull CitizenData citizen)
    {
        residents.add(citizen);
        citizen.setHomeBuilding(this);

        markDirty();
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        @Nullable final EntityPlayer owner = ServerUtils.getPlayerFromUUID(getColony().getPermissions().getOwner(), getColony().getWorld());

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingColonist);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeColonistMax);
        }
    }

    @Override
    public void serializeToView(@NotNull ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(residents.size());
        for (@NotNull CitizenData citizen : residents)
        {
            buf.writeInt(citizen.getId());
        }
    }

    @Override
    public void setBuildingLevel(int level)
    {
        super.setBuildingLevel(level);
        getColony().calculateMaxCitizens();
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 5;
    }

    /**
     * Returns whether the citizen has this as home or not
     *
     * @param citizen Citizen to check
     * @return True if citizen lives here, otherwise false
     */
    public boolean hasResident(CitizenData citizen)
    {
        return residents.contains(citizen);
    }

    public static class View extends AbstractBuildingHut.View
    {
        @NotNull
        private List<Integer> residents = new ArrayList<>();

        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public List<Integer> getResidents()
        {
            return Collections.unmodifiableList(residents);
        }

        @NotNull
        public com.blockout.views.Window getWindow()
        {
            return new WindowHomeBuilding(this);
        }

        @Override
        public void deserialize(@NotNull ByteBuf buf)
        {
            super.deserialize(buf);

            int numResidents = buf.readInt();
            for (int i = 0; i < numResidents; ++i)
            {
                residents.add(buf.readInt());
            }
        }
    }
}
