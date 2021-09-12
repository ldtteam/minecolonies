package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IItemStackStorageFactory;
import com.minecolonies.api.crafting.ItemStackStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.TypeConstants;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ImmutableItemStorage.
 */
public class ItemStackStorageFactory implements IItemStackStorageFactory
{

    /**
     * Compound tag for the size.
     */
    private static final String TAG_SIZE = "size";

    /**
     * Compound tag for the stack.
     */
    private static final String TAG_STACK = "stack";

    @NotNull
    @Override
    public TypeToken<? extends ItemStorage> getFactoryOutputType()
    {
        return TypeConstants.ITEMSTACKSTORAGE;
    }

    @NotNull
    @Override
    public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @Override
    public short getSerializationId()
    {
        return 46;
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final ItemStorage storage)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull CompoundNBT stackTag = storage.getItemStack().serializeNBT();
        compound.put(TAG_STACK, stackTag);
        compound.putInt(TAG_SIZE, storage.getAmount());
        return compound;
    }

    @NotNull
    @Override
    public ItemStorage deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final ItemStack stack = ItemStack.of(nbt.getCompound(TAG_STACK));
        final int size = nbt.getInt(TAG_SIZE);
        return this.getNewInstance(stack, size);
    }

    @Override
    public void serialize(IFactoryController controller, ItemStorage input, PacketBuffer packetBuffer)
    {
        packetBuffer.writeItemStack(input.getItemStack(), false);
        packetBuffer.writeVarInt(input.getAmount());
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final ItemStack stack = ItemStack.of(buffer.readNbt());
        final int size = buffer.readVarInt();
        return this.getNewInstance(stack, size);
    }

    @Override
    public ItemStorage getNewInstance(ItemStack stack, int size)
    {
        @NotNull final ItemStorage newItem = new ItemStackStorage(stack);
        newItem.setAmount(size);
        return newItem.toImmutable();
    }
    
}
