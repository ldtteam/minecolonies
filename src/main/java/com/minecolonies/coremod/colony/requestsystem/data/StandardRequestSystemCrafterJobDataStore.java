package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemCrafterJobDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
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
    private       IToken<?>             id;

    /**
     * The queue of the store.
     */
    private final LinkedList<IToken<?>> queue;
    private final List<IToken<?>>             tasks;

    /**
     * Constructor to create the data store.
     * @param id the id of it.
     * @param queue the queue to start with.
     * @param tasks
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
        public NBTTagCompound serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestSystemCrafterJobDataStore standardRequestSystemCrafterJobDataStore)
        {
            final NBTTagCompound compound = new NBTTagCompound();

            compound.setTag(TAG_TOKEN, controller.serialize(standardRequestSystemCrafterJobDataStore.id));
            compound.setTag(TAG_LIST, standardRequestSystemCrafterJobDataStore.queue.stream().map(controller::serialize).collect(NBTUtils.toNBTTagList()));

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemCrafterJobDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt) throws Throwable
        {
            final IToken<?> token = controller.deserialize(nbt.getCompoundTag(TAG_TOKEN));
            final LinkedList<IToken<?>> queue = NBTUtils.streamCompound(nbt.getTagList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                                  .map(nbtTagCompound -> (IToken<?>) controller.deserialize(nbtTagCompound))
                                                  .collect(Collectors.toCollection(LinkedList::new));
            final List<IToken<?>> taskList = NBTUtils.streamCompound(nbt.getTagList(TAG_ASSIGNED_LIST, Constants.NBT.TAG_COMPOUND))
              .map(nbtTagCompound -> (IToken<?>) controller.deserialize(nbtTagCompound))
              .collect(Collectors.toList());

            return new StandardRequestSystemCrafterJobDataStore(token, queue, taskList);
        }
    }
}
