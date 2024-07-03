package BetterMaxHP;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpireInitializer
public class BetterMaxHP implements PostInitializeSubscriber {
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
}
