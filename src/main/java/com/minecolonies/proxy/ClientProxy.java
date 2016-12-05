package com.minecolonies.proxy;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.client.gui.WindowBuildTool;
import com.minecolonies.client.gui.WindowCitizen;
import com.minecolonies.client.render.EmptyTileEntitySpecialRenderer;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.client.render.RenderFishHook;
import com.minecolonies.client.render.TileEntityScarecrowRenderer;
import com.minecolonies.colony.CitizenDataView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.event.ClientEventHandler;
import com.minecolonies.items.ModItems;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.structures.event.RenderEventHandler;
import com.structures.helpers.Settings;
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

public class ClientProxy extends CommonProxy
{
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

        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutBaker), 0, new ModelResourceLocation(ModBlocks.blockHutBaker.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutBlacksmith), 0, new ModelResourceLocation(ModBlocks.blockHutBlacksmith.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutBuilder), 0, new ModelResourceLocation(ModBlocks.blockHutBuilder.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutCitizen), 0, new ModelResourceLocation(ModBlocks.blockHutCitizen.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutFarmer), 0, new ModelResourceLocation(ModBlocks.blockHutFarmer.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutFisherman), 0, new ModelResourceLocation(ModBlocks.blockHutFisherman.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutLumberjack), 0, new ModelResourceLocation(ModBlocks.blockHutLumberjack.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutMiner), 0, new ModelResourceLocation(ModBlocks.blockHutMiner.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutStonemason), 0, new ModelResourceLocation(ModBlocks.blockHutStonemason.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutTownHall), 0, new ModelResourceLocation(ModBlocks.blockHutTownHall.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutWarehouse), 0, new ModelResourceLocation(ModBlocks.blockHutWarehouse.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockSubstitution), 0, new ModelResourceLocation(ModBlocks.blockSubstitution.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutField), 0, new ModelResourceLocation(ModBlocks.blockHutField.getRegistryName(), "inventory"));
        itemModelMesher.register(Item.getItemFromBlock(ModBlocks.blockHutGuardTower), 0, new ModelResourceLocation(ModBlocks.blockHutGuardTower.getRegistryName(), "inventory"));

        itemModelMesher.register(ModItems.buildTool, 0, new ModelResourceLocation(ModItems.buildTool.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.caliper, 0, new ModelResourceLocation(ModItems.caliper.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.scanTool, 0, new ModelResourceLocation(ModItems.scanTool.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.scepterGuard, 0, new ModelResourceLocation(ModItems.scepterGuard.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.supplyChest, 0, new ModelResourceLocation(ModItems.supplyChest.getRegistryName(), "inventory"));


        // Achievement proxy Items
        itemModelMesher.register(ModItems.itemAchievementProxySettlement, 0, new ModelResourceLocation(ModItems.itemAchievementProxySettlement.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.itemAchievementProxyTown, 0, new ModelResourceLocation(ModItems.itemAchievementProxyTown.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.itemAchievementProxyCity, 0, new ModelResourceLocation(ModItems.itemAchievementProxyCity.getRegistryName(), "inventory"));
        itemModelMesher.register(ModItems.itemAchievementProxyMetropolis, 0, new ModelResourceLocation(ModItems.itemAchievementProxyMetropolis.getRegistryName(), "inventory"));
    }
}
