package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTileEntityScarecrow extends BlockEntity implements MenuProvider
{

    /**
     * Default constructor.
     */
    protected AbstractTileEntityScarecrow(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.SCARECROW.get(), pos, state);
    }

    /**
     * Getter of the seed of the field.
     *
     * @return the ItemSeed
     */
    @Nullable
    public abstract Item getPlant();

    /**
     * Get the inventory of the scarecrow.
     *
     * @return the IItemHandler.
     */
    public abstract IItemHandler getInventory();

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    public abstract ScareCrowType getScarecrowType();

    /**
     * The colony this field is located in.
     *
     * @return the colony instance.
     */
    public abstract IColony getCurrentColony();

    /**
     * Check whether the menu of the scarecrow can still be opened.
     *
     * @param playerIn the current player.
     * @return true if so.
     */
    public abstract boolean canOpenMenu(final Player playerIn);
}
