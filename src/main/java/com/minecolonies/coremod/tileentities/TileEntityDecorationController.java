package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.api.util.IRotatableBlockEntity;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityDecorationController extends BlockEntity implements IBlueprintDataProviderBE, IRotatableBlockEntity
{
    /**
     * Tag to store the basic facing to NBT
     */
    private static final String TAG_FACING = "facing";

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
     * The used rotation.
     */
    private Rotation rotation = Rotation.NONE;

    /**
     * The used mirror.
     */
    private boolean mirror;

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    public TileEntityDecorationController(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.DECO_CONTROLLER, pos, state);
    }

    /**
     * Getter for the name stored in this.
     *
     * @return String name.
     */
    public String getSchematicPath()
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
    }

    @Override
    public void readSchematicDataFromNBT(CompoundTag compound)
    {
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(compound);
        if (compound.contains(TAG_NAME))
        {
            this.schematicPath = compound.getString(TAG_NAME);
        }

        if (compound.contains(TAG_PACK))
        {
            this.packName = compound.getString(TAG_PACK);
        }
        else
        {
            final String[] split = this.schematicPath.split("/");
            if (split.length == 4)
            {
                this.packName = BlueprintMapping.styleMapping.get(split[2]);
            }
            else
            {
                this.packName = DEFAULT_STYLE;
            }
            this.schematicPath = StructurePacks.getStructurePack(this.packName).getSubPath(StructurePacks.findBlueprint(this.packName, schematicName));
        }

        if (this.packName == null)
        {
            this.packName = DEFAULT_STYLE;
        }
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(compound);
        this.rotation = Rotation.values()[compound.getInt(TAG_ROTATION)];
        this.mirror = compound.getBoolean(TAG_MIRROR);
        this.schematicPath = compound.getString(TAG_NAME);
        this.packName = compound.getString(TAG_PACK);
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        super.saveAdditional(compound);
        writeSchematicDataToNBT(compound);
        compound.putInt(TAG_ROTATION, this.rotation.ordinal());
        compound.putBoolean(TAG_MIRROR, this.mirror);
        compound.putString(TAG_NAME, schematicPath);
        compound.putString(TAG_PACK, packName);
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
    }

    @Override
    public void setPackName(final String packName)
    {
        this.packName = packName;
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
        this.rotation = rotationIn;
    }

    @Override
    public void mirror(final Mirror mirror)
    {
        this.mirror = mirror != Mirror.NONE;
    }

    /**
     * Get the rotation of the controller.
     * @return the placed rotation.
     */
    public Rotation getRotation()
    {
        return rotation;
    }

    /**
     * Get the mirroring setting of the controller.
     * @return true if mirrored.
     */
    public boolean getMirror()
    {
        return this.mirror;
    }
}
