package com.minecolonies.core.tileentities;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.inventory.container.ContainerBuildingInventory;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.ITickable;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.IItemHandlerCapProvider;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ROTATION_MIRROR;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TileEntityColonyBuilding extends AbstractTileEntityColonyBuilding implements ITickable
{
    /**
     * NBTTag to store the colony id.
     */
    private static final String TAG_COLONY = "colony";
    private static final String TAG_MIRROR = "mirror";
    private static final String TAG_STYLE  = "style";
    private static final String TAG_PACK   = "pack";
    private static final String TAG_PATH   = "path";

    /**
     * The colony id.
     */
    private int colonyId = 0;

    /**
     * The colony.
     */
    private IColony colony;

    /**
     * The building the tileEntity belongs to.
     */
    private IBuilding building;

    /**
     * Check if the building has a mirror.
     */
    @Nullable
    private RotationMirror rotationMirror;

    /**
     * The style of the building.
     */
    private String packMeta = "";

    /**
     * Path of the blueprint.
     */
    private String path = "";

    /**
     * The name of the building location.
     */
    public ResourceLocation registryName;

    /**
     * Create the combined inv wrapper for the building.
     */
    private CombinedItemHandler combinedInv;

    /**
     * Pending blueprint future.
     */
    private Future<Blueprint> pendingBlueprintFuture = null;

    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityColonyBuilding(final BlockPos pos, final BlockState state)
    {
        this(MinecoloniesTileEntities.BUILDING.get(), pos, state);
    }

    /**
     * Alternative overriden constructor.
     *
     * @param type the entity type.
     */
    public TileEntityColonyBuilding(final BlockEntityType<? extends AbstractTileEntityColonyBuilding> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    @Override
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    @Override
    public IColony getColony()
    {
        if (colony == null)
        {
            updateColonyReferences();
        }
        return colony;
    }

    /**
     * Synchronises colony references from the tile entity.
     */
    private void updateColonyReferences()
    {
        if (colony == null && getLevel() != null)
        {
            if (colonyId == 0)
            {
                colony = IColonyManager.getInstance().getColonyByPosFromWorld(getLevel(), this.getBlockPos());
            }
            else
            {
                colony = IColonyManager.getInstance().getColonyByWorld(colonyId, getLevel());
            }

            // It's most probably previewed building, please don't spam it here.
            if (colony == null && !getLevel().isClientSide)
            {
                //log on the server
                //Log.getLogger().info(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] had colony.",getWorld().getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
            }
        }

        if (building == null && colony != null)
        {
            building = colony.getBuildingManager().getBuilding(getPosition());
            if (building != null && (getLevel() == null || !getLevel().isClientSide))
            {
                registryName = building.getBuildingType().getRegistryName();
                building.setTileEntity(this);
            }
        }
    }

    /**
     * Returns the position of the tile entity.
     *
     * @return Block Coordinates of the tile entity.
     */
    @Override
    public BlockPos getPosition()
    {
        return worldPosition;
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Override
    @Nullable
    public BlockPos getPositionOfChestWithItemStack(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        final Predicate<ItemStack> notEmptyPredicate = itemStackSelectionPredicate.and(ItemStackUtils.NOT_EMPTY_PREDICATE);
        @Nullable final IBuildingContainer theBuilding = getBuilding();

        if (theBuilding != null)
        {
            for (final BlockPos pos : theBuilding.getContainers())
            {
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = getLevel().getBlockEntity(pos);
                    if (entity instanceof final AbstractTileEntityRack rack)
                    {
                        if (rack.hasItemStack(notEmptyPredicate))
                        {
                            return pos;
                        }
                    }
                    else if (isInTileEntity(IItemHandlerCapProvider.wrap(entity), notEmptyPredicate))
                    {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    @Override
    public void setColony(final IColony c)
    {
        colony = c;
        colonyId = c.getID();
        setChanged();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        if (building != null)
        {
            building.markDirty();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider)
    {
        return saveWithId(provider);
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag, @NotNull final HolderLookup.Provider provider)
    {
        this.loadAdditional(tag, provider);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet, @NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = packet.getTag();
        colonyId = compound.getInt(TAG_COLONY);
        super.onDataPacket(net, packet, provider);
    }

    @Override
    public void onLoad()
    {
        if (building != null)
        {
            building.setTileEntity(null);
        }
    }

    /**
     * Returns the building associated with the tile entity.
     *
     * @return {@link IBuildingContainer} associated with the tile entity.
     */
    @Override
    public IBuilding getBuilding()
    {
        if (building == null)
        {
            updateColonyReferences();
        }
        return building;
    }

    /**
     * Sets the building associated with the tile entity.
     *
     * @param b {@link IBuilding} to associate with the tile entity.
     */
    @Override
    public void setBuilding(final IBuilding b)
    {
        building = b;
    }

    @NotNull
    @Override
    public Component getDisplayName()
    {
        return getBlockState().getBlock().getName();
    }

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link IBuildingView} the tile entity is associated with.
     */
    @Override
    public IBuildingView getBuildingView()
    {
        final IColonyView c = IColonyManager.getInstance().getColonyView(colonyId, level.dimension());
        return c == null ? null : c.getBuilding(getPosition());
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.loadAdditional(compound, provider);
        if (compound.contains(TAG_COLONY))
        {
            colonyId = compound.getInt(TAG_COLONY);
        }

        if (compound.contains(TAG_ROTATION_MIRROR, Tag.TAG_BYTE))
        {
            rotationMirror = RotationMirror.values()[compound.getByte(TAG_ROTATION_MIRROR)];
        }

        String packName;
        String path;
        if (compound.contains(TAG_STYLE) && !compound.getString(TAG_STYLE).isEmpty())
        {
            packName = BlueprintMapping.getStyleMapping(compound.getString(TAG_STYLE));

            if (this.getSchematicName().isEmpty())
            {
                path = null;
            }
            else
            {
                final String level = this.getSchematicName().substring(this.getSchematicName().length() - 1);
                path = BlueprintMapping.getPathMapping(compound.getString(TAG_STYLE), this.getSchematicName().substring(0, this.getSchematicName().length() - 1)) + level
                         + ".blueprint";
            }
        }
        else
        {
            packName = compound.getString(TAG_PACK);
            path = compound.getString(TAG_PATH);
        }

        if (packName == null || packName.isEmpty())
        {
            final List<String> tags = new ArrayList<>(getPositionedTags().getOrDefault(BlockPos.ZERO, new ArrayList<>()));
            if (!tags.isEmpty())
            {
                tags.remove(DEACTIVATED);
                if (!tags.isEmpty())
                {
                    packName = BlueprintMapping.getStyleMapping(tags.get(0));
                    if (path == null || path.isEmpty())
                    {
                        path = BlueprintMapping.getPathMapping(tags.get(0), ((AbstractBlockHut) getBlockState().getBlock()).getBlueprintName()) + "1.blueprint";
                    }
                }
            }
            else if (StructurePacks.selectedPack != null)
            {
                packName = StructurePacks.selectedPack.getName();
            }
        }

        if (path == null || path.isEmpty() || path.contains("null"))
        {
            path = BlueprintMapping.getPathMapping("", ((AbstractBlockHut) getBlockState().getBlock()).getBlueprintName()) + "1.blueprint";
        }

        if (!path.endsWith(".blueprint"))
        {
            path += ".blueprint";
        }

        this.packMeta = packName;
        this.path = path;

        if (compound.contains(TAG_BUILDING_TYPE))
        {
            registryName = ResourceLocation.parse(compound.getString(TAG_BUILDING_TYPE));
        }
        buildingPos = worldPosition;
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.saveAdditional(compound, provider);
        compound.putInt(TAG_COLONY, colonyId);
        if (rotationMirror != null)
        {
            compound.putByte(TAG_ROTATION_MIRROR, (byte) rotationMirror.ordinal());
        }
        compound.putString(TAG_PACK, packMeta == null ? "" : packMeta);
        compound.putString(TAG_PATH, path == null ? "" : path);
        if (registryName != null)
        {
            compound.putString(TAG_BUILDING_TYPE, registryName.toString());
        }
    }

    @Override
    public void tick()
    {
        if (combinedInv != null)
        {
            invalidateCapabilities();
            combinedInv = null;
        }
        if (!getLevel().isClientSide && colonyId == 0)
        {
            final IColony tempColony = IColonyManager.getInstance().getColonyByPosFromWorld(getLevel(), this.getPosition());
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }
        }

        if (!getLevel().isClientSide && colonyId != 0 && colony == null)
        {
            updateColonyReferences();
        }

        if (pendingBlueprintFuture != null && pendingBlueprintFuture.isDone())
        {
            try
            {
                processBlueprint(pendingBlueprintFuture.get());
                pendingBlueprintFuture = null;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isUsableByPlayer(@NotNull final Player player)
    {
        return this.hasAccessPermission(player);
    }

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    @Override
    public boolean hasAccessPermission(final Player player)
    {
        // TODO This is called every tick the GUI is open. Is that bad?
        return building == null || building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS);
    }

    @Override
    public void setRotationMirror(final RotationMirror rotationMirror)
    {
        this.rotationMirror = rotationMirror;
    }

    @Override
    public RotationMirror getRotationMirror()
    {
        if (rotationMirror == null)
        {
            processBlueprint(StructurePacks.getBlueprint(this.packMeta, this.path.replace("0.blueprint", "1.blueprint"), level.registryAccess()));
        }
        return rotationMirror;
    }

    /**
     * Getter for the style.
     *
     * @return the string of it.
     */
    @Override
    public StructurePackMeta getStructurePack()
    {
        return StructurePacks.getStructurePack(this.packMeta);
    }

    /**
     * Set the style of the tileEntity.
     *
     * @param style the style to set.
     */
    public void setStructurePack(final StructurePackMeta style)
    {
        this.packMeta = style.getName();
    }

    @Override
    public void setBlueprintPath(final String path)
    {
        this.path = path;
    }

    @Override
    public void setPackName(final String packName)
    {
        this.packMeta = packName;
    }

    @Override
    public String getPackName()
    {
        return packMeta;
    }

    @Override
    public String getBlueprintPath()
    {
        return path;
    }

    @Override
    public ResourceLocation getBuildingName()
    {
        if (registryName != null && !registryName.getPath().isEmpty())
        {
            return new ResourceLocation(registryName.getNamespace(), registryName.getPath().replace("home", "residence"));
        }
        return getBlockState().getBlock() instanceof AbstractBlockHut<?> ? ((AbstractBlockHut<?>) getBlockState().getBlock()).getBuildingEntry().getRegistryName() : null;
    }

    @Override
    public void updateBlockState()
    {
        // Do nothing
    }

    @Override
    public IItemHandler getItemHandlerCap(final Direction side)
    {
        if (!remove && getBuilding() != null)
        {
            if (combinedInv == null)
            {
                //Add additional containers
                final Set<IItemHandlerModifiable> handlers = new LinkedHashSet<>();
                final Level world = colony.getWorld();
                if (world != null)
                {
                    for (final BlockPos pos : building.getContainers())
                    {
                        if (WorldUtil.isBlockLoaded(world, pos) && !pos.equals(this.worldPosition))
                        {
                            final BlockEntity te = world.getBlockEntity(pos);
                            if (te != null)
                            {
                                if (te instanceof final AbstractTileEntityRack rack)
                                {
                                    handlers.add(rack.getInventory());
                                    rack.setBuildingPos(this.getBlockPos());
                                }
                                else
                                {
                                    building.removeContainerPosition(pos);
                                }
                            }
                        }
                    }
                }
                handlers.add(this.getInventory());

                combinedInv = new CombinedItemHandler(building.getSchematicName(), handlers.toArray(new IItemHandlerModifiable[0]));
            }
            return combinedInv;
        }
        return super.getItemHandlerCap(side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
    {
        return new ContainerBuildingInventory(id, inv, colonyId, getBlockPos());
    }

    /**
     * Reactivate the hut of this tileEntity.
     * Load the schematic data and set the style correctly.
     */
    public void reactivate()
    {
        final List<String> tags = new ArrayList<>(this.getPositionedTags().get(BlockPos.ZERO));
        tags.remove(DEACTIVATED);
        if (tags.isEmpty())
        {
            this.pendingBlueprintFuture = StructurePacks.getBlueprintFuture(this.packMeta, this.path, level.registryAccess());
            return;
        }

        // First tag on those buildings always has to be the stylename.
        String tagName = tags.get(0);
        final String blueprintPath;
        final String packName;
        if (tagName.contains("/"))
        {
            final String[] split = tagName.split("/");
            packName = split[0];
            blueprintPath = tagName.replace(packName, "");
        }
        else
        {
            final String level = this.getSchematicName().substring(this.getSchematicName().length() - 1);
            packName = BlueprintMapping.getStyleMapping(tagName);
            blueprintPath = BlueprintMapping.getPathMapping(tagName, this.getSchematicName().substring(0, this.getSchematicName().length() - 1)) + level + ".blueprint";
        }

        if (!StructurePacks.hasPack(packName))
        {
            this.pendingBlueprintFuture = StructurePacks.getBlueprintFuture(this.packMeta, this.path, level.registryAccess());
            return;
        }

        this.setStructurePack(StructurePacks.getStructurePack(packName));
        this.pendingBlueprintFuture = StructurePacks.getBlueprintFuture(packName, blueprintPath, level.registryAccess());
    }

    /**
     * Process the blueprint to read relevant data.
     *
     * @param blueprint the queried blueprint.
     */
    private void processBlueprint(final Blueprint blueprint)
    {
        if (blueprint == null)
        {
            return;
        }

        final BlockState structureState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (structureState != null)
        {
            if (!(structureState.getBlock() instanceof AbstractBlockHut) || !(level.getBlockState(this.getPosition()).getBlock() instanceof AbstractBlockHut))
            {
                Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", blueprint.getName()));
                return;
            }
            final int structureRotation = structureState.getValue(AbstractBlockHut.FACING).get2DDataValue();
            final int worldRotation = level.getBlockState(this.getPosition()).getValue(AbstractBlockHut.FACING).get2DDataValue();

            final int rotation;
            if (structureRotation <= worldRotation)
            {
                rotation = worldRotation - structureRotation;
            }
            else
            {
                rotation = 4 + worldRotation - structureRotation;
            }

            rotationMirror = RotationMirror.of(Rotation.values()[rotation], rotationMirror == null ? Mirror.NONE : rotationMirror.mirror());
            blueprint.setRotationMirror(rotationMirror, level);
            final BlockInfo info = blueprint.getBlockInfoAsMap().getOrDefault(blueprint.getPrimaryBlockOffset(), null);

            if (info.getTileEntityData() != null)
            {
                final CompoundTag teCompound = info.getTileEntityData().copy();
                final CompoundTag tagData = teCompound.getCompound(TAG_BLUEPRINTDATA);

                tagData.putString(TAG_PACK, blueprint.getPackName());
                final String location = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(blueprint.getFilePath().resolve(blueprint.getFileName()));
                tagData.putString(TAG_NAME, location);
                this.readSchematicDataFromNBT(teCompound);
            }
        }
    }
}
