import java.awt.*;
import java.io.*;
import java.util.*;

public class Config implements Serializable{
	private static long serialVersionUID = -59140414705285416L;
	final static int NONE = BreakScope.NONE;
	final static int MAX = BreakScope.MAX;
	final static int MIN = BreakScope.MIN;

	//敵の配列群
	String[] enemyName; //敵の名前、他configで名前指定されるときに使用
	int[] enemyHP, //体力
		enemyWeapon, //装備武器
		enemySpeed, //移動速度
		enemyTurnSpeed, //旋回速度
		enemySize, //衝突判定大きさ
		enemySelectSize, //選択判定大きさ（エディター用）
		enemyScore, //スコア
		enemyItem, //ドロップアイテム
		enemyHeat, //発熱量（サーモグラフィーに影響）
		enemyWeight; //重量（ノックバックに影響）
	BitSet enemyConflict;
	String[] enemyImg, //画像パス
		enemyIconImg, //エディター用アイコン画像パス
		enemyCode; //追加コードファイル名
	int enemyKindTotal;
	
	HashMap<String,Integer> nameToID_enemy;
	
	EnemyListener[] enemyClass;
	
	//エンティティの配列群
	String[] entityName; //エンティティの名前、他configで名前指定されるときに使用
	int[] entityHP, //体力
		entitySize, //衝突判定大きさ
		entitySelectSize, //選択判定大きさ（エディター用）
		entityHeat, //発熱量（サーモグラフィーに影響）
		entityWeight; //重量（ノックバックに影響）
	BitSet entityConflict;
	String[] entityImg, //画像パス
		entityIconImg, //エディター用アイコン画像パス
		entityCode; //追加コードファイル名
	int entityKindTotal;
	
	HashMap<String,Integer> nameToID_entity;
	
	EntityListener[] entityClass;
	
	//弾の配列群
	String[] bulletName; //弾の名前
	int[] bulletSize, //直径
		bulletWithEffect[], //弾の常時アニメーション
		bulletDestroyEffect[], //弾の衝突時のエフェクト
		bulletLostEffect[], //弾の消失時のエフェクト
		bulletLimitFrame, //存在時間
		bulletHitBurst, //衝突時の爆発威力
		//**以下追加弾関連,複雑な弾幕用**
		bulletWithGun[], //弾が常時撃つ追加弾
		bulletGunRate, //追加銃の発射間隔
		bulletDestroyGun[], //弾の命中/破壊時の追加弾,一般的な破壊時に起こる追加攻撃
		bulletLostGun[]; //弾の消失時の追加弾,弾幕パターン専用
		//*****************************
	double[] bulletStallRatio; //失速率
	double[] bulletRotateSpeed, //弾が公転するとき一フレームに回転する角度
		bulletRotateStartAngle; //公転の開始角度、指定なし=NONEだとランダム
	int[] bulletRotateRadius; //公転の半径、0で自転する(あたり判定は自転で変動しない)
	//以下3つはどれか1つのみの使用にしないと本来の性能が発揮できない
	BitSet bulletLaserAction, //レーザー軌道,一瞬で目標到達,消滅するまで前進処理を続けそれらを一度に描画
		bulletGunnerFollow, //射撃手追随,この弾を発射した自機や敵・弾の動きに合わせて座標が移動
		bulletHeatHoming, //ホーミング性能,>1で目標に向かって追随する
		bulletSuperPenetration; //超貫通性能,貫通時に移動速度が影響されない
	String[] bulletImg; //画像パス
	int bulletKindTotal;
	
	HashMap<String,Integer> nameToID_bullet;
	
	//設置物/ギミックの配列
	String[] gimmickName; //ギミックの名前
	int[] gimmickHP, //耐久値
		gimmickHeat, //発熱量（サーモグラフィーに影響）
		gimmickDestroyEffect, //破壊エフェクト
		gimmickDamage, //接触ダメージ
		gimmickExplosive, //爆発力
		gimmickBrightness; //発光量
	String[] gimmickImg, //画像パス
		gimmickIconImg, //エディター用アイコン画像パス
		gimmickCode; //追加コードファイル名
	int gimmickKindTotal;
	
	HashMap<String,Integer> nameToID_gimmick;
	
	GimmickListener[] gimmickClass;
	
	//エフェクトの配列群
	String[] effectName; //エフェクトの名前
	int[] effectDisplacement, //生成地点の散らばり
		effectQuantity, //生成個数
		effectSizeGrow, //サイズ変化
		effectSize_min, //初期サイズ最小値
		effectSize_max, //初期サイズ最大値
		effectSpeed_min, //初期速度最少
		effectSpeed_max, //初期速度最大
		effectVibration[]; //振動量,配列長さ0でなし、1で円形振動幅,2で矩形振動xy幅
	double[] effectAlphaInitial, //透過度初期値
		effectAlphaChanges[]; //透過度変動値（フェード速度）
	int[] effectAlphaChangeFrame[]; //上記変動値切り替えフレーム（フェード速度切り替えタイミング）
	double[] effectAccel; //加速度
	int[] effectAmount; //設置個数
	int[] effectTimePhase[], //各画像の表示フレーム数
		effectLimitFrame; //エフェクト自体の表示フレーム数
	String[][] effectImg; //使用画像のパス
	int effectKindTotal; //最大のID番号,総数も示している
	
	HashMap<String,Integer> nameToID_effect;
	
	//武器情報
	String[] weaponName; //武器の名前
	int[] weaponBulletKind[], //発射弾種類
		weaponStrength, //威力
		weaponCost, //武器コスト
		weaponTiming, //発射タイミング
		weaponBurst, //発射量
		weaponFireRate; //連射速度
	double[] weaponAberration, //ブレ
		weaponDirection, //強制発射角
		weaponDirectionCorrect; //発射角修正
	int[] weaponFirePoint; //発射地点(射手中央、照準地点、一番近い敵...)
	int[][] weaponFirePointsX,weaponFirePointsY; //発射地点x,y調整
	int[] weaponBulletSpeed, //平均弾速
		weaponBulletSpeedDispersion, //弾速変化範囲
		weaponBulletReflectiveness, //弾跳弾回数
		weaponBulletOffSet, //弾相殺力
		weaponBulletPenetration; //弾貫通力
	double[] weaponBulletReflectDamageRatio; //跳弾ダメージ率
	int[] weaponBulletKnockBack, //弾ノックバック力
		weaponGunnerForce; //射手にかかる力(+前進,-後退)
	double[] weaponBulletAccel, //弾加速度
		weaponBulletAccelDispersion, //弾加速度変動範囲
		weaponBulletAccelDirection, //弾加速度方向
		weaponBulletAccelDirectionCorrect; //弾加速度方向修正
	double[] weaponInertiaRate; //慣性影響率（発射中の移動が弾速へ影響する倍率）
	int[] weaponLimitRange, //射程
		weaponAmmoKind, //使用弾薬種
		weaponMagazineSize, //弾倉
		weaponReloadTime; //再装填時間
	BitSet weaponAutoReload; //自動リロードをするか
	int[] weaponGunLoop, //銃重複回数
		weaponGunLoopDelay, //重複間隔
		weaponGunChain, //連鎖武器
		weaponGunChainDelay; //連鎖間隔
	String[] weaponSE; //発射効果音パス
	BitSet weaponSEIsSerial; //複数回音連結SEであるか
	int weaponUPG1Limit[], //威力開発可能回数
		weaponUPG2Limit[], //精度開発可能回数
		weaponUPG3Limit[], //連射速度開発可能回数
		weaponUPG4Limit[]; //初速開発可能回数
	int[] weaponActionType; //アクションポーズ
	BitSet weaponBulletPreventMultiPenetration; //貫通弾による多段ヒットを防ぐか
	double[] weaponStrExtend, //威力継承率
		weaponFiRExtend, //連射間隔継承率
		weaponLiRExtend, //制限距離継承率
		weaponBulletSpdExtend, //弾速継承率
		weaponBulletPenExtend, //弾貫通回数継承率
		weaponBulletRefExtend, //弾跳弾回数継承率
		weaponBulletOfSExtend, //弾相殺性能継承率
		weaponBulletLiFExtend; //弾残留時間継承率
	String[] weaponImg,weaponIconImg; //画像パス,アイコン画像パス
	int weaponKindTotal;
	
	HashMap<String,Integer> nameToID_weapon;
	
	//ID変換系
	int convertID_enemy(String name){
		if(!BreakScope.isActualString(name))
			return BreakScope.NONE;
		if(nameToID_enemy.containsKey(name)) //指定名をIDに変換
			return nameToID_enemy.get(name);
		//指定名が見つからない
		System.out.println("enemy[" + name + "]が見つかりませんでした。");
		return BreakScope.NONE;
	}
	int convertID_entity(String name){
		if(!BreakScope.isActualString(name))
			return BreakScope.NONE;
		if(nameToID_entity.containsKey(name)) //指定名をIDに変換
			return nameToID_entity.get(name);
		//指定名が見つからない
		System.out.println("entity[" + name + "]が見つかりませんでした。");
		return BreakScope.NONE;
	}
	int convertID_effect(String name){
		if(!BreakScope.isActualString(name))
			return BreakScope.NONE;
		if(nameToID_effect.containsKey(name)) //指定名をIDに変換
			return nameToID_effect.get(name);
		//指定名が見つからない
		System.out.println("effect[" + name + "]が見つかりませんでした。");
		return BreakScope.NONE;
	}
	int convertID_gimmick(String name){
		if(!BreakScope.isActualString(name))
			return BreakScope.NONE;
		if(nameToID_gimmick.containsKey(name)) //指定名をIDに変換
			return nameToID_gimmick.get(name);
		//指定名が見つからない
		System.out.println("gimmick[" + name + "]が見つかりませんでした。");
		return BreakScope.NONE;
	}
	int convertID_weapon(String name){
		if(!BreakScope.isActualString(name))
			return BreakScope.NONE;
		if(nameToID_weapon.containsKey(name)) //指定名をIDに変換
			return nameToID_weapon.get(name);
		//指定名が見つからない
		System.out.println("weapon[" + name + "]が見つかりませんでした。");
		return BreakScope.NONE;
	}
	int convertID_bullet(String name){
		if(!BreakScope.isActualString(name))
			return BreakScope.NONE;
		if(nameToID_bullet.containsKey(name)) //指定名をIDに変換
			return nameToID_bullet.get(name);
		//指定名が見つからない
		System.out.println("bullet[" + name + "]が見つかりませんでした。");
		return BreakScope.NONE;
	}
	
	//配列拡張系
	void expandsData_enemy(int length){}
	void expandsData_entity(int length){}
	void expandsData_effect(int length){}
	void expandsData_gimmick(int length){}
	void expandsData_weapon(int length){
		if(length <= 0)
			return;
		//すべての配列の長さを増やす
		autoSizer_setOperation(weaponKindTotal + length);
		weaponName = autoSizer_execute(weaponName);
		weaponBulletKind = autoSizer_execute(weaponBulletKind);
		weaponStrength = autoSizer_execute(weaponStrength);
		weaponCost = autoSizer_execute(weaponCost);
		weaponTiming = autoSizer_execute(weaponTiming);
		weaponBurst = autoSizer_execute(weaponBurst);
		weaponFireRate = autoSizer_execute(weaponFireRate);
		weaponAberration = autoSizer_execute(weaponAberration);
		weaponDirection = autoSizer_execute(weaponDirection);
		weaponDirectionCorrect = autoSizer_execute(weaponDirectionCorrect);
		weaponFirePoint = autoSizer_execute(weaponFirePoint);
		weaponFirePointsX = autoSizer_execute(weaponFirePointsX);
		weaponFirePointsY = autoSizer_execute(weaponFirePointsY);
		weaponBulletSpeed = autoSizer_execute(weaponBulletSpeed);
		weaponBulletSpeedDispersion = autoSizer_execute(weaponBulletSpeedDispersion);
		weaponBulletReflectiveness = autoSizer_execute(weaponBulletReflectiveness);
		weaponBulletReflectDamageRatio = autoSizer_execute(weaponBulletReflectDamageRatio);
		weaponBulletPenetration = autoSizer_execute(weaponBulletPenetration);
		weaponBulletKnockBack = autoSizer_execute(weaponBulletKnockBack);
		weaponGunnerForce = autoSizer_execute(weaponGunnerForce);
		weaponBulletAccel = autoSizer_execute(weaponBulletAccel);
		weaponBulletAccelDispersion = autoSizer_execute(weaponBulletAccelDispersion);
		weaponBulletAccelDirection = autoSizer_execute(weaponBulletAccelDirection);
		weaponBulletAccelDirectionCorrect = autoSizer_execute(weaponBulletAccelDirectionCorrect);
		weaponLimitRange = autoSizer_execute(weaponLimitRange);
		weaponAmmoKind = autoSizer_execute(weaponAmmoKind);
		weaponMagazineSize = autoSizer_execute(weaponMagazineSize);
		weaponReloadTime = autoSizer_execute(weaponReloadTime);
		weaponGunLoop = autoSizer_execute(weaponGunLoop);
		weaponGunLoopDelay = autoSizer_execute(weaponGunLoopDelay);
		weaponGunChain = autoSizer_execute(weaponGunChain);
		weaponGunChainDelay = autoSizer_execute(weaponGunChainDelay);
		weaponInertiaRate = autoSizer_execute(weaponInertiaRate);
		weaponBulletOffSet = autoSizer_execute(weaponBulletOffSet);
		weaponSE = autoSizer_execute(weaponSE);
		weaponSEIsSerial = autoSizer_execute(weaponSEIsSerial);
		weaponUPG1Limit = autoSizer_execute(weaponUPG1Limit);
		weaponUPG2Limit = autoSizer_execute(weaponUPG2Limit);
		weaponUPG3Limit = autoSizer_execute(weaponUPG3Limit);
		weaponUPG4Limit = autoSizer_execute(weaponUPG4Limit);
		weaponActionType = autoSizer_execute(weaponActionType);
		weaponImg = autoSizer_execute(weaponImg);
		weaponIconImg = autoSizer_execute(weaponIconImg);
		weaponStrExtend = autoSizer_execute(weaponStrExtend);
		weaponFiRExtend = autoSizer_execute(weaponFiRExtend);
		weaponLiRExtend = autoSizer_execute(weaponLiRExtend);
		weaponBulletSpdExtend = autoSizer_execute(weaponBulletSpdExtend);
		weaponBulletPenExtend = autoSizer_execute(weaponBulletPenExtend);
		weaponBulletRefExtend = autoSizer_execute(weaponBulletRefExtend);
		weaponBulletOfSExtend = autoSizer_execute(weaponBulletOfSExtend);
		weaponBulletLiFExtend = autoSizer_execute(weaponBulletLiFExtend);
		
		//特定初期値を設定する
		autoFiller_setOperation(weaponKindTotal,weaponKindTotal + length);
		autoFiller_execute(weaponBurst,1);
		autoFiller_execute(weaponFireRate,1);
		autoFiller_execute(weaponDirection,NONE);
		autoFiller_execute(weaponBulletAccelDirection,NONE);
		autoFiller_execute(weaponBulletPenetration,1);
		autoFiller_execute(weaponLimitRange,MAX);
		autoFiller_execute(weaponFirePoint,BreakScope.SELF);
		autoFiller_execute(weaponAmmoKind,9);
		autoFiller_execute(weaponMagazineSize,MAX);
		autoFiller_execute(weaponReloadTime,1);
		autoFiller_execute(weaponGunChain,NONE);
		autoFiller_execute(weaponActionType,2);
		autoFiller_execute(weaponImg,"NONE");
		autoFiller_execute(weaponIconImg,"NONE");
		autoFiller_execute(weaponSE,"NONE");

		//2次元配列のnull回収
		cleanNulls_2DArray(weaponBulletKind);
		cleanNulls_2DArray(weaponFirePointsX);
		cleanNulls_2DArray(weaponFirePointsY);
		
		//総数記録を増やす
		weaponKindTotal += length;
	}
	void expandsData_bullet(int length){
		if(length <= 0)
			return;
		//すべての配列の長さを増やす
		autoSizer_setOperation(bulletKindTotal + length);
		bulletName = autoSizer_execute(bulletName);
		bulletSize = autoSizer_execute(bulletSize);
		bulletWithEffect = autoSizer_execute(bulletWithEffect);
		bulletDestroyEffect = autoSizer_execute(bulletDestroyEffect);
		bulletLostEffect = autoSizer_execute(bulletLostEffect);
		bulletLimitFrame = autoSizer_execute(bulletLimitFrame);
		bulletHitBurst = autoSizer_execute(bulletHitBurst);
		bulletWithGun = autoSizer_execute(bulletWithGun);
		bulletDestroyGun = autoSizer_execute(bulletDestroyGun);
		bulletLostGun = autoSizer_execute(bulletLostGun);
		bulletStallRatio = autoSizer_execute(bulletStallRatio);
		bulletRotateSpeed = autoSizer_execute(bulletRotateSpeed);
		bulletRotateStartAngle = autoSizer_execute(bulletRotateStartAngle);
		bulletRotateRadius = autoSizer_execute(bulletRotateRadius);
		bulletLaserAction = autoSizer_execute(bulletLaserAction);
		bulletGunnerFollow = autoSizer_execute(bulletGunnerFollow);
		bulletHeatHoming = autoSizer_execute(bulletHeatHoming);
		bulletSuperPenetration = autoSizer_execute(bulletSuperPenetration);
		bulletImg = autoSizer_execute(bulletImg);

		//初期値を設定する
		autoFiller_setOperation(bulletKindTotal,bulletKindTotal + length);
		autoFiller_execute(bulletSize,10);
		autoFiller_execute(bulletStallRatio,1.0);
		autoFiller_execute(bulletLimitFrame,MAX);
		autoFiller_execute(bulletRotateStartAngle,NONE);

		//2次元配列のnull回収
		cleanNulls_2DArray(bulletWithGun);
		cleanNulls_2DArray(bulletDestroyGun);
		cleanNulls_2DArray(bulletLostGun);
		cleanNulls_2DArray(bulletWithEffect);
		cleanNulls_2DArray(bulletDestroyEffect);
		cleanNulls_2DArray(bulletLostEffect);
		
		//総数記録を増やす
		bulletKindTotal += length;
	}
	
	//オートサイザー
	private int sizer_newLength;
	final void autoSizer_setOperation(int newLength){
		this.sizer_newLength = newLength;
	}
	final int[] autoSizer_execute(int[] targetArray){
		return Arrays.copyOf(targetArray,sizer_newLength);
	}
	final int[][] autoSizer_execute(int[][] targetArray){
		return Arrays.copyOf(targetArray,sizer_newLength);
	}
	final double[] autoSizer_execute(double[] targetArray){
		return Arrays.copyOf(targetArray,sizer_newLength);
	}
	final double[][] autoSizer_execute(double[][] targetArray){
		return Arrays.copyOf(targetArray,sizer_newLength);
	}
	final String[] autoSizer_execute(String[] targetArray){
		return Arrays.copyOf(targetArray,sizer_newLength);
	}
	final String[][] autoSizer_execute(String[][] targetArray){
		return Arrays.copyOf(targetArray,sizer_newLength);
	}
	final BitSet autoSizer_execute(BitSet targetArray){
		return targetArray.get(0,sizer_newLength);
	}
	
	//オートフィーラー
	//※多次元配列には対応していない
	private int filler_oldLength,filler_newLength;
	final void autoFiller_setOperation(int oldLength,int newLength){
		this.filler_oldLength = oldLength;
		this.filler_newLength = newLength;
	}
	final void autoFiller_execute(int[] targetArray,int defaultValue){
		Arrays.fill(targetArray,filler_oldLength,filler_newLength,defaultValue);
	}
	final void autoFiller_execute(double[] targetArray,double defaultValue){
		Arrays.fill(targetArray,filler_oldLength,filler_newLength,defaultValue);
	}
	final void autoFiller_execute(String[] targetArray,String defaultValue){
		Arrays.fill(targetArray,filler_oldLength,filler_newLength,defaultValue);
	}
	final void autoFiller_execute(BitSet targetArray,boolean defaultValue){
		targetArray.set(filler_oldLength,filler_newLength,defaultValue);
	}
	
	//NULLクリーナー
	final void cleanNulls_2DArray(int[][] array){
		for(int i = 0;i < array.length;i++){
			if(array[i] == null)
				array[i] = new int[0];
		}
	}
	final void cleanNulls_2DArray(double[][] array){
		for(int i = 0;i < array.length;i++){
			if(array[i] == null)
				array[i] = new double[0];
		}
	}
	final void cleanNulls_2DArray(String[][] array){
		for(int i = 0;i < array.length;i++){
			if(array[i] == null)
				array[i] = new String[0];
		}
	}
	
	//オートタッガー
	private HashMap<String,Integer> tagger_targetMap;
	private int tagger_nowID;
	final void autoTagger_startOperation(HashMap<String,Integer> targetMap){
		this.tagger_targetMap = targetMap;
		this.tagger_nowID = targetMap.size();
	}
	final void autoTagger_execute(String name){
		tagger_targetMap.put(name,tagger_nowID++);
	}
}