package BetterMaxHP.patches;


import BetterMaxHP.BetterMaxHP;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass.WATCHER;

public class PlayerPatch {

    private static Method findMethod(Class clz, String methodName, Class... parameterTypes)
            throws NoSuchMethodException {
        try {
            return clz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class superClass = clz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return findMethod(superClass, methodName, parameterTypes);
        }
    }

    private static Field findField(Class clz, String fieldName) throws NoSuchFieldException {
        try {
            return clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return findField(superClass, fieldName);
        }
    }

    private static Object invokeMethod(Object object, Method method, Object... args)
            throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(object, args);
    }

    private static <A> A getField(Object obj, String name) {
        Field field;
        A result;
        try {
            field = findField(obj.getClass(), name);
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

    private static <A> void setField(Object obj, String name, A value) {
        Field field;
        try {
            field = findField(obj.getClass(), name);
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
