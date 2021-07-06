package com.minecolonies.api.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Special minecolonies minecart that doesn't collide.
 */
public class MinecoloniesMinecart extends AbstractMinecartEntity
{
    /**
     * Railshape matrix.
     */
    private static final Map<RailShape, Pair<Vector3i, Vector3i>> MATRIX = Util.make(Maps.newEnumMap(RailShape.class), (entry) ->
    {
        Vector3i westVec = Direction.WEST.getNormal();
        Vector3i eastVec = Direction.EAST.getNormal();
        Vector3i northVec = Direction.NORTH.getNormal();
        Vector3i southVec = Direction.SOUTH.getNormal();

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
    public MinecoloniesMinecart(final EntityType<?> type, final World world)
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
        Vector3d posVec = this.getPos(x, y, z);
        y = pos.getY();
        boolean isPowered = false;
        boolean flag = false;
        AbstractRailBlock abstractrailblock = (AbstractRailBlock) state.getBlock();
        if (abstractrailblock instanceof PoweredRailBlock && !((PoweredRailBlock) abstractrailblock).isActivatorRail())
        {
            isPowered = state.getValue(PoweredRailBlock.POWERED);
            flag = !isPowered;
        }

        RailShape railshape = ((AbstractRailBlock) state.getBlock()).getRailDirection(state, this.level, pos, this);
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

        Vector3d motion = this.getDeltaMovement();
        Pair<Vector3i, Vector3i> pair = getShapeMatrix(railshape);
        Vector3i vecIn = pair.getFirst();
        Vector3i vecOut = pair.getSecond();
        double xDif = (vecOut.getX() - vecIn.getX());
        double zDif = (vecOut.getZ() - vecIn.getZ());
        double difSq = Math.sqrt(xDif * xDif + zDif * zDif);
        double difMotion = motion.x * xDif + motion.z * zDif;
        if (difMotion < 0.0D)
        {
            xDif = -xDif;
            zDif = -zDif;
        }

        double veloc = Math.min(2.0D, Math.sqrt(getHorizontalDistanceSqr(motion)));
        motion = new Vector3d(veloc * xDif / difSq, motion.y, veloc * zDif / difSq);
        this.setDeltaMovement(motion);

        if (flag && shouldDoRailFunctions())
        {
            double tempMot = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
            if (tempMot < 0.03D)
            {
                this.setDeltaMovement(Vector3d.ZERO);
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
        if (vecIn.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == vecIn.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == vecIn.getZ())
        {
            this.setPos(this.getX(), this.getY() + (double) vecIn.getY(), this.getZ());
        }
        else if (vecOut.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == vecOut.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == vecOut.getZ())
        {
            this.setPos(this.getX(), this.getY() + (double) vecOut.getY(), this.getZ());
        }

        this.applyNaturalSlowdown();
        Vector3d newPos = this.getPos(this.getX(), this.getY(), this.getZ());
        if (newPos != null && posVec != null)
        {
            double yMot = (posVec.y - newPos.y) * 0.05D;
            Vector3d tempMot = this.getDeltaMovement();
            double tempVeloc = Math.sqrt(getHorizontalDistanceSqr(tempMot));
            if (tempVeloc > 0.0D)
            {
                this.setDeltaMovement(tempMot.multiply((tempVeloc + yMot) / tempVeloc, 1.0D, (tempVeloc + yMot) / tempVeloc));
            }

            this.setPos(this.getX(), newPos.y, this.getZ());
        }

        int xFloor = MathHelper.floor(this.getX());
        int zFloor = MathHelper.floor(this.getZ());
        if (xFloor != pos.getX() || zFloor != pos.getZ())
        {
            Vector3d tempMot = this.getDeltaMovement();
            double temoVeloc = Math.sqrt(getHorizontalDistanceSqr(tempMot));
            this.setDeltaMovement(temoVeloc * (double) (xFloor - pos.getX()), tempMot.y, temoVeloc * (double) (zFloor - pos.getZ()));
        }

        if (shouldDoRailFunctions())
        {
            ((AbstractRailBlock) state.getBlock()).onMinecartPass(state, level, pos, this);
        }

        if (isPowered && shouldDoRailFunctions())
        {
            Vector3d tempMot = this.getDeltaMovement();
            double tempVeloc = Math.sqrt(getHorizontalDistanceSqr(tempMot));
            if (tempVeloc > 0.01D)
            {
                this.setDeltaMovement(tempMot.add(tempMot.x / tempVeloc * 0.06D, 0.0D, tempMot.z / tempVeloc * 0.06D));
            }
            else
            {
                Vector3d mot = this.getDeltaMovement();
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

    private static Pair<Vector3i, Vector3i> getShapeMatrix(RailShape p_226573_0_)
    {
        return MATRIX.get(p_226573_0_);
    }

    @Override
    public ActionResultType interact(final PlayerEntity p_184230_1_, final Hand p_184230_2_)
    {
        return ActionResultType.FAIL;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @NotNull
    public AbstractMinecartEntity.Type getMinecartType()
    {
        return AbstractMinecartEntity.Type.RIDEABLE;
    }

    @Override
    public void push(@NotNull final Entity entityIn)
    {
        // Do nothing
    }

    @Override
    public void playerTouch(final PlayerEntity entityIn)
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
    public Vector3d collide(Vector3d vec)
    {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox();
        final ISelectionContext iselectioncontext = ISelectionContext.of(this);
        final VoxelShape voxelshape = this.level.getWorldBorder().getCollisionShape();
        final Stream<VoxelShape> stream = VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(axisalignedbb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
        final ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(stream);
        final Vector3d vector3d = vec.lengthSqr() == 0.0D ? vec : collideBoundingBoxHeuristically(this, vec, axisalignedbb, this.level, iselectioncontext, reuseablestream);
        final boolean xDif = vec.x != vector3d.x;
        final boolean yDif = vec.y != vector3d.y;
        final boolean zDif = vec.z != vector3d.z;
        final boolean groundDif = this.onGround || yDif && vec.y < 0.0D;
        if (this.maxUpStep > 0.0F && groundDif && (xDif || zDif)) {
            Vector3d vector3d1 = collideBoundingBoxHeuristically(this, new Vector3d(vec.x, (double)this.maxUpStep, vec.z), axisalignedbb, this.level, iselectioncontext, reuseablestream);
            final Vector3d vector3d2 = collideBoundingBoxHeuristically(this, new Vector3d(0.0D, (double)this.maxUpStep, 0.0D), axisalignedbb.expandTowards(vec.x, 0.0D, vec.z), this.level, iselectioncontext, reuseablestream);
            if (vector3d2.y < (double)this.maxUpStep) {
                Vector3d vector3d3 = collideBoundingBoxHeuristically(this, new Vector3d(vec.x, 0.0D, vec.z), axisalignedbb.move(vector3d2), this.level, iselectioncontext, reuseablestream).add(vector3d2);
                if (getHorizontalDistanceSqr(vector3d3) > getHorizontalDistanceSqr(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }

            if (getHorizontalDistanceSqr(vector3d1) > getHorizontalDistanceSqr(vector3d)) {
                return vector3d1.add(collideBoundingBoxHeuristically(this, new Vector3d(0.0D, -vector3d1.y + vec.y, 0.0D), axisalignedbb.move(vector3d1), this.level, iselectioncontext, reuseablestream));
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
            this.remove();
        }
    }

    @NotNull
    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
