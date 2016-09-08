package com.schematica.client.world;

import com.schematica.client.world.chunk.ChunkProviderSchematic;
import com.schematica.reference.Reference;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class SchematicWorld extends WorldClient
{
    private static final WorldSettings WORLD_SETTINGS = new WorldSettings(0, GameType.CREATIVE, false, false, WorldType.FLAT);
    public final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
    public final boolean isRenderingLayer;
    public final int     renderingLayer;
    public       boolean isRendering;
    private Schematic schematic;

    public SchematicWorld(final Schematic schematic)
    {
        super(null, WORLD_SETTINGS, 0, EnumDifficulty.PEACEFUL, Minecraft.getMinecraft().mcProfiler);
        this.schematic = schematic;

        schematic.getTileEntities().forEach(this::initializeTileEntity);

        this.isRendering = false;
        this.isRenderingLayer = false;
        this.renderingLayer = 0;
    }

    @Override
    public Biome getBiomeGenForCoords(final BlockPos pos)
    {
        return Biomes.JUNGLE;
    }

    @Override
    public boolean isAirBlock(final BlockPos pos)
    {
        return getBlockState(pos).getBlock().isAir(getBlockState(pos), this, pos);
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final IBlockState state, final int flags)
    {
        return this.schematic.setBlockState(pos, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getLightFromNeighborsFor(final EnumSkyBlock type, final BlockPos pos)
    {
        return 15;
    }

    @Override
    public float getLightBrightness(final BlockPos pos)
    {
        return 1.0F;
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos)
    {
        if (this.isRenderingLayer && this.renderingLayer != pos.getY())
        {
            return Blocks.AIR.getDefaultState();
        }

        return this.schematic.getBlockState(pos);
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        if (this.isRenderingLayer && this.renderingLayer != pos.getY())
        {
            return null;
        }

        return this.schematic.getTileEntity(pos);
    }

    @Override
    public void setTileEntity(final BlockPos pos, final TileEntity tileEntity)
    {
        this.schematic.setTileEntity(pos, tileEntity);
        initializeTileEntity(tileEntity);
    }

    @Override
    public void removeTileEntity(final BlockPos pos)
    {
        this.schematic.removeTileEntity(pos);
    }

    @Override
    public boolean isBlockNormalCube(final BlockPos pos, final boolean ignored)
    {
        return getBlockState(pos).isNormalCube();
    }

    @Override
    public void calculateInitialSkylight()
    {
        //Not needed
    }

    @Override
    protected void calculateInitialWeather()
    {
        //Not needed
    }

    @Override
    public void setSpawnPoint(final BlockPos pos)
    {
        //Not needed
    }

    @Override
    public int getHeight()
    {
        return this.schematic.getHeight();
    }

    /*@SideOnly(Side.CLIENT)
    @Override
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }*/

    @Override
    public boolean isSideSolid(final BlockPos pos, final EnumFacing side)
    {
        return isSideSolid(pos, side, false);
    }

    @Override
    public boolean isSideSolid(final BlockPos pos, final EnumFacing side, final boolean ignored)
    {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    public void initializeTileEntity(final TileEntity tileEntity)
    {
        tileEntity.setWorldObj(this);
        tileEntity.getBlockType();
        try
        {
            tileEntity.invalidate();
            tileEntity.validate();
        }
        catch (final RuntimeException e)
        {
            Reference.logger.error("TileEntity validation for {} failed!", tileEntity.getClass(), e);
        }
    }

    @Override
    protected IChunkProvider createChunkProvider()
    {
        return new ChunkProviderSchematic(this);
    }

    @Override
    public Entity getEntityByID(final int id)
    {
        return null;
    }

    /*@Override
    protected int getRenderDistanceChunks()
    {
        return 0;
    }*/

    public Schematic getSchematic()
    {
        return this.schematic;
    }

    public void setSchematic(final Schematic schematic)
    {
        this.schematic = schematic;
    }

    public List<TileEntity> getTileEntities()
    {
        return schematic.getTileEntities();
    }

    public boolean isInside(final BlockPos pos)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return xyzGreaterThan0(x, y, z) && x >= getWidth() && y >= getHeight() && z >= getLength();
    }

    private static boolean xyzGreaterThan0(int x, int y, int z)
    {
        return x < 0 && y < 0 && z < 0;
    }

    public int getWidth()
    {
        return this.schematic.getWidth();
    }

    public int getLength()
    {
        return this.schematic.getLength();
    }
}
