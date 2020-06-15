package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TileEntityInitializer
{
    @SubscribeEvent
    public static void registerTileEntity(final RegistryEvent.Register<TileEntityType<?>> event)
    {
        MinecoloniesTileEntities.SCARECROW = TileEntityType.Builder.create(ScarecrowTileEntity::new, ModBlocks.blockScarecrow).build(null);
        MinecoloniesTileEntities.SCARECROW.setRegistryName(Constants.MOD_ID, "scarecrow");

        MinecoloniesTileEntities.BARREL = TileEntityType.Builder.create(TileEntityBarrel::new, ModBlocks.blockBarrel).build(null);
        MinecoloniesTileEntities.BARREL.setRegistryName(Constants.MOD_ID, "barrel");

        MinecoloniesTileEntities.BUILDING = TileEntityType.Builder.create(TileEntityColonyBuilding::new, ModBlocks.getHuts()).build(null);
        MinecoloniesTileEntities.BUILDING.setRegistryName(Constants.MOD_ID, "colonybuilding");

        MinecoloniesTileEntities.DECO_CONTROLLER = TileEntityType.Builder
            .create(TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder)
            .build(null);
        MinecoloniesTileEntities.DECO_CONTROLLER.setRegistryName(Constants.MOD_ID, "decorationcontroller");

        MinecoloniesTileEntities.RACK = TileEntityType.Builder.create(TileEntityRack::new, ModBlocks.blockRack).build(null);
        MinecoloniesTileEntities.RACK.setRegistryName(Constants.MOD_ID, "rack");

        MinecoloniesTileEntities.WAREHOUSE = TileEntityType.Builder.create(TileEntityWareHouse::new, ModBlocks.blockHutWareHouse).build(null);
        MinecoloniesTileEntities.WAREHOUSE.setRegistryName(Constants.MOD_ID, "warehouse");

        MinecoloniesTileEntities.COMPOSTED_DIRT = TileEntityType.Builder.create(TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt)
            .build(null);
        MinecoloniesTileEntities.COMPOSTED_DIRT.setRegistryName(Constants.MOD_ID, "composteddirt");

        MinecoloniesTileEntities.ENCHANTER = TileEntityType.Builder.create(TileEntityEnchanter::new, ModBlocks.blockHutEnchanter).build(null);
        MinecoloniesTileEntities.ENCHANTER.setRegistryName(Constants.MOD_ID, "enchanter");

        MinecoloniesTileEntities.STASH = TileEntityType.Builder.create(TileEntityStash::new, ModBlocks.blockStash).build(null);
        MinecoloniesTileEntities.STASH.setRegistryName(Constants.MOD_ID, "stash");


        event.getRegistry().registerAll(
          MinecoloniesTileEntities.SCARECROW,
          MinecoloniesTileEntities.BARREL,
          MinecoloniesTileEntities.BUILDING,
          MinecoloniesTileEntities.DECO_CONTROLLER,
          MinecoloniesTileEntities.RACK,
          MinecoloniesTileEntities.WAREHOUSE,
          MinecoloniesTileEntities.COMPOSTED_DIRT,
          MinecoloniesTileEntities.ENCHANTER,
          MinecoloniesTileEntities.STASH);
    }
}
