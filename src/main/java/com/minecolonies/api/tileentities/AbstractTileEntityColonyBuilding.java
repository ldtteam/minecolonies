package com.minecolonies.api.tileentities;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.IItemHandlerCapProvider;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractTileEntityColonyBuilding extends TileEntityRack implements IBlueprintDataProviderBE
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

    public AbstractTileEntityColonyBuilding(final BlockEntityType<? extends AbstractTileEntityColonyBuilding> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Finds the first @see ItemStack the type of {@code is}. It will be taken from the chest and placed in the worker inventory. Make sure that the worker stands next the chest to
     * not break immersion. Also make sure to have inventory space for the stack.
     *
     * @param entity                      the tileEntity chest or building.
     * @param itemStackSelectionPredicate the itemStack predicate.
     * @return true if found the stack.
     */
        public static boolean isInTileEntity(final IItemHandlerCapProvider entity, @NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
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
    public abstract boolean hasAccessPermission(Player player);

    /**
     * @param rotationMirror rotation and mirror of the entity.
     */
    public abstract void setRotationMirror(RotationMirror rotationMirror);

    /**
     * @return rotation and mirror of the entity.
     */
    public abstract RotationMirror getRotationMirror();

    /**
     * Getter for the style.
     *
     * @return the pack of it.
     */
    public abstract StructurePackMeta getStructurePack();

    /**
     * Set the pack of the tileEntity.
     *
     * @param style the pack to set.
     */
    public abstract void setStructurePack(final StructurePackMeta style);

    /**
     * Set the blueprint path of the tileEntity.
     *
     * @param path the path to set.
     */
    public abstract void setBlueprintPath(final String path);

    /**
     * Get the blueprint path of the tileEntity.
     *
     * @return  path the path to get.
     */
    public abstract String getBlueprintPath();

    /**
     * Get the building name that this {@link AbstractTileEntityColonyBuilding} belongs to.
     *
     * @return The buildings name.
     */
    public abstract ResourceLocation getBuildingName();

    @Override
    public String getSchematicName()
    {
        return schematicName.replace(".blueprint", "");
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
        setChanged();
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
        setChanged();
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.loadAdditional(compound, provider);
        readSchematicDataFromNBT(compound);
        this.version = compound.getInt(TAG_VERSION);
    }

    @Override
    public void readSchematicDataFromNBT(final CompoundTag originalCompound)
    {
        final String old = getSchematicName();
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(originalCompound);

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

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.saveAdditional(compound, provider);
        writeSchematicDataToNBT(compound);
        compound.putInt(TAG_VERSION, this.version);
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
