import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import static java.lang.Math.*;

public class Frog extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //基本処理
		//移動処理
		super.defaultMove(id);
		//攻撃処理
		if(aimed && bs.nowFrame % 2 == 0)
			bs.setDefaultEnemyBullet(id,"ACID",x,y,toRadians(bs.nowTime),0,20,1);
		//描画処理
		if(!inScreen) //スクリーン外のときは描画しない
			return;
		if(playerDistance < 300){ //かえるフェード処理
			final int x_int = (int)x,y_int = (int)y;
			bs.g2.rotate(angle,x_int,y_int);
			final Image img = bs.enemyImg[bs.enemyKind[id]];
			if(playerDistance > 200){ //200~300の間は薄くなる
				bs.g2.setComposite(AlphaComposite.SrcOver.derive((float)(300 - playerDistance)*0.01F)); //遠くになるにつれ薄く見える
				bs.drawImageBS_centerDot(img,x_int,y_int); //描画
				bs.g2.setComposite(AlphaComposite.SrcOver);
			}else{ //200以内ははっきり見える
				bs.drawImageBS_centerDot(img,x_int,y_int); //描画
			}
			bs.g2.rotate(-angle,x_int,y_int);
		} //300以上離れると描画されない
	}
	@Override
	public void damaged(int id,int dmg){ //傷害追加処理
		bs.setEffect("SPARK_GREEN",(int)bs.enemyX[id],(int)bs.enemyY[id],bs.random(3,6)); //攻撃されるとエフェクトが出る
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
		if(bs.rnd.nextInt(10) < 5) //50%で回復アイテムを落とす
			bs.setItem(bs.RECOVERY_PACK,bs.random(1,3),(int)bs.enemyX[id],(int)bs.enemyY[id]);
	}
}