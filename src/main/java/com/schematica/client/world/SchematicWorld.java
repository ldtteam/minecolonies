package com.schematica.client.world;

import com.schematica.core.util.BlockPosHelper;
import com.schematica.core.util.MBlockPos;
import com.schematica.api.ISchematic;
import com.schematica.block.state.pattern.BlockStateReplacer;
import com.schematica.client.world.chunk.ChunkProviderSchematic;
import com.schematica.reference.Reference;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

public class SchematicWorld extends WorldClient {
    private static final WorldSettings WORLD_SETTINGS = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);

    private Schematic schematic;

    public final MBlockPos position = new MBlockPos();
    public boolean isRendering;
    public boolean isRenderingLayer;
    public int renderingLayer;

    public SchematicWorld(final Schematic schematic) {
        super(null, WORLD_SETTINGS, 0, EnumDifficulty.PEACEFUL, Minecraft.getMinecraft().mcProfiler);
        this.schematic = schematic;

        for (final TileEntity tileEntity : schematic.getTileEntities()) {
            initializeTileEntity(tileEntity);
        }

        this.isRendering = false;
        this.isRenderingLayer = false;
        this.renderingLayer = 0;
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos) {
        if (this.isRenderingLayer && this.renderingLayer != pos.getY()) {
            return Blocks.air.getDefaultState();
        }

        return this.schematic.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final IBlockState state, final int flags) {
        return this.schematic.setBlockState(pos, state);
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos) {
        if (this.isRenderingLayer && this.renderingLayer != pos.getY()) {
            return null;
        }

        return this.schematic.getTileEntity(pos);
    }

    @Override
    public void setTileEntity(final BlockPos pos, final TileEntity tileEntity) {
        this.schematic.setTileEntity(pos, tileEntity);
        initializeTileEntity(tileEntity);
    }

    @Override
    public void removeTileEntity(final BlockPos pos) {
        this.schematic.removeTileEntity(pos);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getLightFromNeighborsFor(final EnumSkyBlock type, final BlockPos pos) {
        return 15;
    }

    @Override
    public float getLightBrightness(final BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean isBlockNormalCube(final BlockPos pos, final boolean _default) {
        return getBlockState(pos).getBlock().isNormalCube(this, pos);
    }

    @Override
    public void calculateInitialSkylight() {}

    @Override
    protected void calculateInitialWeather() {}

    @Override
    public void setSpawnPoint(final BlockPos pos) {}

    @Override
    protected int getRenderDistanceChunks() {
        return 0;
    }

    @Override
    public boolean isAirBlock(final BlockPos pos) {
        return getBlockState(pos).getBlock().isAir(this, pos);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(final BlockPos pos) {
        return BiomeGenBase.jungle;
    }

    public int getWidth() {
        return this.schematic.getWidth();
    }

    public int getLength() {
        return this.schematic.getLength();
    }

    @Override
    public int getHeight() {
        return this.schematic.getHeight();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new ChunkProviderSchematic(this);
    }

    @Override
    public Entity getEntityByID(final int id) {
        return null;
    }

    @Override
    public boolean isSideSolid(final BlockPos pos, final EnumFacing side) {
        return isSideSolid(pos, side, false);
    }

    @Override
    public boolean isSideSolid(final BlockPos pos, final EnumFacing side, final boolean _default) {
        return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
    }

    public void setSchematic(final Schematic schematic) {
        this.schematic = schematic;
    }

    public Schematic getSchematic() {
        return this.schematic;
    }

    public void initializeTileEntity(final TileEntity tileEntity) {
        tileEntity.setWorldObj(this);
        tileEntity.getBlockType();
        try {
            tileEntity.invalidate();
            tileEntity.validate();
        } catch (final Exception e) {
            Reference.logger.error("TileEntity validation for {} failed!", tileEntity.getClass(), e);
        }
    }

    public void setIcon(final ItemStack icon) {
        this.schematic.setIcon(icon);
    }

    public ItemStack getIcon() {
        return this.schematic.getIcon();
    }

    public List<TileEntity> getTileEntities() {
        return this.schematic.getTileEntities();
    }

    public boolean toggleRendering() {
        this.isRendering = !this.isRendering;
        return this.isRendering;
    }

    public String getDebugDimensions() {
        return "WHL: " + getWidth() + " / " + getHeight() + " / " + getLength();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int replaceBlock(final BlockStateHelper matcher, final BlockStateReplacer replacer, final Map<IProperty, Comparable> properties) {
        int count = 0;

        for (final MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, getWidth(), getHeight(), getLength())) {
            final IBlockState blockState = this.schematic.getBlockState(pos);

            // TODO: add support for tile entities?
            if (blockState.getBlock().hasTileEntity(blockState)) {
                continue;
            }

            if (matcher.apply(blockState)) {
                final IBlockState replacement = replacer.getReplacement(blockState, properties);

                // TODO: add support for tile entities?
                if (replacement.getBlock().hasTileEntity(replacement)) {
                    continue;
                }

                if (this.schematic.setBlockState(pos, replacement)) {
                    markBlockForUpdate(pos.add(this.position));
                    count++;
                }
            }
        }

        return count;
    }

    public boolean isInside(final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return !(x < 0 || y < 0 || z < 0 || x >= getWidth() || y >= getHeight() || z >= getLength());
    }
}
