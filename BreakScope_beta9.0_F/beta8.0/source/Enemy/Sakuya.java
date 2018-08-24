import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class Sakuya extends EnemyListener{
	boolean bgmPlaying = false;
	SoundClip bgm;
	Image SakuyaImg1,
		SakuyaImg2;
	
	@Override
	public void construct(BreakScope bs){
		super.construct(bs);
		//追加音声ファイル
		SakuyaImg1 = bs.loadImage("Sakuya/Sakuya.png");
		SakuyaImg2 = bs.loadImage("Sakuya/透過型雷電砲.png");
		bgm = new SoundClip("source/media/night of knights(re2).mp3");
		//追加コンフィグ
		//ID変換マップを作る
		//武器
		bs.cfg.autoTagger_startOperation(bs.cfg.nameToID_weapon);
		bs.cfg.autoTagger_execute("幻符「殺人ドール」");
		bs.cfg.autoTagger_execute("doll2");
		bs.cfg.autoTagger_execute("Waltz");
		bs.cfg.autoTagger_execute("Rondo");
		bs.cfg.autoTagger_execute("Rondo2");
		bs.cfg.autoTagger_execute("Rondo3");
		bs.cfg.autoTagger_execute("Rondo4");
		bs.cfg.autoTagger_execute("Rondo5");
		bs.cfg.autoTagger_execute("Rondo6");
		bs.cfg.autoTagger_execute("Rondo7");
		bs.cfg.autoTagger_execute("Rondo8");
		bs.cfg.autoTagger_execute("Magicsword");
		bs.cfg.autoTagger_execute("速符「ルミネスリコシェ」");
		bs.cfg.autoTagger_execute("ルミネス");
		bs.cfg.autoTagger_execute("回転knife");
		bs.cfg.autoTagger_execute("弾幕knife");
		bs.cfg.autoTagger_execute("弾幕knife2");
		bs.cfg.autoTagger_execute("弾幕knife3");
		bs.cfg.autoTagger_execute("弾幕knife4");
		bs.cfg.autoTagger_execute("弾幕knife5");
		bs.cfg.autoTagger_execute("弾幕knife6");
		bs.cfg.autoTagger_execute("離剣の見");
		bs.cfg.autoTagger_execute("離剣の見2");
		//弾
		bs.cfg.autoTagger_startOperation(bs.cfg.nameToID_bullet);
		bs.cfg.autoTagger_execute("doll");
		bs.cfg.autoTagger_execute("doll2");
		bs.cfg.autoTagger_execute("Concerto");
		bs.cfg.autoTagger_execute("Concerto2");
		bs.cfg.autoTagger_execute("Concerto3");
		bs.cfg.autoTagger_execute("Concerto4");
		bs.cfg.autoTagger_execute("Concerto5");
		bs.cfg.autoTagger_execute("Concerto6");
		bs.cfg.autoTagger_execute("Concerto7");
		bs.cfg.autoTagger_execute("Concerto8");
		bs.cfg.autoTagger_execute("Starsword");
		bs.cfg.autoTagger_execute("C");
		bs.cfg.autoTagger_execute("C2");
		bs.cfg.autoTagger_execute("回転knife");
		bs.cfg.autoTagger_execute("弾幕knife");
		bs.cfg.autoTagger_execute("離剣");
		bs.cfg.autoTagger_execute("離剣2");
		//開始
		addNewWeapon();
		addNewBullet();
	}
	public void addNewWeapon(){
		//追加準備
		int nowWeaponLength = bs.cfg.weaponKindTotal;//今登録されている武器の総数
		final int kosu = 23; //追加個数
		
		//全配列の長さを増やす
		bs.cfg.expandsData_weapon(kosu);

		//雛形//////////////////////////
		/*
		bs.cfg.weaponName[nowWeaponLength] = "新しい武器";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾A","弾B",...);
		bs.cfg.weaponStrength[nowWeaponLength] = 武器の威力;
		bs.cfg.weaponCost[nowWeaponLength] = 武器コスト、0で初期武器、-1で通常使用不可;
		bs.cfg.weaponBurst[nowWeaponLength] = 一度に撃つ弾の数;
		bs.cfg.weaponFireRate[nowWeaponLength] = 発射間隔;
		bs.cfg.weaponAberration[nowWeaponLength] = ブレの最終計算;
		bs.cfg.weaponDirection[nowWeaponLength] = "発射角度指定";
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 発射角度指定からずらす角度;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{発射地点1x座標,発射地点2x座標,...};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{発射地点1y座標,発射地点2y座標,...};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 弾速度;
		bs.cfg.weaponBulletSpeedDispersion[nowWeaponLength] = 弾速変動範囲;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 弾反射回数;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 相殺威力;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 弾の攻撃回数;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 弾加速度;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 弾加速度変動範囲;
		bs.cfg.weaponBulletAccelDirection[nowWeaponLength] = 弾加速度方向;
		bs.cfg.weaponBulletAccelDirectionCorrect[nowWeaponLength] = "弾加速度方向ずらし";
		bs.cfg.weaponLimitRange[nowWeaponLength] = 射程;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = "弾薬の種類";
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 弾倉容量;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 再装填時間;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 慣性力;
		bs.cfg.weaponSE[nowWeaponLength] = "発射音";
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 0;
		bs.cfg.weaponImg[nowWeaponLength] = "武器画像";
		*//////////////////////////////////
		//1
		bs.cfg.weaponName[nowWeaponLength] = "幻符「殺人ドール」";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("doll");
		bs.cfg.weaponStrength[nowWeaponLength] = 0;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.REVERSE_SELF;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{150,147,137,121,100,75,46,16,-16,-46,-75,-100,-121,-137,-147,-150,147,137,121,100,75,46,16,-16,-46,-75,-100,-121,-137,-147};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0,31,61,88,111,130,143,149,149,143,130,111,88,61,31,0,-31,-61,-88,-111,-130,-143,-149,-149,-143,-130,-111,-88,-61,-31};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 1;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 0;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 300;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		//2
		bs.cfg.weaponName[nowWeaponLength] = "doll2";
 		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("doll2");
		bs.cfg.weaponStrength[nowWeaponLength] = 25;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 0;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 7;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		//3
		bs.cfg.weaponName[nowWeaponLength] = "Waltz";
 		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto","Concerto2","Concerto3","Concerto4","Concerto5","Concerto6","Concerto7","Concerto8");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 600;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		//4
		bs.cfg.weaponName[nowWeaponLength] = "Rondo";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		//5
		bs.cfg.weaponName[nowWeaponLength] = "Rondo2";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto2");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		//6
		bs.cfg.weaponName[nowWeaponLength] = "Rondo3";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto3");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		//7
		bs.cfg.weaponName[nowWeaponLength] = "Rondo4";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto4");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//8
		bs.cfg.weaponName[nowWeaponLength] = "Rondo5";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto5");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		
		//9
		bs.cfg.weaponName[nowWeaponLength] = "Rondo6";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto6");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//10
		bs.cfg.weaponName[nowWeaponLength] = "Rondo7";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto7");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//11
		bs.cfg.weaponName[nowWeaponLength] = "Rondo8";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Concerto8");
		bs.cfg.weaponStrength[nowWeaponLength] = 500;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] =20;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 3;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 1000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 200;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//12
		bs.cfg.weaponName[nowWeaponLength] = "Magicsword";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("Starsword");
		bs.cfg.weaponStrength[nowWeaponLength] = 150;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 1;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{70};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 70;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 7;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 3;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 5000;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 10;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 30;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//13
		bs.cfg.weaponName[nowWeaponLength] = "速符「ルミネスリコシェ」";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("C");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 5;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{50};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 35;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 7;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 5;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 0;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;
		
		//14
		bs.cfg.weaponName[nowWeaponLength] = "ルミネス";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("C2");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 5;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = -35;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 7;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 5;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;

		//15
		bs.cfg.weaponName[nowWeaponLength] = "回転knife";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("回転knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 0;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 5;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{30};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 0;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 1;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 50;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;

		//16
		bs.cfg.weaponName[nowWeaponLength] = "弾幕knife";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾幕knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 5;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 10;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//17
		bs.cfg.weaponName[nowWeaponLength] = "弾幕knife2";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾幕knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 355;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 10;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";
		nowWeaponLength++;

		//18
		bs.cfg.weaponName[nowWeaponLength] = "弾幕knife3";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾幕knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 10;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 10;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";		
		nowWeaponLength++;

		//19
		bs.cfg.weaponName[nowWeaponLength] = "弾幕knife4";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾幕knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 350;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 10;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;

		//20
		bs.cfg.weaponName[nowWeaponLength] = "弾幕knife5";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾幕knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 15;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 10;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;

		//21
		bs.cfg.weaponName[nowWeaponLength] = "弾幕knife6";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("弾幕knife");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponDirection[nowWeaponLength] = bs.TARGET;
		bs.cfg.weaponDirectionCorrect[nowWeaponLength] = 345;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 30;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 10;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = BreakScope.MAX;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 30;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;

		//22
		bs.cfg.weaponName[nowWeaponLength] = "離剣の見";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("離剣");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{20,-30,50,-40,70,-60,90,-90};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{-50,-30,40,60,10,70,-20,55};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 10;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 5;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 200;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 7;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;

		//23
		bs.cfg.weaponName[nowWeaponLength] = "離剣の見2";
		bs.cfg.weaponBulletKind[nowWeaponLength] = toBulletIDs("離剣2");
		bs.cfg.weaponStrength[nowWeaponLength] = 300;
		bs.cfg.weaponCost[nowWeaponLength] = -1;
		bs.cfg.weaponBurst[nowWeaponLength] = 1;
		bs.cfg.weaponFireRate[nowWeaponLength] = 30;
		bs.cfg.weaponAberration[nowWeaponLength] = 0;
		bs.cfg.weaponFirePointsX[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponFirePointsY[nowWeaponLength] = new int[]{0};
		bs.cfg.weaponBulletSpeed[nowWeaponLength] = 0;
		bs.cfg.weaponBulletReflectiveness[nowWeaponLength] = 0;
		bs.cfg.weaponBulletOffSet[nowWeaponLength] = 5;
		bs.cfg.weaponBulletPenetration[nowWeaponLength] = 5;
		bs.cfg.weaponBulletAccel[nowWeaponLength] = 0;
		bs.cfg.weaponBulletAccelDispersion[nowWeaponLength] = 0;
		bs.cfg.weaponLimitRange[nowWeaponLength] = 500;
		bs.cfg.weaponAmmoKind[nowWeaponLength] = 9;
		bs.cfg.weaponMagazineSize[nowWeaponLength] = 7;
		bs.cfg.weaponReloadTime[nowWeaponLength] = 300;
		bs.cfg.weaponInertiaRate[nowWeaponLength] = 0;
		bs.cfg.weaponSEIsSerial.set(nowWeaponLength,false);
		bs.cfg.weaponUPG1Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG2Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG3Limit[nowWeaponLength] = 0;
		bs.cfg.weaponUPG4Limit[nowWeaponLength] = 0;
		bs.cfg.weaponActionType[nowWeaponLength] = 6;
		bs.cfg.weaponImg[nowWeaponLength] = "殺人ドール.png";	
		nowWeaponLength++;
		
		//角度変換処理
		for(int i = 0;i < nowWeaponLength;i++){
			bs.cfg.weaponDirection[i] = bs.toRadians2(bs.cfg.weaponDirection[i]);
			bs.cfg.weaponDirectionCorrect[i] = bs.toRadians2(bs.cfg.weaponDirectionCorrect[i]);
			bs.cfg.weaponBulletAccelDirectionCorrect[i] = bs.toRadians2(bs.cfg.weaponBulletAccelDirectionCorrect[i]);
		}
		
		System.out.println("Sakuya.java: 武器" + kosu + "個を追加しました。");	
	}
	public void addNewBullet(){
		//追加準備
		int nowBulletLength = bs.cfg.bulletKindTotal; //今登録されている弾の総数
		final int kosu = 17;
		
		//全配列の長さを増やす
		bs.cfg.expandsData_bullet(kosu);

		//雛形//////////////////
		/*
		bs.cfg.bulletName[nowBulletLength] = "新しい弾";
		bs.cfg.bulletSize[nowBulletLength] = 弾のサイズ;
		bs.cfg.bulletWithEffect[nowBulletLength] = toEffectIDs("エフェクトA","エフェクトB",...);
		bs.cfg.bulletDestroyEffect[nowBulletLength] = toEffectIDs("エフェクトA","エフェクトB",...);
		bs.cfg.bulletLostEffect[nowBulletLength] = toEffectIDs("エフェクトA","エフェクトB",...);
		bs.cfg.bulletLimitFrame[nowBulletLength] = 150;
		bs.cfg.bulletHitBurst[nowBulletLength] = 0;
		bs.cfg.bulletKnock[nowBulletLength] = 0;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("武器A","武器B",...);
		bs.cfg.bulletDestroyGun[nowBulletLength] = toWeaponIDs("武器A","武器B",...);
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("武器A","武器B",...);
		bs.cfg.bulletStallRatio[nowBulletLength] = 0;
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 0;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 0;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 0;
		bs.cfg.bulletLaserAction[nowBulletLength] = false;
		bs.cfg.bulletGunnerFollow[nowBulletLength] = false;
		bs.cfg.bulletHeatHoming[nowBulletLength] = false;
		bs.cfg.bulletSuperPenetration[nowBulletLength] = false;
		bs.cfg.bulletImg[nowBulletLength] = "";
		*/////////////////////////
		//1
		bs.cfg.bulletName[nowBulletLength] = "doll";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 150;
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 15;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 0;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 0;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//2
		bs.cfg.bulletName[nowBulletLength] = "doll2";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletStallRatio[nowBulletLength] = 1.03;
		bs.cfg.bulletSuperPenetration.set(nowBulletLength,true);
		bs.cfg.bulletImg[nowBulletLength] = "knife4.png";
		nowBulletLength++;
		//3
		bs.cfg.bulletName[nowBulletLength] = "Concerto";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 0;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//4
		bs.cfg.bulletName[nowBulletLength] = "Concerto2";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo2");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 90;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//5
		bs.cfg.bulletName[nowBulletLength] = "Concerto3";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo3");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 180;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//6
		bs.cfg.bulletName[nowBulletLength] = "Concerto4";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo4");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 270;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//7
		bs.cfg.bulletName[nowBulletLength] = "Concerto5";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo5");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 45;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//8
		bs.cfg.bulletName[nowBulletLength] = "Concerto6";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo6");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 135;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//9
		bs.cfg.bulletName[nowBulletLength] = "Concerto7";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo7");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 225;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//10
		bs.cfg.bulletName[nowBulletLength] = "Concerto8";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletWithGun[nowBulletLength] = toWeaponIDs("Magicsword");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("Rondo8");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 5;
		bs.cfg.bulletRotateStartAngle[nowBulletLength] = 315;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 100;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//11
		bs.cfg.bulletName[nowBulletLength] = "Starsword";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//12
		bs.cfg.bulletName[nowBulletLength] = "C";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = BreakScope.MAX;
		bs.cfg.bulletStallRatio[nowBulletLength] = 1.03;
		bs.cfg.bulletDestroyGun[nowBulletLength] = toWeaponIDs("ルミネス");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("ルミネス");
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//13
		bs.cfg.bulletName[nowBulletLength] = "C2";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = BreakScope.MAX;
		bs.cfg.bulletStallRatio[nowBulletLength] = 1.03;
		bs.cfg.bulletDestroyGun[nowBulletLength] = toWeaponIDs("速符「ルミネスリコシェ」");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("速符「ルミネスリコシェ」");
		bs.cfg.bulletImg[nowBulletLength] = "knife2.png";
		nowBulletLength++;
		//14
		bs.cfg.bulletName[nowBulletLength] = "回転knife";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 50;
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("弾幕knife","弾幕knife2","弾幕knife3","弾幕knife4","弾幕knife5","弾幕knife6");
		bs.cfg.bulletRotateSpeed[nowBulletLength] = 10;
		bs.cfg.bulletRotateRadius[nowBulletLength] = 0;
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//15
		bs.cfg.bulletName[nowBulletLength] = "弾幕knife";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 1000;
		bs.cfg.bulletSuperPenetration.set(nowBulletLength,true);
		bs.cfg.bulletImg[nowBulletLength] = "knife4.png";
		nowBulletLength++;
		//16
		bs.cfg.bulletName[nowBulletLength] = "離剣";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 100;
		bs.cfg.bulletDestroyGun[nowBulletLength] = toWeaponIDs("離剣の見2");
		bs.cfg.bulletLostGun[nowBulletLength] = toWeaponIDs("離剣の見2");		
		bs.cfg.bulletSuperPenetration.set(nowBulletLength,true);
		bs.cfg.bulletImg[nowBulletLength] = "knife.png";
		nowBulletLength++;
		//17
		bs.cfg.bulletName[nowBulletLength] = "離剣2";
		bs.cfg.bulletSize[nowBulletLength] = 7;
		bs.cfg.bulletLimitFrame[nowBulletLength] = 300;
		bs.cfg.bulletSuperPenetration.set(nowBulletLength,true);
		bs.cfg.bulletImg[nowBulletLength] = "knife4.png";
		nowBulletLength++;
		
		
		System.out.println("Sakuya.java: 弾" + kosu + "個を追加しました。");	
	}
	//IDを自動で入れる
	public int[] toWeaponIDs(String... data){
		int[] ids = new int[data.length];
		int miss = 0;
		for(int i = 0;i < ids.length;i++){
			if(bs.cfg.nameToID_weapon.containsKey(data[i]))
				ids[i - miss] = bs.cfg.nameToID_weapon.get(data[i]);
			else{
				System.out.println("by Sakuya.java：　コードエラー 武器名指定に間違いがあります。 (" + data[i] + ")");
				miss++;
			}
		}
		if(miss == 0)
			return ids;
		else
			return Arrays.copyOf(ids,ids.length - miss);
	}
	public int[] toBulletIDs(String... data){
		int[] ids = new int[data.length];
		int miss = 0;
		for(int i = 0;i < ids.length;i++){
			if(bs.cfg.nameToID_bullet.containsKey(data[i]))
				ids[i - miss] = bs.cfg.nameToID_bullet.get(data[i]);
			else{
				System.out.println("by Sakuya.java：　コードエラー 弾名指定に間違いがあります。 (" + data[i] + ")");
				miss++;
			}
		}
		if(miss == 0)
			return ids;
		else
			return Arrays.copyOf(ids,ids.length - miss);
	}
	public int[] toEffectIDs(String... data){
		int[] ids = new int[data.length];
		int miss = 0;
		for(int i = 0;i < ids.length;i++){
			if(bs.cfg.nameToID_effect.containsKey(data[i]))
				ids[i - miss] = bs.cfg.nameToID_effect.get(data[i]);
			else{
				System.out.println("by Sakuya.java：　コードエラー エフェクト名指定に間違いがあります。 (" + data[i] + ")");
				miss++;
			}
		}
		if(miss == 0)
			return ids;
		else
			return Arrays.copyOf(ids,ids.length - miss);
	}
	@Override
	public void gamePaint(int id,double x,double y,double angle,boolean aimed,int readyTime,int playerDistance,boolean inScreen){ //攻撃
		//移動処理
		if(readyTime <= 50){
			int moveSpeed = 6;
			if(bs.enemyWound[id] > 0)
				moveSpeed -= -1;
			bs.AI00(id,moveSpeed,bs.enemyFoundMe.get(id));
		}
		boolean shooting = false;
		//攻撃処理
		if(aimed){
			if(readyTime >= 190){
				bs.enemyShotFrame[id] = bs.nowFrame;
				if(bs.nowFrame % 3 == 0){
					bs.makeEnemyAttack(id,"幻符「殺人ドール」");
					shooting = true;
				}
			}else if(130 <= readyTime && readyTime < 180){
				bs.makeEnemyAttack(id,"回転knife");
				shooting = true;
			}else if(readyTime <= 20){
				if(bs.nowFrame % 3 == 0){
					bs.makeEnemyAttack(id,"離剣の見");
					shooting = true;
				}
			}else if(30 <= readyTime && readyTime < 90)	
					bs.makeEnemyAttack(id,"速符「ルミネスリコシェ」");
			else if(readyTime == 100 || readyTime == 110 || readyTime == 120){
				if(bs.nowFrame % 3 == 0){
					bs.makeEnemyAttack(id,"Waltz");
					shooting = true;
				}
			}
		}
		//特殊
		if(!bgmPlaying && bs.enemyFoundMe.get(id)){
			bgmPlaying = true;
			bs.battleBGM[bs.nowBattleBGM].stop();
			bgm.play();
		}
		if(!bs.enemyFoundMe.get(id) && bgmPlaying){
			bgmPlaying = false;
			bgm.stop();
			bs.battleBGM[bs.nowBattleBGM].play();
		}
		//描画処理
		final int x_int = (int)x,y_int = (int)y;
		bs.g2.rotate(angle,x_int,y_int);
		bs.drawImageBS_centerDot(shooting ? SakuyaImg1 : SakuyaImg2,x_int,y_int);//(条件 ? 合ってるとき描画　：　それ以外のとき描画)
		bs.g2.rotate(-angle,x_int,y_int);
	}
	@Override
	public void killed(int id){ //死亡追加処理
		bs.enemyDeathSE.stop();
		bs.enemyDeathSE.play(); //死亡SEを再生
	}
}