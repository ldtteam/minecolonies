package com.minecolonies.api.advancements.place_structure;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

public class PlaceStructureCriterionInstance extends CriterionInstance
{
    private String        hutName;
    private StructureName structureName;

    public PlaceStructureCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.AndPredicate.field_234582_a_);
    }

    public PlaceStructureCriterionInstance(final String hutName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.AndPredicate.field_234582_a_);

        this.hutName = hutName;
    }

    public PlaceStructureCriterionInstance(final StructureName structureName)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_STRUCTURE_PLACED), EntityPredicate.AndPredicate.field_234582_a_);

        this.structureName = structureName;
    }

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
}
