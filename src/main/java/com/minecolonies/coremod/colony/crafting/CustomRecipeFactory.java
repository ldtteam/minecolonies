package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.PARAMS_CUSTOM_RECIPE;
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
        if (context.length != PARAMS_CUSTOM_RECIPE)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only " + PARAMS_CUSTOM_RECIPE + " are allowed!");
        }
        if (!(context[0] instanceof String))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid Crafter Recipe crafter");
        }
        if (!(context[2] instanceof ItemStack))
        {
            throw new IllegalArgumentException("Unsupported context - Invalid output ItemStack");
        }
        return getNewInstance((String)context[0], (List<ItemStorage>)context[1], (ItemStack)context[2], (List<ItemStack>)context[3], (List<ItemStack>)context[4],
          (ResourceLocation)context[5], (ResourceLocation)context[6], (ResourceLocation)context[7], (ResourceLocation)context[8],
          (int)context[9], (int)context[10], (boolean)context[11], (boolean)context[12]);
    }

    private CustomRecipe getNewInstance(final String crafter, final List<ItemStorage> inputs, final ItemStack primaryOutput, final List<ItemStack> secondaryOutput, final List<ItemStack> altOutputs,
      final ResourceLocation lootTable, final ResourceLocation recipeId, final ResourceLocation researchReq, final ResourceLocation researchExclude, final int minBldgLevel, final int maxBldgLevel, final boolean mustExist, final boolean showTooltip)
    {
        return new CustomRecipe(crafter, inputs, primaryOutput, secondaryOutput, altOutputs, lootTable, recipeId, researchReq, researchExclude, minBldgLevel, maxBldgLevel, mustExist, showTooltip);
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
        if(recipe.getLootTable() != null)
        {
            compound.putString(RECIPE_LOOTTABLE_PROP, recipe.getLootTable().toString());
        }
        compound.putInt(RECIPE_BUILDING_MIN_LEVEL_PROP, recipe.getMinBuildingLevel());
        compound.putInt(RECIPE_BUILDING_MAX_LEVEL_PROP, recipe.getMaxBuildingLevel());
        compound.putBoolean(RECIPE_MUST_EXIST, recipe.getMustExist());
        compound.putBoolean(RECIPE_SHOW_TOOLTIP, recipe.getShowTooltip());
        // RecipeStorage has its own factory class, but its output is larger than necessary (~1KB per recipe, vs ~800b for below, averaged over all minecolonies recipes)
        // Most users don't care that much about the bandwidth -- 100kb for 500 recipes wouldn't be that bad compared to chunk updates, and this is only sent on rare occasions --
        // but the wrapping CompoundNBT containing the sent Custom Recipe NBTs can not exceed 2MB: this approach allows >1,700 (average) recipes, where RecipeStorage serializer would become unsafe at ~1,300 (average).
        // As the default sawmill recipes involve 342 recipes already (a/o 4/2021), and an expert pack could easily involve a large set of complex recipes, this isn't a far-off concern.
        // We're sending the recipes as multiple compoundNBTs, to avoid this issue going to a crash, but want to avoid the potential if refactored.
        // Calling getRecipeStorage() also populates and retains a cache internal to CustomRecipe, which we probably don't want to do.
        // Instead, we'll get only those components necessary to recreate on a remote client.
        final ListNBT inputs = new ListNBT();
        for(final ItemStorage in : recipe.getInputs())
        {
            final CompoundNBT item = getCompoundForItemStack(in.getItemStack());
            item.putBoolean(RECIPE_IGNORE_NBT, in.ignoreNBT());
            item.putBoolean(RECIPE_IGNORE_DMG, in.ignoreDamageValue());
            inputs.add(item);
        }
        compound.put(RECIPE_INPUTS_PROP, inputs);

        compound.put(RECIPE_RESULT_PROP, getCompoundForItemStack(recipe.getPrimaryOutput()));

        if(recipe.getSecondaryOutput().size() > 0)
        {
            final ListNBT secondaryOutputs = new ListNBT();
            for (final ItemStack is : recipe.getSecondaryOutput())
            {
                secondaryOutputs.add(getCompoundForItemStack(is));
            }
            compound.put(RECIPE_SECONDARY_PROP, secondaryOutputs);
        }
        if(recipe.getAltOutputs().size() > 0)
        {
            final ListNBT altOutputs = new ListNBT();
            for (final ItemStack is : recipe.getAltOutputs())
            {
                altOutputs.add(getCompoundForItemStack(is));
            }
            compound.put(RECIPE_ALTERNATE_PROP, altOutputs);
        }

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
        final ResourceLocation lootTable = new ResourceLocation(nbt.getString(RECIPE_LOOTTABLE_PROP));
        final int minBldgLevel = nbt.getInt(RECIPE_BUILDING_MIN_LEVEL_PROP);
        final int maxBldgLevel = nbt.getInt(RECIPE_BUILDING_MAX_LEVEL_PROP);
        final boolean mustExist = nbt.getBoolean(RECIPE_MUST_EXIST);
        final boolean showTooltip = nbt.getBoolean(RECIPE_SHOW_TOOLTIP);

        final List<ItemStorage> inputs = new ArrayList<>();
        for(INBT input : nbt.getList(RECIPE_INPUTS_PROP, Constants.TAG_COMPOUND))
        {
            if(input instanceof CompoundNBT)
            {
                final CompoundNBT in = (CompoundNBT)input;
                inputs.add(new ItemStorage(getItemStackForCompound(in), in.getBoolean(RECIPE_IGNORE_DMG), in.getBoolean(RECIPE_IGNORE_NBT)));
            }
        }
        final ItemStack primaryOutput = getItemStackForCompound(nbt.getCompound(RECIPE_RESULT_PROP));
        final List<ItemStack> secondaryOutput = new ArrayList<>();
        for(INBT sec : nbt.getList(RECIPE_SECONDARY_PROP, Constants.TAG_COMPOUND))
        {
            if(sec instanceof CompoundNBT)
            {
                secondaryOutput.add(getItemStackForCompound((CompoundNBT) sec));
            }
        }
        final List<ItemStack> altOutputs = new ArrayList<>();
        for(INBT alt : nbt.getList(RECIPE_ALTERNATE_PROP, Constants.TAG_COMPOUND))
        {
            if(alt instanceof CompoundNBT)
            {
                altOutputs.add(getItemStackForCompound((CompoundNBT) alt));
            }
        }

        return getNewInstance(crafter, inputs, primaryOutput, secondaryOutput, altOutputs, lootTable, recipeId, researchReq, researchExclude, minBldgLevel, maxBldgLevel, mustExist, showTooltip);
    }

    @Override
    public void serialize(@NotNull IFactoryController controller, CustomRecipe recipe, PacketBuffer packetBuffer)
    {
        //This serialization is drastically more efficient: expect <150 bytes per recipes, avg, compared to 800 bytes for CompoundNBT variant.
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
            packetBuffer.writeItemStack(input.getItemStack());
            packetBuffer.writeBoolean(input.ignoreDamageValue());
            packetBuffer.writeBoolean(input.ignoreNBT());
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
            inputs.add(new ItemStorage(buffer.readItemStack(), buffer.readBoolean(), buffer.readBoolean()));
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

        return getNewInstance(crafter, inputs, primaryOutput, secondaryOutput, altOutputs, lootTable, recipeId, researchReq, researchExclude, minBldgLevel, maxBldgLevel, mustExist, showTooltip);
    }

    @Override
    public short getSerializationId()
    {
        return 44;
    }
}
