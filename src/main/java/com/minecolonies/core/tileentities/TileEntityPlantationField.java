package com.minecolonies.core.tileentities;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.tileentities.AbstractTileEntityPlantationField;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Implementation for plantation field tile entities.
 */
public class TileEntityPlantationField extends AbstractTileEntityPlantationField
{
    /**
     * Cached result for {@link TileEntityPlantationField#getWorkingPositions(String)} ()}.
     */
    private final Map<String, List<BlockPos>> workingPositions = new HashMap<>();

    /**
     * The schematic name of the placeholder block.
     */
    private String schematicName = "";

    /**
     * The schematic path of the placeholder block.
     */
    private String schematicPath = "";

    /**
     * The packName it is included in.
     */
    private String packName = "";

    /**
     * Corner positions of schematic, relative to te pos.
     */
    private BlockPos corner1 = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    /**
     * The used rotation and mirror.
     */
    private RotationMirror rotationMirror;

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    /**
     * The colony this plantation field is located in.
     */
    private IColony currentColony;

    /**
     * Cached result for {@link TileEntityPlantationField#getPlantationFieldTypes()}.
     */
    private Set<FieldRegistries.FieldEntry> plantationFieldTypes;

    /**
     * Default constructor.
     *
     * @param pos   The positions this tile entity is at.
     * @param state The state the entity is in.
     */
    public TileEntityPlantationField(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.PLANTATION_FIELD.get(), pos, state);
    }

    @Override
    public Set<FieldRegistries.FieldEntry> getPlantationFieldTypes()
    {
        if (plantationFieldTypes == null)
        {
            plantationFieldTypes = tagPosMap.values().stream()
                                     .flatMap(Collection::stream)
                                     .map(this::getPlantationFieldEntryFromFieldTag)
                                     .filter(Objects::nonNull)
                                     .collect(Collectors.toSet());
        }
        return plantationFieldTypes;
    }

    @Override
    public List<BlockPos> getWorkingPositions(final String tag)
    {
        workingPositions.computeIfAbsent(tag, newTag -> tagPosMap.entrySet().stream()
                                                          .filter(f -> f.getValue().contains(newTag))
                                                          .distinct()
                                                          .map(Map.Entry::getKey)
                                                          .map(worldPosition::offset)
                                                          .toList());
        return workingPositions.get(tag);
    }

    @Override
    public IColony getCurrentColony()
    {
        if (currentColony == null && level != null)
        {
            this.currentColony = IColonyManager.getInstance().getIColony(level, worldPosition);
        }
        return currentColony;
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

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Get the rotation of the controller.
     *
     * @return the placed rotation.
     */
    @Override
    public RotationMirror getRotationMirror()
    {
        return rotationMirror;
    }

    private FieldRegistries.FieldEntry getPlantationFieldEntryFromFieldTag(String fieldTag)
    {
        return FieldRegistries.getFieldRegistry().stream()
                 .filter(fieldEntry -> {
                     List<IPlantationModule> modules = fieldEntry.getFieldModuleProducers().stream().map(m -> m.apply(null))
                                                         .filter(IPlantationModule.class::isInstance)
                                                         .map(m -> (IPlantationModule) m)
                                                         .toList();

                     return modules.stream().anyMatch(module -> module.getFieldTag().equals(fieldTag));
                 })
                 .findFirst()
                 .orElse(null);
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
        setChanged();
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
        setChanged();
    }

    @Override
    public void readSchematicDataFromNBT(final CompoundTag compound)
    {
        super.readSchematicDataFromNBT(compound);
        final CompoundTag blueprintDataProvider = compound.getCompound(TAG_BLUEPRINTDATA);
        if (compound.contains(TAG_PACK)) // New structure
        {
            // path is the folder containing the schematic
            final String path = blueprintDataProvider.getString(TAG_NAME);
            this.schematicPath = path + File.separator + this.schematicName + ".blueprint";
        }
        else
        {
            // This is only recovery handling for old structures, it shouldn't be called otherwise.
            if (compound.contains(TAG_NAME))
            {
                this.schematicPath = compound.getString(TAG_NAME);
                final String[] split = Utils.splitPath(this.schematicPath);
                this.schematicName = split[split.length - 1].replace(".blueprint", "");
            }

            final String[] split = Utils.splitPath(this.schematicPath);
            if (split.length >= 4)
            {
                this.packName = BlueprintMapping.getStyleMapping(split[2]);
            }

            if (this.packName == null || this.packName.isEmpty())
            {
                this.packName = DEFAULT_STYLE;
            }

            if (this.schematicName.contains("/") || this.schematicName.contains("\\"))
            {
                final String[] splitName = Utils.splitPath(this.schematicPath);
                this.schematicName = splitName[splitName.length - 1].replace(".blueprint", "");
            }

            if (StructurePacks.hasPack(this.packName))
            {
                this.schematicPath = StructurePacks.getStructurePack(this.packName).getSubPath(StructurePacks.findBlueprint(this.packName, schematicName));
            }
            else
            {
                this.schematicPath = this.schematicName;
            }

            if (!this.schematicPath.endsWith(".blueprint"))
            {
                this.schematicPath = this.schematicPath + ".blueprint";
            }
        }

        if (blueprintDataProvider.contains(TAG_PACK))
        {
            this.packName = blueprintDataProvider.getString(TAG_PACK);
        }

        if (this.packName == null)
        {
            this.packName = DEFAULT_STYLE;
        }
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    @Override
    public void rotateAndMirror(final RotationMirror rotMir)
    {
        this.rotationMirror = rotMir;
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        this.load(compound);
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);
        super.readSchematicDataFromNBT(compound);
        if (compound.contains(TAG_ROTATION_MIRROR, Tag.TAG_BYTE))
        {
            this.rotationMirror = RotationMirror.values()[compound.getByte(TAG_ROTATION_MIRROR)];
        }
        else
        {
            // TODO: remove this later (data break introduced in 1.20.4) because of blueprint data
            this.rotationMirror = RotationMirror.of(Rotation.values()[compound.getInt(TAG_ROTATION)], compound.getBoolean(TAG_MIRROR) ? Mirror.FRONT_BACK : Mirror.NONE);
        }
        if (compound.contains(TAG_PATH))
        {
            this.schematicPath = compound.getString(TAG_PATH);
        }

        if (compound.contains(TAG_NAME))
        {
            this.schematicName = compound.getString(TAG_NAME);
            if (this.schematicPath == null || this.schematicPath.isEmpty())
            {
                //Setup for recovery
                this.schematicPath = this.schematicName;
                this.schematicName = "";
            }
        }
        this.packName = compound.getString(TAG_PACK);

        if (!this.schematicPath.endsWith(".blueprint"))
        {
            this.schematicPath = this.schematicPath + ".blueprint";
        }
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        super.saveAdditional(compound);
        writeSchematicDataToNBT(compound);
        compound.putByte(TAG_ROTATION_MIRROR, (byte) this.rotationMirror.ordinal());
        compound.putString(TAG_NAME, schematicName == null ? "" : schematicName);
        compound.putString(TAG_PATH, schematicPath == null ? "" : schematicPath);
        compound.putString(TAG_PACK, (packName == null || packName.isEmpty()) ? "" : packName);
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithId();
    }

    @Override
    public void setBlueprintPath(final String filePath)
    {
        this.schematicPath = filePath;
        if (!this.schematicPath.endsWith(".blueprint"))
        {
            this.schematicPath = this.schematicPath + ".blueprint";
        }
        setChanged();
    }

    @Override
    public void setPackName(final String packName)
    {
        this.packName = packName;
        setChanged();
    }

    @Override
    public String getBlueprintPath()
    {
        return schematicPath;
    }

    /**
     * Getter for the pack.
     *
     * @return String name.
     */
    public String getPackName()
    {
        return packName;
    }
}