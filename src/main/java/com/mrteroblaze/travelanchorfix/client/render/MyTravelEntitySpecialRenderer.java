package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class MyTravelEntitySpecialRenderer extends TileEntitySpecialRenderer<TileTravelAnchor> {

    @Override
    public void renderTileEntityAt(TileTravelAnchor te, double x, double y, double z, float partialTick) {
        String name = te.getLabel();
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        boolean oldUnicode = fr.getUnicodeFlag();
        fr.setUnicodeFlag(true); // включаем Unicode для устранения квадратиков

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);

        // Поворот к игроку (MC 1.7.10)
        GL11.glRotatef(-this.func_147498_b().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(this.func_147498_b().playerViewX, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int textWidth = fr.getStringWidth(name);
        fr.drawString(name, -textWidth / 2, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        fr.setUnicodeFlag(oldUnicode); // возвращаем флаг
    }
}
