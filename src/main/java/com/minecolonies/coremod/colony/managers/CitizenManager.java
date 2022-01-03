package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.ICitizenManager;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.modules.AbstractAssignedCitizenModule;
import com.minecolonies.coremod.colony.buildings.modules.BedHandlingModule;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.WorkAtHomeBuildingModule;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenSpawnedEvent;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobUndertaker;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewCitizenViewMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewRemoveCitizenMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.CITIZEN_CAP;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZENS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.ALL_CITIZENS_ARE_SLEEPING;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_MOURN;

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
     * Whether all citizens excluding guards are sleeping
     */
    private boolean areCitizensSleeping;

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
    public void registerCivilian(final AbstractCivilianEntity entity)
    {
        if (entity.getCivilianID() == 0 || citizens.get(entity.getCivilianID()) == null)
        {
            entity.remove();
            return;
        }

        final ICitizenData data = citizens.get(entity.getCivilianID());
        final Optional<AbstractEntityCitizen> existingCitizen = data.getEntity();

        if (!existingCitizen.isPresent())
        {
            data.setEntity(entity);
            entity.level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), colony.getTeam());
            return;
        }

        if (existingCitizen.get() == entity)
        {
            entity.level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), colony.getTeam());
            return;
        }

        if (entity.isAlive())
        {
            existingCitizen.get().remove();
            data.setEntity(entity);
            entity.setCivilianData(data);
            entity.level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), colony.getTeam());
            return;
        }

        entity.remove();
    }

    @Override
    public void unregisterCivilian(final AbstractCivilianEntity entity)
    {
        final ICitizenData data = citizens.get(entity.getCivilianID());
        if (data != null && data.getEntity().isPresent() && data.getEntity().get() == entity)
        {
            try
            {
                if (colony.getWorld().getScoreboard().getPlayersTeam(entity.getScoreboardName()) == colony.getTeam())
                {
                    colony.getWorld().getScoreboard().removePlayerFromTeam(entity.getScoreboardName(), colony.getTeam());
                }
            }
            catch (Exception ignored)
            {
                // For some weird reason we can get an exception here, though the exception is thrown for team != colony team which we check == on before
            }

            citizens.get(entity.getCivilianID()).setEntity(null);
        }
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        citizens.forEach((id, citizen) -> citizen.getEntity().ifPresent(Entity::remove));
        citizens.clear();
        //  Citizens before Buildings, because Buildings track the Citizens
        citizens.putAll(NBTUtils.streamCompound(compound.getList(TAG_CITIZENS, Constants.NBT.TAG_COMPOUND))
                          .map(this::deserializeCitizen)
                          .collect(Collectors.toMap(ICitizenData::getId, Function.identity())));

        // Update child state after loading citizen data
        colony.updateHasChilds();
    }

    /**
     * Creates a citizen data from NBT
     *
     * @param compound NBT
     * @return citizen data
     */
    private ICitizenData deserializeCitizen(@NotNull final CompoundNBT compound)
    {
        final ICitizenData data = ICitizenDataManager.getInstance().createFromNBT(compound, colony);
        topCitizenId = Math.max(topCitizenId, data.getId());
        return data;
    }

    @Override
    public void write(@NotNull final CompoundNBT compoundNBT)
    {
        @NotNull final ListNBT citizenTagList = citizens.values().stream().map(citizen -> citizen.serializeNBT()).collect(NBTUtils.toListNBT());
        compoundNBT.put(TAG_CITIZENS, citizenTagList);
    }

    @Override
    public void sendPackets(
      @NotNull final Set<ServerPlayerEntity> closeSubscribers,
      @NotNull final Set<ServerPlayerEntity> newSubscribers)
    {
        if (isCitizensDirty || !newSubscribers.isEmpty())
        {
            final Set<ServerPlayerEntity> players = new HashSet<>();
            if (isCitizensDirty)
            {
                players.addAll(closeSubscribers);
            }
            players.addAll(newSubscribers);
            for (@NotNull final ICitizenData citizen : citizens.values())
            {
                if (citizen.isDirty() || !newSubscribers.isEmpty())
                {
                    players.forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewCitizenViewMessage(colony, citizen), player));
                }
            }
        }
    }

    @Override
    public ICitizenData spawnOrCreateCivilian(@Nullable final ICivilianData data, final World world, final BlockPos spawnPos, final boolean force)
    {
        if (!colony.getBuildingManager().hasTownHall() || (!colony.canMoveIn() && !force))
        {
            return (ICitizenData) data;
        }

        BlockPos spawnLocation = spawnPos;
        if (colony.hasTownHall() && (spawnLocation == null || spawnLocation.equals(BlockPos.ZERO)))
        {
            spawnLocation = colony.getBuildingManager().getTownHall().getPosition();
        }

        if (WorldUtil.isEntityBlockLoaded(world, spawnLocation))
        {
            BlockPos calculatedSpawn = EntityUtils.getSpawnPoint(world, spawnLocation);
            if (calculatedSpawn != null)
            {
                return spawnCitizenOnPosition((ICitizenData) data, world, force, calculatedSpawn);
            }
            else
            {
                if (colony.hasTownHall())
                {
                    calculatedSpawn = EntityUtils.getSpawnPoint(world, colony.getBuildingManager().getTownHall().getID());
                    if (calculatedSpawn != null)
                    {
                        return spawnCitizenOnPosition((ICitizenData) data, world, force, calculatedSpawn);
                    }
                }

                LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(),
                  "com.minecolonies.coremod.citizens.nospace",
                  spawnLocation.getX(),
                  spawnLocation.getY(),
                  spawnLocation.getZ());
            }
        }

        return (ICitizenData) data;
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
            citizenData = createAndRegisterCivilianData();

            if (getMaxCitizens() >= getCurrentCitizenCount() && !force)
            {
                if (maxCitizensFromResearch() <= getCurrentCitizenCount())
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getMessagePlayerEntities(),
                      "block.blockhuttownhall.messagemaxsize.research",
                      colony.getName());
                }
                else
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getMessagePlayerEntities(),
                      "block.blockhuttownhall.messagemaxsize.config",
                      colony.getName());
                }
            }

            colony.getEventDescriptionManager().addEventDescription(new CitizenSpawnedEvent(spawnPoint, citizenData.getName()));
        }
        final EntityCitizen entity = (EntityCitizen) ModEntities.CITIZEN.create(world);

        entity.setPos(spawnPoint.getX() + HALF_BLOCK, spawnPoint.getY() + SLIGHTLY_UP, spawnPoint.getZ() + HALF_BLOCK);
        world.addFreshEntity(entity);

        entity.getCitizenColonyHandler().registerWithColony(citizenData.getColony().getID(), citizenData.getId());

        colony.getProgressManager()
          .progressCitizenSpawn(citizens.size(), citizens.values().stream().filter(tempDate -> tempDate.getJob() != null).collect(Collectors.toList()).size());
        markDirty();
        return citizenData;
    }

    @Override
    public ICitizenData createAndRegisterCivilianData()
    {
        //This ensures that citizen IDs are getting reused.
        //That's needed to prevent bugs when calling IDs that are not used.
        for (int i = 1; i <= this.getCurrentCitizenCount() + 1; i++)
        {
            if (this.getCivilian(i) == null)
            {
                topCitizenId = i;
                break;
            }
        }

        final CitizenData citizenData = new CitizenData(topCitizenId, colony);
        citizenData.initForNewCivilian();
        citizens.put(citizenData.getId(), citizenData);

        return citizenData;
    }

    @Override
    public ICitizenData resurrectCivilianData(@NotNull final CompoundNBT compoundNBT, final boolean resetId, @NotNull final World world, final BlockPos spawnPos)
    {
        //This ensures that citizen IDs are getting reused.
        //That's needed to prevent bugs when calling IDs that are not used.
        for (int i = 1; i <= this.getCurrentCitizenCount() + 1; i++)
        {
            if (this.getCivilian(i) == null)
            {
                topCitizenId = i;
                break;
            }
        }

        if(resetId)
        {
            compoundNBT.putInt(TAG_ID, topCitizenId);
        }

        final ICitizenData citizenData = deserializeCitizen(compoundNBT);
        citizenData.onResurrect();
        citizens.put(citizenData.getId(), citizenData);
        spawnOrCreateCitizen(citizenData, world, spawnPos);
        return citizenData;
    }

    @Override
    public void removeCivilian(@NotNull final ICivilianData citizen)
    {
        if (!(citizen instanceof ICitizenData))
        {
            return;
        }

        //Remove the Citizen
        citizens.remove(citizen.getId());

        for (@NotNull final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            for (final AbstractAssignedCitizenModule assignedCitizenModule : building.getModules(AbstractAssignedCitizenModule.class))
            {
                assignedCitizenModule.removeCitizen((ICitizenData) citizen);
            }
        }

        colony.getWorkManager().clearWorkForCitizen((ICitizenData) citizen);

        //  Inform Subscribers of removed citizen
        for (final ServerPlayerEntity player : colony.getPackageManager().getCloseSubscribers())
        {
            Network.getNetwork().sendToPlayer(new ColonyViewRemoveCitizenMessage(colony, citizen.getId()), player);
        }

        calculateMaxCitizens();
        markDirty();
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
                if (b.hasModule(BedHandlingModule.class) && b.hasModule(WorkAtHomeBuildingModule.class))
                {
                    final WorkAtHomeBuildingModule module = b.getFirstModuleOccurance(WorkAtHomeBuildingModule.class);
                    newMaxCitizens += b.getAllAssignedCitizen().size();
                    potentialMax += module.getModuleMax() - b.getAllAssignedCitizen().size();
                }
                else if (b.hasModule(LivingBuildingModule.class))
                {
                    newMaxCitizens += b.getFirstModuleOccurance(LivingBuildingModule.class).getModuleMax();
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
    public Map<Integer, ICivilianData> getCivilianDataMap()
    {
        return Collections.unmodifiableMap(citizens);
    }

    @Override
    public void markDirty()
    {
        colony.markDirty();
        isCitizensDirty = true;
    }

    @Override
    public ICitizenData getCivilian(final int citizenId)
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
        return (int) Math.max(1, Math.min(maxCitizens, Math.min(maxCitizensFromResearch(), MineColonies.getConfig().getServer().maxCitizenPerColony.get())));
    }

    @Override
    public int getPotentialMaxCitizens()
    {
        return (int) Math.max(1, Math.min(potentialMaxCitizens, Math.min(maxCitizensFromResearch(), MineColonies.getConfig().getServer().maxCitizenPerColony.get())));
    }

    /**
     * Get the max citizens based on the research.
     *
     * @return the max.
     */
    private double maxCitizensFromResearch()
    {
        if(MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().hasResearchEffect(CITIZEN_CAP))
        {
            final double max = 25 + colony.getResearchManager().getResearchEffects().getEffectStrength(CITIZEN_CAP);
            return Math.min(max, MineColonies.getConfig().getServer().maxCitizenPerColony.get());
        }
        else
        {
            return MineColonies.getConfig().getServer().maxCitizenPerColony.get();
        }
    }

    /**
     * Get the current amount of citizens, might be bigger then {@link #getMaxCitizens()}
     *
     * @return The current amount of citizens in the colony.
     */
    @Override
    public int getCurrentCitizenCount()
    {
        return citizens.size() + colony.getGraveManager().getGraves().size();
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

    @Override
    public void updateModifier(final String id)
    {
        for (final ICitizenData citizenData : citizens.values())
        {
            citizenData.getCitizenHappinessHandler().getModifier(id).reset();
        }
    }

    @Override
    public void checkCitizensForHappiness()
    {
        for (final ICitizenData citizenData : citizens.values())
        {
            citizenData.getCitizenHappinessHandler().processDailyHappiness(citizenData);
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
            getCitizens().stream().filter(Objects::nonNull).forEach(ICitizenData::updateEntityIfNecessary);
        }

        //  Spawn initial Citizens
        if (colony.canMoveIn() && colony.hasTownHall() && getCitizens().size() < MineColonies.getConfig().getServer().initialCitizenAmount.get())
        {
            respawnInterval -= 500 + (SECONDS_A_MINUTE * colony.getBuildingManager().getTownHall().getBuildingLevel());

            if (respawnInterval <= 0)
            {
                respawnInterval = MineColonies.getConfig().getServer().citizenRespawnInterval.get() * TICKS_SECOND;
                int femaleCount = 0;
                for (ICitizenData citizens : getCitizens())
                {
                    femaleCount += citizens.isFemale() ? 1 : 0;
                }
                final ICitizenData newCitizen = createAndRegisterCivilianData();

                // For first citizen, give a random chance of male or female.
                if (getCitizens().size() == 1)
                {
                    newCitizen.setGenderAndGenerateName(random.nextBoolean());
                }
                // Otherwise, set the new colonist's gender to whatever gender is less common.
                // Use double division to avoid getting two male colonists in a row for the first set.
                else if (femaleCount < (getCitizens().size() - 1) / 2.0)
                {
                    newCitizen.setGenderAndGenerateName(true);
                }
                else
                {
                    newCitizen.setGenderAndGenerateName(false);
                }

                spawnOrCreateCivilian(newCitizen, colony.getWorld(), null, true);

                colony.getEventDescriptionManager().addEventDescription(new CitizenSpawnedEvent(colony.getBuildingManager().getTownHall().getPosition(),
                      newCitizen.getName()));
            }
        }
    }

    @Override
    public void updateCitizenMourn(final ICitizenData data, final boolean mourn)
    {
        if (mourn)
        {
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), COM_MINECOLONIES_COREMOD_MOURN, colony.getName(), data.getName());
        }

        for (final ICitizenData citizen : getCitizens())
        {
            if (mourn)
            {
                if (!(citizen.getJob() instanceof AbstractJobGuard) && !(citizen.getJob() instanceof JobUndertaker) && (citizen.isRelatedTo(data) || citizen.doesLiveWith(data)))
                {
                    citizen.getCitizenMournHandler().addDeceasedCitizen(data.getName());
                }
            }
            else
            {
                citizen.getCitizenMournHandler().removeDeceasedCitizen(data.getName());
            }
        }
    }

    @Override
    public ICitizenData getRandomCitizen()
    {
        return (ICitizenData) citizens.values().toArray()[random.nextInt(citizens.values().size())];
    }

    @Override
    public void updateCitizenSleep(final boolean sleep)
    {
        this.areCitizensSleeping = sleep;
    }

    @Override
    public void onCitizenSleep()
    {
        for (final ICitizenData citizenData : citizens.values())
        {
            if (!(citizenData.isAsleep() || citizenData.getJob() instanceof AbstractJobGuard))
            {
                return;
            }
        }

        if (!this.areCitizensSleeping)
        {
            LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(), ALL_CITIZENS_ARE_SLEEPING);
        }

        this.areCitizensSleeping = true;
    }

    @Override
    public void onWakeUp()
    {
        for (final ICitizenData citizenData : citizens.values())
        {
            if (citizenData.getCitizenMournHandler().isMourning())
            {
                citizenData.getCitizenMournHandler().clearDeceasedCitizen();
                citizenData.getCitizenMournHandler().setMourning(false);
            }
            else
            {
                if (citizenData.getCitizenMournHandler().shouldMourn())
                {
                    citizenData.getCitizenMournHandler().setMourning(true);
                }
            }
        }
    }
}
