package com.minecolonies.coremod.proxy;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityEnchanter;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.ClientMinecoloniesAPIImpl;
import com.minecolonies.coremod.client.gui.*;
import com.minecolonies.coremod.client.render.*;
import com.minecolonies.coremod.client.render.mobs.RenderMercenary;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererBarbarian;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererChiefBarbarian;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererArcherPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererChiefPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererPirate;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.EntityMercenary;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.entity.mobs.pirates.EntityArcherPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityCaptainPirate;
import com.minecolonies.coremod.entity.mobs.pirates.EntityPirate;
import com.minecolonies.coremod.event.ClientEventHandler;
import com.minecolonies.coremod.event.DebugRendererChunkBorder;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import com.minecolonies.coremod.tileentities.TileEntityScarecrow;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /**
     * Inventory description string.
     */
    private static final String INVENTORY = "inventory";

    public ClientProxy()
    {
        apiImpl = new ClientMinecoloniesAPIImpl();
    }

    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public void registerEvents()
    {
        super.registerEvents();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new DebugRendererChunkBorder());

    }

    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, RenderBipedCitizen::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFishHook.class, RenderFishHook::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBarbarian.class, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityArcherBarbarian.class, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityChiefBarbarian.class, RendererChiefBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPirate.class, RendererPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityArcherPirate.class, RendererArcherPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCaptainPirate.class, RendererChiefPirate::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityMercenary.class, RenderMercenary::new);
    }

    @Override
    public void registerTileEntityRendering()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityColonyBuilding.class, new EmptyTileEntitySpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScarecrow.class, new TileEntityScarecrowRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInfoPoster.class, new TileEntityInfoPosterRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnchanter.class, new TileEntityEnchanterRenderer());
    }

    @Override
    public void showCitizenWindow(final ICitizenDataView citizen)
    {
        @NotNull final WindowCitizen window = new WindowCitizen(citizen);
        window.open();
    }

    @Override
    public void openBuildToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowMinecoloniesBuildTool window = new WindowMinecoloniesBuildTool(pos);
        window.open();
    }

    @Override
    public void openDecorationControllerWindow(@Nullable final BlockPos pos)
    {
        if (pos == null)
        {
            return;
        }

        @Nullable final WindowDecorationController window = new WindowDecorationController(pos);
        window.open();
    }

    @Override
    public void openSuggestionWindow(@NotNull final BlockPos pos, @NotNull final IBlockState state, @NotNull final ItemStack stack)
    {
        new WindowSuggestBuildTool(pos, state, stack).open();
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowMinecoloniesBuildTool window = new WindowMinecoloniesBuildTool(pos, structureName, rotation, mode);
        window.open();
    }

    /**
     * Creates a custom model ResourceLocation for a block with metadata 0
     */
    private static void createCustomModel(final Block block)
    {
        final Item item = Item.getItemFromBlock(block);
        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(item, 0,
              new ModelResourceLocation(block.getRegistryName(), INVENTORY));
        }
    }

    /**
     * Creates a custom model ResourceLocation for an item with metadata 0
     */
    private static void createCustomModel(final Item item)
    {
        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(item, 0,
              new ModelResourceLocation(item.getRegistryName(), INVENTORY));
        }
    }

    /**
     * Event handler for forge ModelRegistryEvent event.
     *
     * @param event the forge pre ModelRegistryEvent event.
     */
    @SubscribeEvent
    public static void registerModels(@NotNull final ModelRegistryEvent event)
    {
        createCustomModel(ModBlocks.blockHutBaker);
        createCustomModel(ModBlocks.blockHutBlacksmith);
        createCustomModel(ModBlocks.blockHutBuilder);
        createCustomModel(ModBlocks.blockHutHome);
        createCustomModel(ModBlocks.blockHutFarmer);
        createCustomModel(ModBlocks.blockHutFisherman);
        createCustomModel(ModBlocks.blockHutLumberjack);
        createCustomModel(ModBlocks.blockHutMiner);
        createCustomModel(ModBlocks.blockHutStonemason);
        createCustomModel(ModBlocks.blockHutTownHall);
        createCustomModel(ModBlocks.blockHutWareHouse);
        createCustomModel(ModBlocks.blockHutDeliveryman);
        createCustomModel(ModBlocks.blockBarracksTowerSubstitution);
        createCustomModel(ModBlocks.blockScarecrow);
        createCustomModel(ModBlocks.blockHutGuardTower);
        createCustomModel(ModBlocks.blockHutBarracks);
        createCustomModel(ModBlocks.blockHutBarracksTower);
        createCustomModel(ModBlocks.blockHutCook);
        createCustomModel(ModBlocks.blockHutShepherd);
        createCustomModel(ModBlocks.blockHutCowboy);
        createCustomModel(ModBlocks.blockHutSwineHerder);
        createCustomModel(ModBlocks.blockHutChickenHerder);
        createCustomModel(ModBlocks.blockHutSmeltery);
        createCustomModel(ModBlocks.blockHutComposter);
        createCustomModel(ModBlocks.blockHutLibrary);
        createCustomModel(ModBlocks.blockHutArchery);
        createCustomModel(ModBlocks.blockHutCombatAcademy);
        createCustomModel(ModBlocks.blockHutSawmill);
        createCustomModel(ModBlocks.blockHutStoneSmeltery);
        createCustomModel(ModBlocks.blockHutCrusher);
        createCustomModel(ModBlocks.blockHutSifter);
        createCustomModel(ModBlocks.blockHutFlorist);
        createCustomModel(ModBlocks.blockHutEnchanter);

        createCustomModel(ModBlocks.blockConstructionTape);
        createCustomModel(ModBlocks.blockRack);
        createCustomModel(ModBlocks.blockWayPoint);
        createCustomModel(ModBlocks.blockPostBox);
        createCustomModel(ModBlocks.blockDecorationPlaceholder);

        createCustomModel(ModItems.clipboard);
        createCustomModel(ModItems.caliper);
        createCustomModel(ModItems.scepterGuard);
        createCustomModel(ModItems.scepterLumberjack);
        createCustomModel(ModItems.supplyChest);
        createCustomModel(ModItems.supplyCamp);
        createCustomModel(ModItems.permTool);
        createCustomModel(ModItems.ancientTome);
        createCustomModel(ModItems.chiefSword);
        createCustomModel(ModItems.scimitar);

        createCustomModel(ModItems.pirateBoots_1);
        createCustomModel(ModItems.pirateChest_1);
        createCustomModel(ModItems.pirateHelmet_1);
        createCustomModel(ModItems.pirateLegs_1);

        createCustomModel(ModItems.pirateBoots_2);
        createCustomModel(ModItems.pirateChest_2);
        createCustomModel(ModItems.pirateHelmet_2);
        createCustomModel(ModItems.pirateLegs_2);

        createCustomModel(ModItems.santaHat);

        // Achievement proxy Items
        createCustomModel(ModItems.itemAchievementProxySettlement);
        createCustomModel(ModItems.itemAchievementProxyTown);
        createCustomModel(ModItems.itemAchievementProxyCity);
        createCustomModel(ModItems.itemAchievementProxyMetropolis);
        createCustomModel(ModBlocks.blockBarrel);
        createCustomModel(ModItems.compost);
        createCustomModel(ModItems.resourceScroll);
        createCustomModel(ModBlocks.blockCompostedDirt);
    }

    @Override
    public void openClipBoardWindow(final int colonyId)
    {
        @Nullable final WindowClipBoard window = new WindowClipBoard(colonyId);
        window.open();
    }

    @Override
    public void openResourceScrollWindow(final int colonyId, final BlockPos buildingPos)
    {
        @Nullable final WindowResourceList window = new WindowResourceList(colonyId, buildingPos);
        window.open();
    }

    @Override
    public File getSchematicsFolder()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            if (IColonyManager.getInstance().getServerUUID() != null)
            {
                return new File(Minecraft.getMinecraft().gameDir, Constants.MOD_ID + "/" + IColonyManager.getInstance().getServerUUID());
            }
            else
            {
                Log.getLogger().error("ColonyManager.getServerUUID() => null this should not happen");
                return null;
            }
        }

        // if the world schematics folder exists we use it
        // otherwise we use the minecraft folder  /minecolonies/schematics if on the physical client on the logical server
        final File worldSchematicFolder = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory()
                                                     + "/" + Constants.MOD_ID + '/' + Structures.SCHEMATICS_PREFIX);

        if (!worldSchematicFolder.exists())
        {
            return new File(Minecraft.getMinecraft().gameDir, Constants.MOD_ID);
        }

        return worldSchematicFolder.getParentFile();
    }

    @Nullable
    @Override
    public World getWorldFromMessage(@NotNull final MessageContext context)
    {
        return context.getClientHandler().world;
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            return super.getWorld(dimension);
        }
        return Minecraft.getMinecraft().world;
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final EntityPlayer player)
    {
        if (player instanceof EntityPlayerSP)
        {
            return ((EntityPlayerSP) player).getRecipeBook();
        }

        return super.getRecipeBookFromPlayer(player);
    }
}
