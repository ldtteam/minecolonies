package com.minecolonies.api.colony.colonyEvents;

import com.minecolonies.api.colony.IColony;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for colony event types.
 */
public interface IColonyEvent
{
    void setSpawnPoint(BlockPos spawnPoint);

    /**
     * The position the event starts at
     *
     * @return
     */
    public BlockPos getStartPos();

    /**
     * The list of entities related to this event
     *
     * @return
     */
    default public List<Entity> getEntities()
    {
        return new ArrayList<>();
    }

    /**
     * Returns the events current status
     *
     * @return
     */
    public EventStatus getStatus();

    /**
     * Sets the current event status
     *
     * @return
     */
    public void setStatus(final EventStatus status);

    /**
     * Returns this events ID.
     *
     * @return
     */
    public int getID();

    /**
     * The event type's id
     *
     * @return
     */
    public ResourceLocation getEventTypeID();

    /**
     * Sets the colony
     *
     * @param colony
     */
    public void setColony(@NotNull final IColony colony);

    /**
     * Writes the event to NBT
     *
     * @param compound
     * @return
     */
    public NBTTagCompound writeToNBT(final NBTTagCompound compound);

    /**
     * Reads the events values from NBT
     *
     * @param compound
     */
    public void readFromNBT(final NBTTagCompound compound);

    /**
     * Called to register an entity with this event
     *
     * @param entity
     */
    default public void registerEntity(final Entity entity) {}

    /**
     * called to unregister an entity with this event
     *
     * @param entity
     */
    default public void unregisterEntity(final Entity entity) {}

    /**
     *
     * Event triggers
     *
     */

    /**
     * Trigger on entity death.
     *
     * @param entity
     */
    default public void onEntityDeath(final EntityLiving entity) {}

    /**
     * Onupdate function, called every 25s / 500 ticks. Comes from colony -> eventmanager -> event.
     */
    default public void onUpdate() {}

    /**
     * Actions which are done on the start of the event.
     */
    default public void onStart() {}

    /**
     * Actions which are done on the finish/removal of the event.
     */
    default public void onFinish() {}

    /**
     * Called by tileentities relevant to the event on invalidation.
     *
     * @param te
     */
    default public void onTileEntityBreak(final TileEntity te) {}

    /**
     * Called on night fall, to execute special day-based logic.
     */
    default public void onNightFall() {}
}
