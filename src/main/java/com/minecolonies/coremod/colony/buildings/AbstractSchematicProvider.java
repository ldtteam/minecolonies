package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableSet;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.colony.buildings.modules.IAltersBuildingFootprint;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.managers.interfaces.IRegisteredStructureManager;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_INVALID_BUILDING;

public abstract class AbstractSchematicProvider implements ISchematicProvider, IBuilding
{
    /**
     * The Colony for this schematic Provider
     */
    protected final IColony colony;

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
    private String structurePack = "Default";

    /**
     * The building blueprint path.
     */
    private String path = "";

    /**
     * Height of the building.
     */
    private int height;

    /**
     * The type of the building
     */
    private BuildingEntry buildingType = null;

    /**
     * If the building was deconstructed by the builder.
     */
    private boolean isDeconstructed;

    /**
     * Corners of the building.
     */
    private BlockPos lowerCorner  = null;
    private BlockPos higherCorner = null;

    /**
     * Cached rotation.
     */
    public int cachedRotation = -1;

    /**
     * Parent schematic this is in
     */
    private BlockPos parentSchematic = BlockPos.ZERO;

    /**
     * Child schematics within this
     */
    private Set<BlockPos>     childSchematics = ImmutableSet.of();

    /**
     * Blueprint future for delayed info reading.
     */
    private Future<Blueprint> blueprintFuture;

    public AbstractSchematicProvider(final BlockPos pos, final IColony colony)
    {
        this.location = pos;
        this.colony = colony;
    }

    @Override
    public int hashCode()
    {
        return (int) (31 * this.getID().asLong());
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
    public String getBlueprintPath()
    {
        return path;
    }

    @Override
    public void setBlueprintPath(final String path)
    {
        this.path = path;
        getTileEntity().setBlueprintPath(path);
        cachedRotation = -1;
        this.markDirty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        BlockPosUtil.write(compound, TAG_LOCATION, location);

        compound.putString(TAG_PACK, structurePack);
        compound.putString(TAG_PATH, getBlueprintPath());

        compound.putInt(TAG_SCHEMATIC_LEVEL, buildingLevel);
        compound.putBoolean(TAG_MIRROR, isBuildingMirrored);

        getCorners();
        BlockPosUtil.write(compound, TAG_CORNER1, this.lowerCorner);
        BlockPosUtil.write(compound, TAG_CORNER2, this.higherCorner);

        compound.putInt(TAG_HEIGHT, this.height);

        compound.putInt(TAG_ROTATION, getRotation());

        compound.putBoolean(TAG_DECONSTRUCTED, isDeconstructed);

        BlockPosUtil.write(compound, TAG_PARENT_SCHEM, parentSchematic);
        BlockPosUtil.writePosListToNBT(compound, TAG_CHILD_SCHEM, new ArrayList<>(childSchematics));
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        buildingLevel = compound.getInt(TAG_SCHEMATIC_LEVEL);

        deserializerStructureInformationFrom(compound);

        isBuildingMirrored = compound.getBoolean(TAG_MIRROR);

        if (compound.getAllKeys().contains(TAG_CORNER1) && compound.getAllKeys().contains(TAG_CORNER2))
        {
            setCorners(BlockPosUtil.read(compound, TAG_CORNER1), BlockPosUtil.read(compound, TAG_CORNER2));
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

        parentSchematic = BlockPosUtil.read(compound, TAG_PARENT_SCHEM);
        childSchematics = ImmutableSet.copyOf(BlockPosUtil.readPosListFromNBT(compound, TAG_CHILD_SCHEM));
    }

    private void deserializerStructureInformationFrom(final CompoundTag compound)
    {
        if (compound.contains(TAG_STYLE))
        {
            structurePack = BlueprintMapping.styleMapping.get(compound.getString(TAG_STYLE));
            path = BlueprintMapping.pathMapping.get(compound.getString(TAG_STYLE) + ":" + this.getSchematicName().substring(0, this.getSchematicName().length() - 1)) + buildingLevel;
        }
        else
        {
            structurePack = compound.getString(TAG_PACK);
            path = compound.getString(TAG_PATH);
        }

        if (structurePack.isEmpty())
        {
            Log.getLogger().warn("Loaded empty style, setting to Default");
            structurePack = "Default";
        }
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public BlockPos getPosition()
    {
        return location;
    }

    @Override
    public void setCorners(final BlockPos pos1, final BlockPos pos2)
    {
        this.lowerCorner = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        this.higherCorner = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
        if (this.getTileEntity() != null && !lowerCorner.equals(higherCorner))
        {
            this.getTileEntity().setSchematicCorners(lowerCorner.subtract(getPosition()), higherCorner.subtract(getPosition()));
        }
    }

    @Override
    public Tuple<BlockPos, BlockPos> getCorners()
    {
        if (lowerCorner == null || higherCorner == null)
        {
            this.calculateCorners();
        }
        return new Tuple<>(lowerCorner, higherCorner);
    }

    @Override
    public BlockPos getID()
    {
        // Location doubles as ID.
        return location;
    }

    @Override
    public BlockPos getParent()
    {
        final IBuilding building = colony.getBuildingManager().getBuilding(parentSchematic);
        if (building != null)
        {
            if (!building.getChildren().contains(getID()))
            {
                building.addChild(getID());
            }
        }

        return parentSchematic;
    }

    @Override
    public boolean hasParent()
    {
        return !parentSchematic.equals(BlockPos.ZERO);
    }

    @Override
    public void setParent(final BlockPos pos)
    {
        if (!pos.equals(getID()))
        {
            parentSchematic = pos;
        }
    }

    @Override
    public Set<BlockPos> getChildren()
    {
        // Validate childs existance
        final IRegisteredStructureManager manager = colony.getBuildingManager();
        List<BlockPos> toRemove = null;

        for (final BlockPos pos : childSchematics)
        {
            if (manager.getBuilding(pos) == null)
            {
                if (toRemove == null)
                {
                    toRemove = new ArrayList<>();
                }
                toRemove.add(pos);
            }
        }

        if (toRemove != null)
        {
            final Set<BlockPos> oldPositions = new HashSet<>(this.childSchematics);
            oldPositions.removeAll(toRemove);
            this.childSchematics = ImmutableSet.copyOf(oldPositions);
        }

        return childSchematics;
    }

    @Override
    public void addChild(final BlockPos pos)
    {
        childSchematics = ImmutableSet.<BlockPos>builder().addAll(childSchematics).add(pos).build();
    }

    @Override
    public void removeChild(final BlockPos pos)
    {
        final Set<BlockPos> oldPositions = new HashSet<>(childSchematics);
        oldPositions.remove(pos);
        childSchematics = ImmutableSet.copyOf(oldPositions);
    }

    @Override
    public int getRotation()
    {
        if (cachedRotation != -1)
        {
            return cachedRotation;
        }

        try
        {
            final Blueprint blueprint = StructurePacks.getBlueprint(this.structurePack, this.path);
            if (blueprint != null)
            {
                final BlockState structureState = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset()).getState();
                if (structureState != null)
                {
                    if (!(structureState.getBlock() instanceof AbstractBlockHut) || !(colony.getWorld().getBlockState(this.location).getBlock() instanceof AbstractBlockHut))
                    {
                        Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", this.path));
                        return 0;
                    }

                    final int structureRotation = structureState.getValue(AbstractBlockHut.FACING).get2DDataValue();
                    final int worldRotation = colony.getWorld().getBlockState(this.location).getValue(AbstractBlockHut.FACING).get2DDataValue();

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
            Log.getLogger().error(String.format("Failed to get rotation for %s: ", this.path), e);
            return 0;
        }

        return 0;
    }

    /**
     * Load updated TE data from the schematic if missing.
     */
    public void safeUpdateTEDataFromSchematic()
    {
        if (buildingLevel <= 0 || blueprintFuture != null)
        {
            return;
        }

        final TileEntityColonyBuilding te = (TileEntityColonyBuilding) colony.getWorld().getBlockEntity(getPosition());

        try
        {
            unsafeUpdateTEDataFromSchematic(te);
            return;
        }
        catch (final Exception ex)
        {
            Log.getLogger().warn("TileEntity with invalid data, restoring correct data from schematic.");
            te.setSchematicName(this.getSchematicName() + Math.max(1, buildingLevel));
        }

        try
        {
            unsafeUpdateTEDataFromSchematic(te);
        }
        catch (final Exception ex)
        {
            MessageUtils.format(WARNING_INVALID_BUILDING, getSchematicName(), getID().getX(), getID().getY(), getID().getZ(), getStructurePack()).sendTo(colony).forAllPlayers();
        }
    }

    @Override
    public void onColonyTick(final IColony colony)
    {
        if (blueprintFuture != null && blueprintFuture.isDone())
        {
            final Blueprint blueprint;
            try
            {
                blueprint = blueprintFuture.get();
                if (blueprint != null)
                {
                    blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(getRotation()), isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, colony.getWorld());
                    final BlockInfo info = blueprint.getBlockInfoAsMap().getOrDefault(blueprint.getPrimaryBlockOffset(), null);
                    if (info.getTileEntityData() != null)
                    {
                        getTileEntity().readSchematicDataFromNBT(info.getTileEntityData());
                    }
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load the schematic data from the TE schematic name, if it's a reattempt, calculate the name from the building (backup).
     * Might throw exceptions if data is invalid.
     */
    private void unsafeUpdateTEDataFromSchematic(final TileEntityColonyBuilding te)
    {
        final String structureName;
        final String packName;
        if (te.getSchematicName().isEmpty())
        {
            structureName = path;
            packName = structurePack;
        }
        else
        {
            structureName = te.getBlueprintPath();
            packName = te.getStructurePack().getName();
        }

        blueprintFuture = StructurePacks.getBlueprintFuture(packName, structureName);
    }

    @Override
    public String getStructurePack()
    {
        if (parentSchematic != BlockPos.ZERO)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(parentSchematic);
            if (building != null)
            {
                return building.getStructurePack();
            }
        }

        return structurePack;
    }

    @Override
    public void setStructurePack(final String pack)
    {
        this.structurePack = pack;
        cachedRotation = -1;
        this.markDirty();
        getTileEntity().setStructurePack(StructurePacks.packMetas.get(pack));
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

    @Override
    public boolean isInBuilding(@NotNull final BlockPos positionVec)
    {
        final Tuple<BlockPos, BlockPos> corners = getCorners();
        BlockPos cornerA = corners.getA();
        BlockPos cornerB = corners.getB();

        if (this.hasModule(IAltersBuildingFootprint.class))
        {
            final Tuple<BlockPos, BlockPos> extensions = this.getFirstModuleOccurance(IAltersBuildingFootprint.class).getAdditionalCorners();
            cornerA = cornerA.offset(extensions.getA());
            cornerB = cornerB.offset(extensions.getB());
        }

        return positionVec.getX() >= cornerA.getX() - 1 && positionVec.getX() <= cornerB.getX() + 1
                 && positionVec.getY() >= cornerA.getY() - 1 && positionVec.getY() <= cornerB.getY() + 1
                 && positionVec.getZ() >= cornerA.getZ() - 1 && positionVec.getZ() <= cornerB.getZ() + 1;
    }

    @Override
    public void upgradeBuildingLevelToSchematicData()
    {
        final BlockEntity tileEntity = colony.getWorld().getBlockEntity(getID());
        if (tileEntity instanceof IBlueprintDataProvider)
        {
            final IBlueprintDataProvider blueprintDataProvider = (IBlueprintDataProvider) tileEntity;
            if (blueprintDataProvider.getSchematicName().isEmpty())
            {
                return;
            }

            setCorners(blueprintDataProvider.getInWorldCorners().getA(), blueprintDataProvider.getInWorldCorners().getB());

            int level = 0;
            try
            {
                level = Integer.parseInt(blueprintDataProvider.getSchematicName().substring(blueprintDataProvider.getSchematicName().length() - 1));
            }
            catch (NumberFormatException e)
            {

            }

            if (level > 0 && level >= getBuildingLevel() && level <= getMaxBuildingLevel())
            {
                if (level > getBuildingLevel())
                {
                    FireworkUtils.spawnFireworksAtAABBCorners(getCorners(), colony.getWorld(), level);
                }
                
                setBuildingLevel(level);
                onUpgradeComplete(level);
            }
        }
    }

    @Override
    public void onUpgradeSchematicTo(final String oldSchematic, final String newSchematic, final IBlueprintDataProvider blueprintDataProvider)
    {
        upgradeBuildingLevelToSchematicData();
    }

    @Override
    public final BuildingEntry getBuildingType()
    {
        return buildingType;
    }

    @Override
    public void setBuildingType(final BuildingEntry buildingType)
    {
        this.buildingType = buildingType;
    }
}
