package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.blocks.ModBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MinecoloniesTileEntities
{
    @ObjectHolder("scarecrow")
    public static TileEntityType<?> SCARECROW;

    @ObjectHolder("barrel")
    public static TileEntityType<?> BARREL;

    @ObjectHolder("colonybuilding")
    public static TileEntityType<?> BUILDING;

    @ObjectHolder("decorationcontroller")
    public static TileEntityType<?> DECO_CONTROLLER;

    @ObjectHolder("infoposter")
    public static TileEntityType<?> INFO_POSTER;

    @ObjectHolder("rack")
    public static TileEntityType<?> RACK;

    @ObjectHolder("warehouse")
    public static TileEntityType<?> WAREHOUSE;

    @SubscribeEvent
    public static void registerTileEntity(final RegistryEvent.Register<TileEntityType<?>> event)
    {
        SCARECROW = TileEntityType.Builder.create(ScarecrowTileEntity::new,
          ModBlocks.blockScarecrow).build(null).setRegistryName(Constants.MOD_ID, "scarecrow");

        BARREL = TileEntityType.Builder.create(TileEntityBarrel::new,
          ModBlocks.blockBarrel).build(null).setRegistryName(Constants.MOD_ID, "barrel");

        BUILDING = TileEntityType.Builder.create(TileEntityColonyBuilding::new,
          ModBlocks.getHuts()).build(null).setRegistryName(Constants.MOD_ID, "colonybuilding");

        DECO_CONTROLLER = TileEntityType.Builder.create(TileEntityDecorationController::new,
          ModBlocks.blockDecorationPlaceholder).build(null).setRegistryName(Constants.MOD_ID, "decorationcontroller");

        INFO_POSTER = TileEntityType.Builder.create(TileEntityInfoPoster::new,
          ModBlocks.blockInfoPoster).build(null).setRegistryName(Constants.MOD_ID, "infoposter");

        RACK = TileEntityType.Builder.create(TileEntityRack::new,
          ModBlocks.blockRack).build(null).setRegistryName(Constants.MOD_ID, "rack");

        WAREHOUSE = TileEntityType.Builder.create(TileEntityWareHouse::new,
          ModBlocks.blockHutWareHouse).build(null).setRegistryName(Constants.MOD_ID, "warehouse");

        event.getRegistry().registerAll(SCARECROW, BARREL, BUILDING, DECO_CONTROLLER, INFO_POSTER, RACK, WAREHOUSE);
    }
}
