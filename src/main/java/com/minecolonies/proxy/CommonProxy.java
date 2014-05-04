package com.minecolonies.proxy;

import com.minecolonies.tilentities.TileEntityTownHall;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public abstract class CommonProxy implements IProxy
{
    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<String, NBTTagCompound>();

    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityTownHall.class, "tileEntityTownHall");
    }

    /**
     * Adds an entity's custom data to the map for temporary storage
     *
     * @param name     player UUID + Properties name, HashMap key
     * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties data only
     */
    public static void storeEntityData(String name, NBTTagCompound compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name or null if none exists
     *
     * @param name player UUID + Properties name, HashMap key
     * @return NBTTagCompound PlayerProperties NBT compound
     */
    public static NBTTagCompound getEntityData(String name)
    {
        return playerPropertiesData.remove(name);
    }
}
