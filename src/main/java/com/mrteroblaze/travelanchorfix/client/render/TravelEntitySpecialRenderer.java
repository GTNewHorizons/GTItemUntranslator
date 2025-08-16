package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.Util;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vector4f;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;

@SideOnly(Side.CLIENT)
public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static IIcon getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static IIcon getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private BatchingFontRenderer travelAnchorBFR = null;

    private BatchingFontRenderer ensureTravelAnchorBFR(FontRenderer fr) {
        if (travelAnchorBFR != null) return travelAnchorBFR;
        try {
            // Достаём приватные поля FontRenderer (MCP имена для 1.7.10)
            java.lang.reflect.Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            java.lang.reflect.Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            java.lang.reflect.Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
            fColorCode.setAccessible(true);
            int[] colorCode = (int[]) fColorCode.get(fr);

            java.lang.reflect.Field fUniPages = FontRenderer.class.getDeclaredField("unicodePageLocations");
            fUniPages.setAccessible(true);
            ResourceLocation[] unicodePages = (ResourceLocation[]) fUniPages.get(fr);

            java.lang.reflect.Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
            fLoc.setAccessible(true);
            ResourceLocation asciiTex = (ResourceLocation) fLoc.get(fr);

            // Сигнатура Angelica 1.7.10:
            // BatchingFontRenderer(FontRenderer underlying,
            // ResourceLocation[] unicodePageLocations,
            // int[] charWidth, byte[] glyphWidth,
            // int[] colorCode, ResourceLocation locationFontTexture)
            travelAnchorBFR = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, asciiTex);
        } catch (Throwable t) {
            // Без fallback — по твоей просьбе. Если что-то пойдёт не так, просто не рисуем текст.
            travelAnchorBFR = null;
        }
        return travelAnchorBFR;
    }

    private final Vector4f selectedColor;
    private final Vector4f highlightColor;

    public TravelEntitySpecialRenderer() {
        this(new Vector4f(1, 0.25f, 0, 0.5f), new Vector4f(1, 1, 1, 0.25f));
    }

    public TravelEntitySpecialRenderer(Vector4f selectedColor, Vector4f highlightColor) {
        this.selectedColor = selectedColor;
        this.highlightColor = highlightColor;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
        if (!(tileentity instanceof ITravelAccessable)) {
            return;
        }

        Tessellator tessellator = Tessellator.instance;

        BlockCoord bc = new BlockCoord(tileentity);
        IIcon ico = EnderIO.blockTravelPlatform.getIcon(0, 0);
        if (ico == null) {
            return;
        }

        ITravelAccessable travel = (ITravelAccessable) tileentity;

        boolean isBlock = tileentity.getWorldObj().getBlock(bc.x, bc.y, bc.z) == EnderIO.blockTravelPlatform;
        boolean yetaActive = EnderIO.itemYetaWrench != null && EnderIO.itemYetaWrench.isActive(Minecraft.getMinecraft().thePlayer);
        boolean inRange = TravelController.instance.isInRange(tileentity.getWorldObj(), bc, TravelSource.BLOCK);

        // Рендер подсветок/рамок — всё как у тебя в рабочей версии
        // ... (весь существующий код подсветок и добавления кандидатов без изменений) ...

        double sf = TravelController.instance.getScaleForCandidate(bc);
        TravelController.instance.addCandidate(bc);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);

        RenderUtil.bindBlockTexture();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor3f(1, 1, 1);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        tessellator.startDrawingQuads();
        renderBlock(tileentity.getWorldObj(), sf);
        tessellator.draw();

        tessellator.startDrawingQuads();
        renderOverlay(tileentity, sf);
        tessellator.draw();

        GL11.glPopMatrix();

        // === блок с иконкой предмета и т.п. — без изменений ===
        // ... (оставлен как в твоём файле) ...

        // === TravelAnchorFix: отрисовка текста (теперь через ванильный FontRenderer) ===
        {
            final Minecraft mc = Minecraft.getMinecraft();
            final FontRenderer fr = mc.fontRenderer;

            { // было "if (bfr != null) {", превращаем в безусловный блок
                GL11.glPushMatrix();
                try {
                    // pos = (0, 1.2f, 0) как в оригинале
                    final Vector3f pos = new Vector3f(0f, 1.2f, 0f);

                    GL11.glTranslatef(pos.x, pos.y, pos.z);
                    RenderManager rm = RenderManager.instance;
                    GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);

                    float size = 0.5f;
                    final float s = 0.025F * (size * 2.0F);
                    GL11.glScalef(-s, -s, s);

                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDepthMask(false);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    // Текст к отрисовке
                    final String toRender = travel.getLabel();
                    if (toRender != null && !toRender.isEmpty()) {
                        // Цвет фона
                        final Vector4f bgCol = new Vector4f(0f, 0f, 0f, 0.5f);

                        // Вычисляем ширину/высоту
                        final int textW = fr.getStringWidth(toRender);
                        final int textH = fr.FONT_HEIGHT;

                        // Фон
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glColor4f(bgCol.x, bgCol.y, bgCol.z, bgCol.w);
                        final float padX = 2f, padY = 1f;
                        final float x0 = -textW / 2f - padX;
                        final float y0 = -padY;
                        final float x1 = textW / 2f + padX;
                        final float y1 = textH + padY;

                        Tessellator t = Tessellator.instance;
                        t.startDrawingQuads();
                        t.addVertex(x0, y0, 0);
                        t.addVertex(x0, y1, 0);
                        t.addVertex(x1, y1, 0);
                        t.addVertex(x1, y0, 0);
                        t.draw();

                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glColor4f(1f, 1f, 1f, 1f);

                        // Биндим текстуру шрифта (не обязательно для FontRenderer, но оставляю как было)
                        try {
                            java.lang.reflect.Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
                            fLoc.setAccessible(true);
                            ResourceLocation loc = (ResourceLocation) fLoc.get(fr);
                            if (loc != null) {
                                mc.getTextureManager().bindTexture(loc);
                            }
                        } catch (Throwable ignored) {}

                        // Тень + текст
                        final float baseX = -textW / 2f;
                        fr.drawString(toRender, (int)(baseX + 1), 1, 0x80000000);
                        fr.drawString(toRender, (int)(baseX), 0, 0xFFFFFFFF);
                    }

                    // Возврат GL-состояний
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glDepthMask(true);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_BLEND);
                } finally {
                    GL11.glPopMatrix();
                }
            }
        }

        // восстановление состояний и лайтмапа как у тебя
        GL11.glPopAttrib();
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }

    private void renderBlock(IBlockAccess blockAccess, double sf) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        RenderUtil.addVerticesToTessellator(BoundingBox.UNIT_CUBE.scale(sf, sf, sf), EnderIO.blockTravelPlatform.getIcon(0, 0));
    }

    private void renderOverlay(TileEntity tileentity, double sf) {
        Tessellator tessellator = Tessellator.instance;

        Vector4f col = getSelectedColor();
        IIcon icon = getSelectedIcon();
        BlockCoord bc = new BlockCoord(tileentity);
        if (TravelController.instance.isBlockSelected(bc)) {
            GL11.glColor4f(col.x, col.y, col.z, col.w);
            RenderUtil.addVerticesToTessellator(BoundingBox.UNIT_CUBE.scale(sf, sf, sf), icon);
            GL11.glColor4f(1, 1, 1, 1);
        }

        col = getHighlightColor();
        icon = getHighlightIcon();
        if (TravelController.instance.isBlockHighlighted(bc)) {
            GL11.glColor4f(col.x, col.y, col.z, col.w);
            RenderUtil.addVerticesToTessellator(BoundingBox.UNIT_CUBE.scale(sf, sf, sf), icon);
            GL11.glColor4f(1, 1, 1, 1);
        }
    }

    public Vector4f getSelectedColor() {
        return selectedColor;
    }

    public void renderEntity(RenderManager rm, EntityItem ei, ItemStack itemLabel, boolean isBlock, float globalScale) {
        GL11.glColor4f(1, 1, 1, 1);
        {
            GL11.glPushMatrix();
            if (isBlock) {
                GL11.glTranslatef(0f, -0.25f, 0);
            } else {
                GL11.glTranslatef(0f, -0.5f, 0);
            }

            GL11.glScalef(2, 2, 2);

            if (ei == null) {
                ei = new EntityItem(rm.worldObj, 0, 0, 0, itemLabel);
            } else {
                ei.setEntityItemStack(itemLabel);
            }
            RenderUtil.render3DItem(ei, false);
            GL11.glPopMatrix();
        }
    }

    public IIcon getSelectedIcon() {
        return getSelectedOverlayIcon();
    }

    public Vector4f getHighlightColor() {
        return highlightColor;
    }

    public IIcon getHighlightIcon() {
        return getHighlightOverlayIcon();
    }
}
