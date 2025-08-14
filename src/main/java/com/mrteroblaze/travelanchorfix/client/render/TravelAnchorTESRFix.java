package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.config.Config;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        double distSq = mc.thePlayer.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);

        // Проверка на дистанцию (в квадрате)
        if (distSq > Config.travelAnchorMaxDistance * Config.travelAnchorMaxDistance) {
            return;
        }

        // Проверка — только если мы наводимся на этот блок
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.blockX != te.xCoord || mop.blockY != te.yCoord || mop.blockZ != te.zCoord) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.renderViewEntity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.renderViewEntity.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        FontRenderer fontrenderer = this.func_147498_b(); // getFontRenderer() для 1.7.10
        int strWidth = fontrenderer.getStringWidth(name) / 2;

        // Рамка (фон)
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glTranslatef(0.0F, 0.0F, 0.01F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
        GL11.glVertex3f(-strWidth - 2, -2, 0.0F);
        GL11.glVertex3f(-strWidth - 2, 8, 0.0F);
        GL11.glVertex3f(strWidth + 2, 8, 0.0F);
        GL11.glVertex3f(strWidth + 2, -2, 0.0F);
        GL11.glEnd();

        // Сам текст
        fontrenderer.drawString(EnumChatFormatting.WHITE + name, -strWidth, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
