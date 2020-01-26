package com.minecolonies.api.research;

import net.minecraft.util.text.TranslationTextComponent;

/**
 * The unlock research effect, it returns true if unlocked and else false.
 */
public class UnlockResearchEffect implements IResearchEffect<Boolean>
{
    /**
     * The String id of the research effect.
     */
    private final String id;

    /**
     * Whether the effect has been unlocked or not.
     */
    private boolean unlocked;

    /**
     * The constructor to create a new unlock research effect.
     * @param id the id to unlock.
     * @param unlocked if unlocked or locked.
     */
    public UnlockResearchEffect(final String id, final boolean unlocked)
    {
        this.id = id;
        this.unlocked = unlocked;
    }

    @Override
    public Boolean getEffect()
    {
        return this.unlocked;
    }

    @Override
    public void setEffect(final Boolean effect)
    {
        this.unlocked = effect;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent("com.minecolonies.coremod.research.effect.unlock", id);
    }
}
