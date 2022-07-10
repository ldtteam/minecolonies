package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityDecorationController extends BlockEntity implements IBlueprintDataProvider
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
     * The current level.
     */
    private int tier = 0;

    /**
     * The basic direction this block is facing.
     */
    private Direction basicFacing = Direction.NORTH;

    /**
     * Corner positions of schematic, relative to te pos.
     */
    private BlockPos corner1 = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

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
     * Setter for the schematic name connected to this.
     *
     * @param schematicPath the name to set.
     */
    public void setSchematicPath(final String schematicPath)
    {
        this.schematicPath = schematicPath;
        if (super.level != null)
        {
            this.update();
        }
    }

    /**
     * Getter for the deco level associated.
     *
     * @return the level.
     */
    public int getTier()
    {
        return tier;
    }

    /**
     * Set the deco level.
     *
     * @param tier the max.
     */
    public void setTier(final int tier)
    {
        this.tier = tier;
        this.update();
    }

    /**
     * Set the basic facing of this block.
     *
     * @param basicFacing the basic facing.
     */
    public void setBasicFacing(final Direction basicFacing)
    {
        this.basicFacing = basicFacing;
    }

    /**
     * Get the basic facing of the block.
     *
     * @return the basic facing.
     */
    public Direction getBasicFacing()
    {
        return basicFacing;
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
        IBlueprintDataProvider.super.readSchematicDataFromNBT(compound);
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
            this.packName = BlueprintMapping.styleMapping.get(split[1]);
            this.schematicPath = StructurePacks.findBlueprint(this.packName, schematicName).toString().replace(StructurePacks.packMetas.get(packName).getPath().toString(), "");
        }

        if (compound.contains(TAG_LEVEL))
        {
            this.tier = compound.getInt(TAG_LEVEL);
        }

        if (compound.contains(TAG_FACING))
        {
            this.basicFacing = Direction.from2DDataValue(compound.getInt(TAG_FACING));
        }
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);
        IBlueprintDataProvider.super.readSchematicDataFromNBT(compound);
        this.schematicPath = compound.getString(TAG_NAME);
        this.tier = compound.getInt(TAG_LEVEL);
        this.basicFacing = Direction.from2DDataValue(compound.getInt(TAG_FACING));
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        super.saveAdditional(compound);
        writeSchematicDataToNBT(compound);
        compound.putString(TAG_NAME, schematicPath);
        compound.putString(TAG_PACK, packName);
        compound.putInt(TAG_LEVEL, tier);
        compound.putInt(TAG_FACING, basicFacing.get2DDataValue());
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
}
