package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.buildings.modules.settings.*;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
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
    public abstract static class AbstractBoolSettingFactory<T extends BoolSetting> implements IBoolSettingFactory<T>
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
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public T deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return this.getNewInstance(nbt.getBoolean(TAG_VALUE), nbt.getBoolean(TAG_DEFAULT));
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final T storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.putBoolean(TAG_VALUE, storage.getValue());
            compound.putBoolean(TAG_DEFAULT, storage.getDefault());
            return compound;
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final T input, @NotNull final PacketBuffer packetBuffer)
        {
            packetBuffer.writeBoolean(input.getValue());
            packetBuffer.writeBoolean(input.getDefault());
        }

        @NotNull
        @Override
        public T deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            return this.getNewInstance(buffer.readBoolean(), buffer.readBoolean());
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static class BoolSettingFactory extends AbstractBoolSettingFactory<BoolSetting>
    {
        @NotNull
        @Override
        public TypeToken<BoolSetting> getFactoryOutputType()
        {
            return TypeToken.of(BoolSetting.class);
        }

        @NotNull
        @Override
        public BoolSetting getNewInstance(final boolean value, final boolean def)
        {
            return new BoolSetting(value, def);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.BOOLEAN_SETTINGS_ID;
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
                packetBuffer.writeUtf(setting);
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
                settings.add(buffer.readUtf(32767));
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
            return SerializationIdentifierConstants.STRING_SETTINGS_ID;
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
            packetBuffer.writeItem(new ItemStack(input.getValue()));
            packetBuffer.writeItem(new ItemStack(input.getDefault()));
        }

        @NotNull
        @Override
        public BlockSetting deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            final BlockItem value = (BlockItem) buffer.readItem().getItem();
            final BlockItem def = (BlockItem) buffer.readItem().getItem();
            return this.getNewInstance(value, def);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.BLOCK_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the bool setting.
     */
    public static abstract class AbstractIntSettingFactory<T extends IntSetting> implements IIntSettingFactory<T>
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
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
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
        public T deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
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
        public T deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            return this.getNewInstance(buffer.readInt(), buffer.readInt());
        }
    }

    public static class IntSettingFactory extends AbstractIntSettingFactory<IntSetting>
    {
        @NotNull
        @Override
        public TypeToken<IntSetting> getFactoryOutputType()
        {
            return TypeToken.of(IntSetting.class);
        }

        @NotNull
        @Override
        public IntSetting getNewInstance(final int value, final int def)
        {
            return new IntSetting(value, def);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.INTEGER_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the plantation setting.
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
            return SerializationIdentifierConstants.PLANTATION_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the string setting with desc.
     */
    public static class StringWithDescSettingsFactory extends AbstractStringSettingsFactory<StringSettingWithDesc>
    {
        @NotNull
        @Override
        public TypeToken<StringSettingWithDesc> getFactoryOutputType()
        {
            return TypeToken.of(StringSettingWithDesc.class);
        }

        @NotNull
        @Override
        public StringSettingWithDesc getNewInstance(final List<String> value, final int curr)
        {
            return new StringSettingWithDesc(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STRING_W_DESC_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the string setting with desc.
     */
    public static class PatrolModeSettingFactory extends AbstractStringSettingsFactory<PatrolModeSetting>
    {
        @NotNull
        @Override
        public TypeToken<PatrolModeSetting> getFactoryOutputType()
        {
            return TypeToken.of(PatrolModeSetting.class);
        }

        @NotNull
        @Override
        public PatrolModeSetting getNewInstance(final List<String> value, final int curr)
        {
            return new PatrolModeSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.PATROL_MODE_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the string setting with desc.
     */
    public static class GuardTaskSettingFactory extends AbstractStringSettingsFactory<GuardTaskSetting>
    {
        @NotNull
        @Override
        public TypeToken<GuardTaskSetting> getFactoryOutputType()
        {
            return TypeToken.of(GuardTaskSetting.class);
        }

        @NotNull
        @Override
        public GuardTaskSetting getNewInstance(final List<String> value, final int curr)
        {
            return new GuardTaskSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.GUARD_TASK_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the string setting with desc.
     */
    public static class FollowModeSettingFactory extends AbstractStringSettingsFactory<FollowModeSetting>
    {
        @NotNull
        @Override
        public TypeToken<FollowModeSetting> getFactoryOutputType()
        {
            return TypeToken.of(FollowModeSetting.class);
        }

        @NotNull
        @Override
        public FollowModeSetting getNewInstance(final List<String> value, final int curr)
        {
            return new FollowModeSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.FOLLOW_MODE_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the string setting with desc.
     */
    public static class CrafterRecipeSettingFactory extends AbstractStringSettingsFactory<CrafterRecipeSetting>
    {
        @NotNull
        @Override
        public TypeToken<CrafterRecipeSetting> getFactoryOutputType()
        {
            return TypeToken.of(CrafterRecipeSetting.class);
        }

        @NotNull
        @Override
        public CrafterRecipeSetting getNewInstance(final List<String> value, final int curr)
        {
            return new CrafterRecipeSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.CRAFTER_RECIPE_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the string setting with desc.
     */
    public static class BuilderModeSettingFactory extends AbstractStringSettingsFactory<BuilderModeSetting>
    {
        @NotNull
        @Override
        public TypeToken<BuilderModeSetting> getFactoryOutputType()
        {
            return TypeToken.of(BuilderModeSetting.class);
        }

        @NotNull
        @Override
        public BuilderModeSetting getNewInstance(final List<String> value, final int curr)
        {
            return new BuilderModeSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.BUILDER_MODE_SETTINGS_ID;
        }
    }

    public static class DynamicTreesSettingFactory extends AbstractIntSettingFactory<DynamicTreesSetting>
    {

        @NotNull
        @Override
        public DynamicTreesSetting getNewInstance(int def, int current)
        {
            return new DynamicTreesSetting(def, current);
        }

        @NotNull
        @Override
        public TypeToken<DynamicTreesSetting> getFactoryOutputType()
        {
            return TypeToken.of(DynamicTreesSetting.class);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.DYNAMIC_TREES_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the beekeeper collection setting.
     */
    public static class BeekeeperCollectionSettingsFactory extends AbstractStringSettingsFactory<BeekeeperCollectionSetting>
    {
        @NotNull
        @Override
        public TypeToken<BeekeeperCollectionSetting> getFactoryOutputType()
        {
            return TypeToken.of(BeekeeperCollectionSetting.class);
        }

        @NotNull
        @Override
        public BeekeeperCollectionSetting getNewInstance(final List<String> value, final int curr)
        {
            return new BeekeeperCollectionSetting(value, curr);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.BEEKEEPER_COLLECTION_SETTINGS_ID;
        }
    }

    /**
     * Specific factory for the recipe setting.
     */
    public static class RecipeSettingFactory implements IRecipeSettingFactory<RecipeSetting>
    {
        /**
         * Compound tag for the module value..
         */
        private static final String TAG_MODULE = "value";

        /**
         * Compound tag for the token.
         */
        private static final String TAG_TOKEN = "token";

        @NotNull
        @Override
        public TypeToken<RecipeSetting> getFactoryOutputType()
        {
            return TypeToken.of(RecipeSetting.class);
        }

        @NotNull
        @Override
        public TypeToken<FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public RecipeSetting getNewInstance(final IToken<?> index, final String craftingModuleId)
        {
            return new RecipeSetting(index, craftingModuleId);
        }

        @NotNull
        @Override
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final RecipeSetting storage)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.put(TAG_TOKEN, StandardFactoryController.getInstance().serialize(storage.currentIndex));
            compound.putString(TAG_MODULE, storage.craftingModuleId);
            return compound;
        }

        @NotNull
        @Override
        public RecipeSetting deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            final IToken<?> token = StandardFactoryController.getInstance().deserialize(nbt.getCompound(TAG_TOKEN));
            final String moduleId = nbt.getString(TAG_MODULE);
            return this.getNewInstance(token, moduleId);
        }

        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final RecipeSetting input, @NotNull final PacketBuffer packetBuffer)
        {
            StandardFactoryController.getInstance().serialize(packetBuffer, input.currentIndex);
            packetBuffer.writeUtf(input.craftingModuleId);
        }

        @NotNull
        @Override
        public RecipeSetting deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable
        {
            final IToken<?> token = StandardFactoryController.getInstance().deserialize(buffer);
            final String moduleId = buffer.readUtf(32767);
            return this.getNewInstance(token, moduleId);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.CRAFTING_SETTINGS_ID;
        }
    }
}
