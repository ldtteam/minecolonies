package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScarecrowTileEntity extends BlockEntity implements MenuProvider
{

    /**
     * Default constructor.
     */
    public AbstractScarecrowTileEntity(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.SCARECROW, pos, state);
    }

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     *
     * @param world    the world object.
     * @param position the position.
     * @return true if it is.
     */
    public abstract boolean isNoPartOfField(@NotNull Level world, @NotNull BlockPos position);

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public abstract BlockPos getID();

    /**
     * Has the field been taken?
     *
     * @return true if the field is not free to use, false after releasing it.
     */
    public abstract boolean isTaken();

    /**
     * Sets the field taken.
     *
     * @param taken is field free or not
     */
    public abstract void setTaken(boolean taken);

    public abstract void nextState();

    /**
     * Checks if the field has been planted.
     *
     * @return true if there are crops planted.
     */
    public abstract ScarecrowFieldStage getFieldStage();

    /**
     * Sets if there are any crops planted.
     *
     * @param fieldStage true after planting, false after harvesting.
     */
    public abstract void setFieldStage(ScarecrowFieldStage fieldStage);

    /**
     * Checks if the field needs work (planting, hoeing).
     *
     * @return true if so.
     */
    public abstract boolean needsWork();

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    /**
     * Sets that the field needs work.
     *
     * @param needsWork true if work needed, false after completing the job.
     */
    public abstract void setNeedsWork(boolean needsWork);

    /**
     * Getter of the seed of the field.
     *
     * @return the ItemSeed
     */
    @Nullable
    public abstract ItemStack getSeed();

    /**
     * Location getter.
     *
     * @return the location of the scarecrow of the field.
     */
    public abstract BlockPos getPosition();

    /**
     * Getter of the owner of the field.
     *
     * @return the string description of the citizen.
     */
    @NotNull
    public abstract String getOwner();

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the id of the citizen.
     */
    public abstract void setOwner(@NotNull int ownerId);

    /**
     * Getter for the ownerId of the field.
     *
     * @return the int id.
     */
    public abstract int getOwnerId();

    /**
     * Sets the owner of the field.
     *
     * @param ownerId    the name of the citizen.
     * @param tempColony the colony view.
     */
    public abstract void setOwner(int ownerId, IColonyView tempColony);

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
     * @param direction the direction to get the range for
     * @return the number of blocks away from the scarecrow the farmer will work with in that direction
     */
    public abstract int getRadius(Direction direction);

    /**
     * Sets the radius of the field plot the farmer works with
     * @param direction the direction for the radius
     * @param radius the number of blocks from the scarecrow that the farmer will work with
     */
    public abstract void setRadius(Direction direction, int radius);
}
