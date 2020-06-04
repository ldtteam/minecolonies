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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Special minecolonies minecart that doesn't collide.
 */
public class MinecoloniesMinecart extends AbstractMinecartEntity
{
    /**
     * Railshape matrix.
     */
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> MATRIX = Util.make(Maps.newEnumMap(RailShape.class), (entry) -> {
        Vec3i westVec = Direction.WEST.getDirectionVec();
        Vec3i eastVec = Direction.EAST.getDirectionVec();
        Vec3i northVec = Direction.NORTH.getDirectionVec();
        Vec3i southVec = Direction.SOUTH.getDirectionVec();

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
        Vec3d posVec = this.getPos(x, y, z);
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

        Vec3d motion = this.getMotion();
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

        double veloc = Math.min(2.0D, Math.sqrt(horizontalMag(motion)));
        motion = new Vec3d(veloc * xDif / difSq, motion.y, veloc * zDif / difSq);
        this.setMotion(motion);
        Entity entity = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
        if (entity instanceof PlayerEntity)
        {
            Vec3d mot = entity.getMotion();
            double horMot = horizontalMag(mot);
            double tempMot = horizontalMag(this.getMotion());
            if (horMot > 1.0E-4D && tempMot < 0.01D)
            {
                this.setMotion(this.getMotion().add(mot.x * 0.1D, 0.0D, mot.z * 0.1D));
                flag = false;
            }
        }

        if (flag && shouldDoRailFunctions())
        {
            double tempMot = Math.sqrt(horizontalMag(this.getMotion()));
            if (tempMot < 0.03D)
            {
                this.setMotion(Vec3d.ZERO);
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
        if (vecIn.getY() != 0 && MathHelper.floor(this.getPosX()) - pos.getX() == vecIn.getX()
            && MathHelper.floor(this.getPosZ()) - pos.getZ() == vecIn.getZ())
        {
            this.setPosition(this.getPosX(), this.getPosY() + (double) vecIn.getY(), this.getPosZ());
        }
        else if (vecOut.getY() != 0 && MathHelper.floor(this.getPosX()) - pos.getX() == vecOut.getX()
            && MathHelper.floor(this.getPosZ()) - pos.getZ() == vecOut.getZ())
        {
            this.setPosition(this.getPosX(), this.getPosY() + (double) vecOut.getY(), this.getPosZ());
        }

        this.applyDrag();
        Vec3d newPos = this.getPos(this.getPosX(), this.getPosY(), this.getPosZ());
        if (newPos != null && posVec != null)
        {
            double yMot = (posVec.y - newPos.y) * 0.05D;
            Vec3d tempMot = this.getMotion();
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
            Vec3d tempMot = this.getMotion();
            double temoVeloc = Math.sqrt(horizontalMag(tempMot));
            this.setMotion(temoVeloc * (double) (xFloor - pos.getX()), tempMot.y, temoVeloc * (double) (zFloor - pos.getZ()));
        }

        if (shouldDoRailFunctions())
        {
            ((AbstractRailBlock) state.getBlock()).onMinecartPass(state, world, pos, this);
        }

        if (isPowered && shouldDoRailFunctions())
        {
            Vec3d tempMot = this.getMotion();
            double tempVeloc = Math.sqrt(horizontalMag(tempMot));
            if (tempVeloc > 0.01D)
            {
                this.setMotion(tempMot.add(tempMot.x / tempVeloc * 0.06D, 0.0D, tempMot.z / tempVeloc * 0.06D));
            }
            else
            {
                Vec3d mot = this.getMotion();
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

    private static Pair<Vec3i, Vec3i> getShapeMatrix(RailShape p_226573_0_)
    {
        return MATRIX.get(p_226573_0_);
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand)
    {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(@NotNull final Entity entityIn)
    {
        return null;
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
