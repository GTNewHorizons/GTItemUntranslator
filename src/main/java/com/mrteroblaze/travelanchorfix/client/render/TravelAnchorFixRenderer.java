package com.mrteroblaze.travelanchorfix.client.render;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import crazypants.enderio.EnderIO; // для выбранного цвета, если нужно
import crazypants.enderio.config.Config;
import crazypants.enderio.teleport.anchor.BlockCoord;

@SideOnly(Side.CLIENT)
public class TravelAnchorFixRenderer extends net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer {

    // цвета — можно взять из EnderCore RenderUtil, но чтобы не тянуть его — зададим дефолты
    private static final Vector4f DEFAULT_TEXT_COLOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector4f DEFAULT_SHADOW_COLOR = new Vector4f(0f, 0f, 0f, 0.5f);
    private static final Vector4f DEFAULT_BG_COLOR = new Vector4f(0f, 0f, 0f, 0.4f);

    // масштаб и смещение текста над блоком
    private static final float GLOBAL_SCALE = 0.025f; // подгоняется относительно FONT_HEIGHT
    private static final Vector3f LABEL_POS = new Vector3f(0f, 1.2f, 0f);

    // ленивый экземпляр batched font renderer (если Angelica присутствует)
    private BatchingFontRenderer batched;

    private BatchingFontRenderer ensureBatched() {
        if (batched != null) return batched;
        if (!Loader.isModLoaded("angelica")) return null;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRenderer;
            // Angelica миксином добавляет интерфейс-геттеры в FontRenderer
            FontRendererAccessor acc = (FontRendererAccessor) (Object) fr;

            ResourceLocation[] unicodePages = acc.getUnicodePageLocations();
            int[] charWidth = acc.getCharWidth();
            byte[] glyphWidth = acc.getGlyphWidth();
            int[] colorCode = acc.getColorCode();
            ResourceLocation fontTex = acc.getLocationFontTexture();

            batched = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);
            return batched;
        } catch (Throwable t) {
            // что-то пошло не так — вернём null, будет fallback на ваниль
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor ta = (TileTravelAnchor) te;

        // не показываем, если блок скрыт настройками
        if (!ta.isVisible()) {
            return;
        }

        // достаём текст
        String label = ta.getLabel();
        if (label == null || label.trim().isEmpty()) {
            return;
        }

        // цвет фона для выбранного якоря (если EnderIO задаёт выделение)
        Vector4f bg = DEFAULT_BG_COLOR;
        try {
            if (TravelController.instance.isBlockSelected(new BlockCoord(te))) {
                // если есть "selectedColor" в EnderIO — можно взять его, иначе просто сделаем ярче
                bg = new Vector4f(0f, 0f, 0f, 0.7f);
            }
        } catch (Throwable ignore) {
        }

        // billboard + масштабирование + рендер
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

            // поворот к камере (billboard)
            RenderManager rm = RenderManager.instance;
            GL11.glRotatef(-rm.playerViewY, 0F, 1F, 0F);
            GL11.glRotatef(rm.playerViewX, 1F, 0F, 0F);

            // смещение вверх
            GL11.glTranslatef(LABEL_POS.x, LABEL_POS.y, LABEL_POS.z);

            // масштаб относительно высоты шрифта
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            float scale = GLOBAL_SCALE * fr.FONT_HEIGHT; // подстройка; при необходимости подкорректируй
            GL11.glScalef(scale, scale, scale);

            // рисуем фон + текст
            drawLabelWithOptionalBatched(fr, label, bg, DEFAULT_TEXT_COLOR, true, DEFAULT_SHADOW_COLOR);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void drawLabelWithOptionalBatched(FontRenderer fr, String text,
                                              Vector4f bg, Vector4f txt,
                                              boolean drawShadow, Vector4f shadow) {
        // пробуем batched
        BatchingFontRenderer b = ensureBatched();

        // ширина/высота
        final int textW = (b != null ? b.getStringWidth(text) : fr.getStringWidth(text));
        final int textH = fr.FONT_HEIGHT;

        // отцентрируем
        GL11.glTranslatef(-textW / 2.0f, 0f, 0f);

        // фон (примитивный прямоугольник с альфой)
        if (bg != null && bg.w > 0f) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            GL11.glColor4f(bg.x, bg.y, bg.z, bg.w);

            final float padX = 2f;
            final float padY = 1f;
            float x0 = -padX;
            float y0 = -padY;
            float x1 = textW + padX;
            float y1 = textH + padY;

            Tessellator tes = Tessellator.instance;
            tes.startDrawingQuads();
            tes.addVertex(x0, y0, 0);
            tes.addVertex(x0, y1, 0);
            tes.addVertex(x1, y1, 0);
            tes.addVertex(x1, y0, 0);
            tes.draw();

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        // обязательно бинд текстуры шрифта перед рисованием — это как раз фикс "квадратов"
        Minecraft mc = Minecraft.getMinecraft();
        try {
            // у 1.7.10 можно достать location через accessor (если Angelica есть)
            if (b != null) {
                // сам batched занимается правильным биндом текстур глифов внутри, но лишним это не будет
                mc.getTextureManager().bindTexture(((FontRendererAccessor) (Object) fr).getLocationFontTexture());
            } else {
                // ваниль
                mc.getTextureManager().bindTexture(fr.getFontTextureName()); // в 1.7.10 это есть
            }
        } catch (Throwable t) {
            // если accessor недоступен, надеемся на стандартное поведение
        }

        final int txtColor = ((int)(txt.w * 255) & 0xFF) << 24 |
                             ((int)(txt.x * 255) & 0xFF) << 16 |
                             ((int)(txt.y * 255) & 0xFF) << 8  |
                             ((int)(txt.z * 255) & 0xFF);

        final int shColor = ((int)(shadow.w * 255) & 0xFF) << 24 |
                            ((int)(shadow.x * 255) & 0xFF) << 16 |
                            ((int)(shadow.y * 255) & 0xFF) << 8  |
                            ((int)(shadow.z * 255) & 0xFF);

        // рисуем
        if (b != null) {
            if (drawShadow) b.drawString(text, 0.5f, 0.5f, shColor, false);
            b.drawString(text, 0f, 0f, txtColor, false);
        } else {
            // fallback на ванильный рендер (тоже должен работать корректно благодаря bindTexture выше)
            if (drawShadow) fr.drawString(text, 1, 1, shColor, false);
            fr.drawString(text, 0, 0, txtColor, false);
        }
    }
}
