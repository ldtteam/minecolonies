package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestIdentitiesDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * A default implementation of the {@link IRequestIdentitiesDataStore} interface.
 */
public class StandardRequestIdentitiesDataStore implements IRequestIdentitiesDataStore
{
    private IToken<?>                     id;
    private final BiMap<IToken<?>, IRequest<?>> map;

    public StandardRequestIdentitiesDataStore(
      final IToken<?> id,
      final BiMap<IToken<?>, IRequest<?>> map) {
        this.id = id;
        this.map = map;
    }

    public StandardRequestIdentitiesDataStore()
    {
        this.id = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN);
        this.map = HashBiMap.create();
    }

    @Override
    public BiMap<IToken<?>, IRequest<?>> getIdentities()
    {
        return map;
    }

    @Override
    public IToken<?> getId()
    {
        return id;
    }

    @Override
    public void setId(final IToken<?> id)
    {
        this.id = id;
    }

    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestIdentitiesDataStore>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardRequestIdentitiesDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestIdentitiesDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestIdentitiesDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestIdentitiesDataStore();
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestIdentitiesDataStore standardRequestIdentitiesDataStore)
        {
            final NBTTagCompound systemCompound = new NBTTagCompound();

            systemCompound.setTag(TAG_TOKEN, controller.serialize(standardRequestIdentitiesDataStore.getId()));
            systemCompound.setTag(TAG_LIST, standardRequestIdentitiesDataStore.getIdentities().keySet().stream().map(token -> {
                NBTTagCompound mapCompound = new NBTTagCompound();
                mapCompound.setTag(TAG_TOKEN, controller.serialize(token));
                mapCompound.setTag(TAG_REQUEST, controller.serialize(standardRequestIdentitiesDataStore.getIdentities().get(token)));
                return mapCompound;
            }).collect(NBTUtils.toNBTTagList()));

            return systemCompound;
        }

        @NotNull
        @Override
        public StandardRequestIdentitiesDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            final IToken<?> token = controller.deserialize(nbt.getCompoundTag(TAG_TOKEN));
            final NBTTagList list = nbt.getTagList(TAG_LIST, Constants.NBT.TAG_COMPOUND);

            final Map<IToken<?>, IRequest<?>> map = NBTUtils.streamCompound(list).map(nbtTagCompound -> {
                final IToken<?> id = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_TOKEN));
                final IRequest<?> request = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_REQUEST));

                return new Tuple<IToken<?>, IRequest<?>>(id, request);
            }).collect(Collectors.toMap((Tuple<IToken<?>, IRequest<?>> t) -> t.getFirst(), (Tuple<IToken<?>, IRequest<?>> t) -> t.getSecond()));

            final BiMap<IToken<?>, IRequest<?>> biMap = HashBiMap.create(map);

            return new StandardRequestIdentitiesDataStore(token, biMap);
        }
    }
}
