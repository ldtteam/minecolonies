package com.minecolonies.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minecolonies.api.colony.buildings.IBuilding;
import java.util.List;

/**
 * Interface defining how a research globally is defined.
 */
public interface IGlobalResearch
{
    /**
     * Check if this research can be executed at this moment.
     *
     * @param building the university building trying to do the research.
     * @param localTree the local tree of the colony.
     * @return true if so.
     */
    public boolean canResearch(@NotNull IBuilding building, @NotNull final ILocalResearchTree localTree);

    /**
     * Check if this research can be displayed in the GUI.
     *
     * @param uni_level the level of the university.
     * @return true if so.
     */
    boolean canDisplay(int uni_level);

    /**
     * Check whether all resources are available to execute the research.
     *
     * @param inventory the inventory to check in.
     * @return true if so
     */
    boolean hasEnoughResources(final @NotNull Player player, final @NotNull BlockPos universityPos);

    /**
     * Get the cost list from the research.
     *
     * @return the list.
     */
    List<IResearchCost> getCostList();

    /**
     * Start the research.
     * @param localResearchTree the local research tree to store in the colony.
     */
    void startResearch(@NotNull final ILocalResearchTree localResearchTree);

    /**
     * Human-readable description of research, in human-readable text or as a translation key.
     * @return the description.
     */
    TranslatableContents getName();

    /**
     * Subtitle description of research, in human-readable text or as a translation key.
     * @return the optional subtitle name.
     */
    TranslatableContents getSubtitle();

    /**
     * Getter of the id of the research.
     * @return the research id, as a ResourceLocation
     */
    ResourceLocation getId();

    /**
     * Get the id of the parent IResearch.
     *
     * @return the parent id, as a ResourceLocation
     */
    @Nullable
    ResourceLocation getParent();

    /**
     * Get the id of the branch.
     *
     * @return the branch id, as a ResourceLocation
     */
    ResourceLocation getBranch();

    /**
     * Get the depth in the research tree.
     *
     * @return the depth.
     */
    int getDepth();

    /**
     * Get the sort order for relative display position.
     *
     * @return the depth.
     */
    int getSortOrder();

    /**
     * Check if this research is an instant research.  If so, it will attempt to start when its requirements are complete, and prompt the player.
     *
     * @return true if so.
     */
    boolean isInstant();

    /**
     * Check if this research should automatically start when requirements are complete.
     * This can temporarily exceed normal limits of the max number of concurrent researches.
     * @return true if so.
     */
    boolean isAutostart();

    /**
     * Check if this research is a hidden research.  If so, it (and its children) should only be visible if all requirements are met.
     *
     * @return true if so.
     */
    boolean isHidden();

    /**
     * Check if this research is an immutable research.  If so, it (and ancestor research unlocking it) can not be reset once completed.
     *
     * @return true if so.
     */
    boolean isImmutable();

    /**
     * Check if this research is an only child research. This means, after researching one child no other children can be researched.
     *
     * @return true if so.
     */
    boolean hasOnlyChild();

    /**
     * Check if this research has other children and if one of these children has been research already.
     *
     * @param localTree the local tree of the colony.
     * @return true if so.
     */
    boolean hasResearchedChild(@NotNull final ILocalResearchTree localTree);

    /**
     * Add a child to a research.
     *
     * @param child the child to add.
     */
    void addChild(IGlobalResearch child);

    /**
     * Add a child to a research, without setting parentage.
     * @param child the child to add
     */
    void addChild(final ResourceLocation child);

    /**
     * Add an individual cost.
     * @param cost the individual item to add to the cost list, as a reseach cost instance.
     */
    void addCost(final IResearchCost cost);

    /**
     * Add an individual effect.
     * @param effect the individual effect to add to the research, as a IResearchEffect.
     */
    void addEffect(final IResearchEffect effect);

    /**
     * Add an individual requirement
     * @param requirement the individual requirement to add to the research, as an IResearchRequirement.
     */
    void addRequirement(final IResearchRequirement requirement);

    /**
     * Get the list of children of the research.
     *
     * @return a copy of the list of child identifiers.
     */
    List<ResourceLocation> getChildren();

    /**
     * Getter for the research requirement.
     *
     * @return the requirement.
     */
    List<IResearchRequirement> getResearchRequirements();

    /**
     * Get the effect of the research.
     *
     * @return the effect.
     */
    List<IResearchEffect> getEffects();

    /**
     * A stack "matches" a research ingredient if:
     * - It has the same Item
     * - It does NOT carry enchantments, custom names, etc.
     */
    public static boolean isPlayerResearchMatch(ItemStack stack, Item cost)
    {
        if (stack.isEmpty() || stack.getItem() != cost)
        {
            return false;
        }

        // Reject anything enchanted or custom-named
        if (stack.isEnchanted() || stack.hasCustomHoverName())
        {
            return false;
        }

        return true;
    }

    /**
     * A stack "matches" a research ingredient if:
     * - It has the same Item (even if they are enchanted or have a custom name)
     */
    public static boolean isUniversityResearchMatch(ItemStack stack, Item cost)
    {
        if (stack.isEmpty() || stack.getItem() != cost)
        {
            return false;
        }

        return true;
    }
}
