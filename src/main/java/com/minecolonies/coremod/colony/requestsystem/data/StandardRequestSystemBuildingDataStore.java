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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class StandardRequestSystemBuildingDataStore implements IRequestSystemBuildingDataStore
{

    private IToken<?>                                id;
    private final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType;
    private final Map<Integer, Collection<IToken<?>>>      openRequestsByCitizen;
    private final Map<Integer, Collection<IToken<?>>>      completedRequestsByCitizen;
    private final Map<IToken<?>, Integer>                  citizenByOpenRequest;

    public StandardRequestSystemBuildingDataStore(
      final IToken<?> id,
      final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType,
      final Map<Integer, Collection<IToken<?>>> openRequestsByCitizen,
      final Map<Integer, Collection<IToken<?>>> completedRequestsByCitizen,
      final Map<IToken<?>, Integer> citizenByOpenRequest) {
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
        public NBTTagCompound serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestSystemBuildingDataStore standardRequestSystemBuildingDataStore)
        {
            final NBTTagCompound compound = new NBTTagCompound();

            compound.setTag(TAG_TOKEN, controller.serialize(standardRequestSystemBuildingDataStore.id));
            compound.setTag(TAG_OPEN_REQUESTS_BY_TYPE, standardRequestSystemBuildingDataStore.openRequestsByRequestableType.keySet().stream().map(typeToken -> {
                final NBTTagCompound entryCompound = new NBTTagCompound();

                entryCompound.setTag(TAG_TOKEN, controller.serialize(typeToken));
                entryCompound.setTag(TAG_LIST, standardRequestSystemBuildingDataStore.openRequestsByRequestableType.get(typeToken)
                                                 .stream()
                                                 .map(controller::serialize)
                                                 .collect(NBTUtils.toNBTTagList()));

                return entryCompound;
            }).collect(NBTUtils.toNBTTagList()));
            compound.setTag(TAG_OPEN_REQUESTS_BY_CITIZEN, standardRequestSystemBuildingDataStore.openRequestsByCitizen.keySet().stream().map(integer -> {
                final NBTTagCompound entryCompound = new NBTTagCompound();

                entryCompound.setTag(TAG_TOKEN, controller.serialize(integer));
                entryCompound.setTag(TAG_LIST, standardRequestSystemBuildingDataStore.openRequestsByCitizen.get(integer)
                                                 .stream()
                                                 .map(controller::serialize)
                                                 .collect(NBTUtils.toNBTTagList()));

                return entryCompound;
            }).collect(NBTUtils.toNBTTagList()));
            compound.setTag(TAG_COMPLETED_REQUESTS_BY_CITIZEN, standardRequestSystemBuildingDataStore.completedRequestsByCitizen.keySet().stream().map(integer -> {
                final NBTTagCompound entryCompound = new NBTTagCompound();

                entryCompound.setTag(TAG_TOKEN, controller.serialize(integer));
                entryCompound.setTag(TAG_LIST, standardRequestSystemBuildingDataStore.completedRequestsByCitizen.get(integer)
                                                 .stream()
                                                 .map(controller::serialize)
                                                 .collect(NBTUtils.toNBTTagList()));

                return entryCompound;
            }).collect(NBTUtils.toNBTTagList()));
            compound.setTag(TAG_CITIZEN_BY_OPEN_REQUEST, standardRequestSystemBuildingDataStore.citizenByOpenRequest.keySet().stream().map(iToken -> {
                final NBTTagCompound entryCompound = new NBTTagCompound();

                entryCompound.setTag(TAG_TOKEN, controller.serialize(iToken));
                entryCompound.setTag(TAG_VALUE, controller.serialize(standardRequestSystemBuildingDataStore.citizenByOpenRequest.get(iToken)));

                return entryCompound;
            }).collect(NBTUtils.toNBTTagList()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemBuildingDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt) throws Throwable
        {
            final IToken<?> token = controller.deserialize(nbt.getCompoundTag(TAG_TOKEN));
            final Map<TypeToken<?>, Collection<IToken<?>>> openRequestsByRequestableType = NBTUtils
                                                                                             .streamCompound(nbt.getTagList(TAG_OPEN_REQUESTS_BY_TYPE, Constants.NBT.TAG_COMPOUND)).map(nbtTagCompound -> {
                  final TypeToken<?> key = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_TOKEN));
                  final Collection<IToken<?>> values = NBTUtils.streamCompound(nbtTagCompound.getTagList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                         .map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                         .collect(Collectors.toList());

                  return new Tuple<>(key, values);
              }).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
            final Map<Integer, Collection<IToken<?>>> openRequestsByCitizen = NBTUtils
                                                                                             .streamCompound(nbt.getTagList(TAG_OPEN_REQUESTS_BY_CITIZEN, Constants.NBT.TAG_COMPOUND)).map(nbtTagCompound -> {
                  final Integer key = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_TOKEN));
                  final Collection<IToken<?>> values = NBTUtils.streamCompound(nbtTagCompound.getTagList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                         .map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                         .collect(Collectors.toList());

                  return new Tuple<>(key, values);
              }).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
            final Map<Integer, Collection<IToken<?>>> completedRequestsByCitizen = NBTUtils
                                                                                .streamCompound(nbt.getTagList(TAG_COMPLETED_REQUESTS_BY_CITIZEN, Constants.NBT.TAG_COMPOUND)).map(nbtTagCompound -> {
                  final Integer key = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_TOKEN));
                  final Collection<IToken<?>> values = NBTUtils.streamCompound(nbtTagCompound.getTagList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                         .map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                         .collect(Collectors.toList());

                  return new Tuple<>(key, values);
              }).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
            final Map<IToken<?>, Integer> citizenByOpenRequest = NBTUtils
                                                                                     .streamCompound(nbt.getTagList(TAG_CITIZEN_BY_OPEN_REQUEST, Constants.NBT.TAG_COMPOUND)).map(nbtTagCompound -> {
                  final IToken<?> key = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_TOKEN));
                  final Integer value = controller.deserialize(nbtTagCompound.getCompoundTag(TAG_VALUE));

                  return new Tuple<>(key, value);
              }).collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));

            return new StandardRequestSystemBuildingDataStore(token, openRequestsByRequestableType, openRequestsByCitizen, completedRequestsByCitizen, citizenByOpenRequest);
        }
    }
}
