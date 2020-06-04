package com.minecolonies.api.tileentities;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModTileEntities
{
    @ObjectHolder("scarecrow")
    public static TileEntityType<ScarecrowTileEntity> SCARECROW;

    @ObjectHolder("barrel")
    public static TileEntityType<TileEntityBarrel> BARREL;

    @ObjectHolder("colonybuilding")
    public static TileEntityType<TileEntityColonyBuilding> BUILDING;

    @ObjectHolder("decorationcontroller")
    public static TileEntityType<TileEntityDecorationController> DECO_CONTROLLER;

    @ObjectHolder("rack")
    public static TileEntityType<TileEntityRack> RACK;

    @ObjectHolder("warehouse")
    public static TileEntityType<TileEntityWareHouse> WAREHOUSE;

    @ObjectHolder("composteddirt")
    public static TileEntityType<TileEntityCompostedDirt> COMPOSTED_DIRT;

    @ObjectHolder("enchanter")
    public static TileEntityType<TileEntityEnchanter> ENCHANTER;

    @ObjectHolder("stash")
    public static TileEntityType<TileEntityStash> STASH;
}
