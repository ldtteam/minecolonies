package com.minecolonies.coremod.colony.colonyEvents;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEntitySpawnEvent;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Event for recruitable citizens spawning in the tavern
 */
public class ColonyRecruitableCitizenEvent implements IColonyEntitySpawnEvent
{
    public static ResourceLocation RECRUITABLE_CITIZEN_EVENT = new ResourceLocation(MOD_ID, "recruit_citizen_event");

    private final List<CitizenData> externalCitizens = new ArrayList<>();

    private final IColony colony;

    public ColonyRecruitableCitizenEvent(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void setSpawnPoint(final BlockPos spawnPoint)
    {

    }

    @Override
    public BlockPos getSpawnPos()
    {
        return null;
    }

    @Override
    public EventStatus getStatus()
    {
        return null;
    }

    @Override
    public void setStatus(final EventStatus status)
    {

    }

    @Override
    public int getID()
    {
        return 0;
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return null;
    }

    @Override
    public void setColony(@NotNull final IColony colony)
    {

    }

    @Override
    public CompoundNBT writeToNBT(final CompoundNBT compound)
    {
        return null;
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {

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
     * @return the raid event.
     */
    public static ColonyRecruitableCitizenEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        ColonyRecruitableCitizenEvent event = new ColonyRecruitableCitizenEvent(colony);
        event.readFromNBT(compound);
        return event;
    }
}
