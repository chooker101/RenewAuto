package net.fabricmc.renew_auto;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class DispenserFishingBobberEntityRenderer extends EntityRenderer<DispenserFishingBobberEntity> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fishing_hook.png");
   private static final RenderLayer LAYER;

   public DispenserFishingBobberEntityRenderer(EntityRendererFactory.Context context) {
      super(context);
   }

   @Override
   public void render(DispenserFishingBobberEntity fishingBobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      matrixStack.push();
      matrixStack.push();
      matrixStack.scale(0.5F, 0.5F, 0.5F);
      matrixStack.multiply(this.dispatcher.getRotation());
      matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
      MatrixStack.Entry entry = matrixStack.peek();
      Matrix4f matrix4f = entry.getPositionMatrix();
      Matrix3f matrix3f = entry.getNormalMatrix();
      VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
      vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
      vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
      vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
      vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
      matrixStack.pop();

      if(fishingBobberEntity.getOwnerPosition() != null) {
         double x = MathHelper.lerp((double)g, fishingBobberEntity.prevX, fishingBobberEntity.getX());
         double y = MathHelper.lerp((double)g, fishingBobberEntity.prevY, fishingBobberEntity.getY()) + 0.25D;
         double z = MathHelper.lerp((double)g, fishingBobberEntity.prevZ, fishingBobberEntity.getZ());
         float aa = (float)(fishingBobberEntity.getOwnerPosition().getX() - x);
         float ab = (float)(fishingBobberEntity.getOwnerPosition().getY() - y);
         float ac = (float)(fishingBobberEntity.getOwnerPosition().getZ() - z);
         VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLineStrip());
         MatrixStack.Entry entry2 = matrixStack.peek();
         
         for(int ae = 0; ae <= 16; ++ae) {
            RenderLine(aa, ab, ac, vertexConsumer2, entry2, percentage(ae, 16), percentage(ae + 1, 16));
         }
      }

      matrixStack.pop();
      super.render(fishingBobberEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   private static float percentage(int value, int max) {
      return (float)value / (float)max;
   }

   private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
      buffer.vertex(matrix, x - 0.5F, (float)y - 0.5F, 0.0F).color(255, 255, 255, 255).texture((float)u, (float)v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
   }

   private static void RenderLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry normal, float f, float g) {
      float h = x * f;
      float i = y * (f * f + f) * 0.5F + 0.25F;
      float j = z * f;
      float k = x * g - h;
      float l = y * (g * g + g) * 0.5F + 0.25F - i;
      float m = z * g - j;
      float n = MathHelper.sqrt(k * k + l * l + m * m);
      k /= n;
      l /= n;
      m /= n;
      buffer.vertex(normal.getPositionMatrix(), h, i, j).color(0, 0, 0, 255).normal(normal.getNormalMatrix(), k, l, m).next();
   }

   @Override
   public Identifier getTexture(DispenserFishingBobberEntity fishingBobberEntity) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getEntityCutout(TEXTURE);
   }
}

