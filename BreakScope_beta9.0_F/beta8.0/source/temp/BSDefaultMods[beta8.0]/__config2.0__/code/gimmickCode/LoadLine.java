import static java.lang.Math.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;

public class LoadLine extends GimmickListener{
	
	final double RIGHT = 0,DOWN = PI*0.5,LEFT = PI,UP = PI*1.5; //ラジアン方向定数
	final int STRAIGHT = 0,BENDED = 1,T_JUNCTION = 2,CROSSED = 3; //パーツ種類定数

	//ライン画像
	final Image[] lineImg = new Image[4];
	
	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		//各パーツの画像を読み込み
		lineImg[STRAIGHT] = bs.loadImage("loadLine/straight.png");
		lineImg[BENDED] = bs.loadImage("loadLine/bended.png");
		lineImg[T_JUNCTION] = bs.loadImage("loadLine/t_junction.png");
		lineImg[CROSSED] = bs.loadImage("loadLine/crossed.png");
	}
	@Override
	public void gamePaint(int map){ //基本処理
		bs.g2.setComposite(AlphaComposite.DstOver); //今までより下に描画
		final int mapX = map/50,mapY = map%50; 
		final int x = mapX*100 + 50,y = mapY*100 + 50; //座標情報に変換
		final int myKind = bs.gimmickKind[map];
		int conjuctions = 0; //接続数
		boolean[] isConjucting = new boolean[4]; //接続方向boolean
		final int mapX_50 = mapX*50;
		if(bs.gimmickKind[mapX_50 + 50 + mapY] == myKind){
			conjuctions++;
			isConjucting[0] = true;
		}
		if(bs.gimmickKind[mapX_50 - 50 + mapY] == myKind){
			conjuctions++;
			isConjucting[2] = true;
		}
		if(bs.gimmickKind[mapX_50 + mapY + 1] == myKind){
			conjuctions++;
			isConjucting[1] = true;
		}
		if(bs.gimmickKind[mapX_50 + mapY - 1] == myKind){
			conjuctions++;
			isConjucting[3] = true;
		}
		double angle = 0.0D; //回転角度
		int parts; //パーツ種類
		switch(conjuctions){
		case 1:
			parts = STRAIGHT;
			if(isConjucting[0])
				angle = RIGHT;
			else if(isConjucting[1])
				angle = DOWN;
			else if(isConjucting[2])
				angle = LEFT;
			else if(isConjucting[3])
				angle = UP;
			break;
		case 2:
			if(isConjucting[0]){
				if(isConjucting[3]){
					angle = UP;
					parts = BENDED;
				}else{
					if(isConjucting[1])
						parts = BENDED;
					else
						parts = STRAIGHT;
					angle = RIGHT;
				}
			}else{
				if(isConjucting[1] && isConjucting[3]){
					parts = STRAIGHT;
					angle = DOWN;
				}else{
					parts = BENDED;
					if(isConjucting[1])
						angle = DOWN;
					else
						angle = LEFT;
				}
			}
			break;
		case 3:
			parts = T_JUNCTION;
			if(!isConjucting[0])
				angle = LEFT;
			else if(!isConjucting[1])
				angle = UP;
			else if(!isConjucting[2])
				angle = RIGHT;
			else if(!isConjucting[3])
				angle = DOWN;
			break;
		default:
			parts = CROSSED;
		}
		bs.g2.rotate(angle,x,y);
		bs.g2.drawImage(lineImg[parts],x - 50,y - 50,100,100,bs);
		bs.g2.rotate(-angle,x,y);
		bs.g2.setComposite(AlphaComposite.SrcOver);
	}
}