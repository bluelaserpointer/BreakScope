import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Sniper extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance){ //基本処理
		//移動はしない
		//攻撃処理
		if(aimed){
			if(readyTime >= 80 || readyTime == 15 || readyTime == 30){
				bs.setDefaultEnemyBullet(id,"BLACK_BULLET",x,y,3,18,1);
				if(readyTime >= 80)
					bs.enemyShotFrame[id] = bs.nowFrame;
			}
		}else if(!bs.enemyFoundMe.get(id)) //自ら向きを変えて警備する
			bs.enemyTargetAngle[id] += (bs.enemyHP[id] + id) % 2 == 0 ? 0.01 : -0.01;
		//描画処理
		super.defaultPaint(id,x,y,angle);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
		bs.setItem(bs.AMMO_SNIPER,bs.random(10,20),(int)bs.enemyX[id],(int)bs.enemyY[id]); //10~20発スナイパーライフル弾を落とす
	}
}