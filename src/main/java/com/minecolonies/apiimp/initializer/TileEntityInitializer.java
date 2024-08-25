package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    static
    {
        MinecoloniesTileEntities.SCARECROW = simple("scarecrow", TileEntityScarecrow::new, ModBlocks.blockScarecrow);

        MinecoloniesTileEntities.PLANTATION_FIELD = simple("plantationfield", TileEntityPlantationField::new, ModBlocks.blockPlantationField);

        MinecoloniesTileEntities.BARREL = simple("barrel", TileEntityBarrel::new, ModBlocks.blockBarrel);

        MinecoloniesTileEntities.BUILDING = list("colonybuilding", TileEntityColonyBuilding::new, ModBlocks.getHuts());

        MinecoloniesTileEntities.DECO_CONTROLLER = simple("decorationcontroller", TileEntityDecorationController::new, ModBlocks.blockDecorationPlaceholder);

        MinecoloniesTileEntities.RACK = simple("rack", TileEntityRack::new, ModBlocks.blockRack);

        MinecoloniesTileEntities.GRAVE = simple("grave", TileEntityGrave::new, ModBlocks.blockGrave);

        MinecoloniesTileEntities.NAMED_GRAVE = simple("namedgrave", TileEntityNamedGrave::new, ModBlocks.blockNamedGrave);

        MinecoloniesTileEntities.WAREHOUSE = simple("warehouse", TileEntityWareHouse::new, ModBlocks.blockHutWareHouse);

        MinecoloniesTileEntities.COMPOSTED_DIRT = simple("composteddirt", TileEntityCompostedDirt::new, ModBlocks.blockCompostedDirt);

        MinecoloniesTileEntities.ENCHANTER = simple("enchanter", TileEntityEnchanter::new, ModBlocks.blockHutEnchanter);

        MinecoloniesTileEntities.STASH = simple("stash", TileEntityStash::new, ModBlocks.blockStash);

        MinecoloniesTileEntities.COLONY_FLAG = list("colony_flag", TileEntityColonyFlag::new, List.of(ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner));
    }

    private static <BE extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<BE>> simple(final String id,
        final BlockEntitySupplier<BE> factory,
        final DeferredBlock<?> block)
    {
        return BLOCK_ENTITIES.register(id, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private static <BE extends BlockEntity, B extends Block> DeferredHolder<BlockEntityType<?>, BlockEntityType<BE>> list(final String id,
        final BlockEntitySupplier<BE> factory,
        final List<DeferredBlock<? extends B>> blocks)
    {
        return BLOCK_ENTITIES.register(id, () -> {
            final Block[] dereferenced = new Block[blocks.size()];
            for (int i = 0; i < dereferenced.length; i++)
            {
                dereferenced[i] = blocks.get(i).get();
            }
            return BlockEntityType.Builder.of(factory, dereferenced).build(null);
        });
    }
}
