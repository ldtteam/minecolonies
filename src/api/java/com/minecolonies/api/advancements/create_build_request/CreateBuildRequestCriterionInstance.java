package com.minecolonies.api.advancements.create_build_request;

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
 * The test instance to check "hut_name" or "structure_name" for the "create_build_request" trigger
 */
public class CreateBuildRequestCriterionInstance extends CriterionInstance
{
    private String        hutName;
    private StructureName structureName;
    private int           level = -1;

    public CreateBuildRequestCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST), EntityPredicate.AndPredicate.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param structureName the structure that has to be requested to succeed
     */
    public CreateBuildRequestCriterionInstance(final StructureName structureName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST), EntityPredicate.AndPredicate.ANY);

        this.structureName = structureName;
    }

    /**
     * Construct the check with a single condition
     * @param hutName the hut that has to be requested to succeed
     */
    public CreateBuildRequestCriterionInstance(final String hutName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST), EntityPredicate.AndPredicate.ANY);

        this.hutName = hutName;
    }

    /**
     * Construct the check with a more specific condition
     * @param structureName the structure that has to be requested to succeed
     * @param level the level that the request should complete
     */
    public CreateBuildRequestCriterionInstance(final StructureName structureName, final int level)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST), EntityPredicate.AndPredicate.ANY);

        this.structureName = structureName;
        this.level = level;
    }

    /**
     * Construct the check with a more specific condition
     * @param hutName the hut that has to be requested to succeed
     * @param level the level that the request should complete
     */
    public CreateBuildRequestCriterionInstance(final String hutName, final int level)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST), EntityPredicate.AndPredicate.ANY);

        this.hutName = hutName;
        this.level = level;
    }

    /**
     * Performs the check for the conditions
     * @param structureName the id of the structure that was just requested
     * @param level the level that the structure will be once completed, or 0
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
    public static CreateBuildRequestCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                          @NotNull final ConditionArrayParser conditions)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = JSONUtils.getAsString(jsonObject, "hut_name");
            if (jsonObject.has("level"))
            {
                final int level = JSONUtils.getAsInt(jsonObject, "level");
                return new CreateBuildRequestCriterionInstance(hutName, level);
            }
            return new CreateBuildRequestCriterionInstance(hutName);
        }

        if (jsonObject.has("structure_name"))
        {
            final StructureName structureName = new StructureName(JSONUtils.getAsString(jsonObject, "structure_name"));
            if (jsonObject.has("structure_name"))
            {
                final int level = JSONUtils.getAsInt(jsonObject, "level");
                return new CreateBuildRequestCriterionInstance(structureName, level);
            }
            return new CreateBuildRequestCriterionInstance(structureName);
        }

        return new CreateBuildRequestCriterionInstance();
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
        if (this.level >= 0)
        {
            json.addProperty("level", this.level);
        }
        return json;
    }
}
