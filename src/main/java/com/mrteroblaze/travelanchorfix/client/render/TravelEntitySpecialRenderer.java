package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private final BatchingFontRenderer bfr;

    public TravelEntitySpecialRenderer() {
        BatchingFontRenderer tmp = null;
        try {
            tmp = new BatchingFontRenderer(Minecraft.getMinecraft().fontRenderer);
            System.out.println("[TravelAnchorFix] BatchingFontRenderer инициализирован");
        } catch (Throwable t) {
            System.out.println("[TravelAnchorFix] BatchingFontRenderer недоступен, fallback на FontRenderer");
        }
        this.bfr = tmp;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;

        // Подсветка рамкой (как раньше)
        IIcon overlay = getSelectedOverlayIcon();
        if (overlay != null) {
            RenderUtil.renderQuad2D(x, y, z, 1, 1, overlay);
        }

        // Рендер текста над якорем
        String toRender = anchor.getLabel();
        if (toRender != null && !toRender.isEmpty()) {
            System.out.println("[TravelAnchorFix] Рендер текста: \"" + toRender + "\" у якоря на " + x + "," + y + "," + z);
            renderFloatingText(toRender, (float) x, (float) y, (float) z);
        }
    }

    private void renderFloatingText(String text, float x, float y, float z) {
        FontRenderer fr = this.func_147498_b();
        if (fr == null) return;

        GL11.glPushMatrix();
        GL11.glTranslatef(x + 0.5F, y + 1.5F, z + 0.5F);

        // Поворот к игроку
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        int width = (bfr != null ? bfr.getStringWidth(text) : fr.getStringWidth(text)) / 2;
        System.out.println("[TravelAnchorFix] Ширина текста: " + width);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-width - 1, -1, 0.0D);
        tessellator.addVertex(-width - 1, 8, 0.0D);
        tessellator.addVertex(width + 1, 8, 0.0D);
        tessellator.addVertex(width + 1, -1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (bfr != null) {
            System.out.println("[TravelAnchorFix] Использую BatchingFontRenderer для текста");
            bfr.drawString(text, -width, 0, 0x80FFFFFF);
        } else {
            System.out.println("[TravelAnchorFix] Использую стандартный FontRenderer для текста");
            fr.drawString(text, -width, 0, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            fr.drawString(text, -width, 0, -1);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Exception e) {
            return null;
        }
    }
}
