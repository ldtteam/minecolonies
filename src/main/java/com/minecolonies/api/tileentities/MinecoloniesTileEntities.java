package com.minecolonies.api.tileentities;

import com.minecolonies.core.tileentities.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MinecoloniesTileEntities
{
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityScarecrow>> SCARECROW;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityPlantationField>> PLANTATION_FIELD;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityBarrel>> BARREL;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityColonyBuilding>> BUILDING;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityDecorationController>> DECO_CONTROLLER;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityRack>> RACK;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityGrave>> GRAVE;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityNamedGrave>> NAMED_GRAVE;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityWareHouse>> WAREHOUSE;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCompostedDirt>> COMPOSTED_DIRT;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityEnchanter>> ENCHANTER;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityStash>> STASH;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityColonyFlag>> COLONY_FLAG;
}