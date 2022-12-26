package com.minecolonies.api.tileentities;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class MinecoloniesTileEntities
{
    @ObjectHolder("scarecrow")
    public static BlockEntityType<? extends AbstractTileEntityScarecrow> SCARECROW;

    @ObjectHolder("plantationfield")
    public static BlockEntityType<? extends AbstractTileEntityPlantationField> PLANTATION_FIELD;

    @ObjectHolder("barrel")
    public static BlockEntityType<? extends AbstractTileEntityBarrel> BARREL;

    @ObjectHolder("colonybuilding")
    public static BlockEntityType<? extends AbstractTileEntityColonyBuilding> BUILDING;

    @ObjectHolder("decorationcontroller")
    public static BlockEntityType<? extends BlockEntity> DECO_CONTROLLER;

    @ObjectHolder("rack")
    public static BlockEntityType<TileEntityRack> RACK;

    @ObjectHolder("grave")
    public static BlockEntityType<TileEntityGrave> GRAVE;

    @ObjectHolder("namedgrave")
    public static BlockEntityType<? extends TileEntityNamedGrave> NAMED_GRAVE;

    @ObjectHolder("warehouse")
    public static BlockEntityType<? extends AbstractTileEntityWareHouse> WAREHOUSE;

    @ObjectHolder("composteddirt")
    public static BlockEntityType<? extends BlockEntity> COMPOSTED_DIRT;

    @ObjectHolder("enchanter")
    public static BlockEntityType<TileEntityEnchanter> ENCHANTER;

    @ObjectHolder("stash")
    public static BlockEntityType<TileEntityStash> STASH;

    @ObjectHolder("colony_flag")
    public static BlockEntityType<TileEntityColonyFlag> COLONY_FLAG;
}