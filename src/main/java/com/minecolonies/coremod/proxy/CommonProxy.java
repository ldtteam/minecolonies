package com.minecolonies.coremod.proxy;

import com.minecolonies.api.configuration.ConfigurationHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianSpawnUtils;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.event.FMLEventHandler;
import com.minecolonies.coremod.inventory.GuiHandler;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.sounds.ModSoundEvents;
import com.minecolonies.coremod.tileentities.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonProxy of the minecolonies mod (Server and Client).
 */
@Mod.EventBusSubscriber
public class CommonProxy implements IProxy
{
    /**
     * feel free to change the following if you want different colored spawn eggs
     */
    private static final int PRIMARY_COLOR   = 5;
    private static final int SECONDARY_COLOR = 700;

    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn.
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<>();
    private              int                         nextEntityId         = 0;

    /**
     * Adds an entity's custom data to the map for temporary storage.
     *
     * @param name     player UUID + Properties name, HashMap key.
     * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties
     *                 data only.
     */
    public static void storeEntityData(final String name, final NBTTagCompound compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name
     * or null if none exists.
     *
     * @param name player UUID + Properties name, HashMap key.
     * @return NBTTagCompound PlayerProperties NBT compound.
     */
    public static NBTTagCompound getEntityData(final String name)
    {
        return playerPropertiesData.remove(name);
    }

    /**
     * Called when registering blocks,
     * we have to register all our modblocks here.
     *
     * @param event the registery event for blocks.
     */
    @SubscribeEvent
    public static void registerBlocks(@NotNull final RegistryEvent.Register<Block> event)
    {
        ModBlocks.init(event.getRegistry());
    }

    /**
     * Called when registering items,
     * we have to register all our mod items here.
     *
     * @param event the registery event for items.
     */
    @SubscribeEvent
    public static void registerItems(@NotNull final RegistryEvent.Register<Item> event)
    {
        ModItems.init(event.getRegistry());
        ModBlocks.registerItemBlock(event.getRegistry());
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
        GameRegistry.registerTileEntity(TileEntityWareHouse.class, Constants.MOD_ID + ".WareHouse");
        GameRegistry.registerTileEntity(TileEntityRack.class, Constants.MOD_ID + ".rack");
        GameRegistry.registerTileEntity(TileEntityInfoPoster.class, Constants.MOD_ID + ".InfoPoster");

        NetworkRegistry.INSTANCE.registerGuiHandler(MineColonies.instance, new GuiHandler());
    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
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
        EntityRegistry.registerModEntity(EntityCitizen.class,
                "Citizen",
                getNextEntityId(),
                MineColonies.instance,
                Constants.ENTITY_TRACKING_RANGE,
                Constants.ENTITY_UPDATE_FREQUENCY,
                true);
        EntityRegistry.registerModEntity(EntityFishHook.class,
                "Fishhook",
                getNextEntityId(),
                MineColonies.instance,
                Constants.ENTITY_TRACKING_RANGE,
                Constants.ENTITY_UPDATE_FREQUENCY_FISHHOOK,
                true);
        EntityRegistry.registerModEntity(EntityBarbarian.class,
                "Barbarian",
                getNextEntityId(),
                MineColonies.instance,
                Constants.ENTITY_TRACKING_RANGE,
                Constants.ENTITY_UPDATE_FREQUENCY,
                true);
        EntityRegistry.registerModEntity(EntityArcherBarbarian.class,
                "ArcherBarbarian",
                getNextEntityId(),
                MineColonies.instance,
                Constants.ENTITY_TRACKING_RANGE,
                Constants.ENTITY_UPDATE_FREQUENCY,
                true);
        EntityRegistry.registerModEntity(EntityChiefBarbarian.class,
                "ChiefBarbarian",
                getNextEntityId(),
                MineColonies.instance,
                Constants.ENTITY_TRACKING_RANGE,
                Constants.ENTITY_UPDATE_FREQUENCY,
                true);


        //Register Barbarian loot tables.
        LootTableList.register(BarbarianSpawnUtils.BarbarianLootTable);
        LootTableList.register(BarbarianSpawnUtils.ArcherLootTable);
        LootTableList.register(BarbarianSpawnUtils.ChiefLootTable);

        //Register Barbarian spawn eggs
        EntityRegistry.registerEgg(EntityBarbarian.class, PRIMARY_COLOR, SECONDARY_COLOR);
        EntityRegistry.registerEgg(EntityArcherBarbarian.class, PRIMARY_COLOR, SECONDARY_COLOR);
        EntityRegistry.registerEgg(EntityChiefBarbarian.class, PRIMARY_COLOR, SECONDARY_COLOR);
    }

    @Override
    public void registerEntityRendering()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void registerSounds()
    {
        ModSoundEvents.registerSounds();
    }

    @Override
    public void registerTileEntityRendering()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void showCitizenWindow(final CitizenDataView citizen)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openClipBoardWindow(final int ColonyId)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void registerRenderer()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public File getSchematicsFolder()
    {
        return null;
    }

    @Nullable
    @Override
    public World getWorldFromMessage(@NotNull final MessageContext context)
    {
        return context.getServerHandler().playerEntity.getServerWorld();
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimension);
    }

    /**
     * Used for entity IDs, starts at 0 & increments for each call.
     */
    private int getNextEntityId()
    {
        return nextEntityId++;
    }
}
