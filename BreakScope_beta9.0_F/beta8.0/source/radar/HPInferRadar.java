
public class HPInferRadar extends Radar{ //熱源探知レーダー
	//もっとも近い熱源の方角を指す、距離はわからない
	//どんなに遠くてもわずかな熱量を正確にとらえることが可能
	//認識は1体まで

	BreakScope bs;
	
	int cost = 0; //使用コスト,0で初期装備

	public void construct(BreakScope bs){
		this.bs = bs;
	}
	public abstract void action(); //基本アクション(レーダーの描画)
}