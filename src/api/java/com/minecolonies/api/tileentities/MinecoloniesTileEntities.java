package com.minecolonies.api.tileentities;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class MinecoloniesTileEntities
{
    @ObjectHolder("scarecrow")
    public static TileEntityType<? extends AbstractScarescrowTileEntity> SCARECROW;

    @ObjectHolder("barrel")
    public static TileEntityType<? extends AbstractTileEntityBarrel> BARREL;

    @ObjectHolder("colonybuilding")
    public static TileEntityType<? extends AbstractTileEntityColonyBuilding> BUILDING;

    @ObjectHolder("decorationcontroller")
    public static TileEntityType<? extends TileEntity> DECO_CONTROLLER;

    @ObjectHolder("rack")
    public static TileEntityType<TileEntityRack> RACK;

    @ObjectHolder("warehouse")
    public static TileEntityType<? extends AbstractTileEntityWareHouse> WAREHOUSE;

    @ObjectHolder("composteddirt")
    public static TileEntityType<? extends TileEntity> COMPOSTED_DIRT;

    @ObjectHolder("enchanter")
    public static TileEntityType<TileEntityEnchanter> ENCHANTER;

    @ObjectHolder("stash")
    public static TileEntityType<TileEntityStash> STASH;
}