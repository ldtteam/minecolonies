package com.minecolonies.api.entity.other;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Special minecolonies minecart that doesn't collide.
 */
public class MinecoloniesMinecart extends Minecart
{
    private static final Vec3 LOWERED_PASSENGER_ATTACHMENT = new Vec3(0.0, 0.0, 0.0);

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
    public InteractionResult interact(final Player player, final InteractionHand hand, final Vec3 location)
    {
        return InteractionResult.FAIL;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    public void push(@NotNull final Entity entity)
    {
        // Citizens use carts as a transport marker; vanilla pushing is unwanted.
    }

    @Override
    public void playerTouch(final Player entity)
    {
        // Do not pick citizens up on contact.
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    public boolean canCollideWith(final Entity entity)
    {
        return false;
    }

    @NotNull
    @Override
    protected Vec3 getPassengerAttachmentPoint(@NotNull final Entity passenger, @NotNull final EntityDimensions dimensions, final float scale)
    {
        return LOWERED_PASSENGER_ATTACHMENT;
    }
}
