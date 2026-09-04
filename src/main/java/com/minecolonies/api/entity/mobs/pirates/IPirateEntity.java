package com.minecolonies.api.entity.mobs.pirates;

import com.minecolonies.api.util.IItemHandlerCapProvider;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.capabilities.Capabilities.Item;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public interface IPirateEntity extends Enemy, CommandSource, IItemHandlerCapProvider

{

    default boolean shouldInformAdmins()
    {
        return false;
    }

    @Override
    default void sendSystemMessage(final Component message)
    {
    }

    @Override
    default boolean acceptsSuccess()
    {
        return false;
    }

    @Override
    default boolean acceptsFailure()
    {
        return false;
    }

    @Override
    @Nullable
    default IItemHandler getItemHandlerCap(final Direction direction)
    {
        final ResourceHandler<ItemResource> handler = Item.ENTITY.getCapability((LivingEntity) this, null);
        return handler == null ? null : IItemHandler.of(handler);
    }
}
