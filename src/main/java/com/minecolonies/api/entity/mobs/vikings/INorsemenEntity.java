package com.minecolonies.api.entity.mobs.vikings;

import com.minecolonies.api.util.IItemHandlerCapProvider;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface INorsemenEntity extends Enemy, CommandSource, IItemHandlerCapProvider
{
    @Override
    @Nullable
    default IItemHandler getItemHandlerCap(final Direction direction)
    {
        // LivingEntities have cap registered by forge
        return ItemHandler.ENTITY.getCapability((LivingEntity) this, null);
    }
}
