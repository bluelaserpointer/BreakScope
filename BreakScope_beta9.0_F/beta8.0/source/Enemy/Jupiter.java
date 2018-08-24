import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Jupiter extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //攻撃
		//移動処理
		if(readyTime <= 50){
			int moveSpeed = 2;
			if(bs.enemyWound[id] > 0)
				moveSpeed -= 0;
			bs.AI00(id,moveSpeed,bs.enemyFoundMe.get(id));
		}
		//攻撃処理
		if(aimed){
			if(readyTime >= 150){
				bs.enemyShotFrame[id] = bs.nowFrame;
				bs.makeEnemyAttack(id,"hotjupiter");
			}else if(10 <= readyTime && readyTime < 50)
				bs.makeEnemyAttack(id,"veiljupiter");
			else if(readyTime == 60 || readyTime == 70 || readyTime == 80)
				bs.makeEnemyAttack(id,"bulletjupiter");
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