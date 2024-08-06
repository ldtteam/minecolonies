package com.minecolonies.core.colony.eventhooks.citizenEvents;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DEATH_CAUSE;

/**
 * The event for handling a citizen death.
 */
public class CitizenDiedEvent extends AbstractCitizenEvent
{

    /**
     * This events id, registry entries use res locations as ids.
     */
    public static final ResourceLocation CITIZEN_DIED_EVENT_ID = new ResourceLocation(Constants.MOD_ID, "citizen_died");

    private String deathCause;

    /**
     * Creates a new citizen died event.
     */
    public CitizenDiedEvent()
    {
        super();
    }

    /**
     * Creates a new citizen died event.
     * 
     * @param eventPos    the position of the hut block of the building.
     * @param citizenName the name of the building.
     * @param deathCause  the cause of the citizen death.
     */
    public CitizenDiedEvent(BlockPos eventPos, String citizenName, String deathCause)
    {
        super(eventPos, citizenName);
        this.deathCause = deathCause;
    }

    @Override
    public ResourceLocation getEventTypeId()
    {
        return CITIZEN_DIED_EVENT_ID;
    }

    @Override
    public String getName()
    {
        return "Citizen Died";
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        CompoundTag compound = super.serializeNBT(provider);
        compound.putString(TAG_DEATH_CAUSE, deathCause);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);
        deathCause = compound.getString(TAG_DEATH_CAUSE);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf)
    {
        super.serialize(buf);
        buf.writeUtf(deathCause);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf)
    {
        super.deserialize(buf);
        deathCause = buf.readUtf();
    }

    /**
     * Gets the cause of the citizen death.
     * 
     * @return the cause of the citizen death.
     */
    public String getDeathCause()
    {
        return deathCause;
    }

    /**
     * Sets the cause of the citizen death.
     * 
     * @param deathCause the cause of the citizen death.
     */
    public void setDeathCause(String deathCause)
    {
        this.deathCause = deathCause;
    }

    /**
     * Loads the citizen died event from the given nbt.
     *
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static CitizenDiedEvent loadFromNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compound)
    {
        final CitizenDiedEvent deathEvent = new CitizenDiedEvent();
        deathEvent.deserializeNBT(provider, compound);
        return deathEvent;
    }

    /**
     * Loads the citizen died event from the given packet buffer.
     *
     * @param buf the packet buffer.
     * @return the colony to load.
     */
    public static CitizenDiedEvent loadFromFriendlyByteBuf(@NotNull final RegistryFriendlyByteBuf buf)
    {
        final CitizenDiedEvent deathEvent = new CitizenDiedEvent();
        deathEvent.deserialize(buf);
        return deathEvent;
    }
}
