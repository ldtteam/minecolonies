package com.minecolonies.api.advancements.place_structure;

import com.google.gson.JsonObject;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the "hut_name" or "structure_name" for the "place_structure" trigger
 */
public class PlaceStructureCriterionInstance extends AbstractCriterionTriggerInstance
{
    private String        hutName;
    private StructureName structureName;

    public PlaceStructureCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.Composite.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param hutName the hut that has to be placed to succeed
     */
    public PlaceStructureCriterionInstance(final String hutName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.Composite.ANY);

        this.hutName = hutName;
    }

    /**
     * Construct the check with a single condition
     * @param structureName the structure that has to be placed to succeed
     */
    public PlaceStructureCriterionInstance(final StructureName structureName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.Composite.ANY);

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
                                                                      @NotNull final DeserializationContext context)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = GsonHelper.getAsString(jsonObject, "hut_name");
            return new PlaceStructureCriterionInstance(hutName);
        }
        else if (jsonObject.has("structure_name"))
        {
            final StructureName structureName = new StructureName(GsonHelper.getAsString(jsonObject, "structure_name"));
            return new PlaceStructureCriterionInstance(structureName);
        }
        return new PlaceStructureCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final SerializationContext context)
    {
        final JsonObject json = super.serializeToJson(context);
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
