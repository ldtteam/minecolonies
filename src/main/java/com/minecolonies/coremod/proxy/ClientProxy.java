package com.minecolonies.coremod.proxy;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.*;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
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
        createCustomModel(ModBlocks.blockHutCitizen);
        createCustomModel(ModBlocks.blockHutFarmer);
        createCustomModel(ModBlocks.blockHutFisherman);
        createCustomModel(ModBlocks.blockHutLumberjack);
        createCustomModel(ModBlocks.blockHutMiner);
        createCustomModel(ModBlocks.blockHutStonemason);
        createCustomModel(ModBlocks.blockHutTownHall);
        createCustomModel(ModBlocks.blockHutWareHouse);
        createCustomModel(ModBlocks.blockHutDeliveryman);
        createCustomModel(ModBlocks.blockSubstitution);
        createCustomModel(ModBlocks.blockHutField);
        createCustomModel(ModBlocks.blockHutGuardTower);
        createCustomModel(ModBlocks.blockHutBarracks);
        createCustomModel(ModBlocks.blockHutBarracksTower);
        createCustomModel(ModBlocks.blockHutCook);
        createCustomModel(ModBlocks.blockHutShepherd);
        createCustomModel(ModBlocks.blockHutCowboy);
        createCustomModel(ModBlocks.blockHutSwineHerder);
        createCustomModel(ModBlocks.blockHutChickenHerder);
        createCustomModel(ModBlocks.blockHutSmeltery);

        createCustomModel(ModBlocks.blockSolidSubstitution);
        createCustomModel(ModBlocks.blockConstructionTape);
        createCustomModel(ModBlocks.blockRack);
        createCustomModel(ModBlocks.blockWayPoint);

        createCustomModel(ModItems.clipboard);
        createCustomModel(ModItems.buildTool);
        createCustomModel(ModItems.caliper);
        createCustomModel(ModItems.scanTool);
        createCustomModel(ModItems.scepterGuard);
        createCustomModel(ModItems.supplyChest);
        createCustomModel(ModItems.supplyCamp);
        createCustomModel(ModItems.permTool);
        createCustomModel(ModItems.ancientTome);
        createCustomModel(ModItems.chiefSword);

        // Achievement proxy Items
        createCustomModel(ModItems.itemAchievementProxySettlement);
        createCustomModel(ModItems.itemAchievementProxyTown);
        createCustomModel(ModItems.itemAchievementProxyCity);
        createCustomModel(ModItems.itemAchievementProxyMetropolis);
        createCustomModel(ModBlocks.blockShingleSlab);

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

        for (final BlockTimberFrame frame : ModBlocks.timberFrames)
        {
            for (final TimberFrameType type : TimberFrameType.values())
            {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(frame), type.getMetadata(),
                        new ModelResourceLocation(frame.getRegistryName() + "_" + type.getName(), INVENTORY));
            }
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
        return Minecraft.getMinecraft().world;
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
