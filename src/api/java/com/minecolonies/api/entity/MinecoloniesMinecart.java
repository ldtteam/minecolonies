package com.minecolonies.api.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.util.*;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;

/**
 * Special minecolonies minecart that doesn't collide.
 */
public class MinecoloniesMinecart extends AbstractMinecart
{
    /**
     * Railshape matrix.
     */
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> MATRIX = Util.make(Maps.newEnumMap(RailShape.class), (entry) ->
    {
        Vec3i westVec = Direction.WEST.getNormal();
        Vec3i eastVec = Direction.EAST.getNormal();
        Vec3i northVec = Direction.NORTH.getNormal();
        Vec3i southVec = Direction.SOUTH.getNormal();

        entry.put(RailShape.NORTH_SOUTH, Pair.of(northVec, southVec));
        entry.put(RailShape.EAST_WEST, Pair.of(westVec, eastVec));
        entry.put(RailShape.ASCENDING_EAST, Pair.of(westVec.below(), eastVec));
        entry.put(RailShape.ASCENDING_WEST, Pair.of(westVec, eastVec.below()));
        entry.put(RailShape.ASCENDING_NORTH, Pair.of(northVec, southVec.below()));
        entry.put(RailShape.ASCENDING_SOUTH, Pair.of(northVec.below(), southVec));
        entry.put(RailShape.SOUTH_EAST, Pair.of(southVec, eastVec));
        entry.put(RailShape.SOUTH_WEST, Pair.of(southVec, westVec));
        entry.put(RailShape.NORTH_WEST, Pair.of(northVec, westVec));
        entry.put(RailShape.NORTH_EAST, Pair.of(northVec, eastVec));
    });

    /**
     * Constructor to create the minecart.
     *
     * @param type  the entity type.
     * @param world the world.
     */
    public MinecoloniesMinecart(final EntityType<?> type, final Level world)
    {
        super(type, world);
    }

    @Override
    protected void moveAlongTrack(BlockPos pos, BlockState state)
    {
        this.fallDistance = 0.0F;
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        Vec3 posVec = this.getPos(x, y, z);
        y = pos.getY();
        boolean isPowered = false;
        boolean flag = false;
        BaseRailBlock abstractrailblock = (BaseRailBlock) state.getBlock();
        if (abstractrailblock instanceof PoweredRailBlock && !((PoweredRailBlock) abstractrailblock).isActivatorRail())
        {
            isPowered = state.getValue(PoweredRailBlock.POWERED);
            flag = !isPowered;
        }

        RailShape railshape = ((BaseRailBlock) state.getBlock()).getRailDirection(state, this.level, pos, this);
        switch (railshape)
        {
            case ASCENDING_EAST:
            case ASCENDING_WEST:
            case ASCENDING_NORTH:
            case ASCENDING_SOUTH:
                ++y;
                break;

            default:
                break;
        }

        Vec3 motion = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> pair = getShapeMatrix(railshape);
        Vec3i vecIn = pair.getFirst();
        Vec3i vecOut = pair.getSecond();
        double xDif = (vecOut.getX() - vecIn.getX());
        double zDif = (vecOut.getZ() - vecIn.getZ());
        double difSq = Math.sqrt(xDif * xDif + zDif * zDif);
        double difMotion = motion.x * xDif + motion.z * zDif;
        if (difMotion < 0.0D)
        {
            xDif = -xDif;
            zDif = -zDif;
        }

        double veloc = Math.min(2.0D, Math.sqrt(motion.horizontalDistanceSqr()));
        motion = new Vec3(veloc * xDif / difSq, motion.y, veloc * zDif / difSq);
        this.setDeltaMovement(motion);

        if (flag && shouldDoRailFunctions())
        {
            double tempMot = Math.sqrt(this.getDeltaMovement().horizontalDistanceSqr());
            if (tempMot < 0.03D)
            {
                this.setDeltaMovement(Vec3.ZERO);
            }
            else
            {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
            }
        }

        double xInDif = (double) pos.getX() + 0.5D + (double) vecIn.getX() * 0.5D;
        double zInDif = (double) pos.getZ() + 0.5D + (double) vecIn.getZ() * 0.5D;
        double xOutDif = (double) pos.getX() + 0.5D + (double) vecOut.getX() * 0.5D;
        double zOutDif = (double) pos.getZ() + 0.5D + (double) vecOut.getZ() * 0.5D;
        xDif = xOutDif - xInDif;
        zDif = zOutDif - zInDif;
        double xzDif;
        if (xDif == 0.0D)
        {
            xzDif = z - (double) pos.getZ();
        }
        else if (zDif == 0.0D)
        {
            xzDif = x - (double) pos.getX();
        }
        else
        {
            double d15 = x - xInDif;
            double d16 = z - zInDif;
            xzDif = (d15 * xDif + d16 * zDif) * 2.0D;
        }

        x = xInDif + xDif * xzDif;
        z = zInDif + zDif * xzDif;
        this.setPos(x, y, z);
        this.moveMinecartOnRail(pos);
        if (vecIn.getY() != 0 && Mth.floor(this.getX()) - pos.getX() == vecIn.getX() && Mth.floor(this.getZ()) - pos.getZ() == vecIn.getZ())
        {
            this.setPos(this.getX(), this.getY() + (double) vecIn.getY(), this.getZ());
        }
        else if (vecOut.getY() != 0 && Mth.floor(this.getX()) - pos.getX() == vecOut.getX() && Mth.floor(this.getZ()) - pos.getZ() == vecOut.getZ())
        {
            this.setPos(this.getX(), this.getY() + (double) vecOut.getY(), this.getZ());
        }

        this.applyNaturalSlowdown();
        Vec3 newPos = this.getPos(this.getX(), this.getY(), this.getZ());
        if (newPos != null && posVec != null)
        {
            double yMot = (posVec.y - newPos.y) * 0.05D;
            Vec3 tempMot = this.getDeltaMovement();
            double tempVeloc = Math.sqrt(tempMot.horizontalDistanceSqr());
            if (tempVeloc > 0.0D)
            {
                this.setDeltaMovement(tempMot.multiply((tempVeloc + yMot) / tempVeloc, 1.0D, (tempVeloc + yMot) / tempVeloc));
            }

            this.setPos(this.getX(), newPos.y, this.getZ());
        }

        int xFloor = Mth.floor(this.getX());
        int zFloor = Mth.floor(this.getZ());
        if (xFloor != pos.getX() || zFloor != pos.getZ())
        {
            Vec3 tempMot = this.getDeltaMovement();
            double temoVeloc = Math.sqrt(tempMot.horizontalDistanceSqr());
            this.setDeltaMovement(temoVeloc * (double) (xFloor - pos.getX()), tempMot.y, temoVeloc * (double) (zFloor - pos.getZ()));
        }

        if (shouldDoRailFunctions())
        {
            ((BaseRailBlock) state.getBlock()).onMinecartPass(state, level, pos, this);
        }

        if (isPowered && shouldDoRailFunctions())
        {
            Vec3 tempMot = this.getDeltaMovement();
            double tempVeloc = Math.sqrt(tempMot.horizontalDistanceSqr());
            if (tempVeloc > 0.01D)
            {
                this.setDeltaMovement(tempMot.add(tempMot.x / tempVeloc * 0.06D, 0.0D, tempMot.z / tempVeloc * 0.06D));
            }
            else
            {
                Vec3 mot = this.getDeltaMovement();
                double tempX = mot.x;
                double tempZ = mot.z;
                if (railshape == RailShape.EAST_WEST)
                {
                    if (this.isNormalCube(pos.west()))
                    {
                        tempX = 0.02D;
                    }
                    else if (this.isNormalCube(pos.east()))
                    {
                        tempX = -0.02D;
                    }
                }
                else
                {
                    if (railshape != RailShape.NORTH_SOUTH)
                    {
                        return;
                    }

                    if (this.isNormalCube(pos.north()))
                    {
                        tempZ = 0.02D;
                    }
                    else if (this.isNormalCube(pos.south()))
                    {
                        tempZ = -0.02D;
                    }
                }

                this.setDeltaMovement(tempX, mot.y, tempZ);
            }
        }
    }

    private boolean isNormalCube(BlockPos pos)
    {
        return this.level.getBlockState(pos).isRedstoneConductor(this.level, pos);
    }

    private static Pair<Vec3i, Vec3i> getShapeMatrix(RailShape p_226573_0_)
    {
        return MATRIX.get(p_226573_0_);
    }

    @Override
    public InteractionResult interact(final Player p_184230_1_, final InteractionHand p_184230_2_)
    {
        return InteractionResult.FAIL;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @NotNull
    public AbstractMinecart.Type getMinecartType()
    {
        return AbstractMinecart.Type.RIDEABLE;
    }

    @Override
    public void push(@NotNull final Entity entityIn)
    {
        // Do nothing
    }

    @Override
    public void playerTouch(final Player entityIn)
    {
        // Do nothing
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @NotNull
    @Override
    public Vec3 collide(Vec3 vec)
    {
        final AABB axisalignedbb = this.getBoundingBox();
        final CollisionContext iselectioncontext = CollisionContext.of(this);
        final VoxelShape voxelshape = this.level.getWorldBorder().getCollisionShape();
        final Stream<VoxelShape> stream = Shapes.joinIsNotEmpty(voxelshape, Shapes.create(axisalignedbb.deflate(1.0E-7D)), BooleanOp.AND) ? Stream.empty() : Stream.of(voxelshape);
        final RewindableStream<VoxelShape> reuseablestream = new RewindableStream<>(stream);
        final Vec3 vector3d = vec.lengthSqr() == 0.0D ? vec : collideBoundingBoxHeuristically(this, vec, axisalignedbb, this.level, iselectioncontext, reuseablestream);
        final boolean xDif = vec.x != vector3d.x;
        final boolean yDif = vec.y != vector3d.y;
        final boolean zDif = vec.z != vector3d.z;
        final boolean groundDif = this.onGround || yDif && vec.y < 0.0D;
        if (this.maxUpStep > 0.0F && groundDif && (xDif || zDif)) {
            Vec3 vector3d1 = collideBoundingBoxHeuristically(this, new Vec3(vec.x, (double)this.maxUpStep, vec.z), axisalignedbb, this.level, iselectioncontext, reuseablestream);
            final Vec3 vector3d2 = collideBoundingBoxHeuristically(this, new Vec3(0.0D, (double)this.maxUpStep, 0.0D), axisalignedbb.expandTowards(vec.x, 0.0D, vec.z), this.level, iselectioncontext, reuseablestream);
            if (vector3d2.y < (double)this.maxUpStep) {
                Vec3 vector3d3 = collideBoundingBoxHeuristically(this, new Vec3(vec.x, 0.0D, vec.z), axisalignedbb.move(vector3d2), this.level, iselectioncontext, reuseablestream).add(vector3d2);
                if (vector3d3.horizontalDistanceSqr() > vector3d1.horizontalDistanceSqr()) {
                    vector3d1 = vector3d3;
                }
            }

            if (vector3d1.horizontalDistanceSqr() > vector3d.horizontalDistanceSqr()) {
                return vector3d1.add(collideBoundingBoxHeuristically(this, new Vec3(0.0D, -vector3d1.y + vec.y, 0.0D), axisalignedbb.move(vector3d1), this.level, iselectioncontext, reuseablestream));
            }
        }

        return vector3d;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.tickCount % 20 == 19 && getPassengers().isEmpty())
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @NotNull
    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
