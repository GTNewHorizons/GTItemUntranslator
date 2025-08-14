package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.config.Config;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;
        TileTravelAnchor ta = (TileTravelAnchor) te;

        String label = ta.getLabel();
        if (label == null || label.isEmpty()) return;

        double distSq = Minecraft.getMinecraft().thePlayer.getDistanceSq(
            ta.xCoord + 0.5, ta.yCoord + 0.5, ta.zCoord + 0.5
        );

        if (distSq > Config.travelAnchorMaxDistanceSq) return;

        renderLabel(label, x + 0.5, y + 1.5, z + 0.5);
    }

    private void renderLabel(String text, double x, double y, double z) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        Tessellator tess = Tessellator.instance;
        int width = fr.getStringWidth(text) / 2;

        // Фон
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tess.addVertex(-width - 1, -1, 0.0D);
        tess.addVertex(-width - 1, 8, 0.0D);
        tess.addVertex(width + 1, 8, 0.0D);
        tess.addVertex(width + 1, -1, 0.0D);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Текст
        fr.drawString(text, -width, 0, 0xFFFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }
}
