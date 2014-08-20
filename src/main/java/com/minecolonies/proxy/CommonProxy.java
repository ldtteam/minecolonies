package com.minecolonies.proxy;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.event.EventHandler;
import com.minecolonies.tileentities.*;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

public class CommonProxy implements IProxy
{
    private int nextEntityId = 0;

    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<String, NBTTagCompound>();

    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityTownHall.class, "tileEntityTownHall");
        GameRegistry.registerTileEntity(TileEntityHutBuilder.class, "tileEntityHutBuilder");
        GameRegistry.registerTileEntity(TileEntityHutCitizen.class, "tileEntityHutCitizen");
        GameRegistry.registerTileEntity(TileEntityHutBaker.class, "tileEntityHutBaker");
        GameRegistry.registerTileEntity(TileEntityHutBlacksmith.class, "tileEntityHutBlacksmith");
        GameRegistry.registerTileEntity(TileEntityHutFarmer.class, "tileEntityHutFarmer");
        GameRegistry.registerTileEntity(TileEntityHutLumberjack.class, "tileEntityHutLumberjack");
        GameRegistry.registerTileEntity(TileEntityHutMiner.class, "tileEntityHutMiner");
        GameRegistry.registerTileEntity(TileEntityHutStonemason.class, "tileEntityHutStonemason");
        GameRegistry.registerTileEntity(TileEntityHutWarehouse.class, "tileEntityHutWarehouse");
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

    /*
    * @param entityName A unique name for the entity
    * @param id A mod specific ID for the entity
    * @param mod The mod
    * @param trackingRange The range at which MC will send tracking updates
    * @param updateFrequency The frequency of tracking updates
    * @param sendsVelocityUpdates Whether to send velocity information packets as well
    * */
    @Override
    public void registerEntities()
    {
        EntityRegistry.registerModEntity(EntityCitizen.class, "entityCitizen", getNextEntityId(), MineColonies.instance, 250, 3, true);
        EntityRegistry.registerModEntity(EntityBuilder.class, "entityBuilder", getNextEntityId(), MineColonies.instance, 250, 3, true);
        EntityRegistry.registerModEntity(EntityDeliveryman.class, "entityDeliveryman", getNextEntityId(), MineColonies.instance, 250, 3, true);
    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    @Override
    public void registerEntityRendering(){}

    @Override
    public void registerTileEntityRendering(){}

    @Override
    public void registerKeybindings(){}

    /**
     * Used for entity IDs, starts at 0 & increments for each call
     */
    private int getNextEntityId()
    {
        return nextEntityId++;
    }
}
