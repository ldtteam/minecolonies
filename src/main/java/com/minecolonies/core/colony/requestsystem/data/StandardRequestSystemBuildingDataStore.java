package com.minecolonies.core.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Datastore for request data for buildings.
 */
public class StandardRequestSystemBuildingDataStore implements IRequestSystemBuildingDataStore
{
    /**
     * The id of this data store.
     */
    private IToken<?> id;

    /**
     * Requests sorted by request type.
     */
    private final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType;

    /**
     * Requests sorted by citizen (or -1 for building)
     */
    private final Int2ObjectMap<Collection<IToken<?>>> openRequestsByCitizen;

    /**
     * Completed Requests sorted by citizen (or -1 for building)
     */
    private final Int2ObjectMap<Collection<IToken<?>>> completedRequestsByCitizen;

    /**
     * Inverted requests sorted by request to citizen.
     */
    private final Object2IntMap<IToken<?>> citizenByOpenRequest;

    public StandardRequestSystemBuildingDataStore(
      final IToken<?> id,
      final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType,
      final Int2ObjectMap<Collection<IToken<?>>> openRequestsByCitizen,
      final Int2ObjectMap<Collection<IToken<?>>> completedRequestsByCitizen,
      final Object2IntMap<IToken<?>> citizenByOpenRequest)
    {
        this.id = id;
        this.openRequestsByRequestableType = openRequestsByRequestableType;
        this.openRequestsByCitizen = openRequestsByCitizen;
        this.completedRequestsByCitizen = completedRequestsByCitizen;
        this.citizenByOpenRequest = citizenByOpenRequest;
    }

    public StandardRequestSystemBuildingDataStore()
    {
        this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
          new HashMap<>(),
          new Int2ObjectOpenHashMap<>(),
          new Int2ObjectOpenHashMap<>(),
          new Object2IntOpenHashMap<>());
    }

    @Override
    public Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
    {
        return openRequestsByRequestableType;
    }

    @Override
    public Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
    {
        return openRequestsByCitizen;
    }

    @Override
    public Map<Integer, Collection<IToken<?>>> getCompletedRequestsByCitizen()
    {
        return completedRequestsByCitizen;
    }

    @Override
    public Map<IToken<?>, Integer> getCitizensByRequest()
    {
        return citizenByOpenRequest;
    }

    @Override
    public void moveToSyncCitizen(final ICitizenData citizenData, final IRequest<?> request)
    {
        if (citizenByOpenRequest.getOrDefault(request.getId(), Integer.MAX_VALUE) == -1)
        {
            citizenByOpenRequest.removeInt(request.getId());
            citizenByOpenRequest.put(request.getId(), citizenData.getId());

            openRequestsByCitizen.getOrDefault(-1, new ArrayList<>()).remove(request.getId());

            final Collection<IToken<?>> list = openRequestsByCitizen.getOrDefault(citizenData.getId(), new ArrayList<>());
            list.add(request.getId());
            openRequestsByCitizen.put(citizenData.getId(), list);
        }

        if (completedRequestsByCitizen.getOrDefault(-1, new ArrayList<>()).contains(request.getId()))
        {
            completedRequestsByCitizen.get(-1).remove(request.getId());

            final Collection<IToken<?>> list = completedRequestsByCitizen.getOrDefault(citizenData.getId(), new ArrayList<>());
            list.add(request.getId());
            completedRequestsByCitizen.put(citizenData.getId(), list);
        }

        citizenData.getJob().markRequestSync(request.getId());
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

    /**
     * Factory to serialize/deserialize this datastore.
     */
    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestSystemBuildingDataStore>
    {
        @NotNull
        @Override
        public TypeToken<? extends StandardRequestSystemBuildingDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestSystemBuildingDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestSystemBuildingDataStore getNewInstance(
            @NotNull final IFactoryController factoryController,
            @NotNull final FactoryVoidInput factoryVoidInput,
            @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestSystemBuildingDataStore();
        }

        @NotNull
        @Override
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequestSystemBuildingDataStore dataStore)
        {
            final CompoundTag compound = new CompoundTag();

            compound.put(TAG_TOKEN, controller.serialize(dataStore.id));
            final ListTag openRequestsByRequestableTag = new ListTag();
            for (final Map.Entry<TypeToken<?>, Collection<IToken<?>>> data : dataStore.openRequestsByRequestableType.entrySet())
            {
                final CompoundTag entryCompound = new CompoundTag();
                entryCompound.put(TAG_TOKEN, controller.serialize(data.getKey()));
                entryCompound.put(TAG_LIST, controller.serializeList(data.getValue()));
                openRequestsByRequestableTag.add(entryCompound);
            }
            compound.put(TAG_OPEN_REQUESTS_BY_TYPE, openRequestsByRequestableTag);

            final ListTag openRequestsByCitizenTag = new ListTag();
            for (final Map.Entry<Integer, Collection<IToken<?>>> data : dataStore.openRequestsByCitizen.entrySet())
            {
                final CompoundTag entryCompound = new CompoundTag();
                entryCompound.put(TAG_TOKEN, controller.serialize(data.getKey()));
                entryCompound.put(TAG_LIST, controller.serializeList(data.getValue()));
                openRequestsByCitizenTag.add(entryCompound);
            }
            compound.put(TAG_OPEN_REQUESTS_BY_CITIZEN, openRequestsByCitizenTag);

            final ListTag completedRequestsByCitizenTag = new ListTag();
            for (final Map.Entry<Integer, Collection<IToken<?>>> data : dataStore.completedRequestsByCitizen.entrySet())
            {
                final CompoundTag entryCompound = new CompoundTag();
                entryCompound.put(TAG_TOKEN, controller.serialize(data.getKey()));
                entryCompound.put(TAG_LIST, controller.serializeList(data.getValue()));
                completedRequestsByCitizenTag.add(entryCompound);
            }
            compound.put(TAG_COMPLETED_REQUESTS_BY_CITIZEN, completedRequestsByCitizenTag);

            final ListTag citizenByOpenRequestTag = new ListTag();
            for (final Map.Entry<IToken<?>, Integer> data : dataStore.citizenByOpenRequest.entrySet())
            {
                final CompoundTag entryCompound = new CompoundTag();
                entryCompound.put(TAG_TOKEN, controller.serialize(data.getKey()));
                entryCompound.put(TAG_VALUE, controller.serialize(data.getValue()));
                citizenByOpenRequestTag.add(entryCompound);
            }
            compound.put(TAG_CITIZEN_BY_OPEN_REQUEST, citizenByOpenRequestTag);

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemBuildingDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType = new HashMap<>();
            for (final Tag tag : nbt.getList(TAG_OPEN_REQUESTS_BY_TYPE, Tag.TAG_COMPOUND))
            {
                final CompoundTag compoundTag = (CompoundTag) tag;
                openRequestsByRequestableType.put(controller.deserialize(compoundTag.getCompound(TAG_TOKEN)), controller.deserializeList(compoundTag.getList(TAG_LIST, Tag.TAG_COMPOUND)));
            }

            final Int2ObjectMap<Collection<IToken<?>>> openRequestsByCitizen = new Int2ObjectOpenHashMap<>();
            for (final Tag tag : nbt.getList(TAG_OPEN_REQUESTS_BY_CITIZEN, Tag.TAG_COMPOUND))
            {
                final CompoundTag compoundTag = (CompoundTag) tag;
                openRequestsByCitizen.put(controller.deserialize(compoundTag.getCompound(TAG_TOKEN)), controller.deserializeList(compoundTag.getList(TAG_LIST, Tag.TAG_COMPOUND)));
            }

            final Int2ObjectMap<Collection<IToken<?>>> completedRequestsByCitizen = new Int2ObjectOpenHashMap<>();
            for (final Tag tag : nbt.getList(TAG_COMPLETED_REQUESTS_BY_CITIZEN, Tag.TAG_COMPOUND))
            {
                final CompoundTag compoundTag = (CompoundTag) tag;
                completedRequestsByCitizen.put(controller.deserialize(compoundTag.getCompound(TAG_TOKEN)), controller.deserializeList(compoundTag.getList(TAG_LIST, Tag.TAG_COMPOUND)));
            }

            final Object2IntMap<IToken<?>> citizenByOpenRequest = new Object2IntOpenHashMap<>();
            for (final Tag tag : nbt.getList(TAG_CITIZEN_BY_OPEN_REQUEST, Tag.TAG_COMPOUND))
            {
                final CompoundTag compoundTag = (CompoundTag) tag;
                citizenByOpenRequest.put(controller.deserialize(compoundTag.getCompound(TAG_TOKEN)), controller.deserialize(compoundTag.getCompound(TAG_VALUE)));
            }

            return new StandardRequestSystemBuildingDataStore(token, openRequestsByRequestableType, openRequestsByCitizen, completedRequestsByCitizen, citizenByOpenRequest);
        }

        @Override
        public void serialize(
            @NotNull final IFactoryController controller,
            @NotNull final StandardRequestSystemBuildingDataStore input,
            @NotNull final FriendlyByteBuf packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.openRequestsByRequestableType.size());
            input.openRequestsByRequestableType.forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                packetBuffer.writeInt(value.size());
                value.forEach(token -> controller.serialize(packetBuffer, token));
            });

            packetBuffer.writeInt(input.openRequestsByCitizen.size());
            input.openRequestsByCitizen.forEach((key, value) -> {
                packetBuffer.writeInt(key);
                packetBuffer.writeInt(value.size());
                value.forEach(token -> controller.serialize(packetBuffer, token));
            });

            packetBuffer.writeInt(input.completedRequestsByCitizen.size());
            input.completedRequestsByCitizen.forEach((key, value) -> {
                packetBuffer.writeInt(key);
                packetBuffer.writeInt(value.size());
                value.forEach(token -> controller.serialize(packetBuffer, token));
            });

            packetBuffer.writeInt(input.citizenByOpenRequest.size());
            input.citizenByOpenRequest.forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                controller.serialize(packetBuffer, value);
            });
        }

        @Override
        public StandardRequestSystemBuildingDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final FriendlyByteBuf buffer) throws Throwable
        {
            final IToken<?> id = controller.deserialize(buffer);
            final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType = new HashMap<>();
            final int openRequestsByRequestableTypeSize = buffer.readInt();
            for (int i = 0; i < openRequestsByRequestableTypeSize; ++i)
            {
                final TypeToken<?> key = controller.deserialize(buffer);
                final List<IToken<?>> tokens = new ArrayList<>();
                final int tokensSize = buffer.readInt();
                for (int ii = 0; ii < tokensSize; ++ii)
                {
                    tokens.add(controller.deserialize(buffer));
                }
                openRequestsByRequestableType.put(key, tokens);
            }

            final Int2ObjectMap<Collection<IToken<?>>> openRequestsByCitizen = new Int2ObjectOpenHashMap<>();
            final int openRequestsByCitizenSize = buffer.readInt();
            for (int i = 0; i < openRequestsByCitizenSize; ++i)
            {
                final int key = buffer.readInt();
                final List<IToken<?>> tokens = new ArrayList<>();
                final int tokensSize = buffer.readInt();
                for (int ii = 0; ii < tokensSize; ++ii)
                {
                    tokens.add(controller.deserialize(buffer));
                }
                openRequestsByCitizen.put(key, tokens);
            }

            final Int2ObjectMap<Collection<IToken<?>>> completedRequestsByCitizen = new Int2ObjectOpenHashMap<>();
            final int completedRequestsByCitizenSize = buffer.readInt();
            for (int i = 0; i < completedRequestsByCitizenSize; ++i)
            {
                final int key = buffer.readInt();
                final List<IToken<?>> tokens = new ArrayList<>();
                final int tokensSize = buffer.readInt();
                for (int ii = 0; ii < tokensSize; ++ii)
                {
                    tokens.add(controller.deserialize(buffer));
                }
                completedRequestsByCitizen.put(key, tokens);
            }

            final Object2IntMap<IToken<?>> citizenByOpenRequest = new Object2IntOpenHashMap<>();
            final int citizenByOpenRequestSize = buffer.readInt();
            for (int i = 0; i < citizenByOpenRequestSize; ++i)
            {
                citizenByOpenRequest.put(controller.deserialize(buffer), (int) controller.deserialize(buffer));
            }

            return new StandardRequestSystemBuildingDataStore(id, openRequestsByRequestableType, openRequestsByCitizen, completedRequestsByCitizen, citizenByOpenRequest);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_REQUEST_SYSTEM_BUILDING_DATASTORE_ID;
        }
    }
}
