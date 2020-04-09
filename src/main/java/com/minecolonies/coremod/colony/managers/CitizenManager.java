package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.HappinessData;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.managers.interfaces.ICitizenManager;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.ColonyViewCitizenViewMessage;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveCitizenMessage;
import com.minecolonies.coremod.network.messages.HappinessDataMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.HAPPINESS_FACTOR;
import static com.minecolonies.api.util.constant.ColonyConstants.WELL_SATURATED_LIMIT;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZENS;

public class CitizenManager implements ICitizenManager
{
    /**
     * Map of citizens with ID,CitizenData
     */
    @NotNull
    private final Map<Integer, ICitizenData> citizens = new HashMap<>();

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
    private int maxCitizens = 0;

    /**
     * Max citizens considering the spot in the empty guard tower.
     */
    private int potentialMaxCitizens;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * The initial citizen spawn interval
     */
    private int respawnInterval = 30 * TICKS_SECOND;

    /**
     * Random obj.
     */
    private Random random = new Random();

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
    public void registerCitizen(final AbstractEntityCitizen citizen)
    {
        if (citizen.getCitizenId() == 0 || citizens.get(citizen.getCitizenId()) == null)
        {
            citizen.remove();
            return;
        }

        final ICitizenData data = citizens.get(citizen.getCitizenId());
        final Optional<AbstractEntityCitizen> existingCitizen = data.getCitizenEntity();

        if (!existingCitizen.isPresent())
        {
            data.setCitizenEntity(citizen);
            citizen.setCitizenData(data);
            return;
        }

        if (existingCitizen.get() == citizen)
        {
            return;
        }

        if (!existingCitizen.get().isAlive() || !existingCitizen.get().world.isBlockLoaded(existingCitizen.get().getPosition()))
        {
            existingCitizen.get().remove();
            data.setCitizenEntity(citizen);
            citizen.setCitizenData(data);
            return;
        }

        citizen.remove();
    }

    @Override
    public void unregisterCitizen(final AbstractEntityCitizen citizen)
    {
        final ICitizenData data = citizens.get(citizen.getCitizenId());
        if (data != null && data.getCitizenEntity().isPresent() && data.getCitizenEntity().get() == citizen)
        {
            citizens.get(citizen.getCitizenId()).setCitizenEntity(null);
        }
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        citizens.clear();
        //  Citizens before Buildings, because Buildings track the Citizens
        citizens.putAll(NBTUtils.streamCompound(compound.getList(TAG_CITIZENS, Constants.NBT.TAG_COMPOUND))
                          .map(this::deserializeCitizen)
                          .collect(Collectors.toMap(ICitizenData::getId, Function.identity())));

        // Update child state after loading citizen data
        colony.updateHasChilds();
    }

    private ICitizenData deserializeCitizen(@NotNull final CompoundNBT compound)
    {
        final ICitizenData data = ICitizenDataManager.getInstance().createFromNBT(compound, colony);
        topCitizenId = Math.max(topCitizenId, data.getId());
        return data;
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        @NotNull final ListNBT citizenTagList = citizens.values().stream().map(citizen -> citizen.serializeNBT()).collect(NBTUtils.toListNBT());
        compound.put(TAG_CITIZENS, citizenTagList);
    }

    @Override
    public void sendPackets(
      @NotNull final Set<ServerPlayerEntity> closeSubscribers,
      @NotNull final Set<ServerPlayerEntity> newSubscribers)
    {
        if (isCitizensDirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayerEntity> players = isCitizensDirty ? closeSubscribers : newSubscribers;
            for (@NotNull final ICitizenData citizen : citizens.values())
            {
                if (citizen.getCitizenEntity().isPresent())
                {
                    if (citizen.isDirty() || !newSubscribers.isEmpty())
                    {
                        players.forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewCitizenViewMessage(colony, citizen), player));
                    }
                }
            }

            players.forEach(player -> Network.getNetwork().sendToPlayer(new HappinessDataMessage(colony, colony.getHappinessData()), player));
        }
    }

    @Override
    public ICitizenData spawnOrCreateCitizen(@Nullable final ICitizenData data, final World world, final BlockPos spawnPos, final boolean force)
    {
        if (!colony.getBuildingManager().hasTownHall() || (!colony.canMoveIn() && !force))
        {
            return data;
        }

        BlockPos spawnLocation = spawnPos;
        if (spawnLocation == null || spawnLocation.equals(BlockPos.ZERO))
        {
            spawnLocation = colony.getBuildingManager().getTownHall().getPosition();
        }


        BlockPos calculatedSpawn = null;

        if (world.getChunkProvider().isChunkLoaded(new ChunkPos(spawnLocation.getX() >> 4, spawnLocation.getZ() >> 4)))
        {
            calculatedSpawn = EntityUtils.getSpawnPoint(world, spawnLocation);
        }

        if (calculatedSpawn == null)
        {
            spawnLocation = colony.getBuildingManager().getTownHall().getPosition();
            if (world.getChunkProvider().isChunkLoaded(new ChunkPos(spawnLocation.getX() >> 4, spawnLocation.getZ() >> 4)))
            {
                calculatedSpawn = EntityUtils.getSpawnPoint(world, spawnLocation);
            }
        }

        if (calculatedSpawn != null)
        {
            return spawnCitizenOnPosition(data, world, force, calculatedSpawn);
        }
        else
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
              "com.minecolonies.coremod.citizens.nospace",
              spawnLocation.getX(),
              spawnLocation.getY(),
              spawnLocation.getZ());
        }
        return data;
    }

    @NotNull
    private ICitizenData spawnCitizenOnPosition(
      @Nullable final ICitizenData data,
      @NotNull final World world,
      final boolean force,
      final BlockPos spawnPoint)
    {
        ICitizenData citizenData = data;
        if (citizenData == null)
        {
            citizenData = createAndRegisterNewCitizenData();

            if (getMaxCitizens() == getCitizens().size() && !force)
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessagePlayerEntities(),
                  "block.blockHutTownHall.messageMaxSize",
                  colony.getName());
            }
        }
        final EntityCitizen entity = (EntityCitizen) ModEntities.CITIZEN.create(world);
        entity.getCitizenColonyHandler().registerWithColony(citizenData.getColony().getID(), citizenData.getId());

        entity.setPosition(spawnPoint.getX() + HALF_BLOCK, spawnPoint.getY() + SLIGHTLY_UP, spawnPoint.getZ() + HALF_BLOCK);
        world.addEntity(entity);

        colony.getProgressManager()
          .progressCitizenSpawn(citizens.size(), citizens.values().stream().filter(tempDate -> tempDate.getJob() != null).collect(Collectors.toList()).size());
        markCitizensDirty();
        return citizenData;
    }

    @Override
    public CitizenData createAndRegisterNewCitizenData()
    {
        //This ensures that citizen IDs are getting reused.
        //That's needed to prevent bugs when calling IDs that are not used.
        for (int i = 1; i <= this.getCurrentCitizenCount() + 1; i++)
        {
            if (this.getCitizen(i) == null)
            {
                topCitizenId = i;
                break;
            }
        }

        final CitizenData citizenData = new CitizenData(topCitizenId, colony);
        citizenData.initForNewCitizen();
        citizens.put(citizenData.getId(), citizenData);

        return citizenData;
    }

    @Override
    public void removeCitizen(@NotNull final ICitizenData citizen)
    {
        //Remove the Citizen
        citizens.remove(citizen.getId());

        for (@NotNull final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            building.removeCitizen(citizen);
        }

        colony.getWorkManager().clearWorkForCitizen(citizen);

        //  Inform Subscribers of removed citizen
        for (final ServerPlayerEntity player : colony.getPackageManager().getCloseSubscribers())
        {
            Network.getNetwork().sendToPlayer(new ColonyViewRemoveCitizenMessage(colony, citizen.getId()), player);
        }

        calculateMaxCitizens();
        colony.markDirty();
    }

    @Override
    public ICitizenData getJoblessCitizen()
    {
        for (@NotNull final ICitizenData citizen : citizens.values())
        {
            if (citizen.getWorkBuilding() == null && !citizen.isChild())
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
        int potentialMax = 0;

        for (final IBuilding b : colony.getBuildingManager().getBuildings().values())
        {
            if (b.getBuildingLevel() > 0)
            {
                if (b instanceof BuildingHome)
                {
                    newMaxCitizens += b.getMaxInhabitants();
                }
                else if (b instanceof AbstractBuildingGuards && b.getBuildingLevel() > 0)
                {
                    if (b.getAssignedCitizen().size() != 0)
                    {
                        newMaxCitizens += b.getAssignedCitizen().size();
                    }
                    else
                    {
                        potentialMax += 1;
                    }
                }
            }
        }
        if (getMaxCitizens() != newMaxCitizens)
        {
            setMaxCitizens(newMaxCitizens);
            setPotentialMaxCitizens(potentialMax + newMaxCitizens);
            colony.markDirty();
        }
    }

    /**
     * Spawn a brand new Citizen.
     */
    public void spawnOrCreateCitizen()
    {
        spawnOrCreateCitizen(null, colony.getWorld(), null);
    }

    @NotNull
    @Override
    public Map<Integer, ICitizenData> getCitizenMap()
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
    public ICitizenData getCitizen(final int citizenId)
    {
        return citizens.get(citizenId);
    }

    @Override
    public void clearDirty()
    {
        isCitizensDirty = false;
        citizens.values().forEach(ICitizenData::clearDirty);
    }

    @Override
    public List<ICitizenData> getCitizens()
    {
        return new ArrayList<>(citizens.values());
    }

    @Override
    public int getMaxCitizens()
    {
        return Math.min(maxCitizens, MineColonies.getConfig().getCommon().maxCitizenPerColony.get());
    }

    @Override
    public int getPotentialMaxCitizens()
    {
        return Math.min(potentialMaxCitizens, MinecoloniesAPIProxy.getInstance().getConfig().getCommon().maxCitizenPerColony.get());
    }

    /**
     * Get the current amount of citizens, might be bigger then {@link #getMaxCitizens()}
     *
     * @return The current amount of citizens in the colony.
     */
    @Override
    public int getCurrentCitizenCount()
    {
        return citizens.size();
    }

    @Override
    public void setMaxCitizens(final int newMaxCitizens)
    {
        this.maxCitizens = newMaxCitizens;
    }

    @Override
    public void setPotentialMaxCitizens(final int newPotentialMax)
    {
        this.potentialMaxCitizens = newPotentialMax;
    }

    // TODO: why isnt this in the happiness manager?
    @Override
    public void checkCitizensForHappiness()
    {
        int guards = 1;
        int housing = 0;
        int workers = 1;
        boolean hasJob = false;
        boolean hasHouse = false;
        double saturation = 0;
        for (final ICitizenData citizen : getCitizens())
        {
            hasJob = false;
            hasHouse = false;
            final IBuildingWorker buildingWorker = citizen.getWorkBuilding();
            if (buildingWorker != null)
            {
                hasJob = true;
                if (buildingWorker instanceof AbstractBuildingGuards)
                {
                    guards += buildingWorker.getBuildingLevel();
                }
                else
                {
                    workers += buildingWorker.getBuildingLevel();
                }
            }

            final IBuilding home = citizen.getHomeBuilding();
            if (home != null)
            {
                hasHouse = true;
                housing += home.getBuildingLevel();
            }

            if (citizen.getCitizenEntity().isPresent())
            {
                citizen.getCitizenHappinessHandler().processDailyHappiness(hasHouse, hasJob);
            }
            saturation += citizen.getSaturation();
        }

        final int averageHousing = housing / Math.max(1, getCitizens().size());

        if (averageHousing > 1)
        {
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
            colony.getHappinessData().setSaturation(HappinessData.DECREASE);
        }
        else if (averageSaturation > WELL_SATURATED_LIMIT)
        {
            colony.getHappinessData().setSaturation(HappinessData.INCREASE);
        }
        else
        {
            colony.getHappinessData().setSaturation(HappinessData.STABLE);
        }

        final int relation = workers / guards;

        if (relation > 1)
        {
            colony.getHappinessData().setHousingModifier(relation * HAPPINESS_FACTOR);
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
    public void tickCitizenData()
    {
        this.getCitizens().forEach(ICitizenData::tick);
    }

    /**
     * Updates the citizen entities when needed and spawn the initial citizens on colony tick.
     *
     * @param colony the colony being ticked.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        if (colony.hasTownHall())
        {
            getCitizens().stream().filter(Objects::nonNull).forEach(ICitizenData::updateCitizenEntityIfNecessary);
        }

        //  Spawn initial Citizens
        if (colony.canMoveIn() && colony.hasTownHall() && getCitizens().size() < MineColonies.getConfig().getCommon().initialCitizenAmount.get())
        {
            respawnInterval -= 500 + (SECONDS_A_MINUTE * colony.getBuildingManager().getTownHall().getBuildingLevel());

            if (respawnInterval <= 0)
            {
                respawnInterval = MineColonies.getConfig().getCommon().citizenRespawnInterval.get() * TICKS_SECOND;
                // Make sure the initial citizen contain both genders
                final CitizenData newCitizen = createAndRegisterNewCitizenData();

                // 50 - 50 Female male ratio for initial citizens
                if (citizens.size() % 2 == 0)
                {
                    newCitizen.setIsFemale(true);
                }
                else
                {
                    newCitizen.setIsFemale(false);
                }

                spawnOrCreateCitizen(newCitizen, colony.getWorld(), null, true);
            }
        }
    }

    @Override
    public void updateCitizenMourn(final boolean mourn)
    {
        for (final ICitizenData citizen : getCitizens())
        {
            if (citizen.getCitizenEntity().isPresent() && !(citizen.getJob() instanceof AbstractJobGuard))
            {
                citizen.getCitizenEntity().get().setMourning(mourn);
            }
        }
    }

    @Override
    public ICitizenData getRandomCitizen()
    {
        return (ICitizenData) citizens.values().toArray()[random.nextInt(citizens.values().size())];
    }
}
