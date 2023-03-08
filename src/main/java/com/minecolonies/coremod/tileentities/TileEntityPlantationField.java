package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityPlantationField;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

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
    public Set<PlantationFieldType> getPlantationFieldTypes()
    {
        return tagPosMap.values().stream()
                 .flatMap(Collection::stream)
                 .map(PlantationModuleRegistry::getFromFieldTag)
                 .filter(Objects::nonNull)
                 .collect(Collectors.toSet());
    }

    @Override
    public List<BlockPos> getWorkingPositions(final String tag)
    {
        return tagPosMap.entrySet().stream()
                 .filter(f -> f.getValue().contains(tag))
                 .distinct()
                 .map(Map.Entry::getKey)
                 .map(worldPosition::offset)
                 .toList();
    }

    /**
     * Check condition whether the field UI can be opened or not.
     *
     * @param player the player attempting to open the menu.
     * @return whether the player is authorized to open this menu.
     */
    @Override
    public boolean canOpenMenu(@NotNull Player player)
    {
        IColony colony = getCurrentColony();
        if (colony != null)
        {
            return colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS);
        }
        return false;
    }

    @Override
    public String getSchematicName()
    {
        return schematicName;
    }

    @Override
    @Nullable
    public ResourceKey<Level> getDimension()
    {
        IColony colony = getCurrentColony();
        if (colony != null)
        {
            return colony.getDimension();
        }
        return null;
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
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    private IColony getCurrentColony()
    {
        return level != null ? IColonyManager.getInstance().getIColony(level, worldPosition) : null;
    }
}
