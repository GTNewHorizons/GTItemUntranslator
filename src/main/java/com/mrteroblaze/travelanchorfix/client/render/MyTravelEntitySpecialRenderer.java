package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class MyTravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        boolean oldUnicode = fr.getUnicodeFlag();
        fr.setUnicodeFlag(true); // включаем Unicode для устранения квадратиков

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);

        // Используем SRG-имена для 1.7.10
        TileEntityRendererDispatcher renderer = TileEntityRendererDispatcher.instance;
        GL11.glRotatef(-renderer.field_147547_x, 0.0F, 1.0F, 0.0F); // yaw
        GL11.glRotatef(renderer.field_147546_y, 1.0F, 0.0F, 0.0F);  // pitch

        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int textWidth = fr.getStringWidth(name);
        fr.drawString(name, -textWidth / 2, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        fr.setUnicodeFlag(oldUnicode); // возвращаем старый флаг
    }
}
