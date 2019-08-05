package com.minecolonies.coremod.proxy;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.mobs.barbarians.IChiefBarbarianEntity;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.MinecoloniesAPIImpl;
import com.minecolonies.apiimp.initializer.ModBlocksInitializer;
import com.minecolonies.apiimp.initializer.ModBuildingsInitializer;
import com.minecolonies.apiimp.initializer.ModItemsInitializer;
import com.minecolonies.apiimp.initializer.ModJobsInitializer;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import com.minecolonies.coremod.util.TownHallRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.ColonyConstants.*;

/**
 * CommonProxy of the minecolonies mod (Server and Client).
 */
@Mod.EventBusSubscriber
public abstract class CommonProxy implements IProxy
{
    /**
     * Spawn egg colors.
     */
    private static final int PRIMARY_COLOR_BARBARIAN   = 5;
    private static final int SECONDARY_COLOR_BARBARIAN = 700;
    private static final int PRIMARY_COLOR_PIRATE   = 7;
    private static final int SECONDARY_COLOR_PIRATE = 600;

    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn.
     */
    private static final Map<String, CompoundNBT> playerPropertiesData = new HashMap<>();
    private              int                         nextEntityId         = 0;

    /**
     * Adds an entity's custom data to the map for temporary storage.
     *
     * @param name     player UUID + Properties name, HashMap key.
     * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties
     *                 data only.
     */
    public static void storeEntityData(final String name, final CompoundNBT compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name
     * or null if none exists.
     *
     * @param name player UUID + Properties name, HashMap key.
     * @return CompoundNBT PlayerProperties NBT compound.
     */
    public static CompoundNBT getEntityData(final String name)
    {
        return playerPropertiesData.remove(name);
    }

    /**
     * Called when registering recipes.
     * @param event the registery event for recipes.
     */
    @SubscribeEvent
    public static void registerRecipes(@NotNull final RegistryEvent.Register<IRecipe> event)
    {
        event.getRegistry().register(new TownHallRecipe());
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
        ModItemsInitializer.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerBuildingTypes(@NotNull final RegistryEvent.Register<BuildingEntry> event)
    {
        ModBuildingsInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerJobTypes(final RegistryEvent.Register<JobEntry> event)
    {
        ModJobsInitializer.init(event);
    }

    @SubscribeEvent
    public static void onRegistryNewRegistry(final RegistryEvent.NewRegistry event)
    {
        MinecoloniesAPIImpl.getInstance().onRegistryNewRegistry(event);
    }

    @Override
    public boolean isClient()
    {
        return false;
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
        final ResourceLocation locationCitizen = new ResourceLocation(Constants.MOD_ID, "Citizen");
        final ResourceLocation locationFishHook = new ResourceLocation(Constants.MOD_ID, "Fishhook");

        // Half as much tracking range and same update frequency as a player
        // See EntityTracker.addEntityToTracker for more default values
        EntityRegistry.registerModEntity(locationCitizen,
          EntityCitizen.class,
          "Citizen",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(locationFishHook,
          EntityFishHook.class,
          "Fishhook",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY_FISHHOOK,
          true);
        EntityRegistry.registerModEntity(BARBARIAN,
          EntityBarbarian.class,
          "Barbarian",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(MERCENARY,
          EntityMercenary.class,
          "Mercenary",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(ARCHER,
          EntityArcherBarbarian.class,
          "ArcherBarbarian",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(CHIEF,
          EntityChiefBarbarian.class,
          "ChiefBarbarian",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);

        EntityRegistry.registerModEntity(PIRATE,
          EntityPirate.class,
          "Pirate",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(PIRATE_ARCHER,
          EntityArcherPirate.class,
          "ArcherPirate",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);
        EntityRegistry.registerModEntity(PIRATE_CHIEF,
          EntityCaptainPirate.class,
          "ChiefPirate",
          getNextEntityId(),
          MineColonies.instance,
          Constants.ENTITY_TRACKING_RANGE,
          Constants.ENTITY_UPDATE_FREQUENCY,
          true);

        //Register Barbarian spawn eggs
        EntityRegistry.registerEgg(BARBARIAN, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN);
        EntityRegistry.registerEgg(ARCHER, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN);
        EntityRegistry.registerEgg(CHIEF, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN);

        //Register Pirate spawn eggs
        EntityRegistry.registerEgg(PIRATE, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE);
        EntityRegistry.registerEgg(PIRATE_ARCHER, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE);
        EntityRegistry.registerEgg(PIRATE_CHIEF, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE);
    }

    @Override
    public void registerEntityRendering()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void registerTileEntityRendering()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void showCitizenWindow(final ICitizenDataView citizen)
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
    public void openSuggestionWindow(@NotNull BlockPos pos, @NotNull BlockState state, @NotNull final ItemStack stack)
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
    public void openClipBoardWindow(final int colonyId)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openResourceScrollWindow(final int colonyId, final BlockPos pos)
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

    /**
     * Used for entity IDs, starts at 0 & increments for each call.
     */
    private int getNextEntityId()
    {
        return nextEntityId++;
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final PlayerEntity player)
    {
        return ((ServerPlayerEntity) player).getRecipeBook();
    }

    @Override
    public void openDecorationControllerWindow(@NotNull final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }
}
