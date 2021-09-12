package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IItemStackHandlingFactory;
import com.minecolonies.api.crafting.ItemStackHandling;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.TypeConstants;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ItemStackHandlingFactory.
 */
public class ItemStackHandlingFactory implements IItemStackHandlingFactory
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
    public TypeToken<? extends ItemStorage> getFactoryOutputType()
    {
        return TypeConstants.ITEMSTACKHANDLING;
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
        return 47;
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final ItemStorage storage)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull CompoundNBT stackTag = storage.getItemStack().serializeNBT();
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
        packetBuffer.writeItemStack(input.getItemStack(), false);
        packetBuffer.writeVarInt(input.getAmount());
        packetBuffer.writeBoolean(input.ignoreDamageValue());
        packetBuffer.writeBoolean(input.ignoreNBT());
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final ItemStack stack = ItemStack.of(buffer.readNbt());
        final int size = buffer.readVarInt();
        final boolean ignoreDamage = buffer.readBoolean();
        final boolean ignoreNBT = buffer.readBoolean();
        return this.getNewInstance(stack, size, ignoreDamage, ignoreNBT);
    }

    @NotNull
    @Override
    public ItemStorage getNewInstance(@NotNull final ItemStack stack, final int size, final boolean ignoreDamage, final boolean ignoreNBT)
    {
        ItemStorage newItem = new ItemStackHandling(stack, ignoreDamage, ignoreNBT);
        newItem.setAmount(size);
        return newItem;

    }
    
}
