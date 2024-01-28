package com.minecolonies.api.tileentities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

public class MinecoloniesTileEntities
{
    public static RegistryObject<BlockEntityType<? extends AbstractTileEntityScarecrow>> SCARECROW;

    public static RegistryObject<BlockEntityType<? extends AbstractTileEntityPlantationField>> PLANTATION_FIELD;

    public static RegistryObject<BlockEntityType<? extends AbstractTileEntityBarrel>> BARREL;

    public static RegistryObject<BlockEntityType<? extends AbstractTileEntityColonyBuilding>> BUILDING;

    public static RegistryObject<BlockEntityType<? extends BlockEntity>> DECO_CONTROLLER;

    public static RegistryObject<BlockEntityType<TileEntityRack>> RACK;

    public static RegistryObject<BlockEntityType<TileEntityGrave>> GRAVE;

    public static RegistryObject<BlockEntityType<? extends TileEntityNamedGrave>> NAMED_GRAVE;

    public static RegistryObject<BlockEntityType<? extends AbstractTileEntityWareHouse>> WAREHOUSE;

    public static RegistryObject<BlockEntityType<? extends BlockEntity>> COMPOSTED_DIRT;

    public static RegistryObject<BlockEntityType<TileEntityEnchanter>> ENCHANTER;

    public static RegistryObject<BlockEntityType<TileEntityStash>> STASH;

    public static RegistryObject<BlockEntityType<TileEntityColonyFlag>> COLONY_FLAG;
}