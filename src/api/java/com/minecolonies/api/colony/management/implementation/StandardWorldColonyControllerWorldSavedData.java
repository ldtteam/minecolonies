package com.minecolonies.api.colony.management.implementation;

import com.minecolonies.api.colony.management.IWorldColonyController;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;
import org.jetbrains.annotations.NotNull;

/**
 * A class that functions as a Wrapper around a {@link IWorldColonyController} data.
 * It is used as a {@link WorldSavedData} to store the data of said {@link IWorldColonyController}
 * into the {@link net.minecraft.world.World} internal Data store.
 */
public class StandardWorldColonyControllerWorldSavedData extends WorldSavedData
{
    ////// --------------------------- String Constants --------------------------- \\\\\\
    public static final String WORLD_SAVED_DATA_PREFIX = "Minecolonies_ColonyManager";
    ////// --------------------------- String Constants --------------------------- \\\\\\

    ////// --------------------------- NBT Constants --------------------------- \\\\\\
    public static final String TAG_CONTROLLER = "Controller";
    ////// --------------------------- NBT Constants --------------------------- \\\\\\

    /**
     * The {@link IWorldColonyController} for which this {@link StandardWorldColonyControllerWorldSavedData}
     * wraps and stores the data into the {@link net.minecraft.world.World}.
     */
    private IWorldColonyController<?, ?> controller;

    /**
     * The NBTTagCompound that was loaded from the NBT of the {@link net.minecraft.world.World}.
     * It is set by MC by calling the {@link #deserializeNBT(NBTBase)} method.
     *
     * Once a Controller is being set after the loading, by calling the {@link #setController(IWorldColonyController)}
     * method this Tag is read into the first given controller and cleared.
     *
     * As such it only functions as a temporary storage of the data stored in the NBT to hold it, while MC finishes the loading
     * of this {@link StandardWorldColonyControllerWorldSavedData}, until Minecolonies sets the {@link IWorldColonyController}
     * for this {@link StandardWorldColonyControllerWorldSavedData}.
     */
    private NBTTagCompound loadCompound;

    public StandardWorldColonyControllerWorldSavedData(final String name)
    {
        this();
    }

    public StandardWorldColonyControllerWorldSavedData()
    {
        super(WORLD_SAVED_DATA_PREFIX);
    }

    //TODO: Fix this.
    @Override
    public void readFromNBT(final NBTTagCompound nbt)
    {
        loadCompound = nbt;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setTag(TAG_CONTROLLER, getController().serializeNBT());
        return compound;
    }

    /**
     * Method to ge the current {@link IWorldColonyController} for which this {@link StandardWorldColonyControllerWorldSavedData}
     * wraps and stores the Data.
     * @return The {@link IWorldColonyController} that is linked to this {@link StandardWorldColonyControllerWorldSavedData}
     */
    public IWorldColonyController<?, ?> getController()
    {
        return controller;
    }

    /**
     * Method to set the {@link IWorldColonyController} of which this {@link StandardWorldColonyControllerWorldSavedData}
     * holds the data.
     *
     * If the field {@link #loadCompound} is not null, this {@link StandardWorldColonyControllerWorldSavedData}
     * will load the data into the given {@link IWorldColonyController}.
     *
     * @param controller The {@link IWorldColonyController} for which this {@link StandardWorldColonyControllerWorldSavedData} is supposed to hold data.
     */
    public void setController(@NotNull final IWorldColonyController<?, ?> controller)
    {
        this.controller = controller;
        if (loadCompound != null) {
            Log.getLogger().debug("First time loading for this World. Reading from NBT.");
            getController().deserializeNBT(loadCompound);
            loadCompound = null;
        }
    }
}
