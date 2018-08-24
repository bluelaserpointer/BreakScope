import static java.lang.Math.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.*;

public class Lever extends GimmickListener{

	transient Image key_space;
	final String[] frequency = new String[2500]; //影響周波数
	
	static final HashMap<String,ArrayList<Integer>> gimmickFrequencyList = new HashMap<String,ArrayList<Integer>>(); //周波数と使用ギミック座標ID
	static final BitSet gimmickSwitch = new BitSet(2500); //入力状態
	
	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		key_space = bs.loadImage("key_space.png");
	}
	@Override
	public void gamePaint(int map){ //基本処理
		final int x = map/50*100 + 50,y = map%50*100 + 50; //座標情報変換
		if(abs(bs.playerX - x) < 50 && abs(bs.playerY - y) < 50){ //自機が接触範囲内
			bs.g2.drawImage(key_space,x - 50,y - 50,bs); //スペースキーで操作することを伝える
			if(bs.actioned && !frequency[map].equals("")){ //操作された
				final ArrayList<Integer> groups = gimmickFrequencyList.get(frequency[map]); //同じ周波数のギミックのIDを取得
				for(int k = 0;k < groups.size();k++){
					final int id = groups.get(k);
					Lever.gimmickSwitch.set(id,!Lever.gimmickSwitch.get(id)); //入力状態を反転
				}
				bs.actioned = false;
			}
		}
		//描画
		if(gimmickSwitch.get(map)){ //ON
			bs.g2.setColor(Color.GREEN);
			bs.g2.fillRect(x - 35,y - 10,20,20);
		}else{ //OFF
			bs.g2.setColor(Color.RED);
			bs.g2.fillRect(x + 15,y - 10,20,20);
		}
		bs.g2.drawImage(bs.gimmickImg[bs.gimmickKind[map]],x - 50,y - 50,100,100,bs);
	}
	public void editorPaint(int map){ //エディター用描画処理
		final int x = map/50*100 + 50,y = map%50*100 + 50; //座標情報に変換
		if(gimmickSwitch.get(map)){ //ON
			bs.g2.setColor(Color.GREEN);
			bs.g2.fillRect(x - 35,y - 10,20,20);
		}else{ //OFF
			bs.g2.setColor(Color.RED);
			bs.g2.fillRect(x + 15,y - 10,20,20);
		}
		bs.g2.drawImage(bs.gimmickImg[bs.gimmickKind[map]],x - 50,y - 50,100,100,bs);
	}
	@Override
	public void created(int id,String dataString){ //追加処理
		final String[] str = dataString.split("_"); //固有情報分解
		gimmickSwitch.set(id,str[0] == "T" ? true : false); //初期電源状態を代入
		final String _frequency = str[1]; //周波数
		this.addFrequency(id,_frequency); //影響周波数を登録
		frequency[id] = _frequency; //影響周波数
	}
	public void created(int id){ //追加処理2
		gimmickSwitch.clear(id); //初期電源状態OFF
		frequency[id] = ""; //影響周波数なし
	}
	public static void addFrequency(int id,String _frequency){ //周波数追加処理(固有メソッド)
		if(!_frequency.equals("")){
			if(gimmickFrequencyList.containsKey(_frequency)){ //登録済みの周波数
				gimmickFrequencyList.get(_frequency).add(id); //IDを登録
			}else{ //未登録の周波数
				final ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(id);
				gimmickFrequencyList.put(_frequency,list); //新しい周波数グループを作成
			}
		}
	}
	public void killed(int id){ //削除追加処理-周波数情報の整理
		final String _frequency = frequency[id];
		if(!_frequency.equals("")){ //周波数を登録しているか
			if(gimmickFrequencyList.get(_frequency).size() == 1) //グループ最後の要素
				gimmickFrequencyList.remove(_frequency); //グループごと削除
			else
				gimmickFrequencyList.get(_frequency).remove(id); //この要素を削除
		}
		//※ゲーム実行時、スイッチが入った状態で削除処理が起きると、入力を受けていたギミックは状態を変えない（ドアが開いたままなど）
	}
	public void cleared(){ //全削除追加処理-周波数情報のクリア
		gimmickSwitch.clear();
		gimmickFrequencyList.clear();
	}
	public void moved(int srcID,int dstID){ //移動追加処理-周波数情報の操作
		gimmickSwitch.set(dstID,gimmickSwitch.get(srcID)); //スイッチ情報を写す
		if(!frequency[srcID].equals("")){ //周波数を登録しているか
			frequency[dstID] = frequency[srcID]; //周波数情報を写す
			gimmickFrequencyList.get(frequency[srcID]).remove(srcID); //登録IDを変更
			gimmickFrequencyList.get(frequency[srcID]).add(dstID);
		}
	}
	public int getDataLength(){ //内部データの数
		return 2; //初期電源状態(スイッチ)と設定周波数の２つ
	}
	public String getDataName(int reference){//指定内部データ名を返す
		switch(reference){
		case 0: //初期電源状態
			return "電源on/off";
		case 1: //周波数
			return "反応周波数";
		default:
			return "---";
		}
	}
	public String getData(int id){ //このIDのギミックの固有データを文字列として出力
		if(gimmickSwitch.get(id))
			return "T_" + frequency[id]; //初期電源状態と周波数を返す
		else
			return "F_" + frequency[id]; //初期電源状態と周波数を返す
	}
	public String getData(int id,int reference){ //このIDのギミックの固有データを文字列として出力
		switch(reference){
		case 0: //初期電源状態
			return gimmickSwitch.get(id) ? "on" : "off";
		case 1: //周波数
			return frequency[id];
		default:
			return "";
		}
	}
	public void setData(int srcID,int reference,String value) throws IllegalArgumentException{ //内部データの変更
		switch(reference){
		case 0: //初期電源状態
			if(value.equalsIgnoreCase("on") || value.equals("1"))
				gimmickSwitch.set(srcID);
			else if(value.equalsIgnoreCase("off") || value.equals("0"))
				gimmickSwitch.clear(srcID);
			else
				throw new IllegalArgumentException("on/off,0/1と入力してください");
			break;
		case 1: //周波数
			frequency[srcID] = value;
			break;
		default:
			throw new IllegalArgumentException("Leverの内部データに" + reference + "の項目はありません");
		}
	}
}