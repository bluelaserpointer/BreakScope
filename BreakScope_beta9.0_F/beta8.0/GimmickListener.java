import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.lang.Integer;
import java.io.Serializable;

public class GimmickListener implements Serializable{
	private static long serialVersionUID = -52141303342785416L;

	final int MAX = BreakScope.MAX,MIN = BreakScope.MIN;
	final int NONE = BreakScope.NONE;

	transient protected BreakScope bs; //ゲーム情報総合インスタンス
	
	public void construct(BreakScope bs){ //起動処理(追加画像の読み込み、インスタンスの取得など)
		this.bs = bs;
	}
	public void damaged(int id,int dmg){} //傷害追加処理
	/**
	* 毎フレーム実行される平常処理です。オブジェクト数に関係なく１回実行されます。
	* かなり特殊なバックグラウンド処理があるとき、このメソッドの使用が想定されます。
	* オブジェクトごとに実行される描画や行動などはgamePaintの方に書くことを推薦します。
	*/
	public void ordinaryProcess(boolean gameActivating){} //平常処理(オブジェクト数に関係なく毎フレーム実行する処理)
	public void gamePaint(int id,boolean inScreen){ //ゲーム用描画追加処理
		if(inScreen)
			defaultPaint(id,true);
	}
	public void editorPaint(int id,boolean inScreen){ //エディター用描画追加処理(デフォルトで上に同じ)
		if(inScreen)
			defaultPaint(id,true);
	}
	final public void defaultPaint(int id,boolean inScreen){ //デフォルト描画メソッド(描画処理を変更しないときはこれを呼び出す)
		if(!inScreen) //スクリーンに入らないとき、処理放棄
			return;
		final int kind = bs.gimmickKind[id]; //種類値を取得
		final int x = id/bs.stageGridH*100,y = id%bs.stageGridH*100; //IDから座標を取得(左上点)
		final Image img = bs.gimmickImg[kind]; //画像を取得
		if(img != null)
			bs.drawImageBS(img,x,y,100,100); //描画
		else
			System.out.println(bs.cfg.gimmickName[kind] + ":画像が指定されていないギミックにデフォルト描画処理が呼び出されました。描画処理が追加コードで記述されている場合、このメソッド呼び出しを削除してください。");
		final int hp;
		if(bs.gimmickHP != null)
			hp = bs.gimmickHP[id];
		else
			hp = bs.cfg.gimmickHP[kind];
		if(hp != MAX && hp != NONE && hp > 0){
			bs.g2.setColor(Color.BLACK);
			bs.g2.drawString(String.valueOf(hp),x + 50,y + 50);
		}
	}
	public void created(int id,String dataString){} //生成追加処理(内部データ指定値)
	public void created(int id){} //生成追加処理(内部データデフォルト値)
	public void loadStarted(){} //ステージロード開始追加処理
	public void loadFinished(){} //ステージロード終了追加処理
	public void gameEnded(){} //ステージ終了追加処理
	public void killed(int id){} //死亡追加処理(特殊なドロップアイテム処理や演出などを行う)(自動的に下のメソッドも呼ばれる)
	public void deleted(int id){} //削除追加処理(内部データの整理などを行う)
	public void cleared(){} //全削除追加処理(内部データの整理などを行う)
	public void moved(int srcID,int dstID){} //移動追加処理(内部データの整理などを行う)

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
		throw new IllegalArgumentException("内部データのないギミック追加コードに変更操作が呼び出されました\n(指定ID" + id + ",参照項目" + reference + ",適用値" + value + ")");
	}
}