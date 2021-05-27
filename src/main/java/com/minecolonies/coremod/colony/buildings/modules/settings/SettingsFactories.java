package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.modules.settings.*;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing Settings.
 */
public class SettingsFactories
{
    /**
     * Specific factory for the bool setting.
     */
    public static class BoolSettingFactory implements IBoolSettingFactory<BoolSetting>
    {
        /**
         * Compound tag for the value.
         */
        private static final String TAG_VALUE = "value";

        /**
         * Compound tag for the default.
         */
        private static final String TAG_DEFAULT = "default";

        @NotNull
        @Override
        public TypeToken<BoolSetting> getFactoryOutputType()
        {
            return TypeToken.of(BoolSetting.class);
        }

        @NotNull
        @Override
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public BoolSetting getNewInstance(final boolean value, final boolean def)
        {
            return new BoolSetting(value, def);
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final BoolSetting storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putBoolean(TAG_VALUE, storage.getValue());
            compound.putBoolean(TAG_DEFAULT, storage.getDefault());
            return compound;
        }

        @NotNull
        @Override
        public BoolSetting deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return this.getNewInstance(nbt.getBoolean(TAG_VALUE), nbt.getBoolean(TAG_DEFAULT));
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final BoolSetting input, @NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeBoolean(input.getValue());
            packetBuffer.writeBoolean(input.getDefault());
        }

        @NotNull
        @Override
        public BoolSetting deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            return this.getNewInstance(buffer.readBoolean(), buffer.readBoolean());
        }

        @Override
        public short getSerializationId()
        {
            return 43;
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static abstract class AbstractStringSettingsFactory<T extends IStringSetting> implements IStringSettingFactory<T>
    {
        /**
         * Compound tag for the value.
         */
        private static final String TAG_VALUE = "value";

        /**
         * Compound tag for the default.
         */
        private static final String TAG_LIST = "settings";

        @NotNull
        @Override
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final IStringSetting storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putInt(TAG_VALUE, storage.getCurrentIndex());

            final ListNBT list = new ListNBT();
            for (final String setting: storage.getSettings())
            {
                final CompoundNBT compoundNBT = new CompoundNBT();
                compoundNBT.putString(TAG_VALUE, setting);
                list.add(compoundNBT);
            }
            compound.put(TAG_LIST, list);
            return compound;
        }

        @NotNull
        @Override
        public T deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            final int current = nbt.getInt(TAG_VALUE);
            final List<String> settings = new ArrayList<>();
            final ListNBT list = nbt.getList(TAG_LIST, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++)
            {
                settings.add(list.getCompound(i).getString(TAG_VALUE));
            }

            return this.getNewInstance(settings, current);
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final IStringSetting input, @NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeInt(input.getCurrentIndex());
            packetBuffer.writeInt(input.getSettings().size());
            for (final String setting: input.getSettings())
            {
                packetBuffer.writeString(setting);
            }
        }

        @NotNull
        @Override
        public T deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            final int currentIndex = buffer.readInt();
            final int size = buffer.readInt();
            final List<String> settings = new ArrayList<>();
            for (int i = 0; i < size; i++)
            {
                settings.add(buffer.readString(32767));
            }
            return this.getNewInstance(settings, currentIndex);
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static class StringSettingsFactory extends AbstractStringSettingsFactory<StringSetting>
    {
        @NotNull
        @Override
        public TypeToken<StringSetting> getFactoryOutputType()
        {
            return TypeToken.of(StringSetting.class);
        }

        @NotNull
        @Override
        public StringSetting getNewInstance(final List<String> value, final int curr)
        {
            return new StringSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return 46;
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static class BlockSettingFactory implements IBlockSettingFactory<BlockSetting>
    {
        /**
         * Compound tag for the value.
         */
        private static final String TAG_VALUE = "value";

        /**
         * Compound tag for the default.
         */
        private static final String TAG_DEF = "default";

        @NotNull
        @Override
        public TypeToken<BlockSetting> getFactoryOutputType()
        {
            return TypeToken.of(BlockSetting.class);
        }

        @NotNull
        @Override
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public BlockSetting getNewInstance(final BlockItem value, final BlockItem def)
        {
            return new BlockSetting(value, def);
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final BlockSetting storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putString(TAG_VALUE, storage.getValue().getRegistryName().toString());
            compound.putString(TAG_DEF, storage.getDefault().getRegistryName().toString());
            return compound;
        }

        @NotNull
        @Override
        public BlockSetting deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            final BlockItem value = (BlockItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString(TAG_VALUE)));
            final BlockItem def = (BlockItem) ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString(TAG_DEF)));
            return this.getNewInstance(value, def);
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final BlockSetting input, @NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeItemStack(new ItemStack(input.getValue()));
            packetBuffer.writeItemStack(new ItemStack(input.getDefault()));
        }

        @NotNull
        @Override
        public BlockSetting deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            final BlockItem value = (BlockItem) buffer.readItemStack().getItem();
            final BlockItem def = (BlockItem) buffer.readItemStack().getItem();
            return this.getNewInstance(value, def);
        }

        @Override
        public short getSerializationId()
        {
            return 47;
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static class IntSettingFactory implements IIntSettingFactory<IntSetting>
    {
        /**
         * Compound tag for the value.
         */
        private static final String TAG_VALUE = "value";

        /**
         * Compound tag for the default.
         */
        private static final String TAG_DEFAULT = "default";

        @NotNull
        @Override
        public TypeToken<IntSetting> getFactoryOutputType()
        {
            return TypeToken.of(IntSetting.class);
        }

        @NotNull
        @Override
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public IntSetting getNewInstance(final int value, final int def)
        {
            return new IntSetting(value, def);
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final IntSetting storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putInt(TAG_VALUE, storage.getValue());
            compound.putInt(TAG_DEFAULT, storage.getDefault());
            return compound;
        }

        @NotNull
        @Override
        public IntSetting deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return this.getNewInstance(nbt.getInt(TAG_VALUE), nbt.getInt(TAG_DEFAULT));
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final IntSetting input, @NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeInt(input.getValue());
            packetBuffer.writeInt(input.getDefault());
        }

        @NotNull
        @Override
        public IntSetting deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            return this.getNewInstance(buffer.readInt(), buffer.readInt());
        }

        @Override
        public short getSerializationId()
        {
            return 48;
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static class PlantationSettingsFactory extends AbstractStringSettingsFactory<PlantationSetting>
    {
        @NotNull
        @Override
        public TypeToken<PlantationSetting> getFactoryOutputType()
        {
            return TypeToken.of(PlantationSetting.class);
        }

        @NotNull
        @Override
        public PlantationSetting getNewInstance(final List<String> value, final int curr)
        {
            return new PlantationSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return 49;
        }
    }
}
