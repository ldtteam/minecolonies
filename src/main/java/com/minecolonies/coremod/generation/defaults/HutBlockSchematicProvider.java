package com.minecolonies.coremod.generation.defaults;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.MapData;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Generates structure blueprints for every registered hutblock, intended to allow
 * placing these blocks outside of a colony when creating schematics.
 */
public class HutBlockSchematicProvider implements IDataProvider
{
    protected final DataGenerator generator;

    public HutBlockSchematicProvider(final DataGenerator generatorIn)
    {
        this.generator = generatorIn;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        final Path path = this.generator.getOutputFolder()
                .resolve(Structures.SCHEMATICS_ASSET_PATH)
                .resolve(MOD_ID)
                .resolve(Structures.SCHEMATICS_PREFIX)
                .resolve("infrastructure")
                .resolve("hutblocks");

        for (final BuildingEntry entry : IBuildingRegistry.getInstance().getValues())
        {
            final String name = entry.getRegistryName().getPath() + "hut";   // this must not match the blockhut name or it will be seen as a "real" hut schematic
            final Blueprint blueprint = generateBlueprint(name, entry.getBuildingBlock());
            final File file = path.resolve(name + Structures.SCHEMATIC_EXTENSION_NEW).toFile();
            Utils.checkDirectory(file.getParentFile());
            CompressedStreamTools.writeCompressed(BlueprintUtil.writeBlueprintToNBT(blueprint), file);
        }
    }

    @NotNull
    private Blueprint generateBlueprint(@NotNull final String name,
                                        @NotNull final AbstractBlockHut<?> block)
    {
        final SingleBlockWorld world = new SingleBlockWorld(block.getDefaultState());
        final short size = 1;
        return BlueprintUtil.createBlueprint(world, BlockPos.ZERO, false, size, size, size, name, Optional.empty());
    }

    @NotNull
    @Override
    public String getName()
    {
        return "HutBlockSchematicProvider";
    }

    private static class SingleBlockWorld extends World
    {
        private final BlockState block;
        private final TileEntity entity;

        public SingleBlockWorld(@NotNull final BlockState block)
        {
            super(null, World.OVERWORLD, getDimensionType(DimensionType.OVERWORLD), null, false, false, 0);

            this.block = block;
            this.entity = block.createTileEntity(this);
        }

        private static DimensionType getDimensionType(@NotNull final RegistryKey<DimensionType> key)
        {
            final DynamicRegistries.Impl reg = new DynamicRegistries.Impl();
            DimensionType.registerTypes(reg);
            return reg.func_230520_a_().getOrThrow(key);
        }

        @NotNull
        @Override
        public BlockState getBlockState(@NotNull final BlockPos pos)
        {
            if (pos.equals(BlockPos.ZERO))
            {
                return this.block;
            }
            return Blocks.VOID_AIR.getDefaultState();
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(@NotNull final BlockPos pos)
        {
            if (pos.equals(BlockPos.ZERO))
            {
                return this.entity;
            }
            return null;
        }

        //region Not implemented
        @Override
        public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags)
        {
            throw new NotImplementedException("");
        }

        @Override
        public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch)
        {
            throw new NotImplementedException("");
        }

        @Override
        public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch)
        {
            throw new NotImplementedException("");
       }

        @Nullable
        @Override
        public Entity getEntityByID(int id)
        {
            throw new NotImplementedException("");
        }

        @Nullable
        @Override
        public MapData getMapData(String mapName)
        {
            throw new NotImplementedException("");
        }

        @Override
        public void registerMapData(MapData mapDataIn)
        {
            throw new NotImplementedException("");
        }

        @Override
        public int getNextMapId()
        {
            throw new NotImplementedException("");
        }

        @Override
        public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
        {
            throw new NotImplementedException("");
        }

        @Override
        public Scoreboard getScoreboard()
        {
            throw new NotImplementedException("");
        }

        @Override
        public RecipeManager getRecipeManager()
        {
            throw new NotImplementedException("");
        }

        @Override
        public ITagCollectionSupplier getTags()
        {
            throw new NotImplementedException("");
        }

        @Override
        public ITickList<Block> getPendingBlockTicks()
        {
            throw new NotImplementedException("");
        }

        @Override
        public ITickList<Fluid> getPendingFluidTicks()
        {
            throw new NotImplementedException("");
        }

        @Override
        public AbstractChunkProvider getChunkProvider()
        {
            throw new NotImplementedException("");
        }

        @Override
        public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data)
        {
            throw new NotImplementedException("");
        }

        @Override
        public DynamicRegistries func_241828_r()
        {
            throw new NotImplementedException("");
        }

        @Override
        public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_)
        {
            throw new NotImplementedException("");
        }

        @Override
        public List<? extends PlayerEntity> getPlayers()
        {
            throw new NotImplementedException("");
        }

        @Override
        public Biome getNoiseBiomeRaw(int x, int y, int z)
        {
            throw new NotImplementedException("");
        }
        //endregion
    }
}
