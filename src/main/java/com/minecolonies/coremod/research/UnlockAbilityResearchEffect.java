package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.AbstractResearchEffect;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * The unlock ability research effect, it returns true if unlocked and else false.
 */
public class UnlockAbilityResearchEffect extends AbstractResearchEffect<Boolean>
{
    /**
     * Whether the effect has been unlocked or not.
     */
    private int level;

    /**
     * The constructor to create a new unlock research effect.
     *
     * @param id                  the id to unlock.
     * @param level               greater than zero if unlocked.
     */
    public UnlockAbilityResearchEffect(final String id, final int level)
    {
        super(id);
        this.level = level;
    }

    @Override
    public Boolean getEffect()
    {
        return (this.level > 0);
    }

    @Override
    public void setEffect(final Boolean effect)
    {
        this.level = (effect ? 1 : 0);
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent(TranslationConstants.RESEARCH_EFFECTS + this.getId() + ".description");
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return false;
    }
}
