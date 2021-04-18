package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.PARAMS_CUSTOM_RECIPE;
import static com.minecolonies.api.util.constant.Constants.PARAMS_CUSTOM_RECIPE_MGR;
import static com.minecolonies.coremod.colony.crafting.CustomRecipe.*;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing Crafter Recipes.
 */
public class CustomRecipeFactory implements IFactory<FactoryVoidInput, CustomRecipe>
{
    private final static String CUSTOM_RECIPE_ID_PROP = "id";
    private final static String RECIPE_IGNORE_NBT = "ign-nbt";
    private final static String RECIPE_IGNORE_DMG = "ign-dmg";
    private final static String RECIPE_TAG = "tag";


    @NotNull
    @Override
    public CustomRecipe getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, final Object... context)
      throws IllegalArgumentException
    {
        if (context.length != PARAMS_CUSTOM_RECIPE && context.length != PARAMS_CUSTOM_RECIPE_MGR)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only " + PARAMS_CUSTOM_RECIPE + " or " + PARAMS_CUSTOM_RECIPE_MGR + "are allowed!");
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
        if(context[8] instanceof RecipeStorage)
        {
            return getNewInstance((String)context[0], (int)context[1], (int)context[2], (boolean)context[3], (boolean)context[4], (ResourceLocation)context[5], (ResourceLocation)context[6], (ResourceLocation)context[7], (RecipeStorage) context[8]);
        }
        if(!(context[8] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid ResourceLocation");
        }
        if(!((context[9]) instanceof List) ||
             !((context[11]) instanceof List) ||
             !((context[12]) instanceof List))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid Item Information");
        }

        return getNewInstance((String)context[0], (int)context[1], (int)context[2], (boolean)context[3], (boolean)context[4], (ResourceLocation)context[5], (ResourceLocation)context[6], (ResourceLocation)context[7],
          (ResourceLocation)context[8], (List<ItemStorage>)context[9], (ItemStack)context[10], (List<ItemStack>) context[11],  (List<ItemStack>) context[12]);
    }

    private CustomRecipe getNewInstance(final String crafter, final int minBldgLevel, final int maxBldgLevel, final boolean mustExist, final boolean showTooltip, final ResourceLocation recipeId,
      final ResourceLocation researchReq, final ResourceLocation researchExclude, final ResourceLocation lootTable, final List<ItemStorage> inputs,
      final ItemStack primaryOutput, final List<ItemStack> secondaryOutput, final List<ItemStack> altOutputs)
    {
        return new CustomRecipe(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, lootTable, inputs, primaryOutput, secondaryOutput, altOutputs);
    }

    private CustomRecipe getNewInstance(final String crafter, final int minBldgLevel, final int maxBldgLevel, final boolean mustExist, final boolean showTooltip, final ResourceLocation recipeId,
      final ResourceLocation researchReq, final ResourceLocation researchExclude, final RecipeStorage recipe)
    {
        return new CustomRecipe(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, recipe);
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

    private CompoundNBT getCompoundForItemStack(final ItemStack is)
    {
        final CompoundNBT item = new CompoundNBT();
        item.putString(ITEM_PROP, is.getItem().getRegistryName().toString());
        item.putInt(COUNT_PROP, is.getCount());
        if(is.hasTag())
        {
            item.put(RECIPE_TAG, is.serializeNBT());
        }
        return item;
    }

    private ItemStack getItemStackForCompound(final CompoundNBT nbt)
    {
        final ItemStack item = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString(ITEM_PROP))), nbt.getInt(COUNT_PROP));
        if(nbt.hasUniqueId(RECIPE_TAG))
        {
            item.deserializeNBT(nbt.getCompound(RECIPE_TAG));
        }
        return item;
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final CustomRecipe recipe)
    {
        // CustomRecipes involve a large number of ItemStacks, and a number of data types.  Individually, this isn't that big of a deal (~1.2KB vs 120bytes).
        // However, for large sets, such as CustomRecipeManager, this can grow into a very large total (ie, default Minecolonies + Structurize recipes alone can total >600KB).
        // If transmitting a large number of recipes at once across a network, favor the byte-based serializer instead.
        // This serialization also populates the RecipeStorage inside the Custom Recipe, which remains cached until the next data pack reload.
        // Be aware of the 2MB limit for CompoundNBTs if assigning a large number of these together into a listNBT.
        final CompoundNBT compound = new CompoundNBT();
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
        compound.putInt(RECIPE_BUILDING_MIN_LEVEL_PROP, recipe.getMinBuildingLevel());
        compound.putInt(RECIPE_BUILDING_MAX_LEVEL_PROP, recipe.getMaxBuildingLevel());
        compound.putBoolean(RECIPE_MUST_EXIST, recipe.getMustExist());
        compound.putBoolean(RECIPE_SHOW_TOOLTIP, recipe.getShowTooltip());
        compound.put(RECIPE_TYPE_RECIPE, controller.serialize(recipe.getRecipeStorage()));

        return compound;
    }

    @NotNull
    @Override
    public CustomRecipe deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final String crafter = nbt.getString(RECIPE_CRAFTER_PROP);
        final ResourceLocation recipeId = new ResourceLocation(nbt.getString(CUSTOM_RECIPE_ID_PROP));
        final ResourceLocation researchReq = new ResourceLocation(nbt.getString(RECIPE_RESEARCHID_PROP));
        final ResourceLocation researchExclude = new ResourceLocation(nbt.getString(RECIPE_EXCLUDED_RESEARCHID_PROP));
        final int minBldgLevel = nbt.getInt(RECIPE_BUILDING_MIN_LEVEL_PROP);
        final int maxBldgLevel = nbt.getInt(RECIPE_BUILDING_MAX_LEVEL_PROP);
        final boolean mustExist = nbt.getBoolean(RECIPE_MUST_EXIST);
        final boolean showTooltip = nbt.getBoolean(RECIPE_SHOW_TOOLTIP);
        final RecipeStorage recipe = controller.deserialize(nbt.getCompound(RECIPE_TYPE_RECIPE));

        return getNewInstance(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, recipe);
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, CustomRecipe recipe, PacketBuffer packetBuffer)
    {
        // This serialization is drastically more efficient: expect <150 bytes per recipes, avg, compared to 800 bytes for CompoundNBT variant.
        // It also avoids populating the RecipeStorage cached inside the CustomRecipe.
        packetBuffer.writeString(recipe.getCrafter());
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
        packetBuffer.writeVarInt(recipe.getMinBuildingLevel());
        packetBuffer.writeVarInt(recipe.getMaxBuildingLevel());
        packetBuffer.writeBoolean(recipe.getMustExist());
        packetBuffer.writeBoolean(recipe.getShowTooltip());
        packetBuffer.writeVarInt(recipe.getInputs().size());
        for(final ItemStorage input : recipe.getInputs())
        {
            controller.serialize(packetBuffer, input);
        }
        packetBuffer.writeItemStack(recipe.getPrimaryOutput());
        packetBuffer.writeVarInt(recipe.getSecondaryOutput().size());
        for(final ItemStack secondary : recipe.getSecondaryOutput())
        {
            packetBuffer.writeItemStack(secondary);
        }
        packetBuffer.writeVarInt(recipe.getAltOutputs().size());
        for(final ItemStack alts : recipe.getAltOutputs())
        {
            packetBuffer.writeItemStack(alts);
        }
    }

    @NotNull
    @Override
    public CustomRecipe deserialize(@NotNull IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final String crafter = buffer.readString();
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
        final int minBldgLevel = buffer.readVarInt();
        final int maxBldgLevel = buffer.readVarInt();
        final boolean mustExist = buffer.readBoolean();
        final boolean showTooltip = buffer.readBoolean();
        final List<ItemStorage> inputs = new ArrayList<>();
        for(int numInputs = buffer.readVarInt(); numInputs > 0; numInputs--)
        {
            inputs.add(controller.deserialize(buffer));
        }
        final ItemStack primaryOutput = buffer.readItemStack();
        final List<ItemStack> secondaryOutput = new ArrayList<>();
        for(int numSec = buffer.readVarInt(); numSec > 0; numSec--)
        {
            secondaryOutput.add(buffer.readItemStack());
        }
        final List<ItemStack> altOutputs = new ArrayList<>();
        for(int numAlts = buffer.readVarInt(); numAlts > 0; numAlts--)
        {
            altOutputs.add(buffer.readItemStack());
        }

        return getNewInstance(crafter, minBldgLevel, maxBldgLevel, mustExist, showTooltip, recipeId, researchReq, researchExclude, lootTable, inputs, primaryOutput, secondaryOutput, altOutputs);
    }

    @Override
    public short getSerializationId()
    {
        return 44;
    }
}
