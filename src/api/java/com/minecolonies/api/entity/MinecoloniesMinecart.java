package com.minecolonies.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Special minecolonies minecart that doesn't collide.
 */
public class MinecoloniesMinecart extends AbstractMinecartEntity
{
    /**
     * Constructor to create the minecart.
     * @param type the entity type.
     * @param world the world.
     */
    public MinecoloniesMinecart(final EntityType<?> type, final World world)
    {
        super(type, world);
    }

    public MinecoloniesMinecart(final World world)
    {
        super(ModEntities.MINECART, world);
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

    @NotNull
    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
