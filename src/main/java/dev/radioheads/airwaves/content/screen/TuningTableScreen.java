package dev.radioheads.airwaves.content.screen;

import dev.radioheads.airwaves.content.menu.TuningTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TuningTableScreen extends AbstractContainerScreen<TuningTableMenu> {
    private static final int FRAME = -13026227;
    private static final int FRAME_HI = -11315349;
    private static final int BODY = -14013130;
    private static final int TEXT = -1644050;
    private static final int DIM = -7695456;

    public TuningTableScreen(TuningTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 242;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x - 2, y - 2, x + 258, y + 244, -15987440);
        guiGraphics.fill(x, y, x + 256, y + 242, FRAME);
        guiGraphics.fill(x, y, x + 256, y + 1, FRAME_HI);
        guiGraphics.fill(x, y, x + 1, y + 242, FRAME_HI);
        guiGraphics.fill(x + 3, y + 3, x + 253, y + 239, BODY);

        guiGraphics.drawString(this.font, "TUNING TABLE", x + 10, y + 7, TEXT, false);
        guiGraphics.drawString(this.font, "ITEM", x + 9, y + 21, DIM, false);

        for (Slot slot : this.menu.slots) {
            this.inset(guiGraphics, x + slot.x, y + slot.y, 16, 16);
        }

        guiGraphics.drawString(this.font, "Inventory", x + 47, y + 150, DIM, false);
    }

    private void inset(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, -15987440);
        graphics.fill(x, y, x + w, y + h, -11907241);
        graphics.fill(x, y + h, x + w + 1, y + h + 1, -11907490);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }
}
