package com.minecolonies.coremod.colony.buildings;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public abstract class AbstractSchematicProvider implements ISchematicProvider
{
    /**
     * The Colony for this schematic Provider
     */
    private final IColony colony;

    /**
     * The location of the building.
     */
    private final BlockPos location;

    /**
     * The level of the building.
     */
    private int buildingLevel = 0;

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
     * Cached rotation.
     */
    public int cachedRotation = -1;

    /**
     * The building area box of this building
     */
    private AxisAlignedBB buildingArea = null;

    /**
     * Made to check if the building has to update the server/client.
     */
    private boolean dirty = false;

    public AbstractSchematicProvider(final BlockPos pos, final IColony colony)
    {
        this.location = pos;
        this.colony = colony;
    }

    @Override
    public int hashCode()
    {
        return (int) (31 * this.getID().toLong());
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof AbstractBuilding && ((IBuilding) o).getID().equals(this.getID());
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = new CompoundNBT();
        BlockPosUtil.write(compound, TAG_LOCATION, location);
        final StructureName structureName = new StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + buildingLevel);
        if (Structures.hasMD5(structureName))
        {
            compound.putString(TAG_SCHEMATIC_MD5, Structures.getMD5(structureName.toString()));
        }

        compound.putInt(TAG_SCHEMATIC_LEVEL, buildingLevel);
        compound.putString(TAG_STYLE, style);

        compound.putBoolean(TAG_MIRROR, isBuildingMirrored);

        compound.putInt(TAG_CORNER1, this.cornerX1);
        compound.putInt(TAG_CORNER2, this.cornerX2);
        compound.putInt(TAG_CORNER3, this.cornerZ1);
        compound.putInt(TAG_CORNER4, this.cornerZ2);

        compound.putInt(TAG_HEIGHT, this.height);

        compound.putInt(TAG_ROTATION, getRotation());

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        buildingLevel = compound.getInt(TAG_SCHEMATIC_LEVEL);

        style = compound.getString(TAG_STYLE);

        deserializerStructureInformationFrom(compound);

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

        if (compound.contains(TAG_ROTATION))
        {
            this.cachedRotation = compound.getInt(TAG_ROTATION);
        }
    }

    private void deserializerStructureInformationFrom(final CompoundNBT compound)
    {
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
                Log.getLogger().warn(String.format("AbstractBuilding.readFromNBT: %s have been moved to %s", sn, newStructureName));
            }
        }

        if (style.isEmpty())
        {
            Log.getLogger().warn("Loaded empty style, setting to wooden");
            style = "wooden";
        }
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    @Override
    public BlockPos getPosition()
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
    @Override
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
    @Override
    public void setHeight(final int height)
    {
        this.height = height;
    }

    /**
     * Get all the corners of the building based on the schematic.
     *
     * @return Tuple of X corners, Tuple of Z corners
     */
    @Override
    public Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> getCorners()
    {
        return new Tuple<>(new Tuple<>(cornerX1, cornerX2), new Tuple<>(cornerZ1, cornerZ2));
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    @Override
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
    @Override
    public AxisAlignedBB getTargetableArea(final World world)
    {
        if (buildingArea == null)
        {
            buildingArea = BuildingUtils.getTargetAbleArea(world, this);
        }
        return buildingArea;
    }

    /**
     * Returns the rotation of the current building.
     *
     * @return integer value of the rotation.
     */
    @Override
    public int getRotation()
    {
        if (cachedRotation != -1)
        {
            return cachedRotation;
        }

        final StructureName structureName = new StructureName(Structures.SCHEMATICS_PREFIX, style, this.getSchematicName() + Math.max(1, buildingLevel));
        try
        {
            final Structure structure = new Structure(colony.getWorld(), structureName.toString(), new PlacementSettings());

            final Blueprint blueprint = structure.getBluePrint();

            if (blueprint != null)
            {
                final BlockState structureState = structure.getBluePrint().getBlockInfoAsMap().get(structure.getBluePrint().getPrimaryBlockOffset()).getState();
                if (structureState != null)
                {
                    if (!(structureState.getBlock() instanceof AbstractBlockHut) || !(colony.getWorld().getBlockState(this.location).getBlock() instanceof AbstractBlockHut))
                    {
                        Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", structureName.toString()));
                        return 0;
                    }

                    final int structureRotation = structureState.get(AbstractBlockHut.FACING).getHorizontalIndex();
                    final int worldRotation = colony.getWorld().getBlockState(this.location).get(AbstractBlockHut.FACING).getHorizontalIndex();

                    if (structureRotation <= worldRotation)
                    {
                        cachedRotation = worldRotation - structureRotation;;
                    }
                    else
                    {
                        cachedRotation = 4 + worldRotation - structureRotation;
                    }
                    return cachedRotation;
                }
            }
        }
        catch (Exception e)
        {
            Log.getLogger().error(String.format("Failed to get rotation for %s: ", structureName.toString()), e);

            return  0;
        }

        return 0;
    }

    /**
     * Returns the style of the current building.
     *
     * @return String representation of the current building-style
     */
    @Override
    public String getStyle()
    {
        return style;
    }

    /**
     * Sets the style of the building.
     *
     * @param style String value of the style.
     */
    @Override
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
    @Override
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Returns the level of the current object.
     *
     * @return Level of the current object.
     */
    @Override
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    /**
     * Sets the current level of the building.
     *
     * @param level Level of the building.
     */
    @Override
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
    @Override
    public final boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets {@link #dirty} to false, meaning that the instance is up to date.
     */
    @Override
    public final void clearDirty()
    {
        dirty = false;
    }

    /**
     * Marks the instance and the building dirty.
     */
    @Override
    public void markDirty()
    {
        dirty = true;
    }

    /**
     * Sets the mirror of the current building.
     */
    @Override
    public void invertMirror()
    {
        this.isBuildingMirrored = !isBuildingMirrored;
    }

    /**
     * Returns the mirror of the current building.
     *
     * @return boolean value of the mirror.
     */
    @Override
    public boolean isMirrored()
    {
        return isBuildingMirrored;
    }
}
