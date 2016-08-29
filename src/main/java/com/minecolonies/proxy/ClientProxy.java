package com.minecolonies.proxy;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.client.gui.WindowBuildTool;
import com.minecolonies.client.gui.WindowCitizen;
import com.minecolonies.client.render.EmptyTileEntitySpecialRenderer;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.client.render.RenderFishHook;
import com.minecolonies.colony.CitizenDataView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.event.ClientEventHandler;
import com.minecolonies.items.ModItems;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.schematica.Settings;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.handler.client.TickHandler;
import com.schematica.handler.client.WorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

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

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        //Schematica
        MinecraftForge.EVENT_BUS.register(RenderSchematic.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TickHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new WorldHandler());
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
    }

    @Override
    public void showCitizenWindow(CitizenDataView citizen)
    {
        WindowCitizen window = new WindowCitizen(citizen);
        window.open();
    }

    @Override
    public void openBuildToolWindow(BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveSchematic() == null)
        {
            return;
        }

        WindowBuildTool window = new WindowBuildTool(pos);
        window.open();
    }

    @Override
    public void registerRenderer()
    {
        super.registerRenderer();

        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutBaker), 0, new ModelResourceLocation(ModBlocks.blockHutBaker.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutBlacksmith), 0, new ModelResourceLocation(ModBlocks.blockHutBlacksmith.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutBuilder), 0, new ModelResourceLocation(ModBlocks.blockHutBuilder.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutCitizen), 0, new ModelResourceLocation(ModBlocks.blockHutCitizen.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutFarmer), 0, new ModelResourceLocation(ModBlocks.blockHutFarmer.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutFisherman), 0, new ModelResourceLocation(ModBlocks.blockHutFisherman.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutLumberjack), 0, new ModelResourceLocation(ModBlocks.blockHutLumberjack.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutMiner), 0, new ModelResourceLocation(ModBlocks.blockHutMiner.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutStonemason), 0, new ModelResourceLocation(ModBlocks.blockHutStonemason.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutTownHall), 0, new ModelResourceLocation(ModBlocks.blockHutTownHall.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockHutWarehouse), 0, new ModelResourceLocation(ModBlocks.blockHutWarehouse.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(Item.getItemFromBlock(ModBlocks.blockSubstitution), 0, new ModelResourceLocation(ModBlocks.blockSubstitution.getRegistryName(), "inventory"));

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.buildTool, 0, new ModelResourceLocation(ModItems.buildTool.getRegistryName(), "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.caliper, 0, new ModelResourceLocation(ModItems.caliper.getRegistryName(), "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.scanTool, 0, new ModelResourceLocation(ModItems.scanTool.getRegistryName(), "inventory"));
        Minecraft.getMinecraft()
                .getRenderItem()
                .getItemModelMesher()
                .register(ModItems.supplyChest, 0, new ModelResourceLocation(ModItems.supplyChest.getRegistryName(), "inventory"));
    }
}
