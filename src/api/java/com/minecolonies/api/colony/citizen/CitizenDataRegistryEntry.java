package com.minecolonies.api.colony.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Registry entry for citizen data types, matches a resource location id to a creation function.
 */
public class CitizenDataRegistryEntry extends ForgeRegistryEntry<CitizenDataRegistryEntry> implements IForgeRegistryEntry<CitizenDataRegistryEntry>
{
    /**
     * Function for creating the data objects.
     */
    private final BiFunction<IColony, CompoundNBT, ICitizenData> dataCreator;

    /**
     * Creates a new registry entry for the given function and registry name
     *
     * @param eventCreator the event creator.
     * @param registryID   the registry id.
     */
    public CitizenDataRegistryEntry(@NotNull final BiFunction<IColony, CompoundNBT, ICitizenData> eventCreator, @NotNull final ResourceLocation registryID)
    {
        if (registryID.getPath().isEmpty())
        {
            Log.getLogger().warn("Created empty registry empty for event, supply a name for it!");
        }

        this.dataCreator = eventCreator;
        setRegistryName(registryID);
    }

    /**
     * Gets the event creation function.
     *
     * @return the event creator (colony, nbt, event).
     */
    public BiFunction<IColony, CompoundNBT, ICitizenData> getDataCreator()
    {
        return dataCreator;
    }
}
