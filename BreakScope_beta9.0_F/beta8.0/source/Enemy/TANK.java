import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import static java.lang.Math.*;

public class TANK extends EnemyListener{
	transient SoundClip tankAttackSE, //追加SE-砲撃音
		tankDamageSE, //追加SE-ダメージ音
		tankBurstSE; //追加SE-爆破音
	transient Image tankBodyImg; //追加画像-車体
	
	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		tankBodyImg = bs.loadImage("tank/body.png");
		tankAttackSE = new SoundClip("source/media/sen_ge_taihou07.wav");
		tankDamageSE = new SoundClip("source/media/sen_ta_dame_gion02.wav");
		tankBurstSE = new SoundClip("source/media/se_maoudamashii_explosion06.wav");
	}
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //基本処理
		//移動処理
		super.defaultMove(id);
		//攻撃処理
		final int x_int = (int)x,y_int = (int)y;
		if(aimed){
			bs.AI01(id,2,bs.enemyFoundMe.get(id));
			if(aimed){
				if(readyTime >= 100){
					bs.setDefaultEnemyBullet(id,"ARTILLERY",x,y,5,50,1);
					bs.enemyShotFrame[id] = bs.nowFrame;
					tankAttackSE.stop();
					tankAttackSE.play();
				}
			}
		}
		//描画処理
		final int pictureSize2 = (int)(tankBodyImg.getWidth(null)*1.5);
		if(bs.outOfScreen_pixel(x_int,y_int,pictureSize2,pictureSize2)) //スクリーン外のときは描画しない
			return;
		final double angle2 = atan2(bs.enemyYPower[id],bs.enemyXPower[id]);
		bs.g2.rotate(angle2,x_int,y_int); //車体描画
		bs.drawImageBS_centerDot(tankBodyImg,x_int,y_int);
		bs.g2.rotate(angle - angle2,x_int,y_int); //砲塔描画
		bs.drawImageBS_centerDot(bs.enemyImg[bs.enemyKind[id]],x_int,y_int);
		bs.g2.rotate(-angle,x_int,y_int);
		final int hp = bs.enemyHP[id]; //hp描画
		if(hp != MAX && hp != MIN){
			bs.g2.setColor(Color.BLACK);
			bs.g2.drawString(String.valueOf(hp),x_int,y_int);
		}
	}
	@Override
	public void editorPaint(int id,int x,int y,double angle,boolean inScreen){ //エディター描画処理
		if(!inScreen) //スクリーン外のときは描画しない
			return;
		final Image img = tankBodyImg;
		bs.g2.drawImage(img,x - img.getWidth(null)/2,y - img.getHeight(null)/2,bs); //車体描画(向き固定)
		bs.g2.rotate(angle,x,y); //砲塔描画
		final Image img2 = bs.enemyImg[bs.enemyKind[id]];
		bs.g2.drawImage(img2,x - img2.getWidth(null)/2,y - img2.getHeight(null)/2,bs);
		bs.g2.rotate(-angle,x,y);
		final int hp = bs.enemyHP[id]; //hp描画
		if(hp != MAX && hp != MIN){
			bs.g2.setColor(Color.BLACK);
			bs.g2.drawString(String.valueOf(hp),x,y);
		}
	}
	@Override
	public void damaged(int id,int dmg){ //傷害追加処理
		tankDamageSE.stop();
		tankDamageSE.play(); //攻撃されると効果音が出る
	}
	@Override
	public void killed(int id){ //死亡追加処理
		final int x = (int)bs.enemyX[id],y = (int)bs.enemyY[id];
		bs.setEffect("EXPLOSION1",x,y,25);
		bs.setEffect("EXPLOSION2",x,y,25);
		bs.setEffect("SMOKE",x,y,bs.random(4,6));
		tankBurstSE.stop();
		tankBurstSE.play();
		bs.setItem(bs.AMMO_ROCKET,x,y,bs.random(2,4)); //2~4発ロケット弾を落とす
	}
}