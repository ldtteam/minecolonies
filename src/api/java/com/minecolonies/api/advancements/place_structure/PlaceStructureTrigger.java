package com.minecolonies.api.advancements.place_structure;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class PlaceStructureTrigger extends AbstractCriterionTrigger<PlaceStructureListeners, PlaceStructureCriterionInstance>
{
    public PlaceStructureTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), PlaceStructureListeners::new);
    }

    public void trigger(final ServerPlayerEntity player, final StructureName structureName)
    {
        final PlaceStructureListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(structureName);
        }
    }

    @NotNull
    @Override
    public PlaceStructureCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = JSONUtils.getString(jsonObject, "hut_name");
            return new PlaceStructureCriterionInstance(hutName);
        }
        else if (jsonObject.has("structure_name"))
        {
            final StructureName structureName = new StructureName(JSONUtils.getString(jsonObject, "structure_name"));
            return new PlaceStructureCriterionInstance(structureName);
        }
        return new PlaceStructureCriterionInstance();
    }
}
