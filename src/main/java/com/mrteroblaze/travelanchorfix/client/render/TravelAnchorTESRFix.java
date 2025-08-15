package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;

import org.lwjgl.opengl.GL11;

import crazypants.enderio.config.Config;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim()
            .isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        double distSq = mc.thePlayer.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);

        // Дальность отображения как в конфиге
        if (distSq > Config.travelAnchorMaxDistance * Config.travelAnchorMaxDistance) return;

        // Проверяем, что игрок действительно навёлся на этот якорь
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.blockX != te.xCoord || mop.blockY != te.yCoord || mop.blockZ != te.zCoord) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        // Поворот к игроку
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        FontRenderer fontrenderer = this.func_147498_b();
        int width = fontrenderer.getStringWidth(name) / 2;

        // Фон рамки
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.25F);
        GL11.glVertex3f(-width - 1, -1, 0.0F);
        GL11.glVertex3f(-width - 1, 8, 0.0F);
        GL11.glVertex3f(width + 1, 8, 0.0F);
        GL11.glVertex3f(width + 1, -1, 0.0F);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Текст
        fontrenderer.drawString(name, -width, 0, 553648127);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontrenderer.drawString(name, -width, 0, -1);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}
