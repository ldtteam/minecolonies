package com.minecolonies.blockout.controls;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import com.minecolonies.blockout.views.View;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Class which can be used to add text fields to a pane.
 */
public class TextField extends Pane
{
    /**
     * Texture resource location.
     */
    private static final ResourceLocation TEXTURE           = new ResourceLocation("textures/gui/widgets.png");
    private static final int RECT_COLOR = -3_092_272;
    private static final int DEFAULT_MAX_TEXT_LENGTH = 32;
    //  Attributes
    protected            int              maxTextLength     = DEFAULT_MAX_TEXT_LENGTH;
    protected            int              textColor         = 0xE0E0E0;
    protected            int              textColorDisabled = 0x707070;
    protected            boolean          shadow            = true;
    @Nullable
    protected            String           tabNextPaneID     = null;
    //  Runtime
    protected            String           text              = "";
    protected Filter filter;
    protected int cursorPosition     = 0;
    protected int scrollOffset       = 0;
    protected int selectionEnd       = 0;
    protected int cursorBlinkCounter = 0;

    /**
     * Simple public constructor to instantiate.
     */
    public TextField()
    {
        super();
        //Required
    }

    /**
     * Public constructor to instantiate the field with params.
     *
     * @param params the parameters for the textField.
     */
    public TextField(@NotNull final PaneParams params)
    {
        super(params);
        maxTextLength = params.getIntegerAttribute("maxlength", maxTextLength);
        textColor = params.getColorAttribute("color", textColor);
        textColorDisabled = params.getColorAttribute("colordisabled", textColorDisabled);
        shadow = params.getBooleanAttribute("shadow", shadow);
        text = params.getLocalizedStringAttribute("textContent", text);
        tabNextPaneID = params.getStringAttribute("tab", null);
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void setFilter(final Filter f)
    {
        filter = f;
    }

    public String getText()
    {
        return text;
    }

    public void setText(@NotNull final String s)
    {
        text = s.length() <= maxTextLength ? s : s.substring(0, maxTextLength);
        setCursorPosition(text.length());
    }

    public void setTextIgnoreLength(@NotNull final String s)
    {
        text = s;
        setCursorPosition(text.length());
    }

    public int getInternalWidth()
    {
        return getWidth();
    }

    public void setCursorPosition(final int pos)
    {
        cursorPosition = MathHelper.clamp(pos, 0, text.length());
        setSelectionEnd(cursorPosition);
    }

    /**
     * Move the cursor by an offset.
     *
     * @param offset the offset.
     */
    public void moveCursorBy(final int offset)
    {
        setCursorPosition(selectionEnd + offset);
    }

    public int getSelectionEnd()
    {
        return selectionEnd;
    }

    public void setSelectionEnd(final int pos)
    {
        selectionEnd = MathHelper.clamp(pos, 0, text.length());

        final int internalWidth = getInternalWidth();
        if (internalWidth > 0)
        {
            if (scrollOffset > text.length())
            {
                scrollOffset = text.length();
            }

            final String visibleString = mc.fontRenderer.trimStringToWidth(text.substring(scrollOffset), internalWidth);
            final int rightmostVisibleChar = visibleString.length() + scrollOffset;

            if (selectionEnd == scrollOffset)
            {
                scrollOffset -= mc.fontRenderer.trimStringToWidth(text, internalWidth, true).length();
            }

            if (selectionEnd > rightmostVisibleChar)
            {
                scrollOffset += selectionEnd - rightmostVisibleChar;
            }
            else if (selectionEnd <= scrollOffset)
            {
                scrollOffset -= scrollOffset - selectionEnd;
            }

            scrollOffset = MathHelper.clamp(scrollOffset, 0, text.length());
        }
    }

    @NotNull
    public String getSelectedText()
    {
        final int start = Math.min(cursorPosition, selectionEnd);
        final int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }

    /**
     * Handle key event.
     *
     * @param c   the character.
     * @param key the key.
     * @return if it should be processed or not.
     */
    private boolean handleKey(final char c, final int key)
    {
        switch (key)
        {
            case Keyboard.KEY_BACK:
            case Keyboard.KEY_DELETE:
                return handleDelete(key);

            case Keyboard.KEY_HOME:
            case Keyboard.KEY_END:
                return handleHomeEnd(key);

            case Keyboard.KEY_LEFT:
            case Keyboard.KEY_RIGHT:
                return handleArrowKeys(key);

            case Keyboard.KEY_TAB:
                return handleTab();

            default:
                return handleChar(c);
        }
    }

    private boolean handleChar(final char c)
    {
        if (filter.isAllowedCharacter(c))
        {
            writeText(Character.toString(c));
            return true;
        }
        return false;
    }

    private boolean handleTab()
    {
        if (tabNextPaneID != null)
        {
            final Pane next = getWindow().findPaneByID(tabNextPaneID);
            if (next != null)
            {
                next.setFocus();
            }
        }
        return true;
    }

    private boolean handleArrowKeys(final int key)
    {
        final int direction = (key == Keyboard.KEY_LEFT) ? -1 : 1;

        if (GuiScreen.isShiftKeyDown())
        {
            if (GuiScreen.isCtrlKeyDown())
            {
                setSelectionEnd(getNthWordFromPos(direction, getSelectionEnd()));
            }
            else
            {
                setSelectionEnd(getSelectionEnd() + direction);
            }
        }
        else if (GuiScreen.isCtrlKeyDown())
        {
            setCursorPosition(getNthWordFromCursor(direction));
        }
        else
        {
            moveCursorBy(direction);
        }
        return true;
    }

    private boolean handleHomeEnd(final int key)
    {
        final int position = (key == Keyboard.KEY_HOME) ? 0 : text.length();

        if (GuiScreen.isShiftKeyDown())
        {
            setSelectionEnd(position);
        }
        else
        {
            setCursorPosition(position);
        }
        return true;
    }

    private boolean handleDelete(final int key)
    {
        final int direction = (key == Keyboard.KEY_BACK) ? -1 : 1;

        if (GuiScreen.isCtrlKeyDown())
        {
            deleteWords(direction);
        }
        else
        {
            deleteFromCursor(direction);
        }

        return true;
    }

    @Override
    public void onFocus()
    {
        setCursorPosition(text.length());
        cursorBlinkCounter = 0;
    }

    /**
     * Draw itself at positions mx and my.
     */
    @Override
    public void drawSelf(final int mx, final int my)
    {
        final int color = enabled ? textColor : textColorDisabled;
        final int drawWidth = getInternalWidth();
        final int drawX = x;
        final int drawY = y;

        //  Determine the portion of the string that is visible on screen
        final String visibleString = mc.fontRenderer.trimStringToWidth(text.substring(scrollOffset), drawWidth);

        final int relativeCursorPosition = cursorPosition - scrollOffset;
        int relativeSelectionEnd = selectionEnd - scrollOffset;
        final boolean cursorVisible = relativeCursorPosition >= 0 && relativeCursorPosition <= visibleString.length();
        final boolean cursorBeforeEnd = cursorPosition < text.length() || text.length() >= maxTextLength;

        //  Enforce selection to the length limit of the visible string
        if (relativeSelectionEnd > visibleString.length())
        {
            relativeSelectionEnd = visibleString.length();
        }

        //  Draw string up through cursor
        int textX = drawX;
        if (visibleString.length() > 0)
        {
            @NotNull final String s1 = cursorVisible ? visibleString.substring(0, relativeCursorPosition) : visibleString;
            mc.renderEngine.bindTexture(TEXTURE);
            textX = mc.fontRenderer.drawString(s1, textX, drawY, color, shadow);
        }

        int cursorX = textX;
        if (!cursorVisible)
        {
            cursorX = relativeCursorPosition > 0 ? (drawX + width) : drawX;
        }
        else if (cursorBeforeEnd && shadow)
        {
            textX -= 1;
            cursorX -= 1;
        }

        //  Draw string after cursor
        if (visibleString.length() > 0 && cursorVisible && relativeCursorPosition < visibleString.length())
        {
            mc.renderEngine.bindTexture(TEXTURE);
            mc.fontRenderer.drawString(visibleString.substring(relativeCursorPosition), textX, drawY, color, shadow);
        }

        //  Should we draw the cursor this frame?
        if (isFocus() && cursorVisible && (cursorBlinkCounter / 6 % 2 == 0))
        {
            if (cursorBeforeEnd)
            {
                drawRect(cursorX, drawY - 1, cursorX + 1, drawY + 1 + mc.fontRenderer.FONT_HEIGHT, RECT_COLOR);
            }
            else
            {
                mc.renderEngine.bindTexture(TEXTURE);
                mc.fontRenderer.drawString("_", cursorX, drawY, color, shadow);
            }
        }

        //  Draw selection
        if (relativeSelectionEnd != relativeCursorPosition)
        {
            final int selectedDrawX = drawX + mc.fontRenderer.getStringWidth(visibleString.substring(0, relativeSelectionEnd));

            int selectionStartX = Math.min(cursorX, selectedDrawX - 1);
            int selectionEndX = Math.max(cursorX, selectedDrawX - 1);

            if (selectionStartX > (x + width))
            {
                selectionStartX = x + width;
            }

            if (selectionEndX > (x + width))
            {
                selectionEndX = x + width;
            }

            final Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableColorLogic();
            GlStateManager.colorLogicOp(GL11.GL_OR_REVERSE);
            final BufferBuilder vertexBuffer = tessellator.getBuffer();

            // There are several to choose from, look at DefaultVertexFormats for more info
            vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            //Since our points do not have any u,v this seems to be the correct code
            vertexBuffer.pos((double) selectionStartX, (double) drawY + 1 + mc.fontRenderer.FONT_HEIGHT, 0.0D).endVertex();
            vertexBuffer.pos((double) selectionEndX, (double) drawY + 1 + mc.fontRenderer.FONT_HEIGHT, 0.0D).endVertex();
            vertexBuffer.pos((double) selectionEndX, (double) drawY - 1, 0.0D).endVertex();
            vertexBuffer.pos((double) selectionStartX, (double) drawY - 1, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.disableColorLogic();
            GlStateManager.enableTexture2D();
        }
    }

    @Override
    public void putInside(final View view)
    {
        super.putInside(view);

        //  Recompute scroll offset
        setSelectionEnd(selectionEnd);
    }

    @Override
    public void handleClick(final int mx, final int my)
    {
        if (mx < 0)
        {
            return;
        }

        final String visibleString = mc.fontRenderer.trimStringToWidth(text.substring(scrollOffset), getInternalWidth());
        final String trimmedString = mc.fontRenderer.trimStringToWidth(visibleString, mx);

        // Cache and restore scrollOffset when we change focus via click,
        // because onFocus() sets the cursor (and thus scroll offset) to the end.
        final int oldScrollOffset = scrollOffset;
        setFocus();
        scrollOffset = oldScrollOffset;
        setCursorPosition(trimmedString.length() + scrollOffset);
    }

    @Override
    public boolean onKeyTyped(final char c, final int key)
    {
        switch (c)
        {
            case 1:
                setCursorPosition(text.length());
                setSelectionEnd(0);
                return true;

            case 3:
                GuiScreen.setClipboardString(getSelectedText());
                return true;

            case 22:
                writeText(GuiScreen.getClipboardString());
                return true;

            case 24:
                GuiScreen.setClipboardString(getSelectedText());
                writeText("");
                return true;

            default:
                return handleKey(c, key);
        }
    }

    @Override
    public void onUpdate()
    {
        cursorBlinkCounter++;
    }

    /**
     * Write text into the field.
     *
     * @param str the string to write.
     */
    public void writeText(final String str)
    {
        final String filteredStr = filter.filter(str);

        final int insertAt = Math.min(cursorPosition, selectionEnd);
        final int insertEnd = Math.max(cursorPosition, selectionEnd);
        final int availableChars = (maxTextLength - text.length()) + (insertEnd - insertAt);
        
        if (availableChars < 0)
        {
            return;
        }

        @NotNull final StringBuilder resultBuffer = new StringBuilder();
        if (text.length() > 0 && insertAt > 0)
        {
            resultBuffer.append(text.substring(0, insertAt));
        }

        final int insertedLength;
        if (availableChars < filteredStr.length())
        {
            resultBuffer.append(filteredStr.substring(0, availableChars));
            insertedLength = availableChars;
        }
        else
        {
            resultBuffer.append(filteredStr);
            insertedLength = filteredStr.length();
        }

        if (text.length() > 0 && insertEnd < text.length())
        {
            resultBuffer.append(text.substring(insertEnd));
        }

        text = resultBuffer.toString();
        moveCursorBy((insertAt - selectionEnd) + insertedLength);
    }

    /**
     * Delete an amount of words.
     *
     * @param count the amount.
     */
    public void deleteWords(final int count)
    {
        if (text.length() != 0)
        {
            if (selectionEnd != cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                deleteFromCursor(this.getNthWordFromCursor(count) - this.cursorPosition);
            }
        }
    }

    /**
     * Delete amount of words from cursor.
     *
     * @param count the amount.
     */
    public void deleteFromCursor(final int count)
    {
        if (text.length() == 0)
        {
            return;
        }

        if (selectionEnd != cursorPosition)
        {
            this.writeText("");
        }
        else
        {
            final boolean backwards = count < 0;
            final int start = backwards ? (this.cursorPosition + count) : this.cursorPosition;
            final int end = backwards ? this.cursorPosition : (this.cursorPosition + count);
            @NotNull String result = "";

            if (start > 0)
            {
                result = text.substring(0, start);
            }

            if (end < text.length())
            {
                result = result + text.substring(end);
            }

            text = result;

            if (backwards)
            {
                this.moveCursorBy(count);
            }
        }
    }

    /**
     * Get the n'th word from a position.
     *
     * @param count the n.
     * @param pos   the position.
     * @return the length of the word.
     */
    public int getNthWordFromPos(final int count, final int pos)
    {
        final boolean reverse = count < 0;
        int position = pos;

        for (int i1 = 0; i1 < Math.abs(count); ++i1)
        {
            if (reverse)
            {
                while (position > 0 && text.charAt(position - 1) == ' ')
                {
                    --position;
                }
                while (position > 0 && text.charAt(position - 1) != ' ')
                {
                    --position;
                }
            }
            else
            {
                position = text.indexOf(' ', position);

                if (position == -1)
                {
                    position = text.length();
                }
                else
                {
                    while (position < text.length() && text.charAt(position) == ' ')
                    {
                        ++position;
                    }
                }
            }
        }

        return position;
    }

    /**
     * Get n'th word from cursor position.
     *
     * @param count the n.
     * @return the length.
     */
    public int getNthWordFromCursor(final int count)
    {
        return getNthWordFromPos(count, cursorPosition);
    }

    /**
     * Interface to filter words.
     */
    public interface Filter
    {
        /**
         * Apply the filter.
         *
         * @param s to the string.
         * @return the correct String.
         */
        String filter(String s);

        /**
         * Check if character is allowed.
         *
         * @param c character to test.
         * @return true if so.
         */
        boolean isAllowedCharacter(char c);
    }
}
