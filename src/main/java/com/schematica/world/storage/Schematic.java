package com.schematica.world.storage;

import com.minecolonies.util.BlockPosUtil;
import com.schematica.api.ISchematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Schematic implements ISchematic
{
    private static final ItemStack                              DEFAULT_ICON   = new ItemStack(Blocks.GRASS);
    //todo: check if there is a non-internal way to use the registry
    @SuppressWarnings ("deprecation")
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private final short[][][] blocks;
    private final byte[][][]  metadata;
    private final List<TileEntity> tileEntities = new ArrayList<>();
    private final List<Entity>     entities     = new ArrayList<>();
    private final int       width;
    private final int       height;
    private final int       length;
    private       ItemStack icon;
    private       BlockPos  offset;

    public Schematic(final ItemStack icon, final int width, final int height, final int length)
    {
        this.icon = icon;
        this.blocks = new short[width][height][length];
        this.metadata = new byte[width][height][length];

        this.width = width;
        this.height = height;
        this.length = length;

        offset = new BlockPos(0, 0, 0);
    }

    //MINECOLONIES START

    public BlockPos getOffset()
    {
        return offset;
    }

    public void setOffset(BlockPos pos)
    {
        offset = pos;
    }

    public String getType()
    {
        if (hasOffset())
        {
            return "Hut";
        }
        return "Decoration";
    }

    private boolean hasOffset()
    {
        return !BlockPosUtil.isEqual(offset, 0, 0, 0);
    }

    //MINECOLONIES END

    @SuppressWarnings ("deprecation")
    @Override
    public IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        if (isInvalid(pos))
        {
            return Blocks.AIR.getDefaultState();
        }

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final Block block = BLOCK_REGISTRY.getObjectById(this.blocks[x][y][z]);

        //todo: be aware of deprecation and possible removal
        return block.getStateFromMeta(this.metadata[x][y][z]);
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final IBlockState blockState)
    {
        if (isInvalid(pos))
        {
            return false;
        }

        final Block block = blockState.getBlock();
        final int id = BLOCK_REGISTRY.getId(block);
        if (id == -1)
        {
            return false;
        }

        final int meta = block.getMetaFromState(blockState);
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        this.blocks[x][y][z] = (short) id;
        this.metadata[x][y][z] = (byte) meta;
        return true;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        for (final TileEntity tileEntity : this.tileEntities)
        {
            if (tileEntity.getPos().equals(pos))
            {
                return tileEntity;
            }
        }

        return null;
    }

    @Override
    public List<TileEntity> getTileEntities()
    {
        return this.tileEntities;
    }

    @Override
    public void setTileEntity(final BlockPos pos, final TileEntity tileEntity)
    {
        if (isInvalid(pos))
        {
            return;
        }

        removeTileEntity(pos);

        if (tileEntity != null)
        {
            this.tileEntities.add(tileEntity);
        }
    }

    @Override
    public void removeTileEntity(final BlockPos pos)
    {
        final Iterator<TileEntity> iterator = this.tileEntities.iterator();

        while (iterator.hasNext())
        {
            final TileEntity tileEntity = iterator.next();
            if (tileEntity.getPos().equals(pos))
            {
                iterator.remove();
            }
        }
    }

    @NotNull
    @Override
    public List<Entity> getEntities()
    {
        return this.entities;
    }

    @Override
    public void addEntity(final Entity entity)
    {
        if (entity == null || entity.getUniqueID() == null || entity instanceof EntityPlayer)
        {
            return;
        }

        for (final Entity e : this.entities)
        {
            if (entity.getUniqueID().equals(e.getUniqueID()))
            {
                return;
            }
        }

        this.entities.add(entity);
    }

    @Override
    public void removeEntity(final Entity entity)
    {
        if (entity == null || entity.getUniqueID() == null)
        {
            return;
        }

        final Iterator<Entity> iterator = this.entities.iterator();
        while (iterator.hasNext())
        {
            final Entity e = iterator.next();
            if (entity.getUniqueID().equals(e.getUniqueID()))
            {
                iterator.remove();
            }
        }
    }

    @Override
    public ItemStack getIcon()
    {
        return this.icon;
    }

    @Override
    public void setIcon(final ItemStack icon)
    {
        if (icon != null)
        {
            this.icon = icon;
        }
        else
        {
            this.icon = DEFAULT_ICON.copy();
        }
    }

    @Override
    public int getWidth()
    {
        return this.width;
    }

    @Override
    public int getLength()
    {
        return this.length;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }

    private boolean isInvalid(final BlockPos pos)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
    }
}
