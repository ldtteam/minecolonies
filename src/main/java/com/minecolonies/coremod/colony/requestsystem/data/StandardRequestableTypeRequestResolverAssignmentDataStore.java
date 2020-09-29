package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestableTypeRequestResolverAssignmentDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class StandardRequestableTypeRequestResolverAssignmentDataStore implements IRequestableTypeRequestResolverAssignmentDataStore
{

    private       IToken<?>                                id;
    private final Map<TypeToken<?>, Collection<IToken<?>>> assignments;

    public StandardRequestableTypeRequestResolverAssignmentDataStore(
      final IToken<?> id,
      final Map<TypeToken<?>, Collection<IToken<?>>> assignments)
    {
        this.id = id;
        this.assignments = assignments;
    }

    public StandardRequestableTypeRequestResolverAssignmentDataStore()
    {
        this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN), new HashMap<>());
    }

    @NotNull
    @Override
    public Map<TypeToken<?>, Collection<IToken<?>>> getAssignments()
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

    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestableTypeRequestResolverAssignmentDataStore>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardRequestableTypeRequestResolverAssignmentDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestableTypeRequestResolverAssignmentDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestableTypeRequestResolverAssignmentDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestableTypeRequestResolverAssignmentDataStore();
        }

        @NotNull
        @Override
        public CompoundNBT serialize(
          @NotNull final IFactoryController controller,
          @NotNull final StandardRequestableTypeRequestResolverAssignmentDataStore standardRequestableTypeRequestResolverAssignmentDataStore)
        {
            CompoundNBT compound = new CompoundNBT();

            compound.put(NbtTagConstants.TAG_TOKEN, controller.serialize(standardRequestableTypeRequestResolverAssignmentDataStore.id));
            compound.put(NbtTagConstants.TAG_LIST, standardRequestableTypeRequestResolverAssignmentDataStore.assignments.keySet().stream().map(t -> {
                CompoundNBT entryCompound = new CompoundNBT();

                entryCompound.put(NbtTagConstants.TAG_TOKEN, controller.serialize(t));
                entryCompound.put(NbtTagConstants.TAG_LIST, standardRequestableTypeRequestResolverAssignmentDataStore.assignments.get(t).stream()
                                                              .map(StandardFactoryController.getInstance()::serialize)
                                                              .collect(NBTUtils.toListNBT()));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestableTypeRequestResolverAssignmentDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
          throws Throwable
        {
            IToken<?> token = controller.deserialize(nbt.getCompound(NbtTagConstants.TAG_TOKEN));
            Map<TypeToken<?>, Collection<IToken<?>>> map = NBTUtils.streamCompound(nbt.getList(NbtTagConstants.TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                             .map(CompoundNBT -> {
                                                                 final TypeToken<?> elementToken = controller.deserialize(CompoundNBT.getCompound(NbtTagConstants.TAG_TOKEN));
                                                                 final Collection<IToken<?>> elements = NBTUtils.streamCompound(CompoundNBT.getList(NbtTagConstants.TAG_LIST,
                                                                   Constants.NBT.TAG_COMPOUND)).map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                                                                          .collect(Collectors.toList());

                                                                 return new Tuple<>(elementToken, elements);
                                                             }).collect(Collectors.toMap(t -> t.getA(), t -> t.getB()));

            return new StandardRequestableTypeRequestResolverAssignmentDataStore(token, map);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestableTypeRequestResolverAssignmentDataStore input,
          PacketBuffer packetBuffer)
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
        public StandardRequestableTypeRequestResolverAssignmentDataStore deserialize(
          IFactoryController controller,
          PacketBuffer buffer) throws Throwable
        {
            final IToken<?> token = controller.deserialize(buffer);
            final Map<TypeToken<?>, Collection<IToken<?>>> assignments = new HashMap<>();
            final int assignmentsSize = buffer.readInt();
            for (int i = 0; i < assignmentsSize; ++i)
            {
                final TypeToken<?> key = controller.deserialize(buffer);
                final List<IToken<?>> tokens = new ArrayList<>();
                final int tokensSize = buffer.readInt();
                for (int ii = 0; ii < tokensSize; ++ii)
                {
                    tokens.add(controller.deserialize(buffer));
                }
                assignments.put(key, tokens);
            }

            return new StandardRequestableTypeRequestResolverAssignmentDataStore(token, assignments);
        }

        @Override
        public short getSerializationId()
        {
            return 36;
        }
    }
}
