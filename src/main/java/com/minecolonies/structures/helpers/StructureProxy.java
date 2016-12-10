package com.minecolonies.structures.helpers;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Proxy class translating the structures method to something we can use.
 */
public class StructureProxy
{
    private final Structure structure;
    private final List<TileEntity> tileEntities = new ArrayList<>();
    private final List<Entity>     entities     = new ArrayList<>();
    private Block[][][]       blocks;
    private IBlockState[][][] metadata;
    private int               width;
    private int               height;
    private int               length;
    private BlockPos          offset;

    /**
     * @param worldObj the world.
     * @param name     the string where the structure is saved at.
     */
    public StructureProxy(final World worldObj, final String name)
    {
        this.structure = new Structure(worldObj, name, new PlacementSettings());
        final BlockPos size = structure.getSize(Rotation.NONE);

        this.width = size.getX();
        this.height = size.getY();
        this.length = size.getZ();

        this.blocks = new Block[width][height][length];
        this.metadata = new IBlockState[width][height][length];

        for (final Template.BlockInfo info : structure.getBlockInfo())
        {
            final BlockPos tempPos = info.pos;
            blocks[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = info.blockState.getBlock();
            metadata[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = info.blockState;

            if (info.blockState.getBlock() instanceof AbstractBlockHut)
            {
                offset = info.pos;
            }
        }
    }

    /**
     * Getter of the offset.
     *
     * @return the blockPos of the offset.
     */
    public BlockPos getOffset()
    {
        return offset;
    }

    /**
     * Setter of the offset.
     *
     * @param pos the new offset.
     */
    public void setOffset(final BlockPos pos)
    {
        offset = pos;
    }

    /**
     * Getter of the type of the structure.
     *
     * @return true if so.
     */
    public String getType()
    {
        if (hasOffset())
        {
            return "Hut";
        }
        return "Decoration";
    }

    /**
     * Checks if the structure has an offset.
     *
     * @return true if so.
     */
    private boolean hasOffset()
    {
        return !BlockPosUtil.isEqual(offset, 0, 0, 0);
    }

    /**
     * Getter for the structure.
     * @return the structure object.
     */
    public Structure getStructure()
    {
        return structure;
    }

    /**
     * Getter of the IBlockState at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    public IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        return metadata[pos.getX()][pos.getY()][pos.getZ()];
    }

    /**
     * return a tileEntity at a certain position.
     *
     * @param pos the position.
     * @return the tileEntity.
     */
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

    /**
     * Return a list of tileEntities.
     *
     * @return list of them.
     */
    public List<TileEntity> getTileEntities()
    {
        return this.tileEntities;
    }

    /**
     * Sets tileEntities.
     *
     * @param pos        at position.
     * @param tileEntity the entity to set.
     */
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

    /**
     * Checks if a position is inside the structure.
     *
     * @param pos the position.
     * @return true if so.
     */
    private boolean isInvalid(final BlockPos pos)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        return (x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length);
    }

    /**
     * Removes a tileEntity at a position.
     *
     * @param pos the position to remove it at.
     */
    private void removeTileEntity(final BlockPos pos)
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

    /**
     * Return all entities.
     *
     * @return the list of entities.
     */
    @NotNull
    public List<Entity> getEntities()
    {
        return this.entities;
    }

    /**
     * Add an entitiy.
     *
     * @param entity the entity to add.
     */
    public void addEntity(final Entity entity)
    {
        if (entity == null || entity instanceof EntityPlayer)
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

    /**
     * Remove a certain entitiy.
     *
     * @param entity that should be removed.
     */
    public void removeEntity(final Entity entity)
    {
        if (entity == null)
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

    /**
     * Getter of the width.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Getter of the length.
     *
     * @return the length
     */
    public int getLength()
    {
        return this.length;
    }

    /**
     * Getter of the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Rotate the structure depending on the direction it's facing.
     *
     * @param times times to rotate.
     */
    public void rotate(final int times)
    {
        final Rotation rotation;
        switch (times)
        {
            case 1:
                rotation = Rotation.CLOCKWISE_90;
                break;
            case 2:
                rotation = Rotation.CLOCKWISE_180;
                break;
            case 3:
                rotation = Rotation.COUNTERCLOCKWISE_90;
                break;
            default:
                rotation = Rotation.NONE;
        }
        structure.setPlacementSettings(new PlacementSettings().setRotation(rotation));

        final BlockPos size = structure.getSize(rotation);

        this.width = size.getX();
        this.height = size.getY();
        this.length = size.getZ();

        this.blocks = new Block[width][height][length];
        this.metadata = new IBlockState[width][height][length];

        int minX = 0;
        int minY = 0;
        int minZ = 0;

        for (final Template.BlockInfo info : structure.getBlockInfoWithSettings(new PlacementSettings().setRotation(rotation)))
        {
            final BlockPos tempPos = info.pos;
            final int x = tempPos.getX();
            final int y = tempPos.getY();
            final int z = tempPos.getZ();
            if (x < minX)
            {
                minX = x;
            }

            if (y < minY)
            {
                minY = y;
            }

            if (z < minZ)
            {
                minZ = z;
            }
        }

        minX = Math.abs(minX);
        minY = Math.abs(minY);
        minZ = Math.abs(minZ);
        boolean foundHut = false;

        for (final Template.BlockInfo info : structure.getBlockInfoWithSettings(new PlacementSettings().setRotation(rotation)))
        {
            final BlockPos tempPos = info.pos;
            final int x = tempPos.getX() + minX;
            final int y = tempPos.getY() + minY;
            final int z = tempPos.getZ() + minZ;

            this.blocks[x][y][z] = info.blockState.getBlock();
            this.metadata[x][y][z] = info.blockState;

            if (info.blockState.getBlock() instanceof AbstractBlockHut)
            {
                foundHut = true;
                offset = info.pos.add(minX, minY, minZ);
            }
        }
        updateOffSetIfDecoration(foundHut, size, times, minX, minY, minZ);
    }

    /**
     * Updates the offset if the structure is a decoration.
     *
     * @param foundHut if false update.
     */
    private void updateOffSetIfDecoration(final boolean foundHut, final BlockPos size, int rotation, int minX, int minY, int minZ)
    {
        if (!foundHut)
        {
            BlockPos tempSize = size;
            if (rotation == 1)
            {
                tempSize = new BlockPos(-size.getX(), size.getY(), size.getZ());
            }
            if (rotation == 2)
            {
                tempSize = new BlockPos(-size.getX(), size.getY(), -size.getZ());
            }
            if (rotation == 3)
            {
                tempSize = new BlockPos(size.getX(), size.getY(), -size.getZ());
            }

            offset = new BlockPos(tempSize.getX() / 2, 0, tempSize.getZ() / 2).add(minX, minY, minZ);
        }
    }
}
