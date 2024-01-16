package com.minecolonies.api.colony.buildings;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;

public interface ISchematicProvider extends INBTSerializable<CompoundTag>
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
     * This is the lowest corner (x,y,z) and the highest corner (x,y,z).
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
     * Get the parent building position
     *
     * @return
     */
    BlockPos getParent();

    /**
     * Whether we have a parent
     *
     * @return true if there is a parent building
     */
    boolean hasParent();

    /**
     * Set the parent building position
     * @param pos
     */
    void setParent(BlockPos pos);

    /**
     * Get the child building positions
     * @return
     */
    Set<BlockPos> getChildren();

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
    String getStructurePack();

    /**
     * Sets the style of the building.
     *
     * @param style String value of the style.
     */
    void setStructurePack(String style);

    /**
     * Returns the blueprint path of the current building.
     *
     * @return String representation of the current blueprint-path
     */
    String getBlueprintPath();

    /**
     * Sets the blueprint path of the building.
     *
     * @param path String value of the blueprint-path.
     */
    void setBlueprintPath(String path);

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

    /**
     * Called when the old schematic is updated to a new one
     *
     * @param oldSchematic
     * @param newSchematic
     */
    void onUpgradeSchematicTo(final String oldSchematic, final String newSchematic, final IBlueprintDataProviderBE blueprintDataProvider);
}
