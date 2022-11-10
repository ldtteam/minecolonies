package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing Crafter Recipes.
 */
public class CustomRecipeFactory implements IFactory<FactoryVoidInput, CustomRecipe>
{
    private final static String CUSTOM_RECIPE_ID_PROP = "id";

    private static boolean checkContext(final Object[] context, final Class<?>... types)
    {
        if (context.length != types.length) return false;
        for (int index = 0; index < context.length; ++index)
        {
            if (!types[index].isInstance(context[index])) return false;
        }
        return true;
    }

    @NotNull
    @Override
    public CustomRecipe getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, final Object... context)
      throws IllegalArgumentException
    {
        if (context.length != 15)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 15 are allowed!");
        }
        if (!(context[0] instanceof String))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid Crafter Recipe crafter");
        }
        if (!(context[5] instanceof ResourceLocation) ||
              !(context[6] instanceof ResourceLocation) ||
              !(context[7] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid ResourceLocation");
        }
        if(!(context[8] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid ResourceLocation");
        }
        if(!((context[10]) instanceof List) ||
             !((context[12]) instanceof List) ||
             !((context[13]) instanceof List))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid Item Information");
        }

        final String crafter = (String)context[0];
        final int minBldgLevel = (int)context[1];
        final int maxBldgLevel = (int)context[2];
        final boolean mustExist = (boolean)context[3];
        final boolean showTooltip = (boolean)context[4];
        final ResourceLocation recipeId = (ResourceLocation)context[5];
        final ResourceLocation researchReq = (ResourceLocation)context[6];
        final ResourceLocation researchExclude = (ResourceLocation)context[7];
        final ResourceLocation lootTable = (ResourceLocation)context[8];
        final IToolType requiredTool = (IToolType)context[9];
        final List<ItemStorage> inputs = (List<ItemStorage>)context[10];
        final ItemStack primaryOutput = (ItemStack)context[11];
        final List<ItemStack> secondaryOutput = (List<ItemStack>)context[12];
        final List<ItemStack> altOutputs = (List<ItemStack>)context[13];
        final Block intermediate = (Block)context[14];

        return getNewInstance(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude,
          lootTable, requiredTool, inputs, primaryOutput, secondaryOutput, altOutputs, intermediate);
    }

    private CustomRecipe getNewInstance(final String crafter, final int minBldgLevel, final int maxBldgLevel, final boolean mustExist, final boolean showTooltip, final ResourceLocation recipeId,
                                        final ResourceLocation researchReq, final ResourceLocation researchExclude, final ResourceLocation lootTable, final IToolType toolType, final List<ItemStorage> inputs,
                                        final ItemStack primaryOutput, final List<ItemStack> secondaryOutput, final List<ItemStack> altOutputs, final Block intermediate)
    {
        return new CustomRecipe(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, lootTable, toolType, inputs, primaryOutput, secondaryOutput, altOutputs, intermediate);
    }

    @NotNull
    @Override
    public TypeToken<CustomRecipe> getFactoryOutputType()
    {
        return TypeToken.of(CustomRecipe.class);
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final CustomRecipe recipe)
    {
        // CustomRecipes involve a large number of ItemStacks, and a number of data types that are inefficient in NBT form.  Individually, this isn't that big of a deal (~1.2KB vs 120bytes).
        // However, for large sets, such as CustomRecipeManager, this can grow into a very large total (ie, default Minecolonies + Structurize recipes alone can total >600KB).
        // If transmitting a large number of recipes at once across a network, favor the byte-based serializer instead.
        // This serialization also populates the RecipeStorage inside the Custom Recipe, which remains cached until the next data pack reload.
        // Be aware of the 2MB limit for CompoundTags if assigning a large number of these together into a listNBT.
        final CompoundTag compound = new CompoundTag();
        compound.putString(RECIPE_CRAFTER_PROP, recipe.getCrafter());
        compound.putString(CUSTOM_RECIPE_ID_PROP, recipe.getRecipeStorage().getRecipeSource().toString());
        if(recipe.getRequiredResearchId() != null)
        {
            compound.putString(RECIPE_RESEARCHID_PROP, recipe.getRequiredResearchId().toString());
        }
        if(recipe.getExcludedResearchId() != null)
        {
            compound.putString(RECIPE_EXCLUDED_RESEARCHID_PROP, recipe.getExcludedResearchId().toString());
        }
        if(recipe.getLootTable() != null)
        {
            compound.putString(RECIPE_LOOTTABLE_PROP, recipe.getLootTable().toString());
        }
        if(recipe.getRequiredTool() != ToolType.NONE)
        {
            compound.putString(RECIPE_TOOL_PROP, recipe.getRequiredTool().getName());
        }
        compound.putInt(RECIPE_BUILDING_MIN_LEVEL_PROP, recipe.getMinBuildingLevel());
        compound.putInt(RECIPE_BUILDING_MAX_LEVEL_PROP, recipe.getMaxBuildingLevel());
        compound.putBoolean(RECIPE_MUST_EXIST, recipe.getMustExist());
        compound.putBoolean(RECIPE_SHOW_TOOLTIP, recipe.getShowTooltip());
        final ListTag inputs = new ListTag();
        for(final ItemStorage in : recipe.getInputs())
        {
            inputs.add(controller.serialize(in));
        }
        compound.put(RECIPE_INPUTS_PROP, inputs);

        compound.put(RECIPE_RESULT_PROP, recipe.getPrimaryOutput().save(new CompoundTag()));

        if(recipe.getSecondaryOutput().size() > 0)
        {
            final ListTag secondaryOutputs = new ListTag();
            for (final ItemStack is : recipe.getSecondaryOutput())
            {
                secondaryOutputs.add(is.save(new CompoundTag()));
            }
            compound.put(RECIPE_SECONDARY_PROP, secondaryOutputs);
        }
        if(recipe.getAltOutputs().size() > 0)
        {
            final ListTag altOutputs = new ListTag();
            for (final ItemStack is : recipe.getAltOutputs())
            {
                altOutputs.add(is.save(new CompoundTag()));
            }
            compound.put(RECIPE_ALTERNATE_PROP, altOutputs);
        }

        compound.putString(RECIPE_INTERMEDIATE_PROP, ForgeRegistries.BLOCKS.getKey(recipe.getIntermediate()).toString());

        return compound;
    }

    @NotNull
    @Override
    public CustomRecipe deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final String crafter = nbt.getString(RECIPE_CRAFTER_PROP);
        final ResourceLocation recipeId;
        if(nbt.hasUUID(CUSTOM_RECIPE_ID_PROP))
        {
            recipeId = new ResourceLocation(nbt.getString(CUSTOM_RECIPE_ID_PROP));
        }
        else
        {
            recipeId = null;
        }
        final ResourceLocation researchReq;
        if(nbt.hasUUID(RECIPE_RESEARCHID_PROP))
        {
            researchReq = new ResourceLocation(nbt.getString(RECIPE_RESEARCHID_PROP));
        }
        else
        {
            researchReq = null;
        }
        final ResourceLocation researchExclude;
        if(nbt.hasUUID(RECIPE_EXCLUDED_RESEARCHID_PROP))
        {
             researchExclude = new ResourceLocation(nbt.getString(RECIPE_EXCLUDED_RESEARCHID_PROP));
        }
        else
        {
            researchExclude = null;
        }
        final ResourceLocation lootTable;
        if(nbt.hasUUID(RECIPE_LOOTTABLE_PROP))
        {
            lootTable = new ResourceLocation(nbt.getAsString());
        }
        else
        {
            lootTable = null;
        }
        IToolType requiredTool = ToolType.NONE;
        if(nbt.hasUUID(RECIPE_TOOL_PROP))
        {
            requiredTool = ToolType.getToolType(nbt.getAsString());
        }
        final int minBldgLevel = nbt.getInt(RECIPE_BUILDING_MIN_LEVEL_PROP);
        final int maxBldgLevel = nbt.getInt(RECIPE_BUILDING_MAX_LEVEL_PROP);
        final boolean mustExist = nbt.getBoolean(RECIPE_MUST_EXIST);
        final boolean showTooltip = nbt.getBoolean(RECIPE_SHOW_TOOLTIP);
        final ListTag inputList = nbt.getList(RECIPE_INPUTS_PROP, Tag.TAG_COMPOUND);
        final List<ItemStorage> inputs = new ArrayList<>();
        for(Tag input : inputList)
        {
            if(input instanceof CompoundTag)
            {
                inputs.add(controller.deserialize((CompoundTag)input));
            }
        }
        final ItemStack primaryOutput = ItemStack.of(nbt.getCompound(RECIPE_RESULT_PROP));

        final ListTag secondaryList = nbt.getList(RECIPE_SECONDARY_PROP, Tag.TAG_COMPOUND);
        final List<ItemStack> secondaryOutput = new ArrayList<>();
        for(Tag secondary : secondaryList)
        {
            if(secondary instanceof CompoundTag)
            {
                secondaryOutput.add(ItemStack.of((CompoundTag)secondary));
            }
        }

        final ListTag altList = nbt.getList(RECIPE_ALTERNATE_PROP, Tag.TAG_COMPOUND);
        final List<ItemStack> altOutputs = new ArrayList<>();
        for(Tag alt : altList)
        {
            if(alt instanceof CompoundTag)
            {
                secondaryOutput.add(ItemStack.of((CompoundTag)alt));
            }
        }

        final Block intermediate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.get(RECIPE_INTERMEDIATE_PROP).getAsString()));

        return getNewInstance(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, lootTable, requiredTool, inputs, primaryOutput, secondaryOutput, altOutputs, intermediate);
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, CustomRecipe recipe, FriendlyByteBuf packetBuffer)
    {
        // This serialization is drastically more efficient: expect <150 bytes per recipes, avg, compared to 800 bytes for CompoundTag variant.
        // It also avoids populating the RecipeStorage cached inside the CustomRecipe.
        packetBuffer.writeUtf(recipe.getCrafter());
        packetBuffer.writeResourceLocation(recipe.getRecipeStorage().getRecipeSource());
        packetBuffer.writeBoolean(recipe.getRequiredResearchId() != null);
        if(recipe.getRequiredResearchId() != null)
        {
            packetBuffer.writeResourceLocation(recipe.getRequiredResearchId());
        }
        packetBuffer.writeBoolean(recipe.getExcludedResearchId() != null);
        if(recipe.getExcludedResearchId() != null)
        {
            packetBuffer.writeResourceLocation(recipe.getExcludedResearchId());
        }
        packetBuffer.writeBoolean(recipe.getLootTable() != null);
        if(recipe.getLootTable() != null)
        {
            packetBuffer.writeResourceLocation(recipe.getLootTable());
        }
        packetBuffer.writeUtf(recipe.getRequiredTool().getName());
        packetBuffer.writeVarInt(recipe.getMinBuildingLevel());
        packetBuffer.writeVarInt(recipe.getMaxBuildingLevel());
        packetBuffer.writeBoolean(recipe.getMustExist());
        packetBuffer.writeBoolean(recipe.getShowTooltip());
        packetBuffer.writeVarInt(recipe.getInputs().size());
        for(final ItemStorage input : recipe.getInputs())
        {
            controller.serialize(packetBuffer, input);
        }
        packetBuffer.writeItem(recipe.getPrimaryOutput());
        packetBuffer.writeVarInt(recipe.getSecondaryOutput().size());
        for(final ItemStack secondary : recipe.getSecondaryOutput())
        {
            packetBuffer.writeItem(secondary);
        }
        packetBuffer.writeVarInt(recipe.getAltOutputs().size());
        for(final ItemStack alts : recipe.getAltOutputs())
        {
            packetBuffer.writeItem(alts);
        }
        packetBuffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(recipe.getIntermediate()));
    }

    @NotNull
    @Override
    public CustomRecipe deserialize(@NotNull IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final String crafter = buffer.readUtf();
        final ResourceLocation recipeId = buffer.readResourceLocation();
        final ResourceLocation researchReq;
        if(buffer.readBoolean())
        {
            researchReq = buffer.readResourceLocation();
        }
        else
        {
            researchReq = null;
        }
        final ResourceLocation researchExclude;
        if(buffer.readBoolean())
        {
            researchExclude = buffer.readResourceLocation();
        }
        else
        {
            researchExclude = null;
        }
        final ResourceLocation lootTable;
        if(buffer.readBoolean())
        {
            lootTable = buffer.readResourceLocation();
        }
        else
        {
            lootTable = null;
        }
        final IToolType requiredTool = ToolType.getToolType(buffer.readUtf());
        final int minBldgLevel = buffer.readVarInt();
        final int maxBldgLevel = buffer.readVarInt();
        final boolean mustExist = buffer.readBoolean();
        final boolean showTooltip = buffer.readBoolean();
        final List<ItemStorage> inputs = new ArrayList<>();
        for(int numInputs = buffer.readVarInt(); numInputs > 0; numInputs--)
        {
            inputs.add(controller.deserialize(buffer));
        }
        final ItemStack primaryOutput = buffer.readItem();
        final List<ItemStack> secondaryOutput = new ArrayList<>();
        for(int numSec = buffer.readVarInt(); numSec > 0; numSec--)
        {
            secondaryOutput.add(buffer.readItem());
        }
        final List<ItemStack> altOutputs = new ArrayList<>();
        for(int numAlts = buffer.readVarInt(); numAlts > 0; numAlts--)
        {
            altOutputs.add(buffer.readItem());
        }
        
        final Block intermediate = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());

        return getNewInstance(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, lootTable, requiredTool, inputs, primaryOutput, secondaryOutput, altOutputs, intermediate);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.CUSTOM_RECIPE_ID;
    }
}
