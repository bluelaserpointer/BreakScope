import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Yuyuko extends EnemyListener{
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //攻撃
		//移動処理
		if(readyTime <= 50){
			int moveSpeed = 3;
			if(bs.enemyWound[id] > 0)
				moveSpeed -= 3;
			bs.AI00(id,moveSpeed,bs.enemyFoundMe.get(id));
		}
		//攻撃処理
		if(aimed){
			if(70 <=readyTime && readyTime < 100){
				bs.enemyShotFrame[id] = bs.nowFrame;
				if(bs.nowFrame % 7 == 0)
				bs.makeEnemyAttack(id,"楼観剣");
			}else if(10 <= readyTime && readyTime < 30){
			    if(bs.nowFrame % 6 == 0)
				bs.makeEnemyAttack(id,"幻扇");
			}else if(readyTime == 40 || readyTime == 50 || readyTime == 60){
			if(bs.nowFrame % 3 == 0)
				bs.makeEnemyAttack(id,"拡散蝶射撃");
		    }else if(readyTime >= 110){
				bs.enemyShotFrame[id] = bs.nowFrame;
				if(bs.nowFrame % 5 == 0)
					bs.makeEnemyAttack(id,"人魂");
			}
	    }   
		//描画処理
		if(playerDistance < 100){ //かえるフェード処理
			final int x_int = (int)x,y_int = (int)y;
			bs.g2.rotate(angle,x_int,y_int);
			final Image img = bs.enemyImg[bs.enemyKind[id]];
			if(playerDistance > 50){ //50~100の間は薄くなる
				try{
					bs.g2.setComposite(AlphaComposite.SrcOver.derive((float)(100 - playerDistance)*0.01F)); //遠くになるにつれ薄く見える
				}catch(java.lang.IllegalArgumentException e){
					bs.g2.setComposite(AlphaComposite.SrcOver);
				}
				bs.g2.drawImage(img,x_int - img.getWidth(null)/2,y_int - img.getHeight(null)/2,bs); //描画
				bs.g2.setComposite(AlphaComposite.SrcOver);
			}else{ //50以内ははっきり見える
				bs.g2.drawImage(img,x_int - img.getWidth(null)/2,y_int - img.getHeight(null)/2,bs); //描画
			}
			bs.g2.rotate(-angle,x_int,y_int);
		} //100以上離れると描画されない
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
	}
}