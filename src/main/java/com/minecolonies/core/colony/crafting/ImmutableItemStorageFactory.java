package com.minecolonies.core.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IImmutableItemStorageFactory;
import com.minecolonies.api.crafting.ImmutableItemStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing ImmutableItemStorage.
 */
public class ImmutableItemStorageFactory implements IImmutableItemStorageFactory
{

    @NotNull
    @Override
    public TypeToken<? extends ImmutableItemStorage> getFactoryOutputType()
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
        return SerializationIdentifierConstants.IMMUTABLE_ITEM_STORAGE_ID;
    }

    @Override
    public CompoundTag serialize(IFactoryController controller, ImmutableItemStorage output)
    {
        @NotNull final CompoundTag compound = StandardFactoryController.getInstance().serialize(output.copy());

        return compound;
    }

    @Override
    public ImmutableItemStorage deserialize(IFactoryController controller, CompoundTag nbt) throws Throwable
    {
        final ItemStorage readStorage = StandardFactoryController.getInstance().deserializeTag(nbt);
        return readStorage.toImmutable();
    }

    @Override
    public void serialize(IFactoryController controller, ImmutableItemStorage output, RegistryFriendlyByteBuf packetBuffer)
    {
        StandardFactoryController.getInstance().serialize(packetBuffer, output.copy());
    }

    @Override
    public ImmutableItemStorage deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        @NotNull final ItemStorage newItem = StandardFactoryController.getInstance().deserializeTag(buffer);
        return newItem.toImmutable();
    }

    @Override
    public ImmutableItemStorage getNewInstance(ItemStack stack, int size)
    {
        @NotNull final ItemStorage newItem = new ItemStorage(stack);
        newItem.setAmount(size);
        return newItem.toImmutable();
    }
    
}
