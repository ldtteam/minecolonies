package com.minecolonies.coremod.colony.colonyEvents;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEntitySpawnEvent;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.*;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.Constants.TAG_COMPOUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CITIZENS;

/**
 * Event for recruitable citizens spawning in the tavern
 */
public class ColonyRecruitableCitizenEvent implements IColonyEntitySpawnEvent
{
    /**
     * Event registry id
     */
    public static ResourceLocation RECRUITABLE_CITIZEN_EVENT = new ResourceLocation(MOD_ID, "recruit_citizen_event");

    /**
     * List of additional citizens
     */
    private final List<CitizenData> externalCitizens = new ArrayList<>();

    /**
     * The tavern's pos
     */
    private BlockPos spawnPos;

    /**
     * Colony of the event
     */
    private IColony colony;

    /**
     * The events status
     */
    private EventStatus status = EventStatus.STARTING;

    /**
     * The event's id
     */
    private int eventID;

    public ColonyRecruitableCitizenEvent(final IColony colony)
    {
        this.colony = colony;
        eventID = colony.getEventManager().getAndTakeNextEventID();
    }

    @Override
    public void onStart()
    {
        status = EventStatus.PROGRESSING;
    }

    @Override
    public void onUpdate()
    {
        if (colony.getBuildingManager().getBuilding(spawnPos) == null)
        {
            for (final ICitizenData data : externalCitizens)
            {
                if (data.getCitizenEntity().isPresent())
                {
                    data.getCitizenEntity().get().remove();
                }
            }

            status = EventStatus.DONE;
            return;
        }

        if (externalCitizens.size() < 1)
        {
            CitizenData newCitizen = new CitizenData(0, colony);
            newCitizen.initForNewCitizen();
            externalCitizens.add(newCitizen);
            newCitizen.setBedPos(spawnPos);
            newCitizen.setHomeBuilding(colony.getBuildingManager().getBuilding(spawnPos));
            newCitizen.getCitizenSkillHandler().init(20);
            colony.getCitizenManager().spawnOrCreateCitizen(newCitizen, colony.getWorld(), spawnPos, true);
        }
    }

    @Override
    public void setSpawnPoint(final BlockPos spawnPoint)
    {
        this.spawnPos = spawnPoint;
    }

    @Override
    public BlockPos getSpawnPos()
    {
        return spawnPos;
    }

    @Override
    public EventStatus getStatus()
    {
        return status;
    }

    @Override
    public void setStatus(final EventStatus status)
    {
        this.status = status;
    }

    @Override
    public int getID()
    {
        return eventID;
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return RECRUITABLE_CITIZEN_EVENT;
    }

    @Override
    public void setColony(@NotNull final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public CompoundNBT writeToNBT(final CompoundNBT compound)
    {
        compound.putInt(TAG_EVENT_ID, eventID);
        BlockPosUtil.write(compound, TAG_SPAWN_POS, spawnPos);
        compound.putInt(TAG_EVENT_STATUS, status.ordinal());
        final ListNBT citizenList = new ListNBT();
        for (final ICitizenData data : externalCitizens)
        {
            citizenList.add(data.serializeNBT());
        }

        compound.put(TAG_CITIZENS, citizenList);

        return compound;
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        eventID = compound.getInt(TAG_EVENT_ID);
        spawnPos = BlockPosUtil.read(compound, TAG_SPAWN_POS);
        final ListNBT citizenList = compound.getList(TAG_CITIZENS, TAG_COMPOUND);
        for (final INBT data : citizenList)
        {
            CitizenData citizenData = new CitizenData(0, colony);
            citizenData.deserializeNBT((CompoundNBT) data);
            externalCitizens.add(citizenData);
        }
        status = EventStatus.values()[compound.getInt(TAG_EVENT_STATUS)];

    }

    /**
     * The list of entities related to this event
     *
     * @return the list.
     */
    @Override
    public List<Entity> getEntities()
    {
        return new ArrayList<>();
    }

    /**
     * Called to register an entity with this event
     *
     * @param entity the entity to register.
     */
    @Override
    public void registerEntity(final Entity entity) {}

    /**
     * called to unregister an entity with this event
     *
     * @param entity the entity to unregister.
     */
    @Override
    public void unregisterEntity(final Entity entity) {}

    /**
     * Trigger on entity death.
     *
     * @param entity the killed entity.
     */
    @Override
    public void onEntityDeath(final LivingEntity entity) {}

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBTcompound with saved values
     * @return the event.
     */
    public static ColonyRecruitableCitizenEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        ColonyRecruitableCitizenEvent event = new ColonyRecruitableCitizenEvent(colony);
        event.readFromNBT(compound);
        return event;
    }
}
