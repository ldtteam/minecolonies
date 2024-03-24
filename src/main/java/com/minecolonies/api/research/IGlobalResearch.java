package com.minecolonies.api.research;

import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface defining how a research globally is defined.
 */
public interface IGlobalResearch
{
    /**
     * Check if this research can be executed at this moment.
     *
     * @param uni_level the level of the university.
     * @param localTree the local tree of the colony.
     * @return true if so.
     */
    boolean canResearch(int uni_level, @NotNull final ILocalResearchTree localTree);

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
    boolean hasEnoughResources(final IItemHandler inventory);

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
     * Getter of the research icon's resource location.
     * On the client, this texture file's presence has already been validated.
     * @return the ResourceLocation of the icon.
     */
    ResourceLocation getIconTextureResourceLocation();

    /**
     * Getter of the research icon's item stack.
     * @return the ItemStack for the icon.
     */
    ItemStack getIconItemStack();

    /**
     * Get the id of the parent IResearch.
     *
     * @return the parent id, as a ResourceLocation
     */
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
     * Set if a research should only allow one child.
     *
     * @param onlyChild the param to set.
     */
    void setOnlyChild(boolean onlyChild);

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
    void addEffect(final IResearchEffect<?> effect);

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
     * Set the parent of a research.
     *
     * @param id the id of the parent.
     */
    void setParent(ResourceLocation id);

    /**
     * Getter for the research requirement.
     *
     * @return the requirement.
     */
    List<IResearchRequirement> getResearchRequirement();

    /**
     * Get the effect of the research.
     *
     * @return the effect.
     */
    List<IResearchEffect<?>> getEffects();
}
