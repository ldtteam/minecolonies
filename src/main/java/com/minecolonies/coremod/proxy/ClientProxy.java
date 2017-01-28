package com.minecolonies.coremod.proxy;

import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.client.gui.WindowCitizen;
import com.minecolonies.coremod.client.render.EmptyTileEntitySpecialRenderer;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.client.render.RenderFishHook;
import com.minecolonies.coremod.client.render.TileEntityScarecrowRenderer;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.event.ClientEventHandler;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.structures.event.RenderEventHandler;
import com.minecolonies.structures.helpers.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    }

    @Override
    public void registerTileEntityRendering()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityColonyBuilding.class, new EmptyTileEntitySpecialRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(ScarecrowTileEntity.class, new TileEntityScarecrowRenderer());
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
    public void registerRenderer()
    {
        super.registerRenderer();

        final ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutBaker), 0, new ModelResourceLocation(ModBlocks.blockHutBaker.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutBlacksmith), 0, new ModelResourceLocation(ModBlocks.blockHutBlacksmith.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutBuilder), 0, new ModelResourceLocation(ModBlocks.blockHutBuilder.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutCitizen), 0, new ModelResourceLocation(ModBlocks.blockHutCitizen.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutFarmer), 0, new ModelResourceLocation(ModBlocks.blockHutFarmer.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutFisherman), 0, new ModelResourceLocation(ModBlocks.blockHutFisherman.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutLumberjack), 0, new ModelResourceLocation(ModBlocks.blockHutLumberjack.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutMiner), 0, new ModelResourceLocation(ModBlocks.blockHutMiner.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutStonemason), 0, new ModelResourceLocation(ModBlocks.blockHutStonemason.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutTownHall), 0, new ModelResourceLocation(ModBlocks.blockHutTownHall.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutWareHouse), 0, new ModelResourceLocation(ModBlocks.blockHutWareHouse.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutDeliveryman), 0, new ModelResourceLocation(ModBlocks.blockHutDeliveryman.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockSubstitution), 0, new ModelResourceLocation(ModBlocks.blockSubstitution.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutField), 0, new ModelResourceLocation(ModBlocks.blockHutField.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutGuardTower), 0, new ModelResourceLocation(ModBlocks.blockHutGuardTower.getRegistryName(), INVENTORY));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockSolidSubstitution), 0,
                new ModelResourceLocation(ModBlocks.blockSolidSubstitution.getRegistryName(), INVENTORY));

        itemModelMesher.register(ModItems.buildTool, 0, new ModelResourceLocation(ModItems.buildTool.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.caliper, 0, new ModelResourceLocation(ModItems.caliper.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.scanTool, 0, new ModelResourceLocation(ModItems.scanTool.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.scepterGuard, 0, new ModelResourceLocation(ModItems.scepterGuard.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.supplyChest, 0, new ModelResourceLocation(ModItems.supplyChest.getRegistryName(), INVENTORY));

        // Achievement proxy Items
        itemModelMesher.register(ModItems.itemAchievementProxySettlement, 0, new ModelResourceLocation(ModItems.itemAchievementProxySettlement.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.itemAchievementProxyTown, 0, new ModelResourceLocation(ModItems.itemAchievementProxyTown.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.itemAchievementProxyCity, 0, new ModelResourceLocation(ModItems.itemAchievementProxyCity.getRegistryName(), INVENTORY));
        itemModelMesher.register(ModItems.itemAchievementProxyMetropolis, 0, new ModelResourceLocation(ModItems.itemAchievementProxyMetropolis.getRegistryName(), INVENTORY));
    }
}
