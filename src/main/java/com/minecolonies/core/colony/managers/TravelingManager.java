package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.ITravelingManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation that manages the traveling system for a given colony.
 */
public class TravelingManager implements ITravelingManager
{
    /**
     * The colony this manager belongs to.
     */
    private final IColony colony;

    /**
     * The map containing the information on which citizens are travelling.
     */
    private final Map<Integer, TravelerData> travelerDataMap = new HashMap<>();

    /**
     * Whether this manager is dirty and needs to sync to the client again.
     */
    private boolean dirty = false;

    /**
     * Default constructor.
     *
     * @param colony the colony this manager belongs to.
     */
    public TravelingManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean isTravelling(final int citizenId)
    {
        return Optional.ofNullable(travelerDataMap.get(citizenId)).map(TravelerData::isTraveling).orElse(false);
    }

    @Override
    public Optional<BlockPos> getTravellingTargetFor(final int citizenId)
    {
        return Optional.ofNullable(travelerDataMap.get(citizenId)).map(TravelerData::getTarget);
    }

    @Override
    public void startTravellingTo(final int citizenId, final BlockPos target, final int travelTimeInTicks, final boolean canRecall)
    {
        travelerDataMap.put(citizenId, new TravelerData(citizenId, target, travelTimeInTicks, canRecall));
        dirty = true;
        colony.markDirty();
    }

    @Override
    public void finishTravellingFor(final int citizenId)
    {
        if (travelerDataMap.containsKey(citizenId))
        {
            travelerDataMap.remove(citizenId);
            dirty = true;
            colony.markDirty();
        }
    }

    @Override
    public void recallAllTravellingCitizens()
    {
        final Map<Integer, TravelerData> travelersToKeep = new HashMap<>();
        for (final TravelerData travelerData : this.travelerDataMap.values())
        {
            if (!travelerData.canRecall)
            {
                travelersToKeep.put(travelerData.citizenId, travelerData);
                continue;
            }

            final ICitizenData citizenData = this.colony.getCitizenManager().getCivilian(travelerData.citizenId);
            final BlockPos spawnHutPos;
            if (citizenData.getWorkBuilding() != null)
            {
                spawnHutPos = citizenData.getWorkBuilding().getPosition();
            }
            else
            {
                spawnHutPos = colony.getBuildingManager().getTownHall().getPosition();
            }

            Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
            if (optionalEntityCitizen.isEmpty())
            {
                Log.getLogger().warn(String.format("The traveller %d from colony #%d has returned very confused!", citizenData.getId(), colony.getID()));
                citizenData.setNextRespawnPosition(EntityUtils.getSpawnPoint(colony.getWorld(), spawnHutPos));
                citizenData.updateEntityIfNecessary();
                optionalEntityCitizen = citizenData.getEntity();
            }

            optionalEntityCitizen.ifPresent(abstractEntityCitizen -> TeleportHelper.teleportCitizen(abstractEntityCitizen, colony.getWorld(), spawnHutPos));
        }

        this.travelerDataMap.clear();
        this.travelerDataMap.putAll(travelersToKeep);
        dirty = true;
        colony.markDirty();
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void setDirty(final boolean dirty)
    {
        this.dirty = dirty;
    }

    /**
     * Tick the travelers it's data.
     */
    public void onTick()
    {
        travelerDataMap.values().forEach(TravelerData::onTick);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag data = new CompoundTag();
        final ListTag travelerData = new ListTag();

        travelerDataMap.values().stream().map(TravelerData::serializeNBT).forEach(travelerData::add);

        data.put(NbtTagConstants.TRAVELER_DATA, travelerData);

        return data;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final ListTag travelerData = nbt.getList(NbtTagConstants.TRAVELER_DATA, Tag.TAG_COMPOUND);
        travelerDataMap.clear();

        travelerData
          .stream()
          .filter(CompoundTag.class::isInstance)
          .map(CompoundTag.class::cast)
          .map(TravelerData::new)
          .forEach(data -> travelerDataMap.put(data.getCitizenId(), data));
    }

    /**
     * Container class for a traveling citizen.
     */
    private static final class TravelerData implements INBTSerializable<CompoundTag>
    {
        /**
         * The id of the citizen.
         */
        private int citizenId = -1;

        /**
         * The position they are traveling to.
         */
        private BlockPos target = BlockPos.ZERO;

        /**
         * The amount of time they are traveling for.
         */
        private int initialTravelTime = 0;

        /**
         * How much of their travel time is remaining.
         */
        private int remainingTravelTime = 0;

        /**
         * Whether this citizen can be recalled to the town hall or not.
         */
        private boolean canRecall = true;

        /**
         * Default constructor.
         *
         * @param citizenId         the id of the citizen.
         * @param target            the position they are traveling to.
         * @param initialTravelTime the amount of time they are traveling for.
         * @param canRecall         whether this citizen can be recalled to the town hall or not.
         */
        public TravelerData(final int citizenId, final BlockPos target, final int initialTravelTime, final boolean canRecall)
        {
            this.citizenId = citizenId;
            this.target = target;
            this.initialTravelTime = initialTravelTime;
            this.remainingTravelTime = initialTravelTime;
            this.canRecall = canRecall;
        }

        /**
         * Deserialization constructor.
         *
         * @param tag the compound data.
         */
        public TravelerData(final CompoundTag tag)
        {
            this.deserializeNBT(tag);
        }

        /**
         * Ticking method for the travelers.
         */
        public void onTick()
        {
            if (remainingTravelTime > 0)
            {
                remainingTravelTime -= ColonyConstants.UPDATE_TRAVELING_INTERVAL;
                remainingTravelTime = Math.max(0, remainingTravelTime);
            }
        }

        /**
         * Whether the citizen has reached their target.
         *
         * @return true when the travel time is done.
         */
        public boolean hasReachedTarget()
        {
            return remainingTravelTime == 0;
        }

        /**
         * Get the current percentage of travel time.
         *
         * @return a percentage.
         */
        public double getTravelPercentage()
        {
            return ((double) remainingTravelTime * 100) / (double) initialTravelTime;
        }

        /**
         * Get the id of the citizen.
         *
         * @return the number.
         */
        public int getCitizenId()
        {
            return citizenId;
        }

        /**
         * Get the position they are traveling to.
         *
         * @return the position.
         */
        public BlockPos getTarget()
        {
            return target;
        }

        /**
         * Get the amount of time they are traveling for.
         *
         * @return the time.
         */
        public int getInitialTravelTime()
        {
            return initialTravelTime;
        }

        /**
         * Get how much of their travel time is remaining.
         *
         * @return the time.
         */
        public int getRemainingTravelTime()
        {
            return remainingTravelTime;
        }

        /**
         * Get whether the citizen is still busy traveling or not.
         *
         * @return true when the travel time is not done.
         */
        public boolean isTraveling()
        {
            return !hasReachedTarget();
        }

        /**
         * Get whether this citizen can be recalled to the town hall or not.
         *
         * @return true if so.
         */
        public boolean canRecall()
        {
            return canRecall;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag data = new CompoundTag();
            data.putInt(NbtTagConstants.TAG_CITIZEN, citizenId);
            data.put(NbtTagConstants.TAG_TARGET, NbtUtils.writeBlockPos(target));
            data.putInt(NbtTagConstants.TAG_INITIAL_TRAVEL_TIME, initialTravelTime);
            data.putInt(NbtTagConstants.TAG_REMAINING_TRAVEL_TIME, remainingTravelTime);
            data.putBoolean(NbtTagConstants.TAG_CAN_RECALL, canRecall);
            return data;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.citizenId = nbt.getInt(NbtTagConstants.TAG_CITIZEN);
            this.target = NbtUtils.readBlockPos(nbt.getCompound(NbtTagConstants.TAG_TARGET));
            this.initialTravelTime = nbt.getInt(NbtTagConstants.TAG_INITIAL_TRAVEL_TIME);
            this.remainingTravelTime = nbt.getInt(NbtTagConstants.TAG_REMAINING_TRAVEL_TIME);
            this.canRecall = nbt.getBoolean(NbtTagConstants.TAG_CAN_RECALL);
        }
    }
}