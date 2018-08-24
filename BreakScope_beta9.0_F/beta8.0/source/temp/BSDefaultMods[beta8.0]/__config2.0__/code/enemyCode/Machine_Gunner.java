import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Machine_Gunner extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance){ //基本処理
		//攻撃処理
		boolean shooting = false;
		if(aimed){
			if(readyTime >= 120 || readyTime <= 60 && readyTime % 3 == 0){
				bs.setDefaultEnemyBullet(id,"BLACK_BULLET",x,y,6,20,1);
				shooting = true;
				if(readyTime >= 120)
					bs.enemyShotFrame[id] = bs.nowFrame;
			}
		}
		//移動処理
		if(!shooting){ //射撃中は止まる
			super.defaultMove(id);
		}
		//描画処理
		super.defaultPaint(id,x,y,angle);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
		bs.setItem(bs.AMMO_ASSAULT_RIFLE,bs.random(90,120),(int)bs.enemyX[id],(int)bs.enemyY[id]); //90~120発のアサルトライフル弾を落とす
	}
}