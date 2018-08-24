import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import static java.awt.event.KeyEvent.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import static java.lang.Math.*;

public class BreakScope_Editor extends BreakScope implements MouseListener,MouseMotionListener,MouseWheelListener,KeyListener,Runnable{

	//汎用変数群
	final int CREATE = -102,SAVE = -104,
		CREATE_NEW_STAGE = -115,STAGE_PROFILE = -116;
	
	//エディター追加画像
	final Image button_returnEdit_,button_save_,button_changeStage_;
	
	final int SPAWN_POINT = -2,ENEMY = 0,PATROL_POINT = 1,GIMMICK = 2,ENTITY = 3,ITEM = 4;
	int mode = ENEMY;
	final int mode_max = 5;
	final String modeName[] = {"enemy","patrol","gimmick","entity","item"};
		
	//巡回点配列群
	//グリッド座標位置にしか設置できないが、エディターではピクセル座標に変換して処理
	final int[] patrolPointX = new int[300],patrolPointY = new int[300],
		patrolTargetX = new int[300],patrolTargetY = new int[300],
		patrolDelay = new int[300];
	int patrolPoint_maxID = -1;
	
	//ギミック　エディター追加配列群
	final int CEMENT = 0,IRON = 1,CONCRETE = 2,BRICK = 3,MUD = 4,BELT = 5,GATE = 6,FENCE = 7,LEVER = 8,TNT = 9; //旧バージョン変換用定数
	
	int visionX,visionY,visionSpeed = 30;
	boolean saved = true;
	
	public static void main(String[] args){
		new BreakScope_Editor();
	}
	public BreakScope_Editor(){
		super(false); //エディター実行モード
		background[0] = this.loadImage("field2.PNG");
		button_returnEdit_ = this.loadImage("_back_.png");
		button_save_ = this.loadImage("_save_.png");
		button_changeStage_ = this.loadImage("_changeStage_.png");
		try{
			tracker.waitForAll();
		}catch(InterruptedException e){}
		
		new Thread(this).start();
	}
	public final void run(){
		//try{
			while(true){
				nowFrame++;
				if(nowFrame == 1)
					eventChange(STAGE_SELECT);
				try{
					Thread.sleep(30);
				}catch(InterruptedException e){}
				repaint();
			}
		//}catch(Exception e){
			//JOptionPane.showMessageDialog(null, "申し訳ありませんが、エラーが発生しました。\nエラーコード：" + e,"エラー",JOptionPane.ERROR_MESSAGE);		
		//}
	}
	private final Color turningColor = new Color(200,200,0,70);
	public final void paintComponent(Graphics g){
		if(g2 == null){
			g2 = offImage.createGraphics();
			return;
		}
		//キャンバスをクリア
		g2.setComposite(AlphaComposite.Clear);
		g2.fill(screenRect);
		g2.setComposite(AlphaComposite.SrcOver);
		
		if(nowEvent == CREATE){
			basicBehavior: {
				if(leftKey && visionX - visionSpeed > 700)
					visionX -= visionSpeed;
				if(rightKey && visionX + visionSpeed < stageW - 700)
					visionX += visionSpeed;
				if(upKey && visionY - visionSpeed > 700)
					visionY -= visionSpeed;
				if(downKey && visionY + visionSpeed < stageH - 700)
					visionY += visionSpeed;
				if(dragging && targetKind != NONE){ //ドラッグによるオブジェクト移動操作
					int x = visionX + focusX - defaultScreenW/2 + distanceW,y = visionY + focusY - defaultScreenH/2 + distanceH;
					switch(targetKind){
					case SPAWN_POINT:
						spawnPointX[targetID] = x;
						spawnPointY[targetID] = y;
						break;
					case ENEMY:
						enemyX[targetID] = x;
						enemyY[targetID] = y;
						break;
					case PATROL_POINT:
						patrolPointX[targetID] = x/100*100 + 50;
						patrolPointY[targetID] = y/100*100 + 50;
						break;
					case GIMMICK:
						final int dstID = x/100*stageGridH + y/100;
						if(gimmickKind[dstID] == NONE){ //移動先に物がないか確認
							cfg.gimmickClass[gimmickKind[targetID]].moved(targetID,dstID); //IDの変更=座標の変更
							gimmickKind[dstID] = gimmickKind[targetID];
							gimmickKind[targetID] = NONE;
							targetID = dstID;
						}
						break;
					case ENTITY:
						entityX[targetID] = x;
						entityY[targetID] = y;
						break;
					case ITEM:
						itemX[targetID] = x;
						itemY[targetID] = y;
					}
				}
			}
			final int visionX = this.visionX,visionY = this.visionY,tX = -visionX+defaultScreenW/2,tY = -visionY+defaultScreenH/2;
			g2.translate(tX,tY);
			g2.setComposite(AlphaComposite.SrcOver.derive(1.0f - stageLuminance));
			g2.setColor(Color.BLACK);
			g2.fillRect(-tX,-tY,defaultScreenW,defaultScreenH);
			g2.setComposite(AlphaComposite.SrcOver);
			//開始地点、リスタート地点
			g2.setFont(commentFont);
			g2.setStroke(stroke1);
			g2.setColor(Color.ORANGE);
			g2.drawOval(spawnPointX[0] - 5,spawnPointY[0] - 5,10,10);
			g2.drawOval(spawnPointX[0] - 7,spawnPointY[0] - 7,14,14);
			g2.drawString("spawn",spawnPointX[0] + 10,spawnPointY[0] + 10);
			
			//enemyAction
			nearestEnemyDistance = NONE;
			for(int i = 0;i <= enemy_maxID;i++){
				final int kind = enemyKind[i];
				if(kind == NONE)
					continue;
				final int x = (int)enemyX[i],y = (int)enemyY[i];
				if(super.outOfStage_pixel(x,y)){ //ステージ外削除
					super.deleteEnemyID(i);
					continue;
				}
				final double enemyDistance = sqrt(pow(visionX - x,2) + pow(visionY - y,2));
				if(nearestEnemyDistance > enemyDistance){
					nearestEnemyDistance = enemyDistance;
					nearestEnemyAngle = atan2(x - visionY,y - visionX);
				}
				//描画
				cfg.enemyClass[kind].editorPaint(i,x,y,enemyAngle[i],outOfScreen_img2(enemyImg[kind],x,y));
			}
			if(targetKind == ENEMY){ //敵を操作中
				if(enemyKind[targetID] == NONE){ //何らかの理由で削除されている
					targetKind = NONE;
					angleMode = false;
				}else{ //操作中であることを強調表示
					final Image img = enemyImg[enemyKind[targetID]];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					g2.drawImage(focusImg2,(int)enemyX[targetID] - imgW/2,(int)enemyY[targetID] - imgH/2,imgW,imgH,this);
					if(angleMode){ //向き調節モード
						g2.setColor(turningColor);
						g2.fillRect((int)enemyX[targetID] - imgW/2,(int)enemyY[targetID] - imgH/2,imgW,imgH);
					}
				}
			}
			
			//gimmickAction
			final int xLimit = defaultScreenW/100,yLimit = defaultScreenH/100; //縦横検索数
			for(int map = (visionX - defaultScreenW/2)/100*stageGridH + (visionY - defaultScreenH/2)/100,xCount = 0;xCount <= xLimit;map += stageGridH,xCount++){
				int yCount = 0;
				for(;yCount <= yLimit;map++,yCount++){
					try{
						final int kind = gimmickKind[map];
						if(kind != NONE)
							cfg.gimmickClass[kind].editorPaint(map,true); //描画
					}catch(ArrayIndexOutOfBoundsException e){}
				}
				map -= yCount;
			}
			if(targetKind == GIMMICK){ //ギミックを操作中
				final int x = targetID/stageGridH*100 + 50,y = targetID%stageGridH*100 + 50; //IDから座標情報を分解
				//操作中であることを強調表示
				g2.drawImage(focusImg2,x - 50,y - 50,100,100,this);
			}
			
			//entityAction
			for(int i = 0;i <= entity_maxID;i++){
				final int kind = entityKind[i];
				if(kind == NONE)
					continue;
				final int x = (int)entityX[i],y = (int)entityY[i];
				if(super.outOfStage_pixel(x,y)){ //ステージ外削除
					super.deleteEntityID(i);
					continue;
				}
				//描画
				cfg.entityClass[kind].editorPaint(i,x,y,entityAngle[i],outOfScreen_img2(entityImg[kind],x,y));
			}
			if(targetKind == ENTITY){ //エンティティを操作中
				if(entityKind[targetID] == NONE){ //何らかの理由で削除されている
					targetKind = NONE;
					angleMode = false;
				}else{ //操作中であることを強調表示
					final Image img = entityImg[entityKind[targetID]];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					g2.drawImage(focusImg2,(int)entityX[targetID] - imgW/2,(int)entityY[targetID] - imgH/2,imgW,imgH,this);
					if(angleMode){ //向き調節モード
						g2.setColor(turningColor);
						g2.fillRect((int)entityX[targetID] - imgW/2,(int)entityY[targetID] - imgH/2,imgW,imgH);
					}
				}
			}
			
			//itemAction
			for(int i = 0;i <= item_maxID;i++){
				final int kind = itemKind[i];
				if(kind == NONE)
					continue;
				final int x = itemX[i],y = itemY[i];
				if(super.outOfStage_pixel(x,y)){ //ステージ外削除
					super.deleteItemID(i);
					continue;
				}
				final Image img = itemImg[kind];
				g2.drawImage(img,x - img.getWidth(null)/2,y - img.getHeight(null)/2,this);
			}
			if(targetKind == ITEM){ //アイテムを操作中
				if(itemKind[targetID] == NONE) //何らかの理由で削除されている
					targetKind = NONE;
				else{ //操作中であることを強調表示
					final Image img = itemImg[itemKind[targetID]];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					g2.drawImage(focusImg2,(int)itemX[targetID] - imgW/2,(int)itemY[targetID] - imgH/2,imgW,imgH,this);
				}
			}
			//patrolAction
			g2.setStroke(stroke1);
			for(int i = 0;i <= patrolPoint_maxID;i++){
				final int x = patrolPointX[i],y = patrolPointY[i]; //巡回点座標
				final int targetX = patrolTargetX[i],targetY = patrolTargetY[i]; //巡回先座標
				g2.setColor(new Color(Color.HSBtoRGB(i* 0.1F,1F,0.5F))); //IDごとに色を変える
				g2.drawRect(x - 40,y - 40,80,80); //巡回点を四角で表示
				if(patrolTargetX[i] != NONE){ //巡回先を丸で表示
					final double angle = atan2(targetY - y,targetX - x); //巡回先の方向
					g2.drawLine(x,y,x + (int)(20*cos(angle)),y + (int)(20*sin(angle))); //巡回先の方向を線で表示
					g2.drawOval(targetX - 15,targetY - 15,30,30); //巡回先を丸で表示
				}
			}
			if(targetKind == PATROL_POINT){ //巡回点を操作中
				g2.setColor(Color.WHITE);
				g2.drawString("目標地点をクリック",patrolPointX[targetID] - 20,patrolPointY[targetID] + 60);
			}
			
			//レーダー
			//敵
			if(nearestEnemyDistance != NONE){
				final double angle = nearestEnemyAngle;
				g2.rotate(angle,visionX,visionY);
				g2.drawImage(radarImg,visionX - radarImg.getWidth(null)/2,visionY - radarImg.getHeight(null)/2,this);
				g2.rotate(-angle,visionX,visionY);
			}
			//スタート地点
			g2.setColor(Color.ORANGE);
			final double angle = atan2(spawnPointY[0] - visionY,spawnPointX[0] - visionX);
			g2.drawLine(visionX + (int)(10*cos(angle)),visionY + (int)(10*sin(angle)),visionX + (int)(25*cos(angle)),visionY + (int)(25*sin(angle)));
			//背景描写
			g2.setComposite(AlphaComposite.DstOver);
			final int wBGLimit = (stageGridW - 14)/6,hBGLimit = (stageGridH - 14)/6;
			for(int i = 0;i < wBGLimit;i++){
				if(abs(i*600 + 1000 - visionX) < defaultScreenW/2 + 300){
					for(int j = 0;j < hBGLimit;j++){
						if(abs(j*600 + 1000 - visionY) < defaultScreenH/2 + 300)
							g2.drawImage(background[0],i*600 + 700,j*600 + 700,600,600,this);
					}
				}
			}
			g2.setComposite(AlphaComposite.SrcOver);
			g2.translate(-tX,-tY);
			
			//ステータス表示（画面左上）
			g2.setColor(Color.WHITE);
			g2.drawString((visionX + focusX - defaultScreenW/2) + "," + (visionY + focusY - defaultScreenH/2),20,20);
			switch(mode){
			case ENEMY:
				if((double)enemy_total/(double)enemyKind.length >= 0.9)
					g2.setColor(Color.RED);
				g2.drawString(enemy_total + "/" + enemyKind.length,20,50);
				break;
			case PATROL_POINT:
				if((double)patrolPoint_maxID/(double)patrolPointX.length >= 0.9)
					g2.setColor(Color.RED);
				g2.drawString((patrolPoint_maxID+1) + "/" + patrolPointX.length,20,50);
				break;
			case GIMMICK:
				if((double)gimmick_total/(double)stageGridTotal >= 0.9)
					g2.setColor(Color.RED);
				g2.drawString(gimmick_total + "/" + stageGridTotal,20,50);
				break;
			case ENTITY:
				if((double)entity_total/(double)entityKind.length >= 0.9)
					g2.setColor(Color.RED);
				g2.drawString(entity_total + "/" + entityKind.length,20,50);
				break;
			case ITEM:
				if((double)item_total/(double)itemKind.length >= 0.9)
					g2.setColor(Color.RED);
				g2.drawString(item_total + "/" + itemKind.length,20,50);
			}
			if(saved){
				g2.setColor(Color.BLUE);
				g2.drawString("saved",100,20);
			}else{
				g2.setColor(Color.RED);
				g2.drawString("unsaved",100,20);
			}
			//明るさ調節バー（画面左端）
			g2.setColor(Color.GRAY);
			g2.fill3DRect(10,100,30,300,true);
			g2.fill3DRect(14,100 + (int)(stageLuminance*300F) - 4,22,8,true);
			//パレット（画面左下）
			g2.drawImage(statusTrayImg,0,defaultScreenH - statusTrayImg.getHeight(null),this);
			g2.setColor(new Color(Color.HSBtoRGB(mode*0.1F,1F,1F)));
			g2.setStroke(stroke5);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawOval(3,defaultScreenH - 86,82,82);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.drawString(modeName[mode],38 - modeName[mode].length()*2,defaultScreenH - 43);
			//詳細設定（画面右下）
			g2.setColor(Color.GRAY);
			g2.fillRect(defaultScreenW - 320,defaultScreenH - 120,320,120);
			if(targetKind != NONE){
				int[] values;
				switch(targetKind){
				case ENEMY:
					/*value1 = (int)(enemyAngle[targetID]/(2*PI) *200) + 100;
					g2.setColor(Color.WHITE);
					g2.drawString("ランダム",defaultScreenW - 50,defaultScreenH - 25);*/
					break;
				case PATROL_POINT:
					break;
				case GIMMICK:
					g2.setColor(Color.WHITE);
					final GimmickListener gimmick = cfg.gimmickClass[gimmickKind[targetID]];
					g2.drawString(gimmick.getDataName(0),700,525);
					g2.drawString(gimmick.getDataName(1),700,555);
					g2.drawString(gimmick.getDataName(2),700,585);
					g2.setColor(Color.WHITE);
					g2.fill3DRect(780,510,130,20,true);
					g2.fill3DRect(780,540,130,20,true);
					g2.fill3DRect(780,570,130,20,true);
					g2.setColor(Color.GRAY);
					for(int k = 0;k < gimmick.getDataLength();k++){
						if(k == configingRef)
							g2.drawString(configingText,785,525 + 30*k);
						else
							g2.drawString(gimmick.getData(targetID,k),785,525 + 30*k);
					}
				}
			}
			g2.setColor(Color.WHITE);
			switch(mode){
			case ENEMY:
				for(int i = 0;i < 10 && i < cfg.enemyKindTotal;i++){
					final Image img = enemyIconImg[(i + page) % cfg.enemyKindTotal];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					if(imgW > imgH){
						final int imgH2 = (int)(imgH*(50D/imgW));
						g2.drawImage(img,100 + i*50,defaultScreenH - 50 + (50 - imgH2)/2,50,imgH2,this);
					}else{
						final int imgW2 = (int)(imgW*(50D/imgH));
						g2.drawImage(img,100 + i*50 + (50 - imgW2)/2,defaultScreenH - 50,imgW2,50,this);
					}
					g2.drawString(String.valueOf(i+1),135 + i*50,defaultScreenH - 5);
				}
				break;
			case PATROL_POINT:
				g2.setFont(basicFont);
				Color c = g2.getColor();
				g2.setColor(Color.BLACK);
				g2.drawString("set",110,defaultScreenH - 15);
				g2.setColor(c);
				g2.setFont(commentFont);
				g2.drawString("1",135,defaultScreenH - 5);
				break;
			case GIMMICK:
				for(int i = 0;i < 10 && i < cfg.gimmickKindTotal;i++){
					final Image img = gimmickIconImg[(i + page) % cfg.gimmickKindTotal];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					if(imgW > imgH){
						final int imgH2 = (int)(imgH*(50D/imgW));
						g2.drawImage(img,100 + i*50,defaultScreenH - 50 + (50 - imgH2)/2,50,imgH2,this);
					}else{
						final int imgW2 = (int)(imgW*(50D/imgH));
						g2.drawImage(img,100 + i*50 + (50 - imgW2)/2,defaultScreenH - 50,imgW2,50,this);
					}
					g2.drawString(String.valueOf(i+1),135 + i*50,defaultScreenH - 5);
				}
				break;
			case ENTITY:
				for(int i = 0;i < 10 && i < cfg.entityKindTotal;i++){
					final Image img = entityIconImg[(i + page) % cfg.entityKindTotal];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					if(imgW > imgH){
						final int imgH2 = (int)(imgH*(50D/imgW));
						g2.drawImage(img,100 + i*50,defaultScreenH - 50 + (50 - imgH2)/2,50,imgH2,this);
					}else{
						final int imgW2 = (int)(imgW*(50D/imgH));
						g2.drawImage(img,100 + i*50 + (50 - imgW2)/2,defaultScreenH - 50,imgW2,50,this);
					}
					g2.drawString(String.valueOf(i+1),135 + i*50,defaultScreenH - 5);
				}
				break;
			case ITEM:
				for(int i = 0;i <= 8;i++){
					final Image img = itemImg[i];
					final int imgW = img.getWidth(null),imgH = img.getHeight(null);
					if(imgW > imgH){
						final int imgH2 = (int)(imgH*(50D/imgW));
						g2.drawImage(img,100 + i*50,defaultScreenH - 50 + (50 - imgH2)/2,50,imgH2,this);
					}else{
						final int imgW2 = (int)(imgW*(50D/imgH));
						g2.drawImage(img,100 + i*50 + (50 - imgW2)/2,defaultScreenH - 50,imgW2,50,this);
					}
					g2.drawString(String.valueOf(i+1),135 + i*50,defaultScreenH - 5);
				}
			}
			g2.drawImage(focusImg,focusX - focusImg.getWidth(null)/2,focusY - focusImg.getHeight(null)/2,this);
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
				g2.drawString("EM:" + enemy_total + " ET:" + entity_total + " G:" + gimmick_total + " P:" + (patrolPoint_maxID + 1),30,100);
				g2.setColor(reloadGaugeColor);
				g2.drawString("(" + (-tX + focusX) + "," + (-tY + focusY) + ")",focusX + 20,focusY + 40);
			}
		}else if(nowEvent == PAUSE_MENU){
			g2.drawImage(button_returnEdit_,300,150,400,100,this);
			g2.drawImage(button_save_,300,300,400,100,this);
			g2.drawImage(button_changeStage_,300,450,400,100,this);
			g2.setColor(Color.WHITE);
			g2.setStroke(stroke10);
			switch(focusing){
			case CREATE:
				g2.drawRect(300,150,400,100);
				break;
			case SAVE:
				g2.drawRect(300,300,400,100);
				break;
			case STAGE_SELECT:
				g2.drawRect(300,450,400,100);
			}
		}else if(nowEvent == STAGE_SELECT || nowEvent == CREATE_NEW_STAGE || nowEvent == STAGE_PROFILE){
			g2.setComposite(AlphaComposite.SrcOver);
			g2.setStroke(stroke10);
			g2.setFont(commentFont);
			g2.setColor(Color.WHITE);
			for(int i = 0;i < stageName.length;i++){ //ステージボタン描画
				g2.drawImage(basicButton,100,stagePage + 60*i,400,50,this);
				g2.drawString(stageName[i],110,50 - 20 + stagePage + 60*i);
			}
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
			if(nowEvent == STAGE_SELECT){
				//右側ステージ詳細欄
				g2.setColor(new Color(145,215,55,40));
				g2.fillRect(580,50,390,500);
				g2.setColor(reloadGaugeColor);
				g2.drawString("stage " + stageName[stage],585,65);
				g2.setColor(Color.BLACK);
				g2.fillRect(600,500,150,40);
				g2.fillRect(800,500,150,40);
				g2.setColor(Color.WHITE);
				g2.setFont(commentFont.deriveFont(30F));
				final String str1 = "新規作成";
				g2.drawString(str1,675 - g2.getFontMetrics().stringWidth(str1)/2,530);
				final String str2 = "詳細設定";
				g2.drawString(str2,875 - g2.getFontMetrics().stringWidth(str2)/2,530);
				if(focusing == CREATE){ //ステージボタンを選択
					//ボタン強調表示
					g2.drawRect(100,stagePage + 60*stage,400,50);
				}
			}else if(nowEvent == CREATE_NEW_STAGE){
				g2.setColor(Color.LIGHT_GRAY);
				g2.fillRect(defaultScreenW/2 - 150,defaultScreenH/2 - 40,300,80);
				g2.setColor(Color.WHITE);
				final String str = "ステージ名を入力してください";
				g2.drawString(str,(defaultScreenW - g2.getFontMetrics().stringWidth(str))/2,defaultScreenH/2 - 25);
				g2.drawString(configingText,(defaultScreenW - g2.getFontMetrics().stringWidth(configingText))/2,defaultScreenH/2 + 10);
				g2.drawImage(button_return_,0,550,150,50,this);
				if(focusing == oldEvents.getFirst())
					g2.drawRect(0,550,150,50);
			}else if(nowEvent == STAGE_PROFILE){
				g2.drawImage(button_return_,0,550,150,50,this);
				if(focusing == oldEvents.getFirst())
					g2.drawRect(0,550,150,50);
			}
		}
		g.setColor(Color.BLACK);
		g.fillRect(0,0,defaultScreenW,defaultScreenH);
		g.drawImage(offImage,0,0,defaultScreenW,defaultScreenH,this);
	}
	boolean textInputting; //テキスト入力中か
	int configingRef = NONE;
	int targetKind = NONE; //選択オブジェクト系統種類
	int targetID; //選択オブジェクトID
	final int propertyDefaultValues[] = new int[3]; //特殊値１・２・３の既定値
	int distanceW,distanceH; //マウスのクリック地点とオブジェクトの中心からの距離
	boolean angleMode,strictMode;
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
		int x = visionX + focusX - defaultScreenW/2,y = visionY + focusY - defaultScreenH/2;
		int targetRef = NONE; //<詳細設定>変更先の項目
		if(nowEvent == 0)
			return;
		if(nowEvent == CREATE){
			if(defaultScreenW - 320 < focusX && defaultScreenH - 120 < focusY){ //画面右下の詳細設定枠
				switch(targetKind){
				case PATROL_POINT:
					//patrolDelay[targetID] = delayDefaultValue = value1;
					break;
				case GIMMICK:
					if(750 < focusX && focusX < 880){
						if(510 < focusY && focusY < 530)
							targetRef = 0;
						else if(540 < focusY && focusY < 560)
							targetRef = 1;
						else if(570 < focusY && focusY < 590)
							targetRef = 2;
						final GimmickListener gimmick = cfg.gimmickClass[gimmickKind[targetID]];
						if(targetRef != NONE && targetRef >= gimmick.getDataLength()) //欄外判定
							targetRef = NONE;
						if(configingRef != NONE && targetRef != configingRef){ //項目設定中で、同じ項目以外のところをクリックしたとき、内容を代入
							try{
								gimmick.setData(targetID,configingRef,configingText);
							}catch(IllegalArgumentException ex){}
						}
						if(targetRef != NONE){ //参照先を変更、テキスト入力状態を開く
							configingRef = targetRef;
							textInputting = true;
							configingText = "";
						}else{ //テキスト入力状態を閉じる
							configingRef = NONE;
							textInputting = false;
						}
					}
				}
				return;
			}
			if(configingRef != NONE){ //詳細設定枠外をクリックしても内容を代入
				try{
					cfg.gimmickClass[gimmickKind[targetID]].setData(targetID,configingRef,configingText);
				}catch(IllegalArgumentException ex){}
			}
			configingRef = NONE;
			textInputting = false;
			if(pow(41 - focusX,2) + pow(defaultScreenH - 45 - focusY,2) < pow(41,2)){ //画面左下のモードチェンジボタン
				mode++;
				if(mode >= mode_max)
					mode = 0;
			}else if(10 < focusX && focusX < 40 && 100 < focusY && focusY < 400){ //画面左端の明るさ調整バー
				stageLuminance = (focusY - 100)/300F;
				saved = false;
			}else if(targetKind == PATROL_POINT){ //巡回先の設定中
				patrolTargetX[targetID] = (x/100)*100 + 50;
				patrolTargetY[targetID] = (y/100)*100 + 50;
				targetKind = NONE;
			}else{
				if(sqrt(pow(spawnPointX[0] - x,2) + pow(spawnPointY[0] - y,2)) < 15){ //開始地点の選択
					distanceW = spawnPointX[0] - x;distanceH = spawnPointY[0] - y;
					targetID = 0;
					targetKind = SPAWN_POINT;
				}else if(!angleMode){ //オブジェクトの選択
					final int mapX = x/100,mapY = y/100,map = mapX*stageGridH + mapY;
					if(gimmickKind[map] != NONE){ //ギミックの選択
						distanceW = mapX*100 + 50 - x;distanceH = mapY*100 + 50 - y;
						targetKind = GIMMICK;
						targetID = map;
						return;
					}
					for(int i = enemy_maxID;i >= 0;i--){ //敵の選択(検索順序は描画と逆)
						final int kind = enemyKind[i];
						if(kind == NONE)
							continue;
						final int size_2 = cfg.enemySelectSize[kind];
						if(abs(x - (int)enemyX[i]) < size_2 && abs(y - (int)enemyY[i]) < size_2){
							distanceW = (int)enemyX[i] - x;distanceH = (int)enemyY[i] - y;
							targetKind = ENEMY;
							targetID = i;
							return;
						}
					}
					for(int i = 0;i <= patrolPoint_maxID;i++){ //巡回点の選択
						if(abs(x - patrolPointX[i]) < 40 && abs(y - patrolPointY[i]) < 40){
							distanceW = patrolPointX[i] - x;distanceH = patrolPointY[i] - y;
							targetKind = PATROL_POINT;
							targetID = i;
							return;
						}
					}
					for(int i = entity_maxID;i >= 0;i--){ //エンティティの選択(検索順序は描画と逆)
						final int kind = entityKind[i];
						if(kind == NONE)
							continue;
						final int size_2 = cfg.entitySelectSize[kind];
						if(abs(x - (int)entityX[i]) < size_2 && abs(y - (int)entityY[i]) < size_2){
							distanceW = (int)entityX[i] - x;distanceH = (int)entityY[i] - y;
							targetKind = ENTITY;
							targetID = i;
							return;
						}
					}
					for(int i = 0;i <= item_maxID;i++){ //アイテムの選択
						final int kind = itemKind[i];
						if(kind != NONE && abs(x - itemX[i]) < itemImg[kind].getWidth(null)/2 && abs(y - itemY[i]) < itemImg[kind].getHeight(null)/2){
							distanceW = itemX[i] - x;distanceH = itemY[i] - y;
							targetKind = ITEM;
							targetID = i;
							return;
						}
					}
					targetKind = NONE; //何もないところを押された、選択解除
				}
			}
		}else if(nowEvent == STAGE_SELECT){ //ステージ選択画面
			if(focusing == CREATE){
				loadData(true);
				focusing = NONE;
				eventChange(CREATE);
			}else if(focusing == CREATE_NEW_STAGE){
				eventChange(CREATE_NEW_STAGE);
				textInputting = true;
				configingText = "";
			}else if(focusing == STAGE_PROFILE)
				eventChange(STAGE_PROFILE);
		}else if(nowEvent == CREATE_NEW_STAGE || nowEvent == STAGE_PROFILE){
			if(focusing == STAGE_SELECT){
				eventChange(STAGE_SELECT);
				textInputting = false;
			}
		}else if(nowEvent == PAUSE_MENU){ //ポーズメニュー
			if(focusing == SAVE) //セーブ
				saveData();
			else if(focusing != NONE) //何かのボタンを押した
				eventChange(focusing); //リンク先を代入
		}
	}
	public void mouseReleased(MouseEvent e){
		dragging = false;
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseMoved(MouseEvent e){
		focusX = e.getX();focusY = e.getY();
		focusing = NONE;
		if(nowEvent == STAGE_SELECT){
			if(focusX < 550){
				if(focusY < defaultScreenH/2 - 175) //画面上部スクロール範囲にマウスがある
					pagePower = min(pagePower + abs(defaultScreenH/2 + 175 - focusY)/25.0,20.0);
				else if(defaultScreenH/2 + 175 < focusY) //画面下部にマウスがある
					pagePower = max(pagePower - abs(defaultScreenH/2 - 175 - focusY)/25.0,-20.0);
				for(int i = 0;i < stageName.length;i++){
					if(abs(60*i + 30 + stagePage - focusY) < 25){
						focusing = CREATE;
						stage = i;
						break;
					}
				}
			}
			if(focusing == NONE){
				if(500 < focusY && focusY < 540){
					if(600 < focusX && focusX < 750)
						focusing = CREATE_NEW_STAGE;
					else if(800 < focusX && focusX < 950)
						focusing = STAGE_PROFILE;
				}
			}
		}else if(nowEvent == CREATE_NEW_STAGE || nowEvent == STAGE_PROFILE){
			if(focusX < 150 && focusY > 550)
				focusing = oldEvents.getFirst();
		}else if(nowEvent == PAUSE_MENU){
 			if(300 < focusX && focusX < 700){
 				if(150 < focusY && focusY < 250)
 					focusing = CREATE;
 				else if(300 < focusY && focusY < 400)
 					focusing = SAVE;
 				else if(450 < focusY && focusY < 550)
 					focusing = STAGE_SELECT;
 			}
		}
	}
	public void mouseDragged(MouseEvent e){
		focusX = e.getX();focusY = e.getY();
		int x = visionX + focusX - defaultScreenW/2 + distanceW,y = visionY + focusY - defaultScreenH/2 + distanceH;
		if(targetKind != NONE){
			saved = false;
			if(angleMode){ //回転操作
				double angle;
				switch(targetKind){
				case ENEMY:
					angle = toDegrees(atan2(y - enemyY[targetID],x - enemyX[targetID]));
					if(strictMode) //角度を45度刻みで変更
						enemyAngle[targetID] = toRadians(round(angle/45D)*45);
					else //角度をなめらかに変更
						enemyAngle[targetID] = toRadians(angle);
					break;
				case ENTITY:
					angle = toDegrees(atan2(y - entityY[targetID],x - entityX[targetID]));
					if(strictMode) //角度を45度刻みで変更
						entityAngle[targetID] = toRadians(round(angle/45D)*45);
					else //角度をなめらかに変更
						entityAngle[targetID] = toRadians(angle);
					break;
				}
			}else if(10 < focusX && focusX < 40 && 100 < focusY && focusY < 400) //画面左端の明るさ調整バー
				stageLuminance = (focusY - 100)/300F;
			else if(pow(41 - focusX,2) + pow(defaultScreenH - 45 - focusY,2) > pow(41,2) &&
				focusX < defaultScreenW - 320 && focusY < defaultScreenH - 120) //ドラッグ操作(左下のモードチェンジボタンと右下の詳細設定枠エリアを回避)
				dragging = true;
		}
	}
	public void mouseWheelMoved(MouseWheelEvent e){
		mode += e.getWheelRotation(); //モードチェンジホイール操作
		if(mode >= mode_max)
			mode = mode_max-1;
		else if(mode < 0)
			mode = 0;
	}
	boolean debugMode;
	public void keyPressed(KeyEvent e){
		if(configingRef != NONE)
			return;
		if(nowEvent == CREATE){ //ステージ編集画面
			switch(e.getKeyCode()){
			case VK_UP:
			case VK_W:
				if(dragging || angleMode)
					upKey = true;
				else
					moveObject(targetKind,0,-1);
				break;
			case VK_LEFT:
			case VK_A:
				if(dragging || angleMode)
					leftKey = true;
				else
					moveObject(targetKind,-1,0);
				break;
			case VK_S: //セーブショットカットShift+S
				if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0){
					saveData();
					break;
				}
			case VK_DOWN:
				if(dragging || angleMode)
					downKey = true;
				else
					moveObject(targetKind,0,1);
				break;
			case VK_RIGHT:
			case VK_D:
				if(dragging || angleMode)
					rightKey = true;
				else
					moveObject(targetKind,1,0);
				break;
			case VK_E: //設置物切替
				if(mode == GIMMICK){
					page++;
					page %= cfg.gimmickKindTotal;
				}else if(mode == ENEMY){
					page++;
					page %= cfg.enemyKindTotal;
				}
				break;
			case VK_TAB: //設置物グループ切替
				mode++;
				if(mode >= mode_max)
					mode = 0;
				break;
			case VK_R: //回転モード
				if(angleMode)
					angleMode = false;
				else{
					switch(targetKind){
					case ENEMY:
					case ENTITY:
						angleMode = true;
						break;
					}
				}
				break;
			case VK_SHIFT: //厳密モード
				strictMode = !strictMode;
				break;
			case VK_ESCAPE:
			case VK_P: //メニュー
				if(nowEvent == CREATE){
					upKey = leftKey = downKey = rightKey = false;
					eventChange(PAUSE_MENU);
				}
				break;
			case VK_BACK_SPACE:
			case VK_DELETE: //削除
				if(targetKind != NONE){
					switch(targetKind){
					case ENEMY: //敵の削除
						super.deleteEnemyID(targetID);
						angleMode = false;
						break;
					case ENTITY: //エンティティの削除
						super.deleteEntityID(targetID);
						angleMode = false;
						break;
					case PATROL_POINT: //巡回点の削除
						if(targetID != patrolPoint_maxID){ //※巡回点のみ特殊な配列整理法を使用
							patrolPointX[targetID] = patrolPointX[patrolPoint_maxID];
							patrolPointY[targetID] = patrolPointY[patrolPoint_maxID];
							patrolTargetX[targetID] = patrolTargetX[patrolPoint_maxID];
							patrolTargetY[targetID] = patrolTargetY[patrolPoint_maxID];
						}
						patrolPoint_maxID--;
						break;
					case GIMMICK: //ギミックの削除
						super.deleteGimmickID(targetID);
						break;
					case ITEM: //アイテムの削除
						super.deleteItemID(targetID);
					}
					targetKind = NONE; //選択解除
					saved = false;
				}
				break;
			case VK_F3: //デバックモード
				if(nowEvent == CREATE)
					debugMode = !debugMode;
				break;
			case VK_F4:
				stageSizeChange_PlusDirection(1,1);
				break;
			case VK_F5:
				stageSizeChange_PlusDirection(-1,-1);
				break;
			case VK_F6:
				stageSizeChange_MinusDirection(1,1);
				break;
			case VK_F7:
				stageSizeChange_MinusDirection(-1,-1);
				break;
			/*case VK_Z:
				if(e.stateMask == SWT.CTRL)
					enemyKind[--enemy_maxID] = NONE;*/
			}
		}else if(nowEvent == STAGE_SELECT){
			switch(e.getKeyCode()){
			case VK_F12: //全保存
				System.out.println("update start");
				final int stage_tmp = stage;
				int stage = 0;
				for(;stage < stageName.length;stage++){
					loadData(true);
					saveData();
				}
				System.out.println("update complete");
				stage = stage_tmp;
				break;
			}
		}
	}
	void moveObject(int type,int xd,int yd){ //オブジェクトを移動
		switch(type){
		case ENEMY:
			if(strictMode){ //座標を100刻みで変更
				enemyX[targetID] += xd*100;
				enemyY[targetID] += yd*100;
			 }else{ //座標をなめらかに変更
				enemyX[targetID] += xd;
				enemyY[targetID] += yd;
			}
			saved = false;
			break;
		case GIMMICK:
			final int dstID = targetID + xd*stageGridH + yd; //移動先座標(ID)
			if(gimmickKind[dstID] == NONE){ //移動先に物がないか確認
				final int kind = gimmickKind[targetID];
				cfg.gimmickClass[kind].moved(targetID,dstID); //ID変更追加処理
				gimmickKind[dstID] = gimmickKind[targetID];
				gimmickKind[targetID] = NONE;
				targetID = dstID;
			}
			saved = false;
			break;
		case ENTITY:
			if(strictMode){ //座標を100刻みで変更
				entityX[targetID] += xd*100;
				entityY[targetID] += yd*100;
			 }else{ //座標をなめらかに変更
				entityX[targetID] += xd;
				entityY[targetID] += yd;
			}
			saved = false;
			break;
		case ITEM:
			if(strictMode){ //座標を100刻みで変更
				itemX[targetID] += xd*100;
				itemY[targetID] += yd*100;
			 }else{ //座標をなめらかに変更
				itemX[targetID] += xd;
				itemY[targetID] += yd;
			}
			saved = false;
			break;
		default:
			if(yd == -1)
				upKey = true;
			else if(xd == -1)
				leftKey = true;
			else if(yd == +1)
				downKey = true;
			else if(xd == +1)
				rightKey = true;
		}
	}
	public void keyReleased(KeyEvent e){
		if(nowEvent != CREATE || configingRef != NONE)
			return;
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
		}
	}
	String configingText = "";
	public void keyTyped(KeyEvent e){
		if(nowEvent != CREATE && nowEvent != CREATE_NEW_STAGE)
			return;
		if(textInputting){ //テキスト入力中
			final String s = String.valueOf(e.getKeyChar());
			final int length = configingText.length();
			if(s.equals("\b")){ //バックスペース
				if(length > 0)
					configingText = configingText.substring(0,length - 1);
			}else if(s.equals("\n")){ //決定
				if(configingRef != NONE){ //コンフィグ
					final GimmickListener gimmick = cfg.gimmickClass[gimmickKind[targetID]];
					if(gimmick.getData(targetID,configingRef) != configingText){
						try{
							gimmick.setData(targetID,configingRef,configingText);
							saved = false;
						}catch(IllegalArgumentException ex){
							System.out.println(ex.getCause());
						}
					}
					configingRef = NONE;
				}else if(nowEvent == CREATE_NEW_STAGE){ //ステージ新規作成
					try{
						new File("stage/" + configingText + ".ini").createNewFile(); //ステージファイルを新規作成
					}catch(IOException ex){}
					//まだ編集画面に入らないので、この時点ではステージファイルは空白のまま
					eventChange(STAGE_SELECT);
				}
			}else if(length < 64)
				configingText += s;
			return;
		}
		int keyCode;
		try{
			keyCode = Integer.parseInt(String.valueOf(e.getKeyChar())); //数字キーであることを確認
		}catch(NumberFormatException ex){
			return;
		}
		if(keyCode == 0)
			keyCode = 10;
		//生成処理
		int x = visionX + focusX - defaultScreenW/2,y = visionY + focusY - defaultScreenH/2;
		int kind;
		switch(mode){
		case ENEMY:
			kind = (keyCode - 1 + page) % cfg.enemyKindTotal;
			if(super.setEnemy(kind,x/100*100 + 50,y/100*100 + 50,0.0)) //敵生成
				saved = false; //生成成功,未セーブ状態にする
			else //エラー
				System.out.println("could not set enemy kind = " + kind);
			break;
		case GIMMICK:
			kind = (keyCode - 1 + page) % cfg.gimmickKindTotal;
			if(super.setGimmick(kind,x,y,true)) //ギミック生成(マップ座標自動変換のため座標調整処理なし)(上書きモード)
				saved = false; //生成成功,未セーブ状態にする
			else //エラー
				System.out.println("could not set gimmick kind = " + kind);
			break;
		case ENTITY:
			kind = keyCode - 1;
			if(super.setEntity(kind,x/100*100 + 50,y/100*100 + 50,0.0)) //エンティティ生成
				saved = false; //生成成功,未セーブ状態にする
			else //エラー
				System.out.println("could not set entity kind = " + kind);
			break;
		case PATROL_POINT:
			if(keyCode == 1){
				final int id = ++patrolPoint_maxID;
				patrolPointX[id] = x/100*100 + 50;
				patrolPointY[id] = y/100*100 + 50;
				patrolTargetX[id] = patrolTargetY[id] = NONE;
				targetKind = PATROL_POINT;
				targetID = id;
				saved = false;
			}
			break;
		case ITEM:
			kind = keyCode - 1;
			if(0 <= kind && kind <= 8){
				if(super.setItem(kind,itemPakageSizeData[kind],x/100*100 + 50,y/100*100 + 50)) //アイテム生成
					saved = false; //生成成功,未セーブ状態にする
				else //エラー
					System.out.println("could not set item kind = " + kind);
			}
		}
	}
	public void saveData(){
		final StringBuilder data = new StringBuilder();
		int i = 0;
		//ヘッダー部分の書き込み
		data.append("BreakScope StageData ver2.0")
			.append("\r\ncreateDate=").append(new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()))
			.append("\r\nversion=").append(super.NOW_VERSION)
			.append("\r\n");
		//ステージ大きさの書き込み
		data.append("\r\nstageGridW=").append(stageGridW);
		data.append("\r\nstageGridH=").append(stageGridH);
		//開始地点の書き込み
		data.append("\r\nspawnPoint=");
		while(true){
			data.append(spawnPointX[i]*stageH + spawnPointY[i]); //座標-圧縮情報
			if(i + 1 < spawnPointX.length){
				data.append(",");
				i++;
			}else
				break;
		}
		//敵情報の書き込み
		final StringBuilder sb1 = new StringBuilder(), //種類
			sb2 = new StringBuilder(), //座標
			sb3 = new StringBuilder(), //向き
			sb4 = new StringBuilder(); //その他の固有情報
		i = 0;
		if(enemy_total > 0){
			final DecimalFormat DF0_0 = new DecimalFormat("0.0");
			while(true){
				final int kind = enemyKind[i];
				if(kind == NONE){
					i++;
					continue;
				}
				sb1.append(cfg.enemyName[kind]); //種類
				sb2.append((int)enemyX[i]*stageH + (int)enemyY[i]); //座標-圧縮情報
				sb3.append(DF0_0.format(enemyAngle[i])); //向き-小数点第３位切り捨て
				if(cfg.enemyClass[kind] != null) //追加コードあり
					sb4.append(cfg.enemyClass[kind].getData(i)); //その他の固有情報
				if(++i <= enemy_maxID){ //続きあり
					sb1.append(",");
					sb2.append(",");
					sb3.append(",");
					sb4.append(",");
				}else //書き込み終了
					break;
			}
		}
		data.append("\r\nenemyKind=").append(sb1.toString())
			.append("\r\nenemyLocation=").append(sb2.toString())
			.append("\r\nenemyAngle=").append(sb3.toString())
			.append("\r\nenemyProperty=").append(sb4.toString());
		//エンティティ情報の書き込み
		sb1.delete(0,sb1.length()); //種類
		sb2.delete(0,sb2.length()); //座標
		sb3.delete(0,sb3.length()); //向き
		sb4.delete(0,sb4.length()); //その他の固有情報
		i = 0;
		if(entity_total > 0){
			final DecimalFormat DF0_0 = new DecimalFormat("0.0");
			while(true){
				final int kind = entityKind[i];
				if(kind == NONE){
					i++;
					continue;
				}
				sb1.append(cfg.entityName[kind]); //種類
				sb2.append((int)entityX[i]*stageH + (int)entityY[i]); //座標-圧縮情報
				sb3.append(DF0_0.format(entityAngle[i])); //向き-小数点第３位切り捨て
				if(cfg.entityClass[kind] != null) //追加コードあり
					sb4.append(cfg.entityClass[kind].getData(i)); //その他の固有情報
				if(++i <= entity_maxID){ //続きあり
					sb1.append(",");
					sb2.append(",");
					sb3.append(",");
					sb4.append(",");
				}else //書き込み終了
					break;
			}
		}
		data.append("\r\nentityKind=").append(sb1.toString())
			.append("\r\nentityLocation=").append(sb2.toString())
			.append("\r\nentityAngle=").append(sb3.toString())
			.append("\r\nentityProperty=").append(sb4.toString());
		//巡回点情報の書き込み
		sb1.delete(0,sb1.length()); //座標
		sb2.delete(0,sb2.length()); //移動先
		sb3.delete(0,sb3.length()); //駐屯時間
		i = 0;
		if(patrolPoint_maxID >= 0){
			while(true){
				sb1.append(patrolPointX[i]/100*stageGridH + patrolPointY[i]/100); //座標-圧縮情報
				sb2.append(patrolTargetX[i]/100*stageGridH + patrolTargetY[i]/100); //移動先-圧縮情報
				sb3.append(patrolDelay[i]); //駐屯時間
				if(++i <= patrolPoint_maxID){ //続きあり
					sb1.append(",");
					sb2.append(",");
					sb3.append(",");
				}else //書き込み終了
					break;
			}
		}
		data.append("\r\npatrolPoint=").append(sb1.toString())
			.append("\r\npatrolTarget=").append(sb2.toString())
			.append("\r\npatrolDelay=").append(sb3.toString());
		//ギミック情報の書き込み
		sb1.delete(0,sb1.length()); //種類
		sb2.delete(0,sb2.length()); //座標
		sb3.delete(0,sb3.length()); //その他の固有情報
		i = 0;
		int gimmickCount = 0; //書き込み数カウント
		for(;i < stageGridTotal;i++){
			if(gimmickKind[i] == NONE)
				continue;
			final int kind = gimmickKind[i];
			sb1.append(cfg.gimmickName[kind]); //種類
			sb2.append(i); //ID=座標-圧縮情報
			if(cfg.gimmickClass[kind] != null) //追加コードあり
				sb3.append(cfg.gimmickClass[kind].getData(i)); //その他の固有情報
			if(++gimmickCount < gimmick_total){ //続きあり
				sb1.append(",");
				sb2.append(",");
				sb3.append(",");
			}else //書き込み終了
				break;
		}
		data.append("\r\ngimmickKind=").append(sb1.toString())
			.append("\r\ngimmickLocation=").append(sb2.toString())
			.append("\r\ngimmickProperty=").append(sb3.toString());
		//アイテム情報の書き込み
		sb1.delete(0,sb1.length()); //種類
		sb2.delete(0,sb2.length()); //座標
		sb3.delete(0,sb3.length()); //個数
		i = 0;
		if(item_total > 0){
			while(true){
				sb1.append(itemKind[i]); //種類
				sb2.append(itemX[i]*stageH + itemY[i]); //座標-圧縮情報
				sb3.append(itemAmount[i]); //個数
				if(++i <= item_maxID){ //続きあり
					sb1.append(",");
					sb2.append(",");
					sb3.append(",");
				}else //書き込み終了
					break;
			}
		}
		//
		data.append("\r\nitemKind=").append(sb1.toString())
			.append("\r\nitemLocation=").append(sb2.toString())
			.append("\r\nitemAmount=").append(sb3.toString());
		//ステージステータス
		data.append("\r\nstageLuminance=").append(String.valueOf(stageLuminance));
		//書き込み
		final File dst = new File("stage/" + stageName[stage] + ".ini");
		if(!dst.exists()){ //何らかの原因で元ファイルが存在しないときのリカバリー
			try{
				dst.createNewFile(); //ステージファイルを新規作成
			}catch(IOException e){}
		}
		try(BufferedWriter bw = new BufferedWriter(new FileWriter("stage/" + stageName[stage] + ".ini"))){ //全情報を記入
			bw.write(data.toString());
			bw.flush();
			saved = true;
		}catch(IOException e){
		}
	}
	public void loadData(boolean newVersion){
		saved = true;
		//オブジェクト情報をクリア
		Arrays.fill(enemyKind,NONE);
		for(EnemyListener ver : cfg.enemyClass)
			ver.cleared();
		Arrays.fill(entityKind,NONE);
		for(EntityListener ver : cfg.entityClass)
			ver.cleared();
		for(GimmickListener ver : cfg.gimmickClass)
			ver.cleared();
		Arrays.fill(itemKind,NONE);

		final File dst = new File("stage/" + stageName[stage] + ".ini");
		if(!dst.exists()){ //未作成のステージ
			//初期情報を代入してメソッドを終了
			enemy_maxID = item_maxID = patrolPoint_maxID = -1;
			enemy_total = item_total = gimmick_total = entity_total = 0;
			stageLuminance = 1.0F;
			stageGridW = stageGridH = 50;
			stageW = stageH = 5000;
			stageGridTotal = 2500;
			try{
				dst.createNewFile();
				saveData();
			}catch(IOException e){}
			return;
		}
		final Properties data = new Properties();
		try{
			data.load(new InputStreamReader(getClass().getResourceAsStream("stage/" + stageName[stage] + ".ini")));
		}catch(IOException e){
			JOptionPane.showMessageDialog(null,"ステージの読み込みにエラーが発生しました。\nエラーコード：" + e,"エラー",JOptionPane.ERROR_MESSAGE);
		}

		//ステージ縦横長さの読み込み
		final String stageGridW_str = data.getProperty("stageGridW"),
			stageGridH_str = data.getProperty("stageGridH");
		if(stageGridW_str != null && !stageGridW_str.isEmpty())
			stageGridW = Integer.parseInt(stageGridW_str);
		else
			stageGridW = 50;
		stageW = stageGridW*100;
		if(stageGridH_str != null && !stageGridH_str.isEmpty())
			stageGridH = Integer.parseInt(stageGridH_str);
		else
			stageGridH = 50;
		stageH = stageGridH*100;
		stageGridTotal = stageGridW*stageGridH;
		//マップ関係配列初期化(ステージ寸法に依存するため、resetDataメソッド内では行えない)
		gimmickKind = new int[stageGridTotal];
		Arrays.fill(gimmickKind,NONE);
		xPatrolTargetMap = new int[stageGridTotal];
		yPatrolTargetMap = new int[stageGridTotal];
		Arrays.fill(xPatrolTargetMap,NONE);
		//開始地点の読み込み
		final String[] spawnPointStr = super.split2(data.getProperty("spawnPoint"),",");
		if(spawnPointStr.length > 0){
			spawnPointX = new int[spawnPointStr.length];
			spawnPointY = new int[spawnPointStr.length];
			for(int i = 0;i < spawnPointStr.length;i++){
				int spawnPoint = Integer.parseInt(spawnPointStr[i]);
				spawnPointX[i] = spawnPoint/stageH;
				spawnPointY[i] = spawnPoint%stageH;
			}
			visionX = spawnPointX[0];
			visionY = spawnPointY[0];
		}else{ //スタート地点情報なし※異常
			spawnPointX = spawnPointY = new int[]{900}; //暫定スタート地点
			visionX = visionY = playerX = playerY = 900; //左上からスタート
		}
		//ステージ明るさの読み込み
		final String luminance = data.getProperty("stageLuminance");
		if(luminance != null)
			stageLuminance = Float.parseFloat(luminance);
		else
			stageLuminance = 1.0F;
		
		//敵の読み込み
		for(EnemyListener ver : cfg.enemyClass)
			ver.loadStarted(); //ロード開始追加処理
		String[] data1 = super.split2(data.getProperty("enemyKind"),","),
			data2 = super.split2(data.getProperty("enemyLocation"),","),
			data3 = super.split2(data.getProperty("enemyAngle"),","),
			data4 = super.split2(data.getProperty("enemyProperty"),",");
		if(data1.length > 0 && !data1[0].isEmpty()){
			enemy_maxID = data1.length - 1;
			enemy_total = data1.length;
			for(int i = 0;i < data1.length;i++){
				final int kind = cfg.convertID_enemy(data1[i]);
				if(kind == -1)
					continue;
				enemyKind[i] = kind;
				if(i < data2.length){
					final int position = Integer.parseInt(data2[i]);
					enemyX[i] = position/stageH;
					enemyY[i] = position%stageH;
				}
				if(i < data3.length)
					enemyAngle[i] = Double.parseDouble(data3[i]);
				enemyHP[i] = cfg.enemyHP[kind];
				if(i < data4.length)
					cfg.enemyClass[kind].created(i,data4[i]);
			}
		}else{
			enemy_maxID = -1;
			enemy_total = 0;
		}
		for(EnemyListener ver : cfg.enemyClass)
			ver.loadFinished();
		
		//エンティティの読み込み
		for(EntityListener ver : cfg.entityClass)
			ver.loadStarted(); //ロード開始追加処理
		data1 = super.split2(data.getProperty("entityKind"),",");
		data2 = super.split2(data.getProperty("entityLocation"),",");
		data3 = super.split2(data.getProperty("entityAngle"),",");
		data4 = super.split2(data.getProperty("entityProperty"),",");
		if(data1.length > 0 && !data1[0].isEmpty()){
			entity_maxID = data1.length - 1;
			entity_total = data1.length;
			for(int i = 0;i < data1.length;i++){
				final int kind = cfg.convertID_entity(data1[i]);
				entityKind[i] = kind;
				if(i < data2.length){
					final int position = Integer.parseInt(data2[i]);
					entityX[i] = position/stageH;
					entityY[i] = position%stageH;
				}
				if(i < data3.length)
					entityAngle[i] = Double.parseDouble(data3[i]);
				entityHP[i] = cfg.entityHP[kind];
				if(i < data4.length)
					cfg.entityClass[kind].created(i,data4[i]);
			}
		}else{
			entity_maxID = -1;
			entity_total = 0;
		}
		for(EntityListener ver : cfg.entityClass)
			ver.loadFinished();
		
		//巡回点の読み込み
		data1 = super.split2(data.getProperty("patrolPoint"),",");
		data2 = super.split2(data.getProperty("patrolTarget"),",");
		data3 = super.split2(data.getProperty("patrolDelay"),",");
		if(data1.length > 0 && !data1[0].isEmpty()){
			patrolPoint_maxID = data1.length - 1;
			for(int i = 0;i < data1.length;i++){
				int position = Integer.parseInt(data1[i]);
				patrolPointX[i] = position/stageGridH*100 + 50;
				patrolPointY[i] = position%stageGridH*100 + 50;
				position = Integer.parseInt(data2[i]);
				patrolTargetX[i] = position/stageGridH*100 + 50;
				patrolTargetY[i] = position%stageGridH*100 + 50;
				patrolDelay[i] = Integer.parseInt(data3[i]);
			}
		}else
			patrolPoint_maxID = -1;
		
		//ギミックの読み込み
		for(GimmickListener ver : cfg.gimmickClass)
			ver.loadStarted(); //ロード開始追加処理
		data1 = super.split2(data.getProperty("gimmickKind"),",");
		data2 = super.split2(data.getProperty("gimmickLocation"),",");
		data3 = super.split2(data.getProperty("gimmickProperty"),",");
		if(data1.length > 0 && !data1[0].isEmpty()){
			gimmick_total = data1.length;
			for(int i = 0;i < data1.length;i++){
				final int kind = cfg.convertID_gimmick(data1[i]), //種類
					map = Integer.parseInt(data2[i]); //位置兼ID
				gimmickKind[map] = kind;
				if(i < data3.length)
					cfg.gimmickClass[kind].created(map,data3[i]);
			}
		}else
			gimmick_total = 0;
		//ギミック読み込み後処理
		for(GimmickListener ver : cfg.gimmickClass)
			ver.loadFinished();
		
		try{
			//アイテムの読み込み
			data1 = super.split2(data.getProperty("itemKind"),",");
			data2 = super.split2(data.getProperty("itemLocation"),",");
			data3 = super.split2(data.getProperty("itemAmount"),",");
			if(data1.length > 0 && !data1[0].isEmpty()){
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
		}catch(NullPointerException e){}
	}
	Image loadImage(String url){
		try{
			final Image img = createImage((ImageProducer)getClass().getResource("picture/" + url).getContent());
			tracker.addImage(img,1);
			return img;
		}catch(IOException | NullPointerException e){
			JOptionPane.showMessageDialog(null,"画像\"" + url + "\"が見つかりませんでした。この画像は描画されません。","読み込みエラー",JOptionPane.WARNING_MESSAGE);
			return createImage(1,1);
		}
	}
	/**
	* ステージの大きさを左上方向から変更します。
	* 内部的にはこの数値をすべてのオブジェクトに足して動かしているので、頻繁な呼び出しは重いかもしれません。
	* @param gridW 横幅増幅量(グリッド値)
	* @param gridH 縦幅増幅量(グリッド値)
	* @return 変更が成功したか
	* @since beta9.0
	*/
	boolean stageSizeChange_MinusDirection(int gridW,int gridH){
		saved = false;
		if(stageGridW + gridW <= 14 || stageGridH + gridH <= 14)
			return false;
		final int w = gridW*100,h = gridH*100;
		//まず全オブジェクトに加算
		for(int i = 0;i <= enemy_maxID;i++){
			if(enemyKind[i] != NONE){
				enemyX[i] += w;
				enemyY[i] += h;
			}
		}
		for(int i = 0;i <= entity_maxID;i++){
			if(entityKind[i] != NONE){
				entityX[i] += w;
				entityY[i] += h;
			}
		}
		for(int i = 0;i <= item_maxID;i++){
			if(itemKind[i] != NONE){
				itemX[i] += w;
				itemY[i] += h;
			}
		}
		for(int i = 0;i <= patrolPoint_maxID;i++){
			patrolPointX[i] += w;
			patrolPointY[i] += h;
			patrolTargetX[i] += w;
			patrolTargetY[i] += h;
		}
		for(int i = 0;i < spawnPointX.length;i++){
			spawnPointX[i] += w;
			spawnPointY[i] += h;
		}
		visionX += w;
		visionY += h;
		//位置情報がIDに入っているオブジェクトの処理
		final int newStageGridW = stageGridW + gridW,newStageGridH = stageGridH + gridH; //新ステージ寸法
		final int newStageGridTotal = newStageGridW*newStageGridH; //新ステージグリッド総数
		final int[] gimmickKind_tmp = new int[newStageGridTotal];
		Arrays.fill(gimmickKind_tmp,NONE);
		for(int i = 0;i < stageGridTotal;i++){
			final int kind = gimmickKind[i];
			if(kind == NONE)
				continue;
			final int oldX = i/stageGridH,oldY = i%stageGridH;
			if(oldX < -gridW || oldY < -gridH){ //ステージ縮小によって消された位置にあるギミック(一番右、一番下にあるギミック)
				gimmick_total--; //総数減少
				cfg.gimmickClass[kind].deleted(i); //削除追加処理を呼び出す
				continue;
			}
			final int newID = i + gridH*(oldX + 1) + gridW*newStageGridH;
			cfg.gimmickClass[kind].moved(i,newID); //ID変更追加処理
			gimmickKind_tmp[newID] = kind;
		}
		this.gimmickKind = gimmickKind_tmp;
		//実際にステージ幅を変更する
		stageW = (stageGridW = newStageGridW)*100;
		stageH = (stageGridH = newStageGridH)*100;
		stageGridTotal = newStageGridTotal;
		return true;
	}
	/**
	* ステージの大きさを右下方向から変更します。
	* ステージの大きさ変更はできるだけこちらから行ってください。
	* @param gridW 横幅増幅量(グリッド値)
	* @param gridH 縦幅増幅量(グリッド値)
	* @return 変更が成功したか
	* @since beta9.0
	*/
	boolean stageSizeChange_PlusDirection(int gridW,int gridH){
		saved = false;
		if(stageGridW + gridW <= 14 || stageGridH + gridH <= 14)
			return false;
		final int newStageGridW = stageGridW + gridW,newStageGridH = stageGridH + gridH; //新ステージ寸法
		final int totalGrid_new = newStageGridW*newStageGridH; //新ステージグリッド総数
		//位置情報がIDに入っているオブジェクトを先に処理
		if(gridH != 0){ //グリッド位置は縦を左から右へ数えているので、縦長さが変わる場合だけを処理する
			final int[] gimmickKind_tmp = new int[totalGrid_new];
			Arrays.fill(gimmickKind_tmp,NONE);
			for(int i = 0;i < stageGridTotal;i++){
				final int kind = gimmickKind[i];
				if(kind == NONE)
					continue;
				final int oldX = i/stageGridH,oldY = i%stageGridH;
				if(oldX >= newStageGridW || oldY >= newStageGridH){ //ステージ縮小によって消された位置にあるギミック
					cfg.gimmickClass[kind].deleted(i); //削除追加処理を呼び出す
					gimmick_total--; //総数減少
					continue;
				}
				final int newID = i + gridH*oldX;
				cfg.gimmickClass[kind].moved(i,newID); //ID変更追加処理
				gimmickKind_tmp[newID] = kind; //新ID = 旧ID + 縦方向増加量*旧横座標
			}
			this.gimmickKind = gimmickKind_tmp;
			//巡回点はグリッド位置にしか存在しないが、エディター内ではピクセル座標に変換して処理しているのでこの操作は必要としない
			//ステージ縮小によって消された他のオブジェクトは、各自のAction内で自動的に消える
		}
		//実際にステージ幅を変更する
		stageW = (stageGridW = newStageGridW)*100;
		stageH = (stageGridH = newStageGridH)*100;
		stageGridTotal = totalGrid_new;
		return true;
	}
}