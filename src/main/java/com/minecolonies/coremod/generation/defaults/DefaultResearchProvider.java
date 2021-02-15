package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.research.AbstractResearchProvider;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class for creating the Research-related JSONs, including Research, ResearchEffects, and (optional) Branches.
 * Note that this does not validate that the resulting research tree is coherent:
 * programmers should make sure that research parents and effects exist, that depth is 1 or one level above the parent depth,
 * and that cost and requirement identifiers match real items.
 *
 * Avoid changing research resource locations here unless necessary.
 * If such a change is required, add the old and new ResearchIds to ResearchCompatMap.
 * ResearchIDs are stored to disk, and if not present on a GlobalResearchTree during colony load, will be lost.
 * Effect and Branch ResourceLocations are not stored to disk, but changing them may cause confusion with outside datapacks.
 */
public class DefaultResearchProvider extends AbstractResearchProvider
{
    public DefaultResearchProvider(final DataGenerator generator)
    {
        super(generator);
    }

    private static final String CIVIL  = "civilian";
    private static final String COMBAT = "combat";
    private static final String TECH   = "technology";

    @Override
    public Collection<ResearchBranch> getResearchBranchCollection()
    {
        final List<ResearchBranch> branches = new ArrayList<>();
        branches.add(new ResearchBranch(new ResourceLocation(Constants.MOD_ID, CIVIL))
                       .setBranchName("Civilian").setBranchTimeMultiplier(1.0));
        branches.add(new ResearchBranch(new ResourceLocation(Constants.MOD_ID, COMBAT))
                       .setBranchName("Combat").setBranchTimeMultiplier(1.0));
        branches.add(new ResearchBranch(new ResourceLocation(Constants.MOD_ID, TECH))
                       .setBranchName("Technology").setBranchTimeMultiplier(1.0));
        return branches;
    }

    @Override
    public Collection<ResearchEffect> getResearchEffectCollection()
    {
        final List<ResearchEffect> effects = new ArrayList<>();

        return effects;
    }

    @Override
    public Collection<Research> getResearchCollection()
    {
        final List<Research> researches = new ArrayList<>();

        return researches;
    }
}
