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
import org.lwjgl.opengl.GL14;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
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

    // Оставлено как в твоём рабочем файле (Angelica присутствует как зависимость),
    // но сам bfr дальше НЕ используется для рисования текста.
    private BatchingFontRenderer travelAnchorBFR = null;

    private BatchingFontRenderer ensureTravelAnchorBFR(FontRenderer fr) {
        if (travelAnchorBFR != null) return travelAnchorBFR;
        try {
            java.lang.reflect.Field fGlyphWidth = FontRenderer.class.getDeclaredField("glyphWidth");
            fGlyphWidth.setAccessible(true);
            byte[] glyphWidth = (byte[]) fGlyphWidth.get(fr);

            java.lang.reflect.Field fCharWidth = FontRenderer.class.getDeclaredField("charWidth");
            fCharWidth.setAccessible(true);
            int[] charWidth = (int[]) fCharWidth.get(fr);

            java.lang.reflect.Field fColorCode = FontRenderer.class.getDeclaredField("colorCode");
            fColorCode.setAccessible(true);
            int[] colorCode = (int[]) fColorCode.get(fr);

            java.lang.reflect.Field fFontTex = FontRenderer.class.getDeclaredField("locationFontTexture");
            fFontTex.setAccessible(true);
            ResourceLocation fontTex = (ResourceLocation) fFontTex.get(fr);

            ResourceLocation[] unicodePages = null; // не используем на 1.7.10
            travelAnchorBFR = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);
        } catch (Throwable t) {
            t.printStackTrace();
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
        if (!TravelController.instance.showTargets()) {
            return;
        }

        ITravelAccessable ta = (ITravelAccessable) tileentity;

        if (!ta.isVisible()) {
            return;
        }

        BlockCoord onBlock = TravelController.instance.onBlockCoord;
        if (onBlock != null && onBlock.equals(ta.getLocation())) {
            return;
        }
        if (!ta.canSeeBlock(Minecraft.getMinecraft().thePlayer)) {
            return;
        }

        final CubeRenderer cr = CubeRenderer.get();
        final Tessellator tessellator = Tessellator.instance;

        // === ВОЗВРАТ К 1.7.10: без getRayTraceVectorsEio ===
        Vector3d eye = Util.getEyePositionEio(Minecraft.getMinecraft().thePlayer);
        Vector3d loc = new Vector3d(tileentity.xCoord + 0.5, tileentity.yCoord + 0.5, tileentity.zCoord + 0.5);
        Vector3d look = Util.getLookVecEio(Minecraft.getMinecraft().thePlayer);
        // точка "куда смотрим" на дистанции ~32 блока
        Vector3d dir = new Vector3d(eye.x + look.x * 32.0, eye.y + look.y * 32.0, eye.z + look.z * 32.0);
        double diff = dir.distance(loc);
        // === /ВОЗВРАТ К 1.7.10 ===

        double scale = 0.2 * (diff / 12d);
        scale = Math.max(0.2, scale);
        scale = RenderUtil.clamp(scale, 0.1, 2.5);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);

        float sf = (float) scale;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
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
        tessellator.setBrightness(15 << 20 | 15 << 4);
        if (TravelController.instance.isBlockSelected(new BlockCoord(tileentity))) {
            tessellator.setColorRGBA_F(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), getSelectedIcon());
        } else {
            tessellator.setColorRGBA_F(highlightColor.x, highlightColor.y, highlightColor.z, highlightColor.w);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), getHighlightIcon());
        }
        tessellator.draw();
        GL11.glPopMatrix();

        renderLabel(tileentity, x, y, z, ta, sf);

        GL11.glPopAttrib();

        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }

    private EntityItem ei;

    private void renderLabel(TileEntity tileentity, double x, double y, double z, ITravelAccessable ta, float globalScale) {

        if (TravelController.instance.isTargetEnderIo()) {
            RenderManager rm = RenderManager.instance;

            // инвентарь
            ItemStack itemLabel = ta.getItemLabel();
            if (itemLabel != null) {
                if (itemLabel.getItem() != null) {
                    if (!(itemLabel.getItem() instanceof ItemBlock)) {
                        itemLabel = null;
                    }
                }
            }
            if (itemLabel != null) {
                GL11.glPushMatrix();
                {
                    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
                    {
                        GL11.glPushMatrix();
                        try {
                            GL11.glScalef(globalScale, globalScale, globalScale);
                            {
                                float s = 1.2f;
                                Vector3f pos = new Vector3f(0.0f, 0.0f, 0.0f);

                                // Направление на игрока
                                GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
                                GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);

                                // Масштаб
                                GL11.glScalef(-s, -s, s);

                                // Сдвиг
                                GL11.glTranslatef(-0.5f, -0.5f, 0.0f);

                                if (ei == null) {
                                    ei = new EntityItem(tileentity.getWorldObj(), x, y, z, itemLabel);
                                } else {
                                    ei.setEntityItemStack(itemLabel);
                                }
                                RenderUtil.render3DItem(ei, false);
                                GL11.glPopMatrix();
                            }
                        } finally {
                            GL11.glPopMatrix();
                        }
                    }
                }
                GL11.glPopMatrix();
            }
        }

        String toRender = ta.getLabel();
        if (toRender != null && toRender.trim().length() > 0) {
            GL11.glColor4f(1, 1, 1, 1);
            Vector4f bgCol = RenderUtil.DEFAULT_TEXT_BG_COL;
            if (TravelController.instance.isBlockSelected(new BlockCoord(tileentity))) {
                bgCol = new Vector4f(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
            }

            {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
                {
                    GL11.glPushMatrix();
                    GL11.glScalef(globalScale, globalScale, globalScale);
                    Vector3f pos = new Vector3f(0, 1.2f, 0);
                    float size = 0.5f;

                    // ======= ТОЛЬКО СМЕНА РИСОВАНИЯ ТЕКСТА =======
                    final Minecraft mc = Minecraft.getMinecraft();
                    final FontRenderer fr = mc.fontRenderer;
                    // (оставлено как раньше, не используется для draw)
                    ensureTravelAnchorBFR(fr);

                    GL11.glPushMatrix();
                    try {
                        // позиция текста
                        GL11.glTranslatef(pos.x, pos.y, pos.z);

                        // billboard к камере
                        RenderManager rm = RenderManager.instance;
                        GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);

                        // масштаб как в исходнике
                        final float s = 0.025F * (size * 2.0F);
                        GL11.glScalef(-s, -s, s);

                        // GL-состояния
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL11.glDepthMask(false);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                        // размеры текста
                        final int textW = fr.getStringWidth(toRender);
                        final int textH = fr.FONT_HEIGHT;

                        // фон
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

                        // биндим текстуру шрифта (поле 1.7.10)
                        try {
                            java.lang.reflect.Field fLoc = FontRenderer.class.getDeclaredField("locationFontTexture");
                            fLoc.setAccessible(true);
                            ResourceLocation loc = (ResourceLocation) fLoc.get(fr);
                            if (loc != null) {
                                mc.getTextureManager().bindTexture(loc);
                            }
                        } catch (Throwable ignored) {}

                        // сам текст (тень + основной), центрируем как было
                        final int baseX = -textW / 2;
                        fr.drawString(toRender, baseX + 1, 1, 0x80000000); // тень
                        fr.drawString(toRender, baseX, 0, 0xFFFFFFFF);     // белый

                        // возврат GL-состояний
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glDepthMask(true);
                        GL11.glEnable(GL11.GL_LIGHTING);
                        GL11.glDisable(GL11.GL_BLEND);
                    } finally {
                        GL11.glPopMatrix();
                    }
                    // ======= /ТОЛЬКО СМЕНА РИСОВАНИЯ ТЕКСТА =======

                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
        }
    }

    protected void renderBlock(IBlockAccess world, double sf) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        RenderUtil.bindBlockTexture();
        RenderUtil.renderConnectedTextureFace(world, new BlockCoord(0, 0, 0), EnderIO.blockTravelPlatform, 0, 0, 0, 0, 1, 0, 1, 0, 1);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        RenderUtil.bindBlockTexture();
        RenderUtil.drawBillboardedTexturedCube(new BlockCoord(0, 0, 0), new Vector3f((float) sf, (float) sf, (float) sf));
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
