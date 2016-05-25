package com.minecolonies.proxy;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.event.EventHandler;
import com.minecolonies.event.FMLEventHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.schematica.world.storage.Schematic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;

public class CommonProxy implements IProxy
{
    private int nextEntityId = 0;

    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<>();

    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityColonyBuilding.class, Constants.MOD_ID + ".ColonyBuilding");
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
     * @param name  player UUID + Properties name, HashMap key
     * @return      NBTTagCompound PlayerProperties NBT compound
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
        // Half as much tracking range and same update frequency as a player
        // See EntityTracker.addEntityToTracker for more default values
        EntityRegistry.registerModEntity(EntityCitizen.class, "Citizen", getNextEntityId(), MineColonies.instance, 256, 2, true);
        EntityRegistry.registerModEntity(EntityFishHook.class, "Fishhook", getNextEntityId(), MineColonies.instance, 250, 5, true);


    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
    }

    @Override
    public void registerEntityRendering(){}

    @Override
    public void registerTileEntityRendering(){}

    @Override
    public void registerKeyBindings(){}

    @Override
    public void showCitizenWindow(CitizenData.View citizen) {}

    @Override
    public void openBuildToolWindow(BlockPos pos) {}

    //Schematica
    @Override
    public void setActiveSchematic(Schematic schematic)
    {
    }

    @Override
    public Schematic getActiveSchematic()
    {
        return null;
    }

    /**
     * Used for entity IDs, starts at 0 & increments for each call
     */
    private int getNextEntityId()
    {
        return nextEntityId++;
    }

	@Override
	public void registerRenderer() { }
}
