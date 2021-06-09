package com.minecolonies.coremod.util.text;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

public class NonSiblingFormattingTextComponent extends StringTextComponent
{
    public NonSiblingFormattingTextComponent()
    {
        super("");
    }

    @NotNull
    @Override
    public String getString()
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (ITextComponent itextcomponent : this.siblings)
        {
            String s = itextcomponent.getString();

            if (!s.isEmpty())
            {
                stringbuilder.append(s);
            }
        }

        stringbuilder.append(TextFormatting.RESET);

        return stringbuilder.toString();
    }
}
