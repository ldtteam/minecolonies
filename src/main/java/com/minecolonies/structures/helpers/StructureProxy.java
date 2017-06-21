package com.minecolonies.structures.helpers;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Proxy class translating the structures method to something we can use.
 */
public class StructureProxy
{
    private final Structure structure;
    private Template.EntityInfo[][][] entities;
    private Template.BlockInfo[][][] blocks;
    private int                      width;
    private int                      height;
    private int                      length;
    private BlockPos                 offset;

    /**
     * @param worldObj the world.
     * @param name     the string where the structure is saved at.
     */
    public StructureProxy(final World worldObj, final String name)
    {
        this.structure = new Structure(worldObj, name, new PlacementSettings());

        if(structure.isTemplateMissing())
        {
            return;
        }
        final BlockPos size = structure.getSize(Rotation.NONE);

        this.width = size.getX();
        this.height = size.getY();
        this.length = size.getZ();

        this.blocks = new Template.BlockInfo[width][height][length];
        this.entities = new Template.EntityInfo[width][height][length];

        for (final Template.BlockInfo info : structure.getBlockInfo())
        {
            final BlockPos tempPos = info.pos;
            blocks[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = info;
            entities[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = null;

            if (info.blockState.getBlock() instanceof AbstractBlockHut)
            {
                offset = info.pos;
            }
        }

        for(final Template.EntityInfo info: structure.getTileEntities())
        {
            final BlockPos tempPos = info.blockPos;
            entities[tempPos.getX()][tempPos.getY()][tempPos.getZ()] = info;
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
        return blocks[pos.getX()][pos.getY()][pos.getZ()].blockState;
    }

    /**
     * Getter of the BlockInfo at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    public Template.BlockInfo getBlockInfo(@NotNull final BlockPos pos)
    {
        return blocks[pos.getX()][pos.getY()][pos.getZ()];
    }

    /**
     * Return a list of tileEntities.
     *
     * @return list of them.
     */
    public List<Template.EntityInfo> getTileEntities()
    {
        return this.structure.getTileEntities();
    }

    /**
     * Getter of the EntityInfo at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    @Nullable
    public Template.EntityInfo getEntityinfo(@NotNull final BlockPos pos)
    {
        if(entities[pos.getX()][pos.getY()].length == 0)
        {
            return null;
        }
        return entities[pos.getX()][pos.getY()][pos.getZ()];
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
     * @param times times to rotateWithMirror.
     * @param world the world to rotateWithMirror it in.
     * @param rotatePos the pos to rotateWithMirror it around.
     * @param mirror the mirror
     */
    public void rotateWithMirror(final int times, World world, BlockPos rotatePos, Mirror mirror)
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
        structure.setPlacementSettings(new PlacementSettings().setRotation(rotation).setMirror(mirror));

        final BlockPos size = structure.getSize(rotation);

        this.width = size.getX();
        this.height = size.getY();
        this.length = size.getZ();

        this.blocks = new Template.BlockInfo[width][height][length];
        this.entities = new Template.EntityInfo[width][height][length];

        int minX = 0;
        int minY = 0;
        int minZ = 0;

        for (final Template.BlockInfo info : structure.getBlockInfoWithSettings(new PlacementSettings().setRotation(rotation).setMirror(mirror)))
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
        final PlacementSettings settings = new PlacementSettings().setRotation(rotation).setMirror(mirror);

        for (final Template.BlockInfo info : structure.getBlockInfoWithSettings(settings))
        {
            final BlockPos tempPos = info.pos;
            final int x = tempPos.getX() + minX;
            final int y = tempPos.getY() + minY;
            final int z = tempPos.getZ() + minZ;

            this.blocks[x][y][z] = info;
            this.entities[x][y][z] = null;

            if (info.blockState.getBlock() instanceof AbstractBlockHut)
            {
                foundHut = true;
                offset = info.pos.add(minX, minY, minZ);
            }
        }

        updateOffSetIfDecoration(foundHut, size, times, minX, minY, minZ);

        for(final Template.EntityInfo info: structure.getTileEntities())
        {
            final Template.EntityInfo newInfo = structure.transformEntityInfoWithSettings(info, world, rotatePos.subtract(offset).add(new BlockPos(minX, minY, minZ)), settings);
            //289 74 157 - 289.9 76.5, 157.5
            final BlockPos tempPos = Template.transformedBlockPos(settings, info.blockPos);
            final int x = tempPos.getX() + minX;
            final int y = tempPos.getY() + minY;
            final int z = tempPos.getZ() + minZ;
            this.entities[x][y][z] = newInfo;
        }
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
