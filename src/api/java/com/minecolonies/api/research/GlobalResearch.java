package com.minecolonies.api.research;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The implementation of the IResearch interface which represents one type of research.
 */
public class GlobalResearch implements IGlobalResearch
{
    /**
     * The costList of the research.
     */
    private final List<ItemStorage> costList = new ArrayList<>();

    /**
     * The parent research which has to be completed first.
     */
    private String parent = "";

    /**
     * The string id of the research.
     */
    private final String id;

    /**
     * The research branch.
     */
    private final String branch;

    /**
     * The description of the research.
     */
    private final String desc;

    /**
     * The research effect of this research.
     */
    private final IResearchEffect effect;

    /**
     * The depth level in the tree.
     */
    private final int depth;

    /**
     * If the research has an only child.
     */
    private boolean onlyChild;

    /**
     * List of childs of a research.
     */
    private final List<String> childs = new ArrayList<>();

    /**
     * The requirement for this research.
     */
    private IResearchRequirement requirement;

    /**
     * Create the new research.
     * @param id it's id.
     * @param desc it's description text.
     * @param effect it's effect.
     * @param depth the depth in the tree.
     * @param branch the branch it is on.
     */
    public GlobalResearch(final String id, final String branch, final String desc, final int depth, final IResearchEffect effect)
    {
        this.id = id;
        this.desc = desc;
        this.effect = effect;
        this.depth = depth;
        this.branch = branch;
    }

    @Override
    public boolean canResearch(final int uni_level, @NotNull final LocalResearchTree localTree)
    {
        final IGlobalResearch parentResearch = GlobalResearchTree.researchTree.getResearch(branch, parent);
        final ILocalResearch localParentResearch = localTree.getResearch(parentResearch.getBranch(), parentResearch.getId());
        final ILocalResearch localResearch = localTree.getResearch(this.getBranch(), this.getId());

        return localResearch == null && canDisplay(uni_level) && localParentResearch != null && localParentResearch.getState() == ResearchState.FINISHED && (!parentResearch.hasResearchedChild(localTree) || !parentResearch.isOnlyChild());
    }

    @Override
    public boolean canDisplay(final int uni_level)
    {
        return uni_level >= depth;
    }

    @Override
    public void loadCostFromConfig()
    {
        costList.clear();
        try
        {
            final String[] researchCost = (String[]) MinecoloniesAPIProxy.getInstance().getConfig().getCommon().getClass().getField(id).get(new String[0]);
            for (final String cost : researchCost)
            {
                final String[] tuple = cost.split("/*");
                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tuple[0]));
                if (item == null)
                {
                    Log.getLogger().warn("Couldn't retrieve research costList from config for " + branch + "/" + id + " for item: " + tuple[0]);
                    return;
                }
                costList.add(new ItemStorage(new ItemStack(item, 1), Integer.parseInt(tuple[1]), false));
            }
        }
        catch (final NoSuchFieldException | IllegalAccessException | NumberFormatException e)
        {
            Log.getLogger().warn("Couldn't retrieve research costList from config for " + branch + "/" + id + " !", e);
        }
    }

    @Override
    public boolean hasEnoughResources(final IItemHandler inventory)
    {
        for (final ItemStorage cost: costList)
        {
            final int count = InventoryUtils.getItemCountInItemHandler(inventory, stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(cost.getItemStack()));
            if (count < cost.getAmount())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void startResearch(@NotNull final PlayerEntity player, @NotNull final LocalResearchTree localResearchTree)
    {
        if (localResearchTree.getResearch(this.branch, this.id) == null && hasEnoughResources(new InvWrapper(player.inventory)))
        {
            final ILocalResearch research = new LocalResearch(this.id, this.branch, this.depth);
            research.setState(ResearchState.IN_PROGRESS);
            localResearchTree.addResearch(branch, research);
        }
    }

    @Override
    public String getDesc()
    {
        return this.desc;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getParent()
    {
        return this.parent;
    }

    @Override
    public String getBranch()
    {
        return this.branch;
    }

    @Override
    public int getDepth()
    {
        return this.depth;
    }

    @Override
    public boolean isOnlyChild()
    {
        return onlyChild;
    }

    @Override
    public void setOnlyChild(final boolean onlyChild)
    {
        this.onlyChild = onlyChild;
    }

    @Override
    public boolean hasResearchedChild(@NotNull final LocalResearchTree localTree)
    {
        for (final String child: this.childs)
        {
            final IGlobalResearch childResearch = GlobalResearchTree.researchTree.getResearch(branch, child);
            final ILocalResearch localResearch = localTree.getResearch(childResearch.getBranch(), childResearch.getId());
            if (localResearch == null)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addChild(final IGlobalResearch child)
    {
        this.childs.add(child.getId());
        child.setParent(this.getId());
    }

    @Override
    public void setRequirement(final IResearchRequirement requirement)
    {
        this.requirement = requirement;
    }

    @Override
    public IResearchRequirement getResearchRequirement()
    {
        return this.requirement;
    }

    @Override
    public void setParent(final String id)
    {
        this.parent = id;
    }

    @Override
    public List<String> getChilds()
    {
        return new ArrayList<>(this.childs);
    }

    @Override
    public IResearchEffect getEffect()
    {
        return effect;
    }
}
