package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.MovingObjectPosition;

import org.lwjgl.opengl.GL11;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.config.Config;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        double distSq = mc.thePlayer.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);

        double maxDistSq = Config.travelAnchorMaxDistance * Config.travelAnchorMaxDistance;
        if (distSq > maxDistSq) return;

        // Показываем только при наведении
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.blockX != te.xCoord || mop.blockY != te.yCoord || mop.blockZ != te.zCoord) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        // Разворачиваем к игроку
        RenderManager renderManager = RenderManager.instance;
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.016F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Рамка (фон)
        FontRenderer fontrenderer = this.getFontRenderer();
        int stringWidth = fontrenderer.getStringWidth(name) / 2;
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-stringWidth - 2, -1, 0.0D);
        tessellator.addVertex(-stringWidth - 2, 8, 0.0D);
        tessellator.addVertex(stringWidth + 2, 8, 0.0D);
        tessellator.addVertex(stringWidth + 2, -1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Текст
        fontrenderer.drawString(EnumChatFormatting.WHITE + name, -stringWidth, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
