import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import static java.lang.Math.*;

public class Bike extends EntityListener{ //バイク
	
	//追加内部データ
	final double[] anglePower = new double[1024]; //残存回転加速度
	
	@Override
	public void gamePaint(int id,double x,double y,double angle){ //ゲーム用描画処理
		bs.g2.setColor(Color.BLACK);
		final int kind = bs.entityKind[id];
		//操縦処理
		if(bs.playerRidingID == id){ //このエンティティを操縦
			final double bikeAccel = bs.sneak ? 0.6 : 2.0,bikeAccel_sqrt2 = bikeAccel/sqrt(2); //バイク加速力と×sin45°定数
			double turning = 0.0;
			if(bs.leftKey){
				if(bs.upKey){
					bs.entityXPower[id] -= bikeAccel_sqrt2;
					bs.entityYPower[id] -= bikeAccel_sqrt2;
					this.turnBike(id,-PI*3/4);
				}else if(bs.downKey){
					bs.entityXPower[id] -= bikeAccel_sqrt2;
					bs.entityYPower[id] += bikeAccel_sqrt2;
					this.turnBike(id,PI*3/4);
				}else{
					bs.entityXPower[id] -= bikeAccel;
					this.turnBike(id,PI);
				}
			}else if(bs.rightKey){
				if(bs.upKey){
					bs.entityXPower[id] += bikeAccel_sqrt2;
					bs.entityYPower[id] -= bikeAccel_sqrt2;
					this.turnBike(id,-PI/4);
				}else if(bs.downKey){
					bs.entityXPower[id] += bikeAccel_sqrt2;
					bs.entityYPower[id] += bikeAccel_sqrt2;
					this.turnBike(id,PI/4);
				}else{
					bs.entityXPower[id] += bikeAccel;
					this.turnBike(id,0);
				}
			}else{
				if(bs.upKey){
					bs.entityYPower[id] -= bikeAccel;
					this.turnBike(id,-PI/2);
				}else if(bs.downKey){
					bs.entityYPower[id] += bikeAccel;
					this.turnBike(id,PI/2);
				}
			}
			if(bs.actioned){ //降車処理
				bs.playerRidingID = bs.NONE;
				bs.actioned = false;
				if(turning != 0.0)
					anglePower[id] = turning;
			}
		}else if(bs.actioned && bs.playerRidingID == bs.NONE
			&& abs(bs.playerX - (int)x) < 50 && abs(bs.playerY - (int)y) < 50){ //乗車処理
			bs.playerRidingID = id;
			bs.actioned = false;
			bs.playerX = (int)x;
			bs.playerY = (int)y;
		}else if(anglePower[id] != 0.0){
			double anglePower2 = anglePower[id];
			bs.entityAngle[id] += anglePower2;
			anglePower2 *= abs(anglePower2) > 0.01 ? 0.8 : 0;
			anglePower[id] = anglePower2;
		}
		//移動処理
		double angle2 = bs.entityAngle[id],
			xPower = bs.entityXPower[id],yPower = bs.entityYPower[id];
		if(xPower != 0 || yPower != 0){
			final int targetX = (int)(x + xPower),targetY = (int)(y + yPower);
			final int hitMap = bs.getSquareHitGimmickID(targetX,targetY,bs.cfg.entitySize[kind]);
			if(hitMap != bs.NONE || targetX < 700 || 4300 < targetX //何かのブロックに衝突
						|| targetY < 700 || 4300 < targetY){ //またはステージ外へ侵入
				bs.entityXPower[id] *= -0.7;
				bs.entityYPower[id] *= -0.7;
			}
			//動力を加算
			x += bs.entityXPower[id];
			y += bs.entityYPower[id];
			bs.entityX[id] = x;
			bs.entityY[id] = y;
			if(bs.playerRidingID == id){
				bs.playerX = (int)x;
				bs.playerY = (int)y;
				if(!bs.bulletSwitch)
					bs.playerAngle = angle2;
			}
			//敵をひく処理
			final double totalSpeed = sqrt(pow(bs.entityXPower[id],2) + pow(bs.entityYPower[id],2));
			if(totalSpeed > 0.1){
				for(int j = 0;j <= bs.enemy_maxID;j++){
					if(bs.enemyKind[j] == bs.NONE)
						continue;
					final int halfEnemySize = bs.cfg.enemySize[bs.enemyKind[j]]/2;
					if(abs(bs.enemyX[j] - x) < 40 + halfEnemySize && abs(bs.enemyY[j] - y) < 40 + halfEnemySize && bs.enemyHP[j] > 0){ //衝突
						bs.enemyHP[j] -= totalSpeed/2;
						bs.enemyXPower[j] += bs.entityXPower[id]*1.5;
						bs.enemyYPower[j] += bs.entityYPower[id]*1.5;
					}
				}
			}
		}
		//動力減衰
		bs.entityXPower[id] *= abs(bs.entityXPower[id]) > 0.05 ? 0.95 : 0.0;
		bs.entityYPower[id] *= abs(bs.entityXPower[id]) > 0.05 ? 0.95 : 0.0;
		//ダメージ処理
		bs.entityHP[id] = bs.receiveBulletInArea(bs.entityHP[id],(int)x,(int)y,bs.cfg.entitySize[kind],bs.PLAYER);
		//描画
		bs.g2.rotate(angle,(int)x,(int)y);
		bs.g2.drawImage(bs.entityImg[kind],(int)x - bs.entityImgW[kind]/2,(int)y - bs.entityImgH[kind]/2,bs);
		bs.g2.drawString(String.valueOf(bs.entityHP[id]),(int)x,(int)y);
		bs.g2.rotate(-angle,(int)x,(int)y);
	}
	private void turnBike(int id,double targetAngle){ //バイク回転処理
		final double angle2 = (targetAngle - bs.entityAngle[id])%(PI*2); //必要回転角
		if(toDegrees(abs(angle2)) < 3)
			return;
		bs.entityAngle[id] = bs.angleFormat(bs.entityAngle[id] + bs.angleFormat(angle2)/5); //5フレーム以内に到達
	}
	@Override
	public void created(int id,String dataString){ //生成処理
		anglePower[id] = 0.0;
	}
}