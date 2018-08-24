import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import static java.lang.Math.*;
import java.util.*;

public class Sign extends GimmickListener{
	
	//追加内部データ
	final BitSet read = new BitSet(2500); //既読
	final String[] text = new String[2500];
	
	@Override
	public void created(int id,String dataString){ //追加処理(読み込み)
		read.clear(id);
		text[id] = dataString; //看板の文字
	}
	@Override
	public void created(int id){ //追加処理(生成)
		this.created(id,""); //看板の文字
	}
	@Override
	public void gamePaint(int grid,boolean inScreen){
		if(inScreen) //スクリーン外のとき描画をしない
			super.defaultPaint(grid,true);
		final int x = grid/bs.stageGridH*100 + 50,y = grid%bs.stageGridH*100 + 50;
		if(abs(bs.playerX - x) < 50 && abs(bs.playerY - y) < 50){
			bs.playerComment = text[grid];
			bs.playerCommentFrame = bs.nowFrame;
		}
		/*if(abs(bs.playerX - x) < 50 && abs(bs.playerY - y) < 50 && !read.get(grid)){ //範囲内&未読
			if(bs.playerActionTarget == bs.NONE){
				bs.playerActionTarget = bs.GIMMICK + grid;
				bs.playerActionProgress = 0;
			}else
				bs.playerActionProgress++;
			if(bs.playerActionProgress >= 100) //読み終わった
				read.set(grid);
		}else if(bs.playerActionTarget / 10000 == bs.GIMMICK && bs.playerActionTarget % 10000 == grid)
			bs.playerActionTarget = bs.NONE;*/
	}
	public String getData(int id){ //指定IDの内部データの出力 ステージデータの保存に使用
		return text[id];
	}
	public String getData(int id,int reference){ //指定IDの指定内部データを返す 内部データの表示に使用
		if(reference == 0)
			return text[id];
		else
			return "";
	}
	public int getDataLength(){ //内部データの数
		return 1;
	}
	public String getDataName(int reference){ //指定内部データ名を返す
		if(reference == 0)
			return "内容";
		else
			return "---";
	}
	public void setData(int id,int reference,String value) throws IllegalArgumentException{ //内部データの変更
		if(reference == 0)
			text[id] = value;
		else
			throw new IllegalArgumentException("内部データのないギミック追加コードに変更操作が呼び出されました\n(指定ID" + id + ",参照項目" + reference + ",適用値" + value + ")");
	}
	public void moved(int srcID,int dstID){ //移動追加処理
		text[dstID] = text[srcID];
	}
}