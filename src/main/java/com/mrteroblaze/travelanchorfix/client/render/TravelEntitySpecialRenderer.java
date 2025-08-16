package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.EnderIO;
import com.enderio.machine.travel.AnchorTravelSource;
import com.enderio.machine.travel.TileTravelAnchor;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr;

    // Reflection поля для иконок
    private static Field selectedOverlayField = null;
    private static Field highlightOverlayField = null;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer();

        tryInitReflection();
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            if (vanillaFr instanceof FontRendererAccessor) {
                FontRendererAccessor acc = (FontRendererAccessor) vanillaFr;
                BatchingFontRenderer bfr = acc.angelica$getBatcher();
                if (bfr != null) {
                    LOG.info("[TravelAnchorFix] Успешно получен BatchingFontRenderer из Angelica");
                    return bfr;
                } else {
                    LOG.warn("[TravelAnchorFix] angelica$getBatcher() вернул null, используем fallback");
                }
            } else {
                LOG.warn("[TravelAnchorFix] FontRenderer не является FontRendererAccessor, используем fallback");
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при создании BatchingFontRenderer", t);
        }
        return null;
    }

    private void tryInitReflection() {
        try {
            selectedOverlayField = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            selectedOverlayField.setAccessible(true);
            highlightOverlayField = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            highlightOverlayField.setAccessible(true);
            LOG.info("[TravelAnchorFix] Reflection поля для overlay иконок инициализированы");
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Не удалось инициализировать reflection поля overlay иконок", t);
        }
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            if (selectedOverlayField != null) {
                Object val = selectedOverlayField.get(EnderIO.blockTravelPlatform);
                LOG.debug("[TravelAnchorFix] selectedOverlayIcon = {}", val);
                return (IIcon) val;
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при получении selectedOverlayIcon", t);
        }
        return null;
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            if (highlightOverlayField != null) {
                Object val = highlightOverlayField.get(EnderIO.blockTravelPlatform);
                LOG.debug("[TravelAnchorFix] highlightOverlayIcon = {}", val);
                return (IIcon) val;
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при получении highlightOverlayIcon", t);
        }
        return null;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        String text = anchor.getLabel();
        if (text == null || text.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-0.016F, -0.016F, 0.016F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        int width;
        if (batchingFr != null) {
            try {
                width = batchingFr.getStringWidth(text, 0, text.length());
            } catch (Throwable t) {
                LOG.error("[TravelAnchorFix] Ошибка при вычислении ширины строки через BatchingFontRenderer", t);
                width = vanillaFr.getStringWidth(text);
            }
        } else {
            width = vanillaFr.getStringWidth(text);
        }

        int half = width / 2;

        // Фон рамки
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // Текст
        if (batchingFr != null) {
            try {
                batchingFr.drawString(text, -half, 0, 0xFFFFFFFF, false, false, null, 0, text.length());
                LOG.debug("[TravelAnchorFix] Отрисовали текст '{}' через BatchingFontRenderer", text);
            } catch (Throwable t) {
                LOG.error("[TravelAnchorFix] Ошибка при рендере текста через BatchingFontRenderer", t);
                vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
            }
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
            LOG.debug("[TravelAnchorFix] Отрисовали текст '{}' через Vanilla FontRenderer", text);
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
