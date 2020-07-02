package com.minecolonies.coremod.util.text;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NonSiblingFormattingTextComponent extends StringTextComponent
{
    public NonSiblingFormattingTextComponent()
    {
        super("");
    }

    @Override
    public String getFormattedText()
    {
        StringBuilder stringbuilder = new StringBuilder();

        stringbuilder.append(this.getStyle().getFormattingCode());

        for (ITextComponent itextcomponent : this)
        {
            String s = itextcomponent.getUnformattedComponentText();

            if (!s.isEmpty())
            {
                stringbuilder.append(s);
            }
        }

        stringbuilder.append(TextFormatting.RESET);

        return stringbuilder.toString();
    }
}
