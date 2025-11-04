package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.AbstractColonyBlock;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    static
    {
        MinecoloniesTileEntities.SCARECROW = BLOCK_ENTITIES.register("scarecrow", () -> BlockEntityType.Builder.of(TileEntityScarecrow::new, ModBlocks.blockScarecrow.get()).build(null));

        MinecoloniesTileEntities.PLANTATION_FIELD = BLOCK_ENTITIES.register("plantationfield", () -> BlockEntityType.Builder.of(TileEntityPlantationField::new, ModBlocks.blockPlantationField.get()).build(null));

        MinecoloniesTileEntities.BARREL = BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder.of(TileEntityBarrel::new, ModBlocks.blockBarrel.get()).build(null));

        MinecoloniesTileEntities.BUILDING = BLOCK_ENTITIES.register("colonybuilding",
            () -> BlockEntityType.Builder.of(TileEntityColonyBuilding::new, ModBlocks.HUTS.stream().map(DeferredHolder::get).toArray(AbstractColonyBlock[]::new)).build(null));

        MinecoloniesTileEntities.DECO_CONTROLLER = BLOCK_ENTITIES.register("decorationcontroller", () -> BlockEntityType.Builder
                                                     .of(TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder.get())
                                                     .build(null));

        MinecoloniesTileEntities.RACK = BLOCK_ENTITIES.register("rack", () -> BlockEntityType.Builder.of(TileEntityRack::new, ModBlocks.blockRack.get()).build(null));

        MinecoloniesTileEntities.GRAVE = BLOCK_ENTITIES.register("grave", () -> BlockEntityType.Builder.of(TileEntityGrave::new, ModBlocks.blockGrave.get()).build(null));

        MinecoloniesTileEntities.NAMED_GRAVE = BLOCK_ENTITIES.register("namedgrave", () -> BlockEntityType.Builder.of(TileEntityNamedGrave::new, ModBlocks.blockNamedGrave.get()).build(null));

        MinecoloniesTileEntities.WAREHOUSE = BLOCK_ENTITIES.register("warehouse", () -> BlockEntityType.Builder.of(TileEntityWareHouse::new, ModBlocks.blockHutWareHouse.get()).build(null));

        MinecoloniesTileEntities.COMPOSTED_DIRT = BLOCK_ENTITIES.register("composteddirt", () -> BlockEntityType.Builder.of(TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt.get())
                                                    .build(null));

        MinecoloniesTileEntities.ENCHANTER = BLOCK_ENTITIES.register("enchanter", () -> BlockEntityType.Builder.of(TileEntityEnchanter::new, ModBlocks.blockHutEnchanter.get()).build(null));

        MinecoloniesTileEntities.STASH = BLOCK_ENTITIES.register("stash", () -> BlockEntityType.Builder.of(TileEntityStash::new, ModBlocks.blockStash.get()).build(null));

        MinecoloniesTileEntities.COLONY_FLAG = BLOCK_ENTITIES.register("colony_flag", () -> BlockEntityType.Builder.of(TileEntityColonyFlag::new, ModBlocks.blockColonyBanner.get(), ModBlocks.blockColonyWallBanner.get()).build(null));

        MinecoloniesTileEntities.COLONY_SIGN = BLOCK_ENTITIES.register("colonysign", () -> BlockEntityType.Builder.of(TileEntityColonySign::new, ModBlocks.blockColonySign.get()).build(null));
    }
}
