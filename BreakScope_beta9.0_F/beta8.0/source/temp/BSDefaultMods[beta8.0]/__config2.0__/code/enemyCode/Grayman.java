import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Grayman extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance){ //攻撃
		//移動処理
		super.defaultMove(id);
		//攻撃処理
		if(aimed){
			if(readyTime >= 80 || readyTime == 15 || readyTime == 30){
				bs.setDefaultEnemyBullet(id,bs.cfg.nameToID_bullet.get("BLACK_BULLET"),x,y,3,18,1);
				if(readyTime >= 80)
					bs.enemyShotFrame[id] = bs.nowFrame;
			}
		}
		//描画処理
		super.defaultPaint(id,x,y,angle);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
		final int settingItem = bs.rnd.nextInt(9); //ランダムでアイテムを落とす
		if(settingItem == bs.RECOVERY_PACK)
			bs.setItem(bs.RECOVERY_PACK,bs.random(5,10),(int)bs.enemyX[id],(int)bs.enemyY[id]);
		else
			bs.setItem(settingItem,bs.random(12,24),(int)bs.enemyX[id],(int)bs.enemyY[id]);
	}
}