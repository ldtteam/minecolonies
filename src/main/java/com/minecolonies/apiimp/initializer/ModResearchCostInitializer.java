package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchCosts;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.research.costs.ListItemCost;
import com.minecolonies.coremod.research.costs.SimpleItemCost;
import com.minecolonies.coremod.research.costs.TagItemCost;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.research.ModResearchCosts.*;

/**
 * Registry initializer for the {@link ModResearchCosts}.
 */
public class ModResearchCostInitializer
{
    public static final DeferredRegister<ModResearchCosts.ResearchCostEntry>
      DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "researchcosttypes"), Constants.MOD_ID);
    static
    {
        ModResearchCosts.simpleItemCost =
          DEFERRED_REGISTER.register(SIMPLE_ITEM_COST_ID.getPath(), () -> new ModResearchCosts.ResearchCostEntry(SimpleItemCost::new));

        ModResearchCosts.listItemCost =
          DEFERRED_REGISTER.register(LIST_ITEM_COST_ID.getPath(), () -> new ModResearchCosts.ResearchCostEntry(ListItemCost::new));

        ModResearchCosts.tagItemCost =
          DEFERRED_REGISTER.register(TAG_ITEM_COST_ID.getPath(), () -> new ModResearchCosts.ResearchCostEntry(TagItemCost::new));
    }
    private ModResearchCostInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchCostInitializer but this is a Utility class.");
    }
}
