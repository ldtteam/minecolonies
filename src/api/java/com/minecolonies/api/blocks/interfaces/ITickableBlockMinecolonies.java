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
    default  <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type)
    {
        return createTickerHelper(type, type, (l, pos, s, te) -> ((ITickable) te).tick(l, s, pos));
    }

    @Nullable
    static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_)
    {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
    }
}
