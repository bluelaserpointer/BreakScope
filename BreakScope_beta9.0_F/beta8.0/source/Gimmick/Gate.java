import static java.lang.Math.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Gate extends GimmickListener{

	int[] gateMoving, //ゲート開閉進行度0~50
		direction; //ゲート開閉方向
	String[] frequency; //影響周波数
	
	final int CLOSE = 0,OPEN = 50;
	final int HORIZONTAL = 0,VERTICAL = 1; //横、縦方向
	
	@Override
	public void loadStarted(){ //変数初期化
		gateMoving = new int[bs.stageGridTotal];
		direction = new int[bs.stageGridTotal];
		frequency = new String[bs.stageGridTotal];
	}
	@Override
	public void gamePaint(int grid,boolean inScreen){
		//基本処理
		final int x = grid/bs.stageGridH*100 + 50,y = grid%bs.stageGridH*100 + 50; //座標情報に変換
		int move = gateMoving[grid]; //進行状況
		if(Lever.gimmickSwitch.get(grid)){ //この座標の入力を検出-on
			bs.gimmickHP[grid] = NONE;
			if(move <= OPEN)
				move += 2;
		}else{
			bs.gimmickHP[grid] = MAX;
			if(move >= CLOSE)
				move -= 2;
		}
		gateMoving[grid] = move;
		//描画
		if(!inScreen) //スクリーン外のとき処理放棄
			return;
		final Shape clip = bs.g2.getClip(); //もともとのトリミング範囲を保存
		bs.g2.setClip(x - 50,y - 50,100,100);
		final Image gateImg = bs.gimmickImg[bs.gimmickKind[grid]];
		if(direction[grid] == VERTICAL){ //縦
			bs.g2.rotate(PI/2,x,y);
			bs.drawImageBS(gateImg,x - 100 - move,y - 50,100,100);
			bs.drawImageBS(gateImg,x + move,y - 50,100,100);
			bs.g2.rotate(-PI/2,x,y);
		}else{
			bs.drawImageBS(gateImg,x - 100 - move,y - 50,100,100);
			bs.drawImageBS(gateImg,x + move,y - 50,100,100);
		}
		bs.g2.setClip(clip); //トリミングを戻す
	}
	@Override
	public void editorPaint(int grid,boolean inScreen){
		if(!inScreen) //スクリーン外のとき処理放棄
			return;
		final int x = grid/bs.stageGridH*100 + 50,y = grid%bs.stageGridH*100 + 50; //座標情報に変換
		final Shape clip = bs.g2.getClip(); //もともとのトリミング範囲を保存
		bs.g2.setClip(x - 50,y - 50,100,100);
		final Image gateImg = bs.gimmickImg[bs.gimmickKind[grid]];
		if(Lever.gimmickSwitch.get(grid)){ //開いている	
			if(direction[grid] == VERTICAL){ //縦
				bs.g2.rotate(PI/2,x,y);
				bs.drawImageBS(gateImg,x - 130,y - 50,100,100);
				bs.drawImageBS(gateImg,x + 30,y - 50,100,100);
				bs.g2.rotate(-PI/2,x,y);
			}else{
				bs.drawImageBS(gateImg,x - 130,y - 50,100,100);
				bs.drawImageBS(gateImg,x + 30,y - 50,100,100);
			}
		}else{ //閉じている
			if(direction[grid] == VERTICAL){ //縦
				bs.g2.rotate(PI/2,x,y);
				bs.drawImageBS(gateImg,x - 100,y - 50,100,100);
				bs.drawImageBS(gateImg,x,y - 50,100,100);
				bs.g2.rotate(-PI/2,x,y);
			}else{
				bs.drawImageBS(gateImg,x - 100,y - 50,100,100);
				bs.drawImageBS(gateImg,x,y - 50,100,100);
			}
		}
		bs.g2.setClip(clip); //トリミングを戻す
	}
	public void created(int id,String dataString){ //追加処理
		final String str[] = dataString.split("_"); //固有情報分解
		final boolean gateOpened = str[0] == "T" ? true : false;
		Lever.gimmickSwitch.set(id,gateOpened); //初期電源状態を代入
		if(gateOpened)
			gateMoving[id] = OPEN; //ゲートを閉じる
		else
			gateMoving[id] = CLOSE; //ゲートを閉じる
		if(str[1].equals("1"))
			direction[id] = VERTICAL; //開閉向き 0:縦,1:横
		else 
			direction[id] = HORIZONTAL; //0か、違う文字が入れば横
		final String _frequecy = str[2]; //周波数
		Lever.addFrequency(id,_frequecy); //影響周波数を登録
		frequency[id] = _frequecy; //影響周波数
	}
	public void created(int id){ //追加処理2
		Lever.gimmickSwitch.clear(id); //初期電源状態:既定OFF
		gateMoving[id] = CLOSE; //ゲート開閉：既定閉じる
		direction[id] = HORIZONTAL; //向き：既定横
		frequency[id] = ""; //影響周波数:既定なし
	}
	public void deleted(int id){ //削除
		Lever.gimmickFrequencyList.remove(frequency[id]);
	}
	public void moved(int srcID,int dstID){ //移動追加処理
		gateMoving[dstID] = gateMoving[srcID];
		direction[dstID] = direction[srcID];
		final String _frequency = frequency[srcID];
		if(!_frequency.equals("")){
			Lever.gimmickFrequencyList.get(_frequency).remove(srcID);
			Lever.gimmickFrequencyList.get(_frequency).add(dstID);
			frequency[dstID] = _frequency;
		}
	}
	public int getDataLength(){ //内部データの数
		return 3;
	}
	public String getDataName(int reference){//指定内部データ名を返す
		switch(reference){
		case 0: //初期電源状態
			return "電源on/off";
		case 1:
			return "向き";
		case 2: //周波数
			return "反応周波数";
		default:
			return "---";
		}
	}
	public String getData(int id){ //このIDのギミックの固有データを文字列として出力
		return getData(id,0) + "_" + getData(id,1) + "_" + getData(id,2); //入力状態と、向きと周波数を返す
	}
	public String getData(int id,int reference){ //このIDのギミックの固有データを文字列として出力
		switch(reference){
		case 0: //初期電源状態
			return Lever.gimmickSwitch.get(id) ? "on" : "off";
		case 1: //向き
			return String.valueOf(direction[id]);
		case 2: //周波数
			return frequency[id];
		default:
			return "";
		}
	}
	public void setData(int id,int reference,String value) throws IllegalArgumentException{ //内部データの変更
		switch(reference){
		case 0: //初期電源状態
			if(value.equalsIgnoreCase("on") || value.equals("1")
				|| value.equalsIgnoreCase("true") || value.equalsIgnoreCase("open"))
				Lever.gimmickSwitch.set(id);
			else if(value.equalsIgnoreCase("off") || value.equals("0")
				|| value.equalsIgnoreCase("false")|| value.equalsIgnoreCase("close"))
				Lever.gimmickSwitch.clear(id);
			else
				throw new IllegalArgumentException("on/off,0/1,true/falseと入力してください");
			break;
		case 1: //向き（縦・横）
			if(value.equals("0")) //横
				direction[id] = HORIZONTAL;
			else if(value.equals("1")) //縦
				direction[id] = VERTICAL;
			else
				throw new IllegalArgumentException("0/1と入力してください");
			break;
		case 2: //周波数
			frequency[id] = value;
			break;
		default:
			throw new IllegalArgumentException("その設定はありません");
		}
	}
}