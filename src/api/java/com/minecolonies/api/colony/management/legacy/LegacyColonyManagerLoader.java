package com.minecolonies.api.colony.management.legacy;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class used to load the legacy Save mechanic
 */
public class LegacyColonyManagerLoader<B extends IBuilding, C extends IColony<B>>
{

    /**
     * The file name of the minecolonies path.
     */
    private final String FILENAME_MINECOLONIES_PATH = "minecolonies";

    /**
     * The file name of the minecolonies.
     */
    private final String FILENAME_MINECOLONIES = "colonies.dat";

    /**
     * The tag of the colonies.
     */
    private final String TAG_COLONIES = "colonies";
    /**
     * The tag of the pseudo unique identifier
     */
    private final String TAG_UUID     = "uuid";

    /**
     * The list of all colonies by world.
     */
    @NotNull
    private Map<Integer, List<C>> coloniesByWorld;

    /**
     * The UUID Id of the server stored in the Legacy data.
     */
    @NotNull
    private UUID serverUUID = UUID.randomUUID();

    /**
     * Eventhandler for the world load event.
     * Will load the Legacy NBT data only on the first call.
     * @param worldLoadEvent The event of the world that is being loaded.
     */
    private void onWorldLoadEvent(final @NotNull WorldEvent.Load worldLoadEvent) {
        if (coloniesByWorld == null) {
            readFromNBT(loadNBTFromPath(getSaveLocation()));
        }
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    private File getSaveLocation()
    {
        @NotNull final File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Load a file and return the data as an NBTTagCompound.
     *
     * @param file The path to the file.
     * @return the data from the file as an NBTTagCompound, or null.
     */
    private NBTTagCompound loadNBTFromPath(@Nullable final File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when loading Colony Data through the legacy code.", exception);
        }
        return null;
    }

    /**
     * Loads the colony data from the given NBTTagCompound.
     * @param compound The compound to load the data from.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        coloniesByWorld = new HashMap<>();

        Log.getLogger().warn("Loading colonies from Legacy File.");

        final NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            @NotNull final C colony = StandardFactoryController.getInstance().deserialize(colonyTags.getCompoundTagAt(i));
            coloniesByWorld.computeIfAbsent(colony.getDimension(), ArrayList::new).add(colony);
        }

        if (compound.hasUniqueId(TAG_UUID))
        {
            serverUUID = compound.getUniqueId(TAG_UUID);
        }

        Log.getLogger().warn("Finished loading colonies from Legacy File.");
    }

    /**
     * Method to get all the {@link C} loaded by the legacy loader for a given {@link net.minecraft.world.World}.
     * @param worldId The ID of the world to get the {@link C}
     * @return A {@link List<C>} with colonies loaded by this Loader. Null if no colonies were loaded for that map.
     */
    @Nullable
    public List<C> getColoniesForWorldId(@NotNull final Integer worldId) {
        return coloniesByWorld.get(worldId);
    }
}
