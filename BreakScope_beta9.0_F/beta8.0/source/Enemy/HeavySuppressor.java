import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class HeavySuppressor extends EnemyListener{
	transient SoundClip attackSE;

	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		attackSE = new SoundClip("source/media/gun1.wav");
	}
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //攻撃
		//移動処理
		super.defaultMove(id);
		//攻撃処理
		if(aimed){
			if(readyTime >= 160 || readyTime == 40 || readyTime == 80){
				bs.setDefaultEnemyBullet(id,"BLACK_BULLET",x,y,10,25,15);
				attackSE.stop();
				attackSE.play();
				if(readyTime >= 160)
					bs.enemyShotFrame[id] = bs.nowFrame;
			}
		}
		//描画処理
		super.defaultPaint(id,x,y,angle,inScreen);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.setItem(bs.AMMO_SHOTGUN,bs.random(20,40),(int)bs.enemyX[id],(int)bs.enemyY[id]);
	}
}