package com.minecolonies.core.colony.crafting;
import com.minecolonies.api.util.ItemStackUtils;

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
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
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
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final RecipeStorage recipeStorage)
    {
        final CompoundTag compound = new CompoundTag();
        @NotNull final ListTag inputTagList = new ListTag();
        for (@NotNull final ItemStorage inputItem : recipeStorage.getInput())
        {
            @NotNull final CompoundTag neededRes = StandardFactoryController.getInstance().serializeTag(provider, inputItem);
            inputTagList.add(neededRes);
        }
        compound.put(INPUT_TAG, inputTagList);
        compound.put(NbtTagConstants.STACK, ItemStackUtils.serializeOptional(recipeStorage.getPrimaryOutput(), provider));

        if (recipeStorage.getIntermediate() != null)
        {
            compound.put(BLOCK_TAG, NbtUtils.writeBlockState(recipeStorage.getIntermediate().defaultBlockState()));
        }
        compound.putInt(TAG_GRID, recipeStorage.getGridSize());
        compound.put(TAG_TOKEN, StandardFactoryController.getInstance().serializeTag(provider, recipeStorage.getToken()));
        if(recipeStorage.getRecipeSource() != null)
        {
            compound.putString(SOURCE_TAG, recipeStorage.getRecipeSource().toString());
        }
        compound.putString(TYPE_TAG, recipeStorage.getRecipeType().getId().toString());

        @NotNull final ListTag altOutputTagList = new ListTag();
        for (@NotNull final ItemStack stack : recipeStorage.getAlternateOutputs())
        {
            altOutputTagList.add(ItemStackUtils.serializeOptional(stack, provider));
        }
        compound.put(ALTOUTPUT_TAG, altOutputTagList);

        @NotNull final ListTag secOutputTagList = new ListTag();
        for (@NotNull final ItemStack stack : recipeStorage.getCraftingToolsAndSecondaryOutputs())
        {
            secOutputTagList.add(ItemStackUtils.serializeOptional(stack, provider));
        }
        compound.put(SECOUTPUT_TAG, secOutputTagList);

        if(recipeStorage.getLootTable() != null)
        {
            compound.putString(LOOT_TAG, recipeStorage.getLootTable().identifier().toString());
        }

        compound.putString(TOOL_TAG, recipeStorage.getRequiredTool().getRegistryName().toString());

        return compound;
    }

    @NotNull
    @Override
    public RecipeStorage deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final List<ItemStorage> input = new ArrayList<>();
        final ListTag inputTagList = nbt.getListOrEmpty(INPUT_TAG);
        for (int i = 0; i < inputTagList.size(); ++i)
        {
            final CompoundTag inputTag = inputTagList.getCompoundOrEmpty(i);
            if(inputTag.contains(NEW_NBT_TYPE) || inputTag.contains(NBT_TYPE)) //Check to see if it's something the factorycontroller can handle
            {
                input.add(StandardFactoryController.getInstance().deserializeTag(provider, inputTag));
            }
            else
            {
                final ItemStorage newItem = new ItemStorage(ItemStackUtils.parseOptional(provider, inputTag.getCompoundOrEmpty(NbtTagConstants.STACK)));
                input.add(newItem);
            }
        }

        final ItemStack primaryOutput = ItemStackUtils.parseOptional(provider, nbt.getCompoundOrEmpty(NbtTagConstants.STACK));

        final Block intermediate = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, nbt.getCompoundOrEmpty(BLOCK_TAG)).getBlock();

        final int gridSize = nbt.getIntOr(TAG_GRID, 0);
        final IToken<?> token = StandardFactoryController.getInstance().deserializeTag(provider, nbt.getCompoundOrEmpty(TAG_TOKEN));

        final Identifier source = nbt.contains(SOURCE_TAG) ? Identifier.parse(nbt.getStringOr(SOURCE_TAG, "")) : null;

        final Identifier type = nbt.contains(TYPE_TAG) ? Identifier.parse(nbt.getStringOr(TYPE_TAG, "").toLowerCase()): ModRecipeTypes.CLASSIC_ID;

        final ListTag altOutputTagList = nbt.getListOrEmpty(ALTOUTPUT_TAG);

        final List<ItemStack> altOutputs = new ArrayList<>();
        for (int i = 0; i < altOutputTagList.size(); ++i)
        {
            final CompoundTag altOutputTag = altOutputTagList.getCompoundOrEmpty(i);
            altOutputs.add(ItemStackUtils.parseOptional(provider, altOutputTag));
        }

        final ListTag secOutputTagList = nbt.getListOrEmpty(SECOUTPUT_TAG);

        final List<ItemStack> secOutputs = new ArrayList<>();
        for (int i = 0; i < secOutputTagList.size(); ++i)
        {
            final CompoundTag secOutputTag = secOutputTagList.getCompoundOrEmpty(i);
            secOutputs.add(ItemStackUtils.parseOptional(provider, secOutputTag));
        }

        final ResourceKey<LootTable> lootTable = nbt.contains(LOOT_TAG) ? ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(nbt.getStringOr(LOOT_TAG, ""))) : null;
        final EquipmentTypeEntry requiredTool = ModEquipmentTypes.getRegistry().getValue(EquipmentTypeEntry.parseIdentifier(nbt.getStringOr(TOOL_TAG, "")));

        return RecipeStorage.builder()
                .withToken(token)
                .withInputs(input)
                .withGridSize(gridSize)
                .withPrimaryOutput(primaryOutput)
                .withIntermediate(intermediate)
                .withRecipeId(source)
                .withRecipeType(type)
                .withAlternateOutputs(altOutputs)
                .withSecondaryOutputs(secOutputs)
                .withLootTable(lootTable)
                .withRequiredTool(requiredTool)
                .build();
    }

    @Override
    public void serialize(@NotNull final IFactoryController controller, final RecipeStorage input, final RegistryFriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeVarInt(input.getInput().size());
        input.getInput().forEach(stack -> StandardFactoryController.getInstance().serialize(packetBuffer, stack));
        Utils.serializeCodecMess(packetBuffer, input.getPrimaryOutput());

        packetBuffer.writeBoolean(input.getIntermediate() != null);
        if (input.getIntermediate() != null)
        {
            packetBuffer.writeVarInt(Block.getId(input.getIntermediate().defaultBlockState()));
        }

        packetBuffer.writeVarInt(input.getGridSize());

        packetBuffer.writeIdentifier(input.getRecipeType().getId());

        packetBuffer.writeVarInt(input.getAlternateOutputs().size());
        input.getAlternateOutputs().forEach(stack -> Utils.serializeCodecMess(packetBuffer, stack));

        packetBuffer.writeVarInt(input.getCraftingToolsAndSecondaryOutputs().size());
        input.getCraftingToolsAndSecondaryOutputs().forEach(stack -> Utils.serializeCodecMess(packetBuffer, stack));

        packetBuffer.writeIdentifier(input.getRequiredTool().getRegistryName());

        packetBuffer.writeBoolean(input.getLootTable() != null);
        if(input.getLootTable() != null)
        {
            packetBuffer.writeResourceKey(input.getLootTable());
        }

        packetBuffer.writeBoolean(input.getRecipeSource() != null);
        if (input.getRecipeSource() != null)
        {
            packetBuffer.writeIdentifier(input.getRecipeSource());
        }

        controller.serialize(packetBuffer, input.getToken());
    }

    @NotNull
    @Override
    public RecipeStorage deserialize(@NotNull final IFactoryController controller, final RegistryFriendlyByteBuf buffer) throws Throwable
    {
        final List<ItemStorage> input = new ArrayList<>();
        final int inputSize = buffer.readVarInt();
        for (int i = 0; i < inputSize; ++i)
        {
            input.add(StandardFactoryController.getInstance().deserialize(buffer));
        }

        final ItemStack primaryOutput = Utils.deserializeCodecMess(buffer);
        final Block intermediate = buffer.readBoolean() ? Block.stateById(buffer.readVarInt()).getBlock() : Blocks.AIR;
        final int gridSize = buffer.readVarInt();
        final Identifier type = buffer.readIdentifier();

        final List<ItemStack> altOutputs = new ArrayList<>();
        final int altOutputSize = buffer.readVarInt();
        for (int i = 0; i < altOutputSize; ++i)
        {
            altOutputs.add(Utils.deserializeCodecMess(buffer));
        }

        final List<ItemStack> secOutputs = new ArrayList<>();
        final int secOutputSize = buffer.readVarInt();
        for (int i = 0; i < secOutputSize; ++i)
        {
            secOutputs.add(Utils.deserializeCodecMess(buffer));
        }

        final Identifier resLoc = EquipmentTypeEntry.parseIdentifier(buffer.readIdentifier());
        final EquipmentTypeEntry requiredTool = ModEquipmentTypes.getRegistry().getValue(resLoc);

        ResourceKey<LootTable> lootTable = null;
        if(buffer.readBoolean())
        {
            lootTable = buffer.readResourceKey(Registries.LOOT_TABLE);
        }

        Identifier source = null;
        if(buffer.readBoolean())
        {
            source = buffer.readIdentifier();
        }

        final IToken<?> token = controller.deserialize(buffer);
        return RecipeStorage.builder()
                .withToken(token)
                .withInputs(input)
                .withGridSize(gridSize)
                .withPrimaryOutput(primaryOutput)
                .withIntermediate(intermediate)
                .withRecipeId(source)
                .withRecipeType(type)
                .withAlternateOutputs(altOutputs)
                .withSecondaryOutputs(secOutputs)
                .withLootTable(lootTable)
                .withRequiredTool(requiredTool)
                .build();
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.RECIPE_STORAGE_ID;
    }
}
