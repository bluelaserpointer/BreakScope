
public class SpeedSuspectRadar extends Radar{ //高速移動体察知レーダー
	//自機の80%の速度で移動する敵が自機を発見すると、警告を出すレーダー
	//追跡型の敵の襲撃を事前に察知することが可能
	//認識は1体まで、複数体に反応しても表示は変化しない

	BreakScope bs;
	
	int cost = 0; //使用コスト,0で初期装備

	public void construct(BreakScope bs){
		this.bs = bs;
	}
	public abstract void action(); //基本アクション(レーダーの描画)
}