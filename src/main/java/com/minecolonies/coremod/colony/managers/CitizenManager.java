package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.HappinessData;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracksTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.network.messages.ColonyViewCitizenViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveCitizenMessage;
import com.minecolonies.coremod.network.messages.HappinessDataMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZENS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_MAX_CITIZENS;

public class CitizenManager implements ICitizenManager
{
    /**
     * List of citizens.
     */
    @NotNull
    private final Map<Integer, CitizenData> citizens = new HashMap<>();

    /**
     * Variables to determine if citizens have to be updated on the client side.
     */
    private boolean isCitizensDirty = false;

    /**
     * The highest citizen id.
     */
    private int topCitizenId = 0;

    /**
     * Max citizens without housing.
     */
    private int maxCitizens = Configurations.gameplay.maxCitizens;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Datas about the happiness of a colony
     */
    private final HappinessData happinessData = new HappinessData();

    /**
     * Creates the Citizenmanager for a colony.
     *
     * @param colony the colony.
     */
    public CitizenManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        //  Citizens before Buildings, because Buildings track the Citizens
        citizens.putAll(NBTUtils.streamCompound(compound.getTagList(TAG_CITIZENS, Constants.NBT.TAG_COMPOUND))
                .map(this::deserializeCitizen)
                .collect(Collectors.toMap(CitizenData::getId, Function.identity())));
    }

    private CitizenData deserializeCitizen(@NotNull final NBTTagCompound compound)
    {
        final CitizenData data = CitizenData.createFromNBT(compound, colony);
        topCitizenId = Math.max(topCitizenId, data.getId());
        return data;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        compound.setInteger(TAG_MAX_CITIZENS, maxCitizens);

        @NotNull final NBTTagList citizenTagList = citizens.values().stream().map(citizen -> citizen.writeToNBT(new NBTTagCompound())).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_CITIZENS, citizenTagList);
    }

    @Override
    public void sendPackets(
      @NotNull final Set<EntityPlayerMP> oldSubscribers,
      final boolean hasNewSubscribers,
      @NotNull final Set<EntityPlayerMP> subscribers)
    {
        if (isCitizensDirty || hasNewSubscribers)
        {
            for (@NotNull final CitizenData citizen : citizens.values())
            {
                if (citizen.getCitizenEntity().isPresent())
                {
                    final List<EntityCitizen> list = colony.getWorld()
                            .getEntities(EntityCitizen.class,
                                    entityCitizen -> entityCitizen.getCitizenColonyHandler().getColony().getID() == colony.getID() && entityCitizen.getCitizenData().getId() == citizen.getId());

                    if (!list.isEmpty() && citizen.getCitizenEntity().get().getEntityId() != list.get(0).getEntityId())
                    {
                        citizen.setCitizenEntity(list.get(0));
                    }

                    for (int i = 1; i < list.size(); i++)
                    {
                        Log.getLogger().warn("Removing duplicate entity now!");
                        colony.getWorld().removeEntity(list.get(i));
                    }

                    if (citizen.isDirty() || hasNewSubscribers)
                    {
                        subscribers.stream()
                          .filter(player -> citizen.isDirty() || !oldSubscribers.contains(player))
                          .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewCitizenViewMessage(colony, citizen), player));
                    }
                }
            }

            subscribers.stream()
              .filter(player -> !oldSubscribers.contains(player))
              .forEach(player -> MineColonies.getNetwork().sendTo(new HappinessDataMessage(colony, colony.getHappinessData()), player));
        }
    }

    @Override
    public void spawnCitizen(@Nullable final CitizenData data, @Nullable final World world)
    {
        if (!colony.getBuildingManager().hasTownHall())
        {
            return;
        }

        final BlockPos townHallLocation = colony.getBuildingManager().getTownHall().getLocation();
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
            else
            {
                citizenData.setCitizenEntity(entity);
            }

            entity.getCitizenColonyHandler().setColony(colony, citizenData);

            entity.setPosition(spawnPoint.getX() + HALF_BLOCK, spawnPoint.getY() + SLIGHTLY_UP, spawnPoint.getZ() + HALF_BLOCK);
            world.spawnEntity(entity);

            colony.getStatsManager().checkAchievements();
            markCitizensDirty();
        }
    }

    @Override
    public void removeCitizen(@NotNull final CitizenData citizen)
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
        for (final EntityPlayerMP player : colony.getPackageManager().getSubscribers())
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveCitizenMessage(colony, citizen.getId()), player);
        }

        colony.markDirty();
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
    public void calculateMaxCitizens()
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
            setMaxCitizens(Math.min(newMaxCitizens,Configurations.gameplay.maxCitizenPerColony));
        }
        colony.markDirty();
    }

    /**
     * Spawn a brand new Citizen.
     */
    public void spawnCitizen()
    {
        spawnCitizen(null, colony.getWorld());
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
        colony.markDirty();
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

    @Override
    public void checkCitizensForHappiness()
    {
        int guards = 1;
        int housing = 0;
        int workers = 1;
        double saturation = 0;
        for (final CitizenData citizen : getCitizens())
        {
            final AbstractBuildingWorker buildingWorker = citizen.getWorkBuilding();
            if (buildingWorker != null)
            {
                if (buildingWorker instanceof AbstractBuildingGuards)
                {
                    guards += buildingWorker.getBuildingLevel();
                }
                else
                {
                    workers += buildingWorker.getBuildingLevel();
                }
            }

            final AbstractBuilding home = citizen.getHomeBuilding();
            if (home != null)
            {
                housing += home.getBuildingLevel();
            }

            saturation += citizen.getSaturation();
        }

        final int averageHousing = housing / Math.max(1, getCitizens().size());

        if (averageHousing > 1)
        {
            colony.increaseOverallHappiness(averageHousing * HAPPINESS_FACTOR);
            colony.getHappinessData().setHousing(HappinessData.INCREASE);
        }
        else if (averageHousing < 1)
        {
            colony.getHappinessData().setHousing(HappinessData.DECREASE);
        }
        else
        {
            colony.getHappinessData().setHousing(HappinessData.STABLE);
        }

        final int averageSaturation = (int) (saturation / getCitizens().size());
        if (averageSaturation < WELL_SATURATED_LIMIT)
        {
            colony.decreaseOverallHappiness((averageSaturation - WELL_SATURATED_LIMIT) * -HAPPINESS_FACTOR);
            colony.getHappinessData().setSaturation(HappinessData.DECREASE);
        }
        else if (averageSaturation > WELL_SATURATED_LIMIT)
        {
            colony.increaseOverallHappiness((averageSaturation - WELL_SATURATED_LIMIT) * HAPPINESS_FACTOR);
            colony.getHappinessData().setSaturation(HappinessData.INCREASE);
        }
        else
        {
            colony.getHappinessData().setSaturation(HappinessData.STABLE);
        }

        final int relation = workers / guards;

        if (relation > 1)
        {
            colony.decreaseOverallHappiness(relation * HAPPINESS_FACTOR);
            colony.getHappinessData().setGuards(HappinessData.DECREASE);
        }
        else if (relation < 1)
        {
            colony.getHappinessData().setGuards(HappinessData.INCREASE);
        }
        else
        {
            colony.getHappinessData().setGuards(HappinessData.STABLE);
        }
    }

    @Override
    public void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        //  Cleanup disappeared citizens
        //  It would be really nice if we didn't have to do this... but Citizens can disappear without dying!
        //  Every CLEANUP_TICK_INCREMENT, cleanup any 'lost' citizens
        if (Colony.shallUpdate(event.world, CLEANUP_TICK_INCREMENT) && colony.areAllColonyChunksLoaded(event) && colony.hasTownHall())
        {
            //  All chunks within a good range of the colony should be loaded, so all citizens should be loaded
            //  If we don't have any references to them, destroy the citizen
            getCitizens().stream().filter(Objects::nonNull).forEach(CitizenData::updateCitizenEntityIfNecessary);
        }

        //  Spawn Citizens
        if (colony.hasTownHall() && getCitizens().size() < getMaxCitizens())
        {
            int respawnInterval = Configurations.gameplay.citizenRespawnInterval * TICKS_SECOND;
            respawnInterval -= (SECONDS_A_MINUTE * colony.getBuildingManager().getTownHall().getBuildingLevel());

            if ((event.world.getTotalWorldTime() + 1) % (respawnInterval + 1) == 0)
            {
                spawnCitizen();
            }
        }
    }
}
