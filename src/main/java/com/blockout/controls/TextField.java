package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.blockout.View;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class TextField extends Pane
{
    /**
     * Texture resource location
     */
    private static final ResourceLocation TEXTURE           = new ResourceLocation("textures/gui/widgets.png");
    //  Attributes
    protected            int              maxTextLength     = 32;
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

    public TextField()
    {
        //Required
    }

    public TextField(@NotNull PaneParams params)
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

    public void setFilter(Filter f)
    {
        filter = f;
    }

    public String getText()
    {
        return text;
    }

    public void setText(@NotNull String s)
    {
        text = s.length() <= maxTextLength ? s : s.substring(0, maxTextLength);
        setCursorPosition(text.length());
    }

    public int getInternalWidth()
    {
        return getWidth();
    }

    public int getMaxTextLength()
    {
        return maxTextLength;
    }

    public void setMaxTextLength(int m)
    {
        maxTextLength = m;
    }

    public int getTextColor()
    {
        return textColor;
    }

    public void setTextColor(int c)
    {
        textColor = c;
    }

    public int getTextColorDisabled()
    {
        return textColorDisabled;
    }

    public void setTextColorDisabled(int c)
    {
        textColorDisabled = c;
    }

    @Nullable
    public String getTabNextPaneID()
    {
        return tabNextPaneID;
    }

    public void setTabNextPaneID(String nextID)
    {
        tabNextPaneID = nextID;
    }

    public int getCursorPosition()
    {
        return cursorPosition;
    }

    public void setCursorPosition(int pos)
    {
        cursorPosition = MathHelper.clamp_int(pos, 0, text.length());
        setSelectionEnd(cursorPosition);
    }

    public void moveCursorBy(int offset)
    {
        setCursorPosition(selectionEnd + offset);
    }

    public int getSelectionEnd()
    {
        return selectionEnd;
    }

    public void setSelectionEnd(int pos)
    {
        selectionEnd = MathHelper.clamp_int(pos, 0, text.length());

        int internalWidth = getInternalWidth();
        if (internalWidth > 0)
        {
            if (scrollOffset > text.length())
            {
                scrollOffset = text.length();
            }

            String visibleString = mc.fontRendererObj.trimStringToWidth(text.substring(scrollOffset), internalWidth);
            int rightmostVisibleChar = visibleString.length() + scrollOffset;

            if (selectionEnd == scrollOffset)
            {
                scrollOffset -= mc.fontRendererObj.trimStringToWidth(text, internalWidth, true).length();
            }

            if (selectionEnd > rightmostVisibleChar)
            {
                scrollOffset += selectionEnd - rightmostVisibleChar;
            }
            else if (selectionEnd <= scrollOffset)
            {
                scrollOffset -= scrollOffset - selectionEnd;
            }

            scrollOffset = MathHelper.clamp_int(scrollOffset, 0, text.length());
        }
    }

    @NotNull
    public String getSelectedText()
    {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }

    private boolean handleKey(char c, int key)
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

    private boolean handleChar(char c)
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
            Pane next = getWindow().findPaneByID(tabNextPaneID);
            if (next != null)
            {
                next.setFocus();
            }
        }
        return true;
    }

    private boolean handleArrowKeys(int key)
    {
        int direction = (key == Keyboard.KEY_LEFT) ? -1 : 1;

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

    private boolean handleHomeEnd(int key)
    {
        int position = (key == Keyboard.KEY_HOME) ? 0 : text.length();

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

    private boolean handleDelete(int key)
    {
        int direction = (key == Keyboard.KEY_BACK) ? -1 : 1;

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

    @Override
    protected void drawSelf(int mx, int my)
    {
        int color = enabled ? textColor : textColorDisabled;
        int drawWidth = getInternalWidth();
        int drawX = x;
        int drawY = y;

        //  Determine the portion of the string that is visible on screen
        String visibleString = mc.fontRendererObj.trimStringToWidth(text.substring(scrollOffset), drawWidth);

        int relativeCursorPosition = cursorPosition - scrollOffset;
        int relativeSelectionEnd = selectionEnd - scrollOffset;
        boolean cursorVisible = relativeCursorPosition >= 0 && relativeCursorPosition <= visibleString.length();
        boolean cursorBeforeEnd = cursorPosition < text.length() || text.length() >= maxTextLength;

        //  Enforce selection to the length limit of the visible string
        if (relativeSelectionEnd > visibleString.length())
        {
            relativeSelectionEnd = visibleString.length();
        }

        //  Draw string up through cursor
        int textX = drawX;
        if (visibleString.length() > 0)
        {
            @NotNull String s1 = cursorVisible ? visibleString.substring(0, relativeCursorPosition) : visibleString;
            mc.renderEngine.bindTexture(TEXTURE);
            textX = mc.fontRendererObj.drawString(s1, textX, drawY, color, shadow);
        }

        int cursorX = textX;
        if (!cursorVisible)
        {
            cursorX = relativeCursorPosition > 0 ? (drawX + width) : drawX;
        }
        else if (cursorBeforeEnd)
        {
            if (shadow)
            {
                textX -= 1;
            }
            cursorX = textX;
        }

        //  Draw string after cursor
        if (visibleString.length() > 0 && cursorVisible && relativeCursorPosition < visibleString.length())
        {
            mc.renderEngine.bindTexture(TEXTURE);
            mc.fontRendererObj.drawString(visibleString.substring(relativeCursorPosition), textX, drawY, color, shadow);
        }

        //  Should we draw the cursor this frame?
        if (isFocus() && cursorVisible && (cursorBlinkCounter / 6 % 2 == 0))
        {
            if (cursorBeforeEnd)
            {
                drawRect(cursorX, drawY - 1, cursorX + 1, drawY + 1 + mc.fontRendererObj.FONT_HEIGHT, -3092272);
            }
            else
            {
                mc.renderEngine.bindTexture(TEXTURE);
                mc.fontRendererObj.drawString("_", cursorX, drawY, color, shadow);
            }
        }

        //  Draw selection
        if (relativeSelectionEnd != relativeCursorPosition)
        {
            int selectedDrawX = drawX + mc.fontRendererObj.getStringWidth(visibleString.substring(0, relativeSelectionEnd));

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

            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.enableColorLogic();
            GlStateManager.colorLogicOp(GL11.GL_OR_REVERSE);
            VertexBuffer VertexBuffer = tessellator.getBuffer();

            // There are several to choose from, look at DefaultVertexFormats for more info
            //todo may need to choose a different Format
            VertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            //Since our points do not have any u,v this seems to be the correct code
            VertexBuffer.pos((double) selectionStartX, (double) drawY + 1 + mc.fontRendererObj.FONT_HEIGHT, 0.0D).endVertex();
            VertexBuffer.pos((double) selectionEndX, (double) drawY + 1 + mc.fontRendererObj.FONT_HEIGHT, 0.0D).endVertex();
            VertexBuffer.pos((double) selectionEndX, (double) drawY - 1, 0.0D).endVertex();
            VertexBuffer.pos((double) selectionStartX, (double) drawY - 1, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.disableColorLogic();
            GlStateManager.enableTexture2D();
        }
    }

    @Override
    public void putInside(View view)
    {
        super.putInside(view);

        //  Recompute scroll offset
        setSelectionEnd(selectionEnd);
    }

    @Override
    public void handleClick(int mx, int my)
    {
        if (mx < 0)
        {
            return;
        }

        String visibleString = mc.fontRendererObj.trimStringToWidth(text.substring(scrollOffset), getInternalWidth());
        String trimmedString = mc.fontRendererObj.trimStringToWidth(visibleString, mx);

        // Cache and restore scrollOffset when we change focus via click,
        // because onFocus() sets the cursor (and thus scroll offset) to the end
        int oldScrollOffset = scrollOffset;
        setFocus();
        scrollOffset = oldScrollOffset;
        setCursorPosition(trimmedString.length() + scrollOffset);
    }

    @Override
    public boolean onKeyTyped(char c, int key)
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

    public void writeText(String str)
    {
        String filteredStr = filter.filter(str);

        int insertAt = Math.min(cursorPosition, selectionEnd);
        int insertEnd = Math.max(cursorPosition, selectionEnd);
        int availableChars = (maxTextLength - text.length()) + (insertEnd - insertAt);

        @NotNull String result = "";
        if (text.length() > 0 && insertAt > 0)
        {
            result = text.substring(0, insertAt);
        }

        int insertedLength;
        if (availableChars < filteredStr.length())
        {
            result = result + filteredStr.substring(0, availableChars);
            insertedLength = availableChars;
        }
        else
        {
            result = result + filteredStr;
            insertedLength = filteredStr.length();
        }

        if (text.length() > 0 && insertEnd < text.length())
        {
            result = result + text.substring(insertEnd);
        }

        text = result;
        moveCursorBy((insertAt - selectionEnd) + insertedLength);
    }

    public void deleteWords(int count)
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

    public void deleteFromCursor(int count)
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
            boolean backwards = count < 0;
            int start = backwards ? (this.cursorPosition + count) : this.cursorPosition;
            int end = backwards ? this.cursorPosition : (this.cursorPosition + count);
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

    public int getNthWordFromPos(int count, int pos)
    {
        boolean reverse = count < 0;
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

    public int getNthWordFromCursor(int count)
    {
        return getNthWordFromPos(count, cursorPosition);
    }

    public interface Filter
    {
        String filter(String s);

        boolean isAllowedCharacter(char c);
    }
}
