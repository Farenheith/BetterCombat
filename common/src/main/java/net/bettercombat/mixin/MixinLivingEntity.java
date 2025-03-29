package net.bettercombat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {


    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onLivingHurt(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cbi)
    {
        if (isProtected(getSelf(), source, amount, world)) cbi.setReturnValue(false);
    }

    private LivingEntity getSelf() {
        return (LivingEntity) (Object) this;
    }

    private static boolean isProtected(Entity victim, DamageSource source, float amount, ServerWorld world) {
        final Entity attacker = source.getAttacker();
        if (
            victim == null
            || attacker == null
            || attacker.isInPose(EntityPose.CROUCHING)
            || !attacker.isPlayer()
        ) return false;
        final UUID ownerId = victim instanceof TameableEntity ownable
            ? ownable.getOwnerUuid()
            : null;
        if (ownerId == null) return false;
        if (attacker.getUuid() == ownerId) return true;
        final PlayerEntity owner = world.getPlayerByUuid(ownerId);
        final Team attackerTeam = attacker.getScoreboardTeam();
        if (
            attackerTeam == null
            || attackerTeam.isFriendlyFireAllowed()
        ) return false;
        if (attacker.isTeammate(owner)) return true;

        return false;
    }
}