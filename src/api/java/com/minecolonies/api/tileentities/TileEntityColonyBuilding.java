package com.minecolonies.api.tileentities;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.inventory.container.ContainerBuildingInventory;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.MIN_SLOTS_FOR_RECOGNITION;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;

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
    private boolean mirror;

    /**
     * The style of the building.
     */
    private String style = "";

    /**
     * The name of the building location.
     */
    public ResourceLocation registryName;

    /**
     * Create the combined inv wrapper for the building.
     */
    private LazyOptional<CombinedItemHandler> combinedInv;

    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityColonyBuilding(final BlockPos pos, final BlockState state)
    {
        this(MinecoloniesTileEntities.BUILDING, pos, state);
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
                    if (entity instanceof AbstractTileEntityRack)
                    {
                        if (((AbstractTileEntityRack) entity).hasItemStack(notEmptyPredicate))
                        {
                            return pos;
                        }
                    }
                    else if (isInTileEntity(entity, notEmptyPredicate))
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
        return  ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithId();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        colonyId = compound.getInt(TAG_COLONY);
        super.onDataPacket(net, packet);
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
        if (getBlockState() == null)
        {
            return super.getDisplayName();
        }
        return new TranslatableComponent(getBlockState().getBlock().getDescriptionId() + ".name");
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
    public void load(@NotNull final CompoundTag compound)
    {
        super.load(compound);
        if (compound.getAllKeys().contains(TAG_COLONY))
        {
            colonyId = compound.getInt(TAG_COLONY);
        }
        mirror = compound.getBoolean(TAG_MIRROR);
        style = compound.getString(TAG_STYLE);
        registryName = new ResourceLocation(compound.getString(TAG_BUILDING_TYPE));
        buildingPos = worldPosition;
        single = true;
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt(TAG_COLONY, colonyId);
        compound.putBoolean(TAG_MIRROR, mirror);
        compound.putString(TAG_STYLE, style);
        compound.putString(TAG_BUILDING_TYPE, registryName.toString());
    }

    @Override
    public void tick()
    {
        if (combinedInv != null)
        {
            combinedInv.invalidate();
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
        //TODO This is called every tick the GUI is open. Is that bad?
        return building == null || building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS);
    }

    /**
     * Set if the entity is mirrored.
     *
     * @param mirror true if so.
     */
    @Override
    public void setMirror(final boolean mirror)
    {
        this.mirror = mirror;
    }

    /**
     * Check if building is mirrored.
     *
     * @return true if so.
     */
    @Override
    public boolean isMirrored()
    {
        return mirror;
    }

    /**
     * Getter for the style.
     *
     * @return the string of it.
     */
    @Override
    public String getStyle()
    {
        return this.style;
    }

    /**
     * Set the style of the tileEntity.
     *
     * @param style the style to set.
     */
    @Override
    public void setStyle(final String style)
    {
        this.style = style;
    }

    @Override
    public ResourceLocation getBuildingName()
    {
        return registryName;
    }

    @Override
    public boolean isMain()
    {
        return true;
    }

    @Override
    public void updateBlockState()
    {
        // Do nothing
    }

    @Override
    public void setSingle(final boolean single)
    {
        // Do nothing, these are always single!
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> capability, @Nullable final Direction side)
    {
        if (!remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getBuilding() != null)
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
                                if (te instanceof AbstractTileEntityRack)
                                {
                                    final LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                                    cap.ifPresent(theCap -> {
                                        if (theCap instanceof IItemHandlerModifiable && theCap.getSlots() >= MIN_SLOTS_FOR_RECOGNITION)
                                        {
                                            handlers.add((IItemHandlerModifiable) theCap);
                                        }
                                    });
                                    ((AbstractTileEntityRack) te).setBuildingPos(this.getBlockPos());
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

                combinedInv = LazyOptional.of(() -> new CombinedItemHandler(building.getSchematicName(), handlers.toArray(new IItemHandlerModifiable[0])));
            }
            return (LazyOptional<T>) combinedInv;
        }
        return super.getCapability(capability, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
    {
        return new ContainerBuildingInventory(id, inv, colonyId, getBlockPos());
    }
}
