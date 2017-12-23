package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

/**
 * Deliverable that can only be fulfilled by a single stack with a given minimal amount of items.
 */
public class Stack implements IDeliverable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STACK       = "Stack";
    private static final String NBT_MATCHMETA   = "MatchMeta";
    private static final String NBT_MATCHNBT    = "MatchNBT";
    private static final String NBT_MATCHOREDIC = "MatchOreDic";
    private static final String NBT_RESULT      = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ItemStack stack;

    @NotNull
    private boolean matchMeta = false;

    @NotNull
    private boolean matchNBT = false;

    @NotNull
    private boolean matchOreDic = false;

    @NotNull
    private ItemStack result = ItemStackUtils.EMPTY;

    public Stack(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            throw new IllegalArgumentException("Cannot deliver Empty Stack.");
        }

        setMatchMeta(true).setMatchNBT(true);

        this.stack = stack.copy();
        this.stack.setCount(Math.min(this.stack.getCount(), this.stack.getMaxStackSize()));
    }

    public Stack setMatchNBT(final boolean match)
    {
        this.matchNBT = match;
        return this;
    }

    public Stack setMatchMeta(final boolean match)
    {
        this.matchMeta = match;
        return this;
    }

    public Stack(
                  @NotNull final ItemStack stack,
                  @NotNull final boolean matchMeta,
                  @NotNull final boolean matchNBT,
                  @NotNull final boolean matchOreDic,
                  @NotNull final ItemStack result)
    {
        this.stack = stack;
        this.matchMeta = matchMeta;
        this.matchNBT = matchNBT;
        this.matchOreDic = matchOreDic;
        this.result = result;
    }

    public static NBTTagCompound serialize(IFactoryController controller, Stack input)
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_STACK, input.stack.serializeNBT());
        compound.setBoolean(NBT_MATCHMETA, input.matchMeta);
        compound.setBoolean(NBT_MATCHNBT, input.matchNBT);
        compound.setBoolean(NBT_MATCHOREDIC, input.matchOreDic);

        if (!ItemStackUtils.isEmpty(input.result))
        {
            compound.setTag(NBT_RESULT, input.result.serializeNBT());
        }

        return compound;
    }

    public static Stack deserialize(IFactoryController controller, NBTTagCompound compound)
    {
        ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_STACK));
        boolean matchMeta = compound.getBoolean(NBT_MATCHMETA);
        boolean matchNBT = compound.getBoolean(NBT_MATCHNBT);
        boolean matchOreDic = compound.getBoolean(NBT_MATCHOREDIC);
        ItemStack result = compound.hasKey(NBT_RESULT) ? ItemStackUtils.deserializeFromNBT(compound.getCompoundTag(NBT_RESULT)) : ItemStackUtils.EMPTY;

        return new Stack(stack, matchMeta, matchNBT, matchOreDic, result);
    }

    @Override
    public boolean matches(@NotNull final ItemStack stack)
    {
        if (matchOreDic)
        {
            return OreDictionary.itemMatches(getStack(), stack, matchMeta) && getCount() <= stack.getCount();
        }

        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), stack, matchMeta, matchNBT);
    }

    @Override
    public int getCount()
    {
        return stack.getCount();
    }

    @NotNull
    public ItemStack getStack()
    {
        return stack;
    }    @Override
    public void setResult(@NotNull final ItemStack result)
    {
        this.result = result;
    }

    public Stack setMatchOreDic(final boolean match)
    {
        this.matchOreDic = match;
        return this;
    }    @NotNull
    @Override
    public ItemStack getResult()
    {
        return result;
    }




}

