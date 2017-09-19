package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
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
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Client side proxy.
 */
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
    public void registerRenderer()
    {
        super.registerRenderer();

        final ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        registerBlockHut(ModBlocks.blockHutBaker);
        registerBlockHut(ModBlocks.blockHutBlacksmith);
        registerBlockHut(ModBlocks.blockHutShepherd);
        registerBlockHut(ModBlocks.blockHutCowboy);
        registerBlockHut(ModBlocks.blockHutSwineHerder);
        registerBlockHut(ModBlocks.blockHutChickenHerder);
        registerBlockHut(ModBlocks.blockHutBuilder);
        registerBlockHut(ModBlocks.blockHutCitizen);
        registerBlockHut(ModBlocks.blockHutFarmer);
        registerBlockHut(ModBlocks.blockHutFisherman);
        registerBlockHut(ModBlocks.blockHutLumberjack);
        registerBlockHut(ModBlocks.blockHutMiner);
        registerBlockHut(ModBlocks.blockHutStonemason);
        registerBlockHut(ModBlocks.blockHutTownHall);
        registerBlockHut(ModBlocks.blockHutWareHouse);
        registerBlockHut(ModBlocks.blockHutDeliveryman);
        registerBlockHut(ModBlocks.blockSubstitution);
        registerBlockHut(ModBlocks.blockHutField);
        registerBlockHut(ModBlocks.blockHutGuardTower);
        registerBlockHut(ModBlocks.blockHutBarracks);
        registerBlockHut(ModBlocks.blockHutBarracksTower);

        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockSolidSubstitution), 0,
                new ModelResourceLocation(ModBlocks.blockSolidSubstitution.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockTimberFrame), 0,
                new ModelResourceLocation(ModBlocks.blockTimberFrame.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockConstructionTape), 0,
                new ModelResourceLocation(ModBlocks.blockConstructionTape.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockConstructionTapeCorner), 0,
                new ModelResourceLocation(ModBlocks.blockConstructionTapeCorner.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockRack), 0,
                new ModelResourceLocation(ModBlocks.blockRack.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockWayPoint), 0,
                new ModelResourceLocation(ModBlocks.blockWayPoint.getRegistryName(), INVENTORY));

        itemModelMesher.register(ModItems.buildTool, 0, new ModelResourceLocation(ModItems.buildTool.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.caliper, 0, new ModelResourceLocation(ModItems.caliper.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.scanTool, 0, new ModelResourceLocation(ModItems.scanTool.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.scepterGuard, 0, new ModelResourceLocation(ModItems.scepterGuard.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.supplyChest, 0, new ModelResourceLocation(ModItems.supplyChest.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.supplyCamp, 0, new ModelResourceLocation(ModItems.supplyCamp.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.permTool, 0, new ModelResourceLocation(ModItems.permTool.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.ancientTome, 0, new ModelResourceLocation(ModItems.ancientTome.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.chiefSword, 0, new ModelResourceLocation(ModItems.chiefSword.getRegistryName(), INVENTORY));


        // Achievement proxy Items
        itemModelMesher.register(ModItems.itemAchievementProxySettlement, 0, new ModelResourceLocation(ModItems.itemAchievementProxySettlement.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.itemAchievementProxyTown, 0, new ModelResourceLocation(ModItems.itemAchievementProxyTown.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.itemAchievementProxyCity, 0, new ModelResourceLocation(ModItems.itemAchievementProxyCity.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.itemAchievementProxyMetropolis, 0, new ModelResourceLocation(ModItems.itemAchievementProxyMetropolis.getRegistryName(), INVENTORY));
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
