package BetterMaxHP.patches;


import BetterMaxHP.BetterMaxHP;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static BetterMaxHP.Utils.*;
import static com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass.WATCHER;

public class PlayerPatch {


    public static void reloadAnimation(float ratio) {
        AbstractPlayer player = AbstractDungeon.player;
        String atlasUrl;
        String skeletonUrl;

        switch (player.chosenClass) {
            case IRONCLAD:
                atlasUrl = "images/characters/ironclad/idle/skeleton.atlas";
                skeletonUrl = "images/characters/ironclad/idle/skeleton.json";
                break;
            case THE_SILENT:
                atlasUrl = "images/characters/theSilent/idle/skeleton.atlas";
                skeletonUrl = "images/characters/theSilent/idle/skeleton.json";
                break;
            case DEFECT:
                atlasUrl = "images/characters/defect/idle/skeleton.atlas";
                skeletonUrl = "images/characters/defect/idle/skeleton.json";
                break;
            case WATCHER:
                atlasUrl = "images/characters/watcher/idle/skeleton.atlas";
                skeletonUrl = "images/characters/watcher/idle/skeleton.json";
                break;
            default:
                return;
        }

        try {
            invokeMethod(player,
                    findMethod(player.getClass(), "loadAnimation", String.class, String.class, float.class),
                    atlasUrl,
                    skeletonUrl,
                    ratio);
        } catch (Exception e) {
            BetterMaxHP.logger.error(e);
        }
        player.state.setAnimation(0, "Idle", true);

        if (player.chosenClass == WATCHER) {
            try {
                invokeMethod(player, findMethod(player.getClass(), "loadEyeAnimation"));
                Skeleton skeleton = getField(player, "skeleton");
                Bone bone = skeleton.findBone("eye_anchor");
                setField(player, "eyeBone", bone);
            } catch (Exception e) {
                BetterMaxHP.logger.error(e);
            }
        }
    }

    @SpirePatch(clz = AbstractCreature.class, method = "increaseMaxHp")
    public static class AbstractCreatureIncreaseMaxHPPatch {
        public static void Prefix(AbstractCreature __instance, int amount, boolean showEffect) {
            if (!(__instance instanceof AbstractPlayer)) {
                return;
            }
            AbstractPlayer player = (AbstractPlayer) __instance;
            int base = player.getLoadout().maxHp;
            int newAmount = player.maxHealth + amount;
            float ratio = (float) base / (float) newAmount;
            try {
                reloadAnimation(ratio);
            } catch (Exception e) {
                BetterMaxHP.logger.error(e);
            }
        }
    }

    @SpirePatch(clz = AbstractCreature.class, method = "decreaseMaxHealth")
    public static class AbstractCreatureDecreaseMaxHealthPatch {
        public static void Prefix(AbstractCreature __instance, int amount) {
            if (!(__instance instanceof AbstractPlayer)) {
                return;
            }
            AbstractPlayer player = (AbstractPlayer) __instance;
            int base = player.getLoadout().maxHp;
            int newAmount = player.maxHealth - amount;
            float ratio = (float) base / (float) newAmount;
            try {
                reloadAnimation(ratio);
            } catch (Exception e) {
                BetterMaxHP.logger.error(e);
            }
        }
    }
}
