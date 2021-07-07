package com.minecolonies.api.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryFunctions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractTileEntityColonyBuilding extends TileEntityRack implements IBlueprintDataProvider
{
    /**
     * Version of the TE data.
     */
    private static final String TAG_VERSION = "version";
    private static final int    VERSION     = 2;

    /**
     * Corner positions of schematic, relative to te pos.
     */
    private BlockPos corner1             = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    /**
     * The TE's schematic name
     */
    private String schematicName = "";

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    /**
     * Check if the building might have old data.
     */
    private int version = 0;

    public AbstractTileEntityColonyBuilding(final TileEntityType<? extends AbstractTileEntityColonyBuilding> type)
    {
        super(type);
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}. It will be taken from the chest and placed in the worker inventory. Make sure that the worker stands next the chest to
     * not break immersion. Also make sure to have inventory space for the stack.
     *
     * @param entity                      the tileEntity chest or building.
     * @param itemStackSelectionPredicate the itemStack predicate.
     * @return true if found the stack.
     */
    public static boolean isInTileEntity(final ICapabilityProvider entity, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return InventoryFunctions.matchFirstInProvider(entity, itemStackSelectionPredicate);
    }

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    public abstract int getColonyId();

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    public abstract IColony getColony();

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    public abstract void setColony(IColony c);

    /**
     * Returns the position of the tile entity.
     *
     * @return Block Coordinates of the tile entity.
     */
    public abstract BlockPos getPosition();

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Nullable
    public abstract BlockPos getPositionOfChestWithItemStack(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Returns the building associated with the tile entity.
     *
     * @return {@link IBuildingContainer} associated with the tile entity.
     */
    public abstract IBuilding getBuilding();

    /**
     * Sets the building associated with the tile entity.
     *
     * @param b {@link IBuildingContainer} to associate with the tile entity.
     */
    public abstract void setBuilding(IBuilding b);

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link IBuildingView} the tile entity is associated with.
     */
    public abstract IBuildingView getBuildingView();

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    public abstract boolean hasAccessPermission(PlayerEntity player);

    /**
     * Set if the entity is mirrored.
     *
     * @param mirror true if so.
     */
    public abstract void setMirror(boolean mirror);

    /**
     * Check if building is mirrored.
     *
     * @return true if so.
     */
    public abstract boolean isMirrored();

    /**
     * Getter for the style.
     *
     * @return the string of it.
     */
    public abstract String getStyle();

    /**
     * Set the style of the tileEntity.
     *
     * @param style the style to set.
     */
    public abstract void setStyle(String style);

    /**
     * Get the building name that this {@link AbstractTileEntityColonyBuilding} belongs to.
     *
     * @return The buildings name.
     */
    public abstract ResourceLocation getBuildingName();

    @Override
    public String getSchematicName()
    {
        return schematicName;
    }

    @Override
    public void setSchematicName(final String name)
    {
        schematicName = name;
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
        return new Tuple<>(corner1, corner2);
    }

    @Override
    public void setSchematicCorners(final BlockPos pos1, final BlockPos pos2)
    {
        corner1 = pos1;
        corner2 = pos2;
    }

    @Override
    public void load(final BlockState state, @NotNull final CompoundNBT compound)
    {
        super.load(state, compound);
        readSchematicDataFromNBT(compound);
        this.version = compound.getInt(TAG_VERSION);
    }

    @Override
    public void readSchematicDataFromNBT(final CompoundNBT originalCompound)
    {
        final String old = getSchematicName();
        IBlueprintDataProvider.super.readSchematicDataFromNBT(originalCompound);

        if (level == null || level.isClientSide || getColony() == null || getColony().getBuildingManager() == null)
        {
            return;
        }

        final IBuilding building = getColony().getBuildingManager().getBuilding(worldPosition);
        if (building != null)
        {
            building.onUpgradeSchematicTo(old, getSchematicName(), this);
        }
        this.version = VERSION;
    }

    @NotNull
    @Override
    public CompoundNBT save(@NotNull final CompoundNBT compound)
    {
        super.save(compound);
        writeSchematicDataToNBT(compound);
        compound.putInt(TAG_VERSION, this.version);
        return compound;
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    /**
     * Check if the TE is on an old data version.
     * @return true if so.
     */
    public boolean isOutdated()
    {
        return version < VERSION;
    }
}
