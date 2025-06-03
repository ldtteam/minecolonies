package com.minecolonies.core.colony.managers;

import com.google.common.collect.Maps;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.ITravellingManager;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the travelling system and its API.
 * Keeps track of the travelling destination and progress of a given party.
 * <p>
 * Does not handle any interactions with the entities or the citizens.
 * </p>
 */
public class TravellingManager implements ITravellingManager, INBTSerializable<CompoundTag>
{

    private final IColony                    colony;
    private final Map<Integer, TravelerData> travelerDataMap = Maps.newHashMap();

    public TravellingManager(final IColony colony) {this.colony = colony;}

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
    public void startTravellingTo(final int citizenId, final BlockPos target, final int travelTimeInTicks)
    {
        travelerDataMap.put(citizenId, new TravelerData(citizenId, target, travelTimeInTicks));
        colony.markDirty();
    }

    @Override
    public void finishTravellingFor(final int citizenId)
    {
        if (travelerDataMap.containsKey(citizenId))
        {
            travelerDataMap.remove(citizenId);
            colony.markDirty();
        }
    }

    @Override
    public void recallAllTravellingCitizens()
    {
        for (final Integer citizenId : travelerDataMap.keySet())
        {
            final ICitizenData citizenData = this.colony.getCitizenManager().getCivilian(citizenId);
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
        colony.markDirty();
    }

    public boolean onTick()
    {
        travelerDataMap.values().forEach(TravelerData::onTick);
        return true;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag data = new CompoundTag();
        final ListTag output = new ListTag();

        for (TravelerData travelerData : travelerDataMap.values())
        {
            CompoundTag serializeNBT = travelerData.serializeNBT();
            output.add(serializeNBT);
        }

        data.put(NbtTagConstants.TRAVELER_DATA, output);

        return data;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final ListTag travelerData = nbt.getList(NbtTagConstants.TRAVELER_DATA, Tag.TAG_COMPOUND);
        travelerDataMap.clear();

        for (Tag travelerDatum : travelerData)
        {
            if (travelerDatum instanceof final CompoundTag compoundTag)
            {
                TravelerData data = new TravelerData(compoundTag);
                travelerDataMap.put(data.getCitizenId(), data);
            }
        }
    }

    private static final class TravelerData implements INBTSerializable<CompoundTag>
    {
        private int      citizenId           = -1;
        private BlockPos target              = BlockPos.ZERO;
        private int      initialTravelTime   = 0;
        private int      remainingTravelTime = 0;

        public TravelerData(final int citizenId, final BlockPos target, final int initialTravelTime)
        {
            this.citizenId = citizenId;
            this.target = target;
            this.initialTravelTime = initialTravelTime;
            this.remainingTravelTime = initialTravelTime;
        }

        public TravelerData(final CompoundTag tag)
        {
            this.deserializeNBT(tag);
        }

        public void onTick()
        {
            if (remainingTravelTime > 0)
            {
                remainingTravelTime -= ColonyConstants.UPDATE_TRAVELING_INTERVAL;
                remainingTravelTime = Math.max(0, remainingTravelTime);
            }
        }

        public boolean hasReachedTarget()
        {
            return remainingTravelTime == 0;
        }

        public double getTravelPercentage()
        {
            return ((double) remainingTravelTime * 100) / (double) initialTravelTime;
        }

        public int getCitizenId()
        {
            return citizenId;
        }

        public BlockPos getTarget()
        {
            return target;
        }

        public int getInitialTravelTime()
        {
            return initialTravelTime;
        }

        public int getRemainingTravelTime()
        {
            return remainingTravelTime;
        }

        public boolean isTraveling()
        {
            return !hasReachedTarget();
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag data = new CompoundTag();
            data.putInt(NbtTagConstants.TAG_CITIZEN, citizenId);
            data.put(NbtTagConstants.TAG_TARGET, NbtUtils.writeBlockPos(target));
            data.putInt(NbtTagConstants.TAG_INITIAL_TRAVEL_TIME, initialTravelTime);
            data.putInt(NbtTagConstants.TAG_REMAINING_TRAVEL_TIME, remainingTravelTime);
            return data;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.citizenId = nbt.getInt(NbtTagConstants.TAG_CITIZEN);
            this.target = NbtUtils.readBlockPos(nbt.getCompound(NbtTagConstants.TAG_TARGET));
            this.initialTravelTime = nbt.getInt(NbtTagConstants.TAG_INITIAL_TRAVEL_TIME);
            this.remainingTravelTime = nbt.getInt(NbtTagConstants.TAG_REMAINING_TRAVEL_TIME);
        }
    }
}
