package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.teleport.TravelController;

/**
 * Копия GTNH TravelEntitySpecialRenderer с одной заменой:
 * вместо RenderUtil.drawBillboardedText(...) рисуем текст вручную через FontRenderer,
 * чтобы обойти проблемы с Angelica (enableFontRenderer=false).
 */
@SideOnly(Side.CLIENT)
public class MyTravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private final Vector4f selectedColor;
    private final Vector4f highlightColor;

    public MyTravelEntitySpecialRenderer() {
        this(new Vector4f(1, 0.25f, 0, 0.5f), new Vector4f(1, 1, 1, 0.25f));
    }

    public MyTravelEntitySpecialRenderer(Vector4f selectedColor, Vector4f highlightColor) {
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

        Vector3d eye = Util.getEyePositionEio(Minecraft.getMinecraft().thePlayer);
        Vector3d loc = Util.getEyePosition(tileentity);

        double maxDistance = TravelSource.STAFF.getMaxDistanceTravelledSq();

        TravelSource source = TravelController.instance
            .getTravelItemTravelSource(Minecraft.getMinecraft().thePlayer, false);
        if (source != null) {
            maxDistance = source.getMaxDistanceTravelledSq();
        }
        if (eye.distanceSquared(loc) > maxDistance) {
            return;
        }

        double sf = TravelController.instance.getScaleForCandidate(loc);

        BlockCoord bc = new BlockCoord(tileentity);
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

        // ===== отрисовка коробки-рамки (как в оригинале) =====
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5f, y + 0.5f, z + 0.5f);

        RenderUtil.rotateToPlayer();

        float xf = (float) (loc.x - eye.x);
        float yf = (float) (loc.y - eye.y);
        float zf = (float) (loc.z - eye.z);

        float xLen = Math.abs(xf);
        float yLen = Math.abs(yf);
        float zLen = Math.abs(zf);

        float max = Math.max(xLen, Math.max(yLen, zLen));
        float l = 1;
        float ll = 0.5f;
        float xs = Math.min(max, l);
        float ys = Math.min(max, l);
        float zs = Math.min(max, l);

        xs = xs / max * sf;
        ys = ys / max * sf;
        zs = zs / max * sf;

        GL11.glScalef(xs, ys, zs);

        IIcon icon = getSelectedIcon();
        IIcon selectedIcon = getSelectedIcon();
        IIcon highlightIcon = getHighlightIcon();

        CubeRenderer cr = CubeRenderer.get();

        if (TravelController.instance.isBlockSelected(bc)) {
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), selectedIcon);
        } else {
            cr.render(BoundingBox.UNIT_CUBE.scale(sf, sf, sf), icon);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), highlightIcon);
        }
        tessellator.draw();
        GL11.glPopMatrix();

        // === НАША ЗАМЕНА: рендер текста без RenderUtil.drawBillboardedText ===
        renderLabelNoBatch(tileentity, x, y, z, ta, sf);

        GL11.glPopAttrib();
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }

    private EntityItem ei;

    /** Рендер иконки-лейбла и текста с именем якоря “в лоб” через FontRenderer */
    private void renderLabelNoBatch(TileEntity tileentity, double x, double y, double z, ITravelAccessable ta, double sf) {
        float globalScale = (float) sf;

        // ----- иконка-лейбл (как в оригинале) -----
        ItemStack itemLabel = ta.getItemLabel();
        if (itemLabel != null && itemLabel.getItem() != null) {
            boolean isBlock = itemLabel.getItem() instanceof ItemBlock;

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
            float col = 0.5f;
            GL14.glBlendColor(col, col, col, col);
            GL11.glColor4f(1, 1, 1, 1);

            GL11.glPushMatrix();
            GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
            if (!isBlock && Minecraft.getMinecraft().gameSettings.fancyGraphics) {
                RenderUtil.rotateToPlayer();
            }
            GL11.glPushMatrix();
            GL11.glScalef(globalScale, globalScale, globalScale);
            {
                GL11.glPushMatrix();
                GL11.glTranslatef(0, 1.2f, 0);
                if (!isBlock) {
                    RenderUtil.rotateToPlayer();
                } else {
                    GL11.glRotatef(90, 1, 0, 0);
                }
                if (isBlock) {
                    GL11.glTranslatef(0f, -0.25f, 0);
                } else {
                    GL11.glTranslatef(0f, -0.5f, 0);
                }
                GL11.glScalef(2, 2, 2);

                if (ei == null) {
                    ei = new EntityItem(tileentity.getWorldObj(), x, y, z, itemLabel);
                } else {
                    ei.setEntityItemStack(itemLabel);
                }
                RenderUtil.render3DItem(ei, false);
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }

        // ----- текстовое имя якоря (НАШ способ) -----
        String toRender = ta.getLabel();
        if (toRender == null || toRender.trim().isEmpty()) return;

        Vector4f bgCol = RenderUtil.DEFAULT_TEXT_BG_COL;
        if (TravelController.instance.isBlockSelected(new BlockCoord(tileentity))) {
            bgCol = new Vector4f(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
        }

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
        GL11.glPushMatrix();
        GL11.glScalef(globalScale, globalScale, globalScale);

        // позиция как в оригинале
        GL11.glTranslatef(0, 1.2f, 0);
        RenderUtil.rotateToPlayer();

        // масштаб для шрифта: как у nameplate — 1.6 * 0.0166667, умножаем ещё на 0.5f (оригинальный size)
        final float fontBase = 0.016666668F * 1.6F * 0.5F;
        GL11.glScalef(fontBase, fontBase, fontBase);

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int textWidth = fr.getStringWidth(toRender);
        int half = textWidth / 2;
        int height = fr.FONT_HEIGHT;

        // фон
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST); // главное для “сквозь стены”
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        t.startDrawingQuads();
        t.setColorRGBA_F(bgCol.x, bgCol.y, bgCol.z, bgCol.w);
        // прямоугольник чуть больше текста
        int padX = 3;
        int padY = 2;
        t.addVertex(-half - padX, -height - padY, 0.0);
        t.addVertex(-half - padX, padY, 0.0);
        t.addVertex(half + padX, padY, 0.0);
        t.addVertex(half + padX, -height - padY, 0.0);
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // сам текст (с тенью)
        fr.drawString(toRender, -half, -height, 0xFFFFFFFF, true);

        // восстановление стейтов
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    // ===== остальное — как в оригинале =====
    protected void renderBlock(IBlockAccess world, double sf) {
        Tessellator.instance.setColorRGBA_F(1, 1, 1, 0.75f);
        CubeRenderer.get().render(BoundingBox.UNIT_CUBE.scale(sf, sf, sf),
                EnderIO.blockTravelPlatform.getIcon(0, 0));
    }

    public Vector4f getSelectedColor() {
        return selectedColor;
    }

    public IIcon getSelectedIcon() {
        return EnderIO.blockTravelPlatform.selectedOverlayIcon;
    }

    public Vector4f getHighlightColor() {
        return highlightColor;
    }

    public IIcon getHighlightIcon() {
        return EnderIO.blockTravelPlatform.highlightOverlayIcon;
    }
}
