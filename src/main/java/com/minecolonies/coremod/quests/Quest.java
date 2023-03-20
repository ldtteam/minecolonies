package com.minecolonies.coremod.quests;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.quests.type.IQuestType;
import com.minecolonies.coremod.quests.type.sideeffects.IQuestSideEffect;
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
    private final List<IQuestSideEffect> activeEffects = new ArrayList<>();

    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The giver of this quest
     */
    private final ICitizenData questGiver;

    /**
     * The id of this quest
     */
    private int questID;

    // We want two different things. a) A global "quest holder" kind of thing that has all the fields that we read from the json (e.g. the necessary triggers, side effects, etc)
    // and b) A local ColonyQuest that has the state like the quest giver + a reference to the global quest to query the other things.

    protected Quest(final int questID, final IQuestType type, final IColony colony, final ICitizenData questGiver)
    {
        this.type = type;
        this.colony = colony;
        this.questGiver = questGiver;
        this.questID = questID;
    }

    @Override
    public void onEffectComplete(final IQuestSideEffect effect)
    {
        // do tracking and rewards last
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public ICitizenData getQuestGiver()
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
        for (final IQuestSideEffect effect : activeEffects)
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