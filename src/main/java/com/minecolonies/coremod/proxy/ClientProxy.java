package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.BlockPaperwall;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.client.gui.WindowCitizen;
import com.minecolonies.coremod.client.render.*;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererBarbarian;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererChiefBarbarian;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.EntityChiefBarbarian;
import com.minecolonies.coremod.event.ClientEventHandler;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import com.minecolonies.structures.event.RenderEventHandler;
import com.minecolonies.structures.helpers.Settings;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public void registerEvents()
    {
        super.registerEvents();

        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @Override
    public void registerEntityRendering()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityCitizen.class, RenderBipedCitizen::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFishHook.class, RenderFishHook::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBarbarian.class, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityArcherBarbarian.class, RendererBarbarian::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityChiefBarbarian.class, RendererChiefBarbarian::new);
    }

    @Override
    public void registerTileEntityRendering()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityColonyBuilding.class, new EmptyTileEntitySpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(ScarecrowTileEntity.class, new TileEntityScarecrowRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInfoPoster.class, new TileEntityInfoPosterRenderer());
    }

    @Override
    public void showCitizenWindow(final CitizenDataView citizen)
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

        @Nullable final WindowBuildTool window = new WindowBuildTool(pos);
        window.open();
    }

    public void registerBlockHut(final Block block)
    {
        final ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        itemModelMesher.register(Item.getItemFromBlock(block), 0 , new ModelResourceLocation(block.getRegistryName(), INVENTORY));
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowBuildTool window = new WindowBuildTool(pos, structureName, rotation, mode);
        window.open();
    }

    /**
     * Registers one model for a block, with meta 0.
     * @param block the block to register the model for.
     */
    private static void registerBlockModel(final Block block)
    {
        registerBlockModel(block, 0, block.getRegistryName().toString());
    }

    /**
     * Registers one model for a block.
     * @param block the block to register the model for.
     * @param meta the meta of the block to register the model for.
     * @param resourceName the name to use for the block
     */
    private static void registerBlockModel(final Block block, final int meta, final String resourceName)
    {
        final Item item = Item.getItemFromBlock(block);
        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(item, meta,
              new ModelResourceLocation(resourceName, INVENTORY));
        }
    }

    /**
     * Registers one model for an item, with meta 0.
     * @param item the item to register the model for.
     */
    private static void registerItemModel(final Item item)
    {
        registerItemModel(item, 0, item.getRegistryName().toString());
    }

    /**
     * Registers one model for a item.
     * @param item the item to register the model for.
     * @param meta the meta of the item to register the model for.
     * @param resourceName the name to use for the block
     */
    private static void registerItemModel(final Item item, final int meta, final String resourceName)
    {
        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(item, meta,
              new ModelResourceLocation(resourceName, INVENTORY));
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
        //Colony Buildings
        registerBlockModel(ModBlocks.blockHutBaker);
        registerBlockModel(ModBlocks.blockHutBlacksmith);
        registerBlockModel(ModBlocks.blockHutBuilder);
        registerBlockModel(ModBlocks.blockHutCitizen);
        registerBlockModel(ModBlocks.blockHutFarmer);
        registerBlockModel(ModBlocks.blockHutFisherman);
        registerBlockModel(ModBlocks.blockHutLumberjack);
        registerBlockModel(ModBlocks.blockHutMiner);
        registerBlockModel(ModBlocks.blockHutStonemason);
        registerBlockModel(ModBlocks.blockHutTownHall);
        registerBlockModel(ModBlocks.blockHutWareHouse);
        registerBlockModel(ModBlocks.blockHutDeliveryman);
        registerBlockModel(ModBlocks.blockSubstitution);
        registerBlockModel(ModBlocks.blockHutField);
        registerBlockModel(ModBlocks.blockHutGuardTower);
        registerBlockModel(ModBlocks.blockHutBarracks);
        registerBlockModel(ModBlocks.blockHutBarracksTower);

        //Standard blocks
        registerBlockModel(ModBlocks.blockSolidSubstitution);
        registerBlockModel(ModBlocks.blockTimberFrame);
        registerBlockModel(ModBlocks.blockConstructionTape);
        registerBlockModel(ModBlocks.blockConstructionTapeCorner);
        registerBlockModel(ModBlocks.blockRack);
        registerBlockModel(ModBlocks.blockWayPoint);

        ModelLoader.setCustomStateMapper(ModBlocks.blockPaperWall, new StateMap.Builder().withName(BlockPaperwall.VARIANT).withSuffix("_blockPaperwall").build());

        for(final BlockPaperwall.EnumType type: BlockPaperwall.EnumType.values())
        {
            registerBlockModel(ModBlocks.blockPaperWall, type.getMetadata(), ModBlocks.blockPaperWall.getRegistryName()  + "_" + type.getName());
        }

        //Items
        registerItemModel(ModItems.buildTool);
        registerItemModel(ModItems.caliper);
        registerItemModel(ModItems.scanTool);
        registerItemModel(ModItems.scepterGuard);
        registerItemModel(ModItems.supplyChest);
        registerItemModel(ModItems.supplyCamp);
        registerItemModel(ModItems.permTool);
        registerItemModel(ModItems.ancientTome);
        registerItemModel(ModItems.chiefSword);

        // Achievement proxy Items
        registerItemModel(ModItems.itemAchievementProxySettlement);
        registerItemModel(ModItems.itemAchievementProxyTown);
        registerItemModel(ModItems.itemAchievementProxyCity);
        registerItemModel(ModItems.itemAchievementProxyMetropolis);
    }

    @Override
    public File getSchematicsFolder()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            if (ColonyManager.getServerUUID() != null)
            {
                return new File(Minecraft.getMinecraft().mcDataDir, Constants.MOD_ID + "/" + ColonyManager.getServerUUID());
            }
            else
            {
                Log.getLogger().error("ColonyManager.getServerUUID() => null this should not happen");
                return null;
            }
        }

        // if the world schematics folder exists we use it
        // otherwise we use the minecraft folder  /minecolonies/schematics if on the physical client on the logical server
        final File worldSchematicFolder = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSaveHandler().getWorldDirectory()
                                                     + "/" + Constants.MOD_ID + '/' + Structures.SCHEMATICS_PREFIX);

        if (!worldSchematicFolder.exists())
        {
            return new File(Minecraft.getMinecraft().mcDataDir, Constants.MOD_ID);
        }

        return worldSchematicFolder.getParentFile();
    }
}
