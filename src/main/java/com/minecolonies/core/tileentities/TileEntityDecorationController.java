package com.minecolonies.core.tileentities;
import net.minecraft.nbt.CompoundTag;

import com.ldtteam.structurize.api.util.IRotatableBlockEntity;
import com.ldtteam.structurize.util.RotationMirror;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.ldtteam.structurize.api.util.Tuple;
import net.minecraft.core.BlockPos;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * The used rotation and mirror.
     */
    private RotationMirror rotationMirror = RotationMirror.NONE;

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
        final CompoundTag blueprintDataProvider = compound.getCompoundOrEmpty(TAG_BLUEPRINTDATA);

        this.packName = blueprintDataProvider.getStringOr(TAG_PACK, "");
        this.schematicPath = blueprintDataProvider.getStringOr(TAG_PATH, "");
    }

    @Override
    public void loadAdditional(final ValueInput compound)
    {
        super.loadAdditional(compound);
        compound.read(TAG_BLUEPRINTDATA, CompoundTag.CODEC)
          .ifPresent(data -> IBlueprintDataProviderBE.super.readSchematicDataFromNBT(data));
        final byte rotationMirrorId = compound.getByteOr(TAG_ROTATION_MIRROR, (byte) -1);
        if (rotationMirrorId >= 0)
        {
            this.rotationMirror = RotationMirror.values()[rotationMirrorId];
        }
        else
        {
            // TODO: remove this later (data break introduced in 1.20.4) because of blueprint data
            this.rotationMirror = RotationMirror.of(Rotation.values()[compound.getIntOr(TAG_ROTATION, 0)], compound.getBooleanOr(TAG_MIRROR, false) ? Mirror.FRONT_BACK : Mirror.NONE);
        }

        // inexplicably IBlueprintDataProviderBE does not load the pack/path even though it saved them
        final ValueInput blueprintDataProvider = compound.childOrEmpty(TAG_BLUEPRINTDATA);
        this.packName = blueprintDataProvider.getStringOr(TAG_PACK, "");
        this.schematicPath = blueprintDataProvider.getStringOr(TAG_PATH, "");

        // the rest of this is backwards compat code that can be removed at some point (maybe even now)
        if(compound.getString(TAG_PATH).isPresent() && StringUtils.isEmpty(this.schematicName))
        {
            this.schematicPath = compound.getStringOr(TAG_PATH, "");
        }
        if(compound.getString(TAG_PACK).isPresent() && StringUtils.isEmpty(this.packName))
        {
            this.packName = compound.getStringOr(TAG_PACK, "");
        }
        if(compound.getString(TAG_NAME).isPresent() && StringUtils.isEmpty(this.schematicName))
        {
            this.schematicName = compound.getStringOr(TAG_NAME, "");
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
    public void saveAdditional(final ValueOutput compound)
    {
        super.saveAdditional(compound);
        final CompoundTag schematicData = new CompoundTag();
        writeSchematicDataToNBT(schematicData);
        compound.store(TAG_BLUEPRINTDATA, CompoundTag.CODEC, schematicData);
        compound.putByte(TAG_ROTATION_MIRROR, (byte) this.rotationMirror.ordinal());
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
    public CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider)
    {
        return this.saveWithFullMetadata(provider);
    }

    @Override
    public void onDataPacket(final Connection net, final ValueInput compound)
    {
        this.loadAdditional(compound);
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    public void rotateAndMirror(final RotationMirror rotationMirror)
    {
        this.rotationMirror = rotationMirror;
    }

    @Override
    public void rotate(final Rotation rotation)
    {
        this.rotationMirror = this.rotationMirror.rotate(rotation);
    }

    @Override
    public void mirror(final Mirror mirror)
    {
        this.rotationMirror = this.rotationMirror.mirrorate(mirror);
    }

    /**
     * Get the rotation of the controller.
     * @return the placed rotation and mirror.
     */
    public RotationMirror getRotationMirror()
    {
        return rotationMirror;
    }
}
