package com.minecolonies.api.tileentities;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class MinecoloniesTileEntities
{
    @ObjectHolder("scarecrow")
    public static TileEntityType<AbstractScarescrowTileEntity> SCARECROW;

    @ObjectHolder("barrel")
    public static TileEntityType<?> BARREL;

    @ObjectHolder("colonybuilding")
    public static TileEntityType<AbstractTileEntityColonyBuilding> BUILDING;

    @ObjectHolder("decorationcontroller")
    public static TileEntityType<?> DECO_CONTROLLER;

    @ObjectHolder("rack")
    public static TileEntityType<?> RACK;

    @ObjectHolder("warehouse")
    public static TileEntityType<?> WAREHOUSE;

    @ObjectHolder("composteddirt")
    public static TileEntityType<?> COMPOSTED_DIRT;

    @ObjectHolder("enchanter")
    public static TileEntityType<? extends TileEntityColonyBuilding> ENCHANTER;
}
