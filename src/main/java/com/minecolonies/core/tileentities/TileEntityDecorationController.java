package com.minecolonies.core.tileentities;

import com.ldtteam.structurize.api.util.IRotatableBlockEntity;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.util.BuildingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityDecorationController extends BlockEntity implements IBlueprintDataProviderBE, IRotatableBlockEntity
{
    /**
     * The schematic name of the placerholder block.
     */
    private String schematicName = "";

    /**
     * The schematic path of the placerholder block.
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
     * The used rotation/mirror.
     */
    private int cachedRotation = -1;
    private boolean isMirrored = false;

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    public TileEntityDecorationController(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.DECO_CONTROLLER.get(), pos, state);
    }

    @Override
    public String getPackName()
    {
        return packName;
    }

    @Override
    public String getBlueprintPath()
    {
        return schematicPath;
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

    /**
     * Trigger update action.
     */
    private void update()
    {
        this.setChanged();
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
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
    public void readSchematicDataFromNBT(CompoundTag compound)
    {
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(compound);
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

            if (compound.contains(TAG_LEVEL))
            {
                this.schematicName += compound.getInt(TAG_LEVEL);
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
    public void load(@NotNull final CompoundTag compound)
    {
        super.load(compound);
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(compound);
        this.cachedRotation = -1;
        this.isMirrored = compound.getBoolean(TAG_MIRROR);

        // inexplicably IBlueprintDataProviderBE does not load the pack/path even though it saved them
        this.packName = compound.getCompound(TAG_BLUEPRINTDATA).getString(TAG_PACK);
        this.schematicPath = compound.getCompound(TAG_BLUEPRINTDATA).getString(TAG_PATH);

        // the rest of this is backwards compat code that can be removed at some point (maybe even now)
        if(compound.contains(TAG_PATH) && StringUtils.isEmpty(this.schematicPath))
        {
            this.schematicPath = compound.getString(TAG_PATH);
        }
        if(compound.contains(TAG_PACK) && StringUtils.isEmpty(this.packName))
        {
            this.packName = compound.getString(TAG_PACK);
        }
        if(compound.contains(TAG_NAME) && StringUtils.isEmpty(this.schematicName))
        {
            this.schematicName = compound.getString(TAG_NAME);
            if (this.schematicPath == null || this.schematicPath.isEmpty())
            {
                //Setup for recovery
                this.schematicPath = this.schematicName;
                this.schematicName = "";
            }
        }
        // end of backwards compat code

        if (!this.schematicPath.endsWith(".blueprint"))
        {
            this.schematicPath = this.schematicPath + ".blueprint";
        }
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        writeSchematicDataToNBT(compound);
        compound.putBoolean(TAG_MIRROR, this.isMirrored);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
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

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithId();
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        this.load(compound);
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    @Override
    public void rotate(final Rotation rotationIn)
    {
        this.cachedRotation = -1;
    }

    @Override
    public void mirror(final Mirror mirror)
    {
        this.isMirrored = mirror != Mirror.NONE;
    }

    /**
     * Get the rotation of the controller.
     * @return the placed rotation.
     */
    public Rotation getRotation()
    {
        if (this.cachedRotation == -1)
        {
            this.cachedRotation = BuildingUtils.getRotationFromBlueprint(getLevel(), getBlockPos());
        }
        return BlockPosUtil.getRotationFromRotations(this.cachedRotation);
    }

    /**
     * Get the mirroring setting of the controller.
     * @return true if mirrored.
     */
    public boolean getMirror()
    {
        return this.isMirrored;
    }
}
