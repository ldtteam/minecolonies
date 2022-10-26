package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IQuestGiver;
import com.minecolonies.coremod.quests.type.IQuestType;
import com.minecolonies.coremod.quests.type.effects.IQuestEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance of a specific quest type
 */
public class Quest implements IQuest
{
    /**
     * Type reference
     */
    private IQuestType type;

    /**
     * Current effects
     */
    private final List<IQuestEffect> activeEffects = new ArrayList<>();

    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The giver of this quest
     */
    private final IQuestGiver questGiver;

    /**
     * The id of this quest
     */
    private int questID;

    protected Quest(final int questID, final IQuestType type, final IColony colony, final IQuestGiver questGiver)
    {
        this.type = type;
        this.colony = colony;
        this.questGiver = questGiver;
        this.questID = questID;
    }

    @Override
    public void onEffectComplete(final IQuestEffect effect)
    {

    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public IQuestGiver getQuestGiver()
    {
        return questGiver;
    }

    @Override
    public void onStart(final Player player)
    {
        activeEffects.addAll(type.createEffectsFor(this));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putInt("id", questID);
        compoundNBT.putString("type", type.getID().toString());
        final ListTag listNBT = new ListTag();
        for (final IQuestEffect effect : activeEffects)
        {
            final CompoundTag compoundNBT1 = new CompoundTag();
            compoundNBT.putString("", effect.getID().toString());
            listNBT.add(effect.serializeNBT());
        }

        compoundNBT.put("", listNBT);

        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        questID = nbt.getInt("id");
        // TODO: get type from reg type = nbt.getString("type");
        // TODO: create effects from res loc/read nbt
    }
}