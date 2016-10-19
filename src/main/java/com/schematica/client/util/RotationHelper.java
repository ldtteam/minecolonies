package com.schematica.client.util;

import com.minecolonies.util.BlockPosUtil;
import com.schematica.block.state.BlockStateHelper;
import com.schematica.client.world.SchematicWorld;
import com.schematica.core.util.BlockPosHelper;
import com.schematica.reference.Reference;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RotationHelper
{
    /**
     * This is needed for Schematica to work.
     */
    @SuppressWarnings("deprecation")
    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();
    private static final EnumFacing[][]                         FACINGS        = new EnumFacing[EnumFacing.VALUES.length][];
    private static final EnumFacing.Axis[][]                    AXISES         = new EnumFacing.Axis[EnumFacing.Axis.values().length][];
    private static final BlockLog.EnumAxis[][]                  AXISES_LOG     = new BlockLog.EnumAxis[EnumFacing.Axis.values().length][];
    private static final BlockQuartz.EnumType[][]               AXISES_QUARTZ  = new BlockQuartz.EnumType[EnumFacing.Axis.values().length][];
    static
    {
        FACINGS[EnumFacing.DOWN.ordinal()] = new EnumFacing[] {
          EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH
        };
        FACINGS[EnumFacing.UP.ordinal()] = new EnumFacing[] {
          EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH
        };
        FACINGS[EnumFacing.NORTH.ordinal()] = new EnumFacing[] {
          EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.UP
        };
        FACINGS[EnumFacing.SOUTH.ordinal()] = new EnumFacing[] {
          EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN
        };
        FACINGS[EnumFacing.WEST.ordinal()] = new EnumFacing[] {
          EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST
        };
        FACINGS[EnumFacing.EAST.ordinal()] = new EnumFacing[] {
          EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST
        };

        AXISES[EnumFacing.Axis.X.ordinal()] = new EnumFacing.Axis[] {
          EnumFacing.Axis.X, EnumFacing.Axis.Z, EnumFacing.Axis.Y
        };
        AXISES[EnumFacing.Axis.Y.ordinal()] = new EnumFacing.Axis[] {
          EnumFacing.Axis.Z, EnumFacing.Axis.Y, EnumFacing.Axis.X
        };
        AXISES[EnumFacing.Axis.Z.ordinal()] = new EnumFacing.Axis[] {
          EnumFacing.Axis.Y, EnumFacing.Axis.X, EnumFacing.Axis.Z
        };

        AXISES_LOG[EnumFacing.Axis.X.ordinal()] = new BlockLog.EnumAxis[] {
          BlockLog.EnumAxis.X, BlockLog.EnumAxis.Z, BlockLog.EnumAxis.Y, BlockLog.EnumAxis.NONE
        };
        AXISES_LOG[EnumFacing.Axis.Y.ordinal()] = new BlockLog.EnumAxis[] {
          BlockLog.EnumAxis.Z, BlockLog.EnumAxis.Y, BlockLog.EnumAxis.X, BlockLog.EnumAxis.NONE
        };
        AXISES_LOG[EnumFacing.Axis.Z.ordinal()] = new BlockLog.EnumAxis[] {
          BlockLog.EnumAxis.Y, BlockLog.EnumAxis.X, BlockLog.EnumAxis.Z, BlockLog.EnumAxis.NONE
        };

        AXISES_QUARTZ[EnumFacing.Axis.X.ordinal()] = new BlockQuartz.EnumType[] {
          BlockQuartz.EnumType.DEFAULT,
          BlockQuartz.EnumType.CHISELED,
          BlockQuartz.EnumType.LINES_Z,
          BlockQuartz.EnumType.LINES_X,
          BlockQuartz.EnumType.LINES_Y
        };
        AXISES_QUARTZ[EnumFacing.Axis.Y.ordinal()] = new BlockQuartz.EnumType[] {
          BlockQuartz.EnumType.DEFAULT,
          BlockQuartz.EnumType.CHISELED,
          BlockQuartz.EnumType.LINES_Y,
          BlockQuartz.EnumType.LINES_Z,
          BlockQuartz.EnumType.LINES_X
        };
        AXISES_QUARTZ[EnumFacing.Axis.Z.ordinal()] = new BlockQuartz.EnumType[] {
          BlockQuartz.EnumType.DEFAULT,
          BlockQuartz.EnumType.CHISELED,
          BlockQuartz.EnumType.LINES_X,
          BlockQuartz.EnumType.LINES_Y,
          BlockQuartz.EnumType.LINES_Z
        };
    }
    /**
     * Singleton constructor
     */
    private RotationHelper()
    {
    }

    public static boolean rotate(@Nullable final SchematicWorld world, @NotNull final EnumFacing axis, final boolean forced)
    {
        if (world == null)
        {
            return false;
        }

        try
        {
            @NotNull final Schematic schematicRotated = rotate(world.getSchematic(), axis, forced);

            updatePosition(world, axis);

            world.setSchematic(schematicRotated);

            world.getTileEntities().forEach(world::initializeTileEntity);

            return true;
        }
        catch (@NotNull final RotationException re)
        {
            Reference.logger.error(re.getMessage(), re);
        }
        catch (@NotNull final RuntimeException e)
        {
            Reference.logger.fatal("Something went wrong!", e);
        }

        return false;
    }

    @NotNull
    public static Schematic rotate(@NotNull final Schematic schematic, @NotNull final EnumFacing axis, final boolean forced) throws RotationException
    {
        @NotNull final Vec3i dimensionsRotated = rotateDimensions(axis, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        @NotNull final Schematic schematicRotated = new Schematic(schematic.getIcon(), dimensionsRotated.getX(), dimensionsRotated.getY(), dimensionsRotated.getZ());
        @NotNull final BlockPos.MutableBlockPos tmp = new BlockPos.MutableBlockPos();

        for (@NotNull final BlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1, schematic.getLength() - 1))
        {
            final IBlockState blockState = schematic.getBlockState(pos);
            final IBlockState blockStateRotated = rotateBlock(blockState, axis, forced);
            schematicRotated.setBlockState(rotatePos(pos, axis, dimensionsRotated, tmp), blockStateRotated);
        }

        @NotNull final List<TileEntity> tileEntities = schematic.getTileEntities();
        for (@NotNull final TileEntity tileEntity : tileEntities)
        {
            final BlockPos pos = tileEntity.getPos();
            tileEntity.setPos(new BlockPos(rotatePos(pos, axis, dimensionsRotated, tmp)));
            schematicRotated.setTileEntity(tileEntity.getPos(), tileEntity);
        }

        schematicRotated.setOffset(rotatePos(schematic.getOffset(), axis, dimensionsRotated, tmp));

        return schematicRotated;
    }

    private static void updatePosition(@NotNull final SchematicWorld world, @NotNull final EnumFacing axis)
    {
        switch (axis)
        {
            case DOWN:
            case UP:
                final int udOffset = (world.getWidth() - world.getLength()) / 2;
                BlockPosUtil.set(world.position, world.position.add(udOffset, 0, -udOffset));
                break;

            case NORTH:
            case SOUTH:
                final int nsOffset = (world.getWidth() - world.getHeight()) / 2;
                BlockPosUtil.set(world.position, world.position.add(nsOffset, -nsOffset, 0));
                break;

            case WEST:
            case EAST:
                final int weOffset = (world.getHeight() - world.getLength()) / 2;
                BlockPosUtil.set(world.position, world.position.add(0, weOffset, -weOffset));
                break;
            default:
                throw new IllegalArgumentException("Invalid axis.");
        }
    }

    @NotNull
    private static Vec3i rotateDimensions(@NotNull final EnumFacing axis, final int width, final int height, final int length) throws RotationException
    {
        switch (axis)
        {
            case DOWN:
            case UP:
                return new Vec3i(length, height, width);

            case NORTH:
            case SOUTH:
                return new Vec3i(height, width, length);

            case WEST:
            case EAST:
                return new Vec3i(width, length, height);
            default:
                throw new RotationException("'%s' is not a valid axis!", axis.getName());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState rotateBlock(@NotNull final IBlockState blockState, @NotNull final EnumFacing axisRotation, final boolean forced) throws RotationException
    {
        @Nullable final IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
        if (propertyFacing instanceof PropertyDirection)
        {
            final Comparable value = blockState.getValue(propertyFacing);
            if (value instanceof EnumFacing)
            {
                final EnumFacing facing = getRotatedFacing(axisRotation, (EnumFacing) value);
                if (propertyFacing.getAllowedValues().contains(facing))
                {
                    return blockState.withProperty(propertyFacing, facing);
                }
            }
        }
        else if (propertyFacing instanceof PropertyEnum)
        {
            if (BlockLever.EnumOrientation.class.isAssignableFrom(propertyFacing.getValueClass()))
            {
                @NotNull final BlockLever.EnumOrientation orientation = (BlockLever.EnumOrientation) blockState.getValue(propertyFacing);
                final BlockLever.EnumOrientation orientationRotated = getRotatedLeverFacing(axisRotation, orientation);
                if (propertyFacing.getAllowedValues().contains(orientationRotated))
                {
                    return blockState.withProperty(propertyFacing, orientationRotated);
                }
            }
        }
        else if (propertyFacing != null)
        {
            Reference.logger.error("'{}': found 'facing' property with unknown type {}",
              BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), propertyFacing.getClass().getSimpleName());
        }

        @Nullable final IProperty propertyAxis = BlockStateHelper.getProperty(blockState, "axis");
        if (propertyAxis instanceof PropertyEnum)
        {
            if (EnumFacing.Axis.class.isAssignableFrom(propertyAxis.getValueClass()))
            {
                @NotNull final EnumFacing.Axis axis = (EnumFacing.Axis) blockState.getValue(propertyAxis);
                final EnumFacing.Axis axisRotated = getRotatedAxis(axisRotation, axis);
                return blockState.withProperty(propertyAxis, axisRotated);
            }

            if (BlockLog.EnumAxis.class.isAssignableFrom(propertyAxis.getValueClass()))
            {
                @NotNull final BlockLog.EnumAxis axis = (BlockLog.EnumAxis) blockState.getValue(propertyAxis);
                final BlockLog.EnumAxis axisRotated = getRotatedLogAxis(axisRotation, axis);
                return blockState.withProperty(propertyAxis, axisRotated);
            }
        }
        else if (propertyAxis != null)
        {
            Reference.logger.error("'{}': found 'axis' property with unknown type {}",
              BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), propertyAxis.getClass().getSimpleName());
        }

        @Nullable final IProperty propertyVariant = BlockStateHelper.getProperty(blockState, "variant");
        if (propertyVariant instanceof PropertyEnum && BlockQuartz.EnumType.class.isAssignableFrom(propertyVariant.getValueClass()))
        {
            @NotNull final BlockQuartz.EnumType type = (BlockQuartz.EnumType) blockState.getValue(propertyVariant);
            final BlockQuartz.EnumType typeRotated = getRotatedQuartzType(axisRotation, type);
            return blockState.withProperty(propertyVariant, typeRotated);
        }

        if (!forced && (propertyFacing != null || propertyAxis != null))
        {
            throw new RotationException("'%s' cannot be rotated around '%s'", BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), axisRotation);
        }

        return blockState;
    }

    private static BlockPos rotatePos(@NotNull final BlockPos pos, @NotNull final EnumFacing axis, @NotNull final Vec3i dimensions, @NotNull final BlockPos.MutableBlockPos rotated)
      throws RotationException
    {
        switch (axis)
        {
            case DOWN:
                return rotated.setPos(pos.getZ(), pos.getY(), dimensions.getZ() - 1 - pos.getX());

            case UP:
                return rotated.setPos(dimensions.getX() - 1 - pos.getZ(), pos.getY(), pos.getX());

            case NORTH:
                return rotated.setPos(dimensions.getX() - 1 - pos.getY(), pos.getX(), pos.getZ());

            case SOUTH:
                return rotated.setPos(pos.getY(), dimensions.getY() - 1 - pos.getX(), pos.getZ());

            case WEST:
                return rotated.setPos(pos.getX(), dimensions.getY() - 1 - pos.getZ(), pos.getY());

            case EAST:
                return rotated.setPos(pos.getX(), pos.getZ(), dimensions.getZ() - 1 - pos.getY());
            default:
                throw new RotationException("'%s' is not a valid axis!", axis.getName());
        }
    }

    private static EnumFacing getRotatedFacing(@NotNull final EnumFacing source, @NotNull final EnumFacing side)
    {
        return FACINGS[source.ordinal()][side.ordinal()];
    }

    private static BlockLever.EnumOrientation getRotatedLeverFacing(@NotNull final EnumFacing source, @NotNull final BlockLever.EnumOrientation side)
    {
        final EnumFacing facing;
        if (source.getAxis().isVertical() && side.getFacing().getAxis().isVertical())
        {
            facing = side == BlockLever.EnumOrientation.UP_X || side == BlockLever.EnumOrientation.DOWN_X ? EnumFacing.NORTH : EnumFacing.WEST;
        }
        else
        {
            facing = side.getFacing();
        }

        final EnumFacing facingRotated = getRotatedFacing(source, side.getFacing());
        return BlockLever.EnumOrientation.forFacings(facingRotated, facing);
    }

    private static EnumFacing.Axis getRotatedAxis(@NotNull final EnumFacing source, @NotNull final EnumFacing.Axis axis)
    {
        return AXISES[source.getAxis().ordinal()][axis.ordinal()];
    }

    private static BlockLog.EnumAxis getRotatedLogAxis(@NotNull final EnumFacing source, @NotNull final BlockLog.EnumAxis axis)
    {
        return AXISES_LOG[source.getAxis().ordinal()][axis.ordinal()];
    }

    private static BlockQuartz.EnumType getRotatedQuartzType(@NotNull final EnumFacing source, @NotNull final BlockQuartz.EnumType type)
    {
        return AXISES_QUARTZ[source.getAxis().ordinal()][type.ordinal()];
    }

    public static class RotationException extends Exception
    {
        public static final long serialVersionUID = 2093580928750972354L;
        RotationException(@NotNull final String message, final Object... args)
        {
            super(String.format(message, args));
        }
    }
}
