package com.minecolonies.proxy;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenDataView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.event.EventHandler;
import com.minecolonies.event.FMLEventHandler;
import com.minecolonies.inventory.GuiHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;

public class CommonProxy implements IProxy
{
    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<>();
    private int nextEntityId = 0;

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

    @Override
    public void registerSounds()
    {
        
    }

    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityColonyBuilding.class, Constants.MOD_ID + ".ColonyBuilding");
        GameRegistry.registerTileEntity(ScarecrowTileEntity.class, Constants.MOD_ID + ".Scarecrow");
        NetworkRegistry.INSTANCE.registerGuiHandler(MineColonies.instance, new GuiHandler());
    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
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
        // Half as much tracking range and same update frequency as a player
        // See EntityTracker.addEntityToTracker for more default values
        EntityRegistry.registerModEntity(EntityCitizen.class, "Citizen", getNextEntityId(), MineColonies.instance, 256, 2, true);
        EntityRegistry.registerModEntity(EntityFishHook.class, "Fishhook", getNextEntityId(), MineColonies.instance, 250, 5, true);
    }

    @Override
    public void registerEntityRendering() {}

    @Override
    public void registerTileEntityRendering() {}

    @Override
    public void showCitizenWindow(CitizenDataView citizen) {}

    @Override
    public void openBuildToolWindow(BlockPos pos) {}

    @Override
    public void registerRenderer() { }

    /**
     * Used for entity IDs, starts at 0 & increments for each call
     */
    private int getNextEntityId()
    {
        return nextEntityId++;
    }
}
