package com.schematica.client.util;

import com.schematica.core.util.BlockPosHelper;
import com.schematica.core.util.MBlockPos;
import com.schematica.api.ISchematic;
import com.schematica.block.state.BlockStateHelper;
import com.schematica.client.world.SchematicWorld;
import com.schematica.reference.Reference;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.List;

public class FlipHelper {
    public static final FlipHelper INSTANCE = new FlipHelper();

    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();

    public boolean flip(final SchematicWorld world, final EnumFacing axis, final boolean forced) {
        if (world == null) {
            return false;
        }

        try {
            final ISchematic schematic = world.getSchematic();
            final Schematic schematicFlipped = flip(schematic, axis, forced);

            world.setSchematic(schematicFlipped);

            for (final TileEntity tileEntity : world.getTileEntities()) {
                world.initializeTileEntity(tileEntity);
            }

            return true;
        } catch (final FlipException fe) {
            Reference.logger.error(fe.getMessage());
        } catch (final Exception e) {
            Reference.logger.fatal("Something went wrong!", e);
        }

        return false;
    }

    public Schematic flip(final ISchematic schematic, final EnumFacing axis, final boolean forced) throws FlipException {
        final Vec3i dimensionsFlipped = new Vec3i(schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        final Schematic schematicFlipped = new Schematic(schematic.getIcon(), dimensionsFlipped.getX(), dimensionsFlipped.getY(), dimensionsFlipped.getZ());
        final MBlockPos tmp = new MBlockPos();

        for (final MBlockPos pos : BlockPosHelper.getAllInBox(0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1, schematic.getLength() - 1)) {
            final IBlockState blockState = schematic.getBlockState(pos);
            final IBlockState blockStateFlipped = flipBlock(blockState, axis, forced);
            schematicFlipped.setBlockState(flipPos(pos, axis, dimensionsFlipped, tmp), blockStateFlipped);
        }

        final List<TileEntity> tileEntities = schematic.getTileEntities();
        for (final TileEntity tileEntity : tileEntities) {
            final BlockPos pos = tileEntity.getPos();
            tileEntity.setPos(new BlockPos(flipPos(pos, axis, dimensionsFlipped, tmp)));
            schematicFlipped.setTileEntity(tileEntity.getPos(), tileEntity);
        }

        return schematicFlipped;
    }

    private BlockPos flipPos(final BlockPos pos, final EnumFacing axis, final Vec3i dimensions, final MBlockPos flipped) throws FlipException {
        switch (axis) {
        case DOWN:
        case UP:
            return flipped.set(pos.getX(), dimensions.getY() - 1 - pos.getY(), pos.getZ());

        case NORTH:
        case SOUTH:
            return flipped.set(pos.getX(), pos.getY(), dimensions.getZ() - 1 - pos.getZ());

        case WEST:
        case EAST:
            return flipped.set(dimensions.getX() - 1 - pos.getX(), pos.getY(), pos.getZ());
        }

        throw new FlipException("'%s' is not a valid axis!", axis.getName());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private IBlockState flipBlock(final IBlockState blockState, final EnumFacing axis, final boolean forced) throws FlipException {
        final IProperty propertyFacing = BlockStateHelper.getProperty(blockState, "facing");
        if (propertyFacing instanceof PropertyDirection) {
            final Comparable value = blockState.getValue(propertyFacing);
            if (value instanceof EnumFacing) {
                final EnumFacing facing = getFlippedFacing(axis, (EnumFacing) value);
                if (propertyFacing.getAllowedValues().contains(facing)) {
                    return blockState.withProperty(propertyFacing, facing);
                }
            }
        } else if (propertyFacing instanceof PropertyEnum) {
            if (BlockLever.EnumOrientation.class.isAssignableFrom(propertyFacing.getValueClass())) {
                final BlockLever.EnumOrientation orientation = (BlockLever.EnumOrientation) blockState.getValue(propertyFacing);
                final BlockLever.EnumOrientation orientationRotated = getFlippedLeverFacing(axis, orientation);
                if (propertyFacing.getAllowedValues().contains(orientationRotated)) {
                    return blockState.withProperty(propertyFacing, orientationRotated);
                }
            }
        } else if (propertyFacing != null) {
            Reference.logger.error("'{}': found 'facing' property with unknown type {}", BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), propertyFacing.getClass().getSimpleName());
        }

        if (!forced && propertyFacing != null) {
            throw new FlipException("'%s' cannot be flipped across '%s'", BLOCK_REGISTRY.getNameForObject(blockState.getBlock()), axis);
        }

        return blockState;
    }

    private static EnumFacing getFlippedFacing(final EnumFacing axis, final EnumFacing side) {
        if (axis.getAxis() == side.getAxis()) {
            return side.getOpposite();
        }

        return side;
    }

    private static BlockLever.EnumOrientation getFlippedLeverFacing(final EnumFacing source, final BlockLever.EnumOrientation side) {
        if (source.getAxis() != side.getFacing().getAxis()) {
            return side;
        }

        final EnumFacing facing;
        if (side == BlockLever.EnumOrientation.UP_Z || side == BlockLever.EnumOrientation.DOWN_Z) {
            facing = EnumFacing.NORTH;
        } else if (side == BlockLever.EnumOrientation.UP_X || side == BlockLever.EnumOrientation.DOWN_X) {
            facing = EnumFacing.WEST;
        } else {
            facing = side.getFacing();
        }

        final EnumFacing facingFlipped = getFlippedFacing(source, side.getFacing());
        return BlockLever.EnumOrientation.forFacings(facingFlipped, facing);
    }

    public static class FlipException extends Exception {
        public FlipException(final String message, final Object... args) {
            super(String.format(message, args));
        }
    }
}
