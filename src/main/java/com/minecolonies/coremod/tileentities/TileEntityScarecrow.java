package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldRecord;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.container.ContainerField;
import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.FarmField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_INVENTORY;

/**
 * The scarecrow tile entity to store extra data.
 */
public class TileEntityScarecrow extends AbstractTileEntityScarecrow
{
    /**
     * Inventory of the field.
     */
    private final TileEntityScarecrowInventoryHandler inventory;

    /**
     * Random generator.
     */
    private final Random random = new Random();

    /**
     * The colony this field is located in.
     */
    private IColony currentColony;

    /**
     * The type of the scarecrow.
     */
    private ScareCrowType type;

    /**
     * Creates an instance of the tileEntity.
     */
    public TileEntityScarecrow(final BlockPos pos, final BlockState state)
    {
        super(pos, state);
        this.inventory = new TileEntityScarecrowInventoryHandler((oldPlant, newPlant) -> {
            IColony colony = getCurrentColony();
            if (colony instanceof Colony)
            {
                final IField field = getCurrentColony().getBuildingManager().getField(FieldType.FARMER_FIELDS, new FieldRecord(pos, oldPlant));
                if (field instanceof FarmField farmField)
                {
                    getCurrentColony().getBuildingManager().removeField(FieldType.FARMER_FIELDS, new FieldRecord(pos, oldPlant));

                    FarmField newField = new FarmField(farmField);
                    newField.setPlant(newPlant);
                    getCurrentColony().getBuildingManager().addOrUpdateField(newField);
                }
            }
        });
    }

    @Override
    public IItemHandler getInventory()
    {
        return inventory;
    }

    @Override
    public @Nullable Item getPlant()
    {
        return inventory.plant;
    }

    @Override
    public ScareCrowType getScarecrowType()
    {
        if (this.type == null)
        {
            this.type = ScareCrowType.values()[this.random.nextInt(2)];
        }
        return this.type;
    }

    @Override
    public IColony getCurrentColony()
    {
        if (currentColony == null && level != null)
        {
            this.currentColony = IColonyManager.getInstance().getIColony(level, worldPosition);
        }
        return currentColony;
    }

    /**
     * Check condition whether the field UI can be opened or not.
     *
     * @param player the player attempting to open the menu.
     * @return whether the player is authorized to open this menu.
     */
    @Override
    public boolean canOpenMenu(@NotNull Player player)
    {
        IColony colony = getCurrentColony();
        if (colony != null)
        {
            return colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS) &&
                     colony.getBuildingManager().getField(FieldType.FARMER_FIELDS, new FieldRecord(worldPosition, inventory.plant)) != null;
        }
        return false;
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        if (compound != null)
        {
            this.load(compound);
        }
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);
        final CompoundTag inventoryTagList = compound.getCompound(TAG_INVENTORY);
        inventory.deserializeNBT(inventoryTagList);
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.put(TAG_INVENTORY, inventory.serializeNBT());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithId();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
    {
        return new ContainerField(id, inv, getBlockPos());
    }

    @NotNull
    @Override
    public Component getDisplayName()
    {
        return new TextComponent("");
    }

    private static class TileEntityScarecrowInventoryHandler extends ItemStackHandler
    {
        /**
         * Callback function to indicate changes in the plant item.
         */
        private final BiConsumer<Item, Item> onPlantChanged;

        /**
         * The plant which is at index 0.
         */
        private Item plant;

        public TileEntityScarecrowInventoryHandler(BiConsumer<Item, Item> onPlantChanged)
        {
            this.onPlantChanged = onPlantChanged;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            return stack.is(Tags.Items.SEEDS) || (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock);
        }

        @Override
        protected void onLoad()
        {
            super.onLoad();
            this.plant = getItemInSlot();
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            super.onContentsChanged(slot);
            final Item item = getItemInSlot();
            if (!Objects.equals(plant, item))
            {
                onPlantChanged.accept(plant, item);
                plant = item;
            }
        }

        @Nullable
        private Item getItemInSlot()
        {
            Item item = this.getStackInSlot(0).getItem();
            return !item.equals(Items.AIR) ? item : null;
        }
    }
}
