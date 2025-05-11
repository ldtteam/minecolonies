package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchCosts;
import com.minecolonies.api.research.costs.ListItemCost;
import com.minecolonies.api.research.costs.SimpleItemCost;
import com.minecolonies.api.research.costs.TagItemCost;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.minecolonies.api.research.ModResearchCosts.*;

/**
 * Registry initializer for the {@link ModResearchCosts}.
 */
public class ModResearchCostInitializer
{
    public static final DeferredRegister<ResearchCostEntry> DEFERRED_REGISTER =
        DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "researchcosttypes"), Constants.MOD_ID);
    static
    {
        simpleItemCost = create(SIMPLE_ITEM_COST_ID, SimpleItemCost::new, SimpleItemCost::new);
        listItemCost = create(LIST_ITEM_COST_ID, ListItemCost::new, ListItemCost::new);
        tagItemCost = create(TAG_ITEM_COST_ID, TagItemCost::new, TagItemCost::new);
    }
    private ModResearchCostInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchCostInitializer but this is a Utility class.");
    }

    /**
     * Utility method to aid in the creation of a research cost.
     *
     * @param registryName the registry name for this entry.
     * @param readFromNBT  function to read this item from json.
     * @param readFromJson function to read this item from NBT.
     * @return the finalized registry object.
     */
    private static RegistryObject<ResearchCostEntry> create(final ResourceLocation registryName, final ReadFromNBTFunction readFromNBT, final ReadFromJsonFunction readFromJson)
    {
        return DEFERRED_REGISTER.register(registryName.getPath(), () -> new ResearchCostEntry(registryName, readFromNBT, readFromJson));
    }
}
