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

    private EntityItem ei = null;

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTicks) {
        if (!(tileentity instanceof TileTravelAnchor)) return;

        // Показ целей только когда TravelController в “режиме выбора”
        if (!TravelController.instance.showTargets()) return;

        ITravelAccessable ta = (ITravelAccessable) tileentity;
        if (!ta.isVisible()) return;

        // Не показываем маркер на блоке, на котором стоим
        BlockCoord onBlock = TravelController.instance.onBlockCoord;
        if (onBlock != null && onBlock.equals(ta.getLocation())) return;

        // ВНИМАНИЕ: убрали проверку видимости блока для игрока, чтобы было видно сквозь стены
        // if (!ta.canSeeBlock(Minecraft.getMinecraft().thePlayer)) return;

        Minecraft mc = Minecraft.getMinecraft();

        Vector3d eye = Util.getEyePositionEio(mc.thePlayer);
        Vector3d loc = new Vector3d(tileentity.xCoord + 0.5, tileentity.yCoord + 0.5, tileentity.zCoord + 0.5);

        double maxDistance = TravelSource.BLOCK.getMaxDistanceTravelledSq();
        TravelSource src = TravelController.instance.getTravelItemTravelSource(mc.thePlayer, false);
        if (src != null) maxDistance = src.getMaxDistanceTravelledSq();
        if (eye.distanceSquared(loc) > maxDistance) return;

        double sf = TravelController.instance.getScaleForCandidate(loc);

        // ОБЯЗАТЕЛЬНО: добавляем кандидата, иначе выбор/телепорт не работают
        BlockCoord bc = new BlockCoord(tileentity);
        TravelController.instance.addCandidate(bc);

        mc.entityRenderer.disableLightmap(0);

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

        // Полупрозрачный куб
        tess.startDrawingQuads();
        renderBlock(tileentity.getWorldObj(), sf);
        tess.draw();

        // Подсветка/выделение
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

        // Маленький значок предмета над якорем
        ItemStack itemLabel = ta.getItemLabel();
        if (itemLabel != null && itemLabel.getItem() != null) {
            boolean isBlock = itemLabel.getItem() instanceof ItemBlock;

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
            GL14.glBlendColor(0.5f, 0.5f, 0.5f, 0.5f);
            GL11.glColor4f(1, 1, 1, 1);

            GL11.glPushMatrix();
            GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
            if (!isBlock && mc.gameSettings.fancyGraphics) {
                RenderUtil.rotateToPlayer();
            }
            GL11.glPushMatrix();
            GL11.glScalef((float) sf, (float) sf, (float) sf);
            GL11.glPushMatrix();
            GL11.glTranslatef(0f, isBlock ? -0.25f : -0.5f, 0f);
            GL11.glScalef(2, 2, 2);

            if (ei == null) ei = new EntityItem(tileentity.getWorldObj(), x, y, z, itemLabel);
            else ei.setEntityItemStack(itemLabel);

            RenderUtil.render3DItem(ei, false);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }

        // Текст названия — через FontRenderer (совместимо с Angelica shaders)
        String label = ta.getLabel();
        if (label != null && label.trim().length() > 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);

            // Билбординг под 1.7.10
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

            float globalScale = (float) sf;
            GL11.glTranslatef(0f, 1.2f * globalScale, 0f);
            float scale = 0.025f * globalScale;
            GL11.glScalef(-scale, -scale, scale);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            int w = fr.getStringWidth(label);

            // Тёмная подложка
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(0f, 0f, 0f, 0.25f);
            float pad = 2f, h = 10f;
            GL11.glVertex3f(-w / 2f - pad, -h, 0);
            GL11.glVertex3f(-w / 2f - pad, 0,   0);
            GL11.glVertex3f( w / 2f + pad, 0,   0);
            GL11.glVertex3f( w / 2f + pad, -h,  0);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // Текст
            fr.drawString(label, -w / 2, -9, 0x21000000, false);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            fr.drawString(label, -w / 2, -9, 0xFFFFFFFF, false);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

        GL11.glPopAttrib();
        mc.entityRenderer.enableLightmap(0);
    }

    protected void renderBlock(IBlockAccess world, double sf) {
        Tessellator.instance.setColorRGBA_F(1, 1, 1, 0.75f);
        CubeRenderer.get().render(BoundingBox.UNIT_CUBE.scale(sf, sf, sf), EnderIO.blockTravelPlatform.getIcon(0, 0));
    }

    public IIcon getSelectedIcon() {
        return EnderIO.blockTravelPlatform.selectedOverlayIcon;
    }

    public IIcon getHighlightIcon() {
        return EnderIO.blockTravelPlatform.highlightOverlayIcon;
    }
}
