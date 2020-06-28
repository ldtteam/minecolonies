package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
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
    private String schematicName        = "";

    /**
     * The current level.
     */
    private int level = 0;

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
     * @return String name.
     */
    public String getSchematicName()
    {
        return schematicName;
    }

    /**
     * Setter for the schematic name connected to this.
     * @param schematicName the name to set.
     */
    public void setSchematicName(final String schematicName)
    {
        this.schematicName = schematicName;
        this.update();
    }

    /**
     * Getter for the deco level associated.
     * @return the level.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Set the deco level.
     * @param level the max.
     */
    public void setLevel(final int level)
    {
        this.level = level;
        this.update();
    }

    /**
     * Set the basic facing of this block.
     * @param basicFacing the basic facing.
     */
    public void setBasicFacing(final Direction basicFacing)
    {
        this.basicFacing = basicFacing;
    }

    /**
     * Get the basic facing of the block.
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
        this.markDirty();
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
    public Tuple<BlockPos, BlockPos> getCornerPositions()
    {
        if (corner1 == BlockPos.ZERO || corner2 == BlockPos.ZERO)
        {
            return new Tuple<>(pos, pos);
        }

        return new Tuple<>(corner1, corner2);
    }

    @Override
    public void setCorners(final BlockPos pos1, final BlockPos pos2)
    {
        corner1 = pos1;
        corner2 = pos2;
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
        readSchematicDataFromNBT(compound);
        this.schematicName = compound.getString(TAG_NAME);
        this.level = compound.getInt(TAG_LEVEL);
        this.basicFacing = Direction.byHorizontalIndex(compound.getInt(TAG_FACING));
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);
        writeSchematicDataToNBT(compound);
        compound.putString(TAG_NAME, schematicName);
        compound.putInt(TAG_LEVEL, level);
        compound.putInt(TAG_FACING, basicFacing.getHorizontalIndex());
        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0x9, this.getUpdateTag());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getNbtCompound();
        this.read(compound);
    }

    @Override
    public BlockPos getTilePos()
    {
        return pos;
    }
}
