package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class StandardRequestSystemBuildingDataStore implements IRequestSystemBuildingDataStore
{

    private       IToken<?>                                id;
    private final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType;
    private final Map<Integer, Collection<IToken<?>>>      openRequestsByCitizen;
    private final Map<Integer, Collection<IToken<?>>>      completedRequestsByCitizen;
    private final Map<IToken<?>, Integer>                  citizenByOpenRequest;

    public StandardRequestSystemBuildingDataStore(
      final IToken<?> id,
      final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType,
      final Map<Integer, Collection<IToken<?>>> openRequestsByCitizen,
      final Map<Integer, Collection<IToken<?>>> completedRequestsByCitizen,
      final Map<IToken<?>, Integer> citizenByOpenRequest)
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
          new HashMap<>(),
          new HashMap<>(),
          new HashMap<>());
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
    public IToken<?> getId()
    {
        return id;
    }

    @Override
    public void setId(final IToken<?> id)
    {
        this.id = id;
    }

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
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestSystemBuildingDataStore();
        }

        @NotNull
        @Override
        public CompoundNBT serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestSystemBuildingDataStore standardRequestSystemBuildingDataStore)
        {
            final CompoundNBT compound = new CompoundNBT();

            compound.put(TAG_TOKEN, controller.serialize(standardRequestSystemBuildingDataStore.id));
            compound.put(TAG_OPEN_REQUESTS_BY_TYPE, standardRequestSystemBuildingDataStore.openRequestsByRequestableType.keySet().stream().map(typeToken -> {
                final CompoundNBT entryCompound = new CompoundNBT();

                entryCompound.put(TAG_TOKEN, controller.serialize(typeToken));
                entryCompound.put(TAG_LIST, standardRequestSystemBuildingDataStore.openRequestsByRequestableType.get(typeToken)
                                              .stream()
                                              .map(controller::serialize)
                                              .collect(NBTUtils.toListNBT()));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));
            compound.put(TAG_OPEN_REQUESTS_BY_CITIZEN, standardRequestSystemBuildingDataStore.openRequestsByCitizen.keySet().stream().map(integer -> {
                final CompoundNBT entryCompound = new CompoundNBT();

                entryCompound.put(TAG_TOKEN, controller.serialize(integer));
                entryCompound.put(TAG_LIST, standardRequestSystemBuildingDataStore.openRequestsByCitizen.get(integer)
                                              .stream()
                                              .map(controller::serialize)
                                              .collect(NBTUtils.toListNBT()));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));
            compound.put(TAG_COMPLETED_REQUESTS_BY_CITIZEN, standardRequestSystemBuildingDataStore.completedRequestsByCitizen.keySet().stream().map(integer -> {
                final CompoundNBT entryCompound = new CompoundNBT();

                entryCompound.put(TAG_TOKEN, controller.serialize(integer));
                entryCompound.put(TAG_LIST, standardRequestSystemBuildingDataStore.completedRequestsByCitizen.get(integer)
                                              .stream()
                                              .map(controller::serialize)
                                              .collect(NBTUtils.toListNBT()));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));
            compound.put(TAG_CITIZEN_BY_OPEN_REQUEST, standardRequestSystemBuildingDataStore.citizenByOpenRequest.keySet().stream().map(iToken -> {
                final CompoundNBT entryCompound = new CompoundNBT();

                entryCompound.put(TAG_TOKEN, controller.serialize(iToken));
                entryCompound.put(TAG_VALUE, controller.serialize(standardRequestSystemBuildingDataStore.citizenByOpenRequest.get(iToken)));

                return entryCompound;
            }).collect(NBTUtils.toListNBT()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemBuildingDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt) throws Throwable
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType = NBTUtils
                                                                                             .streamCompound(nbt.getList(TAG_OPEN_REQUESTS_BY_TYPE, Constants.NBT.TAG_COMPOUND))
                                                                                             .map(CompoundNBT -> {
                                                                                                 final TypeToken<?> key =
                                                                                                   controller.deserialize(CompoundNBT.getCompound(TAG_TOKEN));
                                                                                                 final Collection<IToken<?>> values = NBTUtils.streamCompound(CompoundNBT.getList(
                                                                                                   TAG_LIST,
                                                                                                   Constants.NBT.TAG_COMPOUND))
                                                                                                                                        .map(elementCompound -> (IToken<?>) controller
                                                                                                                                                                              .deserialize(
                                                                                                                                                                                elementCompound))
                                                                                                                                        .collect(Collectors.toList());

                                                                                                 return new Tuple<>(key, values);
                                                                                             })
                                                                                             .collect(Collectors.toMap(Tuple::getA, Tuple::getB));
            final Map<Integer, Collection<IToken<?>>> openRequestsByCitizen = NBTUtils
                                                                                .streamCompound(nbt.getList(TAG_OPEN_REQUESTS_BY_CITIZEN, Constants.NBT.TAG_COMPOUND))
                                                                                .map(CompoundNBT -> {
                                                                                    final Integer key = controller.deserialize(CompoundNBT.getCompound(TAG_TOKEN));
                                                                                    final Collection<IToken<?>> values =
                                                                                      NBTUtils.streamCompound(CompoundNBT.getList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                                                        .map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                                                        .collect(Collectors.toList());

                                                                                    return new Tuple<>(key, values);
                                                                                })
                                                                                .collect(Collectors.toMap(Tuple::getA, Tuple::getB));
            final Map<Integer, Collection<IToken<?>>> completedRequestsByCitizen = NBTUtils
                                                                                     .streamCompound(nbt.getList(TAG_COMPLETED_REQUESTS_BY_CITIZEN, Constants.NBT.TAG_COMPOUND))
                                                                                     .map(CompoundNBT -> {
                                                                                         final Integer key = controller.deserialize(CompoundNBT.getCompound(TAG_TOKEN));
                                                                                         final Collection<IToken<?>> values =
                                                                                           NBTUtils.streamCompound(CompoundNBT.getList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                                                             .map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                                                             .collect(Collectors.toList());

                                                                                         return new Tuple<>(key, values);
                                                                                     })
                                                                                     .collect(Collectors.toMap(Tuple::getA, Tuple::getB));
            final Map<IToken<?>, Integer> citizenByOpenRequest = NBTUtils
                                                                   .streamCompound(nbt.getList(TAG_CITIZEN_BY_OPEN_REQUEST, Constants.NBT.TAG_COMPOUND)).map(CompoundNBT -> {
                  final IToken<?> key = controller.deserialize(CompoundNBT.getCompound(TAG_TOKEN));
                  final Integer value = controller.deserialize(CompoundNBT.getCompound(TAG_VALUE));

                  return new Tuple<>(key, value);
              }).collect(Collectors.toMap(Tuple::getA, Tuple::getB));

            return new StandardRequestSystemBuildingDataStore(token, openRequestsByRequestableType, openRequestsByCitizen, completedRequestsByCitizen, citizenByOpenRequest);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestSystemBuildingDataStore input,
          PacketBuffer packetBuffer)
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
        public StandardRequestSystemBuildingDataStore deserialize(IFactoryController controller, PacketBuffer buffer)
          throws Throwable
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

            final Map<Integer, Collection<IToken<?>>> openRequestsByCitizen = new HashMap<>();
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

            final Map<Integer, Collection<IToken<?>>> completedRequestsByCitizen = new HashMap<>();
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

            final Map<IToken<?>, Integer> citizenByOpenRequest = new HashMap<>();
            final int citizenByOpenRequestSize = buffer.readInt();
            for (int i = 0; i < citizenByOpenRequestSize; ++i)
            {
                citizenByOpenRequest.put(controller.deserialize(buffer), controller.deserialize(buffer));
            }

            return new StandardRequestSystemBuildingDataStore(id, openRequestsByRequestableType, openRequestsByCitizen, completedRequestsByCitizen, citizenByOpenRequest);
        }

        @Override
        public short getSerializationId()
        {
            return 37;
        }
    }
}
