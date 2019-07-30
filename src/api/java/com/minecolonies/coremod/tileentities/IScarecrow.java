package com.minecolonies.coremod.tileentities;

import com.minecolonies.coremod.colony.IColonyView;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IScarecrow extends ICapabilitySerializable<NBTTagCompound>, ILockableContainer, ILootContainer, ITickable
{
    /**
     * Getter of the name of the tileEntity.
     *
     * @return the string.
     */
    String getDesc();

    /**
     * Setter for the name.
     *
     * @param name string to set.
     */
    void setName(String name);

    /**
     * Calculates recursively the length of the field until a certain point.
     * <p>
     * This mutates the field!
     *
     * @param position the start position.
     * @param world    the world the field is in.
     */
    void calculateSize(@NotNull World world, @NotNull BlockPos position);

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     *
     * @param world    the world object.
     * @param position the position.
     * @return true if it is.
     */
    boolean isNoPartOfField(@NotNull World world, @NotNull BlockPos position);

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    BlockPos getID();

    /**
     * Has the field been taken?
     *
     * @return true if the field is not free to use, false after releasing it.
     */
    boolean isTaken();

    /**
     * Sets the field taken.
     *
     * @param taken is field free or not
     */
    void setTaken(boolean taken);

    void nextState();

    /**
     * Checks if the field has been planted.
     *
     * @return true if there are crops planted.
     */
    FieldStage getFieldStage();

    /**
     * Sets if there are any crops planted.
     *
     * @param fieldStage true after planting, false after harvesting.
     */
    void setFieldStage(FieldStage fieldStage);

    /**
     * Checks if the field needs work (planting, hoeing).
     *
     * @return true if so.
     */
    boolean needsWork();

    /**
     * Sets that the field needs work.
     *
     * @param needsWork true if work needed, false after completing the job.
     */
    void setNeedsWork(boolean needsWork);

    /**
     * Getter of the seed of the field.
     *
     * @return the ItemSeed
     */
    @Nullable
    ItemStack getSeed();

    /**
     * Getter of the length in plus x direction.
     *
     * @return field length.
     */
    int getLengthPlusX();

    /**
     * Getter of the with in plus z direction.
     *
     * @return field width.
     */
    int getWidthPlusZ();

    /**
     * Getter of the length in minus x direction.
     *
     * @return field length.
     */
    int getLengthMinusX();

    /**
     * Getter of the with in minus z direction.
     *
     * @return field width.
     */
    int getWidthMinusZ();

    /**
     * Location getter.
     *
     * @return the location of the scarecrow of the field.
     */
    BlockPos getPosition();

    /**
     * Getter of the owner of the field.
     *
     * @return the string description of the citizen.
     */
    @NotNull
    String getOwner();

    /**
     * Getter for the ownerId of the field.
     * @return the int id.
     */
    int getOwnerId();

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the id of the citizen.
     */
    void setOwner(@NotNull int ownerId);

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the name of the citizen.
     * @param tempColony the colony view.
     */
    void setOwner(int ownerId, IColonyView tempColony);

    /**
     * Get the inventory of the scarecrow.
     * @return the IItemHandler.
     */
    IItemHandlerModifiable getInventory();

    SPacketUpdateTileEntity getUpdatePacket();

    @NotNull
    NBTTagCompound getUpdateTag();

    void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet);

    void onLoad();

    void readFromNBT(NBTTagCompound compound);

    NBTTagCompound writeToNBT(NBTTagCompound compound);

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    ScareCrowType getType();

    /**
     * Describes the stage the field is in.
     * Like if it has been hoed, planted or is empty.
     */
    public enum FieldStage
    {
        EMPTY,
        HOED,
        PLANTED
    }

    /**
     * Enum describing the different textures the scarecrow has.
     */
    public enum ScareCrowType
    {
        PUMPKINHEAD,
        NORMAL
    }
}
