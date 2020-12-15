package com.minecolonies.coremod.colony.buildings;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public abstract class AbstractSchematicProvider implements ISchematicProvider, IBuilding
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
     * If the building was deconstructed by the builder.
     */
    private boolean isDeconstructed;

    /**
     * Corners of the building.
     */
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;

    /**
     * Cached rotation.
     */
    public int cachedRotation = -1;

    /**
     * The building area box of this building
     */
    private AxisAlignedBB buildingArea = null;

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
    public boolean isDeconstructed()
    {
        return isDeconstructed;
    }

    @Override
    public void setDeconstructed()
    {
        this.isDeconstructed = true;
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

        getCorners();
        BlockPosUtil.write(compound, TAG_CORNER1, this.pos1);
        BlockPosUtil.write(compound, TAG_CORNER2, this.pos2);

        compound.putInt(TAG_HEIGHT, this.height);

        compound.putInt(TAG_ROTATION, getRotation());

        compound.putBoolean(TAG_DECONSTRUCTED, isDeconstructed);

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        buildingLevel = compound.getInt(TAG_SCHEMATIC_LEVEL);

        style = compound.getString(TAG_STYLE);

        deserializerStructureInformationFrom(compound);

        isBuildingMirrored = compound.getBoolean(TAG_MIRROR);

        if (compound.keySet().contains(TAG_CORNER1) && !compound.keySet().contains(TAG_CORNER3))
        {
            this.pos1 = BlockPosUtil.read(compound, TAG_CORNER1);
            this.pos2 = BlockPosUtil.read(compound, TAG_CORNER2);
        }

        if (compound.contains(TAG_HEIGHT))
        {
            this.height = compound.getInt(TAG_HEIGHT);
        }

        if (compound.contains(TAG_ROTATION))
        {
            this.cachedRotation = compound.getInt(TAG_ROTATION);
        }

        if (compound.contains(TAG_DECONSTRUCTED))
        {
            this.isDeconstructed = compound.getBoolean(TAG_DECONSTRUCTED);
        }
        else
        {
            this.isDeconstructed = false;
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

    @Override
    public BlockPos getPosition()
    {
        return location;
    }

    @Override
    public void setCorners(final BlockPos pos1, final BlockPos pos2)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    @Override
    public Tuple<BlockPos, BlockPos> getCorners()
    {
        if (pos1 == null || pos2 == null)
        {
            this.calculateCorners();
        }
        return new Tuple<>(pos1, pos2);
    }

    @Override
    public BlockPos getID()
    {
        // Location doubles as ID.
        return location;
    }

    @Override
    public AxisAlignedBB getTargetableArea(final World world)
    {
        if (buildingArea == null)
        {
            buildingArea = BuildingUtils.getTargetAbleArea(this);
        }
        return buildingArea;
    }

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
            final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(colony.getWorld(), getPosition(), structureName.toString(), new PlacementSettings(), true);

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
                        cachedRotation = worldRotation - structureRotation;
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

            return 0;
        }

        return 0;
    }

    @Override
    public String getStyle()
    {
        return style;
    }

    @Override
    public void setStyle(final String style)
    {
        this.style = style;
        cachedRotation = -1;
        this.markDirty();
    }

    @Override
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    @Override
    public void setBuildingLevel(final int level)
    {
        if (level > getMaxBuildingLevel())
        {
            return;
        }

        isDeconstructed = false;
        buildingLevel = level;
        markDirty();
    }

    @Override
    public void setIsMirrored(final boolean isMirrored)
    {
        this.isBuildingMirrored = isMirrored;
    }

    @Override
    public boolean isMirrored()
    {
        return isBuildingMirrored;
    }
}
