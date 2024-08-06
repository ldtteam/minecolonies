package com.minecolonies.core.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemDeliveryManJobDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class StandardRequestSystemDeliveryManJobDataStore implements IRequestSystemDeliveryManJobDataStore
{

    private       IToken<?>             id;
    private final LinkedList<IToken<?>> queue;
    private final Set<IToken<?>> ongoingDeliveries;

    public StandardRequestSystemDeliveryManJobDataStore(final IToken<?> id, final LinkedList<IToken<?>> queue, final Set<IToken<?>> ongoingDeliveries)
    {
        this.id = id;
        this.queue = queue;
        this.ongoingDeliveries = ongoingDeliveries;
    }

    public StandardRequestSystemDeliveryManJobDataStore()
    {
        this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
          new LinkedList<>(), new HashSet<>());
    }

    @Override
    public LinkedList<IToken<?>> getQueue()
    {
        return queue;
    }

    @Override
    public Set<IToken<?>> getOngoingDeliveries()
    {
        return ongoingDeliveries;
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

    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestSystemDeliveryManJobDataStore>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardRequestSystemDeliveryManJobDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestSystemDeliveryManJobDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestSystemDeliveryManJobDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestSystemDeliveryManJobDataStore();
        }

        @NotNull
        @Override
        public CompoundTag serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestSystemDeliveryManJobDataStore standardRequestSystemDeliveryManJobDataStore)
        {
            final CompoundTag compound = new CompoundTag();
            compound.put(TAG_TOKEN, controller.serialize(standardRequestSystemDeliveryManJobDataStore.id));
            compound.put(TAG_LIST, standardRequestSystemDeliveryManJobDataStore.queue.stream().map(controller::serialize).collect(NBTUtils.toListNBT()));
            compound.put(TAG_ONGOING_LIST, standardRequestSystemDeliveryManJobDataStore.ongoingDeliveries.stream().map(controller::serialize).collect(NBTUtils.toListNBT()));
            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemDeliveryManJobDataStore deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
        {
            final IToken<?> token = controller.deserializeTag(provider, nbt.getCompound(TAG_TOKEN));
            final LinkedList<IToken<?>> queue = NBTUtils.streamCompound(nbt.getList(TAG_LIST, Tag.TAG_COMPOUND))
                                                  .map(CompoundTag -> (IToken<?>) controller.deserializeTag(CompoundTag))
                                                  .collect(Collectors.toCollection(LinkedList::new));
            final HashSet<IToken<?>> ongoingDeliveries = NBTUtils.streamCompound(nbt.getList(TAG_ONGOING_LIST, Tag.TAG_COMPOUND))
                                                  .map(CompoundTag -> (IToken<?>) controller.deserializeTag(CompoundTag))
                                                  .collect(Collectors.toCollection(HashSet::new));
            return new StandardRequestSystemDeliveryManJobDataStore(token, queue, ongoingDeliveries);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestSystemDeliveryManJobDataStore input,
          RegistryFriendlyByteBuf packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.queue.size());
            input.queue.forEach(entry -> controller.serialize(packetBuffer, entry));
            packetBuffer.writeInt(input.ongoingDeliveries.size());
            input.ongoingDeliveries.forEach(entry -> controller.serialize(packetBuffer, entry));
        }

        @NotNull
        @Override
        public StandardRequestSystemDeliveryManJobDataStore deserialize(
          IFactoryController controller,
          @NotNull RegistryFriendlyByteBuf buffer) throws Throwable
        {
            final IToken<?> id = controller.deserializeTag(buffer);
            final LinkedList<IToken<?>> queue = new LinkedList<>();
            final Set<IToken<?>> ongoingDeliveries = new HashSet<>();

            final int queueSize = buffer.readInt();
            for (int i = 0; i < queueSize; ++i)
            {
                queue.add(controller.deserializeTag(buffer));
            }

            final int ongoingSize = buffer.readInt();
            for (int i = 0; i < ongoingSize; ++i)
            {
                queue.add(controller.deserializeTag(buffer));
            }

            return new StandardRequestSystemDeliveryManJobDataStore(id, queue, ongoingDeliveries);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_REQUEST_SYSTEM_DELIVERY_MAN_JOB_DATASTORE_ID;
        }
    }
}
