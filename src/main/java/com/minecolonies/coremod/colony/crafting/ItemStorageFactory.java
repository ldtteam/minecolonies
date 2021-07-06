package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IItemStorageFactory;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ItemStorage.
 */
public class ItemStorageFactory implements IItemStorageFactory
{
    /**
     * Compound tag for the size.
     */
    private static final String TAG_SIZE = "size";

    /**
     * Compound tag for the stack.
     */
    private static final String TAG_STACK = "stack";

    /**
     * Compound tag for the NBT info
     */
    private static final String TAG_SHOULDIGNORENBT = "ignoreNBT";

    /**
     * Compound tag for the Damage Match Info
     */
    private static final String TAG_SHOULDIGNOREDAMAGE = "ignoreDamage";

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
    public ItemStorage getNewInstance(@NotNull final ItemStack stack, final int size, final boolean ignoreDamage, final boolean ignoreNBT)
    {
        ItemStorage newItem = new ItemStorage(stack, ignoreDamage, ignoreNBT);
        newItem.setAmount(size);
        return newItem;

    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final ItemStorage storage)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull CompoundNBT stackTag = new CompoundNBT();
        storage.getItemStack().save(stackTag);
        compound.put(TAG_STACK, stackTag);
        compound.putInt(TAG_SIZE, storage.getAmount());
        compound.putBoolean(TAG_SHOULDIGNOREDAMAGE, storage.ignoreDamageValue());
        compound.putBoolean(TAG_SHOULDIGNORENBT , storage.ignoreNBT());
        return compound;
    }

    @NotNull
    @Override
    public ItemStorage deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final ItemStack stack = ItemStack.of(nbt.getCompound(TAG_STACK));
        final int size = nbt.getInt(TAG_SIZE);
        final boolean ignoreNBT = nbt.getBoolean(TAG_SHOULDIGNORENBT);
        final boolean ignoreDamage = nbt.getBoolean(TAG_SHOULDIGNOREDAMAGE);
        return this.getNewInstance(stack, size, ignoreDamage, ignoreNBT);
    }

    @Override
    public void serialize(IFactoryController controller, ItemStorage input, PacketBuffer packetBuffer)
    {
        packetBuffer.writeItem(input.getItemStack());
        packetBuffer.writeVarInt(input.getAmount());
        packetBuffer.writeBoolean(input.ignoreDamageValue());
        packetBuffer.writeBoolean(input.ignoreNBT());
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final ItemStack stack = buffer.readItem();
        final int size = buffer.readVarInt();
        final boolean ignoreDamage = buffer.readBoolean();
        final boolean ignoreNBT = buffer.readBoolean();
        return this.getNewInstance(stack, size, ignoreDamage, ignoreNBT);
    }

    @Override
    public short getSerializationId()
    {
        return 27;
    }
}
