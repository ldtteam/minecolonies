package com.minecolonies.core.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.IItemStorageFactory;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final ItemStorage storage)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(TAG_STACK, storage.getItemStack().saveOptional(provider));
        compound.putInt(TAG_SIZE, storage.getAmount());
        compound.putBoolean(TAG_SHOULDIGNOREDAMAGE, storage.ignoreDamageValue());
        compound.putBoolean(TAG_SHOULDIGNORENBT , storage.ignoreNBT());
        return compound;
    }

    @NotNull
    @Override
    public ItemStorage deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final ItemStack stack = ItemStack.parseOptional(provider, nbt.getCompound(TAG_STACK));
        final int size = nbt.getInt(TAG_SIZE);
        final boolean ignoreNBT = nbt.getBoolean(TAG_SHOULDIGNORENBT);
        final boolean ignoreDamage = nbt.getBoolean(TAG_SHOULDIGNOREDAMAGE);
        return this.getNewInstance(stack, size, ignoreDamage, ignoreNBT);
    }

    @Override
    public void serialize(IFactoryController controller, ItemStorage input, RegistryFriendlyByteBuf packetBuffer)
    {
        // Some mods do weird things with banner patterns atm, so we add some special backup functionality here.
        // We might have to extend this to other types of items in the future.
        if (input.getItemStack().has(DataComponents.BANNER_PATTERNS))
        {
            boolean success = false;
            try
            {
                Utils.serializeCodecMess(ItemStack.OPTIONAL_CODEC, packetBuffer.registryAccess(), input.getItemStack());
                success = true;
                Utils.serializeCodecMess(packetBuffer, input.getItemStack());
            }
            catch (final Exception ex)
            {
                //  Noop
            }
            if (!success)
            {
                Utils.serializeCodecMess(packetBuffer, ItemStack.EMPTY);
            }
        }
        else
        {
            Utils.serializeCodecMess(packetBuffer, input.getItemStack());
        }

        packetBuffer.writeVarInt(input.getAmount());
        packetBuffer.writeBoolean(input.ignoreDamageValue());
        packetBuffer.writeBoolean(input.ignoreNBT());
    }

    @Override
    public ItemStorage deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        final ItemStack stack = Utils.deserializeCodecMess(buffer);
        final int size = buffer.readVarInt();
        final boolean ignoreDamage = buffer.readBoolean();
        final boolean ignoreNBT = buffer.readBoolean();
        return this.getNewInstance(stack, size, ignoreDamage, ignoreNBT);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.ITEM_STORAGE_ID;
    }
}
