package com.minecolonies.api.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
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
    List<ItemStorage> getCostList();

    /**
     * Start the research.
     * @param localResearchTree the local research tree to store in the colony.
     */
    void startResearch(@NotNull final ILocalResearchTree localResearchTree);

    /**
     * Human readable description of research.
     * @return the description.
     */
    String getDesc();

    /**
     * Subtitle description of research.
     * @return the optional subtitle name.
     */
    String getSubtitle();

    /**
     * Getter of the id of the research.
     * @return the String id.
     */
    String getId();

    /**
     * Getter of the research icon's resource location.
     * @return the string of the ResourceLocation of the icon, in the pattern namespace:directory:(optional count).
     */
    String getIcon();


    /**
     * Getter of the resource location of the research, if loaded by datapack.
     *
     * @return the resource location for a dynamically loaded research,
     * or minecolonies/staticresearch if statically assigned.
     */
    ResourceLocation getResourceLocation();

    /**
     * Get the id of the parent IResearch.
     *
     * @return the string id.
     */
    String getParent();

    /**
     * Get the string name of the branch.
     *
     * @return the branch name.
     */
    String getBranch();

    /**
     * Get the depth in the research tree.
     *
     * @return the depth.
     */
    int getDepth();

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
    void addChild(final String child);

    /**
     * Get the list of children of the research.
     *
     * @return a copy of the list of child identifiers.
     */
    ImmutableList<String> getChildren();

    /**
     * Set the parent of a research.
     *
     * @param id the id of the parent.
     */
    void setParent(String id);

    /**
     * Set the research requirement.
     *
     * @param requirements the requirements.
     */
    void setRequirement(final List<IResearchRequirement> requirements);

    /**
     * Set the research effects.
     *
     * @param effects      the effects of research completion.
     */
    void setEffects(final List<IResearchEffect<?>> effects);

    /**
     * Set the research costs.
     *
     * @param costs the items consumed to begin research.
     */
    void setCosts(final List<ItemStorage> costs);

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
