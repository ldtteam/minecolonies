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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StandardRequestableTypeRequestResolverAssignmentDataStore implements IRequestableTypeRequestResolverAssignmentDataStore
{

    private IToken<?> id;
    private final Map<TypeToken<?>, Collection<IToken<?>>> assignments;

    public StandardRequestableTypeRequestResolverAssignmentDataStore(
      final IToken<?> id,
      final Map<TypeToken<?>, Collection<IToken<?>>> assignments) {
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
        public NBTTagCompound serialize(
          @NotNull final IFactoryController controller,
          @NotNull final StandardRequestableTypeRequestResolverAssignmentDataStore standardRequestableTypeRequestResolverAssignmentDataStore)
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setTag(NbtTagConstants.TAG_TOKEN, controller.serialize(standardRequestableTypeRequestResolverAssignmentDataStore.id));
            compound.setTag(NbtTagConstants.TAG_LIST, standardRequestableTypeRequestResolverAssignmentDataStore.assignments.keySet().stream().map(t -> {
                NBTTagCompound entryCompound = new NBTTagCompound();

                entryCompound.setTag(NbtTagConstants.TAG_TOKEN, controller.serialize(t));
                entryCompound.setTag(NbtTagConstants.TAG_LIST, standardRequestableTypeRequestResolverAssignmentDataStore.assignments.get(t).stream()
                                                                 .map(StandardFactoryController.getInstance()::serialize)
                                                                 .collect(NBTUtils.toNBTTagList()));

                return entryCompound;
            }).collect(NBTUtils.toNBTTagList()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestableTypeRequestResolverAssignmentDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
          throws Throwable
        {
            IToken<?> token = controller.deserialize(nbt.getCompoundTag(NbtTagConstants.TAG_TOKEN));
            Map<TypeToken<?>, Collection<IToken<?>>> map = NBTUtils.streamCompound(nbt.getTagList(NbtTagConstants.TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                          .map(nbtTagCompound -> {
                                                              final TypeToken<?> elementToken = controller.deserialize(nbtTagCompound.getCompoundTag(NbtTagConstants.TAG_TOKEN));
                                                              final Collection<IToken<?>> elements = NBTUtils.streamCompound(nbtTagCompound.getTagList(NbtTagConstants.TAG_LIST,
                                                                Constants.NBT.TAG_COMPOUND)).map(elementCompound -> (IToken<?>) controller.deserialize(elementCompound))
                                                                                                       .collect(Collectors.toList());

                                                              return new Tuple<>(elementToken, elements);
                                                          }).collect(Collectors.toMap(t -> t.getFirst(), t -> t.getSecond()));

            return new StandardRequestableTypeRequestResolverAssignmentDataStore(token, map);
        }
    }
}
