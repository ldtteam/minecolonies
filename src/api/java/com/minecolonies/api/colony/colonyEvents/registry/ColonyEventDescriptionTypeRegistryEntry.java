package com.minecolonies.api.colony.colonyEvents.registry;

import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * This is the colonies event registry entry class, used for registering any colony related events. Takes a function of colony, nbt to create the right event object.
 */
public class ColonyEventDescriptionTypeRegistryEntry extends ForgeRegistryEntry<ColonyEventDescriptionTypeRegistryEntry>
{
    /**
     * Function for creating the event description from nbt.
     */
    private final Function<CompoundNBT, IColonyEventDescription> nbtEventDescriptionCreator;

    /**
     * Function for creating the event description from a {@link PacketBuffer}.
     */
    private final Function<PacketBuffer, IColonyEventDescription> packetBufferEventDescriptionCreator;

    /**
     * Creates a new registry entry for the given function and registry name.
     *
     * @param nbtEventCreator          the event creator using nbt.
     * @param packetBufferEventCreator the event creator using a {@link PacketBuffer}.
     * @param registryID               the registry id.
     */
    public ColonyEventDescriptionTypeRegistryEntry(@NotNull final Function<CompoundNBT, IColonyEventDescription> nbtEventCreator, @NotNull final Function<PacketBuffer, IColonyEventDescription> packetBufferEventCreator, @NotNull final ResourceLocation registryID)
    {
        if (registryID.getPath().isEmpty())
        {
            Log.getLogger().warn("Created empty registry empty for event, supply a name for it!");
        }

        nbtEventDescriptionCreator = nbtEventCreator;
        packetBufferEventDescriptionCreator = packetBufferEventCreator;
        setRegistryName(registryID);
    }

    /**
     * Deserializes the event description from nbt.
     * 
     * @param compound the nbt to deserialize the event description from.
     * @return the deserialized event description.
     */
    public IColonyEventDescription deserializeEventDescriptionFromNBT(@Nonnull final CompoundNBT compound)
    {
        return nbtEventDescriptionCreator.apply(compound);
    }

    /**
     * Deserializes the event description from the given {@link PacketBuffer}.
     * 
     * @param buffer the {@link PacketBuffer} to deserialize the event description from.
     * @return the deserialized event description.
     */
    public IColonyEventDescription deserializeEventDescriptionFromPacketBuffer(@Nonnull final PacketBuffer buffer)
    {
        return packetBufferEventDescriptionCreator.apply(buffer);
    }
}
