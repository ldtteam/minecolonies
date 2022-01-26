package com.minecolonies.api.advancements.place_structure;

import com.google.gson.JsonObject;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the "hut_name" or "structure_name" for the "place_structure" trigger
 */
public class PlaceStructureCriterionInstance extends CriterionInstance
{
    private String        hutName;
    private StructureName structureName;

    public PlaceStructureCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.AndPredicate.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param hutName the hut that has to be placed to succeed
     */
    public PlaceStructureCriterionInstance(final String hutName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.AndPredicate.ANY);

        this.hutName = hutName;
    }

    /**
     * Construct the check with a single condition
     * @param structureName the structure that has to be placed to succeed
     */
    public PlaceStructureCriterionInstance(final StructureName structureName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.AndPredicate.ANY);

        this.structureName = structureName;
    }

    /**
     * Performs the check for the conditions
     * @param structureName the id of the structure that was just placed
     * @return whether the check succeeded
     */
    public boolean test(final StructureName structureName)
    {
        if (this.hutName != null)
        {
            return this.hutName.equalsIgnoreCase(structureName.getHutName());
        }

        if (this.structureName != null)
        {
            return this.structureName.equals(structureName);
        }

        return true;
    }

    @NotNull
    public static PlaceStructureCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                      @NotNull final ConditionArrayParser conditions)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = JSONUtils.getAsString(jsonObject, "hut_name");
            return new PlaceStructureCriterionInstance(hutName);
        }
        else if (jsonObject.has("structure_name"))
        {
            final StructureName structureName = new StructureName(JSONUtils.getAsString(jsonObject, "structure_name"));
            return new PlaceStructureCriterionInstance(structureName);
        }
        return new PlaceStructureCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final ConditionArraySerializer serializer)
    {
        final JsonObject json = super.serializeToJson(serializer);
        if (this.hutName != null)
        {
            json.addProperty("hut_name", this.hutName);
        }
        else if (this.structureName != null)
        {
            json.addProperty("structure_name", this.structureName.toString());
        }
        return json;
    }
}
