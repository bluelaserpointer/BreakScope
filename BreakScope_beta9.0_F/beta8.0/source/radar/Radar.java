
public abstract class Radar{
	BreakScope bs;
	
	int cost = 0; //使用コスト,0で初期装備

	public void construct(BreakScope bs){
		this.bs = bs;
	}
	public abstract void action(); //基本アクション(レーダーの描画)
}