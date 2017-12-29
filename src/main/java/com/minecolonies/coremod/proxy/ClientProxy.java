package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.BlockPaperwall;
import com.minecolonies.coremod.blocks.BlockShingle;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.PaperwallType;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.client.gui.WindowCitizen;
import com.minecolonies.coremod.client.gui.WindowClipBoard;
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
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
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
     * Event handler for forge ModelRegistryEvent event.
     *
     * @param event the forge pre ModelRegistryEvent event.
     */
    @SubscribeEvent
    public static void registerModels(@NotNull final ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutBaker), 0,
          new ModelResourceLocation(ModBlocks.blockHutBaker.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutBlacksmith), 0,
          new ModelResourceLocation(ModBlocks.blockHutBlacksmith.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutBuilder), 0,
          new ModelResourceLocation(ModBlocks.blockHutBuilder.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutCitizen), 0,
          new ModelResourceLocation(ModBlocks.blockHutCitizen.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutFarmer), 0,
          new ModelResourceLocation(ModBlocks.blockHutFarmer.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutFisherman), 0,
          new ModelResourceLocation(ModBlocks.blockHutFisherman.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutLumberjack), 0,
          new ModelResourceLocation(ModBlocks.blockHutLumberjack.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutMiner), 0,
          new ModelResourceLocation(ModBlocks.blockHutMiner.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutStonemason), 0,
          new ModelResourceLocation(ModBlocks.blockHutStonemason.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutTownHall), 0,
          new ModelResourceLocation(ModBlocks.blockHutTownHall.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutWareHouse), 0,
          new ModelResourceLocation(ModBlocks.blockHutWareHouse.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutDeliveryman), 0,
          new ModelResourceLocation(ModBlocks.blockHutDeliveryman.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockSubstitution), 0,
          new ModelResourceLocation(ModBlocks.blockSubstitution.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutField), 0,
          new ModelResourceLocation(ModBlocks.blockHutField.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutGuardTower), 0,
          new ModelResourceLocation(ModBlocks.blockHutGuardTower.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutBarracks), 0,
          new ModelResourceLocation(ModBlocks.blockHutBarracks.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutBarracksTower), 0,
                new ModelResourceLocation(ModBlocks.blockHutBarracksTower.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockHutCook), 0,
                new ModelResourceLocation(ModBlocks.blockHutCook.getRegistryName(), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockSolidSubstitution), 0,
          new ModelResourceLocation(ModBlocks.blockSolidSubstitution.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockTimberFrame), 0,
          new ModelResourceLocation(ModBlocks.blockTimberFrame.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockConstructionTape), 0,
          new ModelResourceLocation(ModBlocks.blockConstructionTape.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockConstructionTapeCorner), 0,
          new ModelResourceLocation(ModBlocks.blockConstructionTapeCorner.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockRack), 0,
          new ModelResourceLocation(ModBlocks.blockRack.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockWayPoint), 0,
          new ModelResourceLocation(ModBlocks.blockWayPoint.getRegistryName(), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(ModItems.clipboard, 0,
          new ModelResourceLocation(ModItems.clipboard.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.buildTool, 0,
          new ModelResourceLocation(ModItems.buildTool.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.caliper, 0,
          new ModelResourceLocation(ModItems.caliper.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.scanTool, 0,
          new ModelResourceLocation(ModItems.scanTool.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.scepterGuard, 0,
          new ModelResourceLocation(ModItems.scepterGuard.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.supplyChest, 0,
          new ModelResourceLocation(ModItems.supplyChest.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.supplyCamp, 0,
          new ModelResourceLocation(ModItems.supplyCamp.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.permTool, 0,
          new ModelResourceLocation(ModItems.permTool.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.ancientTome, 0,
          new ModelResourceLocation(ModItems.ancientTome.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.chiefSword, 0,
          new ModelResourceLocation(ModItems.chiefSword.getRegistryName(), INVENTORY));

        // Achievement proxy Items
        ModelLoader.setCustomModelResourceLocation(ModItems.itemAchievementProxySettlement, 0,
          new ModelResourceLocation(ModItems.itemAchievementProxySettlement.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.itemAchievementProxyTown, 0,
          new ModelResourceLocation(ModItems.itemAchievementProxyTown.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.itemAchievementProxyCity, 0,
          new ModelResourceLocation(ModItems.itemAchievementProxyCity.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(ModItems.itemAchievementProxyMetropolis, 0,
          new ModelResourceLocation(ModItems.itemAchievementProxyMetropolis.getRegistryName(), INVENTORY));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleSlab), 0,
                new ModelResourceLocation(ModBlocks.blockShingleSlab.getRegistryName(), INVENTORY));

        ModelLoader.setCustomStateMapper(ModBlocks.blockPaperWall, new StateMap.Builder().withName(BlockPaperwall.VARIANT).withSuffix("_blockPaperwall").build());

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleOak), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.OAK.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleBirch), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.BIRCH.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleSpruce), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.SPRUCE.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleJungle), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.JUNGLE.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleDarkOak), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.DARK_OAK.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleAcacia), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.ACACIA.getName()), INVENTORY));

        for (final PaperwallType type : PaperwallType.values())
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockPaperWall), type.getMetadata(),
              new ModelResourceLocation(ModBlocks.blockPaperWall.getRegistryName() + "_" + type.getName(), INVENTORY));
        }
    }

    @Override
    public void openClipBoardWindow(final int colonyId)
    {
        @Nullable final WindowClipBoard window = new WindowClipBoard(colonyId);
        window.open();
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

    @Nullable
    @Override
    public World getWorldFromMessage(@NotNull final MessageContext context)
    {
        return context.getClientHandler().clientWorldController;
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
}
