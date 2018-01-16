package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IDataStore;
import com.minecolonies.api.colony.requestsystem.data.IDataStoreManager;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StandardDataStoreManager implements IDataStoreManager
{

    private final Map<IToken<?>, IDataStore> storeMap;

    public StandardDataStoreManager(final Map<IToken<?>, IDataStore> storeMap) {this.storeMap = storeMap;}

    public StandardDataStoreManager()
    {
        this(new HashMap<>());
    }

    @Override
    public <T extends IDataStore> T get(final IToken<?> id, final T defaultInstance)
    {
        if (!storeMap.containsKey(id))
        {
            defaultInstance.setId(id);
            storeMap.put(id, defaultInstance);
        }

        return (T) storeMap.get(id);
    }

    @Override
    public <T extends IDataStore> T get(final IToken<?> id, final TypeToken<T> type)
    {
        return get(id, StandardFactoryController.getInstance().getNewInstance(type));
    }

    @Override
    public <T extends IDataStore> T get(final IToken<?> id, final Supplier<T> factory)
    {
        return get(id, factory.get());
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
        public StandardDataStoreManager getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardDataStoreManager();
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardDataStoreManager standardDataStoreManager)
        {
            final NBTTagCompound compound = new NBTTagCompound();

            compound.setTag(NbtTagConstants.TAG_LIST, standardDataStoreManager.storeMap.keySet().stream().map(iToken -> {
                final NBTTagCompound entryCompound = new NBTTagCompound();

                entryCompound.setTag(NbtTagConstants.TAG_TOKEN, controller.serialize(iToken));
                entryCompound.setTag(NbtTagConstants.TAG_VALUE, controller.serialize(standardDataStoreManager.storeMap.get(iToken)));

                return entryCompound;
            }).collect(NBTUtils.toNBTTagList()));

            return compound;
        }

        @NotNull
        @Override
        public StandardDataStoreManager deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt) throws Throwable
        {
            final Map<IToken<?>, IDataStore> storeMap = NBTUtils.streamCompound(nbt.getTagList(NbtTagConstants.TAG_LIST, Constants.NBT.TAG_COMPOUND)).map(nbtTagCompound -> {
                final IToken<?> token = controller.deserialize(nbtTagCompound.getCompoundTag(NbtTagConstants.TAG_TOKEN));
                final IDataStore store = controller.deserialize(nbtTagCompound.getCompoundTag(NbtTagConstants.TAG_VALUE));

                return new Tuple<>(token, store);
            }).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));

            return new StandardDataStoreManager(storeMap);
        }
    }
}
