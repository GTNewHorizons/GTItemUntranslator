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
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

@SideOnly(Side.CLIENT)
public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    private final Vector4f selectedColor = new Vector4f(1, 0.25f, 0, 0.5f);
    private final Vector4f highlightColor = new Vector4f(1, 1, 1, 0.25f);

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        // 1) Показывать цели только когда нужно (как в оригинале)
        if (!TravelController.instance.showTargets()) return;

        ITravelAccessable ta = (ITravelAccessable) te;
        if (!ta.isVisible()) return;

        BlockCoord onBlock = TravelController.instance.onBlockCoord;
        if (onBlock != null && onBlock.equals(ta.getLocation())) return;

        // 2) САМЫЙ ГЛАВНЫЙ ПАТЧ: убираем проверку прямой видимости
        // if (!ta.canSeeBlock(Minecraft.getMinecraft().thePlayer)) return;

        // 3) Дистанция / масштаб — как в оригинале
        Vector3d eye = Util.getEyePositionEio(Minecraft.getMinecraft().thePlayer);
        Vector3d loc = new Vector3d(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);

        double maxDistance = TravelSource.BLOCK.getMaxDistanceTravelledSq();
        TravelSource src = TravelController.instance.getTravelItemTravelSource(Minecraft.getMinecraft().thePlayer, false);
        if (src != null) {
            maxDistance = src.getMaxDistanceTravelledSq();
        }
        if (eye.distanceSquared(loc) > maxDistance) return;

        double sf = TravelController.instance.getScaleForCandidate(loc);

        // 4) Обязательно добавить кандидата — иначе не будет выделения/прыжка
        BlockCoord bc = new BlockCoord(te);
        TravelController.instance.addCandidate(bc);

        // 5) Рисуем «рамку» вокруг якоря — как в оригинале
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

        final CubeRenderer cr = CubeRenderer.get();
        final Tessellator tess = Tessellator.instance;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Полупрозрачный «куб»
        tess.startDrawingQuads();
        renderBlock(te.getWorldObj(), sf);
        tess.draw();

        // Оверлей подсветки (selected/hover)
        tess.startDrawingQuads();
        tess.setBrightness(15 << 20 | 15 << 4);
        if (TravelController.instance.isBlockSelected(bc)) {
            tess.setColorRGBA_F(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), getSelectedIcon());
        } else {
            tess.setColorRGBA_F(highlightColor.x, highlightColor.y, highlightColor.z, highlightColor.w);
            cr.render(BoundingBox.UNIT_CUBE.scale(sf + 0.05, sf + 0.05, sf + 0.05), getHighlightIcon());
        }
        tess.draw();
        GL11.glPopMatrix();

        // 6) Рисуем подпись (шрифт) безопасно для Angelica+шейдеров
        renderLabel(te, x, y, z, ta, sf);

        GL11.glPopAttrib();
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }

    private void renderLabel(TileEntity te, double x, double y, double z, ITravelAccessable ta, double sf) {
        float globalScale = (float) sf;

        // (опционально) иконка-лейбл якоря — оставляем как в оригинале
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
            {
                GL11.glPushMatrix();
                GL11.glScalef(globalScale, globalScale, globalScale);
                {
                    GL11.glPushMatrix();
                    if (isBlock) {
                        GL11.glTranslatef(0f, -0.25f, 0);
                    } else {
                        GL11.glTranslatef(0f, -0.5f, 0);
                    }
                    GL11.glScalef(2, 2, 2);

                    // Рендер 3D-иконки (оригинальный метод из EnderCore)
                    EntityItem ei = new EntityItem(te.getWorldObj(), x, y, z, itemLabel);
                    RenderUtil.render3DItem(ei, false);
                    GL11.glPopMatrix();
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
        }

        // Сама надпись
        String label = ta.getLabel();
        if (label == null || label.trim().isEmpty()) return;

        // Цвет подложки (используем только для выбора цвета текста / можем отрисовать BG, если нужно)
        Vector4f bgCol = RenderUtil.DEFAULT_TEXT_BG_COL;
        if (TravelController.instance.isBlockSelected(new BlockCoord(te))) {
            bgCol = new Vector4f(selectedColor.x, selectedColor.y, selectedColor.z, selectedColor.w);
        }

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);

        // Billboard к камере (1.7.10 — через mc.renderManager)
        GL11.glRotatef(-mc.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        // Поднять над якорем и масштабировать
        GL11.glTranslatef(0f, 1.2f * globalScale, 0f);
        float scale = 0.025f * globalScale; // стандартный коэффициент для текста в мире
        GL11.glScalef(-scale, -scale, scale); // инверсия X/Y для правильного направления текста

        // Немного прозрачности — как и весь HUD якоря
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);

        // Отрисовка текста — белым (или оттенком выбранного фона)
        int color =
                0xFFFFFF; // можно захотеть смешать с bgCol по желанию: например, если выбран — сделать теплый оттенок
        int width = fr.getStringWidth(label);
        // Простая полупрозрачная подложка (не обязательно, но помогает читаемости)
        // Комментарий: если когда-нибудь фон будет мешать Angelica — можно выключить.
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        float pad = 2f;
        float w = width;
        float h = 10f;
        // bgCol.w — альфа
        GL11.glColor4f(0f, 0f, 0f, 0.25f); // фиксированная тёмная подложка
        GL11.glVertex3f(-w / 2f - pad, -h, 0);
        GL11.glVertex3f(-w / 2f - pad, 0, 0);
        GL11.glVertex3f(w / 2f + pad, 0, 0);
        GL11.glVertex3f(w / 2f + pad, -h, 0);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        fr.drawString(label, -width / 2, -9, color, false);

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    protected void renderBlock(IBlockAccess world, double sf) {
        Tessellator.instance.setColorRGBA_F(1, 1, 1, 0.75f);
        CubeRenderer.get().render(
                BoundingBox.UNIT_CUBE.scale(sf, sf, sf),
                EnderIO.blockTravelPlatform.getIcon(0, 0));
    }

    public IIcon getSelectedIcon() {
        return EnderIO.blockTravelPlatform.selectedOverlayIcon;
    }

    public IIcon getHighlightIcon() {
        return EnderIO.blockTravelPlatform.highlightOverlayIcon;
    }
}
