package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class TileEntityInitializer
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    static
    {
        MinecoloniesTileEntities.SCARECROW = registerArray("scarecrow", TileEntityScarecrow::new, () -> new Block[] { ModBlocks.blockScarecrow});

        MinecoloniesTileEntities.PLANTATION_FIELD = registerArray("plantationfield", TileEntityPlantationField::new, () -> new Block[] { ModBlocks.blockPlantationField});

        MinecoloniesTileEntities.BARREL = registerArray("barrel", TileEntityBarrel::new, () -> new Block[] { ModBlocks.blockBarrel});

        MinecoloniesTileEntities.BUILDING = registerArray("colonybuilding", TileEntityColonyBuilding::new, ModBlocks::getHuts);

        MinecoloniesTileEntities.DECO_CONTROLLER = registerArray("decorationcontroller", TileEntityDecorationController::new, () -> new Block[] { ModBlocks.blockDecorationPlaceholder});

        MinecoloniesTileEntities.RACK = registerArray("rack", TileEntityRack::new, () -> new Block[] { ModBlocks.blockRack});

        MinecoloniesTileEntities.GRAVE = registerArray("grave", TileEntityGrave::new, () -> new Block[] { ModBlocks.blockGrave});

        MinecoloniesTileEntities.NAMED_GRAVE = registerArray("namedgrave", TileEntityNamedGrave::new, () -> new Block[] { ModBlocks.blockNamedGrave});

        MinecoloniesTileEntities.WAREHOUSE = registerArray("warehouse", TileEntityWareHouse::new, () -> new Block[] { ModBlocks.blockHutWareHouse});

        MinecoloniesTileEntities.COMPOSTED_DIRT = registerArray("composteddirt", TileEntityCompostedDirt::new, () -> new Block[] { ModBlocks.blockCompostedDirt});

        MinecoloniesTileEntities.ENCHANTER = registerArray("enchanter", TileEntityEnchanter::new, () -> new Block[] { ModBlocks.blockHutEnchanter});

        MinecoloniesTileEntities.STASH = registerArray("stash", TileEntityStash::new, () -> new Block[] { ModBlocks.blockStash});

        MinecoloniesTileEntities.COLONY_FLAG = registerArray("colony_flag", TileEntityColonyFlag::new,
            () -> new Block[] {ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner});

        MinecoloniesTileEntities.COLONY_SIGN = registerArray("colonysign", TileEntityColonySign::new, () -> new Block[] { ModBlocks.blockColonySign});
    }

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
        final String name,
        final BlockEntitySupplier<T> factory,
        final java.util.function.Supplier<Block[]> blocks)
    {
        return registerArray(name, factory, blocks);
    }

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerArray(
        final String name,
        final BlockEntitySupplier<T> factory,
        final java.util.function.Supplier<Block[]> blocks)
    {
        return BLOCK_ENTITIES.register(name, () ->
        {
            final Block[] validBlocks = blocks.get();
            for (final Block block : validBlocks)
            {
                if (block == null)
                {
                    throw new IllegalStateException("Uninitialized MineColonies block supplied to block entity " + name);
                }
            }
            return new BlockEntityType<>(factory, Set.of(validBlocks));
        });
    }
}
