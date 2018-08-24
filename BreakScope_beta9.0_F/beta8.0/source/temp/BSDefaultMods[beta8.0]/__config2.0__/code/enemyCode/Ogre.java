import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Ogre extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance){ //基本処理
		//移動処理
		super.defaultMove(id);
		//攻撃処理
		if(playerDistance < 150 && bs.enemyFoundMe.get(id)){
			bs.playerTargetHP--;
			bs.HPChangedFrame = bs.nowFrame;
			bs.setEffect(bs.cfg.nameToID_effect.get("BLOOD"),bs.playerX,bs.playerY,bs.random(3,6));
		}
		//描画処理
		super.defaultPaint(id,x,y,angle);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
		if(bs.rnd.nextInt(10) < 3) //30%でバッテリーを落とす
			bs.setItem(bs.AMMO_BATTERY,bs.random(500,1000),(int)bs.enemyX[id],(int)bs.enemyY[id]);
	}
}