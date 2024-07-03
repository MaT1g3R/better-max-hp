package BetterMaxHP.patches;


import BetterMaxHP.BetterMaxHP;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.*;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;

import java.lang.reflect.Field;

public class PlayerPatch {
    private static Field findField(Class clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return findField(superClass, fieldName);
            }
        }
    }

    public static <A> A getField(Object obj, Class<?> clz, String name) {
        Field field = null;
        A result = null;
        try {
            field = findField(clz, name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            result = (A) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <A> void setField(Object obj, Class<?> clz, String name, A value) {
        Field field = null;
        try {
            field = findField(clz, name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadAnimation(AbstractPlayer player, String atlasUrl, String skeletonUrl, float scale) {
        Class<AbstractCreature> clz = AbstractCreature.class;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasUrl));
        setField(player, clz, "atlas", atlas);
        SkeletonJson json = new SkeletonJson(atlas);

        if (CardCrawlGame.dungeon != null && AbstractDungeon.player != null) {
            if (AbstractDungeon.player.hasRelic("PreservedInsect") &&
                    !player.isPlayer &&
                    AbstractDungeon.getCurrRoom().eliteTrigger) {
                scale += 0.3F;
            }

            if (ModHelper.isModEnabled("MonsterHunter") && !player.isPlayer) {
                scale -= 0.3F;
            }
        }

        json.setScale(Settings.renderScale / scale);
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal(skeletonUrl));

        Skeleton skeleton = new Skeleton(skeletonData);
        setField(player, clz, "skeleton", skeleton);
        skeleton.setColor(Color.WHITE);

        AnimationStateData stateData = new AnimationStateData(skeletonData);
        setField(player, clz, "stateData", stateData);
        player.state = new AnimationState(stateData);
    }

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

        loadAnimation(player, atlasUrl, skeletonUrl, ratio);
        player.state.setAnimation(0, "Idle", true);
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
