package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class AnchorLabelRenderer extends TileEntitySpecialRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final FontRenderer fontRenderer = mc.fontRenderer;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (te == null || te.isInvalid()) {
            return;
        }

        // Тестовый текст, потом сюда можно будет подставить имя якоря
        String text = "Travel Anchor";

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

        // Billboard эффект (разворачиваем текст к игроку)
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        // Масштаб текста
        GL11.glScalef(-0.016F * 1.6F, -0.016F * 1.6F, 0.016F * 1.6F);

        // Фон (полупрозрачный прямоугольник)
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int width = fontRenderer.getStringWidth(text);
        int half = width / 2;

        Tessellator tess = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.5F);
        tess.addVertex(-half - 2, -2, 0.0D);
        tess.addVertex(-half - 2, 9, 0.0D);
        tess.addVertex(half + 2, 9, 0.0D);
        tess.addVertex(half + 2, -2, 0.0D);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Текст
        fontRenderer.drawString(text, -half, 0, 0xFFFFFFFF);

        // Восстановление GL-состояний
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }
}
