package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.AbstractResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The unlock building research effect, it returns true if unlocked and else false.
 */
public class UnlockBuildingResearchEffect extends AbstractResearchEffect<Boolean>
{
    /**
     * Whether the effect has been unlocked or not.
     */
    private boolean unlocked;

    /**
     * The constructor to create a new unlock research effect.
     * 
     * @param id       the id to unlock.
     * @param unlocked if unlocked or locked.
     */
    public UnlockBuildingResearchEffect(final String id, final boolean unlocked)
    {
        super(id);
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
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent("com.minecolonies.coremod.research.effect.unlock", this.getId());
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return false;
    }
}
