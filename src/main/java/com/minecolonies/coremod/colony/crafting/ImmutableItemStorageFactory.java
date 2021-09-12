package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IImmutableItemStorageFactory;
import com.minecolonies.api.crafting.ImmutableItemStorage;
import com.minecolonies.api.crafting.ItemStackHandling;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ImmutableItemStorage.
 */
public class ImmutableItemStorageFactory implements IImmutableItemStorageFactory
{

    @NotNull
    @Override
    public TypeToken<? extends ItemStorage> getFactoryOutputType()
    {
        return TypeConstants.IMMUTABLEITEMSTORAGE;
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
        return 45;
    }

    @Override
    public CompoundNBT serialize(IFactoryController controller, ItemStorage output)
    {
        @NotNull final CompoundNBT compound = StandardFactoryController.getInstance().serialize(output.copy());

        return compound;
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, CompoundNBT nbt) throws Throwable
    {
        final ItemStorage readStorage = StandardFactoryController.getInstance().deserialize(nbt);
        return readStorage.toImmutable();
    }

    @Override
    public void serialize(IFactoryController controller, ItemStorage output, PacketBuffer packetBuffer)
    {
        StandardFactoryController.getInstance().serialize(packetBuffer, output.copy());
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        @NotNull final ItemStorage newItem = StandardFactoryController.getInstance().deserialize(buffer);
        return newItem.toImmutable();
    }

    @Override
    public ItemStorage getNewInstance(ItemStack stack, int size)
    {
        @NotNull final ItemStorage newItem = new ItemStackHandling(stack);
        newItem.setAmount(size);
        return newItem.toImmutable();
    }
    
}
