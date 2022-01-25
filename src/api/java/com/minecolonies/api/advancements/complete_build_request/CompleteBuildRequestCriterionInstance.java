package com.minecolonies.api.advancements.complete_build_request;

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
 * The test instance to check "hut_name" or "structure_name" for the "complete_build_request" trigger
 */
public class CompleteBuildRequestCriterionInstance extends AbstractCriterionTriggerInstance
{
    private String        hutName;
    private StructureName structureName;
    private int           level = -1;

    public CompleteBuildRequestCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COMPLETE_BUILD_REQUEST), EntityPredicate.Composite.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param structureName the structure that has to be completed to succeed
     */
    public CompleteBuildRequestCriterionInstance(final StructureName structureName)
    {
        this();

        this.structureName = structureName;
    }

    /**
     * Construct the check with a single condition
     * @param hutName the hut that has to be completed to succeed
     */
    public CompleteBuildRequestCriterionInstance(final String hutName)
    {
        this();

        this.hutName = hutName;
    }

    /**
     * Construct the check with a more specific condition
     * @param structureName the structure that has to be completed to succeed
     * @param level the level of the structure that should be completed
     */
    public CompleteBuildRequestCriterionInstance(final StructureName structureName, final int level)
    {
        this();

        this.structureName = structureName;
        this.level = level;
    }

    /**
     * Construct the check with a more specific condition
     * @param hutName the hut that has to be completed to succeed
     * @param level the level of the hut that should be completed
     */
    public CompleteBuildRequestCriterionInstance(final String hutName, final int level)
    {
        this();

        this.hutName = hutName;
        this.level = level;
    }

    /**
     * Performs the check for the conditions
     * @param structureName the id of the structure that was just built
     * @param level the level that the structure is now on, or 0
     * @return whether the check succeeded
     */
    public boolean test(final StructureName structureName, final int level)
    {
        if (this.hutName != null && this.level != -1)
        {
            return this.hutName.equalsIgnoreCase(structureName.getHutName()) && this.level <= level;
        }
        else if (this.hutName != null)
        {
            return this.hutName.equalsIgnoreCase(structureName.getHutName());
        }

        if (this.structureName != null && this.level != -1)
        {
            return this.structureName.equals(structureName) && this.level <= level;
        }
        else if (this.structureName != null)
        {
            return this.structureName.equals(structureName);
        }

        return true;
    }

    @NotNull
    public static CompleteBuildRequestCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                            @NotNull final DeserializationContext context)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = GsonHelper.getAsString(jsonObject, "hut_name");
            if (jsonObject.has("level"))
            {
                final int level = GsonHelper.getAsInt(jsonObject, "level");
                return new CompleteBuildRequestCriterionInstance(hutName, level);
            }
            return new CompleteBuildRequestCriterionInstance(hutName);
        }

        if (jsonObject.has("structure_name"))
        {
            final StructureName structureName = new StructureName(GsonHelper.getAsString(jsonObject, "structure_name"));
            if (jsonObject.has("structure_name"))
            {
                final int level = GsonHelper.getAsInt(jsonObject, "level");
                return new CompleteBuildRequestCriterionInstance(structureName, level);
            }
            return new CompleteBuildRequestCriterionInstance(structureName);
        }

        return new CompleteBuildRequestCriterionInstance();
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
        if (this.level >= 0)
        {
            json.addProperty("level", this.level);
        }
        return json;
    }
}
