package com.minecolonies.core.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IDataStore;
import com.minecolonies.api.colony.requestsystem.data.IDataStoreManager;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StandardDataStoreManager implements IDataStoreManager
{

    private final Map<IToken<?>, IDataStore> storeMap;

    public StandardDataStoreManager(final Map<IToken<?>, IDataStore> storeMap) {this.storeMap = storeMap;}

    public StandardDataStoreManager()
    {
        this(new HashMap<>());
    }

    @Override
    public <T extends IDataStore> T get(final IToken<?> id, final TypeToken<T> type)
    {
        return get(id, () -> StandardFactoryController.getInstance().getNewInstance(type));
    }

    @Override
    public <T extends IDataStore> T get(final IToken<?> id, final Supplier<T> factory)
    {
        if (!storeMap.containsKey(id))
        {
            final T defaultInstance = factory.get();

            defaultInstance.setId(id);
            storeMap.put(id, defaultInstance);
        }

        return (T) storeMap.get(id);
    }

    @Override
    public void remove(final IToken<?> id)
    {
        storeMap.remove(id);
    }

    @Override
    public void removeAll()
    {
        storeMap.clear();
    }

    public static class Factory implements IFactory<FactoryVoidInput, StandardDataStoreManager>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardDataStoreManager> getFactoryOutputType()
        {
            return TypeToken.of(StandardDataStoreManager.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardDataStoreManager getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardDataStoreManager();
        }

        @NotNull
        @Override
        public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final StandardDataStoreManager standardDataStoreManager)
        {
            final CompoundTag compound = new CompoundTag();

            compound.put(NbtTagConstants.TAG_LIST, standardDataStoreManager.storeMap.keySet().stream().map(iToken -> {
                final CompoundTag entryCompound = new CompoundTag();

                entryCompound.put(NbtTagConstants.TAG_TOKEN, controller.serializeTag(provider, iToken));
                entryCompound.put(NbtTagConstants.TAG_VALUE, controller.serializeTag(provider, standardDataStoreManager.storeMap.get(iToken)));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));

            return compound;
        }

        @NotNull
        @Override
        public StandardDataStoreManager deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
        {
            final Map<IToken<?>, IDataStore> storeMap = new HashMap<>();
            final ListTag list = nbt.getList(NbtTagConstants.TAG_LIST, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++)
            {
                final CompoundTag tag = list.getCompound(i);
                try
                {
                    final IToken<?> token = controller.deserialize(tag.getCompound(NbtTagConstants.TAG_TOKEN));
                    final IDataStore store = controller.deserialize(tag.getCompound(NbtTagConstants.TAG_VALUE));
                    storeMap.put(token, store);
                }
                catch (final Exception ex)
                {
                    Log.getLogger().error(ex);
                }
            }

            return new StandardDataStoreManager(storeMap);
        }

        @Override
        public void serialize(IFactoryController controller, StandardDataStoreManager input, RegistryFriendlyByteBuf packetBuffer)
        {
            packetBuffer.writeInt(input.storeMap.size());
            input.storeMap.forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                controller.serialize(packetBuffer, value);
            });
        }

        @Override
        public StandardDataStoreManager deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer)
        {
            final Map<IToken<?>, IDataStore> storeMap = new HashMap<>();
            final int storeSize = buffer.readInt();
            for (int i = 0; i < storeSize; ++i)
            {
                try
                {
                    storeMap.put(controller.deserialize(buffer), controller.deserialize(buffer));
                }
                catch (final Exception ex)
                {
                    Log.getLogger().error(ex);
                }
            }

            return new StandardDataStoreManager(storeMap);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_DATASTORE_MANAGER_ID;
        }
    }
}
