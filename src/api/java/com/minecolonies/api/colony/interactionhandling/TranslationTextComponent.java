package com.minecolonies.api.colony.interactionhandling;

import net.minecraft.util.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Custom translation component with fixed hashCode.
 */
public class TranslationTextComponent extends TextComponentTranslation
{
    /**
     * The translation contructor.
     *
     * @param key  the key.
     * @param args possible additional args.
     */
    public TranslationTextComponent(String key, Object... args)
    {
        super(key, args);
    }

    @NotNull
    @Override
    public TextComponentTranslation createCopy()
    {
        Object[] aobject = new Object[this.getFormatArgs().length];

        for (int i = 0; i < this.getFormatArgs().length; ++i)
        {
            if (this.getFormatArgs()[i] instanceof ITextComponent)
            {
                aobject[i] = ((ITextComponent) this.getFormatArgs()[i]).createCopy();
            }
            else
            {
                aobject[i] = this.getFormatArgs()[i];
            }
        }

        TextComponentTranslation textcomponenttranslation = new TranslationTextComponent(this.getKey(), aobject);
        textcomponenttranslation.setStyle(this.getStyle().createShallowCopy());

        for (final ITextComponent itextcomponent : this.getSiblings())
        {
            textcomponenttranslation.appendSibling(itextcomponent.createCopy());
        }

        return textcomponenttranslation;
    }

    @Override
    public int hashCode()
    {
        int i = 31 * this.siblings.hashCode();
        i = 31 * i + this.getKey().hashCode();
        i = 31 * i + Arrays.hashCode(this.getFormatArgs());
        return i;
    }
}
