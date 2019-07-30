package com.minecolonies.coremod.proxy;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ICitizenDataView;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.ai.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.ai.mobs.pirates.EntityPirate;
import com.minecolonies.coremod.inventory.GuiHandler;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.tileentities.*;
import com.minecolonies.coremod.util.TownHallRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
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
        ModItems.init(event.getRegistry());
        ModBlocks.registerBlockItem(event.getRegistry());
    }

    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void registerTileEntities()
    {

        NetworkRegistry.INSTANCE.registerGuiHandler(MineColonies.instance, new GuiHandler());
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

    @Nullable
    @Override
    public World getWorldFromMessage(@NotNull final MessageContext context)
    {
        return context.getServerHandler().player.getServerWorld();
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
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
