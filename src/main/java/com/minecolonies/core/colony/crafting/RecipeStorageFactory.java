package com.minecolonies.core.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorageFactory;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModRecipeTypes;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.colony.requestsystem.StandardFactoryController.NBT_TYPE;
import static com.minecolonies.api.colony.requestsystem.StandardFactoryController.NEW_NBT_TYPE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TOKEN;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing RecipeStorages.
 */
public class RecipeStorageFactory implements IRecipeStorageFactory
{
    /**
     * Compound tag for the grid size.
     */
    private static final String TAG_GRID = "grid";

    /**
     * Tag to store the blockstate.
     */
    private static final String BLOCK_TAG = "block";

    /**
     * Compound tag for the input.
     */
    private static final String INPUT_TAG = "input";

    /**
     * Compound tag for the alternate outputs.
     */
    private static final String ALTOUTPUT_TAG = "alternate-output";

    /**
     * Compound tag for the alternate outputs.
     */
    private static final String SECOUTPUT_TAG = "secondary-output";

    /**
     * Compound tag for Source
     */
    private static final String SOURCE_TAG = "source";

    /**
     * Compound tag for Type
     */
    private static final String TYPE_TAG = "type";

    /**
     * Compound tag for Loot Table
     */
    private static final String LOOT_TAG = "loot-table";

    /**
     * Compound tag for Tool
     */
    private static final String TOOL_TAG = "tool";

    @NotNull
    @Override
    public TypeToken<RecipeStorage> getFactoryOutputType()
    {
        return TypeConstants.RECIPE;
    }

    @NotNull
    @Override
    public TypeToken<? extends IToken<?>> getFactoryInputType()
    {
        return TypeConstants.ITOKEN;
    }

    @NotNull
    @Override
    public RecipeStorage getNewInstance(
      @NotNull final IToken<?> token,
      @NotNull final List<ItemStorage> input,
      final int gridSize,
      @NotNull final ItemStack primaryOutput,
      final Block intermediate,
      final ResourceLocation source,
      final ResourceLocation type,
      final List<ItemStack> altOutputs,
      final List<ItemStack> secOutputs,
      final ResourceLocation lootTable,
      @NotNull final EquipmentTypeEntry requiredTool)
    {
        return new RecipeStorage(token, input, gridSize, primaryOutput, intermediate, source, type, altOutputs, secOutputs, lootTable, requiredTool);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final RecipeStorage recipeStorage)
    {
        final CompoundTag compound = new CompoundTag();
        @NotNull final ListTag inputTagList = new ListTag();
        for (@NotNull final ItemStorage inputItem : recipeStorage.getInput())
        {
            @NotNull final CompoundTag neededRes = StandardFactoryController.getInstance().serialize(inputItem);
            inputTagList.add(neededRes);
        }
        compound.put(INPUT_TAG, inputTagList);
        recipeStorage.getPrimaryOutput().save(compound);

        if (recipeStorage.getIntermediate() != null)
        {
            compound.put(BLOCK_TAG, NbtUtils.writeBlockState(recipeStorage.getIntermediate().defaultBlockState()));
        }
        compound.putInt(TAG_GRID, recipeStorage.getGridSize());
        compound.put(TAG_TOKEN, StandardFactoryController.getInstance().serialize(recipeStorage.getToken()));
        if(recipeStorage.getRecipeSource() != null)
        {
            compound.putString(SOURCE_TAG, recipeStorage.getRecipeSource().toString());
        }
        compound.putString(TYPE_TAG, recipeStorage.getRecipeType().getId().toString());

        @NotNull final ListTag altOutputTagList = new ListTag();
        for (@NotNull final ItemStack stack : recipeStorage.getAlternateOutputs())
        {
            @NotNull final CompoundTag neededRes = new CompoundTag();
            stack.save(neededRes);
            altOutputTagList.add(neededRes);
        }
        compound.put(ALTOUTPUT_TAG, altOutputTagList);

        @NotNull final ListTag secOutputTagList = new ListTag();
        for (@NotNull final ItemStack stack : recipeStorage.getCraftingToolsAndSecondaryOutputs())
        {
            @NotNull final CompoundTag neededRes = new CompoundTag();
            stack.save(neededRes);
            secOutputTagList.add(neededRes);
        }
        compound.put(SECOUTPUT_TAG, secOutputTagList);

        if(recipeStorage.getLootTable() != null)
        {
            compound.putString(LOOT_TAG, recipeStorage.getLootTable().toString());
        }

        compound.putString(TOOL_TAG, recipeStorage.getRequiredTool().getRegistryName().toString());

        return compound;
    }

    @NotNull
    @Override
    public RecipeStorage deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final List<ItemStorage> input = new ArrayList<>();
        final ListTag inputTagList = nbt.getList(INPUT_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < inputTagList.size(); ++i)
        {
            final CompoundTag inputTag = inputTagList.getCompound(i);
            if(inputTag.contains(NEW_NBT_TYPE) || inputTag.contains(NBT_TYPE)) //Check to see if it's something the factorycontroller can handle
            {
                input.add(StandardFactoryController.getInstance().deserialize(inputTag));
            }
            else
            {
                final ItemStorage newItem = new ItemStorage(ItemStack.of(inputTag));
                input.add(newItem);
            }
        }

        final ItemStack primaryOutput = ItemStack.of(nbt);

        final Block intermediate = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound(BLOCK_TAG)).getBlock();

        final int gridSize = nbt.getInt(TAG_GRID);
        final IToken<?> token = StandardFactoryController.getInstance().deserialize(nbt.getCompound(TAG_TOKEN));

        final ResourceLocation source = nbt.contains(SOURCE_TAG) ? new ResourceLocation(nbt.getString(SOURCE_TAG)) : null; 

        final ResourceLocation type = nbt.contains(TYPE_TAG) ? new ResourceLocation(nbt.getString(TYPE_TAG).toLowerCase()): ModRecipeTypes.CLASSIC_ID;

        final ListTag altOutputTagList = nbt.getList(ALTOUTPUT_TAG, Tag.TAG_COMPOUND);

        final List<ItemStack> altOutputs = new ArrayList<>();
        for (int i = 0; i < altOutputTagList.size(); ++i)
        {
            final CompoundTag altOutputTag = altOutputTagList.getCompound(i);
            altOutputs.add(ItemStack.of(altOutputTag));
        }

        final ListTag secOutputTagList = nbt.getList(SECOUTPUT_TAG, Tag.TAG_COMPOUND);

        final List<ItemStack> secOutputs = new ArrayList<>();
        for (int i = 0; i < secOutputTagList.size(); ++i)
        {
            final CompoundTag secOutputTag = secOutputTagList.getCompound(i);
            secOutputs.add(ItemStack.of(secOutputTag));
        }

        final ResourceLocation lootTable = nbt.contains(LOOT_TAG) ? new ResourceLocation(nbt.getString(LOOT_TAG)) : null; 

        final String resLoc = nbt.getString(TOOL_TAG);
        final EquipmentTypeEntry requiredTool = nbt.contains(TOOL_TAG) ? ModEquipmentTypes.getRegistry().getValue(EquipmentTypeEntry.parseResourceLocation(resLoc)) : ModEquipmentTypes.none.get();
        return this.getNewInstance(token, input, gridSize, primaryOutput, intermediate, source, type, altOutputs.isEmpty() ? null : altOutputs, secOutputs.isEmpty() ? null : secOutputs, lootTable, requiredTool);
    }

    @Override
    public void serialize(@NotNull final IFactoryController controller, final RecipeStorage input, final FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeVarInt(input.getInput().size());
        input.getInput().forEach(stack -> StandardFactoryController.getInstance().serialize(packetBuffer, stack));
        packetBuffer.writeItem(input.getPrimaryOutput());

        packetBuffer.writeBoolean(input.getIntermediate() != null);
        if (input.getIntermediate() != null)
        {
            packetBuffer.writeVarInt(Block.getId(input.getIntermediate().defaultBlockState()));
        }

        packetBuffer.writeVarInt(input.getGridSize());

        packetBuffer.writeResourceLocation(input.getRecipeType().getId());

        packetBuffer.writeVarInt(input.getAlternateOutputs().size());
        input.getAlternateOutputs().forEach(stack -> packetBuffer.writeItem(stack));

        packetBuffer.writeVarInt(input.getCraftingToolsAndSecondaryOutputs().size());
        input.getCraftingToolsAndSecondaryOutputs().forEach(stack -> packetBuffer.writeItem(stack));

        packetBuffer.writeResourceLocation(input.getRequiredTool().getRegistryName());

        packetBuffer.writeBoolean(input.getLootTable() != null);
        if(input.getLootTable() != null)
        {
            packetBuffer.writeResourceLocation(input.getLootTable());
        }

        packetBuffer.writeBoolean(input.getRecipeSource() != null);
        if (input.getRecipeSource() != null)
        {
            packetBuffer.writeResourceLocation(input.getRecipeSource());
        }

        controller.serialize(packetBuffer, input.getToken());
    }

    @NotNull
    @Override
    public RecipeStorage deserialize(@NotNull final IFactoryController controller, final FriendlyByteBuf buffer) throws Throwable
    {
        final List<ItemStorage> input = new ArrayList<>();
        final int inputSize = buffer.readVarInt();
        for (int i = 0; i < inputSize; ++i)
        {
            input.add(StandardFactoryController.getInstance().deserialize(buffer));
        }

        final ItemStack primaryOutput = buffer.readItem();
        final Block intermediate = buffer.readBoolean() ? Block.stateById(buffer.readVarInt()).getBlock() : Blocks.AIR;
        final int gridSize = buffer.readVarInt();
        final ResourceLocation type = buffer.readResourceLocation();

        final List<ItemStack> altOutputs = new ArrayList<>();
        final int altOutputSize = buffer.readVarInt();
        for (int i = 0; i < altOutputSize; ++i)
        {
            altOutputs.add(buffer.readItem());
        }

        final List<ItemStack> secOutputs = new ArrayList<>();
        final int secOutputSize = buffer.readVarInt();
        for (int i = 0; i < secOutputSize; ++i)
        {
            secOutputs.add(buffer.readItem());
        }

        final ResourceLocation resLoc = EquipmentTypeEntry.parseResourceLocation(buffer.readResourceLocation());
        final EquipmentTypeEntry requiredTool = ModEquipmentTypes.getRegistry().getValue(resLoc);

        ResourceLocation lootTable = null;
        if(buffer.readBoolean())
        {
            lootTable = buffer.readResourceLocation();
        }

        ResourceLocation source = null;
        if(buffer.readBoolean())
        {
            source = buffer.readResourceLocation();
        }

        final IToken<?> token = controller.deserialize(buffer);
        return this.getNewInstance(token, input, gridSize, primaryOutput, intermediate, source, type, altOutputs.isEmpty() ? null : altOutputs, secOutputs.isEmpty() ? null : secOutputs, lootTable, requiredTool);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.RECIPE_STORAGE_ID;
    }
}
