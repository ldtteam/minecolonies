package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchCostTypes;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.research.costs.ListItemCost;
import com.minecolonies.core.research.costs.SimpleItemCost;
import com.minecolonies.core.research.costs.TagItemCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.research.ModResearchCostTypes.*;

/**
 * Registry initializer for the {@link ModResearchCostTypes}.
 */
public class ModResearchCostTypeInitializer
{
    public static final DeferredRegister<ResearchCostType>
      DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "researchcosttypes"), Constants.MOD_ID);
    static
    {
        ModResearchCostTypes.simpleItemCost =
          DEFERRED_REGISTER.register(SIMPLE_ITEM_COST_ID.getPath(), () -> new ResearchCostType(SIMPLE_ITEM_COST_ID, SimpleItemCost::new));

        ModResearchCostTypes.listItemCost =
          DEFERRED_REGISTER.register(LIST_ITEM_COST_ID.getPath(), () -> new ResearchCostType(LIST_ITEM_COST_ID, ListItemCost::new));

        ModResearchCostTypes.tagItemCost =
          DEFERRED_REGISTER.register(TAG_ITEM_COST_ID.getPath(), () -> new ResearchCostType(TAG_ITEM_COST_ID, TagItemCost::new));
    }
    private ModResearchCostTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchCostInitializer but this is a Utility class.");
    }
}
