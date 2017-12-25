package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingBarracksTower;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.network.messages.ColonyViewCitizenViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveCitizenMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.HALF_BLOCK;
import static com.minecolonies.api.util.constant.Constants.SLIGHTLY_UP;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZENS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_MAX_CITIZENS;

public class CitizenManager implements ICitizenManager
{
    private boolean isCitizensDirty  = false;

    /**
     * The highest citizen id.
     */
    private int topCitizenId = 0;
    /**
     * Max citizens without housing.
     */
    private int maxCitizens = Configurations.gameplay.maxCitizens;

    /**
     * List of citizens.
     */
    @NotNull
    private final Map<Integer, CitizenData> citizens = new HashMap<>();

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound, @NotNull final Colony colony)
    {
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        //  Citizens before Buildings, because Buildings track the Citizens
        final NBTTagList citizenTagList = compound.getTagList(TAG_CITIZENS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < citizenTagList.tagCount(); ++i)
        {
            final NBTTagCompound citizenCompound = citizenTagList.getCompoundTagAt(i);
            final CitizenData data = CitizenData.createFromNBT(citizenCompound, colony);
            citizens.put(data.getId(), data);
            topCitizenId = Math.max(topCitizenId, data.getId());
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        compound.setInteger(TAG_MAX_CITIZENS, maxCitizens);

        //  Citizens
        @NotNull final NBTTagList citizenTagList = new NBTTagList();
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            @NotNull final NBTTagCompound citizenCompound = new NBTTagCompound();
            citizen.writeToNBT(citizenCompound);
            citizenTagList.appendTag(citizenCompound);
        }
        compound.setTag(TAG_CITIZENS, citizenTagList);
    }

    @Override
    public void sendPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers,
            final boolean hasNewSubscribers,
            @NotNull final Set<EntityPlayerMP> subscribers,
            @NotNull final Colony colony)
    {
        if (isCitizensDirty || hasNewSubscribers)
        {
            for (@NotNull final CitizenData citizen : citizens.values())
            {
                if (citizen.isDirty() || hasNewSubscribers)
                {
                    subscribers.stream()
                            .filter(player -> citizen.isDirty() || !oldSubscribers.contains(player))
                            .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewCitizenViewMessage(colony, citizen), player));
                }
            }
        }
    }

    @Override
    public void spawnCitizenIfNull(@NotNull final CitizenData data, @NotNull final World world, @NotNull final IBuildingManager buildingManager,
            @NotNull final Colony colony)
    {
        if (data.getCitizenEntity() == null)
        {
            Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), data.getId()));
            spawnCitizen(data, world, buildingManager, colony);
        }
    }

    @Override
    public void spawnCitizen(@NotNull final CitizenData data, @NotNull final World world, @NotNull final IBuildingManager buildingManager, @NotNull final Colony colony)
    {
        final BlockPos townHallLocation = buildingManager.getTownHall().getLocation();
        if (!world.isBlockLoaded(townHallLocation))
        {
            //  Chunk with TownHall Block is not loaded
            return;
        }

        final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, townHallLocation);

        if (spawnPoint != null)
        {
            final EntityCitizen entity = new EntityCitizen(world);

            CitizenData citizenData = data;
            if (citizenData == null)
            {
                //This ensures that citizen IDs are getting reused.
                //That's needed to prevent bugs when calling IDs that are not used.
                for (int i = 1; i <= this.getMaxCitizens(); i++)
                {
                    if (this.getCitizen(i) == null)
                    {
                        topCitizenId = i;
                        break;
                    }
                }

                citizenData = new CitizenData(topCitizenId, colony);
                citizenData.initializeFromEntity(entity);

                citizens.put(citizenData.getId(), citizenData);

                if (getMaxCitizens() == getCitizens().size())
                {
                    LanguageHandler.sendPlayersMessage(
                            colony.getMessageEntityPlayers(),
                            "tile.blockHutTownHall.messageMaxSize",
                            colony.getName());
                }
            }
            entity.setColony(colony, citizenData);

            entity.setPosition(spawnPoint.getX() + HALF_BLOCK, spawnPoint.getY() + SLIGHTLY_UP, spawnPoint.getZ() + HALF_BLOCK);
            world.spawnEntity(entity);

            colony.checkAchievements();
            markCitizensDirty();
        }
    }

    @Override
    public void removeCitizen(@NotNull final CitizenData citizen, @NotNull final Colony colony)
    {
        //Remove the Citizen
        citizens.remove(citizen.getId());

        if (citizen.getWorkBuilding() != null)
        {
            citizen.getWorkBuilding().cancelAllRequestsOfCitizen(citizen);
        }

        if (citizen.getHomeBuilding() != null)
        {
            citizen.getHomeBuilding().cancelAllRequestsOfCitizen(citizen);
        }

        for (@NotNull final AbstractBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            building.removeCitizen(citizen);
        }

        colony.getWorkManager().clearWorkForCitizen(citizen);

        //  Inform Subscribers of removed citizen
        for (final EntityPlayerMP player : colony.getSubscribers())
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveCitizenMessage(colony, citizen.getId()), player);
        }
    }

    @Override
    public CitizenData getJoblessCitizen()
    {
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            if (citizen.getWorkBuilding() == null)
            {
                return citizen;
            }
        }

        return null;
    }

    @Override
    public void calculateMaxCitizens(@NotNull final Colony colony)
    {
        int newMaxCitizens = 0;

        for (final AbstractBuilding b : colony.getBuildingManager().getBuildings().values())
        {
            if (b.getBuildingLevel() > 0)
            {
                if (b instanceof BuildingHome)
                {
                    newMaxCitizens += ((BuildingHome) b).getMaxInhabitants();
                }
                else if (b instanceof BuildingBarracksTower)
                {
                    newMaxCitizens += b.getBuildingLevel();
                }
            }
        }
        // Have at least the minimum amount of citizens
        newMaxCitizens = Math.max(Configurations.gameplay.maxCitizens, newMaxCitizens);
        if (getMaxCitizens() != newMaxCitizens)
        {
            setMaxCitizens(newMaxCitizens);
            colony.markDirty();
        }
    }

    /**
     * Spawn a brand new Citizen.
     */
    public void spawnCitizen(@NotNull final Colony colony)
    {
        spawnCitizen(null, colony.getWorld(), colony.getBuildingManager(), colony);
    }

    @NotNull
    @Override
    public Map<Integer, CitizenData> getCitizenMap()
    {
        return Collections.unmodifiableMap(citizens);
    }

    @Override
    public void markCitizensDirty()
    {
        isCitizensDirty = true;
    }

    @Override
    public CitizenData getCitizen(final int citizenId)
    {
        return citizens.get(citizenId);
    }

    @Override
    public void clearDirty()
    {
        isCitizensDirty = false;
        citizens.values().forEach(CitizenData::clearDirty);
    }

    @Override
    public List<CitizenData> getCitizens()
    {
        return new ArrayList<>(citizens.values());
    }

    @Override
    public int getMaxCitizens()
    {
        return maxCitizens;
    }

    @Override
    public void setMaxCitizens(final int newMaxCitizens)
    {
        this.maxCitizens = newMaxCitizens;
    }
}
