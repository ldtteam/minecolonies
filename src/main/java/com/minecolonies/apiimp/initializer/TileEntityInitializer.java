package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    static
    {
        MinecoloniesTileEntities.SCARECROW = BLOCK_ENTITIES.register("scarecrow", () -> BlockEntityType.Builder.of(TileEntityScarecrow::new, ModBlocks.blockScarecrow).build(null));

        MinecoloniesTileEntities.PLANTATION_FIELD = BLOCK_ENTITIES.register("plantationfield", () -> BlockEntityType.Builder.of(TileEntityPlantationField::new, ModBlocks.blockPlantationField).build(null));

        MinecoloniesTileEntities.BARREL = BLOCK_ENTITIES.register("barrel", () -> BlockEntityType.Builder.of(TileEntityBarrel::new, ModBlocks.blockBarrel).build(null));

        MinecoloniesTileEntities.BUILDING = BLOCK_ENTITIES.register("colonybuilding", () -> BlockEntityType.Builder.of(TileEntityColonyBuilding::new, ModBlocks.getHuts()).build(null));

        MinecoloniesTileEntities.DECO_CONTROLLER = BLOCK_ENTITIES.register("decorationcontroller", () -> BlockEntityType.Builder
                                                     .of(TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder)
                                                     .build(null));

        MinecoloniesTileEntities.RACK = BLOCK_ENTITIES.register("rack", () -> BlockEntityType.Builder.of(TileEntityRack::new, ModBlocks.blockRack).build(null));

        MinecoloniesTileEntities.GRAVE = BLOCK_ENTITIES.register("grave", () -> BlockEntityType.Builder.of(TileEntityGrave::new, ModBlocks.blockGrave).build(null));

        MinecoloniesTileEntities.NAMED_GRAVE = BLOCK_ENTITIES.register("namedgrave", () -> BlockEntityType.Builder.of(TileEntityNamedGrave::new, ModBlocks.blockNamedGrave).build(null));

        MinecoloniesTileEntities.WAREHOUSE = BLOCK_ENTITIES.register("warehouse", () -> BlockEntityType.Builder.of(TileEntityWareHouse::new, ModBlocks.blockHutWareHouse).build(null));

        MinecoloniesTileEntities.COMPOSTED_DIRT = BLOCK_ENTITIES.register("composteddirt", () -> BlockEntityType.Builder.of(TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt)
                                                    .build(null));

        MinecoloniesTileEntities.ENCHANTER = BLOCK_ENTITIES.register("enchanter", () -> BlockEntityType.Builder.of(TileEntityEnchanter::new, ModBlocks.blockHutEnchanter).build(null));

        MinecoloniesTileEntities.STASH = BLOCK_ENTITIES.register("stash", () -> BlockEntityType.Builder.of(TileEntityStash::new, ModBlocks.blockStash).build(null));

        MinecoloniesTileEntities.COLONY_FLAG = BLOCK_ENTITIES.register("colony_flag", () -> BlockEntityType.Builder.of(TileEntityColonyFlag::new, ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner).build(null));
    }
}
