package com.mrteroblaze.travelanchorfix.client.render;

import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

/**
 * Рендер названия Travel Anchor с фоном.
 * - Ширина: всегда из Vanilla FontRenderer
 * - Отрисовка: через Angelica (FontRendererAccessor#angelica$drawStringBatched) при наличии,
 *   иначе fallback на Vanilla FontRenderer#drawString
 */
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final FontRendererAccessor angelicaAcc; // null, если Angelica недоступна

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        FontRendererAccessor acc = null;
        try {
            if (vanillaFr instanceof FontRendererAccessor) {
                acc = (FontRendererAccessor) vanillaFr;
                LOG.info("[TravelAnchorFix] Angelica FontRendererAccessor доступен — будем рисовать батчем.");
            } else {
                LOG.warn("[TravelAnchorFix] FontRendererAccessor недоступен — fallback на Vanilla FontRenderer.");
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при проверке FontRendererAccessor, fallback на Vanilla.", t);
        }
        this.angelicaAcc = acc;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        // 1. Проверяем тип плитки
        if (!(te instanceof TileTravelAnchor)) return;
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        // 2. Текст ярлыка
        final String text = anchor.getLabel();
        if (text == null || text.isEmpty()) return;

        GL11.glPushMatrix();
        try {
            // 3. Билбординг у камеры
            GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);

            // Масштаб под «табличку»
            GL11.glScalef(-0.025F, -0.025F, 0.025F);

            // 4. Ширина всегда через Vanilla
            int width = vanillaFr.getStringWidth(text);
            int half = width / 2;

            // 5. Фон (полупрозрачный)
            drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

            // 6. Текст
            if (angelicaAcc != null) {
                // Батч-рендер от Angelica: (text, x, y, argb, dropShadow)
                angelicaAcc.angelica$drawStringBatched(text, -half, 0, 0xFFFFFFFF, false);
                // LOG.debug("[TravelAnchorFix] Batched draw: '{}'", text);
            } else {
                vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
                // LOG.debug("[TravelAnchorFix] Vanilla draw: '{}'", text);
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка в renderTileEntityAt (fallback на Vanilla для текста).", t);
            try {
                vanillaFr.drawString(anchor.getLabel(), -vanillaFr.getStringWidth(anchor.getLabel()) / 2, 0, 0xFFFFFFFF);
            } catch (Throwable ignored) {}
        } finally {
            GL11.glPopMatrix();
        }
    }

    /**
     * Локальный прямоугольник (ARGB).
     */
    private static void drawRect(int x1, int y1, int x2, int y2, int argb) {
        // Достаём компоненты цвета
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;

        // Сохранение стейта минимальное — только то, что меняем
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x1, y1, 0.0F);
        GL11.glVertex3f(x1, y2, 0.0F);
        GL11.glVertex3f(x2, y2, 0.0F);
        GL11.glVertex3f(x2, y1, 0.0F);
        GL11.glEnd();

        // Возвращаем стейт
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        // Цвет/альфа ресторятся текстурным биндингом у FontRenderer,
        // но если очень нужно — можно вернуть GL11.glColor4f(1,1,1,1)
    }
}
