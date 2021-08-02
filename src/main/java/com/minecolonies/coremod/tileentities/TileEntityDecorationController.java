package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

public class TileEntityDecorationController extends TileEntity implements IBlueprintDataProvider
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
     * The schematic name of the placerholder block.
     */
    private String schematicPath = "";

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

    public TileEntityDecorationController()
    {
        super(MinecoloniesTileEntities.DECO_CONTROLLER);
    }

    /**
     * Geter for the name stored in this.
     *
     * @return String name.
     */
    public String getSChematicPath()
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
    public void readSchematicDataFromNBT(CompoundNBT compound)
    {
        IBlueprintDataProvider.super.readSchematicDataFromNBT(compound);
        if (compound.contains(TAG_NAME))
        {
            this.schematicPath = compound.getString(TAG_NAME);
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
    public void load(final BlockState state, final CompoundNBT compound)
    {
        super.load(state, compound);
        IBlueprintDataProvider.super.readSchematicDataFromNBT(compound);
        this.schematicPath = compound.getString(TAG_NAME);
        this.tier = compound.getInt(TAG_LEVEL);
        this.basicFacing = Direction.from2DDataValue(compound.getInt(TAG_FACING));
    }

    @NotNull
    @Override
    public CompoundNBT save(final CompoundNBT compound)
    {
        super.save(compound);
        writeSchematicDataToNBT(compound);
        compound.putString(TAG_NAME, schematicPath);
        compound.putInt(TAG_LEVEL, tier);
        compound.putInt(TAG_FACING, basicFacing.get2DDataValue());
        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 0x9, this.getUpdateTag());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getTag();
        this.load(getBlockState(), compound);
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }
}
