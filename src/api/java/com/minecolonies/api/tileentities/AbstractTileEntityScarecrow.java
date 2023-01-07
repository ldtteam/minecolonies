package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for the scarecrow tile entity to store extra data.
 */
public abstract class AbstractTileEntityScarecrow extends BlockEntity implements MenuProvider
{
    /**
     * Default constructor.
     */
    protected AbstractTileEntityScarecrow(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.SCARECROW, pos, state);
    }

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
     * Check condition whether the field UI can be opened or not.
     *
     * @param player the player attempting to open the menu.
     * @return whether the player is authorized to open this menu.
     */
    public abstract boolean canOpenMenu(@NotNull Player player);
}
