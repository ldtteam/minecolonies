package com.minecolonies.coremod.colony.requestsystem.data;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemDeliveryManJobDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

public class StandardRequestSystemDeliveryManJobDataStore implements IRequestSystemDeliveryManJobDataStore
{

    private IToken<?> id;
    private final LinkedList<IToken<?>> queue;
    private boolean returning;

    public StandardRequestSystemDeliveryManJobDataStore(final IToken<?> id, final LinkedList<IToken<?>> queue, final boolean returning) {
        this.id = id;
        this.queue = queue;
        this.returning = returning;
    }

    public StandardRequestSystemDeliveryManJobDataStore()
    {
        this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
          new LinkedList<>(),
      false );
    }

    @Override
    public LinkedList<IToken<?>> getQueue()
    {
        return queue;
    }

    @Override
    public boolean isReturning()
    {
        return returning;
    }

    @Override
    public void setReturning(final boolean returning)
    {
        this.returning = returning;
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
        public CompoundNBT serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestSystemDeliveryManJobDataStore standardRequestSystemDeliveryManJobDataStore)
        {
            final CompoundNBT compound = new CompoundNBT();

            compound.put(TAG_TOKEN, controller.serialize(standardRequestSystemDeliveryManJobDataStore.id));
            compound.put(TAG_LIST, standardRequestSystemDeliveryManJobDataStore.queue.stream().map(controller::serialize).collect(NBTUtils.toListNBT()));
            compound.putBoolean(TAG_VALUE, standardRequestSystemDeliveryManJobDataStore.returning);

            return compound;
        }

        @NotNull
        @Override
        public StandardRequestSystemDeliveryManJobDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt) throws Throwable
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final LinkedList<IToken<?>> queue = NBTUtils.streamCompound(nbt.getTagList(TAG_LIST, Constants.NBT.TAG_COMPOUND))
                                          .map(CompoundNBT -> (IToken<?>) controller.deserialize(CompoundNBT))
                                          .collect(Collectors.toCollection(LinkedList<IToken<?>>::new));
            final boolean returning = nbt.getBoolean(TAG_VALUE);

            return new StandardRequestSystemDeliveryManJobDataStore(token, queue, returning);
        }
    }
}
