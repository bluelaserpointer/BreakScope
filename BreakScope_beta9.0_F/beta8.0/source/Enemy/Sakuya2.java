import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Sakuya2 extends EnemyListener{
	boolean bgmPlaying = false;
	SoundClip bgm;
	
	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		bgm = new SoundClip("source/media/night of knights(re2).mp3");
		//月時計ルナ・ダイヤル.wav");
	}

	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //攻撃
		//移動処理
		if(readyTime <= 50){
			int moveSpeed = 6;
			if(bs.enemyWound[id] > 0)
				moveSpeed -= -1;
			bs.AI00(id,moveSpeed,bs.enemyFoundMe.get(id));
		}
		//攻撃処理
		if(aimed){
			if(readyTime >= 190){
				bs.enemyShotFrame[id] = bs.nowFrame;
				if(bs.nowFrame % 3 == 0)
					bs.makeEnemyAttack(id,"妖剣「輝針剣」");
			}else if(130 <= readyTime && readyTime < 180)
				bs.makeEnemyAttack(id,"妖剣「輝針剣」");
				else if(readyTime <= 20){
				if(bs.nowFrame % 4 == 0)
				bs.makeEnemyAttack(id,"妖剣「輝針剣」");
			}else if(30 <= readyTime && readyTime < 90)	
					bs.makeEnemyAttack(id,"妖剣「輝針剣」");
			else if(readyTime == 100 || readyTime == 110 || readyTime == 120){
				if(bs.nowFrame % 5 == 0)
					bs.makeEnemyAttack(id,"妖剣「輝針剣」");
			}
			
		}
		//特殊
		if(!bgmPlaying && bs.enemyFoundMe.get(id)){
			bgmPlaying = true;
			bs.battleBGM[bs.nowBattleBGM].stop();
			bgm.play();
		}
		if(!bs.enemyFoundMe.get(id) && bgmPlaying){
			bgmPlaying = false;
			bgm.stop();
			bs.battleBGM[bs.nowBattleBGM].play();
		}
		//描画処理
		if(playerDistance < 300){ //かえるフェード処理
			final int x_int = (int)x,y_int = (int)y;
			bs.g2.rotate(angle,x_int,y_int);
			final Image img = bs.enemyImg[bs.enemyKind[id]];
			if(playerDistance > 100){ //100~300の間は薄くなる
				try{
					bs.g2.setComposite(AlphaComposite.SrcOver.derive((float)(300 - playerDistance)*0.01F)); //遠くになるにつれ薄く見える
				}catch(java.lang.IllegalArgumentException e){
					bs.g2.setComposite(AlphaComposite.SrcOver);
				}
				bs.g2.drawImage(img,x_int - img.getWidth(null)/2,y_int - img.getHeight(null)/2,bs); //描画
				bs.g2.setComposite(AlphaComposite.SrcOver);
			}else{ //200以内ははっきり見える
				bs.g2.drawImage(img,x_int - img.getWidth(null)/2,y_int - img.getHeight(null)/2,bs); //描画
			}
			bs.g2.rotate(-angle,x_int,y_int);
		} //300以上離れると描画されない
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
	}
}