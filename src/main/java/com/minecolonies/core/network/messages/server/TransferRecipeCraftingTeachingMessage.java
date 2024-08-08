package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

/**
 * Creates a message to get jei recipes.
 */
public class TransferRecipeCraftingTeachingMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "transfer_recipe_crafting_teaching", TransferRecipeCraftingTeachingMessage::new);

    /**
     * if the recipe is complete.
     */
    private final boolean complete;

    /**
     * Recipes to transfer.
     */
    private final Map<Integer, ItemStack> itemStacks;

    /**
     * Creates a new message to get jei recipes.
     *
     * @param itemStacks the stack recipes to register.
     * @param complete   whether we're complete
     */
    public TransferRecipeCraftingTeachingMessage(final Map<Integer, ItemStack> itemStacks, final boolean complete)
    {
        super(TYPE);
        this.itemStacks = itemStacks;
        this.complete = complete;
    }

    protected TransferRecipeCraftingTeachingMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        itemStacks = buf.readMap(FriendlyByteBuf::readInt, b -> ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) b));
        complete = buf.readBoolean();
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeMap(itemStacks, FriendlyByteBuf::writeInt, (b, v) -> ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, v));
        buf.writeBoolean(complete);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        if (player.containerMenu instanceof final ContainerCrafting container)
        {
            if (complete)
            {
                container.handleSlotClick(container.getSlot(1), itemStacks.getOrDefault(0, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(2), itemStacks.getOrDefault(1, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(3), itemStacks.getOrDefault(2, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(4), itemStacks.getOrDefault(3, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(5), itemStacks.getOrDefault(4, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(6), itemStacks.getOrDefault(5, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(7), itemStacks.getOrDefault(6, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(8), itemStacks.getOrDefault(7, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(9), itemStacks.getOrDefault(8, ItemStackUtils.EMPTY));
            }
            else
            {
                container.handleSlotClick(container.getSlot(1), itemStacks.getOrDefault(0, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(2), itemStacks.getOrDefault(1, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(3), itemStacks.getOrDefault(3, ItemStackUtils.EMPTY));
                container.handleSlotClick(container.getSlot(4), itemStacks.getOrDefault(4, ItemStackUtils.EMPTY));
            }

            container.broadcastChanges();
        }
        else if (player.containerMenu instanceof final ContainerCraftingFurnace container)
        {
            container.setFurnaceInput(itemStacks.getOrDefault(0, ItemStack.EMPTY));
        }
        else if (player.containerMenu instanceof final ContainerCraftingBrewingstand container)
        {
            container.setInput(itemStacks.getOrDefault(0, ItemStack.EMPTY));
            container.setContainer(itemStacks.getOrDefault(1, ItemStack.EMPTY));
        }
    }
}
