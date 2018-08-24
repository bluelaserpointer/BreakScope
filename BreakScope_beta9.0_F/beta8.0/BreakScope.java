/**
 * @author  bluelaserpointer
 * @version 9.0
 
 ブレイクスコープ-マルチコンバットJavadoc
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import static java.awt.event.KeyEvent.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.text.*;
import static java.lang.Math.*;

public class BreakScope extends JPanel implements MouseListener,MouseMotionListener,MouseWheelListener,KeyListener,Runnable{
	//重要定数
	final static String NOW_VERSION = "1.9.0";
	final static int WEAPON_KIND_LIMIT = 512; //武器制作上限数(現在、敵や弾など他のオブジェクト制作上限もこれに追随)
	
	final JFrame myFrame;
	Config cfg; //configシステムによって設定された、様々な規定値の統合データ
	final MediaTracker tracker;
	final Random rnd = new Random();
	
	//パス
	final static String CFG_URL = "source/configPackage/configTextData.txt";
	
	//汎用変数群
	final static int TITLE = -50,OPENING = -11,BATTLE = -12,PAUSE_MENU = -13,STAGE_SELECT = -14,EQUIPMENT_SELECT = -15,SHOP = -16,OPTION = -17,GUIDE = -18,VOLUME_CONTROLER = -19,VOLUME_LINE = -20,RANKING = -21,
		MULTIPLAY_RINKROOM = -22,MULTIPLAY_MAKEROOM = -23,MULTIPLAY_TEAMWAITING = -24,
		shopUpgrade1 = 31,shopUpgrade2 = 32,shopUpgrade3 = 33,shopUpgrade4 = 34,
		SCREEN_SIZE1 = 81,SCREEN_SIZE2 = 82,SCREEN_SIZE3 = 83,NEXT = -51,PREV = -52,NEXT2 = -53,PREV2 = -54; //イベント定数
	final static int NONE = -999999999, //値が存在しない
		MAX = Integer.MAX_VALUE, //無限、値が存在しない(特殊)
		MIN = Integer.MIN_VALUE; //無限小
	final static int TYPE_SEPARATOR = 1000000; //オブジェクト種類を分ける数値
	//final int INT_AMPLIFIER = 5; //疑似小数値を整数値に実現する絶対値増幅ビット数(double演算回避用)
	final static int PLAYER = TYPE_SEPARATOR*1,ENEMY = TYPE_SEPARATOR*2,ENTITY = TYPE_SEPARATOR*3,GIMMICK = TYPE_SEPARATOR*4,BULLET = TYPE_SEPARATOR*5,EFFECT = TYPE_SEPARATOR*6; //物体種類定数
	final static int WEAPON = TYPE_SEPARATOR*7;
	final static int SELF = MAX-1,REVERSE_SELF = MAX-2,TARGET = MAX-3,REVERSE_TARGET = MAX-4,FOCUS = MAX-5, //弾・武器関連定数
		MOVE = MAX-6,STAND = MAX-7,AUTO_ATTACK = MAX-8,AUTO_CURE = MAX-9,STAGE_START = MAX-10;
	int screenW = 1000,screenH = 600;
	int focusing = NONE; //マウスのポイント先,NONEのとき何も指していない
	final int defaultScreenW = 1000,defaultScreenH = 600; //ウィンドウ縦横既定値
	int nowEvent; //イベントモード
	final ArrayDeque<Integer> oldEvents = new ArrayDeque<Integer>(); //前回のイベントモード動的配列(戻るボタンなどで使用)
	int page,page_min,page_max; //現在のページ数(ページ処理が必要なときに使う),最少ページ数,最大ページ数
	double pagePower; //ページ送り加速度
	int gameTime,gameOverFrame,clearFrame; //ゲーム経過フレーム,ゲームオーバー時フレーム,クリア時フレーム
	int nowFrame; //現在フレーム
	long nowTime; //現在時刻ミリ秒 ※他の時間に関するほとんどの変数はフレーム数で計測している
	
	String playerComment;
	long playerCommentFrame;
	int textZone;
	
	//敵の配列群
	final int FROG = 0,OGRE = 1,GUARD = 2,MACHINE_GUNNER = 3,TANK = 4,SNIPER = 5,SHOTGUNMAN = 6,
		B_SHESS = 7;
	final int enemyHPData[] ={200,600,150,150,1250,150,1000,18000,0,0,0}; //HP情報
	final int enemyScoreData[] ={200,500,100,300,1000,250,2500,20000,0,0,0}; //撃破スコア情報
	final int[] enemyKind = new int[1024]; //種類
	final double[] enemyX = new double[1024],enemyY = new double[1024], //座標
		enemyOldX = new double[1024],enemyOldY = new double[1024], //旧座標
		enemyXPower = new double[1024],enemyYPower = new double[1024], //動力
		enemyAngle = new double[1024],enemyTargetAngle = new double[1024],enemyNextAngle = new double[1024], //視野角度、目標角度、第２目標角度
		enemyTargetX = new double[1024],enemyTargetY = new double[1024], //目標座標
		enemyNextTargetX = new double[1024],enemyNextTargetY = new double[1024]; //第２目標座標
	final int[] enemyShotFrame = new int[1024]; //射撃/行動値(一定値まで増え続けて0から循環、攻撃のパターンを編成するのに使用)
	final BitSet enemyFoundMe = new BitSet(1024); //プレイヤー発見状態
	final int[] enemySightCuttingGrid = new int[1024]; //現在目標との視線が切られたグリッド地点(高速化に使用)
	final int[] enemyHP = new int[1024],enemyWound = new int[1024]; //現有HPとスタン時間
	Image[] enemyImg,enemyIconImg; //画像,アイコン画像
	int enemy_maxID; //最大ID番号
	int enemy_total; //総数
	
	//エンティティの配列群
	final int[] entityKind = new int[1024]; //種類値
	final int[] entityHP = new int[1024]; //耐久値
	final double[] entityX = new double[1024],entityY = new double[1024]; //座標
	final double[] entityOldX = new double[1024],entityOldY = new double[1024]; //旧座標
	final double[] entityXPower = new double[1024],entityYPower = new double[1024]; //動力
	final double[] entityAngle = new double[1024];
	final int[] entityTeam = new int[1024];
	Image[] entityImg,entityIconImg; //画像,アイコン画像
	int entity_maxID; //最大ID番号
	int entity_total; //総数

	//エフェクトの配列群
	final int[] effectKind = new int[2048], //種類値
		effectX = new int[2048],effectY = new int[2048], //x座標,y座標
		effectSize = new int[2048], //大きさ
		effectApparFrame = new int[2048], //出現フレーム
		effectXPower = new int[2048],effectYPower = new int[2048]; //x速度,y速度
	final double[] effectAlpha = new double[2048], //透過度
		effectAlphaPower = new double[2048]; //透過度変動値
	final static int EXPLOSION1 = 0,EXPLOSION2 = 1,SPARK_RED = 2,ACID_DUST = 3,SPARK = 4,SWORD_SLASH = 5,SMOKE = 6,BIG_EXPLOSION = 7,ROCK = 8;
	Image[][] effectImg; //画像
	int effect_maxID; //最大のID番号
	int effect_total; //総数

	//弾の配列群
	final int FMJ = 0,SHOT = 1,ROCKET = 2,RIFLE = 3,BLACK_BULLET = 4,ACID = 5,ARTILLERY = 6,EXPLOSION = 7,SLASH = 8,EP_1 = 9,EP_2 = 10;
	final int[] bulletKind = new int[2048]; //種類
	final double[] bulletX = new double[2048],bulletY = new double[2048]; //座標
	final double[] bulletOldX = new double[2048],bulletOldY = new double[2048]; //旧座標
	final int[] bulletAppearFrame = new int[2048],bulletLimitFrame = new int[2048]; //出現フレーム・制限フレーム
	final int[] bulletMovedDistance = new int[2048],bulletLimitMove = new int[2048]; //移動距離・制限距離
	final double[] bulletXPower = new double[2048],bulletYPower = new double[2048]; //動力
	final double[] bulletXAccel = new double[2048],bulletYAccel = new double[2048]; //加速度
	final double[] bulletAngle = new double[2048]; //角度
	final double[] bulletRotateAngle = new double[2048]; //公転・自転角度
	final int[] bulletStr = new int[2048], //現有威力
		bulletOffSet = new int[2048], //現有相殺力
		bulletPenetration = new int[2048], //現有貫通力
		bulletReflectiveness = new int[2048]; //残り跳弾回数
	final double[] bulletReflectDamageRatio = new double[2048]; //現有跳弾ダメージ率(削除予定)
	final int[] bulletFollowTarget = new int[2048]; //追随目標の複合情報、５桁目が種別値で下４桁がID、NONEで追随処理なし
	final int[] bulletKnockBack = new int[2048]; //ノックバック力
	final BitSet bulletLaserAction = new BitSet(2048); //レーザー処理、弾が消滅するまで移動・描画を繰り返す
	final int[] bulletTeam = new int[2048]; //所属チーム,非攻撃対象チーム プレイヤー:PLAYER 敵:ENEMY 無所属:NONE
	Image[] bulletImg; //画像
	int bullet_maxID; //最大ID番号
	int bullet_total; //総数
	
	//設置物/ギミックの配列
	int[] gimmickKind, //種類値
		gimmickHP; //耐久値
	Image[] gimmickImg,gimmickIconImg; //画像,アイコン画像
	int gimmick_total; //総数
	int xPatrolTargetMap[]; //巡回点巡回先x移動量
	int yPatrolTargetMap[]; //巡回点巡回先y移動量
	int patrolDelayMap[]; //巡回を始めるまでの駐屯時間(実装中)
	int xForceMap[]; //x動力がかかった空間(ベルトコンベア、風などで影響)
	int yForceMap[]; //y動力がかかった空間
	int damageMap[]; //ダメージを受ける空間
	
	//アイテムの配列群
	final int RECOVERY_PACK = 0,AMMO_HANDGUN = 1,AMMO_SHOTGUN = 2,AMMO_MACHINEGUN = 3,AMMO_ASSAULT_RIFLE = 4,AMMO_SNIPER = 5,
		AMMO_GRENADE = 6,AMMO_ROCKET = 7,AMMO_BATTERY = 8,INFINITY_POWER = 9;
	final int[] itemKind = new int[512], //種類値
		itemAmount = new int[512], //質、数量
		itemX = new int[512],itemY = new int[512]; //x座標,y座標
	final String[] itemName ={"治療薬","拳銃弾","ショットガン弾","マシンガン弾","アサルトライフル弾","スナイパー弾","手榴弾","ロケット弾","バッテリー"};
	final int[] itemPakageSizeData ={5,20,15,50,30,10,10,5,500};
	final int[] itemPossession = new int[10]; //それぞれのアイテムの所持量
	int ammoHave; //現在持っている武器の弾薬量
	final Image[] itemImg = new Image[50]; //画像
	int item_maxID; //最大ID番号
	int item_total; //総数
		
	//プレイヤー情報
	int playerLife = 2; //残機
	int hp_max = 75; //HP上限
	int playerHP = hp_max,playerTargetHP = hp_max; //表示HPと移行中のHP
	int cureStore = 100; //自動回復エネルギー量
	final int cureStore_default = 100; //初期自動回復エネルギー所持量
	int cureRate = 10; //自動回復速度
	final int playerSize = 40; //大きさ
	int playerSpeed = 11; //移動速度
	int  playerX,playerY; //座標
	int playerOldX,playerOldY; //旧座標
	double playerXPower,playerYPower; //加速度
	double playerAngle,cos_playerAngle,sin_playerAngle; //角度,正弦・余弦定数
	int playerHPChangedFrame; //HPが変動したフレーム
	int respawnFrame; //リスタートしたフレーム
	int playerBarrierTime; //バリア時間
	final int playerBarrierTime_default = 400; //初期バリア時間
	int damageTaken; //被ダメージ
	int mainScore,maxScore,money; //スコアと所持金額
	int scoreGainedFrame; //スコアが変動したフレーム
	int playerLuminance; //受けている光(一定値以下であれば見つからない)
	int playerRidingID; //乗っているエンティティ
	int playerActionProgress; //動作進行度(看板を読む,箱を開けるなど)
	int playerActionTarget = NONE; //動作進行対象(複合情報)
	double nearestEnemyAngle = NONE,nearestEnemyDistance = MAX; //一番近い敵の角度と距離
	static final int OVER_X = 1300,OVER_Y = 1300; //読み込み距離
	int weaponChangedFrame; //武器切り替え時のフレーム
	
	//武器情報
	final int SWORD = 1,HANDGUN = 2,SHOTGUN = 3,RPG = 4,LONG_GUN = 5,CHAIN_SAW = 6; //武器アクションスタイル定数
	int weaponSlot_max = 6; //武器装備上限
	final int weaponSlot[] = new int[6];
	int slot = 1; //現在選択中の武器スロット番号
	int weapon = NONE; //現在選択中の武器ID
	int[] weaponRemaining = new int[ConfigLoader.W_K_L]; //弾倉内残弾
	final int[] shotFrame = new int[weaponSlot_max], //武器の発砲したフレーム
		reloadTime = new int[weaponSlot_max],reloadedFrame = new int[weaponSlot_max]; //リロード時間経過、リロード完了フレーム(ラストマガジン警告用)
	Image[] weaponIconImg;
	SoundClip[] weaponSE;
	
	//装備の有無とショップ
	boolean DIRECTION_RADAR = true,HP_RADAR = true,SUSPECT_RADAR = true;
	boolean no_bodylock;
	int shopPage = 2;
	boolean actionSwitch = true,upKey,leftKey,downKey,rightKey,bulletSwitch;
	int[] weaponArsenal; //倉庫にある武器ID	
	int weaponStockTotal; //倉庫にある武器数
	
	//共通情報
	//アタックプラン - 遅延をおいて連続攻撃するオブジェクトの記録
	final int attackPlanID[] = new int[2028]; //攻撃元ID
	final int attackPlanWeapon[] = new int[2028]; //使用武器ID
	final int attackPlanFrame[] = new int[2028]; //発動フレーム
	final BitSet attackPlanIsStarter = new BitSet(2028); //発動フレーム
	
	//ランキング
	int ranking;
	final String[] rankingName = new String[15],rankingDate = new String[15];
	final int[] rankingScore = new int[15],rankingTime = new int[15];
	final String[] rankingStage = new String[15];
	
	//フリーズスイッチ 特定オブジェクトグループの処理を止める(※ノックバック・ダメージなどは蓄積され、解放された瞬間処理が起こる)(開発中)
	boolean enemyFreezeSwitch, //敵処理
		entityFreezeSwitch, //エンティティ処理
		gimmickFreezeSwitch, //ギミック処理
		bulletFreezeSwitch, //弾処理
		effectFreezeSwitch; //エフェクト処理
	//スローデバフ 移動速度、攻撃速度が落ちる(開発中)
	double[] enemySlowRate,
		entitySlowRate,
		gimmickSlowRate,
		bulletSlowRate,
		effectSlowRate;
	
	//処理速度向上系
	int gimmickSweeper[]; //周囲3x3マスに存在するブロック数(衝突判定があるギミック数)を表したグリッド情報
	BitSet enemyMap; //敵が存在するグリッドの情報
	
	//画像
	//一部はconfig読み込み時に配列の長さを決定
	final Image playerImg[][] = new Image[50][],
		playerChangeImg[] = new Image[2],
		background[] = new Image[10],
		guidePageImgs[][] = new Image[5][10],
		barrierImg,
		_gameOver_,
		_clear_,
		key_space,
		focusImg,noAmmoFocus,focusImg2,radarImg,
		statusTrayImg,
		fireEffect[] = new Image[3],
		titleImg,titleBackImg,rogoImg,bigFocus, //タイトル画面
		noWeaponTile,weaponTile,
		messegeBox,
		button_start_,button_guide_,button_option_,button_ranking_,basicButton,button_returnGame_,button_shop_,button_title_,button_return_,
		button_ready_,buttonPrev,buttonNext,
		button_singleMode_,button_multiMode_,
		activeButton_start,activeButton_guide,activeButton_option,
		checkedSymbol,
		upgradeButton,
		/*_result_,_killCounted_,_damageCounted_,_timePassed_,_rank_,*/_ranking_,
		lightImg,
		nullImg;
	//重複画像・重複音声対策配列群
	Image[] arrayImage = new Image[128];
	String[] arrayImageURL = new String[128];
	int arrayImage_maxID = -1;
	SoundClip[] arrayMedia = new SoundClip[128];
	String[] arrayMediaURL = new String[128];
	int arrayMedia_maxID = -1;

	//サウンド
	final int bgm_max = 2;
	final SoundClip titleBGM,
		battleBGM[] = new SoundClip[bgm_max],playerDeathSE,getAmmoSE,reloadSE,
		enemyDeathSE,gunChangeSE,ricochetSE,punchSE;
	double musicVolume = 0.8;
		
	String[] stageName = new String[0]; //ステージの名前リスト
	int stage; //現在ステージ
	int stageW,stageH; //ステージ縦横長さ(ピクセル長さ)
	int stageGridW,stageGridH; //ステージ縦横長さ(マップ長さ)
	int stageGridTotal; //ステージマップ長さ縦横積(ギミック総数)
	int stagePage; //ステージ選択画面でのスクロール位置記憶
	int[] spawnPointX,spawnPointY; //スタート地点
	float stageLuminance; //ステージ全体の明るさ
	
	//マルチプレイ情報
	boolean singlePlayMode = false;
	private BufferedWriter multiCombat_BW; //マルチプレイデータ出力用Writer
	int multiCombat_playerID; //マルチプレイの自分のID
	String multiCombat_playerName; //マルチプレイの自分の名前
	String roomName; //現在ルーム名
	
	//光
	final int[] lightX = new int[2048],lightY = new int[2048],
		lightness = new int[2048];
	int lightTotal;
	
	//デバッグ用
	long loadTime_enemy,loadTime_entity,loadTime_effect,loadTime_bullet,loadTime_weapon,loadTime_gimmick,loadTime_item,loadTime_other,loadTime_total;
	
	public static void main(String args[]){
		new BreakScope(true); //ゲーム実行モード
	}
	public BreakScope(boolean gameActivation){
		oldEvents.add(NONE);
		long loadTime = System.currentTimeMillis();
		long totalLoadTime = System.currentTimeMillis();

		//ウィンドウの立ち上げ
		if(gameActivation){
			myFrame = new JFrame("BreakScope");
			eventChange(OPENING);
		}else{
			myFrame = new JFrame("BreakScope-ステージエディタ");
			eventChange(0);
		}
		myFrame.add(this,BorderLayout.CENTER); //キャンバスを設置
		myFrame.addWindowListener(new MyWindowAdapter());
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		myFrame.addKeyListener(this);
		myFrame.setBackground(Color.BLACK);
		myFrame.setBounds(120,80,1006,628);
		myFrame.setResizable(false);
		myFrame.setVisible(true);
		
		System.out.println("Window: " + (System.currentTimeMillis() - loadTime));//ロード所要時間表示
		loadTime = System.currentTimeMillis();
		
		//アイコンの読み込み
		if(gameActivation)
			myFrame.setIconImage(new ImageIcon("source\\picture\\icon\\bullet.png").getImage());
		else
			myFrame.setIconImage(new ImageIcon("source\\picture\\icon\\brick.png").getImage());
		//サウンドファイル読み込み
		if(gameActivation){
			titleBGM = loadMedia("media/Crazy_Fire.mp3");
			battleBGM[0] = loadMedia("media/nc92900.mp3");
			battleBGM[1] = loadMedia("media/nc88234.mp3");
			reloadSE = loadMedia("media/reload.wav");
			gunChangeSE = loadMedia("media/sen_ge_gun_kamae03.wav");
			ricochetSE = loadMedia("media/sen_ge_gun_tyou01.wav");
			getAmmoSE = loadMedia("media/sen_ge_yakkyo_nigiru01.wav");
			punchSE = loadMedia("media/punch-high1.wav");
			playerDeathSE = loadMedia("media/Bakana00@11.wav");
			enemyDeathSE = loadMedia("media/Damage01@11.wav");
		}else{
			titleBGM = null;
			reloadSE = null;
			gunChangeSE = null;
			ricochetSE = null;
			getAmmoSE = null;
			punchSE = null;
			playerDeathSE = null;
			enemyDeathSE = null;
		}
		
		System.out.println("SE/BGM: " + (System.currentTimeMillis() - loadTime));//ロード所要時間表示
		loadTime = System.currentTimeMillis();
		
		//フォントの読み込み
		basicFont = createFont("font/upcibi.ttf").deriveFont(Font.BOLD + Font.ITALIC,30.0f);
		commentFont = createFont("font/HGRGM.TTC").deriveFont(Font.PLAIN,15.0f);
		
		System.out.println("Font: " + (System.currentTimeMillis() - loadTime));//ロード所要時間表示
		loadTime = System.currentTimeMillis();
		
		//画像ファイル読み込み
		tracker = new MediaTracker(this);
		//プレイヤー武器別アニメーション
		playerImg[SWORD] = new Image[3]; //剣系
		playerImg[SWORD][0] = loadImage("human0-1.png");
		playerImg[SWORD][1] = loadImage("human0-2.png");
		playerImg[SWORD][2] = loadImage("human0-3.png");
		playerImg[HANDGUN] = new Image[2]; //拳銃系
		playerImg[HANDGUN][0] = loadImage("human1-1.png");
		playerImg[HANDGUN][1] = loadImage("human1-2.png");
		playerImg[SHOTGUN] = new Image[3]; //ショットガン系
		playerImg[SHOTGUN][0] = loadImage("human2-1.png");
		playerImg[SHOTGUN][1] = loadImage("human2-2.png");
		playerImg[SHOTGUN][2] = loadImage("human2-3.png");
		playerImg[RPG] = new Image[1]; //ロケットランチャー系
		playerImg[RPG][0] = loadImage("human3-1.png");
		playerImg[LONG_GUN] = new Image[2]; //長身銃系全般
		playerImg[LONG_GUN][0] = loadImage("human4-1.png");
		playerImg[LONG_GUN][1] = loadImage("human4-2.png");
		playerImg[CHAIN_SAW] = new Image[2]; //チェーンソー
		playerImg[CHAIN_SAW][0] = loadImage("human5-1.png");
		playerImg[CHAIN_SAW][1] = loadImage("human5-2.png");
		playerChangeImg[0] = loadImage("player_change_0.png");
		playerChangeImg[1] = loadImage("player_change_1.png");
		barrierImg = loadImage("FreezeEffect.png");
		itemImg[RECOVERY_PACK] = loadImage("RecoveryPack.png");
		itemImg[AMMO_HANDGUN] = loadImage("HandgunBullet.png");
		itemImg[AMMO_SHOTGUN] = loadImage("Shot.png");
		itemImg[AMMO_MACHINEGUN] = loadImage("gunbelt.png");
		itemImg[AMMO_ASSAULT_RIFLE] = loadImage("AssaultRifleBullet.png");
		itemImg[AMMO_SNIPER] = loadImage("SniperCartridge.png");
		itemImg[AMMO_GRENADE] = loadImage("Grenade.png");
		itemImg[AMMO_ROCKET] = loadImage("cartridge.png");
		itemImg[AMMO_BATTERY] = loadImage("battery.png");
		focusImg = loadImage("focus.png");
		noAmmoFocus = loadImage("NoAmmoFocus.png");
		focusImg2 = loadImage("focus2.png");
		radarImg = loadImage("RedTriangle.png");
		statusTrayImg = loadImage("StatusTray.png");
		background[0] = loadImage("field3.png");
		guidePageImgs[0][0] = loadImage("guide/1_parts1.png"); //遊び方説明のパーツ画像
		guidePageImgs[0][1] = loadImage("guide/1_parts2.png");
		guidePageImgs[0][2] = loadImage("guide/1_parts3.png");
		guidePageImgs[1][0] = loadImage("guide/2_parts1.png");
		guidePageImgs[1][1] = loadImage("guide/2_parts2.png");
		guidePageImgs[1][2] = loadImage("guide/2_parts3.png");
		guidePageImgs[1][3] = loadImage("guide/2_parts4.png");
		guidePageImgs[2][0] = loadImage("guide/3_parts1.png");
		guidePageImgs[2][1] = loadImage("guide/3_parts2.png");
		guidePageImgs[2][2] = loadImage("guide/3_parts3.png");
		guidePageImgs[2][3] = loadImage("guide/3_parts4.png");
		guidePageImgs[3][0] = loadImage("guide/4_parts1.png");
		guidePageImgs[3][1] = loadImage("guide/4_parts2.png");
		guidePageImgs[3][2] = loadImage("guide/4_parts3.png");
		guidePageImgs[4][0] = loadImage("guide/5_parts1.png");
		guidePageImgs[4][1] = loadImage("guide/5_parts2.png");
		messegeBox = loadImage("BlueMessegeBox.png");
		_gameOver_ = loadImage("Red_GameOver_.png");
		fireEffect[0] = loadImage("FireEffect1_0.png");
		fireEffect[1] = loadImage("FireEffect1_1.png");
		_clear_ = loadImage("Green_Clear!_.png");
		key_space = loadImage("key_space.png");
		titleImg = loadImage("title.png");
		titleBackImg = loadImage("titleBack.png");
		rogoImg = loadImage("rogo.png");
		bigFocus = loadImage("BigFocus.png");
		noWeaponTile = loadImage("noWeaponTile.png");
		weaponTile = loadImage("WeaponTile.png");
		button_start_ = loadImage("_start_.png");
		button_guide_ = loadImage("_guide_.png");
		button_option_ = loadImage("_option_.png");
		activeButton_start = loadImage("active_start_.png");
		activeButton_guide = loadImage("active_guide_.png");
		activeButton_option = loadImage("active_option_.png");
		basicButton = loadImage("GrayButton.png");
		button_returnGame_ = loadImage("_backGame_.png");
		button_shop_ = loadImage("_shop_.png");
		button_title_ = loadImage("_backTitle_.png");
		button_return_ = loadImage("_back_.png");
		button_ranking_ = loadImage("_toRanking_.png");
		button_ready_ = loadImage("_ready!_.png");
		buttonPrev = loadImage("leftAllow.png");
		buttonNext = loadImage("rightAllow.png");
		button_singleMode_ = loadImage("_singleMode_.png");
		button_multiMode_ = loadImage("_multiMode_.png");
		checkedSymbol = loadImage("check.png");
		upgradeButton = loadImage("Tool.png");
		//_result_ = loadImage("_MISSION_COMPLETED_.png");
		//_killCounted_ = loadImage("_殺傷数_.png");
		//_damageCounted_ = loadImage("_負傷ダメージ_.png");
		//_timePassed_ = loadImage("_時間_.png");
		//_rank_ = loadImage("_ランク_.png");
		_ranking_ = loadImage("_ranking_.png");
		lightImg = loadImage("Light.png");
		nullImg = loadImage("#NullImage#.png");

		try{
			tracker.waitForAll(); //画像ロード
		}catch(InterruptedException e){}
		System.out.println("LocalImage: " + (System.currentTimeMillis() - loadTime));//ロード所要時間表示

		//コンフィグの読み込み
		loadConfig(true);//ロード所要時間の表示はメソッド内に含む

		//ランキング読み込み
		loadTime = System.currentTimeMillis();
		if(gameActivation){
			try(BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("ScoreRanking.txt")))){
				for(int i = 0;i < 15;i++){
					if((rankingName[i] = br.readLine()).equals("-----")){
						rankingName[i] = null;
						continue;
					}
					rankingScore[i] = Integer.parseInt(br.readLine());
					rankingStage[i] = br.readLine();
					rankingTime[i] = Integer.parseInt(br.readLine());
					rankingDate[i] = br.readLine();
				}
			}catch(IOException | NullPointerException e){}
		}
		System.out.println("Ranking: " + (System.currentTimeMillis() - loadTime));//ロード所要時間表示

		System.out.println("TOTAL LOAD TIME: " + (System.currentTimeMillis() - totalLoadTime) + "(ms)"); //総ロード所要時間表示
		if(gameActivation)
			new Thread(this).start(); //ゲームモードメインループ起動
	}
	int nowBattleBGM; //再生中のBGM番号
	boolean screenShot,debugMode; //静止モード(F5)、情報モード(F3)
	public void run(){
		resetData();
		try{
			while(true){
				nowTime = System.currentTimeMillis();
				try{
					Thread.sleep(25L);
				}catch(InterruptedException e){}
				if(screenShot)
					continue;
				if(nowEvent != PAUSE_MENU){
					nowFrame++;
					if(nowEvent == BATTLE){
						playerAction();
						if(playerLife > 0){
							playerMove();
							playerAttack();
						}
					}
				}
				mouseMoveAction();
				repaint();
				if(nowEvent == BATTLE)
					gameTime += (System.currentTimeMillis() - nowTime);
			}
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "申し訳ありませんが、エラーが発生しました。\nエラーコード：" + e.toString() + "\nクラッシュリポートが作成されます。","エラー",JOptionPane.ERROR_MESSAGE);
			final Date nowDate = new Date();
			final String dateStr = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(nowDate),
				dateStr2 = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(nowDate);
			final StringBuilder sb = new StringBuilder();
			sb.append("//I'm sorry! ブレイクスコープがクラッシュしました。");
			sb.append("\r\n時刻：").append(dateStr);
			sb.append("\r\nバージョン：").append(NOW_VERSION);
			sb.append("\r\nエラー出力：").append(e.getClass().getName()).append(": ").append(e.getMessage() == null ? "" : e.getMessage());
			for(StackTraceElement ver : e.getStackTrace())
				sb.append("\r\nat ").append(ver.toString());
			try{
				new File("source/crash_report/BreakScope_crashreport_latest.txt").createNewFile(); //ステージファイルを新規作成
			}catch(IOException ex){
			
				ex.printStackTrace();
			}
			try(BufferedWriter bw = new BufferedWriter(new FileWriter("source/crash_report/BreakScope_crashreport_latest.txt"))){
				bw.write(sb.toString());
				bw.flush();
			}catch(IOException e2){
				e2.printStackTrace();
			}
		}
	}
	final BufferedImage offImage = new BufferedImage(defaultScreenW,defaultScreenH,BufferedImage.TYPE_INT_ARGB_PRE); //ダブルバッファキャンバス
	Graphics2D g2; //描画のときに呼び出す
	final BufferedImage shadowLayer = new BufferedImage(defaultScreenW/8,defaultScreenH/8,BufferedImage.TYPE_INT_ARGB_PRE);
	Graphics2D g2_shadow;
	final BufferedImage BGLayer = new BufferedImage((defaultScreenW/200 + 2)*200,(defaultScreenH/200 + 2)*200,BufferedImage.TYPE_3BYTE_BGR);
	Graphics2D g2_BG;
	final Font basicFont,commentFont;
	final Color HPWarningColor = new Color(255,120,120),highHPColor = new Color(30,160,250,200),
		weaponReloadingColor = new Color(200,200,200,50),weaponReloadedColor = new Color(200,200,200,220),reloadGaugeColor = new Color(255,255,255,120);
	final BasicStroke stroke1 = new BasicStroke(1f),stroke2 = new BasicStroke(2f),stroke3 = new BasicStroke(3f),stroke5 = new BasicStroke(5f),stroke10 = new BasicStroke(10f);
	final DecimalFormat DF00_00 = new DecimalFormat("00.00");
	final Rectangle2D screenRect = new Rectangle2D.Double(0,0,defaultScreenW,defaultScreenH);
	double xVibration,yVibration;
	public void paintComponent(Graphics g){
		final long repaintTime = System.currentTimeMillis();
		if(g2 == null){
			g2 = offImage.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
		}
		if(g2_shadow == null)
			g2_shadow = shadowLayer.createGraphics();
		if(g2_BG == null){
			g2_BG = BGLayer.createGraphics();
			g2_BG.setColor(Color.BLACK);
			g2.setComposite(AlphaComposite.Src);
		}
		final int nowFrame = this.nowFrame;
		final int playerX = this.playerX,playerY = this.playerY,focusX = this.focusX,focusY = this.focusY;
		final int tX = -playerX + defaultScreenW/2,tY = -playerY + defaultScreenH/2; //キャンバススクロール量
		if(nowEvent == BATTLE){
			realFocusX = -tX + focusX;realFocusY = -tY + focusY; //実際の照準座標を求める(focusX,focusYはスクリーン内での座標)
			g2.setComposite(AlphaComposite.Clear);
			g2.fill(screenRect); //キャンバスをクリア
			g2.translate(tX,tY); //プレイヤーが中心に来るようにキャンバスを移動
		///////////////////////////////////////////////////////////////////////////////////////
			long actionUsedTime = System.currentTimeMillis();
			g2.setComposite(AlphaComposite.SrcOver);
			itemAction:{
				for(int i = 0;i <= item_maxID;i++){
					final int kind = itemKind[i];
					if(kind == NONE)
						continue;
					int x = itemX[i],y = itemY[i];
					if(abs(x - playerX) < 50 && abs(y - playerY) < 50){ //アイテム取得処理
						boolean takeItem = true;
						playerCommentFrame = nowFrame;
						if(kind == RECOVERY_PACK){
							cureStore += itemAmount[i];
							playerComment = "治療だ！";
							/*if(hp_max - playerTargetHP >= itemAmount[i]){
								playerTargetHP += itemAmount[i];
								playerHPChangedFrame = nowFrame;
								playerComment = "治療だ！";
							}else{
								if(playerComment == null)
									playerComment= "今は使いたくない・・・";
								takeItem = false;
							}*/
						}else{
							itemPossession[kind] += itemAmount[i];
							getAmmoSE.stop();
							getAmmoSE.play();
							playerComment = itemName[kind] + "が" + itemAmount[i] + "個だ！";
						}
						if(takeItem){
							deleteItemID(i);
							continue;
						}
					}
					//環境動力
					final int position = x/100*stageGridH + y/100;
					itemX[i] = x += xForceMap[position];
					itemY[i] = y += yForceMap[position];
					//描画
					final Image img = itemImg[kind];
					if(!outOfScreen_img(img,x,y))
						drawImageBS_centerDot(img,x,y);
				}
			}
			g2.setComposite(AlphaComposite.SrcOver);
			loadTime_item = System.currentTimeMillis() - actionUsedTime;
		///////////////////////////////////////////////////////////////////////////////////////
			actionUsedTime = System.currentTimeMillis();
			bulletAction:{ //弾処理
				if(cfg.bulletKindTotal <= 0)
					break bulletAction;
				if(bulletFreezeSwitch)
					break bulletAction;
				double x,y,xPower,yPower;
				int size;
				int x_int,y_int;
				double targetX,targetY;
				int penetration;
				action: for(int i = 0;i <= bullet_maxID;i++){
					final int kind = bulletKind[i];
					if(kind == NONE)
						continue action;
					//変数転写1
					x = bulletX[i]; //x座標
					y = bulletY[i]; //y座標
					xPower = bulletXPower[i]; //x動力
					yPower = bulletYPower[i]; //y動力
					targetX = x + xPower; //目的地X座標
					targetY = y + yPower; //目的地Y座標
					if(outOfStage_pixel((int)targetX,(int)targetY)){ //ステージ外削除処理
						this.deleteBulletID(i);
						continue action;
					}
					//変数転写2
					final double oldX = x,oldY = y;
					size = cfg.bulletSize[kind]; //大きさ
					x_int = (int)x;
					y_int = (int)y;
					penetration = bulletPenetration[i]; //貫通力
					final double speed = sqrt(xPower*xPower + yPower*yPower); //弾速度
					if(nowFrame - bulletAppearFrame[i] >= bulletLimitFrame[i] || bulletLimitMove[i] <= 0){ //弾ロスト処理、制限時間または制限距離に達したとき
						//消滅(ロスト)追加処理
						bulletLost(i);
						continue action;
					}
					if(penetration <= 0){ //貫通力がなくなったとき
						this.deleteBullet(i); //弾破壊処理
						continue action;
					}
					//衝突判定と攻撃処理
					//壁以外の衝突・ダメージ判定
					if(bulletTeam[i] != PLAYER){ //プレイヤーへのあたり判定、プレイヤー側非対象の場合この処理をスキップ
						if(damagePlayerInArea(x_int,y_int,size,bulletStr[i]/10)){ //弾衝突,ダメージ
							if(playerTargetHP > 0){
								playerXPower += xPower * bulletStr[i] / 30D;
								playerYPower += yPower * bulletStr[i] / 30D;
							}
							if(--penetration <= 0){ //貫通力がなくなったとき
								this.deleteBullet(i); //弾破壊処理
								continue action;
							}
						}
					}
					if(bulletTeam[i] != ENEMY){ //敵へのあたり判定、敵側の弾の場合この処理をスキップ
						final int[] ids = detectEnemyInArea(x_int,y_int,size,penetration); //衝突した敵を探索
						for(int id : ids){
							final int enemyKind = this.enemyKind[id];
							if(enemyKind == SHOTGUNMAN && rnd.nextInt(10) > 4 && penetration <= 1){ //ショットガンマンは4割銃弾を弾き返す(貫通力があると無理)
								if(--bulletReflectiveness[i] > 0){
									xPower *= -1;
									yPower *= -1;
									setEffect("BlueScatt",x_int,y_int,1);
									bulletAngle[i] += PI;
									bulletTeam[i] = ENEMY;
									bulletStr[i] = (int)(bulletStr[i] * 0.8);
								}
								break;
							}else{
								final int weight = cfg.enemyWeight[enemyKind];
								final int knockback = bulletKnockBack[i];
								enemyXPower[id] += xPower/weight*knockback;
								enemyYPower[id] += yPower/weight*knockback;
								enemyHP[id] -= bulletStr[i]; //敵へダメージ
								if(!enemyFoundMe.get(id)) //敵が攻撃の方向を振り向く
									enemyTargetAngle[id] = bulletAngle[i] + PI;
								penetration--; //貫通力減衰
							}
						}
						if(penetration <= 0){ //貫通力がなくなったとき
							this.deleteBullet(i); //弾破壊処理
							continue action;
						}
					}
					entity:{
						final int[] ids = detectEntityInArea(x_int,y_int,size,penetration,bulletTeam[i]); //衝突したエンティティを探索
						for(int id : ids){
							final int entityKind = this.entityKind[id];
							entityHP[id] -= bulletStr[i]; //敵へダメージ
							penetration--; //貫通力減衰
						}
						if(penetration <= 0){ //貫通力がなくなったとき
							this.deleteBullet(i); //弾破壊処理
							continue action;
						}
					}
					//壁・ギミックの衝突・ダメージ判定
					gimmick:{
						int[] hitMaps = getSquareHitGimmickID((int)targetX,(int)targetY,size,penetration); //ぶつかったギミックのマップ座標(ダメージを与えるブロックの座標)
						if(hitMaps.length > 0){
							final int penetration_tmp = penetration - 1;
							for(int k = 0;k < min(penetration - 1,hitMaps.length);k++) //衝突ギミックへダメージ(跳弾壁を考慮し-1)
								damageGimmick(hitMaps[k],bulletStr[i]);
							penetration -= hitMaps.length; //貫通力減衰
							if(penetration > 0){ //すべてを貫通できたとき
								if(cfg.bulletSuperPenetration.get(kind)){ //超貫通性能
									x = targetX; //通常直進処理
									y = targetY;
								}
								bulletPenetration[i] = penetration;
								break gimmick; //このラベルの処理を終了
							}
							//貫通しきれない場合、跳弾回数があれば跳弾へ移行する
							if(bulletReflectiveness[i] > 0){
								if(bulletStr[i] == cfg.weaponStrength[LONG_GUN]){ //跳弾音再生
									ricochetSE.stop();
									ricochetSE.play();
								}
								//跳弾処理
								if(xPower == 0.0 && yPower == 0.0){ //回転などによる特殊移動による衝突-製作中
								}else{ //通常飛行による衝突-跳弾直前地点を探索し、速度を反転
									double trialXLength = xPower,trialYLength = yPower; //検査線長さ
									double trialXPower,trialYPower; //検査地点間隔
									double trialX,trialY; //検査地点
									double startX = x,startY = y; //検査開始地点
									reflection: for(int k = 0;k < 100;k++){ //検査線消費ループ
										int collisionMap = 0; //衝突ブロックID
										//大きさ１つ分ずつ前進する
										final double ratio = (double)size/sqrt(trialXLength*trialXLength + trialYLength*trialYLength);
										trialXPower = trialXLength*ratio;trialYPower = trialYLength*ratio; //x,y前進距離を大きさ分に変換
										int moveLeft = (int)(1.0/ratio) - 1; //残り大きさ分移動回数を計算(めり込み反射を回避するため-1,次の残り移動距離で壁から脱走できなくなる場合を考慮する)
										trialX = startX;trialY = startY;
										while(true){ //検査地点移動ループ
											if(moveLeft <= 0){ //大きさ分移動回数がなくなる
												//最後の移動へ移行
												trialXPower = startX + trialXLength - trialX;trialYPower = startY + trialYLength - trialY; //残り移動距離を記録
												trialX = startX + trialXLength;trialY = startY + trialYLength;
												collisionMap = getSquareHitGimmickID((int)trialX,(int)trialY,size); //この点で衝突したか判定
												if(collisionMap != NONE){ //ぶつかったとき
													if(--bulletReflectiveness[i] < 0){ //跳弾回数がなくなったとき
														deleteBullet(i); //弾破壊
														continue action;
													}
													//まだ跳弾回数があるとき、跳弾し、残りの距離を移動
													break;
												}else{ //最終的に何もぶつからなかったとき<ループ終了地点>
													x = trialX; //実際に座標を移動
													y = trialY;
													//実際に方向を反射
													if(xPower > 0.0 ^ trialXPower > 0.0)
														xPower *= -1;
													if(yPower > 0.0 ^ trialYPower > 0.0)
														yPower *= -1;
													break reflection; //跳弾処理終了
												}
											}
											trialX += trialXPower; //試行座標から大きさ分前進
											trialY += trialYPower;
											moveLeft--;
											collisionMap = getSquareHitGimmickID((int)trialX,(int)trialY,size); //この点で衝突したか判定
											if(collisionMap != NONE){ //衝突ブロックが見つかるまで前進を続ける
												if(--bulletReflectiveness[i] <= 0){ //跳弾回数がなくなったとき
													deleteBullet(i); //弾破壊
													continue action; //この弾の処理を終了
												}
												//まだ跳弾回数があるとき、跳弾し、残りの距離を移動
												break;
											}
										}
										damageGimmick(collisionMap,(int)(bulletStr[i]*bulletReflectDamageRatio[i])); //跳弾ダメージ率に従って衝突ブロックにダメージを与える
										//衝突直前の座標にする
										trialX -= trialXPower;
										trialY -= trialYPower;
										//残り移動距離を作成
										trialXLength += startX - trialX;
										trialYLength += startY - trialY;
										//次の移動開始地点にする
										startX = trialX;
										startY = trialY;
										if(cfg.convertID_gimmick("Mud") != gimmickKind[collisionMap] && (trialXLength != 0.0 || trialYLength != 0.0)){ //速度反転処理
											if(!squareHitGimmick((int)(trialX - trialXPower),(int)(trialY + trialYPower),size)){ //x方向を反転すると当たらない時
												trialXLength *= -1;
												bulletAngle[i] = PI - bulletAngle[i];
											}else if(!squareHitGimmick((int)(trialX + trialXPower),(int)(trialY - trialYPower),size)){ //y方向を反転すると当たらない時
												trialYLength *= -1;
												bulletAngle[i] *= -1;
											}else{ //どちらでも当たる時
												trialXLength *= -1;
												trialYLength *= -1;
												bulletAngle[i] += PI;
											}
										}
									}
								}
							}else{ //貫通力、跳弾回数がともになくなったとき
								damageGimmick(hitMaps[penetration_tmp],bulletStr[i]);
								this.deleteBullet(i);
								continue action;
							}
						}else{ //非衝突
							x = targetX; //通常直進処理
							y = targetY;
						}
					}
					//以下弾幕処理
					final double rotateSpeed = cfg.bulletRotateSpeed[kind]; //公転速度(ラジアン)
					final int rotateRadians = cfg.bulletRotateRadius[kind]; //公転半径
					final double rotateAngle = bulletRotateAngle[i];
					//常時追加弾発生
					for(int gunID : cfg.bulletWithGun[kind]){
						if((nowFrame - bulletAppearFrame[i]) % cfg.weaponFireRate[gunID] == 0) //常時追加銃の連射間隔に従って連射(最初のフレーム含む)
							setBulletByWeapon(gunID,x,y,bulletAngle[i] + rotateAngle + rotateSpeed*(nowFrame - bulletAppearFrame[i]),BULLET,i,bulletTeam[i]);
					}
					//常時エフェクト発生
					for(int effectID : cfg.bulletWithEffect[kind])
						setEffect(effectID,x_int,y_int);
					//相殺処理
					int setoffStrength = bulletOffSet[i];
					for(int j = 0;j <= bullet_maxID && setoffStrength > 0;j++){
						if(bulletKind[j] == NONE || bulletTeam[j] == bulletTeam[i] && bulletTeam[j] != NONE)
							continue;
						int hitDistance = (cfg.bulletSize[bulletKind[j]] + size)/2;
						if(abs(bulletX[j] - x) < hitDistance && abs(bulletY[j] - y) < hitDistance //衝突
						&& setoffStrength >= bulletOffSet[j]){ //互いに敵対＆＆自身がより強い相殺力を持つ
							setoffStrength = (bulletOffSet[i] -= bulletOffSet[j] + 1); //相殺力減衰
							bulletTeam[j] = bulletTeam[i]; //所属チーム反転
							bulletXPower[j] *= -1;
							bulletYPower[j] *= -1;
							bulletAngle[j] += PI;
						} //自身が弱かったときは処理しない(いずれ強い方から処理が来る)
					}
					//公転処理
					if(rotateSpeed != 0 && rotateRadians != 0){
						x += rotateRadians*(cos(rotateAngle + rotateSpeed) - cos(rotateAngle));
						y += rotateRadians*(sin(rotateAngle + rotateSpeed) - sin(rotateAngle));
						bulletRotateAngle[i] = rotateAngle + rotateSpeed;
					}
					//ホーミング処理
					if(cfg.bulletHeatHoming.get(kind) && (int)speed != 0){
						double nearstDistance = MAX; //最も近い目標の距離
						int theID = NONE; //目標ID
						double homingAngle = NONE; //目標角度
						final int searchRange = 90000/(int)speed*(int)speed; //速度1で300ピクセルの反比例探索範囲
						if(bulletTeam[i] == PLAYER){
							for(int j = 0;j <= enemy_maxID;j++){
								if(enemyKind[j] == NONE || enemyHP[j] <= 0)
									continue;
								//より近く、発熱が強い目標についていく
								final int xd = (int)enemyX[j] - (int)x,yd = (int)enemyY[j] - (int)y;
								final int distance = xd*xd + yd*yd - cfg.enemyHeat[enemyKind[j]];
								if(distance < searchRange && distance < nearstDistance){
									nearstDistance = distance;
									theID = j;
								}
							}
							if(theID != NONE) //目標を捕捉した場合
								homingAngle = atan2(enemyY[theID] - y,enemyX[theID] - x);
						}else{ //敵側の弾
							if((playerX - x)*(playerX - x) + (playerY - y)*(playerY - y) < searchRange) //プレイヤーを捕捉
								homingAngle = atan2(playerY - y,playerX - x);
						}
						if(homingAngle != NONE){
							//角度差0.05ラジアン以上の場合回転処理
							final double angleDistance = homingAngle - bulletAngle[i];
							if(angleDistance < -0.05){
								final double bulletAngle2 = angleFormat(bulletAngle[i] + max(angleDistance,-0.2));
								bulletAngle[i] = bulletAngle2;
								xPower = speed*cos(bulletAngle2);
								yPower = speed*sin(bulletAngle2);
							}else if(0.05 < angleDistance){
								final double bulletAngle2 = angleFormat(bulletAngle[i] + min(angleDistance,0.2));
								bulletAngle[i] = bulletAngle2;
								xPower = speed*cos(bulletAngle2);
								yPower = speed*sin(bulletAngle2);
							}
						}
					}
					//射手追随処理
					final int target = bulletFollowTarget[i];
					if(cfg.bulletGunnerFollow.get(kind) && target != NONE){
						final int id = target%TYPE_SEPARATOR;
						switch(target/TYPE_SEPARATOR*TYPE_SEPARATOR){
						case PLAYER:
							if(nowFrame - respawnFrame < 1)
								bulletFollowTarget[i] = NONE;
							else{
								x += playerX - playerOldX;
								y += playerY - playerOldY;
							}
							break;
						case ENEMY:
							if(enemyKind[id] == NONE)
								bulletFollowTarget[i] = NONE;
							else{
								x += enemyX[id] - enemyOldX[id];
								y += enemyY[id] - enemyOldY[id];
							}
							break;
						case BULLET:
							if(bulletKind[id] == NONE)
								bulletFollowTarget[i] = NONE;
							else{
								x += bulletX[id] - bulletOldX[id];
								y += bulletY[id] - bulletOldY[id];
							}
							break;
						}
					}
					//移動距離記録
					final double movedX = x - oldX,movedY = y - oldY,movedD = sqrt(movedX*movedX + movedY*movedY);
					bulletMovedDistance[i] += (int)movedD; //移動距離加算
					if(bulletLimitMove[i] != MAX) //限界距離減算
						bulletLimitMove[i] -= (int)movedD;
					//変数同期
					bulletOldX[i] = bulletX[i];
					bulletX[i] = x;
					bulletOldY[i] = bulletY[i];
					bulletY[i] = y;
					xPower += bulletXAccel[i];
					yPower += bulletYAccel[i];
					final double stallRatio = cfg.bulletStallRatio[kind]; //失速率
					xPower *= stallRatio;
					yPower *= stallRatio;
					bulletXPower[i] = xPower;
					bulletYPower[i] = yPower;
					//描画
					final Image img = bulletImg[kind];
					if(!outOfScreen_img2(img,x_int,y_int)){
						final double angle = bulletAngle[i] + rotateSpeed*(nowFrame - bulletAppearFrame[i]);
						g2.rotate(angle,x_int,y_int);
						drawImageBS_centerDot(img,x_int,y_int);
						g2.rotate(-angle,x_int,y_int);
					}
					//レーザー処理,一瞬で目標到達するため同じIDでループ
					if(cfg.bulletLaserAction.get(kind) && ((int)bulletXPower[i] != 0 || (int)bulletYPower[i] != 0))
						i--;
				}
			}
			loadTime_bullet = System.currentTimeMillis() - actionUsedTime;
		///////////////////////////////////////////////////////////////////////////////////////
			actionUsedTime = System.currentTimeMillis();
			entityAction:{ //エンティティアクション
				if(cfg.entityKindTotal <= 0)
					break entityAction;
				if(entityFreezeSwitch)
					break entityAction;
				if(playerRidingID != NONE){ //プレイヤーが何かに乗っている(操縦している)時、旧座標更新
					playerOldX = playerX;
					playerOldY = playerY;
				}
				for(int i = 0;i <= entity_maxID;i++){
					final int kind = entityKind[i];
					if(kind == NONE)
						continue;
					int x = (int)entityX[i],y = (int)entityY[i];
					//死亡処理
					if(entityHP[i] <= 0 && entityHP[i] != NONE){
						cfg.entityClass[kind].killed(i);
						deleteEntityID(i);
						continue;
					}
					//ステージ外削除処理
					if(outOfStage_pixel(x,y)){
						deleteEntityID(i);
						continue;
					}
					final int mapPoint = x/100*stageGridH + y/100;
					//環境動力
					final int xForce = xForceMap[mapPoint],yForce = yForceMap[mapPoint];
					final int halfSize = cfg.entitySize[kind]/2;
					if(xForce != 0 && gimmickHP[(int)(entityX[i] + xForce + halfSize)/100*stageGridH + y/100] <= 0){
						x += xForce;
						if(x < 0)
							x = 0;
						else if(stageW < x)
							x = stageW;
						entityX[i] = x;
					}
					if(yForce != 0 && gimmickHP[x/100*stageGridH + (int)(entityY[i] + yForce + halfSize)/100] <= 0){
						y += yForce;
						if(y < 0)
							y = 0;
						else if(stageH < y)
							y = stageH;
						entityY[i] = y;
					}
					//環境被害
					entityHP[i] -= damageMap[mapPoint];
					//描画とその他の処理
					cfg.entityClass[kind].gamePaint(i,entityX[i],entityY[i],entityAngle[i],!outOfScreen_img2(entityImg[kind],x,y));
				}
			}
			loadTime_entity = System.currentTimeMillis() - actionUsedTime;
			actionUsedTime = System.currentTimeMillis();
		/////otherAction//////////////////////////////////////////////////////////////////////////////////////
			g2.setFont(basicFont);
			int shotFrame = this.shotFrame[slot];
			if(nowFrame - shotFrame < 0)
				shotFrame = nowFrame;
			if(playerLife > 0){ //以下生存処理
				final double angle = playerAngle;
				g2.rotate(angle,playerX,playerY);
				Image img = null;
				if(0 <= weapon && weapon < cfg.weaponKindTotal){
					final int weaponChangedDuration = nowFrame - weaponChangedFrame,
						weaponFiredDuration = nowFrame - shotFrame;
					if(weaponChangedDuration < 3)
						img = playerChangeImg[weaponChangedDuration % 2];
					else{
						final int actionType = cfg.weaponActionType[weapon];
						if(weaponFiredDuration > cfg.weaponFireRate[weapon] || playerImg[actionType].length == 1)
							img = playerImg[actionType][0];
						else{
							switch(actionType){
							case SHOTGUN:
								if(weaponFiredDuration < 7)
									img = playerImg[SHOTGUN][0];
								else if(weaponFiredDuration < 12)
									img = playerImg[SHOTGUN][1];
								else if(weaponFiredDuration < 16)
									img = playerImg[SHOTGUN][2];
								else if(weaponFiredDuration < 20)
									img = playerImg[SHOTGUN][1];
								else
									img = playerImg[SHOTGUN][0];
								break;
							case CHAIN_SAW:
								if(bulletSwitch)
									img = playerImg[CHAIN_SAW][1];
								else
									img = playerImg[CHAIN_SAW][0];
								break;
							default:
								img = playerImg[actionType][(int)round((double)weaponFiredDuration/((double)cfg.weaponFireRate[weapon]/(double)(playerImg[actionType].length - 1)))];
							}
						}
					}
				}
				if(img == null)
					img = playerImg[1][0];
				if(shotFrame == nowFrame)
					drawImageBS_centerDot(img,playerX + nowFrame%2,playerY + nowFrame%2);
				else
					drawImageBS_centerDot(img,playerX,playerY);
				g2.rotate(-angle,playerX,playerY);
				if(playerBarrierTime > 0 && (playerBarrierTime > 40 || nowFrame%6 != 0)) //バリアを表示&もうすぐ切れるときは点滅
					drawImageBS_centerDot(barrierImg,playerX,playerY);
				if(playerLife == 1 && nowFrame % 4 < 2 && nowFrame - respawnFrame < 40){ //ラスト残機警告
					g2.setColor(Color.YELLOW);
					g2.drawString("last respon",playerX - 32,playerY - 5);
				}
				if(playerActionTarget != NONE){ //動作進行度表示(看板を読む,箱を開けるなど)
					g2.setColor(highHPColor);
					g2.drawArc(playerX - 30,playerY - 90,30,30,0,(int)(playerActionProgress*3.6));
					g2.setColor(Color.CYAN);
					g2.drawString(playerActionProgress + "%",playerX - 10,playerY - 80);
					if(playerActionProgress >= 100){
						playerActionProgress = 0;
						playerActionTarget = NONE;
					}
				}
			}
		///////////////////////////////////////////////////////////////////////////////////////
			g2.setFont(commentFont);
			boolean suspect = false;
			enemyAction:{ //敵処理
				if(cfg.enemyKindTotal <= 0)
					break enemyAction;
				if(enemyFreezeSwitch)
					break enemyAction;
				enemyMap.clear();
				nearestEnemyDistance = MAX;
				g2.setStroke(stroke2);
				g2.setColor(Color.BLACK);
				int size,halfSize;
				double x,y,xPower,yPower;
				int x_int,y_int,mapX,mapY,mapPoint;
				double angle,oppositeAngle;
				int playerDx,playerDy,playerDistance;
				for(int i = 0;i <= enemy_maxID;i++){
					final int kind = enemyKind[i]; //種類
					if(kind == NONE)
						continue;
					//死亡処理
					if(enemyHP[i] <= 0 && enemyHP[i] != NONE){
						cfg.enemyClass[kind].killed(i); //死亡追加処理を呼び出す
						mainScore += cfg.enemyScore[kind]; //スコア加算
						money += cfg.enemyScore[kind];
						deleteEnemyID(i); //この敵を削除
						continue;
					}
					x = enemyX[i]; //x座標
					y = enemyY[i]; //y座標
					x_int = (int)x; //int化x座標
					y_int = (int)y; //int化x座標
					//ステージ外削除処理
					if(outOfStage_pixel(x_int,y_int)){
						mainScore += cfg.enemyScore[kind]; //スコア加算
						money += cfg.enemyScore[kind];
						deleteEnemyID(i); //この敵を削除
						continue;
					}
					//生存処理(通常処理)
					size = cfg.enemySize[kind]; //大きさ
					halfSize = size/2; //半分の大きさ
					xPower = enemyXPower[i]; //x方向の動力
					yPower = enemyYPower[i]; //y方向の動力
					mapX = x_int/100; //1/100化マップ用x座標
					mapY = y_int/100; //1/100化マップ用y座標
					mapPoint = mapX*stageGridH + mapY;
					angle = atan2(y - playerY,x - playerX); //プレイヤーからの角度
					oppositeAngle = angle + PI; //プレイヤーへの角度
					playerDx = playerX - x_int;playerDy = playerY - y_int; //プレイヤーとのx,y距離
					playerDistance = (int)sqrt(playerDx*playerDx + playerDy*playerDy); //プレイヤーとの距離
					//レーダー処理
					if(nearestEnemyDistance > playerDistance){
						nearestEnemyDistance = playerDistance;
						nearestEnemyAngle = angle;
					}
					//近接攻撃判定処理
					if(slashed > 3){
						final double d = toDegrees(abs(playerAngle - angle));
						if((d < 50 || d > 310) && playerDistance < size + 40 || playerDistance < playerSize){
							enemyHP[i] -= 20;
							final int weight = cfg.enemyWeight[kind];
							enemyXPower[i] += 100*cos_playerAngle/weight;
							enemyYPower[i] += 100*sin_playerAngle/weight;
							enemyTargetAngle[i] = oppositeAngle;
						}
					}
					//敵の水平・鉛直方向の動力処理&壁衝突判定
					if(xPower < -1.0 || 1.0 < xPower || yPower < -1.0 || 1.0 < yPower){ //低動力の場合はスキップ
						boolean hitBlock = false,xMovable = false,yMovable = false;
						if(!squareHitGimmick((int)(x + xPower),(int)y,size)
							&& x + xPower + halfSize < stageW - 700 && x + xPower - halfSize > 700)
							xMovable = true;
						else{ //壁に衝突
							xPower *= -0.5;
							hitBlock = true;
						}
						if(!squareHitGimmick((int)x,(int)(y + yPower),size)
							&& y + yPower + halfSize < stageH - 700 && y + yPower - halfSize > 700)
							yMovable = true;
						else{ //壁に衝突
							yPower *= -0.5;
							hitBlock = true;
						}
						if(xMovable){
							if(yMovable){
								if(!squareHitGimmick((int)(x + xPower),(int)(y + yPower),size)){ //通常移動
									x += xPower;
									y += yPower;
								}
							}else //x方向のみ移動
								x += xPower;
						}else if(yMovable) //y方向のみ移動
							y += yPower;
						if(hitBlock){ //壁に衝突した時、速度に応じてダメージ
							final double speed = sqrt(xPower*xPower + yPower*yPower);
							if(speed > 10)
								enemyHP[i] -= speed*2;
						}
						//動力自然減衰
						if(xPower < -1.6 || 1.6 < xPower)
							xPower *= 0.8; //x動力自然減衰
						else
							xPower = 0;
						if(yPower < -1.6 || 1.6 < yPower)
							yPower *= 0.8; //y動力自然減衰
						else
							yPower = 0;
						//更新
						enemyXPower[i] = xPower;
						enemyYPower[i] = yPower;
					}
					//環境動力
					final int xForce = xForceMap[mapPoint],yForce = yForceMap[mapPoint];
					if(xForce != 0 && gimmickHP[(int)(x + xForce + halfSize)/100*stageGridH + mapY] <= 0){
						x += xForce;
						if(x < 0)
							x = 0;
						else if(stageW < x)
							x = stageW;
					}
					if(yForce != 0 && gimmickHP[mapX*stageGridH + (int)(y + yForce + halfSize)/100] <= 0){
						y += yForce;
						if(y < 0)
							y = 0;
						else if(stageH < y)
							y = stageH;
					}
					//環境被害
					enemyHP[i] -= damageMap[mapPoint];
					//更新
					enemyOldX[i] = enemyX[i];
					enemyX[i] = x;
					enemyOldY[i] = enemyY[i];
					enemyY[i] = y;
					x_int = (int)x;y_int = (int)y;
					mapX = x_int/100;mapY = y_int/100;
					mapPoint = mapX*stageGridH + mapY;
					//視界処理
					//変数準備(通過判定に使うものもある)
					final int SIGHT_DENSITY = 15; //探索間隔
					final int AWARE_DISTANCE = 10; //直感距離(強制的に目標を視認する距離)
					final double playerDirection = oppositeAngle,directionDifference = angleFormat(abs(playerDirection - enemyTargetAngle[i])),
						incrementalRatio = (double)SIGHT_DENSITY/(double)playerDistance, //探索間隔と実距離の比
						xGridIncremental = playerDx*incrementalRatio/100.0,yGridIncremental = playerDy*incrementalRatio/100.0; //探索地点移動量(グリッド座標)
					boolean sweepable = size < 200; //通過が保障されるか(この初期値はスイーパー使用条件)(trueのとき通過判定をパス)
					//判定開始
					if(playerDistance < playerSize/2 + halfSize + AWARE_DISTANCE){ //直感距離以下
						enemyFoundMe.set(i); //可視
						enemySightCuttingGrid[i] = NONE; //斜線切断地点の登録を消去
					}else if(!enemyFoundMe.get(i) && nowFrame % (playerDistance/50 + 3) != 0 || //探索中のとき、遠いほど視界判定が疎らになる
						directionDifference < -PI/2 || directionDifference > +PI/2 || //視野角に入っていない(180°)
						nowFrame - respawnFrame <= 10 || playerLife <= 0 || //リスタート直後やゲームオーバー後
						gimmickHP[playerX/100*stageGridH + playerY/100] > 0 || gimmickHP[mapPoint] > 0) // //自身、及び目標が壁の中に存在している
						enemyFoundMe.clear(i); //不可視
					else{
						enemyFoundMe.clear(i); //プレイヤー発見状態初期化
						//アルゴリズム：ドットベジテーション法
						//考案者：本ゲーム作者
						//等間隔に点をうち、それらに壁があるかを探索する。非常に高速だが、間隔を広げすぎると壁の角を跨いでしまうこともある。
						//プレイヤーと自分の中心点を結ぶ線一本のみを見ており、個体の大きさは考慮していない。
						vision:{
							//遮蔽壁優先探索は、よりいいアルゴリズムが考え付いた時にまた採用
							/*if(enemySightCuttingGrid[i] != NONE){ //前回で斜線を切ったブロックが存在
								//このブロックで斜線が切れないか優先的に探索
								final int cutterGridX = enemySightCuttingGrid[i]/stageGridH,cutterGlidY = enemySightCuttingGrid[i]%stageGridH;
								if(cutterGlidX > mapX ^ cutterGlid > playerX || cutterGlidY > mapY ^ cutterGlid > playerY
								final int cutterX = cutterGridX*100,cutterY = cutterGlidY*100;
								final int cutterXd = abs(cutterX - x_int),cutterYd = abs(cutterY - y_int); //対象ブロックとのx,y距離
								final double slope = (double)playerDy/(double)playerDx;
								if(abs((cutterXd - 50)*slope) < cutterY + 50 ^ abs((cutterX + 50)*slope) > cutterY - 50) //斜線が切られた
									break vision;
							}*/
							//視界判定本番
							int limit = (int)(1.0/incrementalRatio); //判定回数
							double x2_grid = x/100 + xGridIncremental,y2_grid = y/100 + yGridIncremental; //最初の１回だけ進む
							for(;limit > 0;x2_grid += xGridIncremental,y2_grid += yGridIncremental,limit--){
								if(outOfStage_grid((int)x2_grid,(int)y2_grid)) //異常-ステージ外のグリッド
									break; //可視扱い,判定終了
								final int grid = (int)x2_grid*stageGridH + (int)y2_grid; //探索地点(グリッド座標値)
								if(sweepable && gimmickSweeper[grid] > 0) //サイズがスイーパー有効圏内 && 周囲にブロックあり
									sweepable = false; //通過不能を記録
								if(gimmickHP[grid] > 0){ //遮蔽性あり
									enemySightCuttingGrid[i] = grid; //この地点を登録
									break vision; //視界判定終了
								}
							}
							//探索の結果、遮蔽物がないことが確認されるとここに来る
							enemyFoundMe.set(i); //可視を記録
							enemySightCuttingGrid[i] = NONE; //斜線切断地点の登録を消去
						}
					}
					//壁迂回処理
					avoidstructures:{
						if(enemyFoundMe.get(i)){ //プレイヤー発見状態
							enemyTargetAngle[i] = playerDirection; //プレイヤーの方向を向く
							enemyNextAngle[i] = NONE;
							enemyNextTargetX[i] = playerX;
							enemyNextTargetY[i] = playerY;
							if(cfg.enemySpeed[kind] > playerSpeed*0.8) //素早い敵にレーダーが反応
								suspect = true;
						}else{ //巡回処理または静止状態
							if(x != enemyTargetX[i] || y != enemyTargetY[i] || enemyNextTargetX[i] == enemyTargetX[i] && enemyNextTargetY[i] == enemyTargetY[i]){
								if(x == enemyTargetX[i] && y == enemyTargetY[i] && //移動予定なし
									xPatrolTargetMap[mapPoint] != NONE && //巡回情報あり
									(x_int - 50)%100 == 0 && (y_int - 50)%100 == 0){ //ちょうどマップの格子区切りの中央(巡回地点の中央)にある
										enemyTargetX[i] = enemyNextTargetX[i] = xPatrolTargetMap[mapPoint]*100 + 50;
										enemyTargetY[i] = enemyNextTargetY[i] = yPatrolTargetMap[mapPoint]*100 + 50;
										enemyNextAngle[i] = enemyTargetAngle[i] = atan2(enemyNextTargetY[i] - y,enemyNextTargetX[i] - x);
								}
								break avoidstructures;
							}
							if(enemyNextAngle[i] == NONE)
								enemyNextAngle[i] = atan2(playerY - enemyNextTargetY[i],playerX - enemyNextTargetX[i]);
						}
						//通過判定
						int blockMapX = NONE,blockMapY = NONE; //衝突するブロックの座標
						pass:{ //軌道修正処理
							//敵が十分小さく、スイーパーの反応なしで視界判定が終了したときや、現在位置と目標地点のグリッドが同じとき、この処理をスキップ
							if(sweepable || (int)enemyNextTargetX[i]/100 == mapX || (int)enemyNextTargetY[i]/100 == mapY) //
								break pass;
							double x2_grid = x/100,y2_grid = y/100; //第１探索点
							final int PASS_DENSITY = 101; //通過判定精密度(進行方向,対角線方向の両方に影響)(数値が小さいほど壁を補足しやすくなる)
							int limit = (int)(playerDistance/PASS_DENSITY) + 1; //判定回数
							final boolean sizeUnder200 = size < 200; //スイーパーが使用できるか(2グリッド以上の体格があるとスイーパーの通過保障が使えない)
							//※本当なら視界判定の方でいつスイーパーに反応があったか記憶させた方がいいが、この処理に必ず到達するとは限らず、その判定も複雑なため、もう一度探索
							final boolean slope = playerDx > 0 ^ playerDy > 0; //傾き方向,trueのとき傾き>=0,falseのとき傾き<0
							//対角線上に壁がないか調べる
							do{
								x2_grid += xGridIncremental;y2_grid += xGridIncremental; //移動して次の判定地点へ
								if(!sizeUnder200 || gimmickSweeper[(int)x2_grid*stageGridH + (int)y2_grid] > 0){ //スイーパー無効,詳細検証が必要
									int x3_grid,y3_grid; //第２探索点
									//出っ張り角を結ぶ対角線上で遮蔽ブロックを検査、体格が大きいほど判定数増加
									int side_limit = (int)((double)halfSize/(double)PASS_DENSITY) + 1; //判定回数
									final double kSize_grid_incrimental = PASS_DENSITY/100.0; //判定間隔
									double kSize_grid = 0; //現在の判定幅
									do{
										kSize_grid += kSize_grid_incrimental; //広げて次の判定幅へ
										//右下点のグリッドを判定
										x3_grid = (int)(x2_grid + (slope ? kSize_grid : -kSize_grid));y3_grid = (int)(y2_grid + kSize_grid);
										if(outOfStage_grid(x3_grid,y3_grid)) //異常-ステージ外のグリッド
											break; //遮蔽性なし
										if(gimmickHP[x3_grid*stageGridH + y3_grid] > 0){ //右下に遮蔽ブロックあり
											//遮蔽位置を登録
											blockMapX = x3_grid;
											blockMapY = y3_grid;
											break; //遮蔽性あり
										}
										//左上点のグリッドを判定
										x3_grid = (int)(x2_grid + kSize_grid);y3_grid = (int)(y2_grid + (slope ? kSize_grid : -kSize_grid));
										if(outOfStage_grid(x3_grid,y3_grid)) //異常-ステージ外のグリッド
											break; //遮蔽性なし
										if(gimmickHP[x3_grid*stageGridH + y3_grid] > 0){ //左上に遮蔽ブロックあり
											//遮蔽位置を登録
											blockMapX = x3_grid;
											blockMapY = y3_grid;
											break; //遮蔽性あり
										}
									}while(--side_limit > 0);
								}
							}while(--limit > 0);
						}
						if(blockMapX == NONE){
							enemyTargetX[i] = enemyNextTargetX[i];
							enemyTargetY[i] = enemyNextTargetY[i];
						}else{
							final boolean sideP[] ={true,true,true,true}; //迂回点候補
							final int blockX = blockMapX*100 + 50,blockY = blockMapY*100 + 50; //壁ピクセル座標
							final int w2 = (size + 5)/2 + 50,h2 = (size + 5)/2 + 50; //判定用横幅(少し大きめ)
							if(x_int > blockX - w2){
								if(y_int > blockY - h2)
									sideP[0] = false;
								if(y_int < blockY + h2)
									sideP[2] = false;
							}
							if(x_int < blockX + w2){
								if(y_int > blockY - h2)
									sideP[1] = false;
								if(y_int < blockY + h2)
									sideP[3] = false;
							}
							if(gimmickHP[(blockMapX - 1)*stageGridH + blockMapY - 1] > 0)
								sideP[0] = false;
							if(gimmickHP[(blockMapX + 1)*stageGridH + blockMapY - 1] > 0)
								sideP[1] = false;
							if(gimmickHP[(blockMapX - 1)*stageGridH + blockMapY + 1] > 0)
								sideP[2] = false;
							if(gimmickHP[(blockMapX + 1)*stageGridH + blockMapY + 1] > 0)
								sideP[3] = false;
							double distance0 = MAX,
								distance1 = MAX,
								distance2 = MAX,
								distance3 = MAX;
							if(sideP[0])
								distance0 = (blockX - w2 - enemyNextTargetX[i])*(blockX - w2 - enemyNextTargetX[i]) + (blockY - h2 - enemyNextTargetY[i])*(blockY - h2 - enemyNextTargetY[i]);
							if(sideP[1])
								distance1 = (blockX + w2 - enemyNextTargetX[i])*(blockX + w2 - enemyNextTargetX[i]) + (blockY - h2 - enemyNextTargetY[i])*(blockY - h2 - enemyNextTargetY[i]);
							if(sideP[2])
								distance2 = (blockX - w2 - enemyNextTargetX[i])*(blockX - w2 - enemyNextTargetX[i]) + (blockY + h2 - enemyNextTargetY[i])*(blockY + h2 - enemyNextTargetY[i]);
							if(sideP[3])
								distance3 = (blockX + w2 - enemyNextTargetX[i])*(blockX + w2 - enemyNextTargetX[i]) + (blockY + h2 - enemyNextTargetY[i])*(blockY + h2 - enemyNextTargetY[i]);
							final double closestDistance = min(distance0,min(distance1,min(distance2,distance3)));
							if(closestDistance ==  distance0){
								enemyTargetX[i] = blockX - w2 - 15;
								enemyTargetY[i] = blockY - h2 - 15;
							}else if(closestDistance ==  distance1){
								enemyTargetX[i] = blockX + w2 + 15;
								enemyTargetY[i] = blockY - h2 - 15;
							}else if(closestDistance ==  distance2){
								enemyTargetX[i] = blockX - w2 - 15;
								enemyTargetY[i] = blockY + h2 + 15;
							}else if(closestDistance ==  distance3){
								enemyTargetX[i] = blockX + w2 + 15;
								enemyTargetY[i] = blockY + h2 + 15;
							}
						}
					}
					turnEnemy:{ //敵なめらか回転処理
						final double angle2 = (enemyTargetAngle[i] - enemyAngle[i])%(PI*2); //角度差
						if(toDegrees(abs(angle2)) < 3) //角度差3度以下で回転停止
							break turnEnemy;
						enemyAngle[i] = angleFormat(enemyAngle[i] + angleFormat(angle2)/500.0*cfg.enemyTurnSpeed[kind]);
					}
					if(enemyWound[i] != 0){ //スタン値加算
						enemyShotFrame[i] += enemyWound[i];
						enemyWound[i] = 0;
					}
					boolean aimed;
					if(!enemyFoundMe.get(i) || abs(x - playerX) > defaultScreenW/2 + OVER_X || abs(y - playerY) > defaultScreenH/2 + OVER_Y)
						aimed = false;
					else
						aimed = abs(toDegrees(angleFormat(oppositeAngle - enemyAngle[i]))) < 30;
					final int readyTime = nowFrame - enemyShotFrame[i];
					//敵マップ更新
					final int xStart = (x_int - halfSize)/100,xEnd = (x_int + halfSize)/100;
					final int yStart = (y_int - halfSize)/100,yEnd = (y_int + halfSize)/100;
					for(int x2 = xStart;x2 <= xEnd;x2++){
						for(int y2 = yStart;y2 <= yEnd;y2++)
							enemyMap.set(x2*stageGridH + y2); //自分の位置するマスをtrue代入(Bitmap)
					}
					//描画とその他の処理
					cfg.enemyClass[kind].gamePaint(i,x,y,enemyAngle[i],aimed,readyTime,playerDistance,!outOfScreen_img2(enemyImg[kind],x_int,y_int));
				}
			}
			loadTime_enemy = System.currentTimeMillis() - actionUsedTime;
		///////////////////////////////////////////////////////////////////////////////////////
			actionUsedTime = System.currentTimeMillis();
			gimmickAction:{ //ギミック処理
				if(cfg.gimmickKindTotal <= 0)
					break gimmickAction;
				if(gimmickFreezeSwitch)
					break gimmickAction;
				for(int i = 0;i < stageGridTotal;i++){
					final int kind = gimmickKind[i];
					if(kind == NONE)
						continue;
					final int hp = gimmickHP[i];
					if(hp <= 0 && hp != NONE && hp != MIN){ //死亡判定
						deleteGimmick(i);
						continue;
					}
					final int x = i/stageGridH*100 + 50,y = i%stageGridH*100 + 50; //ピクセル座標に変換
					//発光処理
					final int brightness = cfg.gimmickBrightness[kind];
					if(brightness > 0)
						setLight(x,y,brightness); //光源を設置
					//描画とその他の処理
					cfg.gimmickClass[kind].gamePaint(i,!outOfScreen_pixel(x,y,100,100));
				}
			}
			loadTime_gimmick = System.currentTimeMillis() - actionUsedTime;
		///////////////////////////////////////////////////////////////////////////////////////
			actionUsedTime = System.currentTimeMillis();
			effectAction:{
				if(cfg.effectKindTotal <= 0)
					break effectAction;
				if(effectFreezeSwitch)
					break effectAction;
				float nowAlpha = 1.0f;
				for(int i = 0;i <= effect_maxID;i++){
					final int kind = effectKind[i];
					if(kind == NONE)
						continue;
					//特定種類探知文
					//if(kind == cfg.nameToID_effect.get("name"))
					//	System.out.println("detected target effect:(id: " + i + ",frame: " + nowFrame + ")");
					int framePassed = nowFrame - effectApparFrame[i];
					if(framePassed > cfg.effectLimitFrame[kind]){ //アニメーション終了
						deleteEffectID(i);
						continue;
					}
					//座標の移動
					effectX[i] += effectXPower[i];
					effectY[i] += effectYPower[i];
					//加速度の演算
					final double accel = cfg.effectAccel[kind];
					if(accel != 1.0){
						effectXPower[i] *= accel;
						effectYPower[i] *= accel;
					}
					//透過度の更新
					final float alpha = (float)effectAlpha[i];
					if(cfg.effectAlphaChanges[kind].length != 0){
						effectAlpha[i] += effectAlphaPower[i];
						if(effectAlpha[i] > 1.0)
							effectAlpha[i] = 1.0;
						else if(effectAlpha[i] < 0.0)
							effectAlpha[i] = 0.0;
						//透過度変化量の更新
						final int[] switchingFrames = cfg.effectAlphaChangeFrame[kind]; //変化量を切り替えるフレーム
						for(int section = 0;section < switchingFrames.length;section++){
							if(switchingFrames[section] >= framePassed){ //切り替えフレーム　＞＝　経過フレーム
								if(switchingFrames[section] == framePassed)
									effectAlphaPower[i] = cfg.effectAlphaChanges[kind][section];
								break;
							}
						}
					}
					//描画
					final int x = effectX[i],y = effectY[i];
					final int animetion_max = cfg.effectTimePhase[kind].length; //フェイズ数
					final int[] animetions_time = cfg.effectTimePhase[kind]; //フェイズごとの所要時間
					int size = effectSize[i]; //大きさ(NONEのとき、画像基準)
					if(size == NONE){ //大きさ画像基準
						for(int animetion = 0;animetion < animetion_max;animetion++){
							framePassed -= animetions_time[animetion]; //経過時間からこのフェイズの所要時間を引く
							if(framePassed <= 0){ //該当フェイズを見つける
								final Image img = effectImg[kind][animetion]; //描画する画像
								if(!outOfScreen_img(img,x,y)){ //描画範囲内
									//描画透過度の設定
									if(alpha != nowAlpha){ //前回のエフェクト透過度と同一じゃない時だけ設定
										g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
										nowAlpha = alpha;
									}
									drawImageBS_centerDot(img,x,y);
								}
								break;
							}
						}
					}else{ //大きさ規定値基準
						for(int animetion = 0;animetion < animetion_max;animetion++){
							framePassed -= animetions_time[animetion]; //経過時間からこのフェイズの所要時間を引く
							if(framePassed <= 0){ //該当フェイズを見つける
								//大きさ推定
								final int sizeGrow = cfg.effectSizeGrow[kind];
								if(sizeGrow != 0) //大きさ変化量が存在
									size += sizeGrow*framePassed; //このときの大きさを推定
								if(!outOfScreen_pixel(x,y,size,size)){ //描画範囲内
									//描画透過度の設定
									if(alpha != nowAlpha){ //前回のエフェクト透過度と同一じゃない時だけ設定
										g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
										nowAlpha = alpha;
									}
									final int halfSize = size/2;
									drawImageBS(effectImg[kind][animetion],x - halfSize,y - halfSize,size,size);
								}
								break;
							}
						}
					}
				}
				//透過度設定を戻す
				g2.setComposite(AlphaComposite.SrcOver);
			}
			loadTime_effect = System.currentTimeMillis() - actionUsedTime;
		///////////////////////////////////////////////////////////////////////////////////////
			//共通処理
			everybodyAction: {
				//アタックプラン
				for(int i = 0;i < attackPlanID.length;i++){
					if(nowFrame < attackPlanFrame[i]) //発動フレームに到達したアタックプランを検索
						continue; //達していないときはスキップ
					final int attackerID = attackPlanID[i]%TYPE_SEPARATOR; //攻撃元のID
					switch(attackPlanID[i]/TYPE_SEPARATOR*TYPE_SEPARATOR){ //種類トークンで分割、攻撃元のオブジェクト種類を割り出す
					case PLAYER: //プレーヤー
						if(playerLife <= 0) //攻撃元がやれらた場合は実行しない
							break;
						if(attackPlanIsStarter.get(i)) //重複された攻撃ではないことを確認する
							setBulletByWeapon(attackPlanWeapon[i],playerX,playerY,playerX - playerOldX,playerY - playerOldY,atan2(focusY - defaultScreenH/2,focusX - defaultScreenW/2),PLAYER,0,PLAYER);
						else //重複攻撃によるものである場合、それ以上の攻撃計画を建てない
							setBulletByWeapon_inside(attackPlanWeapon[i],playerX,playerY,playerX - playerOldX,playerY - playerOldY,atan2(focusY - defaultScreenH/2,focusX - defaultScreenW/2),PLAYER,0,PLAYER);
						break;
					case ENEMY: //敵
						if(enemyKind[attackerID] == NONE) //攻撃元がやれらた場合は実行しない
							break;
						if(attackPlanIsStarter.get(i))
							setBulletByWeapon(attackPlanWeapon[i],enemyX[attackerID],enemyY[attackerID],enemyX[attackerID] - enemyOldX[attackerID],enemyY[attackerID] - enemyOldY[attackerID],enemyAngle[attackerID],ENEMY,attackerID,ENEMY);
						else
							setBulletByWeapon_inside(attackPlanWeapon[i],enemyX[attackerID],enemyY[attackerID],enemyX[attackerID] - enemyOldX[attackerID],enemyY[attackerID] - enemyOldY[attackerID],enemyAngle[attackerID],ENEMY,attackerID,ENEMY);
						break;
					case ENTITY: //エンティティ ※entityOldX&Y未実装につき、慣性適用が未実装
						if(entityKind[attackerID] == NONE) //攻撃元がやれらた場合は実行しない
							break;
						if(attackPlanIsStarter.get(i))
							setBulletByWeapon(attackPlanWeapon[i],entityX[attackerID],entityY[attackerID],entityX[attackerID] - entityOldX[attackerID],entityY[attackerID] - entityOldY[attackerID],entityAngle[attackerID],ENTITY,attackerID,entityTeam[attackerID]);
						else
							setBulletByWeapon_inside(attackPlanWeapon[i],entityX[attackerID],entityY[attackerID],entityX[attackerID] - entityOldX[attackerID],entityY[attackerID] - entityOldY[attackerID],entityAngle[attackerID],ENTITY,attackerID,entityTeam[attackerID]);
						break;
					case BULLET: //弾
						if(bulletKind[attackerID] == NONE) //攻撃元がやれらた場合は実行しない
							break;
						if(attackPlanIsStarter.get(i))
							setBulletByWeapon(attackPlanWeapon[i],bulletX[attackerID],bulletY[attackerID],bulletX[attackerID] - bulletOldX[attackerID],bulletY[attackerID] - bulletOldY[attackerID],bulletAngle[attackerID],BULLET,attackerID,bulletTeam[attackerID]);
						else
							setBulletByWeapon_inside(attackPlanWeapon[i],bulletX[attackerID],bulletY[attackerID],bulletX[attackerID] - bulletOldX[attackerID],bulletY[attackerID] - bulletOldY[attackerID],bulletAngle[attackerID],BULLET,attackerID,bulletTeam[attackerID]);
						break;
					default:
						System.out.println("unknown attack plan: " + attackPlanID[i]/TYPE_SEPARATOR*TYPE_SEPARATOR);
					}
					attackPlanFrame[i] = MAX; //削除する
				}
			}
		///////////////////////////////////////////////////////////////////////////////////////
			actionUsedTime = System.currentTimeMillis();
			g2.setFont(basicFont);
			if(playerLife > 0 && nearestEnemyDistance != NONE){ //死んでいない&レーダー反応あり
				if(DIRECTION_RADAR){ //付近の敵の方向を赤い矢印で表示
					final double angle = nearestEnemyAngle;
					g2.rotate(angle,playerX,playerY);
					drawImageBS_centerDot(radarImg,playerX,playerY);
					g2.rotate(-angle,playerX,playerY);
				}
				if(SUSPECT_RADAR){ //危険な敵に気づかれたときに青いマーカーが赤く変化
					g2.setStroke(stroke1);
					if(suspect){
						g2.setColor(Color.RED);
						if(nowFrame % 10 < 5)
							g2.setStroke(stroke3);
					}else
						g2.setColor(Color.BLUE);
					final int radius;
					if(nowFrame/4%40 >= 20)
						radius = nowFrame/4%20 + 90;
					else
						radius = 110 - nowFrame/4%20;
					g2.drawArc(playerX - radius/2,playerY - radius/2,radius,radius,nowFrame*10,50);
					g2.drawArc(playerX - radius/2,playerY - radius/2,radius,radius,nowFrame*10+180,50);
				}
			}
			g2.translate(-tX,-tY);
			//影処理
			g2.setColor(Color.BLACK);
			lightDraw:{
				g2_shadow.setColor(Color.BLACK);
				g2_shadow.setComposite(AlphaComposite.Src);
				g2_shadow.fill(screenRect);
				g2_shadow.setComposite(AlphaComposite.DstOut);
				int size,halfSize;
				for(int i = 0;i < lightTotal;i++){
					size = lightness[i];halfSize = size/2;
					g2_shadow.drawImage(lightImg,lightX[i] - halfSize,lightY[i] - halfSize,size,size,this);
				}
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setComposite(AlphaComposite.SrcOver.derive(1.0f - stageLuminance));
				drawImageBS(shadowLayer,0,0,defaultScreenW,defaultScreenH);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
				lightTotal = 0;
			}
			g2.setComposite(AlphaComposite.SrcOver);
			if(playerLife > 0){
				//照準描画部
				g2.setStroke(stroke10);
				if(ammoHave <= 0 && weaponRemaining[weapon] <= 0) //所持弾薬および弾倉内に弾がない
					drawImageBS_centerDot(noAmmoFocus,focusX,focusY);
				else{
					g2.setColor(reloadGaugeColor);
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
					if(reloadTime[slot] != 0){ //リロード中
						g2.drawArc(focusX - 70,focusY - 70,140,140,nowFrame*10,50);
						g2.drawArc(focusX - 70,focusY - 70,140,140,nowFrame*10+180,50);
						g2.drawArc(focusX - 55,focusY - 55,110,110,95,(int)((double)reloadTime[slot]/(double)cfg.weaponReloadTime[weapon]*360D));
						g2.setColor(Color.WHITE);
						g2.drawString("Reload",focusX - 27,focusY - 13);
						g2.drawString(String.valueOf((int)((double)reloadTime[slot]/(double)cfg.weaponReloadTime[weapon]*100)) + "%",focusX - 15,focusY + 13);
					}else{ //平常時
						if(bulletSwitch) //発砲中
							drawImageBS_centerDot(focusImg,focusX + random2(3),focusY + random2(3));
						else
							drawImageBS_centerDot(focusImg,focusX,focusY);
						g2.drawArc(focusX - 55,focusY - 55,110,110,95,(int)((double)weaponRemaining[weapon]/(double)cfg.weaponMagazineSize[weapon]*360D));
						if(ammoHave == 0 && nowFrame % 10 < 5 && nowFrame - reloadedFrame[slot] < 70){ //ラストマガジンアラート
							g2.setColor(Color.YELLOW);
							g2.drawString("last ammo",focusX - 40,focusY - 40);
						}
					}
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
				}
				g2.setColor(Color.WHITE);
				String str;
				if(weaponRemaining[weapon] == MAX) //無限弾薬
					str = "999";
				else
					str = String.valueOf(weaponRemaining[weapon]);
				g2.drawString(str,focusX - 30 - g2.getFontMetrics().stringWidth(str),focusY - 30);
				if(ammoHave == MAX)
					g2.drawString("999",focusX + 30,focusY - 30);
				else
					g2.drawString(String.valueOf(ammoHave),focusX + 30,focusY-30);
				//発言描画部
				if(playerComment == "まずい！もう使えないぞ！" && ammoHave == 0 && weaponRemaining[weapon] == 0 || nowFrame - playerCommentFrame < 100 && playerComment != ""){
					g2.setFont(commentFont);
					final int length = g2.getFontMetrics().stringWidth(playerComment);
					drawImageBS(messegeBox,(defaultScreenW - length)/2 - 5,defaultScreenH/2 + 80,length + 10,messegeBox.getHeight(null));
					g2.drawString(playerComment,(defaultScreenW - length)/2,defaultScreenH/2 + 95);
				}else
					playerComment = "";
			}
			//ステージ開始演出描画部
			if(gameTime < 4000){
				if(gameTime < 3200){
					g2.setFont(commentFont.deriveFont(20F));
					if(gameTime < 3000) //~3000
						g2.drawString(stageName[stage],500 - g2.getFontMetrics().stringWidth(stageName[stage])/2,280);
					else{ //3000~3200
						g2.setColor(new Color(1.0F,1.0F,1.0F,(3200 - gameTime)*0.005F));
						g2.drawString(stageName[stage],500 - g2.getFontMetrics().stringWidth(stageName[stage])/2,280);
					}
				}else{ //3200~4000
					g2.setFont(commentFont.deriveFont(40F + (gameTime - 3200)/20F));
					g2.setColor(new Color(0.3F,0.3F,0.3F,(4000 - gameTime)*0.00125F));
					g2.drawString("潜入開始！",500 - g2.getFontMetrics().stringWidth("潜入開始！")/2,320);
				}
			} //4000~
			//HPゲージ描画部
			drawImageBS(statusTrayImg,0,defaultScreenH - statusTrayImg.getHeight(null));
			g2.setFont(basicFont);
			//所持金
			g2.setColor(Color.YELLOW);
			g2.drawString("$" + money,900,40);
			//HPゲージ
			if(playerLife > 0){
				//左下のHPゲージ
				g2.setStroke(stroke5);
				if((double)playerHP/(double)hp_max > 0.75) //HPが少なくなるごとに色が変化
					g2.setColor(highHPColor); //水色
				else if((double)playerHP/(double)hp_max > 0.50)
					g2.setColor(Color.GREEN); //緑色
				else if((double)playerHP/(double)hp_max > 0.25)
					g2.setColor(Color.YELLOW); //黄色
				else if((double)playerHP/(double)hp_max > 0.10 || nowFrame % 4 < 2)
					g2.setColor(Color.RED); //赤色
				else
					g2.setColor(HPWarningColor);
				g2.drawString(String.valueOf(playerHP),playerHP >= 10 ? 31 : 37,defaultScreenH - 40);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.drawArc(3,defaultScreenH - 86,82,82,90,(int)((double)playerHP/(double)hp_max*360));
				//自機周囲のHPゲージ
				final int hpChangedDuration = nowFrame - playerHPChangedFrame; //HPが変化してからのフレーム数
				if(hpChangedDuration < 60){ //60フレームの間自機周囲のHPゲージをフェードアウト
					g2.setStroke(stroke3);
					final Color nowColor = g2.getColor();
					g2.setColor(new Color(nowColor.getRed(),nowColor.getGreen(),nowColor.getBlue(),min(255,255 - hpChangedDuration*255/60)));
					g2.drawArc(defaultScreenW/2 - 41,defaultScreenH/2 - 41,82,82,90,(int)((double)playerHP/(double)hp_max*360));
				}
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			}
			//残機
			g2.setColor(Color.GREEN);
			g2.drawString(String.valueOf(playerLife),4,596);
			//回復エネルギー残量
			g2.drawString("+" + cureStore,70,defaultScreenH - 60);
			//武器スロット描画部
			g2.setColor(Color.WHITE);
			for(int i = 0;i < weaponSlot_max;i++){
				final int weaponID = weaponSlot[i];
				if(0 <= weaponID && weaponID < weaponIconImg.length){ //空欄ではない
					final Image img = weaponIconImg[weaponID];
					final int x = 100 + i*50,
						imgW = img.getWidth(null),imgH = img.getHeight(null);
					if(imgW > imgH){ //画像の横が長い場合
						final int imgH2 = (int)(imgH*(50D/imgW));
						drawImageBS(img,x,550 + (50 - imgH2)/2,50,imgH2);
					}else{ //画像の縦が長い場合
						final int imgW2 = (int)(imgW*(50D/imgH));
						drawImageBS(img,x + (50 - imgW2)/2,550,imgW2,50);
					}
					final int ammo = itemPossession[cfg.weaponAmmoKind[weaponID]];
					if(ammo == MAX) //無限弾薬
						g2.drawString("999",x,defaultScreenH - 50);
					else
						g2.drawString(String.valueOf(ammo + weaponRemaining[weaponSlot[i]]),x,defaultScreenH - 50);
				}
			}
			//番号表示
			g2.setColor(Color.BLACK);
			for(int i = 0;i < weaponSlot_max;i++)
				g2.drawString(String.valueOf(i+1),135 + i*50,defaultScreenH - 5);
			drawImageBS(focusImg2,(50*slot+100),defaultScreenH - 50,50,50);
			if(0 <= weapon && weapon < cfg.weaponKindTotal){
				//選択した武器の名前を表示
				final int weaponChangedDuration = nowFrame - weaponChangedFrame;
				if(weaponChangedDuration < 50){
					g2.setFont(commentFont);
					if(weaponChangedDuration < 30)
						g2.setColor(Color.WHITE);
					else
						g2.setColor(new Color(1F,1F,1F,(50 - weaponChangedDuration)*0.05F));
					final String name = cfg.weaponName[weapon];
					g2.drawString(name,125 + slot*50 - g2.getFontMetrics().stringWidth(name)/2,defaultScreenH - 80);
					g2.setFont(basicFont);
				}
				//リロード進行率表示
				for(int i = 0;i < weaponSlot_max;i++){
					if(reloadTime[i] > 0){
						g2.setColor(weaponReloadingColor);
						g2.fillRect(100 + 50*i,defaultScreenH - 50,50,50);
						g2.setColor(weaponReloadedColor);
						final int reloadRate = (int)((double)reloadTime[i]/(double)cfg.weaponReloadTime[weaponSlot[i]]*50);
						g2.fillRect(100 + 50*i,defaultScreenH - 50 + reloadRate,50,50 - reloadRate);
						g2.setColor(Color.BLACK);
						g2.drawString(String.valueOf(reloadRate*2) + "%",105 + 50*i,defaultScreenH - 20);
					}
				}
			}
			//クリア記録とランキング登録
			if(mainScore == maxScore && playerLife > 0){
				if(clearFrame == NONE)
					clearFrame = nowFrame;
				else if(nowFrame - clearFrame > 200){
					weaponSE[weapon].stop();
					rankingRecord:{
						final int totalScore = mainScore - damageTaken * 5;
						int ranking = 15;
						while(ranking > 0 && totalScore - gameTime/10000000.0 > rankingScore[ranking - 1] - rankingTime[ranking - 1]/10000000.0)
							ranking--;
						if(ranking == 15)
							break rankingRecord;
						for(int i = 14;i > ranking;i--){
							rankingName[i] = rankingName[i - 1];
							rankingScore[i] = rankingScore[i - 1];
							rankingTime[i] = rankingTime[i - 1];
							rankingDate[i] = rankingDate[i - 1];
							rankingStage[i] = rankingStage[i - 1];
						}
						rankingName[ranking] = System.getProperty("user.name");
						rankingScore[ranking] = totalScore;
						rankingTime[ranking] = gameTime / 1000;
						rankingDate[ranking] = new SimpleDateFormat("M/d HH:mm").format(new Date());
						rankingStage[ranking] = stageName[stage];
						StringBuilder sb = new StringBuilder();
						for(int i = 0;;i++){
							if(rankingName[i] == null)
								sb.append("-----");
							else{
								sb.append(rankingName[i]).append("\r\n");
								sb.append(rankingScore[i]).append("\r\n");
								sb.append(rankingStage[i]).append("\r\n");
								sb.append(rankingTime[i]).append("\r\n");
								sb.append(rankingDate[i]);
							}
							if(i < rankingName.length - 1){
								sb.append("\r\n");
							}else
								break;
						}
						try(BufferedWriter bw = new BufferedWriter(new FileWriter("ScoreRanking.txt"))){
							bw.write(sb.toString());
							bw.flush();
						}catch(IOException e){}
					}
					resetData();
				}
				drawImageBS_centerDot(_clear_,defaultScreenW/2,defaultScreenH/2);
			}
			//ゲームオーバー演出描画部
			if(playerLife == 0){
				if(gameOverFrame <= 200){
					drawImageBS_centerDot(_gameOver_,defaultScreenW/2,defaultScreenH/2);
					gameOverFrame++;
				}else //200フレーム後、タイトルへ戻る
					resetData();
			}
			//スコア
			g2.setColor(Color.WHITE);
			g2.drawString("score: " + String.format("%1$06d",mainScore) + "  (" + DF00_00.format((double)mainScore/(double)maxScore*100) + "%)",21,40);
			//デバッグモードパラメータ描画部
			if(debugMode){
				g2.setColor(weaponReloadingColor);
				g2.setStroke(stroke5);
				for(int i = 100;i < defaultScreenW;i += 100)
					g2.drawLine(i,0,i,defaultScreenH);
				for(int i = 100;i < defaultScreenH;i += 100)
					g2.drawLine(0,i,defaultScreenW,i);
				g2.setColor(weaponReloadedColor);
				g2.drawString(focusX + "," + focusY,focusX + 20,focusY + 20);
				g2.drawString("EM:" + enemy_total + " ET:" + entity_total + " G:" + gimmick_total + " EF:" + effect_total + " B:" + bullet_total + " I:" + item_total,30,100);
				g2.drawString("LoadTime(ms):" + loadTime_total,30,120);
				g2.drawString("EM:" + loadTime_enemy + " ET:" + loadTime_entity + " G:" + loadTime_gimmick + " EF:" + loadTime_effect + " B:" + loadTime_bullet + " I:" + loadTime_item + " W: " + loadTime_weapon + " Other: " + loadTime_other,30,140);
				g2.drawString("GameTime(ms):" + gameTime,30,160);
				g2.setColor(reloadGaugeColor);
				g2.drawString("(" + realFocusX + "," + realFocusY + ")",focusX + 20,focusY + 40);
			}
			//背景描写
			int BGStartX = tX,BGStartY = tY;
			final int BGW = BGLayer.getWidth(null),BGH = BGLayer.getHeight(null);
			if(playerX - defaultScreenW/2 < 700){
				g.fillRect(0,0,700 + tX,defaultScreenH);
				BGStartX += 700;
			}else if(playerX + defaultScreenW/2 > stageW - 700){
				g.fillRect(stageW - 700 + tX,0,defaultScreenW,defaultScreenH);
				BGStartX += stageW - 700 - BGW;
			}else
				BGStartX += 700 + (playerX - defaultScreenW/2 - 700)/200*200;
			if(playerY - defaultScreenH/2 < 700){
				g.fillRect(0,0,defaultScreenW,700 + tY);
				BGStartY += 700;
			}else if(playerY + defaultScreenH/2 > stageH - 700){
				g.fillRect(0,stageH - 700 + tY,defaultScreenW,defaultScreenH);
				BGStartY += stageH - 700 - BGH;
			}else
				BGStartY += 700 + (playerY - defaultScreenH/2 - 700)/200*200;
			g.drawImage(BGLayer,BGStartX,BGStartY,BGW,BGH,this);
			loadTime_other = System.currentTimeMillis() - actionUsedTime;
		}else if(nowEvent == OPENING){
			if(nowFrame > 220){
				if(nowFrame >= 260)
					eventChange(TITLE);
				else{
					g2.setComposite(AlphaComposite.Src);
					for(int i = 0;i < 10;i++){
						for(int j = 0;j < 7;j++)
							drawImageBS(titleBackImg,i*100,j*100,100,100);
					}
					g2.setComposite(AlphaComposite.SrcOver);
					drawImageBS(titleImg,(defaultScreenW - titleImg.getWidth(null))/2,0);
					g2.setComposite(AlphaComposite.SrcOver.derive((nowFrame - 220)*0.025F));
					final int size = 1300 - (nowFrame - 220)*20;
					final double angle = (double)nowFrame/10D;
					g2.rotate(angle,750,350);
					drawImageBS(bigFocus,750 - size/2,350 - size/2,size,size);
					g2.rotate(-angle,750,350);
					g2.setComposite(AlphaComposite.SrcOver);
					g2.setColor(new Color(0F,0F,0F,1F - (nowFrame - 220)*0.025F));
					g2.fill(screenRect);
					drawImageBS(button_start_,(nowFrame - 220)*15 - 500,200,400,100);
					drawImageBS(button_guide_,(nowFrame - 220)*15 - 500,320,400,100);
					drawImageBS(button_option_,(nowFrame - 220)*15 - 500,440,400,100);
				}
			}else{
				g2.setComposite(AlphaComposite.Src);
				g2.setColor(Color.BLACK);
				g2.fill(screenRect);
				g2.setComposite(AlphaComposite.SrcOver);
				if(75 < nowFrame && nowFrame <= 100){
					g2.setComposite(AlphaComposite.SrcOver.derive((nowFrame - 75)*0.04F));
					drawImageBS_centerDot(rogoImg,defaultScreenW/2,defaultScreenH/2);
				}else if(100 < nowFrame && nowFrame <= 150){
					drawImageBS_centerDot(rogoImg,defaultScreenW/2,defaultScreenH/2);
				}else if(150 < nowFrame && nowFrame < 175){
					g2.setComposite(AlphaComposite.SrcOver.derive(1F - (nowFrame - 150)*0.04F));
					drawImageBS_centerDot(rogoImg,defaultScreenW/2,defaultScreenH/2);
				}
			}
		}else if(nowEvent == TITLE){ //タイトル画面
			g2.setComposite(AlphaComposite.Src);
			for(int i = 0;i < 10;i++){
				for(int j = 0;j < 7;j++)
					drawImageBS(titleBackImg,i*100,j*100,100,100);
			}
			g2.setComposite(AlphaComposite.SrcOver);
			drawImageBS(titleImg,(defaultScreenW - titleImg.getWidth(null))/2,0);
			final double angle = 26 + (double)(nowFrame - 260)/50D;
			g2.rotate(angle,750,350);
			drawImageBS(bigFocus,500,100,500,500); //右にある大きな照準アニメーション
			g2.rotate(-angle,750,350);
			drawImageBS(rogoImg,defaultScreenW - rogoImg.getWidth(null),defaultScreenH - rogoImg.getHeight(null)); //右下にあるbluelaserpointerロゴ
			if(focusing == STAGE_SELECT){ //以下、それぞれのボタン(スタート、遊び方、設定)がカーソルを載せたときに明るく表示する処理
				drawImageBS(activeButton_start,100,200,400,100);
				drawImageBS(button_guide_,100,320,400,100);
				drawImageBS(button_option_,100,440,400,100);
			}else if(focusing == GUIDE){
				drawImageBS(button_start_,100,200,400,100);
				drawImageBS(activeButton_guide,100,320,400,100);
				drawImageBS(button_option_,100,440,400,100);
			}else if(focusing == OPTION){
				drawImageBS(button_start_,100,200,400,100);
				drawImageBS(button_guide_,100,320,400,100);
				drawImageBS(activeButton_option,100,440,400,100);
			}else{
				drawImageBS(button_start_,100,200,400,100);
				drawImageBS(button_guide_,100,320,400,100);
				drawImageBS(button_option_,100,440,400,100);
			}
			if(singlePlayMode)
				drawImageBS(button_singleMode_,460,300,150,40);
			else
				drawImageBS(button_multiMode_,460,300,150,40);
			g2.setFont(commentFont);
			g2.setColor(Color.GRAY);
			g2.drawString("※未実装",400,418);
		}else if(nowEvent == GUIDE){ //遊び方
			g2.setComposite(AlphaComposite.Src);
			g2.setColor(Color.WHITE);
			g2.fill(screenRect);
			g2.setComposite(AlphaComposite.SrcOver);
			String message = "";
			g2.setFont(commentFont.deriveFont(20F));
			g2.setColor(Color.WHITE);
			switch(page){
			case 0: //１ページ目
				drawImageBS(guidePageImgs[0][0],130,50);
				drawImageBS(guidePageImgs[0][1],510,170);
				drawImageBS(guidePageImgs[0][2],550,300);
				g2.setColor(Color.GRAY);
				g2.fillRect(0,500,1000,200);
				g2.setColor(Color.LIGHT_GRAY);
				g2.setStroke(stroke3);
				g2.drawRect(0,500,1000,200);
				drawImageBS(button_return_,0,550,150,50);
				g2.setColor(Color.WHITE);
				message = "WASDキー、もしくは十字キーで移動します。SHIFTキー押しながらでスニークができます。";
				if(nowFrame % 2 == 0 && textZone < message.length())
					textZone++;
				g2.drawString(message.substring(0,textZone),10,530);
				break;
			case 1: //２ページ目
				drawImageBS(guidePageImgs[1][0],100,90);
				drawImageBS(guidePageImgs[1][1],510,160);
				drawImageBS(guidePageImgs[1][2],360,340);
				drawImageBS(guidePageImgs[1][3],540,310);
				g2.setColor(Color.GRAY);
				g2.fillRect(0,500,1000,200);
				g2.setColor(Color.LIGHT_GRAY);
				g2.setStroke(stroke3);
				g2.drawRect(0,500,1000,200);
				drawImageBS(button_return_,0,550,150,50);
				g2.setColor(Color.WHITE);
				message = "マウスを動かして狙いを定めます。";
				if(nowFrame % 2 == 0 && textZone < message.length())
					textZone++;
				g2.drawString(message.substring(0,textZone),10,530);
				break;
			case 2: //３ページ目
				drawImageBS(guidePageImgs[2][0],200,130);
				drawImageBS(guidePageImgs[2][1],500,170);
				drawImageBS(guidePageImgs[2][2],340,370);
				drawImageBS(guidePageImgs[2][3],550,310);
				g2.setColor(Color.GRAY);
				g2.fillRect(0,500,1000,200);
				g2.setColor(Color.LIGHT_GRAY);
				g2.setStroke(stroke3);
				g2.drawRect(0,500,1000,200);
				drawImageBS(button_return_,0,550,150,50);
				g2.setColor(Color.WHITE);
				message = "左クリックで発砲です。多くの武器は、長押しすると連射ができます。";
				if(nowFrame % 2 == 0 && textZone < message.length())
					textZone++;
				g2.drawString(message.substring(0,textZone),10,530);
				break;
			case 3: //４ページ目
				drawImageBS(guidePageImgs[3][0],40,150);
				drawImageBS(guidePageImgs[3][1],510,170);
				drawImageBS(guidePageImgs[3][2],610,280);
				g2.setColor(Color.GRAY);
				g2.fillRect(0,500,1000,200);
				g2.setColor(Color.LIGHT_GRAY);
				g2.setStroke(stroke3);
				g2.drawRect(0,500,1000,200);
				drawImageBS(button_return_,0,550,150,50);
				g2.setColor(Color.WHITE);
				message = "数字キー、もしくはマウスローラーで武器を切り替えることができます。";
				if(nowFrame % 2 == 0 && textZone < message.length())
					textZone++;
				g2.drawString(message.substring(0,textZone),10,530);
				break;
			case 4: //５ページ目
				drawImageBS(guidePageImgs[4][0],100,80);
				drawImageBS(guidePageImgs[4][1],510,170);
				g2.setColor(Color.GRAY);
				g2.fillRect(0,500,1000,200);
				g2.setColor(Color.LIGHT_GRAY);
				g2.setStroke(stroke3);
				g2.drawRect(0,500,1000,200);
				drawImageBS(button_return_,0,550,150,50);
				g2.setColor(Color.WHITE);
				message = "最後に照準についてです。左上にある数字が装填された弾数、右上にある数字がまだ所持している弾数です。";
				if(nowFrame % 2	== 0 && textZone < message.length())
					textZone++;
				g2.drawString(message.substring(0,textZone),10,530);
				break;
			}
		}else if(nowEvent == MULTIPLAY_RINKROOM || nowEvent == MULTIPLAY_MAKEROOM){
			g2.setColor(Color.GRAY);
			g2.fillRect(300,100,650,400);
			g2.setColor(Color.WHITE);
			g2.setFont(commentFont.deriveFont(30F));
			g2.drawString("部屋の名前：",320,120);
			g2.drawString("ゲームの種類：",320,170);
			g2.drawString("使用マップ：",320,220);
			g2.drawString("BGM：",320,270);
			drawImageBS(button_return_,0,550,150,50);
			if(focusInArea(0,550,150,50)){
				g2.setStroke(stroke10);
				g2.drawRect(0,550,150,50);
			}
		}else if(nowEvent == MULTIPLAY_TEAMWAITING){
		}else if(nowEvent == PAUSE_MENU){
			g2.clearRect(0,0,defaultScreenW,defaultScreenH);
			drawImageBS(button_returnGame_,300,150,400,100);
			drawImageBS(button_shop_,300,300,400,100);
			g2.setFont(commentFont);
			g2.setColor(Color.GRAY);
			g2.drawString("※未実装",640,415);
			drawImageBS(button_title_,300,450,400,100);
			g2.setColor(Color.WHITE);
			g2.setStroke(stroke10);
			switch(focusing){
			case BATTLE:
				g2.drawRect(300,150,400,100);
				break;
			case SHOP:
				g2.drawRect(300,300,400,100);
				break;
			case TITLE:
				g2.drawRect(300,450,400,100);
			}
		}else if(nowEvent == STAGE_SELECT){ //ステージ選択画面
			g2.setComposite(AlphaComposite.Src);
			g2.setStroke(stroke10);
			for(int i = 0;i < 10;i++){
				for(int j = 0;j < 7;j++)
					drawImageBS(titleBackImg,i*100,j*100,100,100);
			}
			g2.setComposite(AlphaComposite.SrcOver);
			drawImageBS(titleImg,(defaultScreenW - titleImg.getWidth(null))/2,0);
			g2.setFont(commentFont);
			g2.setColor(Color.WHITE);
			for(int i = 0;i < stageName.length;i++){ //ステージボタン描画
				drawImageBS(basicButton,100,stagePage + 60*i,400,50);
				g2.drawString(stageName[i],110,50 - 20 + stagePage + 60*i);
			}
			drawImageBS(button_return_,0,550,150,50); //戻るボタン描画
			//スクロール処理
			stagePage += (int)pagePower;
			if(stagePage < page_min){
				stagePage = page_min;
				pagePower = 0.0;
			}else if(page_max < stagePage){
				stagePage = page_max;
				pagePower = 0.0;
			}else
				pagePower *= 0.8;
			if(focusInArea(0,550,150,50)) //戻るボタンを選択
				g2.drawRect(0,550,150,50);
			else if(focusing == BATTLE){ //ステージボタンを選択
				//ボタン強調表示
				g2.drawRect(100,stagePage + 60*stage,400,50);
				//右側ステージ詳細欄
				g2.setColor(new Color(145,215,55,40));
				g2.fillRect(580,50,390,500);
				g2.setColor(reloadGaugeColor);
				g2.drawString("stage " + stageName[stage],585,65);
				g2.setColor(Color.WHITE);
				g2.setFont(commentFont);
			}
		}else if(nowEvent == EQUIPMENT_SELECT){ //装備選択画面
			g2.setComposite(AlphaComposite.Src);
			for(int i = 0;i < 10;i++){
				for(int j = 0;j < 7;j++)
					drawImageBS(titleBackImg,i*100,j*100,100,100);
			}
			g2.setComposite(AlphaComposite.SrcOver);
			drawImageBS(titleImg,(defaultScreenW - titleImg.getWidth(null))/2,0);
			int focusTile = page*10; //表示中のタイル番号
			for(int i = 0;i < 2;i++){ //２行x５列の武器タイル
				for(int j = 0;j < 5;j++,focusTile++){
					if(focusTile >= weaponArsenal.length) //武器総数を越したマス目は空欄にする
						drawImageBS(noWeaponTile,300 + j*130,100 + i*130,120,120);
					else{
						drawImageBS(weaponTile,300 + j*130,100 + i*130,120,120);
						final Image img = weaponIconImg[weaponArsenal[focusTile]];
						final int imgW = img.getWidth(null),imgH = img.getHeight(null);
						if(imgW > imgH)
							drawImageBS(img,310 + j*130,110 + i*130 + (100 - (int)(imgH*(100D/imgW)))/2,100,(int)(imgH*(100D/imgW)));
						else
							drawImageBS(img,310 + j*130 + (100 - (int)(imgW*(100D/imgH)))/2,110 + i*130,(int)(imgW*(100D/imgH)),100);
					}
				}
			}
			drawImageBS(statusTrayImg,0,defaultScreenH - statusTrayImg.getHeight(null));
			//武器スロット描画部(画面左下)
			g2.setColor(Color.WHITE);
			for(int i = 0;i < weaponSlot_max;i++){
				if(0 <= weaponSlot[i] && weaponSlot[i] < weaponIconImg.length){
					final Image img = weaponIconImg[weaponSlot[i]];
					final int x = 100 + i*50;
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					if(imgW > imgH){
						final int imgH2 = (int)(imgH*(50D/imgW));
						drawImageBS(img,x,550 + (50 - imgH2)/2,50,imgH2);
					}else{
						final int imgW2 = (int)(imgW*(50D/imgH));
						drawImageBS(img,x + (50 - imgW2)/2,550,imgW2,50);
					}
				}
				g2.drawString(String.valueOf(i+1),135 + i*50,defaultScreenH - 5);
			}
			//ボタン
			drawImageBS(button_return_,30,420,150,50);
			g2.setStroke(stroke10);
			if(focusInArea(30,420,150,50))
				g2.drawRect(30,420,150,50);
			drawImageBS(buttonPrev,540,360,50,40);
			drawImageBS(buttonNext,650,360,50,40);
			drawImageBS(button_ready_,650,420,240,60);
		}else if(nowEvent == SHOP){
			g2.clearRect(0,0,defaultScreenW,defaultScreenH);
			g2.setFont(commentFont);
			g2.setColor(Color.WHITE);
			g2.drawString(cfg.weaponName[shopPage],40,40);
			g2.fillRect(10,280,20,40);
			g2.fillRect(970,280,20,40);
			g2.setFont(basicFont);
			g2.setColor(Color.YELLOW);
			g2.drawString("$" + money,900,40);
			g2.setColor(Color.WHITE);
			g2.drawString(String.valueOf(shopPage),493,550);
			drawImageBS(button_return_,0,550,150,50);
			if(focusInArea(0,550,150,50)){
				g2.setStroke(stroke10);
				g2.drawRect(0,550,150,50);
			}
			g2.setColor(weaponReloadedColor);
			g2.fillRect(100,50,800,250);
			drawImageBS_centerDot(weaponIconImg[shopPage],500,175);
			g2.setColor(Color.WHITE);
			g2.setFont(commentFont);
			g2.drawString("威力",100,344);
			g2.drawString("精度",100,364);
			g2.drawString("連射",100,384);
			g2.drawString("弾速",100,404);
			g2.setStroke(stroke1);
			g2.drawRect(139,329,401,18);
			g2.drawRect(139,349,401,18);
			g2.drawRect(139,369,401,18);
			g2.drawRect(139,389,401,18);
			g2.setColor(Color.RED);
			final int weaponKind = weaponSlot[shopPage];
			Strength:{
				final int level = 10 - cfg.weaponUPG1Limit[weaponKind];
				for(int i = 0;i < level;i++)
					g2.fillRect(140 + i*40,330,38,17);
			}
			Aberration:{
				if(cfg.weaponAberration[weaponKind] == 0.0){ //ブレと関係がない武器
					g2.setColor(Color.LIGHT_GRAY);
					g2.fillRect(140,350,400,17);
					g2.setColor(Color.RED);
				}else{
					final int level = 10 - cfg.weaponUPG2Limit[weaponKind];
					for(int i = 0;i < level;i++)
						g2.fillRect(140 + i*40,350,38,17);
				}
			}
			FireRate:{
				final int level = 10 - cfg.weaponUPG3Limit[weaponKind];
				for(int i = 0;i < level;i++)
					g2.fillRect(140 + i*40,370,38,17);
			}
			BulletSpeed:{
				if(cfg.weaponBulletSpeed[weaponKind] == NONE){ //弾を発射するものではない武器
					g2.setColor(Color.LIGHT_GRAY);
					g2.fillRect(140,390,400,17);
					g2.setColor(Color.RED);
				}else{
					final int level = 10 - cfg.weaponUPG4Limit[weaponKind];
					for(int i = 0;i < level;i++)
						g2.fillRect(140 + i*40,390,38,17);
				}
			}
			g2.setFont(basicFont);
			g2.setColor(Color.WHITE);
			g2.drawString(String.valueOf(cfg.weaponStrength[weaponKind]),543,345);
			if(cfg.weaponAberration[weaponKind] == 0.0)
				g2.drawString("-",543,365);
			else
				g2.drawString(String.valueOf(DF00_00.format(cfg.weaponAberration[weaponKind])),543,365);
			g2.drawString(String.valueOf(cfg.weaponFireRate[weaponKind]),543,385);
			if(cfg.weaponBulletSpeed[weaponKind] == NONE)
				g2.drawString("-",543,405);
			else
				g2.drawString(String.valueOf(cfg.weaponBulletSpeed[weaponKind]),543,405);
			drawImageBS(upgradeButton,595,330,18,18);
			drawImageBS(upgradeButton,595,350,18,18);
			drawImageBS(upgradeButton,595,370,18,18);
			drawImageBS(upgradeButton,595,390,18,18);
		}else if(nowEvent == OPTION){
			g2.setComposite(AlphaComposite.Src);
			for(int i = 0;i < 10;i++){
				for(int j = 0;j < 7;j++)
					drawImageBS(titleBackImg,i*100,j*100,100,100);
			}
			g2.setComposite(AlphaComposite.SrcOver);
			drawImageBS(titleImg,(defaultScreenW - titleImg.getWidth(null))/2,0);
			g2.setStroke(stroke3);
			g2.setColor(Color.WHITE);
			g2.setFont(commentFont);
			g2.drawString("音量調整",190,160);
			g2.drawLine(200,225,700,225);
			g2.fillRect(195 + (int)(musicVolume*500D),200,10,50);
			g2.setColor(Color.WHITE);
			g2.fillRect(200,340,50,50);
			g2.fillRect(200,420,50,50);
			g2.fillRect(200,500,50,50);
			g2.setColor(Color.GREEN);
			switch(focusing){
			case SCREEN_SIZE1:
				g2.drawRect(200,340,50,50);
				break;
			case SCREEN_SIZE2:
				g2.drawRect(200,420,50,50);
				break;
			case SCREEN_SIZE3:
				g2.drawRect(200,500,50,50);
			}
			switch(screenW){
			case 800:
				drawImageBS(checkedSymbol,195,335);
				break;
			case 1000:
				drawImageBS(checkedSymbol,195,415);
				break;
			case 1200:
				drawImageBS(checkedSymbol,195,495);
			}
			g2.setFont(commentFont);
			g2.drawString("画面サイズ800:480",280,370);
			g2.drawString("画面サイズ1000:600",280,450);
			g2.drawString("画面サイズ1200:720",280,530);
			drawImageBS(button_ranking_,600,340,250,80);
			drawImageBS(button_return_,0,550,150,50);
			if(focusInArea(600,340,250,80)) //ランキングボタン
				g2.drawRect(600,340,250,80);
			else if(focusInArea(0,550,150,50)){ //戻るボタン
				g2.setStroke(stroke10);
				g2.drawRect(0,550,150,50);
			}
		}else if(nowEvent == RANKING){
			g2.setComposite(AlphaComposite.Src);
			g2.setColor(Color.GRAY);
			g2.fill(screenRect);
			g2.setComposite(AlphaComposite.SrcOver);
			drawImageBS(_ranking_,280,0);
			g2.setFont(commentFont);
			g2.setColor(Color.BLACK);
			g2.drawString("|名前 | スコア | 所要時間 | ステージ | 日付 |",70,150);
			for(int i = 0;i < 10;i++){
				if(rankingName[i] == null){
					g2.drawString("---------------------------------------------------",70,180 + i*30);
					continue;
				}
				String tmp;
				g2.drawString(String.valueOf(rankingName[i]),70,180 + i*30);
				g2.drawString(tmp = String.valueOf(rankingScore[i]),230 - tmp.length()*7,180 + i*30);
				g2.drawString(tmp = String.valueOf(rankingTime[i]),290 - tmp.length()*7,180 + i*30);
				g2.drawString(tmp = rankingStage[i],360 - tmp.length()*7,180 + i*30);
				g2.drawString(String.valueOf(rankingDate[i]),400,180 + i*30);
			}
			g2.drawString("|名前 | スコア | 所要時間 | ステージ | 日付 |",570,150);
			g2.setStroke(stroke3);
			g2.drawLine(500,150,500,450);
			for(int i = 0;i < 5;i++){
				if(rankingName[i + 10] == null){
					g2.drawString("---------------------------------------------------",570,180 + i*30);
					continue;
				}
				String tmp;
				g2.drawString(String.valueOf(rankingName[i + 10]),570,180 + i*30);
				g2.drawString(tmp = String.valueOf(rankingScore[i + 10]),730 - tmp.length()*7,180 + i*30);
				g2.drawString(tmp = String.valueOf(rankingTime[i + 10]),790 - tmp.length()*7,180 + i*30);
				g2.drawString(tmp = rankingStage[i + 10],860 - tmp.length()*7,180 + i*30);
				g2.drawString(String.valueOf(rankingDate[i + 10]),900,180 + i*30);
			}
			drawImageBS(button_return_,0,550,150,50);
			if(focusInArea(0,550,150,50)){
				g2.setStroke(stroke10);
				g2.drawRect(0,550,150,50);
			}
		}
		g.drawImage(offImage,(int)xVibration,(int)yVibration,screenW,screenH,this);
		if(abs(xVibration) > 1)
			xVibration *= -0.8;
		else
			xVibration = 0;
		if(abs(yVibration) > 1)
			yVibration *= -0.8;
		else
			yVibration = 0;
		loadTime_total = System.currentTimeMillis() - repaintTime;
	}
	
	//アクションイベント系
	int slashed,slashKind = NONE; //近接攻撃処理slashKind
	int focusX,focusY; //ウィンドウ内ポイント座標(マウス座標)
	int realFocusX,realFocusY; //ステージ内ポイント座標
	boolean dragging;
	final int PUNCH = 1;
	public void mouseWheelMoved(MouseWheelEvent e){
		if(nowEvent == BATTLE){ //マウスホイールで武器切り替えができる
			int select = slot + e.getWheelRotation();
			if(select < 0 || weaponSlot_max <= select)
				return;
			if(e.getWheelRotation() > 0){
				for(int i = 0;i < weaponSlot_max;i++,select++){
					select %= weaponSlot_max;
					if(weaponSlot[select] != NONE){
						weaponChangeBySlot(select);
						break;
					}
				}
			}else{
				for(int i = 0;i < weaponSlot_max;i++,select--){
					if(select < 0)
						select = weaponSlot_max-1;
					if(weaponSlot[select] != NONE){
						weaponChangeBySlot(select);
						break;
					}
				}
			}
		}
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent ev){
		if(nowEvent == OPENING || nowEvent == 0)
			return;
		if(nowEvent == BATTLE){
			if(playerLife > 0){
				if(focusY > screenH - 50 && 100 < focusX && focusX < 400)
					weaponChangeBySlot((focusX - 100)/50);
				else{
					bulletSwitch = true;
					if(cfg.weaponActionType[weapon] == SWORD && weaponRemaining[weapon] > 0 && slashKind == NONE){
						slashed = 5;
						slashKind = SWORD;
					}
				}
			}
		}else{
			if(focusing != NONE){
				switch(nowEvent){
				case STAGE_SELECT: //ステージ選択画面
					if(focusing == TITLE)
						eventChange(TITLE);
					else if(focusing == BATTLE){
						if(!new File("source/stage/" + stageName[stage] + ".ini").exists()){
							warningBox("ステージの読み込みに失敗しました","警告");
							eventChange(STAGE_SELECT);
							return;
						}
						eventChange(EQUIPMENT_SELECT);
					}
					break;
				case EQUIPMENT_SELECT: //武器選択画面
					switch(focusing){
					case TITLE:
						eventChange(TITLE);
						focusing = NONE;
						break;
					case BATTLE:
						//ステージ読み込み処理
						//loadStage///////////
						//ファイル確認
						Properties data = new Properties();
						try{
							data.load(new InputStreamReader(getClass().getResourceAsStream("source/stage/" + stageName[stage] + ".ini")));
						}catch(IOException e){
							JOptionPane.showMessageDialog(null,"ステージの読み込みにエラーが発生しました。\nエラーコード：" + e,"エラー",JOptionPane.ERROR_MESSAGE);
							eventChange(STAGE_SELECT);
							return;
						}
						//前処理
						resetData();
						eventChange(BATTLE);
						if(cfg.weaponKindTotal > 0){
							boolean weaponNotSelected = true;
							for(int i = 0;i < weaponSlot_max;i++){
								if(weaponSlot[i] == NONE)
									continue;
								//初期装備武器の選択
								if(weaponNotSelected){
									weapon = weaponSlot[slot = i];
									weaponChangedFrame = nowFrame;
									weaponNotSelected = false;
								}
								//リロード、残弾関連情報を初期化
								final int weaponKind = weaponSlot[i];
								weaponRemaining[weaponKind] = cfg.weaponMagazineSize[weaponKind];
								reloadedFrame[i] = reloadTime[i] = 0;
							}
							ammoHave = itemPossession[cfg.weaponAmmoKind[weapon]];
						}
						//ステージ寸法読み込み
						final String stageGridW_str = data.getProperty("stageGridW"),
							stageGridH_str = data.getProperty("stageGridH");
						if(isActualString(stageGridW_str) && isActualString(stageGridH_str)){
							stageGridW = Integer.parseInt(stageGridW_str);
							stageGridH = Integer.parseInt(stageGridH_str);
						}else{
							stageGridW = 50;
							stageGridH = 50;
						}
						stageW = stageGridW*100;
						stageH = stageGridH*100;
						stageGridTotal = stageGridW*stageGridH;
						//マップ関係配列初期化(ステージ寸法に依存するため、resetDataメソッド内では行えない)
						gimmickKind = new int[stageGridTotal];
						gimmickHP = new int[stageGridTotal];
						gimmickSweeper = new int[stageGridTotal];
						Arrays.fill(gimmickKind,NONE);
						xPatrolTargetMap = new int[stageGridTotal];
						yPatrolTargetMap = new int[stageGridTotal];
						Arrays.fill(xPatrolTargetMap,NONE);
						xForceMap = new int[stageGridTotal];
						yForceMap = new int[stageGridTotal];
						damageMap = new int[stageGridTotal];
						enemyMap = new BitSet(stageGridTotal);
						//スタート地点の読み込み
						final String[] spawnPointStr = split2(data.getProperty("spawnPoint"),",");
						if(isActualString(spawnPointStr)){
							spawnPointX = new int[spawnPointStr.length];
							spawnPointY = new int[spawnPointStr.length];
							for(int i = 0;i < spawnPointStr.length;i++){
								int spawnPoint = Integer.parseInt(spawnPointStr[i]);
								spawnPointX[i] = spawnPoint / stageW;
								spawnPointY[i] = spawnPoint % stageW;
							}
							playerX = spawnPointX[random(0,spawnPointX.length - 1)];
							playerY = spawnPointY[random(0,spawnPointY.length - 1)];
						}else{ //スタート地点情報なし※異常
							spawnPointX = spawnPointY = new int[]{900}; //暫定スタート地点
							playerX = playerY = 900;
						}
						//敵情報の読み込み
						for(EnemyListener ver : cfg.enemyClass)
							ver.loadStarted(); //ロード開始追加処理
						String[] data1 = split2(data.getProperty("enemyKind"),","), //種類
							data2 = split2(data.getProperty("enemyLocation"),","), //座標
							data3 = split2(data.getProperty("enemyAngle"),","), //向き
							data4 = split2(data.getProperty("enemyProperty"),","); //その他の固有情報
						if(isActualString(data1)){
							enemy_total = data1.length; //総数
							enemy_maxID = data1.length - 1; //最大ID
							for(int i = 0;i < data1.length;i++){
								enemyKind[i] = cfg.convertID_enemy(data1[i]); //種類
								final int position = Integer.parseInt(data2[i]); //XY圧縮座標
								enemyX[i] = enemyOldX[i] = enemyTargetX[i] = enemyNextTargetX[i] = position/stageH; //x座標を現座標、旧座標、第１目標座標、第２目標座標に代入
								enemyY[i] = enemyOldY[i] = enemyTargetY[i] = enemyNextTargetY[i] = position%stageH; //y座標
								enemyHP[i] = cfg.enemyHP[enemyKind[i]]; //HP
								enemyFoundMe.clear(i); //プレイヤー発見状態
								enemyAngle[i] = enemyTargetAngle[i] = Double.parseDouble(data3[i]); //向き,目標向き
								enemyNextAngle[i] = NONE; //第２目標向き
								enemyXPower[i] = enemyYPower[i] = 0; //動力
								enemyWound[i] = enemyShotFrame[i] = 0; //スタン値と射撃レート計算用変数
								enemySightCuttingGrid[i] = NONE; //最後の遮蔽地点
								maxScore += cfg.enemyScore[enemyKind[i]]; //スコア最大値加算
								if(i < data4.length)
									cfg.enemyClass[enemyKind[i]].created(i,data4[i]);
							}
							for(EnemyListener ver : cfg.enemyClass)
								ver.loadFinished(); //ロード終了追加処理
						}else{ //データなし
							enemy_total = 0;
							enemy_maxID = -1;
						}
						
						//巡回点の読み込み
						data1 = split2(data.getProperty("patrolPoint"),","); //座標
						data2 = split2(data.getProperty("patrolTarget"),","); //巡回先座標
						data3 = split2(data.getProperty("patrolDelay"),","); //巡回停留時間(未実装)
						if(isActualString(data1)){
							for(int i = 0;i < data1.length;i++){
								final int location = Integer.parseInt(data1[i]),target = Integer.parseInt(data2[i]);
								xPatrolTargetMap[location] = target/stageGridH; //x方向の移動先
								yPatrolTargetMap[location] = target%stageGridH; //y方向の移動先
							}
						}
						
						//ギミックの読み込み
						for(GimmickListener ver : cfg.gimmickClass)
							ver.loadStarted(); //ロード開始追加処理
						data1 = split2(data.getProperty("gimmickKind"),","); //種類
						data2 = split2(data.getProperty("gimmickLocation"),","); //座標
						data3 = split2(data.getProperty("gimmickProperty"),","); //内部データ
						if(isActualString(data1)){
							gimmick_total = data1.length; //総数
							//(最大IDは保存しない)
							for(int i = 0;i < data1.length;i++){
								final int kind = cfg.convertID_gimmick(data1[i]), //種類
									map = Integer.parseInt(data2[i]); //XY圧縮座標(1/100マップ座標)(IDとしても扱う)
								gimmickKind[map] = kind;
								gimmickHP[map] = cfg.gimmickHP[kind];
								damageMap[map] = cfg.gimmickDamage[kind];
								//ギミックスイーパー作成処理
								if(gimmickHP[map] > 0){
									int fillGrid;
									final int gridW = map/stageGridH;
									final int gridH = map%stageGridH;
									int xs = max(gridW - 1,0);
									final int ys = max(gridH - 1,0);
									final int XE = min(gridW + 1,stageGridW - 1);
									final int YE = min(gridH + 1,stageGridH - 1);
									for(;xs <= XE;xs++){
										for(int ys_ = ys;ys_ <= YE;ys_++){
											fillGrid = xs*stageGridH + ys_;
											if(0 <= fillGrid && fillGrid < stageGridTotal)
												gimmickSweeper[fillGrid]++;
										}
									}
								}
								if(i < data3.length)
									cfg.gimmickClass[kind].created(map,data3[i]);
							}
							for(GimmickListener ver : cfg.gimmickClass)
								ver.loadFinished(); //ロード終了追加処理
						}else //データなし
							gimmick_total = 0;
						
						//エンティティの読み込み
						for(EntityListener ver : cfg.entityClass)
							ver.loadStarted(); //ロード開始追加処理
						data1 = split2(data.getProperty("entityKind"),","); //種類
						data2 = split2(data.getProperty("entityLocation"),","); //座標
						data3 = split2(data.getProperty("entityAngle"),","); //内部データ
						data4 = split2(data.getProperty("entityProperty"),","); //内部データ
						if(isActualString(data1)){
							entity_total = data1.length; //総数
							entity_maxID = data1.length - 1; //最大ID
							for(int i = 0;i < data1.length;i++){
								entityKind[i] = cfg.convertID_entity(data1[i]); //種類
								final int location = Integer.parseInt(data2[i]); //位置情報(圧縮情報)
								entityX[i] = location/stageH; //x座標
								entityY[i] = location%stageH; //y座標
								entityXPower[i] = entityYPower[i] = 0.0; //動力
								entityHP[i] = cfg.entityHP[entityKind[i]];
								if(i < data3.length)
									entityAngle[i] = Double.parseDouble(data3[i]); //角度
								if(i < data4.length)
									cfg.entityClass[entityKind[i]].created(i,data4[i]);
							}
						}else{ //データなし
							entity_total = 0;
							entity_maxID = -1;
						}
						
						//アイテムの読み込み
						data1 = split2(data.getProperty("itemKind"),",");
						data2 = split2(data.getProperty("itemLocation"),",");
						data3 = split2(data.getProperty("itemAmount"),",");
						if(isActualString(data1)){
							item_maxID = data1.length - 1;
							item_total = data1.length;
							for(int i = 0;i < data1.length;i++){
								itemKind[i] = Integer.parseInt(data1[i]); //種類
								final int location = Integer.parseInt(data2[i]); //位置情報
								itemX[i] = location/stageH; //x座標分解
								itemY[i] = location%stageH; //y座標分解
								itemAmount[i] = Integer.parseInt(data3[i]);
							}
						}else{
							item_maxID = -1;
							item_total = 0;
						}
						stageLuminance = Float.parseFloat(data.getProperty("stageLuminance")); //ステージ光度の設定
						titleBGM.stop(); //タイトルBGMのストップ
						battleBGM[nowBattleBGM = rnd.nextInt(bgm_max)].loop(); //ランダムに戦闘曲を選び、番号を記録して、再生
						focusing = NONE; //マウスオーバー情報をクリア
						//ステージ背景レイヤーの更新
						final int groundPaintsW = defaultScreenW/200 + 2
							,groundPaintsH = defaultScreenH/200 + 2;
						for(int i = 0;i < groundPaintsW;i++){
							for(int j = 0;j < groundPaintsH;j++)
								g2_BG.drawImage(background[0],i*200,j*200,200,200,this);
						}
						break;
					case PREV:
						if(page > 0)
							page--;
						break;
					case NEXT:
						if(page < page_max)
							page++;
						break;
					default:
						if(focusing == oldEvents.getFirst())
							eventChange(focusing);
						else{
							weaponTaking_Return:{ //武器の出し入れ
								for(int i = 0;i < weaponSlot_max;i++){
									if(weaponSlot[i] == focusing){ //取り外し
										weaponSlot[i] = NONE;
										break weaponTaking_Return;
									}
								}
								for(int i = 0;i < weaponSlot_max;i++){
									if(weaponSlot[i] == NONE){ //取り付け
										weaponSlot[i] = focusing;
										break;
									}
								}
							}
						}
					}
					break;
				case PAUSE_MENU: //バトルポーズメニュー画面
					switch(focusing){
					case BATTLE:
						eventChange(BATTLE);
						break;
					case TITLE:
						gameOverFrame = 0;
						resetData();
						break;
					case SHOP:
						eventChange(SHOP);
						break;
					}
					break;
				case TITLE: //タイトル画面
					switch(focusing){
					case STAGE_SELECT:
						eventChange(STAGE_SELECT);
						focusing = NONE;
						break;
					case GUIDE:
						eventChange(GUIDE);
						page = 0;
						page_max = guidePageImgs.length;
						textZone = 0;
						focusing = NONE;
						break;
					case OPTION:
						eventChange(OPTION);
						focusing = NONE;
						break;
					}
					break;
				case SHOP:
					switch(focusing){
					case PAUSE_MENU:
						eventChange(PAUSE_MENU);
						focusing = NONE;
						break;
					case NEXT:
						if(shopPage < weaponSlot_max - 1)
							shopPage++;
						break;
					case PREV:
						if(shopPage > 0)
							shopPage--;
						break;
					case shopUpgrade1:
						if(cfg.weaponUPG1Limit[weaponSlot[shopPage]] > 0 && money > cfg.weaponCost[shopPage] * 0.1){
							cfg.weaponUPG1Limit[weaponSlot[shopPage]]--;
							cfg.weaponStrength[shopPage] *= 1.1;
							cfg.weaponStrength[shopPage]++;
							money -= cfg.weaponCost[shopPage] * 0.1;
						}
						break;
					case shopUpgrade2:
						if(cfg.weaponUPG2Limit[weaponSlot[shopPage]] > 0 && money > cfg.weaponCost[shopPage] * 0.1){
							cfg.weaponUPG2Limit[weaponSlot[shopPage]]--;
							cfg.weaponAberration[shopPage] *= 0.9;
							money -= cfg.weaponCost[shopPage] * 0.1;
						}
						break;
					case shopUpgrade3:
						if(cfg.weaponUPG3Limit[weaponSlot[shopPage]] > 0 && money > cfg.weaponCost[shopPage] * 0.1){
							cfg.weaponUPG3Limit[weaponSlot[shopPage]]--;
							cfg.weaponFireRate[shopPage] *= 0.9;
							cfg.weaponFireRate[shopPage]--;
							money -= cfg.weaponCost[shopPage] * 0.1;
						}
						break;
					case shopUpgrade4:
						if(cfg.weaponUPG4Limit[weaponSlot[shopPage]] > 0 && money > cfg.weaponCost[shopPage] * 0.1){
							cfg.weaponUPG4Limit[weaponSlot[shopPage]]--;
							cfg.weaponBulletSpeed[shopPage] += 2;
							money -= cfg.weaponCost[shopPage] * 0.1;
						}
						break;
					}
					break;
				case OPTION:
					if(focusing == TITLE){
						eventChange(TITLE);
						focusing = NONE;
					}else if(focusing == VOLUME_LINE || focusing == VOLUME_CONTROLER){
						musicVolume = (focusX - 200)/500D;
						titleBGM.setVolume(musicVolume);
						for(int i = 0;i < bgm_max;i++)
							battleBGM[i].setVolume(musicVolume);
					}else if(focusing == RANKING){
						eventChange(RANKING);
						focusing = NONE;
					}else{
						switch(focusing){
							case SCREEN_SIZE1:
								screenW = 800;screenH = 480;
								myFrame.setSize(806,508);
								break;
							case SCREEN_SIZE2:
								screenW = 1000;screenH = 600;
								myFrame.setSize(1006,628);
								break;
							case SCREEN_SIZE3:
								screenW = 1200;screenH = 720;
								myFrame.setSize(1206,748);
						}
					}
					break;
				case GUIDE:
					if(focusing == NEXT){
						if(++page >= page_max)
							eventChange(TITLE);
						textZone = 0;
					}else if(focusing == PREV){
						if(--page < 0)
							eventChange(TITLE);
						textZone = 0;
					}
					break;
				case RANKING:
					if(focusing == oldEvents.getFirst()){
						eventChange(focusing);
						focusing = NONE;
					}
					break;
				}
			}
			switch(focusing){
			case 1:
				if(money > 1500)
					DIRECTION_RADAR = true;
			case 2:
				if(money > 1300)
					HP_RADAR = true;
			case 3:
				if(money > 1500)
					SUSPECT_RADAR = true;
			case 11:
				if(money > playerSpeed*300)
					playerSpeed++;
			case 12:
				if(money > 10000)
					no_bodylock = true;
			}
		}
	}
	public void mouseReleased(MouseEvent e){
		bulletSwitch = false;
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseMoved(MouseEvent e){
		focusX = e.getX()*defaultScreenW/screenW; //スクリーンの大きさに合わせてマウス位置を算出する
		focusY = e.getY()*defaultScreenH/screenH;
	}
	final void mouseMoveAction(){
		focusing = NONE;
		final int x = focusX,y = focusY;
		switch(nowEvent){
		case TITLE:
			if(100 < x && x < 500){
				if(200 < y && y < 300)
					focusing = STAGE_SELECT;
				else if(320 < y && y < 420)
					focusing = GUIDE;
				else if(440 < y && y < 540)
					focusing = OPTION;
			}
			break;
		case STAGE_SELECT: //ステージ選択画面
			if(x < 150 && y > 550)
				focusing = oldEvents.getFirst();
			/*else if(50 < x && x < 550 && 50 < y && y < 550)
				focusing = (x - 50)/100 + (y - 50)/100*5;*/
			else if(x < 550){
				if(y < defaultScreenH/2 - 175) //画面上部スクロール範囲にマウスがある
					pagePower = min(pagePower + abs(defaultScreenH/2 + 175 - y)/25.0,20.0);
				else if(defaultScreenH/2 + 175 < y) //画面下部にマウスがある
					pagePower = max(pagePower - abs(defaultScreenH/2 - 175 - y)/25.0,-20.0);
				for(int i = 0;i < stageName.length;i++){
					if(abs(60*i + 30 + stagePage - y) < 25){
						focusing = BATTLE;
						stage = i;
						break;
					}
				}
			}
			break;
		case EQUIPMENT_SELECT:
			if(focusInArea(30,420,160,50))
				focusing = oldEvents.getFirst();
			else if(focusInArea(650,420,240,60))
				focusing = BATTLE;
			else if(focusInArea(300,100,620,230)){ //武器選択欄
				try{
					if(100 < y && y < 200 && (x - 300) % 130 < 100) //１段目
						focusing = weaponArsenal[(x - 300)/130 + page*10];
					else if(230 < y && y < 330 && (x - 300) % 130 < 100) //２段目
						focusing = weaponArsenal[(x - 300)/130 + 5 + page*10];
				}catch(ArrayIndexOutOfBoundsException e2){
					focusing = NONE;
				}
			}else if(focusInArea(540,360,50,40)){
				focusing = PREV;
			}else if(focusInArea(650,360,700,40)){
				focusing = NEXT;
			}else if(focusY > screenH - 50 && 100 < focusX && focusX < 400){ //武器スロット(画面左下)
				final int select = (focusX - 100)/50;
				if(weaponSlot[select] != NONE)
					focusing = weaponSlot[select];
			}
			break;
		case SHOP:
			if(x < 150 && y > 550)
				focusing = oldEvents.getFirst();
			else if(585 < x && x < 903){
				if(330 < y && y < 348)
					focusing = shopUpgrade1;
				else if(350 < y && y < 368)
					focusing = shopUpgrade2;
				else if(370 < y && y < 388)
					focusing = shopUpgrade3;
				else if(390 < y && y < 408)
					focusing = shopUpgrade4;
			}else if(280 < y && y < 320){
				if(10 < x && x < 30)
					focusing = PREV;
				else if(970 < x && x < 990)
					focusing = NEXT;
			}
			break;
		case GUIDE:
			if(x < 150 && y > 550){
				focusing = PREV;
			}else
				focusing = NEXT;
			break;
		case OPTION:
			if(x < 150 && y > 550)
				focusing = oldEvents.getFirst();
			else if(190 + musicVolume*5 < x && x < 210 + musicVolume*5 && 200 < y && y < 250)
				focusing = VOLUME_CONTROLER;
			else if(200 < x && x < 700 && 200 < y && y < 250)
				focusing = VOLUME_LINE;
			else if(200 < x && x < 250){
				if(340 < y && y < 390)
					focusing = SCREEN_SIZE1;
				else if(420 < y && y < 470)
					focusing = SCREEN_SIZE2;
				else if(500 < y && y < 550)
					focusing = SCREEN_SIZE3; //600,340,250,80
			}else if(600 < x && x < 850 && 340 < y && y < 420)
				focusing = RANKING;
			break;
		case PAUSE_MENU:
			if(300 < x && x < 700){
				if(150 < y && y < 250)
					focusing = BATTLE;
				else if(300 < y && y < 400)
					focusing = SHOP;
				else if(450 < y && y < 550)
					focusing = TITLE;
			}
			break;
		case RANKING:
			if(x < 150 && y > 550)
				focusing = oldEvents.getFirst();
		}
	}
	public void mouseDragged(MouseEvent e){
		focusX = e.getX()*defaultScreenW/screenW;
		focusY = e.getY()*defaultScreenH/screenH;
		if(focusing == VOLUME_LINE || focusing == VOLUME_CONTROLER){
			musicVolume = (focusX - 200)/500D;
			if(musicVolume < 0.0)
				musicVolume = 0.0;
			else if(musicVolume > 1.0)
				musicVolume = 1.0;
			titleBGM.setVolume(musicVolume);
			for(int i = 0;i < bgm_max;i++)
				battleBGM[i].setVolume(musicVolume);
		}
	}
	//公開キーイベント
	KeyEvent keyPressEvent;
	int keyPressFrame;
	KeyEvent keyReleaseEvent;
	int keyReleaseFrame;
	KeyEvent keyTypeEvent;
	int keyTypeFrame;
	public void keyPressed(KeyEvent e){
		keyPressEvent = e;
		keyPressFrame = nowFrame;
		switch(e.getKeyCode()){
		case VK_UP:
		case VK_W:
			upKey = true;
			if(nowEvent == STAGE_SELECT)
				pagePower += 15;
			break;
		case VK_LEFT:
		case VK_A:
			leftKey = true;
			break;
		case VK_DOWN:
		case VK_S:
			downKey = true;
			if(nowEvent == STAGE_SELECT)
				pagePower -= 15;
			break;
		case VK_RIGHT:
		case VK_D:
			rightKey = true;
			break;
		case VK_R: //リロード処理
			if(reloadTime[slot] == 0 && weaponRemaining[weapon] != cfg.weaponMagazineSize[weapon] && ammoHave > 0){
				final int ammoDemand = cfg.weaponMagazineSize[weapon] - weaponRemaining[weapon];
				if(ammoDemand <= ammoHave)
					reloadTime[slot] = (int)((double)cfg.weaponReloadTime[weapon]*(double)weaponRemaining[weapon]/(double)cfg.weaponMagazineSize[weapon]);
				else
					reloadTime[slot] = (int)((double)cfg.weaponReloadTime[weapon]*(1.0 - (double)ammoHave/(double)cfg.weaponMagazineSize[weapon]));
				if(reloadTime[slot] <= 0)
					reloadTime[slot] = 1;
				reloadSE.stop();
				reloadSE.play();
				if(cfg.weaponSEIsSerial.get(weapon))
					weaponSE[weapon].stop();
			}
			break;
		case VK_SPACE:
			actioned = true;
			if(slashKind == NONE && playerHP > 0){
				slashed = 5;
				slashKind = PUNCH;
				punchSE.stop();
				punchSE.play();
			}
			break;
		case VK_SHIFT:
			sneak = true;
			break;
		case VK_ESCAPE:
		case VK_P:
			if(nowEvent == PAUSE_MENU)
				eventChange(BATTLE);
			else if(nowEvent == BATTLE)
				eventChange(PAUSE_MENU);
			break;
		case VK_F3: //デバッグモード、グリッドと全オブジェクトの情報概略を表示
			debugMode = !debugMode;
			break;
		case VK_F5: //画面フリーズモード、スクリーンショットをとったり、細かいシーンの解析をするのに使う。処理も止まる。
			screenShot = !screenShot;
			break;
		case VK_F9: //コンフィグ&MOD再読み込み、タイトル画面時のみ有効
			if(nowEvent == TITLE)
				loadConfig(false);
			break;
		}
	}
	public void keyReleased(KeyEvent e){
		keyReleaseEvent = e;
		keyReleaseFrame = nowFrame;
		switch(e.getKeyCode()){
		case VK_UP:
		case VK_W:
			upKey = false;
			break;
		case VK_LEFT:
		case VK_A:
			leftKey = false;
			break;
		case VK_DOWN:
		case VK_S:
			downKey = false;
			break;
		case VK_RIGHT:
		case VK_D:
			rightKey = false;
			break;
		case VK_SPACE:
			actioned = false;
			break;
		case VK_SHIFT:
			sneak = false;
		}
	}
	public void keyTyped(KeyEvent e){
		keyTypeEvent = e;
		keyTypeFrame = nowFrame;
		if(nowEvent == BATTLE){
			try{
				weaponChangeBySlot(Integer.parseInt(String.valueOf(e.getKeyChar())) - 1);
			}catch(NumberFormatException e2){}
		}
	}
	class MyWindowAdapter extends WindowAdapter{ //ウィンドウ処理
		public void windowClosing(WindowEvent e){
			System.exit(0);
		}
		public void windowDeactivated(WindowEvent e){
			if(nowEvent == BATTLE && !screenShot){ //ステージ中にウィンドウがアクティブでなくなったとき,またはF5が押されたときは自動的にポーズ
				eventChange(PAUSE_MENU);
				upKey = leftKey = downKey = rightKey = false;
			}
		}
	}
	
	boolean actioned; //スペースキーを押したか
	final void playerAction(){ //プレイヤー基本処理
		if(playerHP <= 0 && playerLife > 0){
			playerDeathSE.play();
			weaponSE[weapon].stop(); //銃声ストップ
			playerRidingID = NONE; //乗り物エンティティから降りる
			playerXPower = playerYPower = 0.0;
			if(playerLife == 1){ //ゲームオーバー処理
				playerLife = 0;
				playerHP = playerTargetHP = 0;
				gameOverFrame = 0;
				slashed = 0;
				slashKind = NONE;
				playerOldX = playerX;
				playerOldY = playerY;
				return;
			}
			playerHP = 0;
			playerTargetHP = hp_max;
			playerLife--;
			damageTaken++;
			playerBarrierTime = playerBarrierTime_default;
			respawnFrame = nowFrame;
		}else{
			if(playerBarrierTime > 0)
				playerBarrierTime--;
			if(playerXPower < -1.6 || 1.6 < playerXPower)
				playerXPower *= 0.8;
			else
				playerXPower = 0;
			if(playerYPower < -1.6 || 1.6 < playerYPower)
				playerYPower *= 0.8;
			else
				playerYPower = 0;
		}
		//自動回復
		if(nowFrame - playerHPChangedFrame > cureRate && cureStore > 0 && playerTargetHP < hp_max){
			cureStore--;
			playerTargetHP++;
			playerHPChangedFrame = nowFrame;
		}
		//環境被害
		final int mapDamage = damageMap[playerX/100*stageGridH + playerY/100];
		if(mapDamage != 0) //バリア状態でも強調HPゲージを表示して警告
			playerHPChangedFrame = nowFrame;
		if(playerBarrierTime <= 0 && mapDamage > 0){ //バリア状態のみ下降ダメージを受ける
			playerTargetHP -= mapDamage;
		}
		//HP変動
		if(playerHP < playerTargetHP)
			playerHP++;
		else if(playerHP > playerTargetHP){
			if(playerBarrierTime <= 0) //無敵状態ではない
				playerHP--;
			else
				playerTargetHP = playerHP;
		}
		playerAngle = atan2(focusY - defaultScreenH/2,focusX - defaultScreenW/2);
		cos_playerAngle = cos(playerAngle);
		sin_playerAngle = sin(playerAngle);
		//マルチプレイ処理
		//multiCombatBW.
	}
	boolean sneak; //スニーク移動中か
	final void playerMove(){
		if(playerRidingID != NONE) //プレイヤーが何かに乗っている(操縦している)時、処理放棄
			return;
		//旧座標更新
		playerOldX = playerX;
		playerOldY = playerY;
		int playerTargetX = playerX + (int)playerXPower, //目標x座標
			playerTargetY = playerY + (int)playerYPower; //目標y座標
		//環境動力
		final int map = playerX/100*stageGridH + playerY/100;
		if(0 <= map && map < stageGridTotal){
			playerTargetX += xForceMap[map];
			playerTargetY += yForceMap[map];
		}
		//移動動力の反映
		final int speed = sneak ? playerSpeed/2 : playerSpeed; //速度(スニーク時半減)
		if(leftKey)
			playerTargetX -= speed;
		else if(rightKey)
			playerTargetX += speed;
		if(upKey)
			playerTargetY -= speed;
		else if(downKey)
			playerTargetY += speed;
		//位置修正
		if(playerTargetX < 700)
			playerTargetX = 700;
		else if(stageW - 700 < playerTargetX)
			playerTargetX = stageW - 700;
		if(playerTargetY < 700)
			playerTargetY = 700;
		else if(stageH - 700 < playerTargetY)
			playerTargetY = stageH - 700;
		//衝突判定１ ギミック・ステージ外
		if(squareHitGimmick(playerTargetX,playerY,playerSize)) //x移動先ブロック判定
			playerTargetX = playerX;
		if(squareHitGimmick(playerX,playerTargetY,playerSize)) //y移動先ブロック判定
			playerTargetY = playerY;
		if(playerTargetX != playerX && playerTargetY != playerY){ //斜め移動
			if(squareHitGimmick(playerTargetX,playerTargetY,playerSize)){ //斜め方向の移動で壁に埋まるのを回避
				playerTargetX = playerX;
				playerTargetY = playerY;
			}else{ //斜め移動速度修正
				playerTargetX = (int)((playerTargetX - playerX)/1.4) + playerX;
				playerTargetY = (int)((playerTargetY - playerY)/1.4) + playerY;
			}
		}
		//衝突判定２ 敵
		boolean canGoTargetX = (playerTargetX != playerX), //x方向移動判定が必要
				canGoTargetY = (playerTargetY != playerY); //y方向移動判定が必要
		for(int j = 0;j <= enemy_maxID && (canGoTargetX || canGoTargetY);j++){ //どちらの判定も必要なくなったら終了
			if(enemyKind[j] == NONE)
				continue;
			final int x = (int)enemyX[j],y = (int)enemyY[j]; //敵座標
			final int size = cfg.enemySize[enemyKind[j]]; //敵サイズ
			if(canGoTargetX && abs(x - playerTargetX) < (size + playerSize)/2 && abs(y - playerY) < (size + playerSize)/2)
				canGoTargetX = false;
			if(canGoTargetY && abs(x - playerX) < (size + playerSize)/2 && abs(y - playerTargetY) < (size + playerSize)/2)
				canGoTargetY = false;
		}
		//代入処理
		if(canGoTargetX)
			playerX = playerTargetX;
		if(canGoTargetY)
			playerY = playerTargetY;
	}
	final void playerAttack(){
		if(!actionSwitch)
			return;
		if(slashed > 0)
			slashed--;
		else
			slashKind = NONE;
		//汎用変数用意
		final int weapon = this.weapon;
		if(weapon < 0 || cfg.weaponKindTotal <= weapon) //weaponが範囲外である例外を回避
			return;
		final int magazineSize = cfg.weaponMagazineSize[weapon]; //弾倉内容量
		int ammoRemaining = weaponRemaining[weapon]; //弾倉内残量
		ammoHave = itemPossession[cfg.weaponAmmoKind[weapon]]; //所持弾薬量
		if(reloadTime[slot] >= cfg.weaponReloadTime[weapon]){ //リロード完了処理
			reloadTime[slot] = 0;
			reloadedFrame[slot] = nowFrame;
			playerComment = "";
			int ammoDemand = magazineSize - ammoRemaining; //要求弾薬量
			if(ammoDemand > ammoHave){ //装填したい弾数が所持量を上回る
				ammoRemaining += ammoHave;
				if(ammoHave != MAX)
					ammoHave = 0;
			}else{ //ふつうに装填する
				ammoRemaining = magazineSize;
				if(ammoHave != MAX)
					ammoHave -= ammoDemand;
			}
		}else if(ammoRemaining <= 0){ //リロード処理
			ammoRemaining = 0;
			if(ammoHave == 0){ //所持弾薬量なし
				if(cfg.weaponSEIsSerial.get(weapon))
					weaponSE[weapon].stop();
				if(playerComment.isEmpty()){
					playerCommentFrame = nowFrame;
					playerComment = "まずい！もう使えないぞ！";
				}
			}else if(ammoHave > 0){ //所持弾薬量あり
				if(reloadTime[slot] == 0){
					reloadSE.stop();
					reloadSE.play();
					playerCommentFrame = nowFrame;
					if(cfg.weaponActionType[weapon] == SWORD)
						playerComment = "剣が鈍くなった";
					else{
						switch(random(1,5)){
						case 1:
							playerComment = "落ち着け、まだ弾はあるさ";
							break;
						case 2:
							playerComment = "もう弾切れか！";
							break;
						case 3:
							playerComment = "まずいな、リロードしよう";
							break;
						case 4:
							playerComment = "来んなよ！今装填中なんだ！";
							break;
						case 5:
							playerComment = "弾倉が空だ";
							break;
						}
					}
					if(cfg.weaponSEIsSerial.get(weapon))
						weaponSE[weapon].stop();
				}
				if(upKey || leftKey || downKey || rightKey)
					reloadTime[slot]++;
				else //移動しないと装填速度が上がる
					reloadTime[slot] += 3;
			}
		}else if(reloadTime[slot] > 0){ //リロード中(残弾がある中での)
			if(upKey || leftKey || downKey || rightKey)
				reloadTime[slot]++;
			else //移動しないと装填速度が上がる
				reloadTime[slot] += 3;
		}else if(bulletSwitch){ //発砲処理
			if(nowFrame - shotFrame[slot] >= cfg.weaponFireRate[weapon] && reloadTime[slot] == 0){
				//射撃フレームを記録
				shotFrame[slot] = nowFrame;
				//武器の設定に基づき弾を設置
				setBulletByWeapon(weapon,playerX,playerY,playerX - playerOldX,playerY - playerOldY,playerAngle,PLAYER,0,PLAYER);
				//光源設置
				setLight(playerX,playerY,300);
				//残弾減少
				if(magazineSize != MAX)
					ammoRemaining--;
				//効果音の再生
				if(cfg.weaponSEIsSerial.get(weapon)){ //連射速度が高い武器は効果音がつぶれるので、別処理になる
					if(!weaponSE[weapon].isRunning())
						weaponSE[weapon].loop();
				}else{
					weaponSE[weapon].stop();
					weaponSE[weapon].play();
				}
			}
		}else if(cfg.weaponSEIsSerial.get(weapon))
			weaponSE[weapon].stop();
		
		//変数同期
		weaponRemaining[weapon] = ammoRemaining;
		itemPossession[cfg.weaponAmmoKind[weapon]] = ammoHave;
	}
	
	//イベント制御系
	final void eventChange(int event){
		if(oldEvents.size() > 0 && event == oldEvents.getFirst()){ //一つ前のイベントへ戻る
			oldEvents.removeFirst(); //先頭の要素を削除
		}else //新イベントへ進む
			oldEvents.addFirst(nowEvent); //先頭へ要素を追加
		nowEvent = event;
		if(event == STAGE_SELECT){
			try{
				stageName = new File(getClass().getResource("source/stage").toURI()).list(new FilenameFilter(){
																							public boolean accept(File dir,String name){
																								return name.endsWith(".ini");
																							}
																						});
				for(int i = 0;i < stageName.length;i++)
					stageName[i] = stageName[i].substring(0,stageName[i].length() - 4);
			}catch(URISyntaxException e){
				stageName = new String[0];
			}catch(NullPointerException e){
				System.out.println("ステージデータの取得に失敗しました。");
			}
			page_min = -(stageName.length - 1)*60;
			page_max = defaultScreenH - 60;
		}else if(event == EQUIPMENT_SELECT){
			page = 0;
			page_max = (weaponStockTotal - 1) / 10;
		}
	}
	
	//プレイヤー関係
	final void weaponChangeByID(int weapon){
		if(this.weapon != weapon && weapon != NONE){
			this.weapon = weapon;
			if(cfg.weaponSEIsSerial.get(weapon))
				weaponSE[weapon].stop();
			weaponChangedFrame = nowFrame;
			ammoHave = itemPossession[cfg.weaponAmmoKind[weapon]];
			gunChangeSE.stop();
			gunChangeSE.play();
		}
	}
	final void weaponChangeBySlot(int slot){
		if(this.slot != slot && 0 <= slot && slot < weaponSlot_max && weaponSlot[slot] != NONE){
			this.slot = slot;
			this.weaponChangeByID(weaponSlot[slot]);
		}
	}
	
	//設置系
	/**
	* 武器の名前を指定して、弾を設置するメソッドです。
	* 指定した武器名がconfigで見つからなかった場合、NullPointExceptionが投げられます。
	* @param weaponName 武器の名前
	* @param x x座標
	* @param y y座標
	* @param angle 発射角度
	* @param gunnerType 射手の種類　発射された弾はどこから撃たれたかの情報を持ちます。指定方法は[PLAYER|ENEMY|ENTITY|GIMMICK|BULLET]です。
	* @param gunnerID 射手のID　プレイヤーの場合は、0を入れてください
	* @param team 自機側の弾の場合PLAYER,敵側の場合ENEMYを入れます。同士討ちを回避したり、反対勢力の弾同士で相殺が起きたりします。
	* @since ~beta7.0
	*/
	final public int[] setBulletByWeapon(String weaponName,double x,double y,double angle,int gunnerType,int gunnerID,int team){ //弾生成(名前指定)
		try{
			return this.setBulletByWeapon(cfg.convertID_weapon(weaponName),x,y,0,0,angle,gunnerType,gunnerID,team); //武器IDに自動変換
		}catch(NullPointerException e){
			System.out.println("「" + weaponName + "」の存在しない武器名が指定されました。");
			return new int[0];
		}
	}
	/**
	* 武器のIDを指定して、弾を設置するメソッドです。ランダムで武器を選んだり、変数から参照して指定するときに使われます。
	* 指定した武器IDが不適当であったときArrayIndexBoundsExceptionが投げられます。
	* @param weaponID 武器ID
	* @param angle 発射角度
	* @param gunnerType 射手の種類　発射された弾はどこから撃たれたかの情報を持ちます。指定方法は[PLAYER|ENEMY|ENTITY|GIMMICK|BULLET]です。
	* @param gunnerID 射手のID　プレイヤーの場合は、0を入れてください
	* @param team 自機側の弾の場合PLAYER,敵側の場合ENEMYを入れます。同士討ちを回避したり、反対勢力の弾同士で相殺が起きたりします。
	* @since ~beta7.0
	*/
	final public int[] setBulletByWeapon(int weaponID,double x,double y,double angle,int gunnerType,int gunnerID,int team){ //弾生成(ID指定)
		return this.setBulletByWeapon(weaponID,x,y,0,0,angle,gunnerType,gunnerID,team);
	}
	/**
	* 同じく武器名指定で弾を設置するメソッドですが、設置弾に初速度をつけることができます。
	* @param weaponName 武器の名前
	* @param xPowerPlus yPowerPlus 与える初速度
	* @param angle 発射角度
	* @param gunnerType 射手の種類　発射された弾はどこから撃たれたかの情報を持ちます。指定方法は[PLAYER|ENEMY|ENTITY|GIMMICK|BULLET]です。
	* @param gunnerID 射手のID　プレイヤーの場合は、0を入れてください
	* @param team 自機側の弾の場合PLAYER,敵側の場合ENEMYを入れます。同士討ちを回避したり、反対勢力の弾同士で相殺が起きたりします。
	* @since ~beta7.0
	*/
	final public int[] setBulletByWeapon(String weaponName,double x,double y,double xPowerPlus,double yPowerPlus,double angle,int gunnerType,int gunnerID,int team){ //弾生成(慣性追加)(名前指定)
		try{
			return this.setBulletByWeapon(cfg.convertID_weapon(weaponName),x,y,xPowerPlus,yPowerPlus,angle,gunnerType,gunnerID,team);
		}catch(NullPointerException e){
			System.out.println("「" + weaponName + "」の存在しない武器名が指定されました。");
			return new int[0];
		}
	}
	/**
	* 重複・連鎖が発動しない特殊な弾設置メソッドです。
	* @param weaponID 武器ID
	* @param xPowerPlus yPowerPlus 与える初速度
	* @param angle 発射角度
	* @param gunnerType 射手の種類　発射された弾はどこから撃たれたかの情報を持ちます。指定方法は[PLAYER|ENEMY|ENTITY|GIMMICK|BULLET]です。
	* @param gunnerID 射手のID　プレイヤーの場合は、0を入れてください
	* @param team 自機側の弾の場合PLAYER,敵側の場合ENEMYを入れます。同士討ちを回避したり、反対勢力の弾同士で相殺が起きたりします。
	* @since ~beta7.0
	*/
	final public int[] setBulletByWeapon_inside(int weaponID,double x,double y,double xPowerPlus,double yPowerPlus,double angle,int gunnerType,int gunnerID,int team){ //弾生成(慣性追加)(ID指定)
		if(weaponID < 0 || cfg.weaponKindTotal <= weaponID){
			System.out.println("weaponID" + weaponID + "の存在しない武器IDが指定されました。");
			return new int[0];
		}
		//設置座標・発射角度の特殊調整
		final int firePointData = cfg.weaponFirePoint[weaponID];
		final double directionData = cfg.weaponDirection[weaponID];
		//必要なときだけ、最も近い敵を探索する処理を行う
		final double nearstTargetX,nearstTargetY;
		if(firePointData == TARGET || (int)directionData == TARGET){
			if(team == ENEMY){
				nearstTargetX = playerX;
				nearstTargetY = playerY;
			}else{
				int nearstDistance = MAX;
				int targetID = NONE;
				int targetType = NONE;
				for(int k = 0;k <= enemy_maxID;k++){
					if(enemyKind[k] == NONE)
						continue;
					final int xd = (int)(enemyX[k] - x),yd = (int)(enemyY[k] - y),d = xd*xd + yd*yd;
					if(nearstDistance > d){
						nearstDistance = d;
						targetID = k;
						targetType = ENEMY;
					}
				}
				for(int k = 0;k <= entity_maxID;k++){
					if(entityKind[k] == NONE || entityTeam[k] != NONE && entityTeam[k] == team)
						continue;
					final int xd = (int)(entityX[k] - x),yd = (int)(entityY[k] - y),d = xd*xd + yd*yd;
					if(nearstDistance > d){
						nearstDistance = d;
						targetID = k;
						targetType = ENTITY;
					}
				}
				if(targetID != NONE){
					if(targetType == ENEMY){
						nearstTargetX = enemyX[targetID];
						nearstTargetY = enemyY[targetID];
					}else{
						nearstTargetX = entityX[targetID];
						nearstTargetY = entityY[targetID];
					}
				}else
					nearstTargetX = nearstTargetY = NONE;
			}
		}else
			nearstTargetX = nearstTargetY = NONE;
		//設置座標調整
		switch(cfg.weaponFirePoint[weaponID]){
		case FOCUS: //照準地点に直接設置する
			x = playerX - defaultScreenW/2 + focusX;
			y = playerY - defaultScreenH/2 + focusY;
			break;
		case TARGET: //最も近い敵に直接設置する
			x = nearstTargetX;
			y = nearstTargetY;
			break;
		}
		//発射角度調整
		double targetX = NONE,targetY = NONE;
		double reverse = 0.0;
		switch((int)directionData){
		case NONE:
			break;
		case SELF: //自分に向かって撃つ
		case REVERSE_SELF: //その逆方向に向かって撃つ
			if((int)directionData == REVERSE_SELF)
				reverse = PI;
			switch(gunnerType){
			case PLAYER:
				targetX = playerX;
				targetY = playerY;
				break;
			case ENEMY:
				targetX = enemyX[gunnerID];
				targetY = enemyY[gunnerID];
				break;
			case ENTITY:
				targetX = entityX[gunnerID];
				targetY = entityY[gunnerID];
				break;
			case GIMMICK:
				targetX = gunnerID/stageH*100;
				targetY = gunnerID%stageH*100;
				break;
			case BULLET:
				targetX = bulletX[gunnerID];
				targetY = bulletY[gunnerID];
			}
			break;
		case TARGET: //一番近い敵に向かって撃つ
		case REVERSE_TARGET: //その逆方向に向かって撃つ
			if((int)directionData == REVERSE_TARGET)
				reverse = PI;
			targetX = nearstTargetX;
			targetY = nearstTargetY;
			break;
		default:
			angle = cfg.weaponDirection[weaponID];
		}
		final double sin_angle = sin(angle),cos_angle = cos(angle);
		int[] usedBulletID = new int[10];
		int usedBullets = 0;
		topLabel:
		for(int time = 0;time < cfg.weaponBulletKind[weaponID].length;time++){ //発射弾種類数
			final int bulletKind = cfg.weaponBulletKind[weaponID][time]; //種類値取得
			if(bulletKind == NONE) //指定なし
				continue;
			final int[] firePointsX = cfg.weaponFirePointsX[weaponID];
			final int[] firePointsY = cfg.weaponFirePointsY[weaponID];
			final double rotateCenterXd = cfg.bulletRotateRadius[bulletKind]*cos(cfg.bulletRotateStartAngle[bulletKind]), //公転処理による位置変動,x座標変化
				rotateCenterYd = cfg.bulletRotateRadius[bulletKind]*sin(cfg.bulletRotateStartAngle[bulletKind]); //y座標変化
			final boolean laserAction = cfg.bulletLaserAction.get(bulletKind);
			final double aberration = cfg.weaponAberration[weaponID],directionCorrect = cfg.weaponDirectionCorrect[weaponID];
			final int speed = cfg.weaponBulletSpeed[weaponID],speedDispersion = cfg.weaponBulletSpeedDispersion[weaponID];
			final double accel = cfg.weaponBulletAccel[weaponID];
			final double inertiaRate = cfg.weaponInertiaRate[weaponID];
			for(int time2 = 0;time2 < firePointsX.length;time2++){ //この種類の発射数
				for(int limit = cfg.weaponBurst[weaponID];limit > 0 && bullet_total < this.bulletKind.length;limit--){
					final int id = createBulletID();
					if(id == -1)
						break topLabel;
					if(usedBullets >= usedBulletID.length)
						usedBulletID = Arrays.copyOf(usedBulletID,usedBulletID.length*2);
					usedBulletID[usedBullets++] = id;
					this.bulletKind[id] = bulletKind;
					bulletAppearFrame[id] = nowFrame;
					bulletLimitFrame[id] = cfg.bulletLimitFrame[bulletKind];
					bulletLimitMove[id] = cfg.weaponLimitRange[weaponID];
					bulletTeam[id] = team;
					bulletStr[id] = cfg.weaponStrength[weaponID];
					bulletX[id] = x + firePointsX[time2]*sin_angle + firePointsY[time2]*cos_angle
						+ rotateCenterXd;
					bulletY[id] = y - firePointsX[time2]*cos_angle + firePointsY[time2]*sin_angle
						+ rotateCenterYd;
					bulletReflectiveness[id] = cfg.weaponBulletReflectiveness[weaponID];
					bulletReflectDamageRatio[id] = cfg.weaponBulletReflectDamageRatio[weaponID];
					bulletOffSet[id] = cfg.weaponBulletOffSet[weaponID];
					bulletPenetration[id] = cfg.weaponBulletPenetration[weaponID];
					bulletRotateAngle[id] = cfg.bulletRotateStartAngle[bulletKind];
					bulletFollowTarget[id] = gunnerID;
					bulletKnockBack[id] = cfg.weaponBulletKnockBack[weaponID];
					//角度計算
					double angle2 = directionCorrect;
					if(targetX != NONE){
						angle2 += atan2(targetY - bulletY[id] + rotateCenterYd,targetX - bulletX[id] + rotateCenterXd) + reverse;
						//難解コード
						//rotateCenterX/Ydとは公転軌道を描く弾が初めから軌道上にいるためにずれる距離であるが、
						//発射角度は弾の設置地点ではなく公転する中心の座標から計算しないと(狙わないと)おかしい
						//よって上の文を angle2 += atan2(targetY - bulletY[id],targetX - bulletX[id]) + reverse; と書くのは間違いである
					}else
						angle2 += angle;
					if(aberration != 0.0)
						angle2 += (rnd.nextDouble()*2.0 - 1.0)*aberration;
					bulletAngle[id] = angle2;
					//加速度
					bulletXAccel[id] = accel*cos(angle2);
					bulletYAccel[id] = accel*sin(angle2);
					//速度計算
					int speed2 = speed;
					if(speedDispersion != 0)
						speed2 += (int)random(-speedDispersion,speedDispersion);
					if(speed2 != 0){
						bulletXPower[id] = speed2*cos(angle2) + xPowerPlus*inertiaRate;
						bulletYPower[id] = speed2*sin(angle2) + yPowerPlus*inertiaRate;
					}else{
						bulletXPower[id] = xPowerPlus*inertiaRate;
						bulletYPower[id] = yPowerPlus*inertiaRate;
					}
				}
			}
		}
		//射手に力をかける(反動etc)
		final int gunnerForce = cfg.weaponGunnerForce[weapon];
		if(gunnerForce != 0){
			final double gunnerAngle; //射手が向いている角度
			switch(gunnerType){ //オブジェクト種類を解析
			case PLAYER:
				playerXPower += gunnerForce*cos(playerAngle);
				playerYPower += gunnerForce*sin(playerAngle);
				break;
			case ENEMY:
				gunnerAngle = enemyAngle[gunnerID];
				enemyXPower[gunnerID] += gunnerForce*cos(gunnerAngle);
				enemyYPower[gunnerID] += gunnerForce*sin(gunnerAngle);
				break;
			case ENTITY:
				gunnerAngle = entityAngle[gunnerID];
				entityXPower[gunnerID] += gunnerForce*cos(gunnerAngle);
				entityYPower[gunnerID] += gunnerForce*sin(gunnerAngle);
				break;
			case BULLET:
				gunnerAngle = bulletAngle[gunnerID];
				bulletXPower[gunnerID] += gunnerForce*cos(gunnerAngle);
				bulletYPower[gunnerID] += gunnerForce*sin(gunnerAngle);
			}
		}
		return Arrays.copyOf(usedBulletID,usedBullets);
	}
	final int[] setBulletByWeapon(int weaponID,double x,double y,double xPowerPlus,double yPowerPlus,double angle,int gunnerType,int gunnerID,int team){
		//重複・連鎖攻撃があれば事前に処理する
		//武器重複
		for(int k = 1;k <= cfg.weaponGunLoop[weaponID];k++)
			setAttackPlan(weaponID,gunnerType,gunnerID,k*cfg.weaponGunLoopDelay[weaponID],false);
		//武器連鎖
		if(cfg.weaponGunChain[weaponID] != NONE)
			setAttackPlan(cfg.weaponGunChain[weaponID],gunnerType,gunnerID,cfg.weaponGunChainDelay[weaponID],true);
		//弾を設置する
		return this.setBulletByWeapon_inside(weaponID,x,y,xPowerPlus,yPowerPlus,angle,gunnerType,gunnerID,team);
	}
	/**
	* 新しい攻撃計画(アタックプラン)を建てます。
	* この攻撃計画は、ある武器による攻撃を一定フレーム後に予約することができ、発動主が削除されていない限り必ず発動します。
	* 主に連続攻撃処理などで活用されますが、武器コンフィグで連鎖攻撃は設定できるので、MODから呼び出す機会は多くありません。(ボタン連打によるコンボはこのメソッドではなく、setComboPlan(未実装)をお使いください)
	* @param waepon 使用武器のID
	* @param gunnerType 攻撃元のオブジェクト種類(BreakScope.ENEMYなどの定数を使ってください)
	* @param gunnerID 攻撃元のオブジェクトID
	* @param frame 発動までのフレーム
	* @return 計画を予約できたか(満杯のときは計画が成功しません)
	* @since beta9.0
	*/
	final public boolean setAttackPlan(int weapon,int gunnerType,int gunnerID,int frame,boolean isStarter){
		if(0 <= weapon && weapon < cfg.weaponKindTotal){ //武器IDが有効範囲内かチェック
			for(int i = 0;i < attackPlanID.length;i++){
				if(attackPlanFrame[i] == MAX){
					attackPlanID[i] = gunnerType + gunnerID;
					attackPlanWeapon[i] = weapon;
					attackPlanFrame[i] = nowFrame + frame;
					attackPlanIsStarter.set(i,isStarter);
					return true;
				}
			}
		}
		return false;
	}
	/**
	* 新しい連撃計画(コンボプラン)を建てます。現在未実装です。
	* この連撃計画は、有効フレーム内にある動作を感知して特定武器の発動をおこすことができます。
	* たとえば、発動イベントにBreakScope.LEFT_CLICK,有効フレームを20~30,使用武器を剣に設定すると、斬撃コンボの実装が可能になります。
	* 武器コンフィグのコンボ攻撃用の処理をメソッド化したもので、さほどMODから呼び出すことはありません。
	* @param waepon 使用武器のID
	* @param objectID 攻撃元のオブジェクトID(通常のIDにオブジェクト種類定数を足してください)
	* @param startFrame 判定開始フレーム
	* @param endFrame 判定終了フレーム
	* @param event 判定イベント(LEFT_CLICK,RIGHT_CLICKのほか、VK_A,VK_BなどのKeyEvent定数が設定可能)
	* @return 計画を予約できたか(満杯のときは計画が成功しません)
	*/
	final public boolean setComboPlan(int weapon,int objectID,int startFrame,int endFrame,int event){
		return false;
	}
	/**
	* 今の敵IDのもっとも小さい番号の空きIDを渡します。
	* 敵総数は自動的に+1されます。
	* 通常はsetEnemyが使われますが、特殊な生成を行う必要があるとき、こちらを使うことができます。
	* @return 空きID
	* @since ~beta7.0
	*/
	final public int createEnemyID(){ //敵ID生成
		for(int i = 0;i < enemyKind.length;i++){
			if(enemyKind[i] == NONE){
				enemyKind[i] = -1; //予約
				if(i > enemy_maxID)
					enemy_maxID = i;
				enemy_total++;
				return i;
			}
		}
		return -1;
	}
	/**
	* 今の弾IDのもっとも小さい番号の空きIDを渡します。
	* 弾総数は自動的に+1されます。
	* 通常はsetBulletが使われますが、特殊な生成を行う必要があるとき、こちらを使うことができます。
	* @return 空きID
	* @since ~beta7.0
	*/
	final public int createBulletID(){ //弾ID生成
		for(int i = 0;i < bulletKind.length;i++){
			if(bulletKind[i] == NONE){
				if(i > bullet_maxID)
					bullet_maxID = i;
				bullet_total++;
				return i;
			}
		}
		return -1;
	}
	/**
	* 今のエンティティIDのもっとも小さい番号の空きIDを渡します。
	* エンティティ総数は自動的に+1されます。
	* 通常はsetEntityが使われますが、特殊な生成を行う必要があるとき、こちらを使うことができます。
	* @return 空きID
	* @since ~beta7.0
	*/
	final public int createEntityID(){ //エンティティID生成
		for(int i = 0;i < entityKind.length;i++){
			if(entityKind[i] == NONE){
				entityKind[i] = -1; //予約
				if(i > entity_maxID)
					entity_maxID = i;
				entity_total++;
				return i;
			}
		}
		return -1;
	}
	/**
	* 今のエフェクトIDのもっとも小さい番号の空きIDを渡します。
	* エフェクト総数は自動的に+1されます。
	* 通常はsetEffectが使われますが、特殊な生成を行う必要があるとき、こちらを使うことができます。
	* @return 空きID
	* @since ~beta7.0
	*/
	final public int createEffectID(){ //エフェクトID生成
		for(int i = 0;i < effectKind.length;i++){
			if(effectKind[i] == NONE){
				if(i > effect_maxID)
					effect_maxID = i;
				effect_total++;
				return i;
			}
		}
		return -1;
	}
	/**
	* 今のアイテムIDのもっとも小さい番号の空きIDを渡します。
	* アイテム総数は自動的に+1されます。
	* 通常はsetItemが使われますが、特殊な生成を行う必要があるとき、こちらを使うことができます。
	* @return 空きID
	* @since ~beta7.0
	*/
	final int createItemID(){ //アイテムID生成
		for(int i = 0;i < itemKind.length;i++){
			if(itemKind[i] == NONE){
				if(i > item_maxID)
					item_maxID = i;
				item_total++;
				return i;
			}
		}
		return -1;
	}
	/**
	* 敵を一体生成する基本メソッドです。
	* このメソッドはcreateEnemyIDメソッドを使って死亡ID・空きIDを探し出し、指定した敵種類のステータスに初期化させます。
	*　敵は途中生成されたものでもスコアにカウントされるため、ステージ進歩率をスコア分減少させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときや、IDに空きがないときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @param direction 向き
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEnemy(int kind,int x,int y,double direction){ //敵生成
		if(kind < 0 || cfg.enemyKindTotal <= kind || enemy_total >= enemyKind.length)
			return false;
		final int id = createEnemyID();
		if(id == -1)
			return false;
		enemyKind[id] = kind;
		enemyTargetX[id] = enemyNextTargetX[id] = enemyX[id] = x;
		enemyTargetY[id] = enemyNextTargetY[id] = enemyY[id] = y;
		enemyHP[id] = cfg.enemyHP[kind];
		enemyFoundMe.clear(id);
		enemyAngle[id] = enemyTargetAngle[id] = direction;
		enemyNextAngle[id] = NONE;
		enemyXPower[id] = enemyYPower[id] = 0;
		enemyWound[id] = enemyShotFrame[id] = 0;
		enemySightCuttingGrid[id] = NONE;
		maxScore += cfg.enemyScore[kind];
		return true;
	}
	/**
	* エンティティを一体生成する基本メソッドです。
	* このメソッドはcreateEntityIDメソッドを使って死亡ID・空きIDを探し出し、指定したエンティティ種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときや、IDに空きがないときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEntity(int kind,int x,int y,double angle){ //エンティティ生成
		if(kind < 0 || cfg.entityClass.length <= kind || entity_total >= entityKind.length)
			return false; //不適切な種類値であった場合、生成失敗
		final int id = createEntityID();
		if(id == -1)
			return false;
		cfg.entityClass[entityKind[id] = kind].created(id);
		entityHP[id] = cfg.entityHP[kind];
		entityX[id] = x;
		entityY[id] = y;
		entityAngle[id] = angle;
		entityTeam[id] = NONE;
		return true;
	}
	/**
	* ギミックを一体生成する基本メソッドです。通常座標を使います。
	* ID管理方法が座標式であるため、空きIDを検索することはありません。
	* 指定した座標にギミックがすでにあった場合、overwriteをtrueに指定することで上書きできます。回避したいときはfalseを指定します。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @param overwrite 上書きモード
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setGimmick(int kind,int x,int y,boolean overwrite){ //ギミック生成-通常座標指定
		return this.setGimmick(kind,x/100*stageGridH + y/100,overwrite); //マップ座標に自動変換する
	}
	/**
	* ギミックを一体生成する基本メソッドです。マップ座標を使います。
	* ID管理方法が座標式であるため、空きIDを検索することはありません。
	* 指定した座標にギミックがすでにあった場合、overwriteをtrueに指定することで上書きできます。回避したいときはfalseを指定します。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param map マップ座標
	* @param overwrite 上書きモード
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setGimmick(int kind,int map,boolean overwrite){ //ギミック生成-マップ座標指定
		if(kind < 0 || cfg.gimmickKindTotal <= kind) //不適切な種類値
			return false; //生成失敗
		if(overwrite || gimmickKind[map] == NONE){ //上書き設定か、空きがあれば生成
			if(gimmickKind[map] == NONE) //新規追加
				gimmick_total++; //総数更新
			else //上書き
				damageMap[map] = cfg.gimmickDamage[kind] - cfg.gimmickDamage[gimmickKind[map]]; //ダメージマップ更新
			gimmickKind[map] = kind;
			if(gimmickHP != null)
				gimmickHP[map] = cfg.gimmickHP[kind];
			cfg.gimmickClass[kind].created(map);
			return true;
		}
		//同じ場所にブロックがあり、かつ上書き設定ではないとき生成しない
		return false; //生成失敗
	}
	/**
	* アイテムを複数生成する基本メソッドです。
	* このメソッドはcreateItemIDメソッドを使って死亡ID・空きIDを探し出し、指定したアイテム種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param strength 多さ
	* @param x x座標
	* @param y y座標
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setItem(int kind,int strength,int x,int y){ //アイテム生成
		if(kind < 0 || 9 <= kind || strength <= 0 || item_total == itemKind.length)
			return false;
		final int id = createItemID();
		if(id == -1)
			return false;
		itemKind[id] = kind;
		itemX[id] = x;
		itemY[id] = y;
		itemAmount[id] = strength;
		return true;
	}
	/**
	* エフェクトを複数生成する基本メソッドです。エフェクト名で種類を指定し、生成量をConfigのデフォルト値で決めます。
	* このメソッドはcreateEffectIDメソッドを使って死亡ID・空きIDを探し出し、指定したエフェクト種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEffect(String name,int x,int y){ //エフェクト生成-既定生成量-名前指定
		final int kind = cfg.convertID_effect(name);
		if(kind < 0 || cfg.effectKindTotal <= kind)
			return false;
		return this.setEffect(kind,x,y,0.0,0,cfg.effectAmount[kind]); //名前をIDに変換
	}
	/**
	* エフェクトを複数生成する基本メソッドです。エフェクト名で種類を指定します。
	* このメソッドはcreateEffectIDメソッドを使って死亡ID・空きIDを探し出し、指定したエフェクト種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param name エフェクト名
	* @param x x座標
	* @param y y座標
	* @param number 生成量
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEffect(String name,int x,int y,int number){ //エフェクト生成-指定生成量-名前指定
		return this.setEffect(cfg.convertID_effect(name),x,y,0.0,0,number); //名前をIDに変換
	}
	/**
	* エフェクトを複数生成する基本メソッドです。数値で種類を指定します。
	* このメソッドはcreateEffectIDメソッドを使って死亡ID・空きIDを探し出し、指定したエフェクト種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @param number 生成量
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEffect(int kind,int x,int y,int number){ //エフェクト生成-指定生成量
		return this.setEffect(kind,x,y,0.0,0,number);
	}
	/**
	* エフェクトを複数生成する基本メソッドです。数値で種類を指定し、生成量をConfigのデフォルト値で決めます。
	* このメソッドはcreateEffectIDメソッドを使って死亡ID・空きIDを探し出し、指定したエフェクト種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEffect(int kind,int x,int y){ //エフェクト生成-既定生成量
		if(kind < 0 || cfg.effectKindTotal <= kind)
			return false;
		return this.setEffect(kind,x,y,0.0,0,cfg.effectAmount[kind]);
	}
	/**
	* エフェクトを複数生成する基本メソッドです。数値で種類を指定し、さらに飛散方向を調節することができます。
	* このメソッドはcreateEffectIDメソッドを使って死亡ID・空きIDを探し出し、指定したエフェクト種類のステータスに初期化させます。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param kind 種類値
	* @param x x座標
	* @param y y座標
	* @param scatterAngle 慣性追加方向
	* @param scatterPower 慣性追加速度
	* @param number 生成量
	* @return 正しく生成されたか
	* @since ~beta7.0
	*/
	final boolean setEffect(int kind,int x,int y,double scatterAngle,int scatterPower,int number){ //エフェクト生成-指定飛散方向,指定生成量
		if(kind < 0 || cfg.effectKindTotal <= kind)
			return false;
		final int displacement = cfg.effectDisplacement[kind], //設置地点発散度
			minSize = cfg.effectSize_min[kind],maxSize = cfg.effectSize_max[kind], //サイズ(最小～最大)
			minRange = cfg.effectSpeed_min[kind],maxRange = cfg.effectSpeed_max[kind]; //飛散距離変動
		final double alphaInitial = cfg.effectAlphaInitial[kind], //透過度初期値
			cosScatterAngle = scatterPower != 0 ? cos(scatterAngle) : 0.0,sinScatterAngle = scatterPower != 0 ? sin(scatterAngle) : 0.0; //飛散角度cos/sin定数,飛散処理がなければ計算回避
		for(;number > 0 && effect_total < effectKind.length;number--){
			final int id = createEffectID();
			if(id == -1)
				return false;
			effectKind[id] = kind;
			effectApparFrame[id] = nowFrame;
			final double displacementAngle = random2(PI),cosDspAngle = cos(displacementAngle),sinDspAngle = sin(displacementAngle);
			if(displacement != 0){ //発散値加算
				final int displacement2 = random2(displacement);
				x += (int)(displacement2*cosDspAngle);
				y += (int)(displacement2*sinDspAngle);
			}
			effectX[id] = x;effectY[id] = y;
			effectSize[id] = random(minSize,maxSize);
			if(minRange != 0 || maxRange != 0){ //飛散値代入
				final int range = random(minRange,maxRange); //基準飛散距離
				if(scatterPower != 0){ //追加飛散指向性があるか
					effectXPower[id] = (int)(range*cosDspAngle + scatterPower*cosScatterAngle);
					effectYPower[id] = (int)(range*sinDspAngle + scatterPower*sinScatterAngle);
				}else{
					effectXPower[id] = (int)(range*cosDspAngle);
					effectYPower[id] = (int)(range*sinDspAngle);
				}
			}else{
				effectXPower[id] = 0;
				effectYPower[id] = 0;
			}
			effectAlpha[id] = alphaInitial;
			effectAlphaPower[id] = 0.0;
		}
		return true;
	}
	/**
	* 爆発を生成する基本メソッドです。
	* 原理としては爆風に限りなく近い円形弾幕を設置しているだけです。よって範囲攻撃とは少し違います。
	* 生成が成功するとtrueを返します。kindが不適当な値であるときは、このメソッドからfalseが返されます。
	* @param x x座標
	* @param y y座標
	* @param range 爆発力(だいたいの半径)
	* @param team 所属チーム,フレンドリーファイヤ回避対象　プレイヤー:PLAYER 敵:ENEMY 無所属(全員にダメージ):NONE
	* @since ~beta7.0
	*/
	final void setExplosion(double x,double y,int range,int team){ //爆発生成
		final int shotKind = cfg.convertID_bullet("EP_1");
		if(shotKind == -1)
			return;
		setLight((int)x,(int)y,range);
		for(int k = 0;k < range && bullet_total < bulletKind.length;k++){
			final int newID = createBulletID();
			if(newID == -1)
				return;
			bulletKind[newID] = shotKind;
			final double placeAngle = random2(PI);
			bulletX[newID] = x + range/4*cos(placeAngle);
			bulletY[newID] = y + range/4*sin(placeAngle);
			final int speed = random(20,30);
			bulletXPower[newID] = speed*cos(placeAngle);
			bulletYPower[newID] = speed*sin(placeAngle);
			bulletXAccel[newID] = bulletYAccel[newID] = 0.0;
			bulletAngle[newID] = placeAngle;
			bulletStr[newID] = 30;
			bulletReflectiveness[newID] = 0;
			bulletReflectDamageRatio[newID] = 1.0;
			bulletOffSet[newID] = 0;
			bulletFollowTarget[newID] = NONE;
			bulletKnockBack[newID] = 15;
			bulletPenetration[newID] = random(1,3);
			bulletLimitFrame[newID] = cfg.bulletLimitFrame[shotKind];
			bulletLimitMove[newID] = range + random2((int)(range*0.2));
			bulletTeam[newID] = team;
		}
	}
	/**
	* 指定する弾の消滅(ロスト)を起こします。
	* 弾はなくなり、設定された消滅処理が実行されます（消滅時追加弾、消滅時エフェクトなど）
	* このメソッドは基本的に内部で呼ばれます。
	* @param id 弾ID
	* @since 9.0
	*/
	final void bulletLost(int id){
		if(id < 0 || bulletKind.length <= id || bulletKind[id] == NONE)
			return;
		final double x = bulletX[id],y = bulletY[id];
		final int x_int = (int)x,y_int = (int)y;
		//追加弾(ロスト)処理
		for(int gunID : cfg.bulletLostGun[bulletKind[id]]){
			final int[] usedBulletIDs //LostGunに使われたすべての弾ID
			 = setBulletByWeapon(gunID,x_int,y_int,bulletAngle[id],BULLET,id,bulletTeam[id]); //消滅時追加弾を発射
			 //ステータス継承処理
			 //発射された弾のステータスを継承
			if(cfg.weaponStrExtend[gunID] != 0.0){
				for(int usedID : usedBulletIDs)
					bulletStr[usedID] = cfg.weaponStrength[gunID] + (int)(bulletStr[id]*cfg.weaponStrExtend[gunID]);
			}
			if(cfg.weaponLiRExtend[gunID] != 0.0){
				for(int usedID : usedBulletIDs)
					bulletLimitMove[usedID] = cfg.weaponLimitRange[gunID] + (int)(bulletLimitMove[id]*cfg.weaponLiRExtend[gunID]);
			}
			if(cfg.weaponBulletSpdExtend[gunID] != 0.0){
				final double xPower = bulletXPower[id],yPower = bulletYPower[id];
				final double speed = sqrt(xPower*xPower + yPower*yPower)*cfg.weaponBulletSpdExtend[gunID]; //旧IDの弾速度
				for(int usedID : usedBulletIDs){
					bulletXPower[usedID] += speed*cos(bulletAngle[id]);
					bulletYPower[usedID] += speed*sin(bulletAngle[id]);
				}
			}
			if(cfg.weaponBulletRefExtend[gunID] != 0.0){
				for(int usedID : usedBulletIDs)
					bulletReflectiveness[usedID] = cfg.weaponBulletReflectiveness[gunID] + (int)(bulletReflectiveness[id]*cfg.weaponBulletRefExtend[gunID]);
			}
			if(cfg.weaponBulletOfSExtend[gunID] != 0.0){
				for(int usedID : usedBulletIDs)
					bulletOffSet[usedID] = cfg.weaponBulletOffSet[gunID] + (int)(bulletOffSet[id]*cfg.weaponBulletOfSExtend[gunID]);
			}
			if(cfg.weaponBulletLiFExtend[gunID] != 0.0){
				for(int usedID : usedBulletIDs)
					bulletLimitFrame[usedID] = cfg.bulletLimitFrame[bulletKind[usedID]] + (int)(bulletLimitFrame[id]*cfg.weaponBulletLiFExtend[gunID]);
			}
			//追加エフェクト
			final int kind = bulletKind[id];
			for(int effectID : cfg.bulletLostEffect[kind]) //消失時のエフェクトがあるか
				setEffect(effectID,x_int,y_int);
			//追加爆発処理
			if(cfg.bulletHitBurst[kind] > 0){ //爆発性あり
				setExplosion(x,y,cfg.bulletHitBurst[kind],bulletTeam[id]);
				final double vibrationAngle = rnd.nextDouble()*PI*2; //画面を揺らす
				xVibration += 10*cos(vibrationAngle);yVibration += 10*sin(vibrationAngle);
			}
		}
		this.deleteBulletID(id); //弾削除
	}
	/**
	* 敵に武器を使って攻撃を行わせます。武器はID指定です。コンフィグコードで使われます。
	* 敵の角度(enemyAngle)の方向に武器を使用するので、発射処理前に角度をずらすことで掃射などのアクションができます。
	* 内部的にはsetBulletByWeaponを呼び出しています。
	* @param id 敵ID
	* @param weapon 武器ID
	* @since beta7.6
	*/
	final void makeEnemyAttack(int id,int weapon){
		setBulletByWeapon(weapon,(int)enemyX[id],(int)enemyY[id],(int)enemyXPower[id],(int)enemyYPower[id],enemyAngle[id],ENEMY,id,ENEMY);
	}
	/**
	* 敵に武器を使って攻撃を行わせます。武器は名前指定です。コンフィグコードで使われます。
	* 敵の角度(enemyAngle)の方向に武器を使用するので、発射処理前に角度をずらすことで掃射などのアクションができます。
	* 内部的にはsetBulletByWeaponを呼び出しています。
	* @param id 敵ID
	* @param weaponName 武器名
	* @since beta7.6
	*/
	final void makeEnemyAttack(int id,String weaponName){
		try{
			this.makeEnemyAttack(id,cfg.convertID_weapon(weaponName));
		}catch(NullPointerException e){
			System.out.println("「" + weaponName + "」の存在しない武器名が指定されました。");
		}
	}
	/**
	* エンティティに武器を使って攻撃を行わせます。武器はID指定です。コンフィグコードで使われます。
	* エンティティの角度(entityAngle)の方向に武器を使用するので、発射中に角度をずらすことで掃射などのアクションができます。
	* 内部的にはsetBulletByWeaponを呼び出しています。
	* @param id エンティティID
	* @param weapon 武器ID
	* @param team 弾チーム
	* @since beta7.6
	*/
	final void makeEntityAttack(int id,int weapon,int team){
		setBulletByWeapon(weapon,(int)entityX[id],(int)entityY[id],(int)entityXPower[id],(int)entityYPower[id],entityAngle[id],ENTITY,id,team);
	}
	/**
	* エンティティに武器を使って攻撃を行わせます。武器は名前指定です。コンフィグコードで使われます。
	* エンティティの角度(entityAngle)の方向に武器を使用するので、発射中に角度をずらすことで掃射などのアクションができます。
	* 内部的にはsetBulletByWeaponを呼び出しています。
	* @param id エンティティID
	* @param weaponName 武器名
	* @param team 弾チーム
	* @since beta7.6
	*/
	final void makeEntityAttack(int id,String weaponName,int team){
		try{
			this.makeEntityAttack(id,cfg.convertID_weapon(weaponName),team);
		}catch(NullPointerException e){
			System.out.println("「" + weaponName + "」の存在しない武器名が指定されました。");
		}
	}
	/**
	* 光を１フレーム生成する基本メソッドです。
	* 連続で呼び出さないと勝手に消えます。
	* @param x x座標
	* @param y y座標
	* @param str 光度
	* @since ~beta7.0
	*/
	final void setLight(int x,int y,int str){ //光源生成
		if(str <= 0 || lightTotal >= lightX.length) //光度マイナスのときや,空きデータがないときをはスキップ
			return;
		if(abs(playerX - x) < OVER_X && abs(playerY - y) < OVER_Y){ //読み込み範囲内であるとき生成
			final int lightID = lightTotal++;
			lightness[lightID] = str/8;
			lightX[lightID] = (x - playerX + defaultScreenW/2)/8;
			lightY[lightID] = (y - playerY + defaultScreenH/2)/8;
		}
	}
	/**
	* 敵IDを一つ直接クリアする基本メソッドです。
	* 直接削除なので、ドロップアイテムなどは発生しません。
	* @param id 対象ID
	* @since ~beta7.0
	*/
	//削除系
	final void deleteEnemyID(int id){ //敵ID削除(完全なIDの抹消であり、ドロップアイテムなどは出ない)
		if(id < 0 || enemy_maxID < id || enemyKind[id] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		final int kind = enemyKind[id];
		enemy_total--; //総数更新
		if(id == enemy_maxID){ //最大IDの更新が必要
			for(int i = id - 1;;i--){ //次に大きいIDを探索
				if(i == -1){ //探索終了,全IDが空
					enemy_maxID = -1;
					break;
				}else if(enemyKind[i] != NONE){ //非空きID発見
					enemy_maxID = i;
					break;
				}
			}
		}
		cfg.enemyClass[kind].deleted(id); //削除追加処理を呼び出す
		enemyKind[id] = NONE;
	}
	/**
	* 弾を一体削除する基本メソッドです。
	* 追加弾幕などが設定されていた場合、通常通りに発生します。
	* @param id 対象ID
	* @since ~beta7.0
	*/
	final void deleteBullet(int id){ //弾削除(通常破壊,エフェクトや爆発は起こる)
		if(id < 0 || bullet_maxID < id || bulletKind[id] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		final int kind = bulletKind[id];
		final int x = (int)bulletX[id],y = (int)bulletY[id];
		if(cfg.bulletHitBurst[kind] > 0){ //爆発性あり
			setExplosion(x,y,cfg.bulletHitBurst[kind],bulletTeam[id]);
			final double vibrationAngle = rnd.nextDouble()*PI*2; //画面を揺らす
			xVibration += 10*cos(vibrationAngle);yVibration += 10*sin(vibrationAngle);
		}
		int size_2 = cfg.bulletSize[kind]/2;
		int effectPlaceX = x + (int)(size_2*cos(bulletAngle[id])),effectPlaceY = y + (int)(size_2*sin(bulletAngle[id])); //破壊エフェクト設置地点(弾の中心から進行方向へ少し先)
		for(int effectID : cfg.bulletDestroyEffect[kind]) //破壊エフェクトの生成
			setEffect(effectID,effectPlaceX,effectPlaceY);
		for(int gunID : cfg.bulletDestroyGun[kind]) //破壊時追加弾の生成
			setBulletByWeapon(gunID,x,y,bulletAngle[id],BULLET,id,bulletTeam[id]);
		this.deleteBulletID(id);
	}
	/**
	* 弾IDを一つ直接クリアする基本メソッドです。
	* 直接削除なので、追加弾幕などは発生しません。
	* @param id 対象ID
	* @since ~beta7.0
	*/
	final void deleteBulletID(int id){ //弾ID削除(完全なIDの抹消)
		if(id < 0 || bulletKind.length <= id || bulletKind[id] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		bullet_total--; //総数更新
		if(id == bullet_maxID){ //最大IDの更新が必要
			for(int i = id - 1;;i--){ //次に大きいIDを探索
				if(i == -1){ //探索終了,全IDが空
					bullet_maxID = -1;
					break;
				}else if(bulletKind[i] != NONE){ //非空きID発見
					bullet_maxID = i;
					break;
				}
			}
		}
		bulletKind[id] = NONE;
	}
	/**
	* エンティテIDを一つ直接クリアする基本メソッドです。
	* 直接削除なので、指定された死後処理は発生しません。
	* @param id 対象ID
	* @since ~beta7.0
	*/
	final void deleteEntityID(int id){ //エンティティ削除
		if(id < 0 || entity_maxID < id || entityKind[id] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		final int kind = entityKind[id];
		entity_total--; //総数更新
		if(playerRidingID == id)
			playerRidingID = NONE;
		if(id == entity_maxID){ //最大IDの更新が必要
			for(int i = id - 1;;i--){ //次に大きいIDを探索
				if(i == -1){ //探索終了,全IDが空
					entity_maxID = -1;
					break;
				}else if(entityKind[i] != NONE){ //非空きID発見
					entity_maxID = i;
					break;
				}
			}
		}
		cfg.entityClass[kind].deleted(id); //削除追加処理を呼び出す
		entityKind[id] = NONE;
	}
	/**
	* ギミックを一体削除する基本メソッドです。通常座標指定です。
	* 設定された死後処理は自動的に呼び出されます。
	* @param x x座標
	* @param y y座標
	* @since ~beta7.0
	*/
	final void deleteGimmick(int x,int y){ //ギミック削除-通常座標指定
		this.deleteGimmick(x/100*stageGridH + y/100);
	}
	/**
	* ギミックを一体削除する基本メソッドです。マップ座標指定です。
	* 設定された死後処理は自動的に呼び出されます。
	* @param map 対象map座標
	* @since ~beta7.0
	*/
	final void deleteGimmick(int map){ //ギミック削除-マップ座標指定
		if(map < 0 ||gimmickKind.length <= map || gimmickKind[map] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		final int kind = gimmickKind[map];
		final int x = map/stageGridH*100 + 50,y = map%stageGridH*100 + 50;
		setEffect(cfg.gimmickDestroyEffect[kind],x,y); //エフェクト生成
		setExplosion(x,y,cfg.gimmickExplosive[kind],NONE); //無所属爆発生成
		gimmick_total--; //総数更新
		if(damageMap != null)
			damageMap[map] -= cfg.gimmickDamage[kind]; //ダメージマップ更新
		//ギミックスイーパー更新
		if(gimmickSweeper != null && cfg.gimmickHP[kind] > 0){
			int fillGrid;
			final int gridW = map/stageGridH;
			final int gridH = map%stageGridH;
			int xs = max(gridW - 1,0);
			final int ys = max(gridH - 1,0);
			final int XE = min(gridW + 1,stageGridW - 1);
			final int YE = min(gridH + 1,stageGridH - 1);
			for(;xs <= XE;xs++){
				for(int ys_ = ys;ys_ <= YE;ys_++){
					fillGrid = xs*stageGridH + ys_;
					if(0 <= fillGrid && fillGrid < stageGridTotal)
						gimmickSweeper[fillGrid]++;
				}
			}
		}
		cfg.gimmickClass[kind].killed(map); //死亡追加処理を呼び出す
		cfg.gimmickClass[kind].deleted(map); //削除追加処理を呼び出す
		gimmickKind[map] = NONE;
		if(gimmickHP != null)
			gimmickHP[map] = 0;
	}
	/**
	* ギミックを一体直接削除する基本メソッドです。マップ座標指定です。
	* 設定された死後処理は呼び出されません。
	* @param map 対象map座標
	* @since ~beta7.0
	*/
	final void deleteGimmickID(int map){ //ギミックID削除
		if(map < 0 ||gimmickKind.length <= map || gimmickKind[map] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		final int kind = gimmickKind[map];
		gimmick_total--; //総数更新
		if(damageMap != null)
			damageMap[map] -= cfg.gimmickDamage[kind]; //ダメージマップ更新
		if(gimmickSweeper != null && cfg.gimmickHP[kind] > 0){
			int fillGrid;
			final int gridW = map/stageGridH;
			final int gridH = map%stageGridH;
			int xs = max(gridW - 1,0);
			final int ys = max(gridH - 1,0);
			final int XE = min(gridW + 1,stageGridW - 1);
			final int YE = min(gridH + 1,stageGridH - 1);
			for(;xs <= XE;xs++){
				for(int ys_ = ys;ys_ <= YE;ys_++){
					fillGrid = xs*stageGridH + ys_;
					if(0 <= fillGrid && fillGrid < stageGridTotal)
						gimmickSweeper[fillGrid]++;
				}
			}
		}
		cfg.gimmickClass[kind].deleted(map); //削除追加処理を呼び出す
		gimmickKind[map] = NONE;
		if(gimmickHP != null)
			gimmickHP[map] = 0;
	}
	/**
	* アイテムを一つ削除する基本メソッドです。
	* 現バージョンにアイテム死後処理はないので、アイテムの削除はこのメソッドに限ります。
	* @param id 対象ID
	* @since ~beta7.0
	*/
	final void deleteItemID(int id){ //アイテム削除
		if(id < 0 || itemKind.length <= id || itemKind[id] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		item_total--; //総数更新
		if(id == item_maxID){ //最大IDの更新が必要
			for(int i = id - 1;;i--){ //次に大きいIDを探索
				if(i == -1){ //探索終了,全IDが空
					item_maxID = -1;
					break;
				}else if(itemKind[i] != NONE){ //非空きID発見
					item_maxID = i;
					break;
				}
			}
		}
		itemKind[id] = NONE;
	}
	/**
	* エフェクトを一つ削除する基本メソッドです。
	* 現バージョンにエフェクト死後処理はないので、エフェクトの削除はこのメソッドに限ります。
	* @param id 対象ID
	* @since ~beta7.0
	*/
	final void deleteEffectID(int id){ //エフェクト削除
		if(id < 0 || effectKind.length <= id || effectKind[id] == NONE)
			return; //idが範囲外か、既に削除済みのときスキップ
		effect_total--; //総数更新
		if(id == effect_maxID){ //最大IDの更新が必要
			for(int i = id - 1;;i--){ //次に大きいIDを探索
				if(i == -1){ //探索終了,全IDが空
					effect_maxID = -1;
					break;
				}else if(effectKind[i] != NONE){ //非空きID発見
					effect_maxID = i;
					break;
				}
			}
		}
		effectKind[id] = NONE;
	}
	
	//敵AI
	void AI03(int i,double speed,boolean enemyFoundMe){ //ボスAI
		final double x = enemyX[i],y = enemyY[i];
		final double xd = x - enemyTargetX[i],yd = y - enemyTargetY[i];
		if(xd == 0.0 && yd == 0.0)
			return;
		if(!enemyFoundMe && xd*xd + yd*yd < speed*speed*50){
			if(enemyNextAngle[i] != NONE)
				enemyTargetAngle[i] = enemyNextAngle[i];
			enemyX[i] = enemyTargetX[i];
			enemyY[i] = enemyTargetY[i];
		}else{
			final double r = speed/sqrt(xd*xd + yd*yd);
			enemyMove(i,x,y,-xd*r,-yd*r,true);
		}
	}
	void AI02(int i,double speed,boolean enemyFoundMe){
		final double xd = enemyX[i] - enemyTargetX[i],yd = enemyY[i] - enemyTargetY[i];
		if(xd == 0.0 && yd == 0.0)
			return;
		if(!enemyFoundMe && sqrt(xd*xd + yd*yd) < speed){
			if(enemyNextAngle[i] != NONE)
				enemyTargetAngle[i] = enemyNextAngle[i];
			enemyX[i] = enemyTargetX[i];
			enemyY[i] = enemyTargetY[i];
		}else{
			final double r = speed/sqrt(xd*xd + yd*yd);
			enemyMove(i,enemyX[i],enemyY[i],-xd*r,-yd*r,true);
		}
	}
	void AI01(int i,double speed,boolean enemyFoundMe){
		final double xd = enemyX[i] - enemyTargetX[i],yd = enemyY[i] - enemyTargetY[i];
		if(xd == 0.0 && yd == 0.0)
			return;
		if(!enemyFoundMe && sqrt(xd*xd + yd*yd) < speed){
			if(enemyNextAngle[i] != NONE)
				enemyTargetAngle[i] = enemyNextAngle[i];
			enemyX[i] = enemyTargetX[i];
			enemyY[i] = enemyTargetY[i];
		}else{
			final double r = speed/sqrt(xd*xd + yd*yd);
			enemyMove(i,enemyX[i],enemyY[i],-xd*r,-yd*r,false);
		}
	}
	void AI00(int i,int speed,boolean enemyFoundMe){
		final double xd = enemyX[i] - enemyTargetX[i],yd = enemyY[i] - enemyTargetY[i];
		if(xd == 0.0 && yd == 0.0)
			return;
		if(!enemyFoundMe && sqrt(xd*xd + yd*yd) < speed){
			if(enemyNextAngle[i] != NONE)
				enemyTargetAngle[i] = enemyNextAngle[i];
			enemyX[i] = enemyTargetX[i];
			enemyY[i] = enemyTargetY[i];
		}else
			enemyMove(i,enemyX[i],enemyY[i],-xd/100.0*speed,-yd/100.0*speed,true);
	}
	final void enemyMove(int id,double x,double y,double xPower,double yPower,boolean directMove){ //敵重なり防止処理
		final int size = cfg.enemySize[enemyKind[id]];
		boolean getXMove = true,getYMove = true;
		if(squareHitGimmick((int)(x + xPower),(int)y,size))
			getXMove = false;
		if(squareHitGimmick((int)x,(int)(y + yPower),size))
			getYMove = false;
		if(cfg.enemyConflict.get(enemyKind[id])){ //敵同士の衝突判定を持つ敵
			for(int i = 0;i <= enemy_maxID && (getXMove || getYMove);i++){
				if(enemyKind[i] == NONE)
					continue;
				if(i != id){ //自分とは判定をしない
					final double sizeTotal = (size + cfg.enemySize[enemyKind[i]])/4,
						dx = abs(x - enemyX[i]),dy = abs(y - enemyY[i]);
					if(getXMove && abs(x + xPower - enemyX[i]) < sizeTotal && abs(y - enemyY[i]) < sizeTotal){ //x方向衝突あり
						getXMove = false; //x移動を取り消し
						final int rebound = enemyX[i] < (x + xPower) ? 3 : -3; //反発が発生,近いほど強い
						enemyXPower[id] += rebound; //自分側
						enemyXPower[i] -= rebound; //判定先
					}
					if(getYMove && abs(x - enemyX[i]) < sizeTotal && abs(y + yPower - enemyY[i]) < sizeTotal){ //y方向衝突あり
						getYMove = false; //y移動を取り消し
						final double rebound = enemyY[i] < (y + yPower) ? 3 : -3; //反発が発生,近いほど強い
						enemyYPower[id] += rebound; //自分側
						enemyYPower[i] -= rebound; //判定先
					}
				}
			}
		}
		if(directMove){
			if(getXMove){
				x += xPower;
				if(x < 700)
					x = 700;
				else if(x > stageW - 700)
					x = stageW - 700;
				enemyX[id] = x;
			}
			if(getYMove){
				y += yPower;
				if(y < 700)
					y = 700;
				else if(y > stageH - 700)
					y = stageH - 700;
				enemyY[id] = y;
			}
		}else{
			enemyXPower[id] += xPower;
			enemyYPower[id] += yPower;
		}
	}
	final void setDefaultEnemyBullet(int enemyID,String bulletName,double x,double y,int aberration,int speed,int amount){ //敵弾生成-自機自動照準
		this.setDefaultEnemyBullet(enemyID,cfg.convertID_bullet(bulletName),x,y,atan2(playerY - y,playerX - x),aberration,speed,amount);
	}
	final void setDefaultEnemyBullet(int enemyID,String bulletName,double x,double y,double angle,int aberration,int speed,int amount){ //敵弾生成-方向指定
		this.setDefaultEnemyBullet(enemyID,cfg.convertID_bullet(bulletName),x,y,angle,aberration,speed,amount);
	}
	final void setDefaultEnemyBullet(int enemyID,int kind,double x,double y,int aberration,int speed,int amount){ //敵弾生成-自機自動照準
		this.setDefaultEnemyBullet(enemyID,kind,x,y,atan2(playerY - y,playerX - x),aberration,speed,amount);
	}
	final void setDefaultEnemyBullet(int enemyID,int kind,double x,double y,double angle,double aberration,int speed,int amount){ //敵弾生成-方向指定
		if(kind < 0 || cfg.bulletKindTotal <= kind)
			return;
		final double enemyAngle = this.enemyAngle[enemyID],
			enemySize = cfg.enemySize[enemyKind[enemyID]],
			bulletXData = x + enemySize*cos(enemyAngle), //弾設置地点x座標
			bulletYData = y + enemySize*sin(enemyAngle); //弾設置地点y座標
		final int limitFrame = cfg.bulletLimitFrame[kind]; //弾飛行時間
		boolean flashed = false; //発光確認
		for(;amount > 0 && bullet_total < bulletKind.length;amount--){
			final int newID = createBulletID();
			if(newID == -1)
				return;
			bulletKind[newID] = kind;
			bulletStr[newID] = 10; //一発10ダメージ
			bulletX[newID] = bulletXData;
			bulletY[newID] = bulletYData;
			bulletReflectiveness[newID] = 0;
			bulletReflectDamageRatio[newID] = 1.0;
			bulletOffSet[newID] = 0;
			bulletFollowTarget[newID] = NONE;
			bulletKnockBack[newID] = 2;
			bulletPenetration[newID] = 1;
			bulletAppearFrame[newID] = nowFrame;
			bulletLimitFrame[newID] = limitFrame;
			bulletLimitMove[newID] = MAX;
			bulletTeam[newID] = ENEMY;
			final double angle2 = angle + toRadians(random(-aberration,aberration));
			bulletXPower[newID] = speed*cos(angle2);
			bulletYPower[newID] = speed*sin(angle2);
			bulletXAccel[newID] = bulletYAccel[newID] = 0.0;
			bulletAngle[newID] = angle2;
			if(!flashed){ //発光処理
				setLight((int)bulletX[newID],(int)bulletY[newID],90);
				flashed = true;
			}
		}
	}
	
	//衝突判定系
	/**
	* 指定正方形エリアに敵が接触していないか調べる基本メソッドです。接触した敵のIDがint配列で返されます。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @return 接触している敵のID配列
	* @since ~beta7.0
	*/
	final int[] detectEnemyInArea(int x,int y,int size){ //敵衝突判定,ID取得
		return this.damageEnemyInArea(x,y,size,0,MAX);
	}
	/**
	* 指定正方形エリアに敵が接触していないか調べます。ただし接触した敵は指定数までしか調べません。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param limit 検索上限
	* @return 接触している敵のID配列
	* @since ~beta7.0
	*/
	final int[] detectEnemyInArea(int x,int y,int size,int limit){ //敵衝突判定,ID取得
		return this.damageEnemyInArea(x,y,size,0,limit);
	}
	/**
	* 指定正方形エリアに敵が接触していないか調べ、自動的に指定ダメージを与える基本メソッドです。
	* ついでにダメージを与えた敵IDも返します。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param damage 与えるダメージ
	* @return 接触している敵のID配列
	* @since ~beta7.0
	*/
	final int[] damageEnemyInArea(int x,int y,int size,int damage){ //敵衝突判定,ダメージ処理を並行,ID取得
		return this.damageEnemyInArea(x,y,size,damage,MAX);
	}
	/**
	* 指定正方形エリアに敵が接触していないか調べ、自動的に指定ダメージを与える基本メソッドです。ダメージを与える敵数に上限を設けれます。
	* ついでにダメージを与えた敵IDも返します。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param damage 与えるダメージ
	* @param limit 検索上限
	* @return 接触している敵のID配列
	* @since ~beta7.0
	*/
	final int[] damageEnemyInArea(int x,int y,int size,int damage,int limit){ //敵衝突判定,ダメージ処理を並行,判定数上限指定,ID取得
		if(!mapEnemyDetection(x,y,size)) //マップ座標内で敵の反応なし
			return new int[0]; //検出IDなし
		final int[] results = new int[min(limit,enemy_total)]; //結果ID
		int count = 0; //衝突数カウント
		for(int i = 0;i <= enemy_maxID && count < limit;i++){
			if(enemyKind[i] == NONE)
				continue;
			final int squareSize_2 = (cfg.enemySize[enemyKind[i]] + size)/2;
			if(abs((int)enemyX[i] - x) < squareSize_2 && abs((int)enemyY[i] - y) < squareSize_2 && enemyHP[i] > 0){ //衝突
				results[count++] = i; //IDを記録し衝突数をカウント
				if(damage != 0 && enemyHP[i] != MAX){ //ダメージ値が0ではない&敵のHPが無敵値ではない
					enemyHP[i] -= damage; //敵へダメージ
				}
				cfg.enemyClass[enemyKind[i]].damaged(i,damage); //追加コードの傷害追加処理を呼び出す
			}
		}
		return Arrays.copyOf(results,count); //衝突した敵のIDは返される、ダメージ処理でなく衝突IDだけがほしい場合はdamageを0に指定する。
	}
	/**
	* 指定正方形エリアにエンティティが接触していないか調べる基本メソッドです。接触したエンティティのIDがint配列で返されます。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @return 接触しているエンティティのID配列
	* @since ~beta7.0
	*/
	final int[] detectEntityInArea(int x,int y,int size,int team){ //エンティティ衝突判定,ID取得
		return this.damageEntityInArea(x,y,size,0,MAX,team);
	}
	/**
	* 指定正方形エリアにエンティティが接触していないか調べます。ただし接触したエンティティは指定数までしか調べません。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param limit 検索上限
	* @return 接触しているエンティティのID配列
	* @since ~beta7.0
	*/
	final int[] detectEntityInArea(int x,int y,int size,int limit,int team){ //エンティティ衝突判定,ID取得,判定数上限指定
		return this.damageEntityInArea(x,y,size,0,limit,team);
	}
	/**
	* 指定正方形エリアにエンティティが接触していないか調べ、自動的に指定ダメージを与える基本メソッドです。
	* ついでにダメージを与えたエンティティIDも返します。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param damage 与えるダメージ
	* @return 接触しているエンティティのID配列
	* @since ~beta7.0
	*/
	final int[] damageEntityInArea(int x,int y,int size,int damage,int team){ //エンティティ衝突判定,ダメージ処理を並行,ID取得
		return this.damageEntityInArea(x,y,size,damage,MAX,team);
	}
	/**
	* 指定正方形エリアにエンティティが接触していないか調べ、自動的に指定ダメージを与える基本メソッドです。ダメージを与えるエンティティ数に上限を設けれます。
	* ついでにダメージを与えたエンティティIDも返します。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param damage 与えるダメージ
	* @param limit 検索上限
	* @return 接触しているエンティティのID配列
	* @since ~beta7.0
	*/
	final int[] damageEntityInArea(int x,int y,int size,int damage,int limit,int team){ //エンティティ衝突判定,ダメージ処理を並行,判定数上限指定,ID取得
		final int[] results = new int[min(limit,entity_total)]; //結果ID
		int count = 0; //衝突数カウント
		for(int i = 0;i <= entity_maxID && count < limit;i++){
			if(entityKind[i] == NONE || entityTeam[i] == team)
				continue;
			final int squareSize_2 = (cfg.entitySize[entityKind[i]] + size)/2;
			if(abs((int)entityX[i] - x) < squareSize_2 && abs((int)entityY[i] - y) < squareSize_2 && entityHP[i] > 0){ //衝突
				results[count++] = i; //IDを記録し衝突数をカウント
				if(damage != 0 && entityHP[i] != MAX) //ダメージ値が0ではない&エンティティのHPが無敵値ではない
					entityHP[i] -= damage; //エンティティへダメージ
				cfg.entityClass[entityKind[i]].damaged(i,damage); //追加コードの傷害追加処理を呼び出す
			}
		}
		return Arrays.copyOf(results,count); //衝突したエンティティのIDは返される、ダメージ処理でなく衝突IDだけがほしい場合はdamageを0に指定する。
	}
	/**
	* 指定正方形範囲内の弾を検索し、指定hp数を削りきるまで弾を消失させます。特殊なオブジェクトの能動被弾処理に使えます。
	* 範囲内の弾は当たったものとして自動的に消去されるので、総ダメージ量だけを知りたい場合はdetectBulletDamageInAreaメソッドを使ってください。
	* @param hp 現在のhp 0以下になると探索終了
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param team 被弾対象チーム プレイヤー:PLAYER 敵：ENEMY 無所属(全部の弾を受ける)：NONE
	* @return 残ったhp
	* @since ~beta7.0
	*/
	final int receiveBulletInArea(int hp,int x,int y,int size,int team){ //自処理被弾(弾消滅)
		for(int i = 0;i <= bullet_maxID && hp > 0;i++){
			if(bulletKind[i] == NONE)
				continue;
			final int squareSize_2 = (cfg.bulletSize[bulletKind[i]] + size)/2;
			if((team != bulletTeam[i]) && abs((int)bulletX[i] - x) < squareSize_2 && abs((int)bulletY[i] - y) < squareSize_2){ //衝突
				hp -= bulletStr[i];
				deleteBullet(i); //弾破壊
			}
		}
		return hp; //残ったHPを返す
	}
	/**
	* 指定正方形範囲内の弾を検索し、総ダメージ量を返します。
	* これの呼び出しによって弾が消されることはありません。
	* receiveBulletInAreaが能動被弾処理に使われるのに対し、こちらは回避アルゴリズムなどでの使用が想定されます。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param team 被弾対象チーム プレイヤー:PLAYER 敵：ENEMY 無所属(全部の弾を受ける)：NONE
	* @return 総ダメージ量
	* @since ~beta7.0
	*/
	final int detectBulletDamageInArea(int x,int y,int size,int team){ //自処理被弾(弾保持)
		int totalDamage = 0;
		for(int i = 0;i <= bullet_maxID;i++){
			if(bulletKind[i] == NONE)
				continue;
			final int squareSize_2 = (cfg.bulletSize[bulletKind[i]] + size)/2;
			if((team != bulletTeam[i]) && abs((int)bulletX[i] - x) < squareSize_2 && abs((int)bulletY[i] - y) < squareSize_2) //衝突
				totalDamage += bulletStr[i];
		}
		return totalDamage; //総ダメージ量を返す
	}
	/**
	* 指定正方形範囲内にプレイヤーが接触しているかを調べます。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @return 接触しているか
	* @since ~beta7.0
	*/
	final boolean detectPlayerInArea(int x,int y,int size){ //プレイヤー衝突判定
		if(playerLife <= 0) //死んでいたとき
			return false;
		final int size2 = (playerSize + size)/2;
		if(abs(playerX - x) < size2 && abs(playerY - y) < size2)
			return true;
		return false;
	}
	/**
	* 指定正方形範囲内にプレイヤーが接触しているかを調べ、同時にdamageを与えます。
	* ダメージを与えられた場合trueが返されます。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @param damage 与えるダメージ
	* @return ダメージを与えたか
	* @since ~beta7.0
	*/
	final boolean damagePlayerInArea(int x,int y,int size,int damage){ //プレイヤーダメージ処理
		if(this.detectPlayerInArea(x,y,size)) //指定矩形内にプレイヤーが重なるかを判定
			return this.damagePlayer(damage);
		else
			return false;
	}

	//ダメージ系
	/**
	* プレイヤーにダメージを与える基本メソッドです。
	* ダメージを与えられた場合trueが返されます。
	* @param damage 与えるダメージ
	* @return ダメージを与えたか
	* @since beta9.0
	*/
	final boolean damagePlayer(int damage){
		if((playerBarrierTime <= 0 || damage < 0)){ //バリアが消滅しているか、逆ダメージのとき
			playerTargetHP -= damage;
			playerHPChangedFrame = nowFrame;
			setEffect("SPARK_RED",playerX,playerY,random(5,10));
			return true;
		}else
			return false;
	}
	/**
	* ギミック１つにダメージを与える基本メソッドです。
	* 内部的にはgimmickHP[map]が無敵(MAX,NONE定数)や死亡処理待ち(<0)でないことを確認してからdamageを与えます。
	* ダメージを与えれた場合trueが返されます。
	* @param map マップID
	* @param damage 与えるダメージ
	* @return ダメージを与えたか
	* @since beta8.0
	*/
	final boolean damageGimmick(int map,int damage){ //ギミックへダメージ
		if(gimmickHP[map] > 0 && gimmickHP[map] != MAX){
			gimmickHP[map] -= damage;
			return true;
		}
		return false;
	}
	
	//高速化系
	/**
	* 指定正方形内に敵がいる可能性があるかを高速で調べます。
	* 当たり判定の前処理として使うことができます。
	* 内部的には交差するenemyMapの中にtrueのマップがあるかを調べています。
	* @param x x座標
	* @param y y座標
	* @param size 正方形半径
	* @return 敵がいる可能性があるか
	* @since beta8.0
	*/
	final boolean mapEnemyDetection(int x,int y,int size){ //占有マップ座標内に敵の痕跡があるか、敵衝突判定の前準備
		size /= 2;
		int x1_grid = (x - size)/100,y1_grid = (y - size)/100; //自身の左上の点から右下の点への正方形で
		final int x2_grid = (x + size)/100,y2_grid = (y + size)/100; //衝突するエリアがないか検査
		final int yStart = y1_grid;
		int mapPoint;
		for(;x1_grid <= x2_grid;x1_grid++){
			for(y1_grid = yStart;y1_grid <= y2_grid;y1_grid++){
				if(outOfStage_grid(x1_grid,y1_grid))
					return false;
				if(enemyMap.get(x1_grid*stageGridH + y1_grid))
					return true;
			}
		}
		return false;
	}
	final boolean squareHitGimmick(int x,int y,int size){ //壁・ギミックとの衝突判定
		if(size < 100){ //小サイズ用高速衝突判定
			final int map = x/100*stageGridH + y/100;
			if(gimmickSweeper[map] == 0)
				return false;
		}
		//通常衝突判定
		size /= 2;
		int x1_grid = (int)(x - size)/100,y1_grid = (int)(y - size)/100; //自身の左上の点から右下の点への正方形で
		final int x2_grid = (int)(x + size)/100,y2_grid = (int)(y + size)/100; //衝突するエリアがないか検査
		for(int gridX = x1_grid;gridX <= x2_grid;gridX++){
			for(int gridY = y1_grid;gridY <= y2_grid;gridY++){
				if(outOfStage_grid(gridX,gridY))
					return false;
				if(gimmickHP[gridX*stageGridH + gridY] > 0)
					return true;
			}
		}
		return false;
	}
	/**
	* 指定範囲に衝突するギミックのIDを一つ返します。
	* @param x 検出地点x座標
	* @param y 検出地点y座標
	* @param size 検出範囲
	* @since ~beta7.0
	*/
	final int getSquareHitGimmickID(int x,int y,int size){ //壁・ギミックとの衝突判定とID検出(一つのみ)
		if(size < 100){ //小サイズ用高速衝突判定
			final int map = x/100*stageGridH + y/100;
			if(gimmickSweeper[map] == 0)
				return NONE;
		}
		//通常衝突判定
		size /= 2;
		int x1 = (int)(x - size)/100,y1 = (int)(y - size)/100; //自身の左上の点から右下の点への正方形で
		final int x2 = (int)(x + size)/100,y2 = (int)(y + size)/100; //衝突するエリアがないか検査
		for(int i = x1;i <= x2;i++){
			for(int j = y1;j <= y2;j++){
				try{
					final int map = i*stageGridH + j;
					if(gimmickHP[map] > 0)
						return map;
				}catch(ArrayIndexOutOfBoundsException e){
					return NONE;
				}
			}
		}
		return NONE;
	}
	/**
	* 指定範囲に衝突するギミックのIDをすべて返します。
	* @param x 検出地点x座標
	* @param y 検出地点y座標
	* @param size 検出範囲
	* @param limit 検出数
	* @since beta8.0
	*/
	final int[] getSquareHitGimmickID(int x,int y,int size,int limit){ //壁・ギミックとの衝突判定とID検出(複数)
		if(size < 100){ //小サイズ用高速衝突判定
			final int map = x/100*stageGridH + y/100;
			if(map < 0 || stageGridTotal <= map || gimmickSweeper[map] == 0) //場外またはスイーパーより検出なし
				return new int[0];
		}
		//通常衝突判定
		int[] results = new int[min(16,limit)]; //結果ID
		int resultTotal = 0; //衝突量
		size /= 2;
		int x1 = (int)(x - size)/100,y1 = (int)(y - size)/100; //自身の左上の点から右下の点への正方形で
		final int x2 = (int)(x + size)/100,y2 = (int)(y + size)/100; //衝突するエリアがないか検査
		label:
		for(int i = x1;i <= x2;i++){
			for(int j = y1;j <= y2;j++){
				try{
					final int map = i*stageGridH + j;
					if(0 <= map && map < stageGridTotal && gimmickHP[map] > 0){ //場内かつ検出あり
						if(resultTotal == results.length)
							results = Arrays.copyOf(results,resultTotal*2); //配列拡張
						results[resultTotal++] = map; //衝突ギミックを登録
					}
				}catch(ArrayIndexOutOfBoundsException e){
					break label;
				}
			}
		}
		return Arrays.copyOf(results,resultTotal);
	}
	/**
	* (未実装)正方形がある地点から別の地点から移動する際に衝突するすべてのギミックのIDを返します。
	* @param x 検出地点x座標
	* @param y 検出地点y座標
	* @param size 検出範囲
	* @param limit 検出数
	* @since beta9.0
	*/
	final int[] getSquareHitGimmickID_Line(int x,int y,int size,int limit){ //正方形軌道上の壁・ギミックとの衝突判定とID検出
		if(size < 100){ //小サイズ用高速衝突判定
			final int map = x/100*stageGridH + y/100;
			if(map < 0 || stageGridTotal <= map || gimmickSweeper[map] == 0) //場外またはスイーパーより検出なし
				return new int[0];
		}
		//通常衝突判定
		int[] results = new int[min(16,limit)]; //結果ID
		int resultTotal = 0; //衝突量
		size /= 2;
		int x1 = (int)(x - size)/100,y1 = (int)(y - size)/100; //自身の左上の点から右下の点への正方形で
		final int x2 = (int)(x + size)/100,y2 = (int)(y + size)/100; //衝突するエリアがないか検査
		label:
		for(int i = x1;i <= x2;i++){
			for(int j = y1;j <= y2;j++){
				try{
					final int map = i*stageGridH + j;
					if(0 <= map && map < stageGridTotal && gimmickHP[map] > 0){ //場内かつ検出あり
						if(resultTotal == results.length)
							results = Arrays.copyOf(results,resultTotal*2); //配列拡張
						results[resultTotal++] = map; //衝突ギミックを登録
					}
				}catch(ArrayIndexOutOfBoundsException e){
					break label;
				}
			}
		}
		return Arrays.copyOf(results,resultTotal);
	}
	
	//描画系
	/**
	* 指定した画像をBreakScopeへ描画する基本メソッドです。
	* @param img 描画画像
	* @param x 描画位置x
	* @param y 描画位置y
	* @since beta8.0
	*/
	final void drawImageBS(Image img,int x,int y){
		g2.drawImage(img,x,y,this);
	}
	/**
	* 指定寸法で指定した画像をBreakScopeへ描画する基本メソッドです。w,hも設定できます。
	* @param img 描画画像
	* @param x 描画位置x
	* @param y 描画位置y
	* @param w 横幅
	* @param h 縦幅
	* @since beta8.0
	*/
	final void drawImageBS(Image img,int x,int y,int w,int h){
		g2.drawImage(img,x,y,w,h,this);
	}
	/**
	* 指定座標を中心として指定した画像をBreakScopeに描画する基本メソッドです。
	* @param img 描画画像
	* @param x 描画中心位置x
	* @param y 描画中心位置y
	* @since beta8.0
	*/
	final void drawImageBS_centerDot(Image img,int x,int y){
		g2.drawImage(img,x - img.getWidth(null)/2,y - img.getHeight(null)/2,this);
	}
	//その他の汎用メソッド
	/**
	* 指定範囲内の整数の乱数を返すメソッドです。
	* 例：random(1,10) 1~10の整数がランダムで返される
	* @param num1 乱数範囲上限(または下限)
	* @param num2 乱数範囲下限(または上限)
	* @return 返される乱数
	* @since ~beta7.0
	*/
	final int random(int num1,int num2){ //整数乱数発生メソッド
		if(num1 == num2)
			return num1;
		else if(num1 > num2)
			return num2 + rnd.nextInt(num1 - num2 + 1);
		else
			return num1 + rnd.nextInt(num2 - num1 + 1);
	}
	/**
	* 指定絶対値以下の整数の乱数を返すメソッドです。
	* 例：random2(10) -10~10の整数がランダムで返される
	* @param num 乱数絶対値上限
	* @return 返される乱数
	* @since beta9.0
	*/
	final int random2(int num){
		return -num + rnd.nextInt(num*2 + 1);
	}
	/**
	* 指定範囲内の少数の乱数を返すメソッドです。
	* 例：random(0.0,3.14) 0.0~3.14の少数がランダムで返される
	* @param num1 乱数範囲下限
	* @param num2 乱数範囲上限
	* @return 返される乱数
	* @since ~beta7.0
	*/
	final double random(double num1,double num2){ //小数乱数発生メソッド
		if(num1 == num2)
			return num1;
		else if(num1 > num2)
			return num2 + rnd.nextDouble()*(num1 - num2);
		else
			return num1 + rnd.nextDouble()*(num2 - num1);
	}
	/**
	* 指定絶対値以下の整数の乱数を返すメソッドです。
	* 例：random2(0.5) -0.5~0.5の少数がランダムで返される
	* @param num 乱数絶対値上限
	* @return 返される乱数
	* @since beta9.0
	*/
	final double random2(double num){
		return -num + rnd.nextDouble()*(num*2);
	}
	final double angleFormat(double radian){ //ラジアン整理メソッド -PI~+PIに直す
		radian %= PI*2;
		if(radian > PI)
			radian -= PI*2;
		else if(radian <= -PI)
			radian += PI*2;
		return radian;
	}
	/**
	* ステージに使われる、全情報をリセットします。ステージ読み込みの前処理として呼び出されます。
	* @since ~beta7.0
	*/
	final void resetData(){ //全情報リセット
		//プレーヤー
		playerRidingID = NONE;
		playerBarrierTime = playerBarrierTime_default;
		cureStore = cureStore_default;
		playerLife = 4;
		playerHP = playerTargetHP = hp_max;
		damageTaken = 0;
		playerHPChangedFrame = 0;
		playerComment = "";
		//システム
		gameTime = 0;
		maxScore = mainScore = 0;
		focusing = NONE;
		clearFrame = NONE;
		if(nowEvent != OPENING) //起動時の呼び出しを除き、タイトルへ移行する処理を付加
			eventChange(TITLE);
		//オブジェクト
		Arrays.fill(enemyKind,NONE);
		Arrays.fill(entityKind,NONE);
		Arrays.fill(bulletKind,NONE);
		Arrays.fill(effectKind,NONE);
		Arrays.fill(itemKind,NONE);
		Arrays.fill(attackPlanFrame,MAX);
		//追加コードの全削除追加処理の呼び出し
		for(EnemyListener ver : cfg.enemyClass)
			ver.cleared();
		for(EntityListener ver : cfg.entityClass)
			ver.cleared();
		for(GimmickListener ver : cfg.gimmickClass)
			ver.cleared();
		//最大ID,総数記録の初期化
		bullet_maxID = enemy_maxID = entity_maxID = item_maxID = effect_maxID = -1;
		bullet_total = enemy_total = entity_total = item_total = effect_total = gimmick_total = 0;
		//※ギミックは検索方法の違いにより最大IDがない
		//※マップ系変数は、最大配列数がマップ総数に依存するため、ステージ読み込み直後に初期化される
		//弾薬数初期化
		itemPossession[AMMO_HANDGUN] = 64;
		itemPossession[AMMO_SHOTGUN] = 12;
		itemPossession[AMMO_MACHINEGUN] = 240;
		itemPossession[AMMO_ASSAULT_RIFLE] = 150;
		itemPossession[AMMO_SNIPER] = 8;
		itemPossession[AMMO_GRENADE] = 5;
		itemPossession[AMMO_ROCKET] = 1;
		itemPossession[AMMO_BATTERY] = 250;
		itemPossession[INFINITY_POWER] = MAX;
		//BGM調整
		battleBGM[nowBattleBGM].stop();
		titleBGM.loop();
	}
	/**
	* このピクセル座標がステージ外であるかを調べます。
	* @param x x座標
	* @param y y座標
	* @return ステージ外のときtrue,そうでなければfalse
	* @since beta9.0
	*/
	final boolean outOfStage_pixel(int x,int y){
		return x < 0 || y < 0 || x >= stageW || y >= stageH;
	}
	/**
	* このグリッド座標がステージ外であるかを調べます。
	* @param gridX xグリッド座標
	* @param gridY yグリッド座標
	* @return ステージ外のときtrue,そうでなければfalse
	* @since beta9.0
	*/
	final boolean outOfStage_grid(int gridX,int gridY){
		return gridX < 0 || gridY < 0 || gridX >= stageGridW || gridY >= stageGridH;
	}
	/**
	* この物体が完全にスクリーン外であるかを調べます。
	* 描画処理冒頭でよく使われます。
	* @param x x座標
	* @param y y座標
	* @param w 横の長さ
	* @param w 縦の長さ
	* @return スクリーン外のときtrue,そうでなければfalse
	* @since beta9.0
	*/
	final boolean outOfScreen_pixel(int x,int y,int w,int h){
		final int xd = playerX - x,yd = playerY - y;
		final int halfW = (defaultScreenW + w)/2,halfH = (defaultScreenH + h)/2;
		return xd < -halfW || halfW < xd || yd < -halfH || halfH < yd;
	}
	/**
	* この画像が無回転のとき完全にスクリーン外であるかを調べます。
	* 描画処理冒頭でよく使われます。
	* @param img 画像
	* @param x x座標
	* @param y y座標
	* @return スクリーン外のときtrue,そうでなければfalse
	* @since beta9.0
	*/
	final boolean outOfScreen_img(Image img,int x,int y){
		final int xd = playerX - x,yd = playerY - y;
		final int halfW = (defaultScreenW + img.getWidth(null))/2,halfH = (defaultScreenH + img.getHeight(null))/2;
		return xd < -halfW || halfW < xd || yd < -halfH || halfH < yd;
	}
	/**
	* この画像が回転しても確実にスクリーン外であるかを調べます。
	* 内部的には、矩形の大きさを２倍してスクリーン内に侵入するかを調べています。
	* 描画処理冒頭でよく使われます。
	* @param img 画像
	* @param x x座標
	* @param y y座標
	* @return スクリーン外のときtrue,そうでなければfalse
	* @since beta9.0
	*/
	final boolean outOfScreen_img2(Image img,int x,int y){
		final int xd = playerX - x,yd = playerY - y;
		final int halfW = defaultScreenW/2 + img.getWidth(null),halfH = defaultScreenH/2 + img.getHeight(null);
		return xd < -halfW || halfW < xd || yd < -halfH || halfH < yd;
	}
	/**
	* このint値は特別な意味が含まれない実数値ゾーンであるかを調べます。
	* BreakScopeでは一部の変数に特殊な意味を持たせた数値を代入し、違った挙動を示すような仕組みがあります。
	* (例：gimmickHPは定数MAXのとき破壊不能,定数NONEのとき衝突判定なし)
	* 実数値と混同しないため、このゾーンは限界値付近であることが多いようになっています。
	* @param value 調べる値
	* @return 実数値であるときtrue,そうでなければfalse
	* @since beta8.0
	*/
	final static boolean isActualNumber(int value){
		switch(value){
		case NONE:
		case MAX:
		case MIN:
		case SELF:
		case REVERSE_SELF:
		case TARGET:
		case REVERSE_TARGET:
		case FOCUS:
			return false;
		}
		return true;
	}
	/**
	* このdouble値は特別な意味が含まれない実数値ゾーンであるかを調べます。
	* BreakScopeでは一部の変数に特殊な意味を持たせた数値を代入し、違った挙動を示すような仕組みがあります。
	* (例：gimmickHPが定数MAXのとき破壊不能,定数NONEのとき衝突判定なし)
	* 実数値と混同しないため、このゾーンは限界値付近であることが多いようになっています。
	* なお、NaNや[NEGATIVE/POSITIVE]_INFINITYでもfalseが返ってきます。
	* @param value 調べる値
	* @return 実数値とであるときtrue,そうでなければfalse
	* @since beta8.0
	*/
	final static boolean isActualNumber(double value){
		if(	value == Double.NaN ||
			value == Double.NEGATIVE_INFINITY ||
			value == Double.POSITIVE_INFINITY ||
			!isActualNumber((int)value))
			return false;
		else
			return true;
	}
	/**
	* マウスポインタの座標が指定矩形内にあるかを判定します。
	* @param x 矩形左上x座標
	* @param x 矩形左上y座標
	* @param w 矩形横幅
	* @param h 矩形縦幅
	* @return 矩形内であればtrue,そうでなければfalse
	* @since beta8.0
	*/
	boolean focusInArea(int x,int y,int w,int h){
		if(x < focusX && focusX < x + w && y < focusY && focusY < y + h)
			return true;
		else
			return false;
	}
	/**
	* 画像を読み込む基本メソッドです。source/pictureフォルダから指定パスを探します。エラーが起きた際、エラー場所の名前を表示させることができます。
	* 追加コードなどはこのメソッドを使って追加画像を読み込ませることができます。
	* @param url 画像パス
	* @param errorSource 読み込みエラーの際、表示させるエラー場所の名前　(例:loadImage("img.png","敵画像読み込み")
	* @return 画像Image値
	* @since ~beta7.0
	*/
	Image loadImage(String url,String errorSource){ //画像読み込みメソッド
		//読み込んだことがある画像かURLより確認する
		for(int id = 0;id < arrayImageURL.length;id++){
			if(url.equals(arrayImageURL[id])) //URL登録済み
				return arrayImage[id]; //画像を返す
		}
		//新しい画像URL
		Image img = null;
		try{
			if(getClass().getResource(url) != null)
				img = createImage((ImageProducer)getClass().getResource(url).getContent());
			else
				img = createImage((ImageProducer)getClass().getResource("picture//" + url).getContent());
			tracker.addImage(img,1);
		}catch(IOException | NullPointerException e){ //異常-読み込み失敗
			if(errorSource != null && !errorSource.isEmpty())
				warningBox("画像\"" + url + "\"がロードできませんでした。この画像は描画されません。\n場所：" + errorSource,"読み込みエラー");
			else
				warningBox("画像\"" + url + "\"がロードできませんでした。この画像は描画されません。\n場所：指定なし","読み込みエラー");
			return createImage(1,1);
		}
		//正常-画像URLを保存
		arrayImage_maxID++; //画像URL数を増やす
		if(arrayImage_maxID == arrayImage.length){ //手動配列延長
			arrayImage = Arrays.copyOf(arrayImage,arrayImage_maxID*2);
			arrayImageURL = Arrays.copyOf(arrayImageURL,arrayImage_maxID*2);
		}
		arrayImageURL[arrayImage_maxID] = url; //この画像URLを登録
		arrayImage[arrayImage_maxID] = img; //この画像を登録
		return img;
	}
	/**
	* 画像を読み込む基本メソッドです。source/pictureフォルダから指定パスを探します。
	* 追加コードなどはこのメソッドを使って追加画像を読み込ませることができます。
	* エラー元の指定を省略したバージョンです。
	* @param url 画像パス
	* @return 画像Image値
	* @since ~beta7.0
	*/
	Image loadImage(String url){ //画像読み込みメソッド
		return this.loadImage(url,null);
	}
	/**
	* 音声を読み込む基本メソッドです。source/mediaフォルダから指定パスを探します。
	* 追加コードなどはこのメソッドを使って追加音声を読み込ませることができます。
	* このとき発生したエラーはSoundClipより報告されます。
	* @param url 画像パス
	* @return 音声SoundClip値
	* @since beta8.0
	*/
	SoundClip loadMedia(String url){ //画像読み込みメソッド
		if(getClass().getResource(url) == null)
			url = "media/" + url;
		//読み込んだことがある音声かURLより確認する
		for(int id = 0;id < arrayMediaURL.length;id++){
			if(url.equals(arrayMediaURL[id])) //URL登録済み
				return arrayMedia[id]; //音声を返す
		}
		//新しい音声URL
		arrayMedia_maxID++; //音声URL数を増やす
		if(arrayMedia_maxID == arrayMedia.length){ //手動配列延長
			arrayMedia = Arrays.copyOf(arrayMedia,arrayMedia_maxID*2);
			arrayMediaURL = Arrays.copyOf(arrayMediaURL,arrayMedia_maxID*2);
		}
		//この音声のURLを登録
		//この音声をSoundClipに変換し登録
		return arrayMedia[arrayMedia_maxID] = new SoundClip(arrayMediaURL[arrayMedia_maxID] = url);
	}
	/**
	* フォントを読み込むメソッドです。
	* 内部的には、Font.createFontメソッドを呼び出しています。
	* @param filename フォントパス
	* @return 読み込まれたFont型
	* @since ~beta7.0
	*/
	final Font createFont(String filename){ //フォント読み込みメソッド
		try{
			final Font font = Font.createFont(Font.TRUETYPE_FONT,getClass().getResourceAsStream(filename));
			return font;
		}catch (IOException | FontFormatException e){
			warningBox("フォントの読み込みに失敗しました。このフォントはデフォルトのものが適用されます。","読み込みエラー");
			return new Font(Font.SERIF,Font.PLAIN,12);
		}
	}
	/**
	* コンフィグを読み込むメソッドです。タイトル画面時にF_9キーを押して呼び出すことができます。
	* コンフィグをビルドし直した後、これを呼び出すことでゲームを再起動をしなくてもMODやコンフィグの再読み込みができます。
	* @param firstLoad 初期起動か(再起動だった場合コンソール表示が変化する)
	* @since beta9.0
	*/
	final void loadConfig(boolean firstLoad){
		//コンフィグ本体読み込み
		if(!firstLoad)
			System.out.println("\nStart Config Reload...");
		final long usedTime = System.currentTimeMillis();
		try(ObjectInputStream ois = new ObjectInputStream(getClass().getResourceAsStream(CFG_URL))){
			cfg = (Config)ois.readObject();
		}catch(IOException | ClassNotFoundException e){
			JOptionPane.showMessageDialog(null,CFG_URL + "の読み込みに失敗しました。\n全ステージに入れない可能性があります。\n「ConfigLoaderコンパイル.bat」の起動をお試しください。","config読み込みエラー",JOptionPane.ERROR_MESSAGE);
			cfg = new Config();
		}
		//敵クラスの準備
		for(EnemyListener ver : cfg.enemyClass)
			ver.construct(this);
		
		//ギミッククラスの準備
		for(GimmickListener ver : cfg.gimmickClass)
			ver.construct(this);
		
		//エンティティクラスの準備
		for(EntityListener ver : cfg.entityClass)
			ver.construct(this);
		//武器効果音の読み込み
		final int seLength = cfg.weaponKindTotal;
		weaponSE = new SoundClip[seLength];
		for(int i = 0;i < seLength;i++){
			if(isActualString(cfg.weaponSE[i]))
				weaponSE[i] = loadMedia(cfg.weaponSE[i]);
			else
				weaponSE[i] = new SoundClip();
		}
		//敵画像の読み込み
		enemyImg = new Image[cfg.enemyKindTotal];
		enemyIconImg = new Image[cfg.enemyKindTotal];
		for(int kind = 0;kind < cfg.enemyKindTotal;kind++){
			final String NAME = cfg.enemyImg[kind];
			if(isActualString(NAME))
				enemyImg[kind] = loadImage(NAME,"enemy「" + cfg.enemyName[kind] + "」の画像読み込み");
			else
				enemyImg[kind] = nullImg;
			final String NAME2 = cfg.enemyIconImg[kind];
			if(!isActualString(NAME2) || NAME2.equals(NAME))
				enemyIconImg[kind] = enemyImg[kind];
			else
				enemyIconImg[kind] = loadImage(cfg.enemyIconImg[kind],"enemy「" + cfg.enemyName[kind] + "」のアイコン画像読み込み");
		}
		
		//エンティティ画像の読み込み
		entityImg = new Image[cfg.entityKindTotal];
		entityIconImg = new Image[cfg.entityKindTotal];
		for(int kind = 0;kind < cfg.entityKindTotal;kind++){
			final String NAME = cfg.entityImg[kind];
			if(isActualString(NAME))
				entityImg[kind] = loadImage(NAME,"entity「" + cfg.entityName[kind] + "」の画像読み込み");
			else
				entityImg[kind] = nullImg;
			final String NAME2 = cfg.entityIconImg[kind];
			if(!isActualString(NAME2) || NAME2.equals(NAME))
				entityIconImg[kind] = entityImg[kind];
			else
				entityIconImg[kind] = loadImage(cfg.entityIconImg[kind],"entity「" + cfg.entityName[kind] + "」のアイコン画像読み込み");
		}
		
		//ギミック画像の読み込み
		gimmickImg = new Image[cfg.gimmickKindTotal];
		gimmickIconImg = new Image[cfg.gimmickKindTotal];
		for(int kind = 0;kind < cfg.gimmickKindTotal;kind++){
			final String NAME = cfg.gimmickImg[kind];
			if(isActualString(NAME))
				gimmickImg[kind] = loadImage(NAME,"gimmick「" + cfg.gimmickName[kind] + "」の画像読み込み");
			else
				gimmickImg[kind] = nullImg;
			final String NAME2 = cfg.gimmickIconImg[kind];
			if(!isActualString(NAME2) || NAME2.equals(NAME))
				gimmickIconImg[kind] = gimmickImg[kind];
			else
				gimmickIconImg[kind] = loadImage(cfg.gimmickIconImg[kind],"gimmick「" + cfg.gimmickName[kind] + "」のアイコン画像読み込み");
		}
		
		//武器画像の読み込み
		weaponIconImg = new Image[cfg.weaponKindTotal];
		for(int kind = 0;kind < cfg.weaponKindTotal;kind++){
			final String NAME = cfg.weaponIconImg[kind];
			if(isActualString(NAME))
				weaponIconImg[kind] = loadImage(NAME,"weapon「" + cfg.weaponName[kind] + "」の画像読み込み");
			else
				weaponIconImg[kind] = nullImg;
		}
		
		//弾画像の読み込み
		bulletImg = new Image[cfg.bulletKindTotal];
		for(int kind = 0;kind < cfg.bulletKindTotal;kind++){
			final String NAME = cfg.bulletImg[kind];
			if(isActualString(NAME))
				bulletImg[kind] = loadImage(NAME,"bullet「" + cfg.bulletName[kind] + "」の画像読み込み");
			else
				bulletImg[kind] = nullImg;
		}
		
		//エフェクト画像の読み込み
		effectImg = new Image[cfg.effectKindTotal][];
		for(int kind = 0;kind < cfg.effectKindTotal;kind++){
			final int imgLength = cfg.effectImg[kind].length;
			effectImg[kind] = new Image[imgLength];
			for(int j = 0;j < imgLength;j++)
				effectImg[kind][j] = loadImage(cfg.effectImg[kind][j],"effect「" + cfg.effectName[kind] + "」のアニメーション" + cfg.effectImg[kind][j] + "の画像読み込み");
		}
		try{
			tracker.waitForAll(); //画像ロード
		}catch(InterruptedException e){}
		//装備品の読み込み
		final int[] arsenal = new int[cfg.weaponKindTotal];
		int stocked = 0;
		int slotID = -1;
		for(int i = 0;;i++){
			if(i >= cfg.weaponKindTotal){ //検索終了
				weaponStockTotal = stocked; //在庫の武器数
				break;
			}
			if(cfg.weaponCost[i] == 0){ //コスト０の武器(初期装備)を倉庫にストックする
				arsenal[stocked++] = i;
				if(++slotID < weaponSlot.length)
					weaponSlot[slotID] = i;
			}
		}
		for(int i = stocked;i < weaponSlot.length;i++)
			weaponSlot[i] = NONE;
		weaponArsenal = Arrays.copyOf(arsenal,stocked);
		//所要時間表示
		if(firstLoad)
			System.out.println("Config&Mod: " + (System.currentTimeMillis() - usedTime));
		else{
			System.out.println("config reload complete!");
			System.out.println("TimeUsed: " + (System.currentTimeMillis() - usedTime) + "(ms)");
		}
	}
	final static String trim2(String str){
		if(str == null || str.length() == 0)
			return "";
		for(int i = 0;;i++){
			if(i >= str.length()){
				str = "";
				break;
			}
			final char c = str.charAt(i);
			if(c == ' ' || c == '　'){
				continue;
			}else{
				str = str.substring(i);
				break;
			}
		}
		for(int i = str.length() - 1;i >= 0;i--){
			final char c = str.charAt(i);
			if(c == ' ' || c == '　'){
				continue;
			}else{
				str = str.substring(0,i + 1);
				break;
			}
		}
		return str;
	}
	/**
	* 文字列をtokenで分割して配列で返す、String.splitメソッドのブレスコ版です。
	* 分割された文字は、さらに前後の半角/全角空白を除去されます。
	* 指定された文字列がnullであったときは空配列が返され、例外は投げません。
	* @param str 分割される文字列
	* @param token 分割に使うトークン
	* @return 分割された文字配列
	* @since beta8.0
	*/
	final static String[] split2(String str,String token){
		if(!isActualString(str))
			return new String[0];
		final String[] strs = str.split(token);
		for(int i = 0;i < strs.length;i++)
			strs[i] = trim2(strs[i]);
		return strs;
	}
	/**
	* String値が無効値ではないことを検証します。
	*/
	final static boolean isActualString(String value){
		if(value != null && !value.isEmpty() && !value.equalsIgnoreCase("NONE"))
			return true;
		return false;
	}
	/**
	* String配列が無効値ではないことを検証します。
	*/
	final static boolean isActualString(String[] value){
		if(value != null && value.length > 0 && !value[0].isEmpty() && !value[0].equalsIgnoreCase("NONE"))
			return true;
		return false;
	}
	/**
	* 度をラジアンへ変換しますが、実数値ではない値を保持します。
	* たとえば、コンフィグ内の角度項目で指定できる"TARGET"や"SELF"などの特殊値はこのメソッドを通しても値は変化しません。
	* 内部的には実際の掛け算で求めています。
	* @param ラジアンに変換する数値
	* @return 変換された値
	* @since beta9.0
	*/
	final static double toRadians2(double degress){
		if(isActualNumber(degress))
			return degress*PI/180;
		else
			return degress;
	}
	/**
	* コンソールに複数行の文字を表示させます。デバックとして使えます。
	* @param strs 表示する文字列
	* @since beta9.0
	*/
	final static void debug(String... strs){
		for(int i = 0;i < strs.length;i++)
			System.out.println(strs[i]);
	}
	/**
	* メッセージウィンドウの表示を行います。
	*　また、このウィンドウを表示していた時間を返します。
	* この間にイベントがゲームであった場合、ポーズ画面に移行することがあります。
	* 内部的にはJOptionPane.showMessageDialogメソッドを呼んでいます。
	* @param message 表示するメッセージ
	* @param title ダイアログのタイトル文字列
	* @return ダイアログが閉じられるまでの経過時間
	* @since beta9.0
	*/
	final static long messageBox(String message,String title){
		long openTime = System.currentTimeMillis();
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.INFORMATION_MESSAGE);
		return System.currentTimeMillis() - openTime;
	}
	/**
	* 警告ウィンドウの表示を行います。
	*　また、このウィンドウを表示していた時間を返します。
	* この間にイベントがゲームであった場合、ポーズ画面に移行することがあります。
	* 内部的にはJOptionPane.showMessageDialogメソッドを呼んでいます。
	* @param message 表示する警告
	* @param title ダイアログのタイトル文字列
	* @return ダイアログが閉じられるまでの経過時間
	* @since beta9.0
	*/
	final static long warningBox(String message,String title){
		long openTime = System.currentTimeMillis();
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.WARNING_MESSAGE);
		return System.currentTimeMillis() - openTime;
	}
	/**
	* エラーウィンドウの表示を行います。
	*　また、このウィンドウを表示していた時間を返します。
	* この間にイベントがゲームであった場合、ポーズ画面に移行することがあります。
	* 内部的にはJOptionPane.showMessageDialogメソッドを呼んでいます。
	* @param message 表示するエラー内容
	* @param title ダイアログのタイトル文字列
	* @return ダイアログが閉じられるまでの経過時間
	* @since beta9.0
	*/
	final static long errorBox(String message,String title){
		long openTime = System.currentTimeMillis();
		JOptionPane.showMessageDialog(null,message,title,JOptionPane.ERROR_MESSAGE);
		return System.currentTimeMillis() - openTime;
	}
}