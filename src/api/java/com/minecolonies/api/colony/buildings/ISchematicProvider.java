package com.minecolonies.api.colony.buildings;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface ISchematicProvider extends INBTSerializable<CompoundNBT>
{
    /**
     * Returns the {@code BlockPos} of the current object, also used as ID.
     *
     * @return {@code BlockPos} of the current object.
     */
    BlockPos getPosition();

    /**
     * Sets the corners of the building based on the schematic.
     *
     * @param corner1 the first corner.
     * @param corner2 the second corner.
     */
    void setCorners(final BlockPos corner1, final BlockPos corner2);

    /**
     * Get all the corners of the building based on the schematic.
     *
     * @return the corners.
     */
    Tuple<BlockPos, BlockPos> getCorners();

    /**
     * Returns the {@code BlockPos} of the current object, also used as ID.
     *
     * @return {@code BlockPos} of the current object.
     */
    BlockPos getID();

    /**
     * Returns the rotation of the current building.
     *
     * @return integer value of the rotation.
     */
    int getRotation();

    /**
     * Returns the style of the current building.
     *
     * @return String representation of the current building-style
     */
    String getStyle();

    /**
     * Sets the style of the building.
     *
     * @param style String value of the style.
     */
    void setStyle(String style);

    /**
     * Returns the level of the current object.
     *
     * @return Level of the current object.
     */
    int getBuildingLevel();

    /**
     * Sets the current level of the building.
     *
     * @param level Level of the building.
     */
    void setBuildingLevel(int level);

    /**
     * Returns whether the instance is dirty or not.
     *
     * @return true if dirty, false if not.
     */
    boolean isDirty();

    /**
     * Sets {@code #dirty} to false, meaning that the instance is up to date.
     */
    void clearDirty();

    /**
     * Marks the instance and the building dirty.
     */
    void markDirty();

    /**
     * Sets the mirror of the current building.
     */
    void setIsMirrored(final boolean isMirrored);

    /**
     * Returns the mirror of the current building.
     *
     * @return boolean value of the mirror.
     */
    boolean isMirrored();

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    String getSchematicName();

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    int getMaxBuildingLevel();

    /**
     * Check if the building was deconstructed.
     *
     * @return true if so.
     */
    boolean isDeconstructed();

    /**
     * Set the building as deconstructed.
     */
    void setDeconstructed();
}
