package com.schematica.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

public class MBlockPos extends BlockPos {
    public int x;
    public int y;
    public int z;

    public MBlockPos() {
        this(0, 0, 0);
    }

    public MBlockPos(final Entity source) {
        this(source.posX, source.posY, source.posZ);
    }

    public MBlockPos(final Vec3 source) {
        this(source.xCoord, source.yCoord, source.zCoord);
    }

    public MBlockPos(final Vec3i source) {
        this(source.getX(), source.getY(), source.getZ());
    }

    public MBlockPos(final double x, final double y, final double z) {
        this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public MBlockPos(final int x, final int y, final int z) {
        super(0, 0, 0);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MBlockPos set(final Entity source) {
        return set(source.posX, source.posY, source.posZ);
    }

    public MBlockPos set(final Vec3 source) {
        return set(source.xCoord, source.yCoord, source.zCoord);
    }

    public MBlockPos set(final Vec3i source) {
        return set(source.getX(), source.getY(), source.getZ());
    }

    public MBlockPos set(final double x, final double y, final double z) {
        return set(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public MBlockPos set(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public MBlockPos add(final Vec3i vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public MBlockPos add(final double x, final double y, final double z) {
        return add(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    @Override
    public MBlockPos add(final int x, final int y, final int z) {
        return new MBlockPos(this.x + x, this.y + y, this.z + z);
    }

    public MBlockPos multiply(final int factor) {
        return new MBlockPos(this.x * factor, this.y * factor, this.z * factor);
    }

    @SuppressWarnings("override")
    public MBlockPos subtract(final Vec3i vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    public MBlockPos subtract(final double x, final double y, final double z) {
        return subtract(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public MBlockPos subtract(final int x, final int y, final int z) {
        return new MBlockPos(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public MBlockPos up() {
        return up(1);
    }

    @Override
    public MBlockPos up(final int n) {
        return offset(EnumFacing.UP, n);
    }

    @Override
    public MBlockPos down() {
        return down(1);
    }

    @Override
    public MBlockPos down(final int n) {
        return offset(EnumFacing.DOWN, n);
    }

    @Override
    public MBlockPos north() {
        return north(1);
    }

    @Override
    public MBlockPos north(final int n) {
        return offset(EnumFacing.NORTH, n);
    }

    @Override
    public MBlockPos south() {
        return south(1);
    }

    @Override
    public MBlockPos south(final int n) {
        return offset(EnumFacing.SOUTH, n);
    }

    @Override
    public MBlockPos west() {
        return west(1);
    }

    @Override
    public MBlockPos west(final int n) {
        return offset(EnumFacing.WEST, n);
    }

    @Override
    public MBlockPos east() {
        return east(1);
    }

    @Override
    public MBlockPos east(final int n) {
        return offset(EnumFacing.EAST, n);
    }

    @Override
    public MBlockPos offset(final EnumFacing facing) {
        return offset(facing, 1);
    }

    @Override
    public MBlockPos offset(final EnumFacing facing, final int n) {
        return new MBlockPos(this.x + facing.getFrontOffsetX() * n, this.y + facing.getFrontOffsetY() * n, this.z + facing.getFrontOffsetZ() * n);
    }

    @Override
    public MBlockPos crossProduct(final Vec3i vec) {
        return new MBlockPos(this.y * vec.getZ() - this.z * vec.getY(), this.z * vec.getX() - this.x * vec.getZ(), this.x * vec.getY() - this.y * vec.getX());
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Deprecated
    public static Iterable<MBlockPos> getAllInRange(final BlockPos from, final BlockPos to) {
        return BlockPosHelper.getAllInBox(from, to);
    }
}
