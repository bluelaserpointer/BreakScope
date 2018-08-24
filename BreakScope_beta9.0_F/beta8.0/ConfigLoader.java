import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.*;
import static java.lang.Math.*;

public class ConfigLoader extends JPanel{

	final static int NONE = BreakScope.NONE,
		MAX = BreakScope.MAX,MIN = BreakScope.MIN;
	final static int W_K_L = BreakScope.WEAPON_KIND_LIMIT; //武器制作上限数(現在、敵や弾など他のオブジェクト制作上限もこれに追随)
	//※注：下記の「W_K_L」はすべてこの定数であり、項目の値を保存する配列の長さにのみ使われる
	
	//敵の配列群
	String[] enemyName = new String[W_K_L]; //敵の名前、他configで名前指定されるときに使用
	int[] enemyHP = new int[W_K_L], //体力
		enemyWeapon = new int[W_K_L], //装備武器
		enemySpeed = new int[W_K_L], //移動速度
		enemyTurnSpeed = new int[W_K_L], //旋回速度
		enemySize = new int[W_K_L], //衝突判定大きさ
		enemySelectSize = new int[W_K_L], //選択判定大きさ（エディター用）
		enemyScore = new int[W_K_L], //スコア
		enemyItem = new int[W_K_L], //ドロップアイテム
		enemyHeat = new int[W_K_L], //発熱量（サーモグラフィーに影響）
		enemyWeight = new int[W_K_L]; //重量（ノックバックに影響）
	BitSet enemyConflict = new BitSet(W_K_L);
	String[] enemyImg = new String[W_K_L], //画像パス
		enemyIconImg = new String[W_K_L], //エディター用アイコン画像パス
		enemyCode = new String[W_K_L]; //追加コードファイル名
	int enemyKindTotal; //種類数

	final HashMap<String,Integer> nameToID_enemy = new HashMap<String,Integer>();

	//弾の配列群
	
	final String[] bulletName = new String[W_K_L]; //他cfgで名前指定されるときに使用
	final int[] bulletSize = new int[W_K_L], //直径
		bulletHitBurst = new int[W_K_L], //衝突時の爆発威力（半径）
		bulletWithGun[] = new int[W_K_L][], //弾が常時撃つ銃
		bulletGunRate = new int[W_K_L], //銃の発射間隔
		bulletLostGun[] = new int[W_K_L][], //弾消滅時に撃つ銃
		bulletDestroyGun[] = new int[W_K_L][], //弾命中時に撃つ銃
		bulletWithEffect[] = new int[W_K_L][], //弾のスキンエフェクト
		bulletDestroyEffect[] = new int[W_K_L][], //弾の破壊エフェクト
		bulletLostEffect[] = new int[W_K_L][]; //弾消滅時に起こるエフェクト
	final BitSet bulletLaserAction = new BitSet(W_K_L), //レーザー処理,通常飛行の処理をループさせることで一瞬で目標に到達する
		bulletGunnerFollow = new BitSet(W_K_L), //射撃手追随処理,発射元の動いた量を自身の座標に加算することで追随するようになる
		bulletHeatHoming = new BitSet(W_K_L), //ホーミング処理,高い熱を持つ目標に向かって角度を調整する
		bulletSuperPenetration = new BitSet(W_K_L); //超貫通性能,貫通時に移動速度が影響されない
	final double[] bulletRotateSpeed = new double[W_K_L], //弾が公転するとき一フレームに回転する角度
		bulletRotateStartAngle = new double[W_K_L]; //公転の開始角度、指定なし=NONEだとランダム
	final int[] bulletRotateRadius = new int[W_K_L]; //公転の半径、0で自転する(あたり判定は自転で変動しない)	
	final int[] bulletLimitFrame = new int[W_K_L]; //存在時間
	final double[] bulletStallRatio = new double[W_K_L]; //失速率
	final String bulletImg[] = new String[W_K_L];
	int bulletKindTotal; //種類数
	
	final HashMap<String,Integer> nameToID_bullet = new HashMap<String,Integer>();
	
	//設置物/ギミックの配列
	String[] gimmickName = new String[W_K_L]; //ギミックの名前
	int[] gimmickHP = new int[W_K_L], //耐久値
		gimmickHeat = new int[W_K_L], //発熱量（サーモグラフィーに影響）
		gimmickDestroyEffect = new int[W_K_L], //破壊エフェクト
		gimmickDamage = new int[W_K_L], //接触ダメージ
		gimmickExplosive = new int[W_K_L], //爆発力
		gimmickBrightness = new int[W_K_L]; //発光量
	String[] gimmickImg = new String[W_K_L], //画像パス
		gimmickIconImg = new String[W_K_L], //エディター用アイコン画像パス
		gimmickCode = new String[W_K_L]; //追加コードファイル名
	int gimmickKindTotal; //種類数
	
	HashMap<String,Integer> nameToID_gimmick = new HashMap<String,Integer>();
	
	//エフェクトの配列群
	final int EXPLOSION1 = 0,EXPLOSION2 = 1,SPARK_RED = 2,ACID_DUST = 3,SPARK = 4,DUST = 5,SWORD_SLASH = 6,BLACK_GAS = 7,BIG_EXPLOSION = 8,ROCK = 9;
	final String[] effectName = new String[W_K_L];
	final int[] effectDisplacement = new int[W_K_L], //生成地点の散らばり
		effectQuantity = new int[W_K_L], //生成個数
		effectSizeGrow = new int[W_K_L], //サイズ変化
		effectSize_min = new int[W_K_L], //初期サイズ最小値
		effectSize_max = new int[W_K_L], //初期サイズ最大値
		effectSpeed_min = new int[W_K_L], //初期速度最少
		effectSpeed_max = new int[W_K_L]; //初期速度最大
	final double[] effectAlphaInitial = new double[W_K_L], //透過度初期値
		effectAlphaChanges[] = new double[W_K_L][]; //透過度変動値（フェード速度）
	final int[] effectAlphaChangeFrame[] = new int[W_K_L][]; //上記変動値切り替えフレーム（フェード速度切り替えタイミング）
	final double[] effectAccel = new double[W_K_L]; //加速度
	final int[] effectAmount = new int[W_K_L]; //加速度
	final int effectTimePhase[][] = new int[W_K_L][], //各画像の表示フレーム数
		effectLimitFrame[] = new int[W_K_L]; //エフェクト自体の表示フレーム数
	final String effectImg[][] = new String[W_K_L][];
	int effectKindTotal; //種類数
	
	final HashMap<String,Integer> nameToID_effect = new HashMap<String,Integer>();
	
	//武器情報
	final int SWORD = 0,AUTOLOADING = 1,SHOTGUN = 2,RPG = 3,SNIPER_RIFLE = 4,CHAIN_SAW = 5;
	final String[] weaponName = new String[W_K_L];
	final int[] weaponBulletKind[] = new int[W_K_L][], //発射弾種類
		weaponStrength = new int[W_K_L], //威力
		weaponCost = new int[W_K_L], //武器コスト
		weaponTiming = new int[W_K_L], //発射タイミング
		weaponBurst = new int[W_K_L], //発射量
		weaponFireRate = new int[W_K_L]; //連射速度
	final double[] weaponAberration = new double[W_K_L], //ブレ
		weaponDirection = new double[W_K_L], //強制発射角
		weaponDirectionCorrect = new double[W_K_L], //発射角修正
		weaponBulletAccel = new double[W_K_L], //弾加速度
		weaponBulletAccelDispersion = new double[W_K_L], //弾加速度変動範囲
		weaponBulletAccelDirection = new double[W_K_L], //弾加速度方向
		weaponBulletAccelDirectionCorrect = new double[W_K_L]; //弾加速度方向修正
	final int[] weaponBulletKnockBack = new int[W_K_L]; //弾ノックバック力
	final int[] weaponGunnerForce = new int[W_K_L]; //射手にかかる力(+前進,-後退)
	final int[] weaponFirePoint = new int[W_K_L]; //発射地点(射手中央、照準地点、一番近い敵...)
	final int[][] weaponFirePointsX = new int[W_K_L][],weaponFirePointsY = new int[W_K_L][]; //発射地点x,y調整
	final int[] weaponBulletSpeed = new int[W_K_L], //弾速
		weaponBulletSpeedDispersion = new int[W_K_L], //弾速変動範囲
		weaponBulletReflectiveness = new int[W_K_L], //弾跳弾回数
		weaponBulletOffSet = new int[W_K_L], //弾相殺力
		weaponBulletPenetration = new int[W_K_L], //弾貫通力
		weaponLimitRange = new int[W_K_L], //射程
		weaponAmmoKind = new int[W_K_L], //使用弾薬種
		weaponMagazineSize = new int[W_K_L], //弾倉
		weaponReloadTime = new int[W_K_L], //再装填時間
		weaponAimStartupTime = new int[W_K_L], //武器始動時間
		weaponAttacksStartupTime = new int[W_K_L]; //攻撃始動時間
	final BitSet weaponAutoReload = new BitSet(W_K_L); //自動リロードをするか
	final int[] weaponGunLoop = new int[W_K_L], //銃重複回数
		weaponGunLoopDelay = new int[W_K_L], //重複間隔
		weaponGunChain = new int[W_K_L], //連鎖武器
		weaponGunChainDelay = new int[W_K_L]; //連鎖間隔
	final double[] weaponBulletReflectDamageRatio = new double[W_K_L]; //跳弾ダメージ率
	final double[] weaponInertiaRate = new double[W_K_L]; //慣性影響率（発射中の移動が弾速へ影響する倍率）
	final String[] weaponSE = new String[W_K_L]; //発射効果音パス
	final BitSet weaponSEIsSerial = new BitSet(W_K_L); //複数回音連結SEであるか
	final int weaponUPG1Limit[] = new int[W_K_L], //威力開発可能回数
		weaponUPG2Limit[] = new int[W_K_L], //精度開発可能回数
		weaponUPG3Limit[] = new int[W_K_L], //連射速度開発可能回数
		weaponUPG4Limit[] = new int[W_K_L]; //初速開発可能回数
	final int[] weaponActionType = new int[W_K_L]; //アクションポーズ
	BitSet weaponBulletPreventMultiPenetration = new BitSet(W_K_L); //貫通弾による多段ヒットを防ぐか
	final double[] weaponStrExtend = new double[W_K_L], //威力継承率
		weaponFiRExtend = new double[W_K_L], //連射間隔継承率
		weaponLiRExtend = new double[W_K_L], //制限距離継承率
		weaponBulletSpdExtend = new double[W_K_L], //弾速継承率
		weaponBulletPenExtend = new double[W_K_L], //弾貫通回数継承率
		weaponBulletRefExtend = new double[W_K_L], //弾跳弾回数継承率
		weaponBulletOfSExtend = new double[W_K_L], //弾相殺性能継承率
		weaponBulletLiFExtend = new double[W_K_L]; //弾残留時間継承率
	final String weaponImg[] = new String[W_K_L],
		weaponIconImg[] = new String[W_K_L];
	int weaponKindTotal; //種類数
	
	final HashMap<String,Integer> nameToID_weapon = new HashMap<String,Integer>();

	//エンティティ情報
	String[] entityName = new String[W_K_L]; //エンティティの名前、他configで名前指定されるときに使用
	int[] entityHP = new int[W_K_L], //体力
		entitySize = new int[W_K_L], //衝突判定大きさ
		entitySelectSize = new int[W_K_L], //選択判定大きさ（エディター用）
		entityHeat = new int[W_K_L], //発熱量（サーモグラフィーに影響）
		entityWeight = new int[W_K_L]; //重量（ノックバックに影響）
	BitSet entityConflict = new BitSet(W_K_L);
	String[] entityImg = new String[W_K_L], //画像パス
		entityIconImg = new String[W_K_L], //エディター用アイコン画像パス
		entityCode = new String[W_K_L]; //追加コードファイル名
	int entityKindTotal; //種類数

	HashMap<String,Integer> nameToID_entity = new HashMap<String,Integer>();
	
	String errorSource = "*未指定*"; //現在の読み込み箇所名(エラー出力用)

	final String[] configType = {"enemy","entity","effect","weapon","bullet","gimmick"};
	final FilenameFilter iniFilter = new FilenameFilter(){
			public boolean accept(File dir,String name){
				return name.endsWith(".ini") && !name.equals("desktop.ini");
			}
		};
	String nowConfigURL; //現在読み込み中のファイルパス
	final String[] propertyN = new String[32],propertyV = new String[32];
	final boolean[] propertyIsUsed = new boolean[32]; //使用済みの項目（警告表示用）
	final int[] propertyLine = new int[32]; //項目の行番号（警告表示用）
	int enemy_maxID = -1,entity_maxID = -1,effect_maxID = -1,weapon_maxID = -1,bullet_maxID = -1,gimmick_maxID = -1;
	int loadedFileNumber = 0;
	//シリアライズ用クラス用意
	final Config cfg = new Config();
	//名前指定プロパティのバックアップ
	//enemy
	final String[] enemyWeapon_BackUp = new String[W_K_L];
	//gimmick
	String[] gimmickDestroyEffect_BackUp = new String[W_K_L];
	//weapon
	String[][] weaponBulletKind_BackUp = new String[W_K_L][];
	String[] weaponGunChain_BackUp = new String[W_K_L];
	//bullet
	String bulletWithGun_BackUp[][] = new String[W_K_L][],
		bulletLostGun_BackUp[][] = new String[W_K_L][],
		bulletDestroyGun_BackUp[][] = new String[W_K_L][],
		bulletWithEffect_BackUp[][] = new String[W_K_L][],
		bulletDestroyEffect_BackUp[][] = new String[W_K_L][],
		bulletLostEffect_BackUp[][] = new String[W_K_L][];
	int propertyTotal = 0; //プロパティ総数
	
	//その他汎用変数
	long messageTime; //メッセージウィンドウの表示に要した時間、ロード時間算出時に使用
	
	public static void main(String[] args){
		new ConfigLoader();
	}
	
	public ConfigLoader(){
		
		final long loadStartTime = System.currentTimeMillis();
		//ローカルコンフィグの読み込み
		System.out.println("BreakScope Ver1.8.0(beta8.0) ConfigLoader");
		System.out.println("start local config load...");
		System.out.println("loaded files:");
		loadConfig_MultiVersion("source","1.0");
		loadConfig_MultiVersion("source","2.0");
		//MODコンフィグの読み込み
		System.out.println("\nstart mod config load...");
		System.out.println("loaded mods:");
		final FileFilter dicFilter = new FileFilter(){
			public boolean accept(File dir){
				return dir.isDirectory();
			}
		};
		File[] folderFiles = new File[0];
		try{
			folderFiles = new File(getClass().getResource("source/__mod__").toURI()).listFiles(dicFilter);
		}catch(URISyntaxException e){
			System.out.println("modファイルが見つかりません");
		}
		for(File file : folderFiles)
			loadConfig_MultiVersion("source/__mod__/" + file.getName(),"2.0");
		//総数を記録
		enemyKindTotal = enemy_maxID + 1;
		entityKindTotal = entity_maxID + 1;
		effectKindTotal = effect_maxID + 1;
		gimmickKindTotal = gimmick_maxID + 1;
		bulletKindTotal = bullet_maxID + 1;
		weaponKindTotal = weapon_maxID + 1;
		
		System.out.println("\nconverting names to ID...");
		//名前バックアップを種類IDに変換する
		//敵装備武器
		autoIDConverter_1D(BreakScope.WEAPON,enemyWeapon_BackUp,enemyWeapon,enemyName,"enemy","Weapon");
		//ギミック破壊エフェクト
		autoIDConverter_1D(BreakScope.EFFECT,gimmickDestroyEffect_BackUp,gimmickDestroyEffect,weaponName,"gimmick","DestroyEffect");
		//武器発射弾
		autoIDConverter_2D(BreakScope.BULLET,weaponBulletKind_BackUp,weaponBulletKind,weaponName,"weapon","BulletKind");
		//武器連続発動銃
		autoIDConverter_1D(BreakScope.WEAPON,weaponGunChain_BackUp,weaponGunChain,weaponName,"weapon","GunChain");
		//弾常時銃
		autoIDConverter_2D(BreakScope.WEAPON,bulletWithGun_BackUp,bulletWithGun,bulletName,"bullet","WithGun");
		//for(int i = 0;i < bulletWithGun.length;i++)
			//System.out.println(bulletName[i] + " , " + bulletWithGun[i].length);
		//弾破壊時銃
		autoIDConverter_2D(BreakScope.WEAPON,bulletDestroyGun_BackUp,bulletDestroyGun,bulletName,"bullet","DestroyGun");
		//弾消滅時銃
		autoIDConverter_2D(BreakScope.WEAPON,bulletLostGun_BackUp,bulletLostGun,bulletName,"bullet","LostGun");
		//弾継続エフェクト
		autoIDConverter_2D(BreakScope.EFFECT,bulletWithEffect_BackUp,bulletWithEffect,bulletName,"bullet","WithEffect");
		//弾破壊エフェクト
		autoIDConverter_2D(BreakScope.EFFECT,bulletDestroyEffect_BackUp,bulletDestroyEffect,bulletName,"bullet","DestroyEffect");
		//弾消滅エフェクト
		autoIDConverter_2D(BreakScope.EFFECT,bulletLostEffect_BackUp,bulletLostEffect,bulletName,"bullet","LostEffect");
		
		//読み込み結果をファイルに出力
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter("source/log/enemyIDs.txt"));
			for(int i = 0;i < enemyKindTotal;i++)
				bw.write(i + ": " + enemyName[i] + "\r\n");
			bw.flush();
			bw = new BufferedWriter(new FileWriter("source/log/entityIDs.txt"));
			for(int i = 0;i < entityKindTotal;i++)
				bw.write(i + ": " + entityName[i] + "\r\n");
			bw.flush();
			bw = new BufferedWriter(new FileWriter("source/log/gimmickIDs.txt"));
			for(int i = 0;i < gimmickKindTotal;i++)
				bw.write(i + ": " + gimmickName[i] + "\r\n");
			bw.flush();
			bw = new BufferedWriter(new FileWriter("source/log/effectIDs.txt"));
			for(int i = 0;i < effectKindTotal;i++)
				bw.write(i + ": " + effectName[i] + "\r\n");
			bw.flush();
			bw = new BufferedWriter(new FileWriter("source/log/weaponIDs.txt"));
			for(int i = 0;i < weaponKindTotal;i++)
				bw.write(i + ": " + weaponName[i] + "\r\n");
			bw.flush();
			bw = new BufferedWriter(new FileWriter("source/log/bulletIDs.txt"));
			for(int i = 0;i < bulletKindTotal;i++)
				bw.write(i + ": " + bulletName[i] + "\r\n");
			bw.flush();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				bw.close();
			}catch(Exception e){}
		}
		System.out.println("finish config load");
		System.out.println("writing...");
		//配列圧縮処理
		cfg.enemyName = Arrays.copyOf(enemyName,enemyKindTotal);
		cfg.enemyHP = Arrays.copyOf(enemyHP,enemyKindTotal);
		cfg.enemyWeapon = Arrays.copyOf(enemyWeapon,enemyKindTotal);
		cfg.enemySpeed = Arrays.copyOf(enemySpeed,enemyKindTotal);
		cfg.enemyTurnSpeed = Arrays.copyOf(enemyTurnSpeed,enemyKindTotal);
		cfg.enemySize = Arrays.copyOf(enemySize,enemyKindTotal);
		cfg.enemySelectSize = Arrays.copyOf(enemySelectSize,enemyKindTotal);
		cfg.enemyScore = Arrays.copyOf(enemyScore,enemyKindTotal);
		cfg.enemyItem = Arrays.copyOf(enemyItem,enemyKindTotal);
		cfg.enemyHeat = Arrays.copyOf(enemyHeat,enemyKindTotal);
		cfg.enemyWeight = Arrays.copyOf(enemyWeight,enemyKindTotal);
		cfg.enemyConflict = enemyConflict.get(0,enemyKindTotal);
		cfg.enemyImg = Arrays.copyOf(enemyImg,enemyKindTotal);
		cfg.enemyIconImg = Arrays.copyOf(enemyIconImg,enemyKindTotal);
		cfg.enemyCode = Arrays.copyOf(enemyCode,enemyKindTotal);
		cfg.enemyKindTotal = enemyKindTotal;
		//entity
		cfg.entityName = Arrays.copyOf(entityName,entityKindTotal);
		cfg.entityHP = Arrays.copyOf(entityHP,entityKindTotal);
		cfg.entitySize = Arrays.copyOf(entitySize,entityKindTotal);
		cfg.entitySelectSize = Arrays.copyOf(entitySelectSize,entityKindTotal);
		cfg.entityHeat = Arrays.copyOf(entityHeat,entityKindTotal);
		cfg.entityWeight = Arrays.copyOf(entityWeight,entityKindTotal);
		cfg.entityConflict = entityConflict.get(0,entityKindTotal);
		cfg.entityImg = Arrays.copyOf(entityImg,entityKindTotal);
		cfg.entityIconImg = Arrays.copyOf(entityIconImg,entityKindTotal);
		cfg.entityCode = Arrays.copyOf(entityCode,entityKindTotal);
		cfg.entityKindTotal = entityKindTotal;
		//effect
		cfg.effectName = Arrays.copyOf(effectName,effectKindTotal);
		cfg.effectDisplacement = Arrays.copyOf(effectDisplacement,effectKindTotal);
		cfg.effectQuantity = Arrays.copyOf(effectQuantity,effectKindTotal);
		cfg.effectSizeGrow = Arrays.copyOf(effectSizeGrow,effectKindTotal);
		cfg.effectSize_min = Arrays.copyOf(effectSize_min,effectKindTotal);
		cfg.effectSize_max = Arrays.copyOf(effectSize_max,effectKindTotal);
		cfg.effectSpeed_min = Arrays.copyOf(effectSpeed_min,effectKindTotal);
		cfg.effectSpeed_max = Arrays.copyOf(effectSpeed_max,effectKindTotal);
		cfg.effectAlphaInitial = Arrays.copyOf(effectAlphaInitial,effectKindTotal);
		cfg.effectAlphaChanges = Arrays.copyOf(effectAlphaChanges,effectKindTotal);
		cfg.effectAlphaChangeFrame = Arrays.copyOf(effectAlphaChangeFrame,effectKindTotal);
		cfg.effectAccel = Arrays.copyOf(effectAccel,effectKindTotal);
		cfg.effectAmount = Arrays.copyOf(effectAmount,effectKindTotal);
		cfg.effectTimePhase = Arrays.copyOf(effectTimePhase,effectKindTotal);
		cfg.effectLimitFrame = Arrays.copyOf(effectLimitFrame,effectKindTotal);
		cfg.effectImg = Arrays.copyOf(effectImg,effectKindTotal);
		cfg.effectKindTotal = effectKindTotal;
		//bullet
		cfg.bulletName = Arrays.copyOf(bulletName,bulletKindTotal);
		cfg.bulletSize = Arrays.copyOf(bulletSize,bulletKindTotal);
		cfg.bulletWithEffect = Arrays.copyOf(bulletWithEffect,bulletKindTotal);
		cfg.bulletDestroyEffect = Arrays.copyOf(bulletDestroyEffect,bulletKindTotal);
		cfg.bulletLostEffect = Arrays.copyOf(bulletLostEffect,bulletKindTotal);
		cfg.bulletLimitFrame = Arrays.copyOf(bulletLimitFrame,bulletKindTotal);
		cfg.bulletStallRatio = Arrays.copyOf(bulletStallRatio,bulletKindTotal);
		cfg.bulletHitBurst = Arrays.copyOf(bulletHitBurst,bulletKindTotal);
		cfg.bulletWithGun = Arrays.copyOf(bulletWithGun,bulletKindTotal);
		cfg.bulletGunRate = Arrays.copyOf(bulletGunRate,bulletKindTotal);
		cfg.bulletDestroyGun = Arrays.copyOf(bulletDestroyGun,bulletKindTotal);
		cfg.bulletLostGun = Arrays.copyOf(bulletLostGun,bulletKindTotal);
		cfg.bulletRotateSpeed = Arrays.copyOf(bulletRotateSpeed,bulletKindTotal);
		cfg.bulletRotateStartAngle = Arrays.copyOf(bulletRotateStartAngle,bulletKindTotal);
		cfg.bulletRotateRadius = Arrays.copyOf(bulletRotateRadius,bulletKindTotal);
		cfg.bulletLaserAction = bulletLaserAction.get(0,bulletKindTotal);
		cfg.bulletGunnerFollow = bulletGunnerFollow.get(0,bulletKindTotal);
		cfg.bulletHeatHoming = bulletHeatHoming.get(0,bulletKindTotal);
		cfg.bulletSuperPenetration = bulletSuperPenetration.get(0,bulletKindTotal);
		cfg.bulletImg = Arrays.copyOf(bulletImg,bulletKindTotal);
		cfg.bulletKindTotal = bulletKindTotal;
		//gimmick
		cfg.gimmickName = Arrays.copyOf(gimmickName,gimmickKindTotal);
		cfg.gimmickHP = Arrays.copyOf(gimmickHP,gimmickKindTotal);
		cfg.gimmickHeat = Arrays.copyOf(gimmickHeat,gimmickKindTotal);
		cfg.gimmickDestroyEffect = Arrays.copyOf(gimmickDestroyEffect,gimmickKindTotal);
		cfg.gimmickDamage = Arrays.copyOf(gimmickDamage,gimmickKindTotal);
		cfg.gimmickExplosive = Arrays.copyOf(gimmickExplosive,gimmickKindTotal);
		cfg.gimmickBrightness = Arrays.copyOf(gimmickBrightness,gimmickKindTotal);
		cfg.gimmickImg = Arrays.copyOf(gimmickImg,gimmickKindTotal);
		cfg.gimmickIconImg = Arrays.copyOf(gimmickIconImg,gimmickKindTotal);
		cfg.gimmickCode = Arrays.copyOf(gimmickCode,gimmickKindTotal);
		cfg.gimmickKindTotal = gimmickKindTotal;
		//weapon
		cfg.weaponName = Arrays.copyOf(weaponName,weaponKindTotal);
		cfg.weaponBulletKind = Arrays.copyOf(weaponBulletKind,weaponKindTotal);
		cfg.weaponStrength = Arrays.copyOf(weaponStrength,weaponKindTotal);
		cfg.weaponCost = Arrays.copyOf(weaponCost,weaponKindTotal);
		cfg.weaponTiming = Arrays.copyOf(weaponTiming,weaponKindTotal);
		cfg.weaponBurst = Arrays.copyOf(weaponBurst,weaponKindTotal);
		cfg.weaponFireRate = Arrays.copyOf(weaponFireRate,weaponKindTotal);
		cfg.weaponAberration = Arrays.copyOf(weaponAberration,weaponKindTotal);
		cfg.weaponDirection = Arrays.copyOf(weaponDirection,weaponKindTotal);
		cfg.weaponDirectionCorrect = Arrays.copyOf(weaponDirectionCorrect,weaponKindTotal);
		cfg.weaponFirePoint = Arrays.copyOf(weaponFirePoint,weaponKindTotal);
		cfg.weaponFirePointsX = Arrays.copyOf(weaponFirePointsX,weaponKindTotal);
		cfg.weaponFirePointsY = Arrays.copyOf(weaponFirePointsY,weaponKindTotal);
		cfg.weaponBulletSpeed = Arrays.copyOf(weaponBulletSpeed,weaponKindTotal);
		cfg.weaponBulletSpeedDispersion = Arrays.copyOf(weaponBulletSpeedDispersion,weaponKindTotal);
		cfg.weaponBulletReflectiveness = Arrays.copyOf(weaponBulletReflectiveness,weaponKindTotal);
		cfg.weaponBulletReflectDamageRatio = Arrays.copyOf(weaponBulletReflectDamageRatio,weaponKindTotal);
		cfg.weaponBulletOffSet = Arrays.copyOf(weaponBulletOffSet,weaponKindTotal);
		cfg.weaponBulletPenetration = Arrays.copyOf(weaponBulletPenetration,weaponKindTotal);
		cfg.weaponBulletKnockBack = Arrays.copyOf(weaponBulletKnockBack,weaponKindTotal);
		cfg.weaponGunnerForce = Arrays.copyOf(weaponGunnerForce,weaponKindTotal);
		cfg.weaponBulletAccel = Arrays.copyOf(weaponBulletAccel,weaponKindTotal);
		cfg.weaponBulletAccelDispersion = Arrays.copyOf(weaponBulletAccelDispersion,weaponKindTotal);
		cfg.weaponBulletAccelDirection = Arrays.copyOf(weaponBulletAccelDirection,weaponKindTotal);
		cfg.weaponBulletAccelDirectionCorrect = Arrays.copyOf(weaponBulletAccelDirectionCorrect,weaponKindTotal);
		cfg.weaponLimitRange = Arrays.copyOf(weaponLimitRange,weaponKindTotal);
		cfg.weaponAmmoKind = Arrays.copyOf(weaponAmmoKind,weaponKindTotal);
		cfg.weaponMagazineSize = Arrays.copyOf(weaponMagazineSize,weaponKindTotal);
		cfg.weaponAutoReload = weaponAutoReload.get(0,weaponKindTotal);
		cfg.weaponReloadTime = Arrays.copyOf(weaponReloadTime,weaponKindTotal);
		cfg.weaponGunLoop = Arrays.copyOf(weaponGunLoop,weaponKindTotal);
		cfg.weaponGunLoopDelay = Arrays.copyOf(weaponGunLoopDelay,weaponKindTotal);
		cfg.weaponGunChain = Arrays.copyOf(weaponGunChain,weaponKindTotal);
		cfg.weaponGunChainDelay = Arrays.copyOf(weaponGunChainDelay,weaponKindTotal);
		cfg.weaponInertiaRate = Arrays.copyOf(weaponInertiaRate,weaponKindTotal);
		cfg.weaponSE = Arrays.copyOf(weaponSE,weaponKindTotal);
		cfg.weaponSEIsSerial = weaponSEIsSerial.get(0,weaponKindTotal);
		cfg.weaponUPG1Limit = Arrays.copyOf(weaponUPG1Limit,weaponKindTotal);
		cfg.weaponUPG2Limit = Arrays.copyOf(weaponUPG2Limit,weaponKindTotal);
		cfg.weaponUPG3Limit = Arrays.copyOf(weaponUPG3Limit,weaponKindTotal);
		cfg.weaponUPG4Limit = Arrays.copyOf(weaponUPG4Limit,weaponKindTotal);
		cfg.weaponActionType = Arrays.copyOf(weaponActionType,weaponKindTotal);
		cfg.weaponBulletPreventMultiPenetration = weaponBulletPreventMultiPenetration.get(0,weaponKindTotal);
		cfg.weaponImg = Arrays.copyOf(weaponImg,weaponKindTotal);
		cfg.weaponIconImg = Arrays.copyOf(weaponIconImg,weaponKindTotal);
		cfg.weaponStrExtend = Arrays.copyOf(weaponStrExtend,weaponKindTotal);
		cfg.weaponFiRExtend = Arrays.copyOf(weaponFiRExtend,weaponKindTotal);
		cfg.weaponLiRExtend = Arrays.copyOf(weaponLiRExtend,weaponKindTotal);
		cfg.weaponBulletSpdExtend = Arrays.copyOf(weaponBulletSpdExtend,weaponKindTotal);
		cfg.weaponBulletPenExtend = Arrays.copyOf(weaponBulletPenExtend,weaponKindTotal);
		cfg.weaponBulletRefExtend = Arrays.copyOf(weaponBulletRefExtend,weaponKindTotal);
		cfg.weaponBulletOfSExtend = Arrays.copyOf(weaponBulletOfSExtend,weaponKindTotal);
		cfg.weaponBulletLiFExtend = Arrays.copyOf(weaponBulletLiFExtend,weaponKindTotal);
		cfg.weaponKindTotal = weaponKindTotal;
		//名前-ID変換HashMapの保存
		cfg.nameToID_enemy = nameToID_enemy;
		cfg.nameToID_entity = nameToID_entity;
		cfg.nameToID_effect = nameToID_effect;
		cfg.nameToID_gimmick = nameToID_gimmick;
		cfg.nameToID_weapon = nameToID_weapon;
		cfg.nameToID_bullet = nameToID_bullet;
		
		//ConfigCodeのシリアライズ
		
		//敵クラスの読み込み
		final EnemyListener[] enemyClass = new EnemyListener[cfg.enemyKindTotal];
		for(int kind = 0;kind < cfg.enemyKindTotal;kind++){
			final String name = enemyCode[kind];
			if(!BreakScope.isActualString(name)){ //追加コードなし
				enemyClass[kind] = new EnemyListener();
				continue;
			}
			try{
				final Object obj = Class.forName(name).newInstance(); //インスタンスを生成
				if(obj instanceof EnemyListener)
					enemyClass[kind] = (EnemyListener)obj; //インスタンスを保存
			}catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
				System.out.println(e);
				enemyClass[kind] = new EnemyListener();
			}
		}
		cfg.enemyClass = enemyClass;
		
		//ギミッククラスの読み込み
		final GimmickListener[] gimmickClass = new GimmickListener[cfg.gimmickKindTotal];
		for(int kind = 0;kind < cfg.gimmickKindTotal;kind++){
			final String name = gimmickCode[kind];
			if(!BreakScope.isActualString(name)){ //追加コードなし
				gimmickClass[kind] = new GimmickListener();
				continue;
			}
			try{
				final Object obj = Class.forName(name).newInstance(); //インスタンスを生成
				if(obj instanceof GimmickListener)
					gimmickClass[kind] = (GimmickListener)obj; //インスタンスを保存
			}catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
				System.out.println(e);
				gimmickClass[kind] = new GimmickListener();
			}
		}
		cfg.gimmickClass = gimmickClass;
		
		//エンティティクラスの読み込み
		final EntityListener[] entityClass = new EntityListener[cfg.entityKindTotal];
		for(int kind = 0;kind < cfg.entityKindTotal;kind++){
			final String name = entityCode[kind];
			if(!BreakScope.isActualString(name)){ //追加コードなし
				entityClass[kind] = new EntityListener();
				continue;
			}
			try{
				final Object obj = Class.forName(name).newInstance(); //インスタンスを生成
				if(obj instanceof EntityListener)
					entityClass[kind] = (EntityListener)obj; //インスタンスを保存
			}catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
				System.out.println(e);
				entityClass[kind] = new EntityListener();
			}
		}
		cfg.entityClass = entityClass;
		
		//読み込み結果をコンソールに出力
		/* SourceDataからの読み込み 変数cfgに一部の可変設定が記録されている */
		Config oldCfg;
		try(ObjectInputStream ois = new ObjectInputStream(getClass().getResourceAsStream("source/configPackage/configTextData.txt"))){
			oldCfg = (Config)ois.readObject();
		}catch(IOException | ClassNotFoundException e){
			oldCfg = null;
		}
		if(oldCfg != null){
			this.plintARLog("弾",cfg.bulletName,oldCfg.bulletName);
			this.plintARLog("エフェクト",cfg.effectName,oldCfg.effectName);
			this.plintARLog("敵",cfg.enemyName,oldCfg.enemyName);
			this.plintARLog("エンティティ",cfg.entityName,oldCfg.entityName);
			this.plintARLog("ギミック",cfg.gimmickName,oldCfg.gimmickName);
			this.plintARLog("武器",cfg.weaponName,oldCfg.weaponName);
		}
		
		try{ //コンフィグテキストデータの書き込み
			final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("source/configPackage/configTextData.txt"));
			oos.writeObject(cfg);
			oos.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		System.out.println("config load complete!");
		System.out.println("file count: " + loadedFileNumber);
		System.out.println("load time: " + (System.currentTimeMillis() - loadStartTime - messageTime) + "(ms)");
		System.out.println("message time: " + messageTime + "(ms)");
	}
	final void loadConfig_MultiVersion(String src,String versionStr){
		//バージョン指定を検査
		double version = 0;
		if(versionStr.equals("1.0")){
			if(getClass().getResource(src + "/__config1.0__") == null){
				System.out.println("（バージョン1.0のファイルは見つかりませんでした）");
				return;
			}
			version = 1.0;
		}else if(versionStr.equals("2.0")){
			if(getClass().getResource(src + "/__config2.0__") == null){
				System.out.println("（バージョン2.0のファイルは見つかりませんでした）");
				return;
			}
			version = 2.0;
		}else{
			System.out.println("コンフィグバージョン「" + versionStr + "」は指定できません");
			return;
		}
		String[] fileNames; //ファイル名
		if(version == 1.0){
			//バージョンC1.0の読み込み
			//"__config1.0__"フォルダ内に"enemy","entity","gimmick",...などのフォルダがあり、その中にiniファイルがある形式
			final String configURL = src + "/__config1.0__";
			for(String typeName : configType){ //まず敵・弾・ギミック...のフォルダで区分
				try{
					fileNames = new File(getClass().getResource(configURL + "/" + typeName).toURI()).list(iniFilter); //フォルダー内のiniファイルを検出
				}catch(URISyntaxException e){
					System.out.println(e);
					continue;
				}
				for(String fileName : fileNames){ //次にファイルごとで区分
					BufferedReader br = null;
					nowConfigURL = configURL + "/"  + typeName;
					try{ //ファイル読み込み開始
						br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(nowConfigURL + "/" + fileName),"SJIS"));
					}catch(IOException e){
						try{
							br.close();
						}catch(Exception e2){}
						continue;
					}
					errorSource = "[" + typeName + "]「" + fileName + "」";
					boolean blockEntered = false;
					String tagName = "";
					loadedFileNumber++; //ロード済みファイル数更新
					System.out.println("[config1.0]" + fileName); //ロードしたファイルの名前を出力
					int line = 0;
					while(true){
						String strData;
						try{
							strData = br.readLine();
							line++;
						}catch(IOException e){
							continue;
						}
						if(!blockEntered){ //最初のタグを見つける
							if(strData == null) //何のタグもなく読み込み終了
								break;
							if(strData.startsWith("[") && strData.endsWith("]")){ //最初のタグを見つける
								tagName = strData;
								blockEntered = true;
							}
						}else{
							readTag: {
								if(strData == null) //null-読み込み終了
									break readTag; //タグチェック処理へ
								else if(strData.startsWith("[") && strData.endsWith("]")){ //新たなタグを見つける
									break readTag; //タグチェック処理へ
								}else{ //プロパティ、または注釈文を見つける
									final int equalCharAt = strData.indexOf("="); //プロパティの記述は必ず"="が含まれる
									if(equalCharAt == -1 || strData.startsWith("//")) //コメント記号で始まる文は無視する
										continue;
									//"="があった位置を保存して、strDataを名前と値に分割
									final String propertyN_tmp = BreakScope.trim2(strData.substring(0,equalCharAt)),
												propertyV_tmp = BreakScope.trim2(strData.substring(equalCharAt + 1));
									if(!BreakScope.isActualString(propertyN_tmp) || !BreakScope.isActualString(propertyV_tmp)) //プロパティの名前、値の両方が有効値であることを確認
										continue;
									propertyN[propertyTotal] = propertyN_tmp; //プロパティ名を登録
									propertyV[propertyTotal] = propertyV_tmp; //プロパティ値を登録
									propertyLine[propertyTotal] = line; //このプロパティの行番号を記録(未使用プロパティの警告表示に使う)
									propertyIsUsed[propertyTotal] = false; //未使用状態にする
									propertyTotal++; //プロパティ総数をカウント
								}
								continue;
							}
							//タグチェック処理
							//break readTagのときのみここへジャンプ
							switch(typeName){ //各項目の代入
								case "enemy":
									readEnemyTag(++enemy_maxID,"1.0");
									break;
								case "entity":
									readEntityTag(++entity_maxID,"1.0");
									break;
								case "effect":
									readEffectTag(++effect_maxID,"1.0");
									break;
								case "weapon":
									readWeaponTag(++weapon_maxID,"1.0");
									break;
								case "bullet":
									readBulletTag(++bullet_maxID,"1.0");
									break;
								case "gimmick":
									readGimmickTag(++gimmick_maxID,"1.0");
							}
							//チェック終了、次のタグへの準備
							//エラー出力のために、このタグの名前を保存する
							if(strData != null && strData.length() > 2) //タグに名前がある"[abc]"
								tagName = strData;
							else //タグに名前がない"[]"
								tagName = "*未指定*";
							//未使用プロパティを警告する
							final StringBuilder sb = new StringBuilder();
							int errorCount = 0;
							for(int k = 0;k < propertyTotal;k++){
								if(propertyIsUsed[k] //しっかり使われた項目
								|| propertyN[k].equalsIgnoreCase("UPG1Limit") //旧バージョン削除項目名
								|| propertyN[k].equalsIgnoreCase("UPG2Limit")
								|| propertyN[k].equalsIgnoreCase("UPG3Limit")
								|| propertyN[k].equalsIgnoreCase("UPG4Limit"))
									continue;
								errorCount++;
								if(errorCount >= 10){ //10個より多いものは...moreを表示して切る
									sb.append("\n...more");
									break;
								}else
									sb.append("\n").append("Line").append(propertyLine[k]).append(": ").append(propertyN[k]);
							}
							//※sbは最初に"\n"を必ず伴う構造をとっている
							if(errorCount > 0)
								messageTime += BreakScope.messageBox("以下の項目は名前が不適切か、重複しており、現在のバージョンで対応されなくなったため、ロードされませんでした。" + sb.toString(),"config構成エラー");
							propertyTotal = 0; //プロパティ総数をクリア
							if(strData == null) //ファイル最後のタグのチェック
								break; //次のファイルへ
						}
					}
					try{
						br.close();
					}catch(IOException e){}
				}
			}
		}else if(version == 2.0){
			//バージョンC2.0の読み込み
			//"__config2.0__"フォルダ内に1段階のファイル構造を作ることができ、複数のコンフィグタイプを同一iniファイル内に書き込める形式
			final String configURL = src + "/__config2.0__";
			try{
				fileNames = new File(getClass().getResource(configURL).toURI()).list(iniFilter); //__config2.0__フォルダー内のiniファイルを検出
			}catch(URISyntaxException e){
				System.out.println(e);
				return;
			}
			file: for(String fileName : fileNames){ //ファイルごとに区分
				BufferedReader br = null;
				nowConfigURL = configURL;
				try{ //ファイル読み込み開始
					br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(configURL + "/" + fileName),"SJIS"));
				}catch(IOException e){
					try{
						br.close();
					}catch(Exception e2){}
					continue;
				}
				boolean blockEntered = false;
				String nowType = "";
				System.out.println("[config2.0]" + fileName); //ロードしたファイルの名前を出力
				int line = 0;
				int lastTagLine = 0;
				read: while(true){
					String strData;
					try{
						strData = br.readLine();
						line++;
					}catch(IOException e){
						continue read;
					}
					if(!blockEntered){ //まだタグを見つけていない
						if(strData == null) //何のタグもなく読み込み終了
							break read;
						if(strData.startsWith("[") && strData.endsWith("]")){ //最初のタグを見つける
							for(String ver : configType){ //タグにタイプ名が書いてあるので、これを判別する
								if(ver.equals(strData.substring(1,strData.length() - 1))){
									nowType = ver;
									errorSource = "「" + src + "」MODの[" + ver + "]「" + fileName + "」";
								}
							}
							lastTagLine = line; //この行にタグがあったことを記録
							blockEntered = true;
						}
					}else{
						String nextType = null;
						tag: { //１つのタグを抜けるまでこの中をループ
							if(strData == null) //null-読み込み終了
								break tag; //タグチェック処理へ
							else if(strData.startsWith("[") && strData.endsWith("]")){ //新たなタグを見つける
								for(String ver : configType){ //タグにタイプ名が書いてあるので、これを判別する
									if(ver.equals(strData.substring(1,strData.length() - 1))){
										nextType = ver;
										errorSource = "「" + src + "」MODの[" + ver + "]「" + fileName + "」";
									}
								}
								lastTagLine = line; //この行にタグがあったことを記録
								break tag; //タグチェック処理へ
							}else{ //プロパティ、または注釈文を見つける
								//継承
								if(lastTagLine + 1 == line && strData.equalsIgnoreCase("#extend")){ //タグ行のすぐ次に継承宣言がかいてある
									extendsMode = true; //継承モードを起動
									continue read; //次の行へ
								}
								final int equalCharAt = strData.indexOf("="); //プロパティの記述は必ず"="が含まれる
								if(equalCharAt == -1) //注釈文や空行
									continue read; //次の行へ
								//"="があった位置を保存して、strDataを名前と値に分割
								final String propertyN_tmp = BreakScope.trim2(strData.substring(0,equalCharAt)),
											propertyV_tmp = BreakScope.trim2(strData.substring(equalCharAt + 1));
								if(!BreakScope.isActualString(propertyN_tmp) || !BreakScope.isActualString(propertyV_tmp)) //プロパティの名前、値の両方が有効値であることを確認
									continue read; //どちらかが無効の場合、無視して次の行へ
								if(propertyTotal >= propertyN.length){ //異常-プロパティ数が配列上限に達する
									System.out.println("ファイルスキップ：" + fileName);
									break read; //次の行へ
								}
								//正常-通常の値書き込み
								propertyN[propertyTotal] = propertyN_tmp; //プロパティ名を登録
								propertyV[propertyTotal] = propertyV_tmp; //プロパティ値を登録
								propertyLine[propertyTotal] = line; //このプロパティの行番号を記録(未使用プロパティの警告表示に使う)
								propertyIsUsed[propertyTotal] = false; //未使用状態にする
								propertyTotal++; //プロパティ総数をカウント
							}
							continue read;
						}
						//タグチェック処理
						//「break tag;」のときのみここへジャンプ,それまではループを続けpropertyN&Vを書き込んでいく
						switch(nowType){ //各項目の代入
							case "enemy":
								readEnemyTag(++enemy_maxID,"2.0");
								break;
							case "entity":
								readEntityTag(++entity_maxID,"2.0");
								break;
							case "effect":
								readEffectTag(++effect_maxID,"2.0");
								break;
							case "weapon":
								readWeaponTag(++weapon_maxID,"2.0");
								break;
							case "bullet":
								readBulletTag(++bullet_maxID,"2.0");
								break;
							case "gimmick":
								readGimmickTag(++gimmick_maxID,"2.0");
								break;
							default:
								System.out.println("could not solve: " + nowType);
						}
						//継承モード初期化
						extendsMode = false;
						//未使用プロパティの警告
						final StringBuilder sb = new StringBuilder();
						int errorCount = 0;
						for(int k = 0;k < propertyTotal;k++){
							if(propertyIsUsed[k] //使われた項目
								|| propertyN[k].equalsIgnoreCase("UPG1Limit") //旧バージョン削除項目名
								|| propertyN[k].equalsIgnoreCase("UPG2Limit")
								|| propertyN[k].equalsIgnoreCase("UPG3Limit")
								|| propertyN[k].equalsIgnoreCase("UPG4Limit"))
								continue;
							errorCount++;
							if(errorCount >= 10){ //10個より多いものは...moreを表示して切る
								sb.append("\n...more");
								break;
							}else
								sb.append("\n").append("Line").append(propertyLine[k]).append(": ").append(propertyN[k]);
						}
						//※sbは最初に"\n"を必ず伴う構造をとっている
						if(errorCount > 0)
							messageTime += BreakScope.messageBox("以下の項目は名前が不適切か、重複しており、現在のバージョンで対応されなくなったため、ロードされませんでした。" + sb.toString(),"config構成エラー");
						propertyTotal = 0; //プロパティ総数をクリア
						if(strData == null) //ファイル末尾のタグを読み終えた
							break read; //次のファイルへ
						if(nextType != null)
							nowType = nextType;
					}
				}
				loadedFileNumber++; //ロード済みファイル数更新
				try{
					br.close();
				}catch(IOException e){}
			}
		}
	}
	/**
	* 敵のコンフィグを読み込みます。
	*/
	final void readEnemyTag(int id,String versionName){
		nameToID_enemy.put(enemyName[id] = getStringProperty("Name",""),id);
		putData_int(id,enemyHP,"HP",100);
		putData_String(id,enemyWeapon_BackUp,"Weapon","NONE");
		putData_int(id,enemySpeed,"Speed",0);
		putData_int(id,enemyTurnSpeed,"TurnSpeed",100);
		putData_int(id,enemySize,"Size",50);
		putData_int(id,enemySelectSize,"SelectSize",enemySize[id]);
		putData_int(id,enemyScore,"Score",0);
		putData_int(id,enemyItem,"Item",NONE);
		putData_int(id,enemyHeat,"Heat",0);
		putData_int(id,enemyWeight,"Weight",100);
		putData_BitSet(id,enemyConflict,"Conflict",true);
		String imgURL = getStringProperty("Img","NONE");
		enemyImg[id] = (extendsMode && blankDetected) ? (imgURL = enemyImg[id - 1]) : unifyURL(imgURL,"picture",versionName);
		final String iconImgURL = getStringProperty("IconImg",imgURL); //アイコンの指定がない場合は画像に準拠
		enemyIconImg[id] = (extendsMode && blankDetected) ? enemyIconImg[id - 1] : unifyURL(iconImgURL,"picture",versionName);
		final String codeURL = getStringProperty("Code","NONE");
		enemyCode[id] = (extendsMode && blankDetected) ? enemyCode[id - 1] : unifyURL(codeURL,"Enemy",versionName);
	}
	boolean extendsMode;
	final void putData_int(int id,int[] property,String name,int defaultValue){
		property[id] = getIntProperty(name,defaultValue);
		if(extendsMode && blankDetected)
			property[id] = property[id - 1];
	}
	final void putData_int(int id,int[] property,String[] names,int defaultValue){
		property[id] = getIntProperty(names,defaultValue);
		if(extendsMode && blankDetected)
			property[id] = property[id - 1];
	}
	final void putData_double(int id,double[] property,String name,double defaultValue){
		property[id] = getDoubleProperty(name,defaultValue);
		if(extendsMode && blankDetected)
			property[id] = property[id - 1];
	}
	final void putData_double(int id,double[] property,String[] names,double defaultValue){
		property[id] = getDoubleProperty(names,defaultValue);
		if(extendsMode && blankDetected)
			property[id] = property[id - 1];
	}
	final void putData_String(int id,String[] property,String name,String defaultValue){
		property[id] = getStringProperty(name,defaultValue);
		if(extendsMode && blankDetected)
			property[id] = property[id - 1];
	}
	final void putData_String(int id,String[] property,String[] names,String defaultValue){
		property[id] = getStringProperty(names,defaultValue);
		if(extendsMode && blankDetected)
			property[id] = property[id - 1];
	}
	final void putData_BitSet(int id,BitSet property,String name,boolean defaultValue){
		property.set(id,getBooleanProperty(name,defaultValue));
		if(extendsMode && blankDetected)
			property.set(id,property.get(id - 1));
	}
	final void putData_BitSet(int id,BitSet property,String[] names,boolean defaultValue){
		property.set(id,getBooleanProperty(names,defaultValue));
		if(extendsMode && blankDetected)
			property.set(id,property.get(id - 1));
	}
	/**
	* エンティティのコンフィグを読み込みます。
	*/
	final void readEntityTag(int id,String versionName){
		nameToID_entity.put(entityName[id] = getStringProperty("Name",""),id);
		putData_int(id,entityHP,"HP",100);
		putData_int(id,entitySize,"Size",50);
		putData_int(id,entitySelectSize,"SelectSize",entitySize[id]);
		putData_int(id,entityHeat,"Heat",0);
		putData_int(id,entityWeight,"Weight",100);
		putData_BitSet(id,entityConflict,"Conflict",true);
		String imgURL = getStringProperty("Img","NONE");
		entityImg[id] = (extendsMode && blankDetected) ? (imgURL = entityImg[id - 1]) : unifyURL(imgURL,"picture",versionName);
		final String iconImgURL = getStringProperty("IconImg",imgURL); //アイコンの指定がない場合は画像に準拠
		entityIconImg[id] = (extendsMode && blankDetected) ? entityIconImg[id - 1] : unifyURL(iconImgURL,"picture",versionName);
		final String codeURL = getStringProperty("Code","NONE");
		entityCode[id] = (extendsMode && blankDetected) ? entityCode[id - 1] : unifyURL(codeURL,"Entity",versionName);
	}
	/**
	* エフェクトのコンフィグを読み込みます。
	*/
	final void readEffectTag(int id,String versionName){
		nameToID_effect.put(effectName[id] = getStringProperty("Name",""),id);
		effectImg[id] = BreakScope.split2(getStringProperty("Img","NONE"),",");
		if(extendsMode && blankDetected)
			effectImg[id] = effectImg[id - 1];
		else{
			for(int i = 0;i < effectImg[id].length;i++)
				effectImg[id][i] = this.unifyURL(effectImg[id][i],"picture",versionName);
		}
		final String[] timePhase_tmp = BreakScope.split2(getStringProperty("ImgTimePhase","NONE"),",");
		int frameTotal = 0;
		if(BreakScope.isActualString(timePhase_tmp)){
			effectTimePhase[id] = new int[timePhase_tmp.length];
			for(int k = 0;k < timePhase_tmp.length;k++){
				try{
					frameTotal += (effectTimePhase[id][k] = Integer.parseInt(timePhase_tmp[k].trim())); //各画像の存在フレーム数の代入と、全体の存在フレーム数の加算
				}catch(NumberFormatException e){
					messageTime += alertWrongStyle("ImgTimePhase",Arrays.toString(timePhase_tmp));
				}
			}
			effectLimitFrame[id] = frameTotal; //全体の存在フレーム数の代入
		}else if(extendsMode && blankDetected){
			effectTimePhase[id] = effectTimePhase[id - 1];
			effectLimitFrame[id] = effectLimitFrame[id - 1];
		}else{
			effectTimePhase[id] = new int[0];
			effectLimitFrame[id] = 0;
		}
		putData_int(id,effectAmount,"Amount",1);
		putData_int(id,effectDisplacement,"Displacement",0);
		putData_int(id,effectSpeed_min,"Speed_min",0);
		putData_int(id,effectSpeed_max,"Speed_max",effectSpeed_min[id]);
		putData_double(id,effectAccel,new String[]{"StallRatio","Accel"},1.0);
		putData_int(id,effectSizeGrow,"SizeGrow",0);
		putData_int(id,effectSize_min,"Size_min",NONE);
		putData_int(id,effectSize_max,"Size_max",effectSize_min[id]);
		final double[] values = convertDouble(BreakScope.split2(getStringProperty("AlphaValues",""),","),NONE); //透過度(double)
		final int[] frames = convertInt(BreakScope.split2(getStringProperty("AlphaTimePhase",""),","),NONE); //タイミング(int)
		if(values.length == 1){ //固定透過度指定,またはデフォルト指定
			if(frames[0] != NONE) //異常-タイミング指定がある
				messageTime += BreakScope.messageBox("AlphaValues「" + Arrays.toString(values) + "」に対しAlphaTimePhase「" + Arrays.toString(frames) + "」は形式が不適切です。\n※固定透過度であれば、AlphaTimePhase項目は不要です。\n場所：" + errorSource,"config構成エラー");
			if(extendsMode && (values.length == 0 || values.length == 1 && !BreakScope.isActualNumber(values[0]))){ //継承モード&何の値も書かれていない
				effectAlphaInitial[id] = effectAlphaInitial[id - 1];
				effectAlphaChanges[id] = effectAlphaChanges[id - 1];
				effectAlphaChangeFrame[id] = effectAlphaChangeFrame[id - 1];
			}else{ //透過度だけを指定,またはデフォルトを適用
				effectAlphaInitial[id] = values[0] == NONE ? 1.0 : values[0];
				effectAlphaChanges[id] = new double[0];
				effectAlphaChangeFrame[id] = new int[0];
			}
		}else if(values.length != frames.length){ //異常-変動透過度指定において、両配列の長さが合わない
			if(frames[0] != NONE) //タイミング指定あり
				messageTime += BreakScope.messageBox("AlphaValues「" + Arrays.toString(values) + "」に対しAlphaTimePhase「" + Arrays.toString(frames) + "」は形式が不適切です。\n※配列の長さが一致しません。\n場所：" + errorSource,"config構成エラー");
			else //タイミング指定なし
				messageTime += BreakScope.messageBox("AlphaValues「" + Arrays.toString(values) + "」に対しAlphaTimePhase「*未指定*」は形式が不適切です。\n場所：" + errorSource,"config構成エラー");
			if(extendsMode){
				effectAlphaInitial[id] = effectAlphaInitial[id - 1];
				effectAlphaChanges[id] = effectAlphaChanges[id - 1];
				effectAlphaChangeFrame[id] = effectAlphaChangeFrame[id - 1];
			}else{
				effectAlphaInitial[id] = 1.0;
				effectAlphaChanges[id] = new double[0];
				effectAlphaChangeFrame[id] = new int[0];
			}
		}else{ //正常な変動透過度指定
			//※PCにとって処理しやすい形に変換する　到達値、タイミング　→　初期値、変化量、タイミング
			//配列の長さを合わせる
			effectAlphaInitial[id] = values[0]; //透過度初期値
			effectAlphaChanges[id] = new double[values.length];
			effectAlphaChangeFrame[id] = new int[values.length];
			for(int j = 0;j + 1 < values.length;j++){
				effectAlphaChanges[id][j] = (values[j + 1] - values[j])/(frames[j + 1] - frames[j]); //透過度変動値
				effectAlphaChangeFrame[id][j] = frames[j]; //上記透過度変動値に切り替えるタイミング
			}
			effectAlphaChanges[id][values.length - 1] = 0.0; //最後の変化量は0
			effectAlphaChangeFrame[effect_maxID][values.length - 1] = frames[values.length - 1];
		}
	}
	/**
	* ギミックのコンフィグを読み込みます。
	*/
	final void readGimmickTag(int id,String versionName){
		nameToID_gimmick.put(gimmickName[id] = getStringProperty("Name",""),id);
		putData_int(id,gimmickHP,"HP",NONE);
		if(gimmickHP[id] == MIN) //beta7.0対応,今のブレスコではNONE値以外の負の値は死亡判定になる
			gimmickHP[id] = NONE;
		putData_int(id,gimmickHeat,"Heat",0);
		putData_String(id,gimmickDestroyEffect_BackUp,"DestroyEffect","NONE");
		putData_int(id,gimmickDamage,"Damage",0);
		putData_int(id,gimmickExplosive,"Explosive",0);
		putData_int(id,gimmickBrightness,"Brightness",0);
		gimmickHP[id] = getIntProperty("HP",NONE);
		String imgURL = getStringProperty("Img","NONE");
		gimmickImg[id] = (extendsMode && blankDetected) ? (imgURL = gimmickImg[id - 1]) : unifyURL(imgURL,"picture",versionName);
		final String iconImgURL = getStringProperty("IconImg",imgURL); //アイコンの指定がない場合は画像に準拠
		gimmickIconImg[id] = (extendsMode && blankDetected) ? gimmickIconImg[id - 1] : unifyURL(iconImgURL,"picture",versionName);
		final String codeURL = getStringProperty("Code","NONE");
		gimmickCode[id] = (extendsMode && blankDetected) ? gimmickCode[id - 1] : unifyURL(codeURL,"Gimmick",versionName);
	}
	/**
	* 武器のコンフィグを読み込みます。
	*/
	final void readWeaponTag(int id,String versionName){
		nameToID_weapon.put(weaponName[id] = getStringProperty("Name",""),id);
		putData_int(id,weaponCost,"Cost",0);
		weaponBulletKind_BackUp[id] = BreakScope.split2(getStringProperty("BulletKind","NONE"),",");
		if(extendsMode && blankDetected)
			weaponBulletKind_BackUp[id] = weaponBulletKind_BackUp[id - 1];
		weaponStrength[id] = getIntProperty("Strength",0);
		final String timingStr = getStringProperty("Timing","NONE");
		if(timingStr.equalsIgnoreCase("STAGE_START")) //ステージ開始時に1回
			weaponTiming[id] = BreakScope.STAGE_START;
		else if(timingStr.equalsIgnoreCase("AUTO_ATTACK")) //全自動攻撃(RANGE範囲内の対抗勢力を探索)
			weaponTiming[id] = BreakScope.AUTO_ATTACK;
		else if(timingStr.equalsIgnoreCase("AUTO_CURE")) //全自動支援(RANGE範囲内の味方勢力を探索)
			weaponTiming[id] = BreakScope.AUTO_CURE;
		else if(timingStr.equalsIgnoreCase("MOVE")) //移動時
			weaponTiming[id] = BreakScope.MOVE;
		else if(timingStr.equalsIgnoreCase("STAND")) //静止時
			weaponTiming[id] = BreakScope.STAND;
		weaponBurst[id] = getIntProperty_saftyMin("Burst",1,1,true);
		weaponFireRate[id] = getIntProperty("FireRate",1);
		weaponAberration[id] = toRadians(getDoubleProperty("Aberration",0.0));
		weaponBulletSpeed[id] = getIntProperty("BulletSpeed",0);
		weaponBulletSpeedDispersion[id] = getIntProperty("BulletSpeedDispersion",0);
		//武器発射角度解析
		final String angleStr = getStringProperty("Direction","NONE");
		if(!BreakScope.isActualString(angleStr))
			weaponDirection[id] = NONE;
		else if(angleStr.equalsIgnoreCase("SELF") || angleStr.equalsIgnoreCase("+SELF"))
			weaponDirection[id] = BreakScope.SELF;
		else if(angleStr.equalsIgnoreCase("TARGET") || angleStr.equalsIgnoreCase("+TARGET"))
			weaponDirection[id] = BreakScope.TARGET;
		else if(angleStr.equalsIgnoreCase("REVERSE_SELF") || angleStr.equalsIgnoreCase("REVERSE-SELF") || angleStr.equalsIgnoreCase("-SELF"))
			weaponDirection[id] = BreakScope.REVERSE_SELF;
		else if(angleStr.equalsIgnoreCase("REVERSE_TARGET") || angleStr.equalsIgnoreCase("REVERSE-TARGET") || angleStr.equalsIgnoreCase("-TARGET"))
			weaponDirection[id] = BreakScope.REVERSE_TARGET;
		else{
			try{
				weaponDirection[id] = toRadians(Double.parseDouble(angleStr));
			}catch(NumberFormatException e){
				messageTime += BreakScope.messageBox("Direction「" + angleStr + "」は該当するものがありません。\n場所：" + errorSource,"config構成エラー");
				weaponDirection[id] = NONE;
			}
		}
		weaponDirectionCorrect[id] = toRadians(getDoubleProperty("DirectionCorrect",0.0));
		weaponBulletKnockBack[id] = getIntProperty("BulletKnockBack",0);
		weaponGunnerForce[id] = getIntProperty("GunnerForce",0);
		weaponBulletAccel[id] = getDoubleProperty("BulletAccel",0.0);
		weaponBulletAccelDispersion[id] = getIntProperty("BulletAccelDispersion",0);
		//武器加速度角度解析
		final String angleStr2 = getStringProperty("BulletAccelDirection","NONE");
		if(!BreakScope.isActualString(angleStr2))
			weaponBulletAccelDirection[id] = NONE;
		else if(angleStr2.equalsIgnoreCase("SELF"))
			weaponBulletAccelDirection[id] = BreakScope.SELF;
		else if(angleStr2.equalsIgnoreCase("TARGET"))
			weaponBulletAccelDirection[id] = BreakScope.TARGET;
		else if(angleStr2.equalsIgnoreCase("REVERSE_SELF"))
			weaponBulletAccelDirection[id] = BreakScope.REVERSE_SELF;
		else if(angleStr2.equalsIgnoreCase("REVERSE_TARGET"))
			weaponBulletAccelDirection[id] = BreakScope.REVERSE_TARGET;
		else{
			try{
				weaponBulletAccelDirection[id] = toRadians(Double.parseDouble(angleStr2));
			}catch(NumberFormatException e){
				messageTime += BreakScope.messageBox("BulletAccelDirection「" + angleStr2 + "」は該当するものがありません。\n場所：" + errorSource,"config構成エラー");
				weaponBulletAccelDirection[id] = NONE;
			}
		}
		weaponBulletAccelDirectionCorrect[id] = getDoubleProperty("BulletAccelDirectionCorrect",0.0);
		weaponBulletReflectiveness[id] = getIntProperty_saftyMin(new String[]{"BulletReflectiveness","BulletRefrectiveness"},0,0,true); //beta8.0対応,反射のスペルがRefrectivenessになっていた
		weaponBulletReflectDamageRatio[id] = getDoubleProperty("BulletReflectDamageRatio",0.0);
		weaponBulletOffSet[id] = getIntProperty_saftyMin(new String[]{"BulletOffSet","BulletSetOff"},0,0,true); //beta8.0対応、相殺のスペルがSetOffになっていた
		weaponBulletPenetration[id] = getIntProperty_saftyMin(new String[]{"BulletPenetration","BulletPenetrate"},1,1,true);
		weaponBulletPreventMultiPenetration.set(id,getBooleanProperty("BulletPreventMultiPenetration",false));
		weaponLimitRange[id] = getIntProperty_saftyMin(new String[]{"LimitRange","Range"},MAX,0,true); //beta8.0対応、制限距離というより射程という概念であったためRangeになっていた
		final String firePointStr = getStringProperty("FirePoint","self");
		if(firePointStr.equalsIgnoreCase("self") || !BreakScope.isActualString(firePointStr)) //自分(デフォルト)
			weaponFirePoint[id] = BreakScope.SELF;
		else if(firePointStr.equalsIgnoreCase("target")) //最も近い敵
			weaponFirePoint[id] = BreakScope.TARGET;
		else if(firePointStr.equalsIgnoreCase("focus")) //照準
			weaponFirePoint[id] = BreakScope.FOCUS;
		else{
			BreakScope.messageBox("FirePoint「" + firePointStr + "」は該当するものがありません。\n場所：" + errorSource,"config構成エラー");
			weaponFirePoint[id] = BreakScope.SELF;
		}
		//発射地点指定-第１フェイズ-XY分離指定-~ver8.9
		//X座標、Y座標が別々の行で書き込まれる
		final int[]
			firePointsX_tmp = new int[W_K_L], //発射地点の指定数上限は256個まで
			firePointsY_tmp = new int[W_K_L];
		int firePointsAmount = 0; //発射地点指定数
		final String[] 
			firePointsX_str = BreakScope.split2(getStringProperty("firePointsX","0"),","),
			firePointsY_str = BreakScope.split2(getStringProperty("firePointsY","0"),",");
		if(BreakScope.isActualString(firePointsX_str) && BreakScope.isActualString(firePointsY_str)){
			firePointsAmount += max(firePointsX_str.length,firePointsY_str.length);
			for(int k = 0;k < firePointsAmount;k++){ //XとYで長さが違った時、足りない方を0で埋める
				firePointsX_tmp[k] = k < firePointsX_str.length ? convertInt(firePointsX_str[k],0) : 0;
				firePointsY_tmp[k] = k < firePointsY_str.length ? convertInt(firePointsY_str[k],0) : 0;
			}
		}
		//発射地点指定-第２フェイズ-XY統合指定-ver9.0
		//X座標とY座標が交互に同一行で書き込まれる
		//項目名はfirePointsXY
		final String
			firePointsXY_rawStr = getStringProperty("firePointsXY","NONE"); 
		//何も座標指定がなかったとき、座標(0,0)がデフォルトとなるが、既に上の処理でデフォルト値が代入されているため、こちらと極座標側では何もしない
		final String[]
			firePointsXY_str = BreakScope.split2(firePointsXY_rawStr,",");
		if(BreakScope.isActualString(firePointsXY_str)){
			if(firePointsXY_str.length % 2 != 0)
				messageTime += alertWrongStyle("firePointsXY",firePointsXY_rawStr);
			else{
				final int limit = firePointsXY_str.length/2;
				for(int k = 0;k < limit;k++){
					firePointsX_tmp[k + firePointsAmount] = convertInt(firePointsXY_str[k*2].replaceAll("[^0-9]",""),0);
					firePointsY_tmp[k + firePointsAmount] = convertInt(firePointsXY_str[k*2 + 1].replaceAll("[^0-9]",""),0);
				}
				firePointsAmount += firePointsXY_str.length;
			}
		}
		//発射地点指定-第３フェイズ-極座標指定-ver9.0
		//距離と角度が交互に同一行で書き込まれる
		//項目名はfirePointsRT
		final String
			firePointsRT_rawStr = getStringProperty("firePointsRT","NONE");
		final String[]
			firePointsRT_str = BreakScope.split2(firePointsRT_rawStr,",");
		if(BreakScope.isActualString(firePointsRT_str)){
			if(firePointsRT_str.length % 2 != 0)
				messageTime += alertWrongStyle("firePointsRT",firePointsRT_rawStr);
			else{
				final int limit = firePointsRT_str.length/2;
				for(int k = 0;k < limit;k++){
					final double length = convertDouble(firePointsRT_str[k*2].replaceAll("[^0-9]",""),0.0);
					final double radian = convertDouble(firePointsRT_str[k*2 + 1].replaceAll("[^0-9]",""),0.0);
					firePointsX_tmp[k + firePointsAmount] = (int)(length*cos(radian));
					firePointsY_tmp[k + firePointsAmount] = (int)(length*sin(radian));
				}
				firePointsAmount += firePointsRT_str.length;
			}
		}
		//第４フェイズ-配列長を調整する
		weaponFirePointsX[id] = Arrays.copyOf(firePointsX_tmp,firePointsAmount);
		weaponFirePointsY[id] = Arrays.copyOf(firePointsY_tmp,firePointsAmount);
		//使用弾薬種解析
		final String ammoKind = getStringProperty("AmmoKind","infinity"); //使用弾薬の読み込み
		if(ammoKind.equalsIgnoreCase("handgun") || ammoKind.equals("1"))
			weaponAmmoKind[id] = 1;
		else if(ammoKind.equalsIgnoreCase("shotgun") || ammoKind.equals("2"))
			weaponAmmoKind[id] = 2;
		else if(ammoKind.equalsIgnoreCase("machinegun") || ammoKind.equals("3"))
			weaponAmmoKind[id] = 3;
		else if(ammoKind.equalsIgnoreCase("assaultrifle") || ammoKind.equals("4"))
			weaponAmmoKind[id] = 4;
		else if(ammoKind.equalsIgnoreCase("sniper") || ammoKind.equals("5"))
			weaponAmmoKind[id] = 5;
		else if(ammoKind.equalsIgnoreCase("grenade") || ammoKind.equals("6"))
			weaponAmmoKind[id] = 6;
		else if(ammoKind.equalsIgnoreCase("rocket") || ammoKind.equals("7"))
			weaponAmmoKind[id] = 7;
		else if(ammoKind.equalsIgnoreCase("battery") || ammoKind.equals("8"))
			weaponAmmoKind[id] = 8;
		else if(ammoKind.equalsIgnoreCase("inf") || ammoKind.equals("infinity") || ammoKind.equalsIgnoreCase("NONE") || ammoKind.equals("9") || ammoKind.equals("-1"))
			weaponAmmoKind[id] = 9;
		else{
			messageTime += BreakScope.messageBox("AmmoKind「" + ammoKind + "」は該当するものがありません。\n場所：" + errorSource,"config構成エラー");
			weaponAmmoKind[id] = 9;
		}
		weaponMagazineSize[id] = getIntProperty("MagazineSize",MAX);
		weaponAutoReload.set(id,getBooleanProperty("AutoReload",false));
		weaponReloadTime[id] = getIntProperty_saftyMin(new String[]{"ReloadTime","ReloadLength"},1,1,false); //beta8.0対応,項目名がReloadLengthになっていた
																									   //リロード時間の0秒は暗黙に1秒に変換してくれる
		if(weaponReloadTime[id] < 1)
			weaponReloadTime[id] = 1;
		final String loopStr = getStringProperty("GunLoop","NONE");
		if(BreakScope.isActualString(loopStr)){
			final String[] data = BreakScope.split2(loopStr,",");
			try{
				weaponGunLoop[id] = Integer.parseInt(data[0]); //重複数(int)
				weaponGunLoopDelay[id] = Integer.parseInt(data[1]); //重複間隔(int)
			}catch(Exception e){
				messageTime += alertWrongStyle("GunLoop",loopStr);
				weaponGunLoop[id] = 0;
			}
		}else
			weaponGunLoop[id] = 0;
		final String chainStr = getStringProperty("GunChain","NONE");
		if(BreakScope.isActualString(chainStr)){
			final String[] data = BreakScope.split2(chainStr,",");
			try{
				weaponGunChain_BackUp[id] = data[0]; //連鎖武器名(String)
				weaponGunChainDelay[id] = Integer.parseInt(data[1]); //連鎖開始時間(int)
			}catch(Exception e){
				messageTime += alertWrongStyle("GunChain",chainStr);
				weaponGunChain_BackUp[id] = "NONE";
			}
		}else
			weaponGunChain_BackUp[id] = "NONE";
		weaponInertiaRate[id] = getDoubleProperty("InertiaRate",0.0);
		//アクションポーズの解析
		final String actionType = getStringProperty("ActionType","handgun");
		if(actionType.equalsIgnoreCase("sword") || actionType.equals("1"))
			weaponActionType[id] = 1;
		else if(actionType.equalsIgnoreCase("handgun") || actionType.equals("2"))
			weaponActionType[id] = 2;
		else if(actionType.equalsIgnoreCase("shotgun") || actionType.equals("3"))
			weaponActionType[id] = 3;
		else if(actionType.equalsIgnoreCase("rpg") || actionType.equals("4"))
			weaponActionType[id] = 4;
		else if(actionType.equalsIgnoreCase("long_gun") || actionType.equals("5"))
			weaponActionType[id] = 5;
		else if(actionType.equalsIgnoreCase("chainsaw") || actionType.equals("6"))
			weaponActionType[id] = 6;
		else{
			if(!BreakScope.isActualString(actionType))
				messageTime += BreakScope.messageBox("ActionType「" + actionType + "」は該当するものがありません。\n場所：" + errorSource,"config構成エラー");
			weaponActionType[id] = 2;
		}
		final String imgURL = getStringProperty("Img","NONE");
		weaponImg[id] = this.unifyURL(imgURL,"picture",versionName);
		weaponIconImg[id] = this.unifyURL(getStringProperty("IconImg",imgURL),"picture",versionName);
		weaponSE[id] = this.unifyURL(getStringProperty("SE","NONE"),"media",versionName);
		weaponSEIsSerial.set(id,getBooleanProperty("SEIsSerial",false));
		weaponStrExtend[id] = getDoubleProperty("StrenghExtend",0.0);
		weaponFiRExtend[id] = getDoubleProperty("FireRateExtend",0.0);
		weaponLiRExtend[id] = getDoubleProperty("LimitRangeExtend",0.0);
		weaponBulletSpdExtend[id] = getDoubleProperty("BulletSpeedExtend",0.0);
		weaponBulletPenExtend[id] = getDoubleProperty("PenetrationExtend",0.0);
		weaponBulletRefExtend[id] = getDoubleProperty("RefrectivenessExtend",0.0);
		weaponBulletOfSExtend[id] = getDoubleProperty("OffSetExtend",0.0);
		weaponBulletLiFExtend[id] = getDoubleProperty("LimitFrameExtend",0.0);
	}
	/**
	* 弾のコンフィグを読み込みます。
	*/
	final void readBulletTag(int id,String versionName){
		nameToID_bullet.put(bulletName[id] = getStringProperty("Name",""),id);
		bulletSize[id] = getIntProperty("Size",10);
		bulletHitBurst[id] = getIntProperty("HitBurst",0);
		bulletLimitFrame[id] = getIntProperty(new String[]{"LimitFrame","LifeSpan"},MAX); //beta7.0対応、制限フレームの項目名がLifeSpanになっていた
		bulletStallRatio[id] = getDoubleProperty("StallRatio",1.0);
		bulletWithGun_BackUp[id] = BreakScope.split2(getStringProperty("WithGun","NONE"),",");
		bulletLostGun_BackUp[id] = BreakScope.split2(getStringProperty("LostGun","NONE"),",");
		bulletDestroyGun_BackUp[id] = BreakScope.split2(getStringProperty("DestroyGun","NONE"),",");
		bulletWithEffect_BackUp[id] = BreakScope.split2(getStringProperty("WithEffect","NONE"),",");
		bulletDestroyEffect_BackUp[id] = BreakScope.split2(getStringProperty("DestroyEffect","NONE"),",");
		bulletLostEffect_BackUp[id] = BreakScope.split2(getStringProperty("LostEffect","NONE"),",");
		bulletLaserAction.set(id,getBooleanProperty("LaserAction",false));
		bulletGunnerFollow.set(id,getBooleanProperty("GunnerFollow",false));
		bulletHeatHoming.set(id,getBooleanProperty("HeatHoming",false));
		bulletSuperPenetration.set(id,getBooleanProperty(new String[]{"SuperPenetration","SuperPenetrate"},false));
		bulletRotateSpeed[id] = toRadians(getDoubleProperty("RotateSpeed",0.0));
		bulletRotateStartAngle[id] = BreakScope.toRadians2(getDoubleProperty("RotateStartAngle",NONE));
		bulletRotateRadius[id] = getIntProperty(new String[]{"RotateRadius","RotateRedians"},0); //beta8.0対応、公転半径の項目名がRotateRediansになっていた
		bulletImg[id] = this.unifyURL(getStringProperty("Img","NONE"),"picture",versionName);
	}
	/**
	* コンフィグバージョンに合わせて、このリソースのURLを調整します。
	* 画像、音声、追加コードなどのリソースはブレスコの特定のフォルダ内に統一保存しても動作し、この場所をローカルスペースといいます。
	* config2.0以降、MODとして独立させるためリソースを各コンフィグフォルダ分割する必要ができたため、コンフィグバージョンによってパスの参照元が異なり、
	* このメソッドを使ってsourceフォルダからの統一パスになるよう調整します。
	* 現在のバージョンでの具体的な挙動は以下の通りです。
	* config1.0 ・・・ 指定ローカルフォルダ内を参照する
	* config2.0 ・・・ __config2.0__フォルダ内にファイルがあるか検索し、存在しなければ警告を出して指定ローカルフォルダ内を参照する
	* @param url リソースのURL
	* @param localSpaceURL このリソースのローカルスペースのURL
	* @param versionName コンフィグのバージョン名
	* @return 調整されたURL
	* @since beta9.0
	*/
	final String unifyURL(String url,String localSpaceURL,String versionName){
		if(versionName.equals("1.0"))
			return url;
		else if(versionName.equals("2.0")){
			if(BreakScope.isActualString(url)){
				if(getClass().getResource(nowConfigURL + "/" + url) == null){ //異常-指定ファイルがそのフォルダ内で見つからない
					messageTime += BreakScope.messageBox("「" + url + "」がコンフィグフォルダ内で見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
					return localSpaceURL + "/" + url; //ローカルスペースを参照させる
				}else //正常
					return nowConfigURL + "/" + url;
			}
		}else
			System.out.println("未対応のバージョン「" + versionName + "」が指定されました。");
		return "NONE";
	}
	final void plintARLog(String type,String[] newData,String[] oldData){
		checkAR:
		for(int i = 0;i < newData.length;i++){
			for(int j = 0;j < oldData.length;j++){
				if(newData[i].equals(oldData[j])){
					oldData[j] = "";
					continue checkAR;
				}
			}
			System.out.println(type + "追加：「" + newData[i] + "」");
		}
		for(String ver : oldData){
			if(!ver.isEmpty())
				System.out.println(type + "削除：「" + ver + "」");
		}
	}
	int convertID_enemy(String name,String errorSource){
		if(!BreakScope.isActualString(name))
			return NONE;
		if(nameToID_enemy.containsKey(name)) //指定名をIDに変換
			return nameToID_enemy.get(name);
		//指定名が見つからない
		messageTime += BreakScope.messageBox("enemy[" + name + "]が見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
		return NONE;
	}
	int convertID_entity(String name,String errorSource){
		if(!BreakScope.isActualString(name))
			return NONE;
		if(nameToID_entity.containsKey(name)) //指定名をIDに変換
			return nameToID_entity.get(name);
		//指定名が見つからない
		messageTime += BreakScope.messageBox("entity[" + name + "]が見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
		return NONE;
	}
	int convertID_effect(String name,String errorSource){
		if(!BreakScope.isActualString(name))
			return NONE;
		if(nameToID_effect.containsKey(name)) //指定名をIDに変換
			return nameToID_effect.get(name);
		//指定名が見つからない
		messageTime += BreakScope.messageBox("effect[" + name + "]が見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
		return NONE;
	}
	int convertID_gimmick(String name,String errorSource){
		if(!BreakScope.isActualString(name))
			return NONE;
		if(nameToID_gimmick.containsKey(name)) //指定名をIDに変換
			return nameToID_gimmick.get(name);
		//指定名が見つからない
		messageTime += BreakScope.messageBox("gimmick[" + name + "]が見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
		return NONE;
	}
	int convertID_weapon(String name,String errorSource){
		if(!BreakScope.isActualString(name))
			return NONE;
		if(nameToID_weapon.containsKey(name)) //指定名をIDに変換
			return nameToID_weapon.get(name);
		//指定名が見つからない
		messageTime += BreakScope.messageBox("weapon[" + name + "]が見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
		return NONE;
	}
	int convertID_bullet(String name,String errorSource){
		if(!BreakScope.isActualString(name))
			return NONE;
		if(nameToID_bullet.containsKey(name)) //指定名をIDに変換
			return nameToID_bullet.get(name);
		//指定名が見つからない
		messageTime += BreakScope.messageBox("bullet[" + name + "]が見つかりませんでした。\n場所：" + errorSource,"config構成エラー");
		return NONE;
	}
	/**
	* ２次元配列のバックアップからリアル配列に値を写します。
	* @param TYPE オブジェクト種類
	* @param backup バックアップ配列(int[][])
	* @param real リアル配列(int[][])
	* @param objectNames 名前配列(String[])
	* @param typeName オブジェクト種類名
	* @param propertyName 項目名(エラー表示用)
	*/
	void autoIDConverter_2D(int TYPE,String[][] backup,int[][] real,String[] objectNames,String typeName,String propertyName){
		for(int i = 0;i < backup.length;i++){
			if(!BreakScope.isActualString(backup[i])){ //２次元目の配列が空
				real[i] = new int[0];
			}else{ //２次元目の配列に何かある
				final int length = backup[i].length;
				real[i] = new int[length]; //リアルの方の２次元配列の長さを指定
				final String errorSource = typeName + "「" + objectNames[i] + "」の" + propertyName + "項目";
				int back = 0;
				switch(TYPE){ //実際に値を移す
				case BreakScope.ENEMY:
					for(int j = 0;j < length;j++){
						real[i][j + back] = convertID_enemy(backup[i][j],errorSource);
						if(real[i][j + back] == NONE)
							back--;
					}
					break;
				case BreakScope.ENTITY:
					for(int j = 0;j < length;j++){
						real[i][j] = convertID_entity(backup[i][j],errorSource);
						if(real[i][j + back] == NONE)
							back--;
					}
					break;
				case BreakScope.EFFECT:
					for(int j = 0;j < length;j++){
						real[i][j] = convertID_effect(backup[i][j],errorSource);
						if(real[i][j + back] == NONE)
							back--;
					}
					break;
				case BreakScope.WEAPON:
					for(int j = 0;j < length;j++){
						real[i][j] = convertID_weapon(backup[i][j],errorSource);
						if(real[i][j + back] == NONE)
							back--;
					}
					break;
				case BreakScope.BULLET:
					for(int j = 0;j < length;j++){
						real[i][j] = convertID_bullet(backup[i][j],errorSource);
						if(real[i][j + back] == NONE)
							back--;
					}
					break;
				case BreakScope.GIMMICK:
					for(int j = 0;j < length;j++){
						real[i][j] = convertID_gimmick(backup[i][j],errorSource);
						if(real[i][j + back] == NONE)
							back--;
					}
					break;
				}
				if(back < 0)
					real[i] = Arrays.copyOf(real[i],length + back);
			}
		}
	}
	/**
	* １次元配列のバックアップからリアル配列に値を写します。
	* @param TYPE オブジェクト種類
	* @param backup バックアップ配列(int[])
	* @param real リアル配列(int[])
	* @param objectNames 名前配列(String[])
	* @param typeName オブジェクトグループ名
	* @param propertyName 項目名(エラー表示用)
	*/
	void autoIDConverter_1D(int TYPE,String[] backup,int[] real,String[] objectNames,String typeName,String propertyName){
		switch(TYPE){ //実際に値を移す
		case BreakScope.ENEMY:
			for(int i = 0;i < backup.length;i++)
				real[i] = convertID_enemy(backup[i],typeName + "「" + objectNames[i] + "」の" + propertyName + "項目");
			break;
		case BreakScope.ENTITY:
			for(int i = 0;i < backup.length;i++)
				real[i] = convertID_entity(backup[i],typeName + "「" + objectNames[i] + "」の" + propertyName + "項目");
			break;
		case BreakScope.EFFECT:
			for(int i = 0;i < backup.length;i++)
				real[i] = convertID_effect(backup[i],typeName + "「" + objectNames[i] + "」の" + propertyName + "項目");
			break;
		case BreakScope.WEAPON:
			for(int i = 0;i < backup.length;i++)
				real[i] = convertID_weapon(backup[i],typeName + "「" + objectNames[i] + "」の" + propertyName + "項目");
			break;
		case BreakScope.BULLET:
			for(int i = 0;i < backup.length;i++)
				real[i] = convertID_bullet(backup[i],typeName + "「" + objectNames[i] + "」の" + propertyName + "項目");
			break;
		case BreakScope.GIMMICK:
			for(int i = 0;i < backup.length;i++)
				real[i] = convertID_gimmick(backup[i],typeName + "「" + objectNames[i] + "」の" + propertyName + "項目");
			break;
		}
	}
	boolean blankDetected; //空白のstringがconvert系やgetProperty系のメソッドに渡されたことを示す(継承システムに使用)
	/**
	* 文字列を安全にint値へ変換します。
	*/
	int convertInt(String str,int defaultValue){
		str = BreakScope.trim2(str);
		if(str == null || str.isEmpty()){
			blankDetected = true;
			return defaultValue;
		}
		blankDetected = false;
		try{ //通常-実数値
			return Integer.parseInt(str);
		}catch(NumberFormatException e){ //数字以外の特殊値または入力ミス
			if(str.equalsIgnoreCase("MAX")) //正常-特殊値MAX
				return MAX;
			if(str.equalsIgnoreCase("MIN")) //正常-特殊値MIN
				return MIN;
			if(str.equalsIgnoreCase("NONE")) //正常-特殊値NONE
				return NONE;
			//異常-変換失敗
			messageTime += BreakScope.messageBox("数字「" + str + "」は不適格です、絶対値が2,147,483,647より小さい半角数字を入力してください。\n場所：" + errorSource,"config構成エラー");
			return defaultValue;
		}
	}
	/**
	* 文字列配列を安全にint値配列へ変換します。
	*/
	int[] convertInt(String[] strs,int defaultValue){
		if(strs == null || strs.length == 0){
			blankDetected = true;
			return new int[]{defaultValue};
		}
		final int[] results = new int[strs.length];
		for(int i = 0;i < strs.length;i++)
			results[i] = this.convertInt(strs[i],defaultValue);
		blankDetected = false; //ここに書かないと、末尾の要素のStringが空白である時、上のconvertIntによってtrueにされる
		return results;
	}
	/**
	* 文字列を安全にdouble値へ変換します。
	*/
	double convertDouble(String str,double defaultValue){
		if(str == null || str.isEmpty()){
			blankDetected = true;
			return defaultValue;
		}
		blankDetected = false;
		str = BreakScope.trim2(str);
		try{ //通常-実数値
			return Double.parseDouble(str);
		}catch(NumberFormatException e){ //数字以外の特殊文字または入力ミス
			if(str.equalsIgnoreCase("MAX")) //正常-特殊値MAX
				return MAX;
			if(str.equalsIgnoreCase("MIN")) //正常-特殊値MIN
				return MIN;
			if(str.equalsIgnoreCase("NONE")) //正常-特殊値NONE
				return NONE;
			messageTime += BreakScope.messageBox("数字「" + str + "」は不適格です、300桁以内の半角小数を入力してください。\n場所：" + errorSource,"config構成エラー");
			return defaultValue;
		}
	}
	/**
	* 文字列配列を安全にdouble値配列へ変換します。
	*/
	double[] convertDouble(String[] strs,double defaultValue){
		if(strs == null || strs.length == 0){
			blankDetected = true;
			return new double[]{defaultValue};
		}
		final double[] results = new double[strs.length];
		for(int i = 0;i < strs.length;i++)
			results[i] = this.convertDouble(strs[i],defaultValue);
		blankDetected = false; //ここに書かないと、末尾の要素のStringが空白である時、上のconvertDoubleによってtrueにされる
		return results;
	}
	/**
	* 文字列配列を安全にboolean値配列へ変換します。
	*/
	boolean convertBoolean(String str,boolean defaultValue){
		if(str == null || str.isEmpty()){
			blankDetected = true;
			return defaultValue;
		}
		blankDetected = false;
		str = BreakScope.trim2(str);
		if(str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on"))
			return true;
		if(str.equalsIgnoreCase("false") || str.equalsIgnoreCase("off"))
			return false;
		//入力ミス
		messageTime += BreakScope.messageBox("ブール値「" + str + "」は不適格です、{true,on,false,off}のどれかにしてください。\n場所：" + errorSource,"config構成エラー");
		return defaultValue;
	}
	boolean[] convertBoolean(String[] strs,boolean defaultValue){
		if(strs == null || strs.length == 0){
			blankDetected = true;
			return new boolean[]{defaultValue};
		}
		final boolean[] results = new boolean[strs.length];
		for(int i = 0;i < strs.length;i++)
			results[i] = this.convertBoolean(strs[i],defaultValue);
		blankDetected = false; //ここに書かないと、末尾の要素のStringが空白である時、上のconvertBooleanによってtrueにされる
		return results;
	}
	int getIntProperty(String propertyName,int defaultValue){
		for(int i = 0;i < propertyTotal;i++){
			if(propertyN[i].equalsIgnoreCase(propertyName)){
				propertyIsUsed[i] = true;
				return this.convertInt(propertyV[i],defaultValue);
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	int getIntProperty(String[] propertyNames,int defaultValue){
		for(String name : propertyNames){
			for(int i = 0;i < propertyTotal;i++){
				if(propertyN[i].equalsIgnoreCase(name)){
					propertyIsUsed[i] = true;
					return this.convertInt(propertyV[i],defaultValue);
				}
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	double getDoubleProperty(String propertyName,double defaultValue){
		for(int i = 0;i < propertyTotal;i++){
			if(propertyN[i].equalsIgnoreCase(propertyName)){
				propertyIsUsed[i] = true;
				return this.convertDouble(propertyV[i],defaultValue);
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	double getDoubleProperty(String[] propertyNames,double defaultValue){
		for(String name : propertyNames){
			for(int i = 0;i < propertyTotal;i++){
				if(propertyN[i].equalsIgnoreCase(name)){
					propertyIsUsed[i] = true;
					return this.convertDouble(propertyV[i],defaultValue);
				}
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	Boolean getBooleanProperty(String propertyName,boolean defaultValue){
		for(int i = 0;i < propertyTotal;i++){
			if(propertyN[i].equalsIgnoreCase(propertyName)){
				propertyIsUsed[i] = true;
				return this.convertBoolean(propertyV[i],defaultValue);
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	Boolean getBooleanProperty(String[] propertyNames,boolean defaultValue){
		for(String name : propertyNames){
			for(int i = 0;i < propertyTotal;i++){
				if(propertyN[i].equalsIgnoreCase(name)){
					propertyIsUsed[i] = true;
					return this.convertBoolean(propertyV[i],defaultValue);
				}
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	String getStringProperty(String propertyName,String defaultValue){
		for(int i = 0;i < propertyTotal;i++){
			if(propertyN[i].equalsIgnoreCase(propertyName)){
				propertyIsUsed[i] = true;
				final String value = propertyV[i];
				if(value == null || value.isEmpty())
					blankDetected = true;
				else
					blankDetected = false;
				return BreakScope.trim2(value);
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	String getStringProperty(String[] propertyNames,String defaultValue){
		for(String name : propertyNames){
			for(int i = 0;i < propertyTotal;i++){
				if(propertyN[i].equalsIgnoreCase(name)){
					propertyIsUsed[i] = true;
					final String value = propertyV[i];
					if(value == null || value.isEmpty())
						blankDetected = true;
					else
						blankDetected = false;
					return BreakScope.trim2(value);
				}
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	/**
	* 安全にプロパティ値を最小値以上の値で読み取り、かつメッセージウィンドウによる警告を行います。
	*/
	private final int getIntProperty_saftyMin(String propertyName,int defaultValue,int min,boolean needWarn){
		for(int i = 0;i < propertyTotal;i++){
			if(propertyN[i].equalsIgnoreCase(propertyName)){
				propertyIsUsed[i] = true;
				final int value = this.convertInt(propertyV[i],defaultValue);
				if(value < min){ //minより小さい場合は警告とともに修正
					if(needWarn)
						messageTime += BreakScope.messageBox(propertyName + "の値「" + propertyV[i] + "」は" + min + "以上にしてください。\n場所：" + errorSource,"config構成エラー");
					return min;
				}else
					return value;
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	/**
	* 安全にプロパティ値を最小値以上の値で読み取り、かつメッセージウィンドウによる警告を行います。
	*/
	private final int getIntProperty_saftyMin(String[] propertyNames,int defaultValue,int min,boolean needWarn){
		for(String name : propertyNames){
			for(int i = 0;i < propertyTotal;i++){
				if(propertyN[i].equalsIgnoreCase(name)){
					propertyIsUsed[i] = true;
					final int value = this.convertInt(propertyV[i],defaultValue);
					if(value < min){ //minより小さい場合は警告とともに修正
						if(needWarn)
							messageTime += BreakScope.messageBox(name + "の値「" + propertyV[i] + "」は" + min + "以上にしてください。\n場所：" + errorSource,"config構成エラー");
						return min;
					}else
						return value;
				}
			}
		}
		blankDetected = true;
		return defaultValue;
	}
	/**
	* 形式が不適切であることをメッセージウィンドウにより警告します。
	*/
	private final long alertWrongStyle(String propertyName,String wrongStr){
		return BreakScope.messageBox(propertyName + "「" + wrongStr + "」は形式が不適切です。\n場所：" + errorSource,"config構成エラー");
	}
}