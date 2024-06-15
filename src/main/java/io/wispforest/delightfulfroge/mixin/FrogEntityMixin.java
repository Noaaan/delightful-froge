package io.wispforest.delightfulfroge.mixin;

import io.wispforest.delightfulfroge.DelightfulFroge;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrogEntity.class)
public abstract class FrogEntityMixin extends MobEntity {

    protected FrogEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    @Final
    public AnimationState usingTongueAnimationState;

    @Shadow
    public abstract RegistryEntry<FrogVariant> getVariant();

    @Shadow
    public abstract void setVariant(RegistryEntry<FrogVariant> registryEntry);

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.isSneaking() || !player.getStackInHand(hand).isEmpty() || this.getVariant().value() != DelightfulFroge.FROGE) {
            return super.interactMob(player, hand);
        }
        if (this.getWorld().isClient) {
            for (int i = 0; i < 5; i++) {
                double x = this.getX() + (this.random.nextDouble() - .5);
                double y = this.getY() + (this.random.nextDouble() - .5) + .25;
                double z = this.getZ() + (this.random.nextDouble() - .5);

                this.getWorld().addParticle(ParticleTypes.HEART, x, y, z, 0, 0, 0);
                this.getWorld().addParticle(ParticleTypes.WAX_ON, x, y + .4, z, this.random.nextDouble() * 2.5, this.random.nextDouble() * 2.5, this.random.nextDouble() * 2.5);
            }

            this.usingTongueAnimationState.start(this.age);
        }
        return ActionResult.SUCCESS;
    }

    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FrogBrain;coolDownLongJump(Lnet/minecraft/entity/passive/FrogEntity;Lnet/minecraft/util/math/random/Random;)V"))
    private void injectFroge(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        if (world.getBiome(this.getBlockPos()).isIn(DelightfulFroge.FROGE_PLACES)) {
            this.setVariant(Registries.FROG_VARIANT.getEntry(DelightfulFroge.FROGE));
            return;
        }

        final var chance = serverWorld.getGameRules().getInt(DelightfulFroge.FROGE_CHANCE);
        if (serverWorld.random.nextBetween(0, 100) > chance) return;

        this.setVariant(Registries.FROG_VARIANT.getEntry(DelightfulFroge.FROGE));
    }

}
