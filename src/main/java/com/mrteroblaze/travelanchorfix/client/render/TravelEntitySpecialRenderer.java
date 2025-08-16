package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;


public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private final BatchingFontRenderer bfr;

    public TravelEntitySpecialRenderer() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        this.bfr = tryCreateBatchingRenderer(fr);
    }

    private static BatchingFontRenderer tryCreateBatchingRenderer(FontRenderer fr) {
        try {
            System.out.println("[TravelAnchorFix] Попытка создать BatchingFontRenderer...");

            Class<?> frc = fr.getClass();

            java.lang.reflect.Field fUnicode = frc.getDeclaredField("unicodePageLocations");
            fUnicode.setAccessible(true);
            ResourceLocation[] unicodePages = (ResourceLocation[]) fUnicode.get(fr);

            java.lang.reflect.Field fChar = frc.getDeclaredField("charWidth");
            fChar.setAccessible(true);
            int[] charWidth = (int[]) fChar.get(fr);

            java.lang.reflect.Field fGlyph = frc.getDeclaredField("glyphWidth");
            fGlyph.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyph.get(fr);

            java.lang.reflect.Field fColor = frc.getDeclaredField("colorCode");
            fColor.setAccessible(true);
            int[] colorCode = (int[]) fColor.get(fr);

            java.lang.reflect.Field fTex = frc.getDeclaredField("locationFontTexture");
            fTex.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fTex.get(fr);

            System.out.println("[TravelAnchorFix] Успешно получили данные из FontRenderer для BatchingFontRenderer");

            return new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);

        } catch (Throwable t) {
            System.out.println("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, fallback. Причина: " + t);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String label = anchor.getLabel();
        if (label == null || label.isEmpty()) return;

        System.out.println("[TravelAnchorFix] Рендер якоря на (" + x + "," + y + "," + z + "), имя=" + label);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-0.016F * 1.6F, -0.016F * 1.6F, 0.016F * 1.6F);

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        if (bfr != null) {
            int textW = bfr.getStringWidth(label);
            System.out.println("[TravelAnchorFix] Используем BatchedRenderer, ширина строки=" + textW);
            bfr.drawString(label, -textW / 2, 0, 0xFFFFFFFF, false);
        } else {
            int textW = fr.getStringWidth(label);
            System.out.println("[TravelAnchorFix] Используем fallback FontRenderer, ширина строки=" + textW);
            fr.drawString(label, -textW / 2, 0, 0xFFFFFFFF);
        }

        // debug: рисуем красный квадратик позади текста
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(1f, 0f, 0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-5, -5, 0);
        GL11.glVertex3f(5, -5, 0);
        GL11.glVertex3f(5, 5, 0);
        GL11.glVertex3f(-5, 5, 0);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    // доступ к иконкам через reflection
    private static IIcon getSelectedOverlay() {
        try {
            java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            System.out.println("[TravelAnchorFix] Ошибка доступа к selectedOverlayIcon: " + t);
            return null;
        }
    }

    private static IIcon getHighlightOverlay() {
        try {
            java.lang.reflect.Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            System.out.println("[TravelAnchorFix] Ошибка доступа к highlightOverlayIcon: " + t);
            return null;
        }
    }
}
