package com.minecolonies.api.tileentities;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuildingContainer;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.MIN_SLOTS_FOR_RECOGNITION;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
public class TileEntityColonyBuilding extends AbstractTileEntityColonyBuilding
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
    private IBuildingContainer building;

    /**
     * Check if the building has a mirror.
     */
    private boolean mirror;

    /**
     * The style of the building.
     */
    private String style = "";

    /**
     * Create the combined inv wrapper for the building.
     */
    private CombinedItemHandler combinedInv;

    /**
     * The name of the building location.
     */
    private ResourceLocation registryName;

    /**
     * Standard constructor.
     */
    public TileEntityColonyBuilding()
    {
        this(MinecoloniesTileEntities.BUILDING);
    }

    /**
     * Alternative overriden constructor.
     */
    public TileEntityColonyBuilding(final TileEntityType type)
    {
        super(type);
        this.registryName = ((AbstractBlockHut) getBlockState().getBlock()).getBuildingEntry().getRegistryName();
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
        if (colony == null && getWorld() != null)
        {
            if (colonyId == 0)
            {
                colony = IColonyManager.getInstance().getColonyByPosFromWorld(getWorld(), this.getPos());
            }
            else
            {
                colony = IColonyManager.getInstance().getColonyByWorld(colonyId, getWorld());
            }

            // It's most probably previewed building, please don't spam it here.
            if (colony == null && !getWorld().isRemote)
            {
                //log on the server
                Log.getLogger().warn(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] had colony.",
                  getWorld().getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
            }
        }

        if (building == null && colony != null)
        {
            building = colony.getBuildingManager().getBuilding(getPosition());
            if (building != null && (getWorld() == null || !getWorld().isRemote))
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
        return pos;
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
            if (isInTileEntity(theBuilding.getTileEntity(), notEmptyPredicate))
            {
                return theBuilding.getPosition();
            }

            for (final BlockPos pos : theBuilding.getAdditionalCountainers())
            {
                final TileEntity entity = getWorld().getTileEntity(pos);
                if ((entity instanceof AbstractTileEntityRack
                       && ((AbstractTileEntityRack) entity).hasItemStack(notEmptyPredicate))
                      || (entity instanceof ChestTileEntity
                            && isInTileEntity(entity, notEmptyPredicate)))
                {
                    return pos;
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
        markDirty();
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if (building != null)
        {
            building.markDirty();
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.putInt(TAG_COLONY, colonyId);
        return new SUpdateTileEntityPacket(this.getPosition(), 0, compound);
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getNbtCompound();
        colonyId = compound.getInt(TAG_COLONY);
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
    public IBuildingContainer getBuilding()
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
     * @param b {@link IBuildingContainer} to associate with the tile entity.
     */
    @Override
    public void setBuilding(final IBuildingContainer b)
    {
        building = b;
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        if (getBlockState() == null)
        {
            return super.getDisplayName();
        }
        return new StringTextComponent(LanguageHandler.format(getBlockState().getBlock().getTranslationKey() + ".name"));
    }

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link IBuildingView} the tile entity is associated with.
     */
    @Override
    public IBuildingView getBuildingView()
    {
        final IColonyView c = IColonyManager.getInstance().getColonyView(colonyId, world.getDimension().getType().getId());
        return c == null ? null : c.getBuilding(getPosition());
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        super.read(compound);
        if (compound.keySet().contains(TAG_COLONY))
        {
            colonyId = compound.getInt(TAG_COLONY);
        }

        updateColonyReferences();
        mirror = compound.getBoolean(TAG_MIRROR);
        style = compound.getString(TAG_STYLE);
        registryName = new ResourceLocation(compound.getString(TAG_BUILDING_TYPE));
    }

    @NotNull
    @Override
    public CompoundNBT write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
        compound.putInt(TAG_COLONY, colonyId);
        compound.putBoolean(TAG_MIRROR, mirror);
        compound.putString(TAG_STYLE, style);
        compound.putString(TAG_BUILDING_TYPE, registryName.toString());
        return compound;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!getWorld().isRemote && colonyId == 0)
        {
            final IColony tempColony = IColonyManager.getInstance().getColonyByPosFromWorld(getWorld(), this.getPosition());
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }
        }

        /*
         * We want a new inventory every tick.
         * The accessed inventory in the same tick must be the same.
         */
        combinedInv = null;
    }

    @Override
    public boolean isUsableByPlayer(@NotNull final PlayerEntity player)
    {
        return super.isUsableByPlayer(player) && this.hasAccessPermission(player);
    }

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    @Override
    public boolean hasAccessPermission(final PlayerEntity player)
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
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> capability, @NotNull final Direction side)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getBuilding() != null)
        {
            if (this.combinedInv == null)
            {
                //Add additional containers
                final Set<ICapabilityProvider> providers = new HashSet<>();
                final World world = colony.getWorld();
                if (world != null)
                {
                    //Add additional containers
                    providers.addAll(building.getAdditionalCountainers().stream()
                                       .map(world::getTileEntity)
                                       .collect(Collectors.toSet()));
                    providers.removeIf(Objects::isNull);
                }

                final List<IItemHandler> handlers = providers.stream()
                                                      .flatMap(provider -> InventoryUtils.getItemHandlersFromProvider(provider).stream())
                                                      .collect(Collectors.toList());
                final T cap = super.getCapability(capability).orElseGet(null);
                if (cap instanceof IItemHandler)
                {
                    handlers.add((IItemHandler) cap);
                }
                
                this.combinedInv = new CombinedItemHandler(building.getSchematicName(), handlers.stream()
                                                                                          .map(handler -> (IItemHandlerModifiable) handler)
                                                                                          .distinct()
                                                                                          .filter(handler -> handler instanceof IItemHandlerModifiable
                                                                                                               && handler.getSlots() >= MIN_SLOTS_FOR_RECOGNITION)
                                                                                          .toArray(IItemHandlerModifiable[]::new));
            }

            return LazyOptional.of(() -> (T) this.combinedInv);
        }
        return super.getCapability(capability);
    }
}
