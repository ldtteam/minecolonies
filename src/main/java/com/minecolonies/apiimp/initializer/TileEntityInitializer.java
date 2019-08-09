package com.minecolonies.apiimp.initializer;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
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
        MinecoloniesTileEntities.SCARECROW = TileEntityType.Builder.create(ScarecrowTileEntity::new,
          ModBlocks.blockScarecrow).build(null).setRegistryName(Constants.MOD_ID, "scarecrow");

        MinecoloniesTileEntities.BARREL = TileEntityType.Builder.create(TileEntityBarrel::new,
          ModBlocks.blockBarrel).build(null).setRegistryName(Constants.MOD_ID, "barrel");

        MinecoloniesTileEntities.BUILDING = TileEntityType.Builder.create(TileEntityColonyBuilding::new,
          ModBlocks.getHuts()).build(null).setRegistryName(Constants.MOD_ID, "colonybuilding");

        MinecoloniesTileEntities.DECO_CONTROLLER = TileEntityType.Builder.create(TileEntityDecorationController::new,
          ModBlocks.blockDecorationPlaceholder).build(null).setRegistryName(Constants.MOD_ID, "decorationcontroller");

        MinecoloniesTileEntities.RACK = TileEntityType.Builder.create(TileEntityRack::new,
          ModBlocks.blockRack).build(null).setRegistryName(Constants.MOD_ID, "rack");

        MinecoloniesTileEntities.WAREHOUSE = TileEntityType.Builder.create(TileEntityWareHouse::new,
          ModBlocks.blockHutWareHouse).build(null).setRegistryName(Constants.MOD_ID, "warehouse");

        event.getRegistry().registerAll(MinecoloniesTileEntities.SCARECROW,
          MinecoloniesTileEntities.BARREL,
          MinecoloniesTileEntities.BUILDING,
          MinecoloniesTileEntities.DECO_CONTROLLER,
          MinecoloniesTileEntities.INFO_POSTER,
          MinecoloniesTileEntities.RACK,
          MinecoloniesTileEntities.WAREHOUSE);
    }
}
