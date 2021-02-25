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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
        Vector3i westVec = Direction.WEST.getDirectionVec();
        Vector3i eastVec = Direction.EAST.getDirectionVec();
        Vector3i northVec = Direction.NORTH.getDirectionVec();
        Vector3i southVec = Direction.SOUTH.getDirectionVec();

        entry.put(RailShape.NORTH_SOUTH, Pair.of(northVec, southVec));
        entry.put(RailShape.EAST_WEST, Pair.of(westVec, eastVec));
        entry.put(RailShape.ASCENDING_EAST, Pair.of(westVec.down(), eastVec));
        entry.put(RailShape.ASCENDING_WEST, Pair.of(westVec, eastVec.down()));
        entry.put(RailShape.ASCENDING_NORTH, Pair.of(northVec, southVec.down()));
        entry.put(RailShape.ASCENDING_SOUTH, Pair.of(northVec.down(), southVec));
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
        double x = this.getPosX();
        double y = this.getPosY();
        double z = this.getPosZ();
        Vector3d posVec = this.getPos(x, y, z);
        y = pos.getY();
        boolean isPowered = false;
        boolean flag = false;
        AbstractRailBlock abstractrailblock = (AbstractRailBlock) state.getBlock();
        if (abstractrailblock instanceof PoweredRailBlock && !((PoweredRailBlock) abstractrailblock).isActivatorRail())
        {
            isPowered = state.get(PoweredRailBlock.POWERED);
            flag = !isPowered;
        }

        RailShape railshape = ((AbstractRailBlock) state.getBlock()).getRailDirection(state, this.world, pos, this);
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

        Vector3d motion = this.getMotion();
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

        double veloc = Math.min(2.0D, Math.sqrt(horizontalMag(motion)));
        motion = new Vector3d(veloc * xDif / difSq, motion.y, veloc * zDif / difSq);
        this.setMotion(motion);

        if (flag && shouldDoRailFunctions())
        {
            double tempMot = Math.sqrt(horizontalMag(this.getMotion()));
            if (tempMot < 0.03D)
            {
                this.setMotion(Vector3d.ZERO);
            }
            else
            {
                this.setMotion(this.getMotion().mul(0.5D, 0.0D, 0.5D));
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
        this.setPosition(x, y, z);
        this.moveMinecartOnRail(pos);
        if (vecIn.getY() != 0 && MathHelper.floor(this.getPosX()) - pos.getX() == vecIn.getX() && MathHelper.floor(this.getPosZ()) - pos.getZ() == vecIn.getZ())
        {
            this.setPosition(this.getPosX(), this.getPosY() + (double) vecIn.getY(), this.getPosZ());
        }
        else if (vecOut.getY() != 0 && MathHelper.floor(this.getPosX()) - pos.getX() == vecOut.getX() && MathHelper.floor(this.getPosZ()) - pos.getZ() == vecOut.getZ())
        {
            this.setPosition(this.getPosX(), this.getPosY() + (double) vecOut.getY(), this.getPosZ());
        }

        this.applyDrag();
        Vector3d newPos = this.getPos(this.getPosX(), this.getPosY(), this.getPosZ());
        if (newPos != null && posVec != null)
        {
            double yMot = (posVec.y - newPos.y) * 0.05D;
            Vector3d tempMot = this.getMotion();
            double tempVeloc = Math.sqrt(horizontalMag(tempMot));
            if (tempVeloc > 0.0D)
            {
                this.setMotion(tempMot.mul((tempVeloc + yMot) / tempVeloc, 1.0D, (tempVeloc + yMot) / tempVeloc));
            }

            this.setPosition(this.getPosX(), newPos.y, this.getPosZ());
        }

        int xFloor = MathHelper.floor(this.getPosX());
        int zFloor = MathHelper.floor(this.getPosZ());
        if (xFloor != pos.getX() || zFloor != pos.getZ())
        {
            Vector3d tempMot = this.getMotion();
            double temoVeloc = Math.sqrt(horizontalMag(tempMot));
            this.setMotion(temoVeloc * (double) (xFloor - pos.getX()), tempMot.y, temoVeloc * (double) (zFloor - pos.getZ()));
        }

        if (shouldDoRailFunctions())
        {
            ((AbstractRailBlock) state.getBlock()).onMinecartPass(state, world, pos, this);
        }

        if (isPowered && shouldDoRailFunctions())
        {
            Vector3d tempMot = this.getMotion();
            double tempVeloc = Math.sqrt(horizontalMag(tempMot));
            if (tempVeloc > 0.01D)
            {
                this.setMotion(tempMot.add(tempMot.x / tempVeloc * 0.06D, 0.0D, tempMot.z / tempVeloc * 0.06D));
            }
            else
            {
                Vector3d mot = this.getMotion();
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

                this.setMotion(tempX, mot.y, tempZ);
            }
        }
    }

    private boolean isNormalCube(BlockPos pos)
    {
        return this.world.getBlockState(pos).isNormalCube(this.world, pos);
    }

    private static Pair<Vector3i, Vector3i> getShapeMatrix(RailShape p_226573_0_)
    {
        return MATRIX.get(p_226573_0_);
    }

    @Override
    public ActionResultType processInitialInteract(final PlayerEntity p_184230_1_, final Hand p_184230_2_)
    {
        return ActionResultType.FAIL;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @NotNull
    public AbstractMinecartEntity.Type getMinecartType()
    {
        return AbstractMinecartEntity.Type.RIDEABLE;
    }

    @Override
    public void applyEntityCollision(@NotNull final Entity entityIn)
    {
        // Do nothing
    }

    @Override
    public void onCollideWithPlayer(final PlayerEntity entityIn)
    {
        // Do nothing
    }

    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @NotNull
    @Override
    public Vector3d getAllowedMovement(Vector3d vec)
    {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox();
        final ISelectionContext iselectioncontext = ISelectionContext.forEntity(this);
        final VoxelShape voxelshape = this.world.getWorldBorder().getShape();
        final Stream<VoxelShape> stream = VoxelShapes.compare(voxelshape, VoxelShapes.create(axisalignedbb.shrink(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
        final ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(stream);
        final Vector3d vector3d = vec.lengthSquared() == 0.0D ? vec : collideBoundingBoxHeuristically(this, vec, axisalignedbb, this.world, iselectioncontext, reuseablestream);
        final boolean xDif = vec.x != vector3d.x;
        final boolean yDif = vec.y != vector3d.y;
        final boolean zDif = vec.z != vector3d.z;
        final boolean groundDif = this.onGround || yDif && vec.y < 0.0D;
        if (this.stepHeight > 0.0F && groundDif && (xDif || zDif)) {
            Vector3d vector3d1 = collideBoundingBoxHeuristically(this, new Vector3d(vec.x, (double)this.stepHeight, vec.z), axisalignedbb, this.world, iselectioncontext, reuseablestream);
            final Vector3d vector3d2 = collideBoundingBoxHeuristically(this, new Vector3d(0.0D, (double)this.stepHeight, 0.0D), axisalignedbb.expand(vec.x, 0.0D, vec.z), this.world, iselectioncontext, reuseablestream);
            if (vector3d2.y < (double)this.stepHeight) {
                Vector3d vector3d3 = collideBoundingBoxHeuristically(this, new Vector3d(vec.x, 0.0D, vec.z), axisalignedbb.offset(vector3d2), this.world, iselectioncontext, reuseablestream).add(vector3d2);
                if (horizontalMag(vector3d3) > horizontalMag(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }

            if (horizontalMag(vector3d1) > horizontalMag(vector3d)) {
                return vector3d1.add(collideBoundingBoxHeuristically(this, new Vector3d(0.0D, -vector3d1.y + vec.y, 0.0D), axisalignedbb.offset(vector3d1), this.world, iselectioncontext, reuseablestream));
            }
        }

        return vector3d;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.ticksExisted % 20 == 19 && getPassengers().isEmpty())
        {
            this.remove();
        }
    }

    @NotNull
    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
