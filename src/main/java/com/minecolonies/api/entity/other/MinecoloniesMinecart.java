package com.minecolonies.api.entity.other;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Special minecolonies minecart that doesn't collide.
 */
public class MinecoloniesMinecart extends Minecart
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

        RailShape railshape = ((BaseRailBlock) state.getBlock()).getRailDirection(state, this.level(), pos, this);
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
            ((BaseRailBlock) state.getBlock()).onMinecartPass(state, level(), pos, this);
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

    @Override
    public void destroy(final DamageSource source)
    {
        this.kill();
    }

    private boolean isNormalCube(BlockPos pos)
    {
        return this.level().getBlockState(pos).isRedstoneConductor(this.level(), pos);
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

    @Override
    public boolean canCollideWith(final Entity p_38168_)
    {
        return false;
    }

    @Override
    public void tick()
    {
        if (this.getHurtTime() > 0)
        {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F)
        {
            this.setDamage(this.getDamage() - 1.0F);
        }

        this.checkBelowWorld();
        this.handleNetherPortal();
        if (this.level().isClientSide)
        {
            if (this.lSteps > 0)
            {
                double d5 = this.getX() + (this.lx - this.getX()) / (double) this.lSteps;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double d7 = this.getZ() + (this.lz - this.getZ()) / (double) this.lSteps;
                double d2 = Mth.wrapDegrees(this.lyr - (double) this.getYRot());
                this.setYRot(this.getYRot() + (float) d2 / (float) this.lSteps);
                this.setXRot(this.getXRot() + (float) (this.lxr - (double) this.getXRot()) / (float) this.lSteps);
                --this.lSteps;
                this.setPos(d5, d6, d7);
                this.setRot(this.getYRot(), this.getXRot());
            }
            else
            {
                this.reapplyPosition();
                this.setRot(this.getYRot(), this.getXRot());
            }
        }
        else
        {
            if (!this.isNoGravity())
            {
                double d0 = this.isInWater() ? -0.005D : -0.04D;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d0, 0.0D));
            }

            int k = Mth.floor(this.getX());
            int i = Mth.floor(this.getY());
            int j = Mth.floor(this.getZ());
            if (this.level().getBlockState(new BlockPos(k, i - 1, j)).is(BlockTags.RAILS))
            {
                --i;
            }

            BlockPos blockpos = new BlockPos(k, i, j);
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (canUseRail() && BaseRailBlock.isRail(blockstate))
            {
                this.moveAlongTrack(blockpos, blockstate);
                if (blockstate.getBlock() instanceof PoweredRailBlock && ((PoweredRailBlock) blockstate.getBlock()).isActivatorRail())
                {
                    this.activateMinecart(k, i, j, blockstate.getValue(PoweredRailBlock.POWERED));
                }
            }
            else
            {
                this.comeOffTrack();
            }

            this.checkInsideBlocks();
            this.setXRot(0.0F);
            double d1 = this.xo - this.getX();
            double d3 = this.zo - this.getZ();
            if (d1 * d1 + d3 * d3 > 0.001D)
            {
                this.setYRot((float) (Mth.atan2(d3, d1) * 180.0D / Math.PI));
                if (this.flipped)
                {
                    this.setYRot(this.getYRot() + 180.0F);
                }
            }

            double d4 = (double) Mth.wrapDegrees(this.getYRot() - this.yRotO);
            if (d4 < -170.0D || d4 >= 170.0D)
            {
                this.setYRot(this.getYRot() + 180.0F);
                this.flipped = !this.flipped;
            }

            this.setRot(this.getYRot(), this.getXRot());
            this.updateInWaterStateAndDoFluidPushing();
            if (this.isInLava())
            {
                this.lavaHurt();
                this.fallDistance *= 0.5F;
            }

            this.firstTick = false;
        }

        if (this.tickCount % 20 == 19 && getPassengers().isEmpty())
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
