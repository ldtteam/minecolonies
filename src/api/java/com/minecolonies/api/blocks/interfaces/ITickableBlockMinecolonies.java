package com.minecolonies.api.blocks.interfaces;

import com.minecolonies.api.tileentities.ITickable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ITickableBlockMinecolonies extends EntityBlock
{
    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type)
    {
        return (l, pos, s, te) -> ((ITickable) te).tick(l, s, pos);
    }
}
