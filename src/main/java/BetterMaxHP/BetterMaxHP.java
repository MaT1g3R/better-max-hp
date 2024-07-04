package BetterMaxHP;

import basemod.BaseMod;
import basemod.interfaces.MaxHPChangeSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static BetterMaxHP.Utils.*;
import static com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass.WATCHER;

@SpireInitializer
public class BetterMaxHP implements PostInitializeSubscriber, MaxHPChangeSubscriber {
    public static final Logger logger = LogManager.getLogger(BetterMaxHP.class.getName());

    public BetterMaxHP() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new BetterMaxHP();
    }

    @Override
    public void receivePostInitialize() {

    }

    @Override
    public int receiveMaxHPChange(int amount) {
        AbstractPlayer player = AbstractDungeon.player;

        int base = player.getLoadout().maxHp;
        int newAmount = player.maxHealth + amount;
        float ratio = (float) base / (float) newAmount;

        reloadAnimation(player, ratio);

        return amount;
    }

    public static void reloadAnimation(AbstractPlayer player, float ratio) {
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
}
