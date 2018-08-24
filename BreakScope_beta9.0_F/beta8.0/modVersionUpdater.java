import java.io.*;
import java.net.*;

class modVersionUpdater{
	public static void main(String[] args){
		new modVersionUpdater();
	}
	modVersionUpdater(){
		final String[] configFileNames = {"bullet","effect","enemy","entity","gimmick","weapon"};
		final FilenameFilter iniFilter = new FilenameFilter(){
				public boolean accept(File dir,String name){
					return name.endsWith(".ini");
				}
			};
		final String configVersionNames[] = {"config","config1.0"};
		for(String configVersionName : configVersionNames){
			for(String configFileName : configFileNames){
				String[] fileNames;
				final File f = new File("source/" + configVersionName + "/" + configFileName);
				if(!f.exists())
					continue;
				fileNames = f.list(iniFilter);
				for(String fileName : fileNames){
					final StringBuilder sb = new StringBuilder("CONFIG_VERSION = 1.0\r\n");
					try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("source/" + configVersionName + "/" + configFileName + "/" + fileName),"SJIS"))){
						String tmp = br.readLine();
						String comment = null;
						if(tmp.startsWith("CONFIG_VERSION = ")) //バージョンスタンプの重複回避
							tmp = br.readLine();
						if(tmp == null)
							continue;
						while(true){
							if(tmp.matches("[0-9]+?_.*")){
								if(tmp.matches("[0-9]+?_Name.*")){
									if(comment == null)
										sb.append("[]\r\n");
									else{
										sb.append("[").append(comment).append("]\r\n");
										comment = null;
									}
								}if(tmp.matches("[0-9]+?_ActionLength.*")){ //ActionLengthは廃止
									tmp = br.readLine();
									if(tmp == null){ //読み込み終了
										if(comment != null)
											sb.append(comment);
										break;
									}
									continue;
								}else{
									if(comment != null){
										sb.append(comment).append("\r\n");
										comment = null;
									}
									//今回のバージョンの項目名変更 計９か所
									//エフェクト
									tmp = tmp.replaceFirst("ImgsLastingFrame","ImgTimePhase");
									tmp = tmp.replaceFirst("AlphaChangeFrame","AlphaTimePhase");
									//ウエポン
									tmp = tmp.replaceFirst("Range","LimitRange");
									tmp = tmp.replaceFirst("ReloadLength","ReloadTime");
									tmp = tmp.replaceFirst("BulletSpeedAVG","BulletSpeed");
									tmp = tmp.replaceFirst("BulletRefrectiveness","BulletReflectiveness");
									//バレット
									tmp = tmp.replaceFirst("RotateRedians","RotateRadius");
									tmp = tmp.replaceFirst("LifeSpan","LimitFrame");
									tmp = tmp.replaceFirst("Accel","StallRatio");
								}
								sb.append(tmp.substring(tmp.indexOf("_") + 1)).append("\r\n");
							}else{
								if(comment != null){
									sb.append(comment).append("\r\n");
									comment = null;
								}
								if(tmp.isEmpty())
									sb.append("\r\n");
								else
									comment = tmp;
							}
							tmp = br.readLine();
							if(tmp == null){ //読み込み終了
								if(comment != null)
									sb.append(comment);
								break;
							}
						}
					}catch(IOException e){}
					BufferedWriter bw = null;
					try{
						new File("source/__config1.0__/" + configFileName).mkdirs();
						final File file = new File("source/__config1.0__/" + configFileName + "/" + fileName);
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"SJIS"));
						bw.write(sb.toString());
						bw.flush();
					}catch(IOException e2){
						System.out.println(e2);
						try{
							bw.close();
						}catch(Exception e){}
					}
				}
			}
		}
	}
}