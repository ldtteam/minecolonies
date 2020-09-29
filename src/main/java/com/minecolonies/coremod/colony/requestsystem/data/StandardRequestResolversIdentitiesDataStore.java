package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestResolverIdentitiesDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * A default implementation of the {@link IRequestResolverIdentitiesDataStore} interface.
 */
public class StandardRequestResolversIdentitiesDataStore implements IRequestResolverIdentitiesDataStore
{
    private       IToken<?>                             id;
    private final BiMap<IToken<?>, IRequestResolver<?>> map;

    public StandardRequestResolversIdentitiesDataStore(
      final IToken<?> id,
      final BiMap<IToken<?>, IRequestResolver<?>> map)
    {
        this.id = id;
        this.map = map;
    }

    public StandardRequestResolversIdentitiesDataStore()
    {
        this.id = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN);
        this.map = HashBiMap.create();
    }

    @Override
    public BiMap<IToken<?>, IRequestResolver<?>> getIdentities()
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

    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestResolversIdentitiesDataStore>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardRequestResolversIdentitiesDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestResolversIdentitiesDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestResolversIdentitiesDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestResolversIdentitiesDataStore();
        }

        @NotNull
        @Override
        public CompoundNBT serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestResolversIdentitiesDataStore standardRequestIdentitiesDataStore)
        {
            final CompoundNBT systemCompound = new CompoundNBT();

            systemCompound.put(TAG_TOKEN, controller.serialize(standardRequestIdentitiesDataStore.getId()));
            systemCompound.put(TAG_LIST, standardRequestIdentitiesDataStore.getIdentities().keySet().stream().map(token -> {
                final CompoundNBT mapCompound = new CompoundNBT();

                mapCompound.put(TAG_TOKEN, controller.serialize(token));
                mapCompound.put(TAG_RESOLVER, controller.serialize(standardRequestIdentitiesDataStore.getIdentities().get(token)));

                return mapCompound;
            }).collect(NBTUtils.toListNBT()));

            return systemCompound;
        }

        @NotNull
        @Override
        public StandardRequestResolversIdentitiesDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final ListNBT list = nbt.getList(TAG_LIST, Constants.NBT.TAG_COMPOUND);

            final Map<IToken<?>, IRequestResolver<?>> map = NBTUtils.streamCompound(list).map(CompoundNBT -> {
                final IToken<?> id = controller.deserialize(CompoundNBT.getCompound(TAG_TOKEN));
                final IRequestResolver<?> resolver = controller.deserialize(CompoundNBT.getCompound(TAG_RESOLVER));

                return new Tuple<IToken<?>, IRequestResolver<?>>(id, resolver);
            }).collect(Collectors.toMap((Tuple<IToken<?>, IRequestResolver<?>> t) -> t.getA(), (Tuple<IToken<?>, IRequestResolver<?>> t) -> t.getB()));

            final BiMap<IToken<?>, IRequestResolver<?>> biMap = HashBiMap.create(map);

            return new StandardRequestResolversIdentitiesDataStore(token, biMap);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestResolversIdentitiesDataStore input,
          PacketBuffer packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.getIdentities().size());
            input.getIdentities().forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                controller.serialize(packetBuffer, value);
            });
        }

        @Override
        public StandardRequestResolversIdentitiesDataStore deserialize(
          IFactoryController controller,
          PacketBuffer buffer) throws Throwable
        {
            final IToken<?> token = controller.deserialize(buffer);
            final Map<IToken<?>, IRequestResolver<?>> identities = new HashMap<>();
            final int assignmentsSize = buffer.readInt();
            for (int i = 0; i < assignmentsSize; ++i)
            {
                identities.put(controller.deserialize(buffer), controller.deserialize(buffer));
            }

            final BiMap<IToken<?>, IRequestResolver<?>> biMap = HashBiMap.create(identities);

            return new StandardRequestResolversIdentitiesDataStore(token, biMap);
        }

        @Override
        public short getSerializationId()
        {
            return 33;
        }
    }
}
