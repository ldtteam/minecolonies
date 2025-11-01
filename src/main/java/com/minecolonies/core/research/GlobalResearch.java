package com.minecolonies.core.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.IResearchCost;
import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the IGlobalResearch interface which represents the research on the global level.
 */
public class GlobalResearch implements IGlobalResearch
{
    /**
     * The costList of the research.
     */
    private final List<IResearchCost> costList = new ArrayList<>();

    /**
     * The id of the research.
     */
    private final ResourceLocation id;

    /**
     * The id of the parent research which has to be completed first.
     */
    @Nullable
    private final ResourceLocation parent;

    /**
     * The research branch id.
     */
    private final ResourceLocation branch;

    /**
     * The pre-localized name for the research.
     */
    private final TranslatableContents name;

    /**
     * Subtitle names for the research.  Optional, and only shows up rarely.
     */
    private final TranslatableContents subtitle;

    /**
     * The research effects of this research.
     */
    private final List<IResearchEffect> effects = new ArrayList<>();

    /**
     * The depth level in the tree.
     */
    private final int depth;

    /**
     * The sort order of the research.
     */
    private final int sortOrder;

    /**
     * If the research has an only child.
     */
    private final boolean onlyChild;

    /**
     * If the research has an only child.
     */
    private final boolean hidden;

    /**
     * If the research starts automatically when requirements met.
     */
    private final boolean autostart;

    /**
     * If the research has an only child.
     */
    private final boolean instant;

    /**
     * If the research can be reset or unlearned after being unlocked.
     */
    private final boolean immutable;

    /**
     * List of children of research.
     */
    private final List<ResourceLocation> children = new ArrayList<>();

    /**
     * The requirement for this research.
     */
    private final List<IResearchRequirement> requirements = new ArrayList<>();

    /**
     * Create the new research with multiple effects
     *
     * @param id        its id.
     * @param parent    the research's parent, if one is present, or null if not.
     * @param branch    the branch it is on.
     * @param name      the optional name of the research. If empty, a key generated from the id will be used instead.
     * @param subtitle  the optional short description of the research, in plaintext or as a translation key.
     * @param depth     the depth in the tree.
     * @param sortOrder the relative vertical order of the research's display, in relation to its siblings.
     * @param onlyChild if the research allows only one child research to be completed.
     * @param hidden    if the research is only visible when eligible to be researched.
     * @param autostart if the research should begin automatically, or notify the player, when it is eligible.
     * @param instant   if the research should be completed instantly (ish) from when begun.
     * @param immutable if the research can not be reset once unlocked.
     */
    public GlobalResearch(
        final ResourceLocation id,
        final @Nullable ResourceLocation parent,
        final ResourceLocation branch,
        final TranslatableContents name,
        final TranslatableContents subtitle,
        final int depth,
        final int sortOrder,
        final boolean onlyChild,
        final boolean hidden,
        final boolean autostart,
        final boolean instant,
        final boolean immutable)
    {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.branch = branch;
        this.parent = parent;
        this.depth = depth;
        this.sortOrder = sortOrder;
        this.onlyChild = onlyChild;
        this.hidden = hidden;
        this.autostart = autostart;
        this.instant = instant;
        this.immutable = immutable;
    }

    @Override
    public boolean canResearch(final int uni_level, @NotNull final ILocalResearchTree localTree)
    {
        final IGlobalResearch parentResearch = parent == null ? null : IGlobalResearchTree.getInstance().getResearch(branch, parent);
        final ILocalResearch localParentResearch = parent == null ? null : localTree.getResearch(branch, parent);
        final ILocalResearch localResearch = localTree.getResearch(this.getBranch(), this.getId());

        return (localResearch == null) && canDisplay(uni_level) && (parentResearch == null
            || localParentResearch != null && localParentResearch.getState() == ResearchState.FINISHED) && (parentResearch == null || !parentResearch.hasResearchedChild(localTree)
            || !parentResearch.hasOnlyChild()) && (depth < 6 || !localTree.branchFinishedHighestLevel(branch));
    }

    @Override
    public boolean canDisplay(final int uni_level)
    {
        return uni_level >= depth;
    }

    @Override
    public boolean hasEnoughResources(final IItemHandler inventory)
    {
        if (costList.isEmpty())
        {
            return true;
        }

        for (final IResearchCost ingredient : costList)
        {
            int totalCount = 0;
            for (final Item cost : ingredient.getItems())
            {
                final int count = InventoryUtils.getItemCountInItemHandler(inventory, stack -> stack.getItem().equals(cost));
                totalCount += count;
            }
            if (totalCount < ingredient.getCount())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<IResearchCost> getCostList()
    {
        return ImmutableList.copyOf(costList);
    }

    @Override
    public void startResearch(@NotNull final ILocalResearchTree localResearchTree)
    {
        if (localResearchTree.getResearch(this.branch, this.id) == null)
        {
            final ILocalResearch research = new LocalResearch(this.id, this.branch, this.depth);
            if (this.instant)
            {
                research.setProgress(IGlobalResearchTree.getInstance().getBranchData(branch).getBaseTime(research.getDepth()));
            }
            research.setState(ResearchState.IN_PROGRESS);
            localResearchTree.addResearch(branch, research);
        }
    }

    @NotNull
    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public TranslatableContents getName() { return this.name; }

    @Override
    public TranslatableContents getSubtitle()
    {
        return this.subtitle;
    }

    @Override
    @Nullable
    public ResourceLocation getParent()
    {
        return this.parent;
    }

    @Override
    public ResourceLocation getBranch()
    {
        return this.branch;
    }

    @Override
    public int getDepth()
    {
        return this.depth;
    }

    @Override
    public int getSortOrder()
    {
        return this.sortOrder;
    }

    @Override
    public boolean isInstant()
    {
        return this.instant;
    }

    @Override
    public boolean isHidden()
    {
        return this.hidden;
    }

    @Override
    public boolean isAutostart()
    {
        return this.autostart;
    }

    @Override
    public boolean isImmutable()
    {
        return this.immutable;
    }

    @Override
    public boolean hasOnlyChild()
    {
        return onlyChild;
    }

    @Override
    public boolean hasResearchedChild(@NotNull final ILocalResearchTree localTree)
    {
        for (final ResourceLocation child : this.children)
        {
            final IGlobalResearch childResearch = IGlobalResearchTree.getInstance().getResearch(branch, child);
            final ILocalResearch localResearch = localTree.getResearch(childResearch.getBranch(), childResearch.getId());
            if (localResearch != null)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addChild(final IGlobalResearch child)
    {
        this.children.add(child.getId());
    }

    @Override
    public void addChild(final ResourceLocation child)
    {
        this.children.add(child);
    }

    @Override
    public void addCost(final IResearchCost cost)
    {
        costList.add(cost);
    }

    public void addEffect(final IResearchEffect effect)
    {
        effects.add(effect);
    }

    public void addRequirement(final IResearchRequirement requirement)
    {
        this.requirements.add(requirement);
    }

    @Override
    public List<IResearchRequirement> getResearchRequirements()
    {
        return this.requirements;
    }

    @Override
    public List<ResourceLocation> getChildren()
    {
        return this.children;
    }

    @Override
    public List<IResearchEffect> getEffects()
    {
        return this.effects;
    }
}
