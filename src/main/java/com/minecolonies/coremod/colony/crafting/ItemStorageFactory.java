package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IItemStorageFactory;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_STACK;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ItemStorage.
 */
public class ItemStorageFactory implements IItemStorageFactory
{
    /**
     * Compound tag for the size.
     */
    private static final String TAG_SIZE = "size";

    @NotNull
    @Override
    public TypeToken<ItemStorage> getFactoryOutputType()
    {
        return TypeConstants.ITEMSTORAGE;
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public ItemStorage getNewInstance(@NotNull final ItemStack stack, final int size)
    {
        return new ItemStorage(stack, size, false);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final ItemStorage storage)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull CompoundNBT stackTag = new CompoundNBT();
        Log.getLogger().info("Writing new ItemStorage: " + storage.getItemStack().getDisplayName().getString());
        storage.getItemStack().write(stackTag);
        compound.put(TAG_STACK, stackTag);
        compound.putInt(TAG_SIZE, storage.getAmount());
        return compound;
    }

    @NotNull
    @Override
    public ItemStorage deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final ItemStack stack = ItemStack.read(nbt.getCompound(TAG_STACK));
        final int size = nbt.getInt(TAG_SIZE);
        Log.getLogger().info("deserializing stack: " + stack.getDisplayName().getString());
        return this.getNewInstance(stack, size);
    }

    @Override
    public void serialize(IFactoryController controller, ItemStorage input, PacketBuffer packetBuffer)
    {
        packetBuffer.writeItemStack(input.getItemStack());
        packetBuffer.writeInt(input.getAmount());
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final ItemStack stack = buffer.readItemStack();
        final int size = buffer.readInt();
        return this.getNewInstance(stack, size);
    }

    @Override
    public short getSerializationId()
    {
        return 27;
    }
}
