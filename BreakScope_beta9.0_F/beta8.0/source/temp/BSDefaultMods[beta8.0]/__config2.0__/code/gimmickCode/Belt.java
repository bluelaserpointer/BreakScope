import static java.lang.Math.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;

public class Belt extends GimmickListener{
		
	final int[] beltSpeed = new int[2500]; //ベルトスピード0~50
	final double[] direction = new double[2500]; //ベルト方向
	
	final double RIGHT = 0,DOWN = PI*0.5,LEFT = PI,UP = PI*1.5; //ベルト方向定数
	
	private boolean gameActivation; //エディター実行モードであるか
	
	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		if(!(bs instanceof BreakScope_Editor))
			gameActivation = true;
	}
	@Override
	public void gamePaint(int map){ //基本処理
		bs.g2.setComposite(AlphaComposite.DstOver); //今までより下に描画
		final int x = map/50*100 + 50,y = map%50*100 + 50; //座標情報に変換
		final Shape CLIP = bs.g2.getClip(); //もともとのトリミング範囲を保存
		bs.g2.setClip(x - 50,y - 50,100,100);
		final double angle = direction[map]; //回転角度
		bs.g2.rotate(angle,x,y);
		final int x2 = x + (bs.nowFrame*beltSpeed[map])%25;
		for(int k = -3;k < 2;k++)
			bs.g2.drawImage(bs.gimmickImg[bs.gimmickKind[map]],x2 + k*25,y - 50,bs);
		bs.g2.rotate(-angle,x,y);
		bs.g2.setClip(CLIP);
		bs.g2.setComposite(AlphaComposite.SrcOver);
	}
	@Override
	public void created(int id,String dataString){ //追加処理
		final String[] str = dataString.split("_"); //固有情報分解
		final int speed = Integer.parseInt(str[1]);
		beltSpeed[id] = speed; //速度
		final String s = str[0]; //運搬向き
		if(s.equals("right")){
			direction[id] = RIGHT;
			if(gameActivation)
				bs.xForceMap[id] += speed;
		}else if(s.equals("up")){
			direction[id] = UP;
			if(gameActivation)
				bs.yForceMap[id] -= speed;
		}else if(s.equals("left")){
			direction[id] = LEFT;
			if(gameActivation)
				bs.xForceMap[id] -= speed;
		}else{
			direction[id] = DOWN;
			if(gameActivation)
				bs.yForceMap[id] += speed;
		}
	}
	@Override
	public void created(int id){ //追加処理2
		direction[id] = RIGHT; //運搬方向右向き
		beltSpeed[id] = 10; //速度10
		if(gameActivation) //エディターモードではない
			bs.xForceMap[id] += 10; //右向き運搬力10をこの地点に与える
	}
	@Override
	public void deleted(int id){ //削除追加処理
		if(gameActivation){ //ゲーム実行モード
			final double direction2 = direction[id];
			final int speed = beltSpeed[id];
			if(direction2 == RIGHT)
				bs.xForceMap[id] -= speed;
			else if(direction2 == UP)
				bs.yForceMap[id] += speed;
			else if(direction2 == LEFT)
				bs.yForceMap[id] += speed;
			else if(direction2 == DOWN)
				bs.xForceMap[id] -= speed;
		}
	}
	@Override
	public void moved(int srcID,int dstID){ //ID変更追加処理
		direction[dstID] = direction[srcID];
		beltSpeed[dstID] = beltSpeed[srcID];
	}
	@Override
	public int getDataLength(){ //内部データの数
		return 2; //向きと速度の２つ
	}
	@Override
	public String getDataName(int reference){//指定内部データ名を返す
		switch(reference){
		case 0: //向き
			return "向き";
		case 1: //速度
			return "速度";
		default:
			return "---";
		}
	}
	@Override
	public String getData(int id){ //このIDのギミックの固有データを文字列として出力
		return getData(id,0) + "_" + getData(id,1); //初期電源状態と向きと周波数を返す
	}
	@Override
	public String getData(int id,int reference){ //このIDのギミックの固有データを文字列として出力
		switch(reference){
		case 0: //向き
			final double direction2 = direction[id];
			if(direction2 == RIGHT)
				return "right";
			if(direction2 == UP)
				return "up";
			if(direction2 == LEFT)
				return "left";
			if(direction2 == DOWN)
				return "down";
			return "?";
		case 1: //速度
			return String.valueOf(beltSpeed[id]);
		default:
			return "";
		}
	}
	@Override
	public void setData(int srcID,int reference,String value) throws IllegalArgumentException{ //内部データの変更
		switch(reference){
		case 0: //向き
			if(value.equalsIgnoreCase("right") || value.equals("0")) //右
				direction[srcID] = RIGHT;
			else if(value.equalsIgnoreCase("up") || value.equals("1")) //上
				direction[srcID] = UP;
			else if(value.equalsIgnoreCase("left") || value.equals("2")) //左
				direction[srcID] = LEFT;
			else if(value.equalsIgnoreCase("down") || value.equals("3")) //下
				direction[srcID] = DOWN;
			break;
		case 1: //速度
			beltSpeed[srcID] = Integer.valueOf(value);
		}
	}
}