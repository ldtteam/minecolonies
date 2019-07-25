package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.registry.BuildingRegistry;
import com.minecolonies.coremod.util.BuildingUtils;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public abstract class AbstractSchematicProvider
{
    /**
     * The location of the building.
     */
    private final BlockPos location;

    /**
     * The level of the building.
     */
    private int buildingLevel = 0;

    /**
     * The rotation of the building.
     */
    private int rotation = 0;

    /**
     * The mirror of the building.
     */
    private boolean isBuildingMirrored = false;

    /**
     * The building style.
     */
    private String style = "wooden";

    /**
     * Height of the building.
     */
    private int height;

    /**
     * Corners of the building.
     */
    private int cornerX1;
    private int cornerX2;
    private int cornerZ1;
    private int cornerZ2;

    /**
     * Made to check if the building has to update the server/client.
     */
    private boolean dirty = false;

    public AbstractSchematicProvider(final BlockPos pos)
    {
        location = pos;
    }

    @Override
    public int hashCode()
    {
        return (int) (31 * this.getID().toLong());
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof AbstractBuilding && ((AbstractBuilding) o).getID().equals(this.getID());
    }

    /**
     * Load data from NBT compound.
     * Writes to {@link #buildingLevel}, {@link #rotation} and {@link #style}.
     *
     * @param compound {@link net.minecraft.nbt.CompoundNBT} to read data from.
     */
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        buildingLevel = compound.getInt(TAG_BUILDING_LEVEL);

        rotation = compound.getInt(TAG_ROTATION);
        style = compound.getString(TAG_STYLE);

        final String md5 = compound.getString(TAG_SCHEMATIC_MD5);
        final int testLevel = buildingLevel == 0 ? 1 : buildingLevel;
        final StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + testLevel);

        if (!Structures.hasMD5(sn))
        {
            final StructureName newStructureName = Structures.getStructureNameByMD5(md5);
            if (newStructureName != null
                    && newStructureName.getPrefix().equals(sn.getPrefix())
                    && newStructureName.getSchematic().equals(sn.getSchematic()))
            {
                //We found the new location for the schematic, update the style accordingly
                style = newStructureName.getStyle();
                Log.getLogger().warn("AbstractBuilding.readFromNBT: " + sn + " have been moved to " + newStructureName);
            }
        }

        if (style.isEmpty())
        {
            Log.getLogger().warn("Loaded empty style, setting to wooden");
            style = "wooden";
        }

        isBuildingMirrored = compound.getBoolean(TAG_MIRROR);

        if (compound.keySet().contains(TAG_CORNER1))
        {
            this.cornerX1 = compound.getInt(TAG_CORNER1);
            this.cornerX2 = compound.getInt(TAG_CORNER2);
            this.cornerZ1 = compound.getInt(TAG_CORNER3);
            this.cornerZ2 = compound.getInt(TAG_CORNER4);
        }

        if (compound.keySet().contains(TAG_HEIGHT))
        {
            this.height = compound.getInt(TAG_HEIGHT);
        }
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #buildingLevel}, {@link #rotation}, {@link #style}, {@link #location}, and {@link #getClass()} value.
     *
     * @param compound {@link net.minecraft.nbt.CompoundNBT} to write data to.
     */
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        final String s = BuildingRegistry.getNameToClassMap().inverse().get(this.getClass());

        if (s == null)
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        else
        {
            compound.putString(TAG_BUILDING_TYPE, s);
            BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);
            final StructureName structureName = new StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + buildingLevel);
            if (Structures.hasMD5(structureName))
            {
                compound.putString(TAG_SCHEMATIC_MD5, Structures.getMD5(structureName.toString()));
            }
        }

        compound.putInt(TAG_BUILDING_LEVEL, buildingLevel);
        compound.putInt(TAG_ROTATION, rotation);
        compound.putString(TAG_STYLE, style);

        compound.putBoolean(TAG_MIRROR, isBuildingMirrored);

        compound.putInt(TAG_CORNER1, this.cornerX1);
        compound.putInt(TAG_CORNER2, this.cornerX2);
        compound.putInt(TAG_CORNER3, this.cornerZ1);
        compound.putInt(TAG_CORNER4, this.cornerZ2);

        compound.putInt(TAG_HEIGHT, this.height);
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public BlockPos getLocation()
    {
        return location;
    }

    /**
     * Sets the corners of the building based on the schematic.
     *
     * @param x1 the first x corner.
     * @param x2 the second x corner.
     * @param z1 the first z corner.
     * @param z2 the second z corner.
     */
    public void setCorners(final int x1, final int x2, final int z1, final int z2)
    {
        this.cornerX1 = x1;
        this.cornerX2 = x2;
        this.cornerZ1 = z1;
        this.cornerZ2 = z2;
    }

    /**
     * Set the height of the building.
     *
     * @param height the height to set.
     */
    public void setHeight(final int height)
    {
        this.height = height;
    }

    /**
     * Get all the corners of the building based on the schematic.
     *
     * @return the corners.
     */
    public Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> getCorners()
    {
        return new Tuple<>(new Tuple<>(cornerX1, cornerX2), new Tuple<>(cornerZ1, cornerZ2));
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public BlockPos getID()
    {
        // Location doubles as ID.
        return location;
    }

    /**
     * Calculates the area of the building.
     *
     * @param world the world.
     * @return the AxisAlignedBB.
     */
    public AxisAlignedBB getTargetableArea(final World world)
    {
        return BuildingUtils.getTargetAbleArea(world, this);
    }

    /**
     * Returns the rotation of the current building.
     *
     * @return integer value of the rotation.
     */
    public int getRotation()
    {
        return rotation;
    }

    /**
     * Sets the rotation of the current building.
     *
     * @param rotation integer value of the rotation.
     */
    public void setRotation(final int rotation)
    {
        this.rotation = rotation;
    }

    /**
     * Returns the style of the current building.
     *
     * @return String representation of the current building-style
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * Sets the style of the building.
     *
     * @param style String value of the style.
     */
    public void setStyle(final String style)
    {
        this.style = style;
        this.markDirty();
    }

    /**
     * Get the height of the building.
     *
     * @return the height..
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Returns the level of the current object.
     *
     * @return Level of the current object.
     */
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    /**
     * Sets the current level of the building.
     *
     * @param level Level of the building.
     */
    public void setBuildingLevel(final int level)
    {
        if (level > getMaxBuildingLevel())
        {
            return;
        }

        buildingLevel = level;
        markDirty();
    }

    /**
     * Returns whether the instance is dirty or not.
     *
     * @return true if dirty, false if not.
     */
    public final boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets {@link #dirty} to false, meaning that the instance is up to date.
     */
    public final void clearDirty()
    {
        dirty = false;
    }

    /**
     * Marks the instance and the building dirty.
     */
    public void markDirty()
    {
        dirty = true;
    }

    /**
     * Sets the mirror of the current building.
     */
    public void invertMirror()
    {
        this.isBuildingMirrored = !isBuildingMirrored;
    }

    /**
     * Returns the mirror of the current building.
     *
     * @return boolean value of the mirror.
     */
    public boolean isMirrored()
    {
        return isBuildingMirrored;
    }

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    public abstract String getSchematicName();

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    public abstract int getMaxBuildingLevel();
}
