package io.github.jason13official.examplemod.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin extends EntityRenderer<FishingHook> {

  @Unique
  private static final int examplemod$PIXEL_SIZE = 2;
  @Unique
  private static final int examplemod$SEGMENTS = 32;
  @Unique
  private static float examplemod$dx, examplemod$dy, examplemod$dz;

  protected FishingHookRendererMixin(EntityRendererProvider.Context context) {
    super(context);
  }

  /// skip vanilla string
  @Inject(method = "stringVertex", at = @At("HEAD"), cancellable = true)
  private static void examplemod$stringVertex(float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose, float stringFraction, float nextStringFraction, CallbackInfo ci) {
    examplemod$dx = x;
    examplemod$dy = y;
    examplemod$dz = z;
    ci.cancel();
  }

  @Unique
  private static void examplemod$cellQuad(VertexConsumer consumer, Matrix4f invMvp, int cx, int cy, float ndcZ, float cell, float width, float height) {
    examplemod$corner(consumer, invMvp, cx, cy, ndcZ, cell, width, height);
    examplemod$corner(consumer, invMvp, cx + 1, cy, ndcZ, cell, width, height);
    examplemod$corner(consumer, invMvp, cx + 1, cy + 1, ndcZ, cell, width, height);
    examplemod$corner(consumer, invMvp, cx, cy + 1, ndcZ, cell, width, height);
  }

  @Unique
  private static void examplemod$corner(VertexConsumer consumer, Matrix4f invMvp, int gx, int gy, float ndcZ, float cell, float width, float height) {

    float ndcX = gx * cell / width * 2.0F - 1.0F;
    float ndcY = gy * cell / height * 2.0F - 1.0F;
    Vector4f v = invMvp.transform(new Vector4f(ndcX, ndcY, ndcZ, 1.0F));

    // camera-relative world coords; the model-view matrix applies camera rotation at draw time
    consumer.addVertex(v.x / v.w, v.y / v.w, v.z / v.w).setColor(0xFF000000);
  }

  /// actually render the pixel string
  @Inject(method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
  private void examplemod$render(FishingHook entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {

    float dx = examplemod$dx;
    float dy = examplemod$dy;
    float dz = examplemod$dz;

    // camera-relative world -> clip space, using same matrices as the level renderer
    Quaternionf invCamRot = this.entityRenderDispatcher.camera.rotation().conjugate(new Quaternionf());
    Matrix4f mvp = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(new Matrix4f().rotation(invCamRot));
    Matrix4f invMvp = new Matrix4f(mvp).invert();
    Matrix4f pose = poseStack.last().pose(); // translation from camera to the hook entity

    Window window = Minecraft.getInstance().getWindow();
    float width = window.getWidth();
    float height = window.getHeight();
    float cell = (float) (examplemod$PIXEL_SIZE * window.getGuiScale());

    // project the curve samples to (cellX, cellY, ndcZ); null when behind the camera
    float[][] pts = new float[examplemod$SEGMENTS + 1][];
    for (int i = 0; i <= examplemod$SEGMENTS; i++) {
      float t = (float) i / examplemod$SEGMENTS;
      Vector3f p = pose.transformPosition(dx * t, dy * (t * t + t) * 0.5F + 0.25F, dz * t, new Vector3f());
      Vector4f clip = mvp.transform(new Vector4f(p, 1.0F));
      if (clip.w <= 0.05F) {
        continue;
      }
      float sx = (clip.x / clip.w * 0.5F + 0.5F) * width / cell;
      float sy = (clip.y / clip.w * 0.5F + 0.5F) * height / cell;
      pts[i] = new float[]{sx, sy, clip.z / clip.w};
    }

    VertexConsumer consumer = buffer.getBuffer(RenderType.debugQuads());
    int lastX = Integer.MIN_VALUE;
    int lastY = Integer.MIN_VALUE;
    for (int i = 0; i < examplemod$SEGMENTS; i++) {

      float[] a = pts[i];
      float[] b = pts[i + 1];

      if (a == null || b == null) {
        continue;
      }

      // bresenham-style walk between the two samples

      int x0 = (int) Math.floor(a[0]);
      int y0 = (int) Math.floor(a[1]);
      int x1 = (int) Math.floor(b[0]);
      int y1 = (int) Math.floor(b[1]);

      int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
      for (int s = 0; s <= steps; s++) {
        float f = steps == 0 ? 0.0F : (float) s / steps;
        int cx = Math.round(x0 + (x1 - x0) * f);
        int cy = Math.round(y0 + (y1 - y0) * f);
        if (cx == lastX && cy == lastY) {
          continue;
        }
        lastX = cx;
        lastY = cy;
        float ndcZ = a[2] + (b[2] - a[2]) * f; // ndc depth is linear in screen space
        examplemod$cellQuad(consumer, invMvp, cx, cy, ndcZ, cell, width, height);
      }
    }
  }
}
