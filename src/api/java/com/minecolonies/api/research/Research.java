package com.minecolonies.api.research;

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

import static com.minecolonies.api.research.ResearchConstants.*;

/**
 * The implementation of the IResearch interface which represents one type of research.
 */
public class Research implements IResearch
{
    /**
     * The costList of the research.
     */
    private final List<ItemStorage> costList   = new ArrayList<>();

    /**
     * The parent research which has to be completed first.
     */
    private final String parent;

    /**
     * The current research state.
     */
    private ResearchState state;

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
     * The progress of the research.
     */
    private int progress;

    /**
     * If the research has an only child.
     */
    private boolean onlyChild;

    /**
     * List of childs of a research.
     */
    private final List<String> childs = new ArrayList<>();

    /**
     * Create the new research.
     * @param id it's id.
     * @param parent the parent id of it.
     * @param desc it's description text.
     * @param effect it's effect.
     * @param depth the depth in the tree.
     * @param branch the branch it is on.
     */
    public Research(final String id, final String parent, final String branch, final String desc, final int depth, final IResearchEffect effect)
    {
        this.parent = parent;
        this.id = id;
        this.desc = desc;
        this.effect = effect;
        this.depth = depth;
        this.branch = branch;
    }

    @Override
    public boolean canResearch(final int uni_level)
    {
        final IResearch parentResearch = GlobalResearchTree.researchTree.getResearch(branch, parent);
        return state == ResearchState.NOT_STARTED && canDisplay(uni_level) && parentResearch != null && parentResearch.getState() == ResearchState.FINISHED && (!parentResearch.hasResearchedChild() || !parentResearch.isOnlyChild());
    }

    @Override
    public boolean canDisplay(final int uni_level)
    {
        final IResearch parentResearch = GlobalResearchTree.researchTree.getResearch(branch, parent);
        return uni_level >= depth && (!parentResearch.hasResearchedChild() || !parentResearch.isOnlyChild());
    }

    @Override
    public void loadCostFromConfig()
    {
        costList.clear();
        try
        {
            final String[] researchCost = (String[]) ResearchConfiguration.class.getField(branch).getClass().getField(id).get(new String[0]);
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
    public void startResearch(@NotNull final PlayerEntity player)
    {
        if (state == ResearchState.NOT_STARTED && hasEnoughResources(new InvWrapper(player.inventory)))
        {
            state = ResearchState.IN_PROGRESS;
        }
    }

    @Override
    public void research(final ResearchEffects effects, final ResearchTree tree)
    {
        if (state == ResearchState.IN_PROGRESS)
        {
            progress++;
            if (progress >= BASE_RESEARCH_TIME * depth)
            {
                state = ResearchState.FINISHED;
                effects.applyEffect(this.effect);
            }
        }
    }

    @Override
    public int getProgress()
    {
        return (BASE_RESEARCH_TIME * depth)/progress;
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
    public ResearchState getState()
    {
        return this.state;
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
    public void setState(final ResearchState value)
    {
        this.state = value;
    }

    @Override
    public void setProgress(final int progress)
    {
        this.progress = progress;
    }

    @Override
    public IResearch copy()
    {
        return new Research(this.id, this.parent, this.branch, this.desc, this.depth, this.effect);
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
    public boolean hasResearchedChild()
    {
        for (final String child: this.childs)
        {
            final IResearch childResearch = GlobalResearchTree.researchTree.getResearch(branch, child);
            if (childResearch != null && childResearch.getState() != ResearchState.NOT_STARTED)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addChild(final String child)
    {
        this.childs.add(child);
    }

    @Override
    public List<String> getChilds()
    {
        return new ArrayList<>(this.childs);
    }
}
