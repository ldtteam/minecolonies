package com.minecolonies.api.research.factories;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.*;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ResearchConstants.*;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing RecipeStorages.
 */
public class ResearchFactory implements IResearchFactory
{
    @NotNull
    @Override
    public TypeToken<IResearch> getFactoryOutputType()
    {
        return TypeConstants.RESEARCH;
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public IResearch getNewInstance(final String id, final String parent, final String branch, @NotNull final String desc, final int depth, final IResearchEffect effect)
    {
        return new Research(id, parent, branch, desc, depth, effect);
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final IResearch effect)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString(TAG_PARENT, effect.getParent());
        compound.setInteger(TAG_STATE, effect.getState().ordinal());
        compound.setString(TAG_ID, effect.getId());
        compound.setString(TAG_BRANCH, effect.getBranch());
        compound.setString(TAG_DESC, effect.getDesc());
        compound.setTag(TAG_EFFECT, StandardFactoryController.getInstance().serialize(effect));
        compound.setInteger(TAG_DEPTH, effect.getDepth());
        compound.setInteger(TAG_PROGRESS, effect.getProgress());
        compound.setBoolean(TAG_ONLY_CHILD, effect.isOnlyChild());

        @NotNull final NBTTagList childTagList = effect.getChilds().stream().map(child ->
                                                     {
                                                         final NBTTagCompound childCompound = new NBTTagCompound();
                                                         childCompound.setString(TAG_CHILD, child);
                                                         return childCompound;
                                                     }).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_CHILDS, childTagList);

        return compound;
    }

    @NotNull
    @Override
    public IResearch deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        final String parent = nbt.getString(TAG_PARENT);
        final int state = nbt.getInteger(TAG_STATE);
        final String id = nbt.getString(TAG_ID);
        final String branch = nbt.getString(TAG_BRANCH);
        final String desc = nbt.getString(TAG_DESC);
        final NBTTagCompound effect = (NBTTagCompound) nbt.getTag(TAG_EFFECT);
        final int depth = nbt.getInteger(TAG_DEPTH);
        final int progress = nbt.getInteger(TAG_PROGRESS);
        final boolean onlyChild = nbt.getBoolean(TAG_ONLY_CHILD);

        final IResearch research = getNewInstance(id, parent, branch, desc, depth, StandardFactoryController.getInstance().deserialize(effect));
        research.setState(ResearchState.values()[state]);
        research.setProgress(progress);
        research.loadCostFromConfig();
        research.setOnlyChild(onlyChild);

        NBTUtils.streamCompound(nbt.getTagList(TAG_CHILDS, Constants.NBT.TAG_COMPOUND)).forEach(compound -> research.addChild(compound.getString(TAG_CHILD)));
        return research;
    }
}
