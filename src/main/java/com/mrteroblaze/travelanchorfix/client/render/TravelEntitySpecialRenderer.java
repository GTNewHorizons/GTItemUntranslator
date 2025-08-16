package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr;

    private final IIcon selectedOverlayIcon;
    private final IIcon highlightOverlayIcon;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer();

        // Достаём иконки через reflection
        this.selectedOverlayIcon = getSelectedOverlayIcon();
        this.highlightOverlayIcon = getHighlightOverlayIcon();
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

            // Достаём приватные поля
            Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            Field fUnicodePageLocations = FontRenderer.class.getDeclaredField("unicodePageLocations");
            fUnicodePageLocations.setAccessible(true);
            ResourceLocation[] unicodePageLocations = (ResourceLocation[]) fUnicodePageLocations.get(fr);

            Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
            fColorCode.setAccessible(true);
            int[] colorCode = (int[]) fColorCode.get(fr);

            Field fFontTexture = FontRenderer.class.getDeclaredField("locationFontTexture");
            fFontTexture.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fFontTexture.get(fr);

            LOG.info("[TravelAnchorFix] Успешно создан BatchingFontRenderer");
            return new BatchingFontRenderer(fr, unicodePageLocations, charWidth, glyphWidth, colorCode, fontTex);

        } catch (Exception e) {
            LOG.warn("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, используем ванильный FontRenderer", e);
            return null;
        }
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            Field f = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            Object val = f.get(EnderIO.blockTravelPlatform);
            LOG.debug("[TravelAnchorFix] selectedOverlayIcon = {}", val);
            return (IIcon) val;
        } catch (Exception e) {
            LOG.warn("[TravelAnchorFix] Не удалось получить selectedOverlayIcon", e);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            Field f = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            Object val = f.get(EnderIO.blockTravelPlatform);
            LOG.debug("[TravelAnchorFix] highlightOverlayIcon = {}", val);
            return (IIcon) val;
        } catch (Exception e) {
            LOG.warn("[TravelAnchorFix] Не удалось получить highlightOverlayIcon", e);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;

        String text = anchor.getLabel();
        if (text == null || text.trim().isEmpty()) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        FontRenderer fr = (batchingFr != null ? batchingFr : vanillaFr);
        int width = fr.getStringWidth(text);
        int half = width / 2;

        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);
        fr.drawString(text, -half, 0, 0xFFFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
