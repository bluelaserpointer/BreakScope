import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class B_SHESS extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //攻撃
		//移動処理
		if(readyTime <= 50){
			int moveSpeed = 6;
			if(bs.enemyWound[id] > 0)
				moveSpeed -= 3;
			bs.AI00(id,moveSpeed,bs.enemyFoundMe.get(id));
		}
		//攻撃処理
		if(aimed){
			if(readyTime >= 150){
				bs.enemyShotFrame[id] = bs.nowFrame;
				bs.makeEnemyAttack(id,"SuperRocket");
			}else if(10 <= readyTime && readyTime < 50)
				bs.makeEnemyAttack(id,"自動小銃");
			else if(readyTime == 60 || readyTime == 70 || readyTime == 80)
				bs.makeEnemyAttack(id,"グロウリング投射機");
		}
		//描画処理
		super.defaultPaint(id,x,y,angle,inScreen);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
	}
}