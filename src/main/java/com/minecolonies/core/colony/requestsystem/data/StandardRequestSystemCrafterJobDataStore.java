package com.minecolonies.core.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemCrafterJobDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Crafter data store.
 */
public class StandardRequestSystemCrafterJobDataStore implements IRequestSystemCrafterJobDataStore
{
    /**
     * The id of the store.
     */
    private IToken<?> id;

    /**
     * The queue of the store.
     */
    private final LinkedList<IToken<?>> queue;
    private final List<IToken<?>>       tasks;

    /**
     * Constructor to create the data store.
     *
     * @param id    the id of it.
     * @param queue the queue to start with.
     * @param tasks the task.
     */
    public StandardRequestSystemCrafterJobDataStore(
      final IToken<?> id,
      final LinkedList<IToken<?>> queue,
      final List<IToken<?>> tasks)
    {
        this.id = id;
        this.queue = queue;
        this.tasks = tasks;
    }

    /**
     * Standard constructor to initialize from zero.
     */
    public StandardRequestSystemCrafterJobDataStore()
    {
        this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN), new LinkedList<>(), new ArrayList<>());
    }

    @Override
    public LinkedList<IToken<?>> getQueue()
    {
        return queue;
    }

    @Override
    public List<IToken<?>> getAssignedTasks()
    {
        return this.tasks;
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
     * Factory to work with the datastore.
     */
    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestSystemCrafterJobDataStore>
    {
        @NotNull
        @Override
        public TypeToken<? extends StandardRequestSystemCrafterJobDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestSystemCrafterJobDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestSystemCrafterJobDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestSystemCrafterJobDataStore();
        }

        @NotNull
        @Override
        public CompoundTag serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestSystemCrafterJobDataStore standardRequestSystemCrafterJobDataStore)
        {
            final CompoundTag compound = new CompoundTag();

            compound.put(TAG_TOKEN, controller.serialize(standardRequestSystemCrafterJobDataStore.id));
            compound.put(TAG_LIST, standardRequestSystemCrafterJobDataStore.queue.stream().map(controller::serialize).collect(NBTUtils.toListNBT()));
            compound.put(TAG_ASSIGNED_LIST, standardRequestSystemCrafterJobDataStore.tasks.stream().map(controller::serialize).collect(NBTUtils.toListNBT()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemCrafterJobDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final LinkedList<IToken<?>> queue = NBTUtils.streamCompound(nbt.getList(TAG_LIST, Tag.TAG_COMPOUND))
                                                  .map(CompoundTag -> (IToken<?>) controller.deserialize(CompoundTag))
                                                  .collect(Collectors.toCollection(LinkedList::new));
            final List<IToken<?>> taskList = NBTUtils.streamCompound(nbt.getList(TAG_ASSIGNED_LIST, Tag.TAG_COMPOUND))
                                               .map(CompoundTag -> (IToken<?>) controller.deserialize(CompoundTag))
                                               .collect(Collectors.toList());

            return new StandardRequestSystemCrafterJobDataStore(token, queue, taskList);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestSystemCrafterJobDataStore input,
          FriendlyByteBuf packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.queue.size());
            input.queue.forEach(entry -> controller.serialize(packetBuffer, entry));
            packetBuffer.writeInt(input.tasks.size());
            input.tasks.forEach(task -> controller.serialize(packetBuffer, task));
        }

        @Override
        public StandardRequestSystemCrafterJobDataStore deserialize(IFactoryController controller, FriendlyByteBuf buffer)
          throws Throwable
        {
            final IToken<?> id = controller.deserialize(buffer);
            final LinkedList<IToken<?>> queue = new LinkedList<>();
            final int queueSize = buffer.readInt();
            for (int i = 0; i < queueSize; ++i)
            {
                queue.add(controller.deserialize(buffer));
            }

            final List<IToken<?>> tasks = new ArrayList<>();
            final int tasksSize = buffer.readInt();
            for (int i = 0; i < tasksSize; ++i)
            {
                tasks.add(controller.deserialize(buffer));
            }

            return new StandardRequestSystemCrafterJobDataStore(id, queue, tasks);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_REQUEST_SYSTEM_CRAFTER_JOB_DATASTORE_ID;
        }
    }
}
