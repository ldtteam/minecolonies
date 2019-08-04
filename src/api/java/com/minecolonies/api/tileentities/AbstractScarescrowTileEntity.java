package com.minecolonies.api.tileentities;

import com.minecolonies.api.colony.IColonyView;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScarescrowTileEntity extends ChestTileEntity
{
    /**
     * Getter of the name of the tileEntity.
     *
     * @return the string.
     */
    public abstract String getDesc();

    /**
     * Setter for the name.
     *
     * @param name string to set.
     */
    public abstract void setName(String name);

    /**
     * Calculates recursively the length of the field until a certain point.
     * <p>
     * This mutates the field!
     *
     * @param position the start position.
     * @param world    the world the field is in.
     */
    public abstract void calculateSize(@NotNull World world, @NotNull BlockPos position);

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     *
     * @param world    the world object.
     * @param position the position.
     * @return true if it is.
     */
    public abstract boolean isNoPartOfField(@NotNull World world, @NotNull BlockPos position);

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
     * Getter of the length in plus x direction.
     *
     * @return field length.
     */
    public abstract int getLengthPlusX();

    /**
     * Getter of the with in plus z direction.
     *
     * @return field width.
     */
    public abstract int getWidthPlusZ();

    /**
     * Getter of the length in minus x direction.
     *
     * @return field length.
     */
    public abstract int getLengthMinusX();

    /**
     * Getter of the with in minus z direction.
     *
     * @return field width.
     */
    public abstract int getWidthMinusZ();

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
     * @return the int id.
     */
    public abstract int getOwnerId();

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the name of the citizen.
     * @param tempColony the colony view.
     */
    public abstract void setOwner(int ownerId, IColonyView tempColony);

    /**
     * Get the inventory of the scarecrow.
     * @return the IItemHandler.
     */
    public abstract IItemHandlerModifiable getInventory();

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    public abstract ScareCrowType getScarecrowType();
}
