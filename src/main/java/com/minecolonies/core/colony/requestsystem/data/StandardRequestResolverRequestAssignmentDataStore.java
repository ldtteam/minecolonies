package com.minecolonies.core.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestResolverRequestAssignmentDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class StandardRequestResolverRequestAssignmentDataStore implements IRequestResolverRequestAssignmentDataStore
{

    private final Map<IToken<?>, Collection<IToken<?>>> assignments;
    private       IToken<?>                             id;

    public StandardRequestResolverRequestAssignmentDataStore(
      final IToken<?> id,
      final Map<IToken<?>, Collection<IToken<?>>> assignments
    )
    {
        this.id = id;
        this.assignments = assignments;
    }

    public StandardRequestResolverRequestAssignmentDataStore()
    {
        this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN), new HashMap<>());
    }

    @NotNull
    @Override
    public Map<IToken<?>, Collection<IToken<?>>> getAssignments()
    {
        return assignments;
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

    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestResolverRequestAssignmentDataStore>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardRequestResolverRequestAssignmentDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestResolverRequestAssignmentDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestResolverRequestAssignmentDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestResolverRequestAssignmentDataStore();
        }

        @NotNull
        @Override
        public CompoundTag serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestResolverRequestAssignmentDataStore standardProviderRequestResolverAssignmentDataStore)
        {
            final CompoundTag compound = new CompoundTag();

            compound.put(NbtTagConstants.TAG_TOKEN, controller.serialize(standardProviderRequestResolverAssignmentDataStore.id));
            compound.put(NbtTagConstants.TAG_LIST, standardProviderRequestResolverAssignmentDataStore.assignments.keySet().stream().map(t -> {
                final CompoundTag entryCompound = new CompoundTag();

                entryCompound.put(NbtTagConstants.TAG_TOKEN, controller.serialize(t));
                entryCompound.put(NbtTagConstants.TAG_LIST, standardProviderRequestResolverAssignmentDataStore.assignments.get(t).stream()
                                                              .map(StandardFactoryController.getInstance()::serialize)
                                                              .collect(NBTUtils.toListNBT()));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestResolverRequestAssignmentDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
        {
            final IToken<?> token = controller.deserializeTag(nbt.getCompound(NbtTagConstants.TAG_TOKEN));
            final Map<IToken<?>, Collection<IToken<?>>> map = NBTUtils.streamCompound(nbt.getList(NbtTagConstants.TAG_LIST, Tag.TAG_COMPOUND))
                                                                .map(CompoundTag -> {
                                                                    final IToken<?> elementToken = controller.deserializeTag(CompoundTag.getCompound(NbtTagConstants.TAG_TOKEN));
                                                                    final Collection<IToken<?>> elements = NBTUtils.streamCompound(CompoundTag.getList(NbtTagConstants.TAG_LIST,
                                                                      Tag.TAG_COMPOUND)).map(elementCompound -> (IToken<?>) controller.deserializeTag(elementCompound))
                                                                                                             .collect(Collectors.toList());

                                                                    return new Tuple<>(elementToken, elements);
                                                                }).collect(Collectors.toMap(t -> t.getA(), t -> t.getB()));

            return new StandardRequestResolverRequestAssignmentDataStore(token, map);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestResolverRequestAssignmentDataStore input,
          FriendlyByteBuf packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.assignments.size());
            input.assignments.forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                packetBuffer.writeInt(value.size());
                value.forEach(token -> controller.serialize(packetBuffer, token));
            });
        }

        @Override
        public StandardRequestResolverRequestAssignmentDataStore deserialize(
          IFactoryController controller,
          FriendlyByteBuf buffer) throws Throwable
        {
            final IToken<?> token = controller.deserializeTag(buffer);
            final Map<IToken<?>, Collection<IToken<?>>> assignments = new HashMap<>();
            final int assignmentsSize = buffer.readInt();
            for (int i = 0; i < assignmentsSize; ++i)
            {
                final IToken<?> key = controller.deserializeTag(buffer);
                final List<IToken<?>> tokens = new ArrayList<>();
                final int tokensSize = buffer.readInt();
                for (int ii = 0; ii < tokensSize; ++ii)
                {
                    tokens.add(controller.deserializeTag(buffer));
                }
                assignments.put(key, tokens);
            }

            return new StandardRequestResolverRequestAssignmentDataStore(token, assignments);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_REQUEST_RESOLVER_REQUEST_ASSIGNMENT_DATASTORE_ID;
        }
    }
}
