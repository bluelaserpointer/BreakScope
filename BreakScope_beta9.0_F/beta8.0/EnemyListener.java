import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.lang.Integer;
import java.io.Serializable;

public class EnemyListener implements Serializable{
	private static long serialVersionUID = -12341342785416L;

	final int MAX = BreakScope.MAX,MIN = BreakScope.MIN;
	final int NONE = BreakScope.NONE;

	transient protected BreakScope bs; //ゲーム情報総合インスタンス
	
	public void construct(BreakScope bs){ //起動処理（追加画像の読み込み）
		this.bs = bs;
	}
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //ゲーム用描画処理(オーバーライドして攻撃追加処理をここで行える)
		defaultMove(id);
		if(inScreen)
			defaultPaint(id,x,y,angle,true);
	}
	public void editorPaint(int id,int x,int y,double angle,boolean inScreen){ //エディター用描画処理
		if(inScreen)
			defaultPaint(id,x,y,angle,true);
	}
	final public void defaultPaint(int id,double x,double y,double angle,boolean inScreen){ //デフォルト描画メソッド(描画処理を変更しないときはこれを呼び出す)
		if(!inScreen) //スクリーンに入らないとき、処理放棄
			return;
		final int kind = bs.enemyKind[id]; //種類値を取得
		final int x_int = (int)x,y_int = (int)y; //座標をint値に変換
		final Image img = bs.enemyImg[kind]; //画像を取得
		if(img != null){
			bs.g2.rotate(angle,x_int,y_int); //キャンバスを回転
			bs.drawImageBS_centerDot(img,x_int,y_int); //描画
			bs.g2.rotate(-angle,x_int,y_int); //キャンバスを戻す
		}else
			System.out.println(bs.cfg.enemyName[kind] + ":画像が未指定の敵にデフォルト描画処理が呼び出されました。描画処理が追加コードで全部記述される場合、このメソッド呼び出しを削除してください。");
		final int hp = bs.enemyHP[id];
		if(hp != MAX && hp != NONE){
			bs.g2.setColor(Color.BLACK);
			bs.g2.drawString(String.valueOf(hp),x_int,y_int);
		}
	}
	final public void defaultMove(int id){ //デフォルト移動メソッド(移動処理を変更しないときはこれを呼び出す)
		if(bs.enemyWound[id] > 0) //速度低下
			bs.AI02(id,bs.cfg.enemySpeed[bs.enemyKind[id]]/3,bs.enemyFoundMe.get(id));
		else
			bs.AI02(id,bs.cfg.enemySpeed[bs.enemyKind[id]],bs.enemyFoundMe.get(id));
	}
	public void damaged(int id,int dmg){} //傷害追加処理
	public void created(int id,String dataString){} //生成追加処理(内部データ指定値)
	public void created(int id){} //生成追加処理(内部データデフォルト値)
	public void loadStarted(){} //ステージロード開始追加処理
	public void loadFinished(){} //ステージロード終了追加処理
	public void gameEnded(){} //ステージ終了追加処理
	public void killed(int id){} //死亡追加処理(特殊なドロップアイテム処理や演出などを行う)(自動的に下のメソッドも呼ばれる)
	public void deleted(int id){} //削除追加処理(内部データの整理などを行う)
	public void cleared(){} //全削除追加処理(内部データの整理などを行う)

	//<内部データ>
	public String getData(int id){ //指定IDの内部データの出力 ステージデータの保存に使用
		return "";
	}
	public String getData(int id,int reference){ //指定IDの指定内部データを返す 内部データの表示に使用
		return "";
	}
	public int getDataLength(){ //内部データの数
		return 0;
	}
	public String getDataName(int reference){ //指定内部データ名を返す
		return "";
	}
	public void setData(int id,int reference,String value) throws IllegalArgumentException{ //内部データの変更
		throw new IllegalArgumentException("内部データのない敵追加コードに変更操作が呼び出されました\n(指定ID" + id + ",参照項目" + reference + ",適用値" + value + ")");
	}
}