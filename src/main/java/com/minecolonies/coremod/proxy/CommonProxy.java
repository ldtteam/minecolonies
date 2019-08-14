package com.minecolonies.coremod.proxy;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.apiimp.MinecoloniesAPIImpl;
import com.minecolonies.apiimp.initializer.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonProxy of the minecolonies mod (Server and Client).
 */
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
    /*
    @SubscribeEvent
    public static void registerRecipes(@NotNull final RegistryEvent.Register<IRecipe> event)
    {
        //todo handle 1.14
        event.getRegistry().register(new TownHallRecipe());
    }*/

    @SubscribeEvent
    public static void registerGuardTypes(final RegistryEvent.Register<GuardType> event)
    {
        ModGuardTypesInitializer.init(event);
    }

    @SubscribeEvent
    public static void registerNewRegistries(final RegistryEvent.NewRegistry event)
    {
        MinecoloniesAPIImpl.getInstance().onRegistryNewRegistry(event);
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

    @Override
    public boolean isClient()
    {
        return false;
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
