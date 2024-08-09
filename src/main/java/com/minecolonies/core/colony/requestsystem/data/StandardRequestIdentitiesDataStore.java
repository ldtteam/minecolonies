package com.minecolonies.core.colony.requestsystem.data;

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
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * A default implementation of the {@link IRequestIdentitiesDataStore} interface.
 */
public class StandardRequestIdentitiesDataStore implements IRequestIdentitiesDataStore
{
    private       IToken<?>                     id;
    private final BiMap<IToken<?>, IRequest<?>> map;

    public StandardRequestIdentitiesDataStore(
      final IToken<?> id,
      final BiMap<IToken<?>, IRequest<?>> map)
    {
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
        public CompoundTag serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestIdentitiesDataStore standardRequestIdentitiesDataStore)
        {
            final CompoundTag systemCompound = new CompoundTag();

            systemCompound.put(TAG_TOKEN, controller.serialize(standardRequestIdentitiesDataStore.getId()));
            systemCompound.put(TAG_LIST, standardRequestIdentitiesDataStore.getIdentities().keySet().stream().map(token -> {
                CompoundTag mapCompound = new CompoundTag();
                mapCompound.put(TAG_TOKEN, controller.serialize(token));
                mapCompound.put(TAG_REQUEST, controller.serialize(standardRequestIdentitiesDataStore.getIdentities().get(token)));
                return mapCompound;
            }).collect(NBTUtils.toListNBT()));

            return systemCompound;
        }

        @NotNull
        @Override
        public StandardRequestIdentitiesDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final ListTag list = nbt.getList(TAG_LIST, Tag.TAG_COMPOUND);

            final BiMap<IToken<?>, IRequest<?>> map = HashBiMap.create();
            for (int i = 0; i < list.size(); i++)
            {
                final CompoundTag tag = list.getCompound(i);
                try
                {
                    final IToken<?> id = controller.deserialize(tag.getCompound(TAG_TOKEN));
                    final IRequest<?> request = controller.deserialize(tag.getCompound(TAG_REQUEST));
                    map.put(id, request);
                }
                catch (final Exception ex)
                {
                    Log.getLogger().error(ex);
                }
            }

            return new StandardRequestIdentitiesDataStore(token, map);
        }

        @Override
        public void serialize(IFactoryController controller, StandardRequestIdentitiesDataStore input, FriendlyByteBuf packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.getIdentities().size());
            input.getIdentities().forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                controller.serialize(packetBuffer, value);
            });
        }

        @Override
        public StandardRequestIdentitiesDataStore deserialize(IFactoryController controller, FriendlyByteBuf buffer)
          throws Throwable
        {
            final IToken<?> token = controller.deserialize(buffer);
            final Map<IToken<?>, IRequest<?>> identities = new HashMap<>();
            final int assignmentsSize = buffer.readInt();
            for (int i = 0; i < assignmentsSize; ++i)
            {
                identities.put(controller.deserialize(buffer), controller.deserialize(buffer));
            }

            final BiMap<IToken<?>, IRequest<?>> biMap = HashBiMap.create(identities);

            return new StandardRequestIdentitiesDataStore(token, biMap);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_REQUEST_IDENTITIES_DATASTORE_ID;
        }
    }
}
