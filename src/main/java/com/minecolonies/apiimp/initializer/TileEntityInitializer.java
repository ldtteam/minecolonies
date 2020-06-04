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
        ModTileEntities.SCARECROW = TileEntityType.Builder.create(ScarecrowTileEntity::new, ModBlocks.blockScarecrow).build(null);
        ModTileEntities.SCARECROW.setRegistryName(Constants.MOD_ID, "scarecrow");

        ModTileEntities.BARREL = TileEntityType.Builder.create(TileEntityBarrel::new, ModBlocks.blockBarrel).build(null);
        ModTileEntities.BARREL.setRegistryName(Constants.MOD_ID, "barrel");

        ModTileEntities.BUILDING = TileEntityType.Builder.create(TileEntityColonyBuilding::new, ModBlocks.getHuts()).build(null);
        ModTileEntities.BUILDING.setRegistryName(Constants.MOD_ID, "colonybuilding");

        ModTileEntities.DECO_CONTROLLER = TileEntityType.Builder
            .create(TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder)
            .build(null);
        ModTileEntities.DECO_CONTROLLER.setRegistryName(Constants.MOD_ID, "decorationcontroller");

        ModTileEntities.RACK = TileEntityType.Builder.create(TileEntityRack::new, ModBlocks.blockRack).build(null);
        ModTileEntities.RACK.setRegistryName(Constants.MOD_ID, "rack");

        ModTileEntities.WAREHOUSE = TileEntityType.Builder.create(TileEntityWareHouse::new, ModBlocks.blockHutWareHouse).build(null);
        ModTileEntities.WAREHOUSE.setRegistryName(Constants.MOD_ID, "warehouse");

        ModTileEntities.COMPOSTED_DIRT = TileEntityType.Builder.create(TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt)
            .build(null);
        ModTileEntities.COMPOSTED_DIRT.setRegistryName(Constants.MOD_ID, "composteddirt");

        ModTileEntities.ENCHANTER = TileEntityType.Builder.create(TileEntityEnchanter::new, ModBlocks.blockHutEnchanter).build(null);
        ModTileEntities.ENCHANTER.setRegistryName(Constants.MOD_ID, "enchanter");

        ModTileEntities.STASH = TileEntityType.Builder.create(TileEntityStash::new, ModBlocks.blockStash).build(null);
        ModTileEntities.STASH.setRegistryName(Constants.MOD_ID, "stash");

        event.getRegistry()
            .registerAll(ModTileEntities.SCARECROW,
                ModTileEntities.BARREL,
                ModTileEntities.BUILDING,
                ModTileEntities.DECO_CONTROLLER,
                ModTileEntities.RACK,
                ModTileEntities.WAREHOUSE,
                ModTileEntities.COMPOSTED_DIRT,
                ModTileEntities.ENCHANTER,
                ModTileEntities.STASH);
    }
}
