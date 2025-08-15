package com.mrteroblaze.travelanchorfix.client.render;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class TravelAnchorTESRFix extends net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer {

    // Поймать лимит из EnderIO config, иначе fallback
    private static int resolveMaxDistance() {
        try {
            Class<?> cfg = Class.forName("crazypants.enderio.config.Config");
            Field f = cfg.getField("travelAnchorMaxDistance");
            f.setAccessible(true);
            Object val = f.get(null);
            if (val instanceof Integer) {
                return Math.max(1, (Integer) val);
            }
        } catch (Throwable ignored) {}
        return 512; // дефолт, если не нашли класс/поле
    }

    private static final int MAX_DIST = resolveMaxDistance();
    private static final double MAX_DIST_SQ = (double) MAX_DIST * (double) MAX_DIST;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;
        final TileTravelAnchor anchor = (TileTravelAnchor) te;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return;

        // Показ только если держим Staff ИЛИ стоим на якоре
        final boolean show = isHoldingTravelStaff(mc) || isStandingOnAnchor(mc.theWorld, mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        if (!show) return;

        final String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) return;

        // Дистанция
        double distSq = mc.thePlayer.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);
        if (distSq > MAX_DIST_SQ) return;

        // Выделяем ли именно этот якорь в прицеле (для толстой рамки)
        boolean aimedAtThis = isAimedAt(te);

        // Рисуем рамку (wireframe)
        renderWireframeBox(x, y, z, aimedAtThis);

        // Рисуем имя сквозь стены (disable depth test)
        renderFloatingLabel(name, x, y, z);
    }

    /** Проверка: держит ли игрок Staff of Traveling (без compile-time зависимости) */
    private boolean isHoldingTravelStaff(Minecraft mc) {
        ItemStack held = mc.thePlayer.getCurrentEquippedItem();
        if (held == null) return false;
        try {
            Class<?> staffClazz = Class.forName("crazypants.enderio.teleport.ItemTravelStaff");
            return staffClazz.isInstance(held.getItem());
        } catch (Throwable ignored) {
            // на всякий случай fallback по имени регистра
            try {
                String unloc = held.getUnlocalizedName();
                return unloc != null && unloc.toLowerCase(java.util.Locale.ROOT).contains("travelstaff");
            } catch (Throwable ignored2) {}
            return false;
        }
    }

    /** Проверка: стоит ли игрок на travel anchor */
    private boolean isStandingOnAnchor(World world, double px, double py, double pz) {
        int bx = (int) Math.floor(px);
        int by = (int) Math.floor(py - 0.1); // блок под ногами
        int bz = (int) Math.floor(pz);
        TileEntity te = world.getTileEntity(bx, by, bz);
        return te instanceof TileTravelAnchor;
    }

    /** В прицеле ли именно этот TE (координатами) */
    private boolean isAimedAt(TileEntity te) {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) return false;
        return mop.blockX == te.xCoord && mop.blockY == te.yCoord && mop.blockZ == te.zCoord;
    }

    /** Простая wireframe-рамка без сторонних утилит */
    private void renderWireframeBox(double x, double y, double z, boolean thick) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).expand(0.002, 0.002, 0.002);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false); // чуть "поверх"
        GL11.glLineWidth(thick ? 3.0F : 1.5F);
        GL11.glColor4f(1f, 1f, 1f, thick ? 0.9f : 0.5f);

        RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFFFFFFFF);

        // reset
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    /** Рендер текста, всегда видимого (disable depth test), биллборд к камере */
    private void renderFloatingLabel(String text, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        GL11.glPushMatrix();
        // позиция над блоком
        GL11.glTranslated(x + 0.5, y + 1.25, z + 0.5);

        // развернуть к камере (1.7.10)
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.016666668F * 1.2F; // чуть крупнее обычного
        GL11.glScalef(-scale, -scale, scale);

        // Снять глубину, чтобы было видно сквозь стены
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int width = fr.getStringWidth(text);
        int half = width / 2;

        // Подложка
        drawQuad(-half - 3, -2, width + 6, fr.FONT_HEIGHT + 2, 0x55000000);

        // Обводка (тени) + основной текст
        fr.drawString(text, -half + 1, 0, 0x000000, false);
        fr.drawString(text, -half - 1, 0, 0x000000, false);
        fr.drawString(text, -half, 1, 0x000000, false);
        fr.drawString(text, -half, -1, 0x000000, false);
        fr.drawString(text, -half, 0, 0xFFFFFF, false);

        // restore
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glPopMatrix();
        // вернуть текстурный биндинг (иногда полезно)
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    }

    private void drawQuad(int x, int y, int w, int h, int argb) {
        // ARGB -> RGBA floats
        float a = (argb >> 24 & 255) / 255.0F;
        float r = (argb >> 16 & 255) / 255.0F;
        float g = (argb >> 8 & 255) / 255.0F;
        float b = (argb & 255) / 255.0F;

        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        t.startDrawingQuads();
        t.addVertex(x,     y + h, 0);
        t.addVertex(x + w, y + h, 0);
        t.addVertex(x + w, y,     0);
        t.addVertex(x,     y,     0);
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
