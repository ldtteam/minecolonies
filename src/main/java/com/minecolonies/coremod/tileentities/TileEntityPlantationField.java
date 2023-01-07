package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.api.tileentities.AbstractTileEntityPlantationField;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Implementation for plantation field tile entities.
 */
public class TileEntityPlantationField extends AbstractTileEntityPlantationField implements IBlueprintDataProvider
{
    /**
     * The schematic name of the placeholder block.
     */
    private String schematicName = "";

    /**
     * Corner positions of schematic, relative to te pos.
     */
    private BlockPos corner1 = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    /**
     * The plantation field type.
     */
    private PlantationFieldType plantationFieldType;

    /**
     * A list of all found tagged working positions.
     */
    private List<BlockPos> workingPositions;

    /**
     * Default constructor.
     *
     * @param pos   The positions this tile entity is at.
     * @param state The state the entity is in.
     */
    public TileEntityPlantationField(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.PLANTATION_FIELD, pos, state);
    }

    @Override
    public String getSchematicName()
    {
        return schematicName;
    }

    @Override
    public void setSchematicName(final String s)
    {
        this.schematicName = s;
    }

    @Override
    public Map<BlockPos, List<String>> getPositionedTags()
    {
        return tagPosMap;
    }

    @Override
    public void setPositionedTags(final Map<BlockPos, List<String>> positionedTags)
    {
        tagPosMap = positionedTags;
        readPlantationFields();
        setChanged();
    }

    @Override
    public Tuple<BlockPos, BlockPos> getSchematicCorners()
    {
        if (corner1 == BlockPos.ZERO || corner2 == BlockPos.ZERO)
        {
            return new Tuple<>(worldPosition, worldPosition);
        }

        return new Tuple<>(corner1, corner2);
    }

    @Override
    public void setSchematicCorners(final BlockPos pos1, final BlockPos pos2)
    {
        corner1 = pos1;
        corner2 = pos2;
    }

    @Override
    public void readSchematicDataFromNBT(final CompoundTag originalCompound)
    {
        IBlueprintDataProvider.super.readSchematicDataFromNBT(originalCompound);
        readPlantationFields();
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    private void readPlantationFields()
    {
        plantationFieldType = tagPosMap.values().stream()
                                .flatMap(Collection::stream)
                                .map(PlantationModuleRegistry::getFromFieldTag)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null);

        PlantationModule module = PlantationModuleRegistry.getPlantationModule(plantationFieldType);
        if (module != null)
        {
            workingPositions = tagPosMap.entrySet().stream()
                                 .filter(f -> f.getValue().contains(module.getWorkTag()))
                                 .map(Map.Entry::getKey)
                                 .map(worldPosition::offset)
                                 .toList();
        }
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        if (compound != null)
        {
            this.load(compound);
        }
    }

    @Override
    public void load(final @NotNull CompoundTag compound)
    {
        super.load(compound);
        readSchematicDataFromNBT(compound);
    }

    @Override
    public void saveAdditional(final @NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);
        writeSchematicDataToNBT(compound);
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithId();
    }

    @Override
    public PlantationFieldType getPlantationFieldType()
    {
        return plantationFieldType;
    }

    @Override
    public List<BlockPos> getWorkingPositions()
    {
        return workingPositions;
    }
}
