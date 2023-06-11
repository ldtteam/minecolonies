package com.minecolonies.api.advancements.place_structure;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the "hut_name" or "structure_name" for the "place_structure" trigger
 */
public class PlaceStructureCriterionInstance extends AbstractCriterionTriggerInstance
{
    private String structureName;

    public PlaceStructureCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), ContextAwarePredicate.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param hutName the hut that has to be placed to succeed
     */
    public PlaceStructureCriterionInstance(final String hutName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), ContextAwarePredicate.ANY);

        this.structureName = hutName;
    }


    /**
     * Performs the check for the conditions
     * @param hutName the id of the structure that was just placed
     * @return whether the check succeeded
     */
    public boolean test(final String hutName)
    {
        if (this.structureName != null)
        {
            return this.structureName.equalsIgnoreCase(hutName);
        }

        return true;
    }

    @NotNull
    public static PlaceStructureCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                      @NotNull final DeserializationContext context)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = GsonHelper.getAsString(jsonObject, "hut_name");
            return new PlaceStructureCriterionInstance(hutName);
        }
        return new PlaceStructureCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final SerializationContext context)
    {
        final JsonObject json = super.serializeToJson(context);
        if (this.structureName != null)
        {
            json.addProperty("hut_name", this.structureName);
        }
        return json;
    }
}
