package com.minecolonies.api.colony.management.implementation;

import com.minecolonies.api.colony.management.IWorldColonyController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

/**
 * ----------------------- Not Documented Object ---------------------
 * TODO: Document Object
 */
public class StandardWorldColonyControllerWorldSavedData extends WorldSavedData
{
    ////// --------------------------- String Constants --------------------------- \\\\\\
    public static final String WORLD_SAVED_DATA_PREFIX = "Minecolonies_ColonyManager";
    ////// --------------------------- String Constants --------------------------- \\\\\\

    ////// --------------------------- String Constants --------------------------- \\\\\\
    public static final String TAG_CONTROLLER = "Controller";
    ////// --------------------------- String Constants --------------------------- \\\\\\

    private IWorldColonyController controller;

    public StandardWorldColonyControllerWorldSavedData(final String name)
    {
        this();
    }

    public StandardWorldColonyControllerWorldSavedData()
    {
        super(WORLD_SAVED_DATA_PREFIX);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt)
    {
        getController().deserializeNBT(nbt.getCompoundTag(TAG_CONTROLLER));
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        compound.setTag(TAG_CONTROLLER, getController().serializeNBT());
        return compound;
    }

    public IWorldColonyController getController()
    {
        return controller;
    }

    public void setController(final IWorldColonyController controller)
    {
        this.controller = controller;
    }
}
