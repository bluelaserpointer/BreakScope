import java.awt.*;
import javax.sound.sampled.*; //javazoom mp3spiで強化されています
import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
	対応ファイル:wav,au,mp3
*/
public class SoundClip{

	private final Clip clip;
	private final FloatControl control;
	SoundClip(String url){
		Clip clip = null;
		try{
			final AudioInputStream in = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(url));
			clip = AudioSystem.getClip();
			if(url.endsWith(".mp3")){
				final AudioFormat baseFormat = in.getFormat();
				clip.open(AudioSystem.getAudioInputStream(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
																							baseFormat.getSampleRate(),
																							16,
																							baseFormat.getChannels(),
																							baseFormat.getChannels() * 2,
																							baseFormat.getSampleRate(),
																							false),in));
			}else
				clip.open(in);
			in.close();
		}catch(NullPointerException | IOException e){
			JOptionPane.showMessageDialog(null,"音声ファイル\"" + url + "\"が見つかりませんでした。このBGMは流れません。","読み込みエラー",JOptionPane.WARNING_MESSAGE);
		}catch(UnsupportedAudioFileException e){
			if(clip != null)
				JOptionPane.showMessageDialog(null,"音声ファイル\"" + url + "\"のメディア形式\"" + clip.getFormat() + "\"には対応できません。","読み込みエラー",JOptionPane.WARNING_MESSAGE);
			else
				JOptionPane.showMessageDialog(null,"音声ファイル\"" + url + "\"のメディア形式には対応できません。","読み込みエラー",JOptionPane.WARNING_MESSAGE);
		}catch(LineUnavailableException e){
			JOptionPane.showMessageDialog(null,"音声ファイル\"" + url + "\"を開けませんでした。ほかのアプリケーションが使用中の可能性があります。","読み込みエラー",JOptionPane.WARNING_MESSAGE);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,e.toString(),"読み込みエラー",JOptionPane.WARNING_MESSAGE);
		}
		this.clip = clip;
		FloatControl control = null;
		try{
			control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		}catch(Exception e){}
		this.control = control;
	}
	SoundClip(){
		clip = null;
		control = null;
	}
	final void play(){ //再生
		if(clip != null)
			clip.start();
	}
	final void pause(){ //一時停止
		if(clip != null)
			clip.stop();
	}
	final void stop(){ //停止
		if(clip != null){
			clip.stop();
			clip.setFramePosition(0);
		}
	}
	final void loop(int count){ //ループ再生
		if(clip != null)
			clip.loop(count);
	}
	final void loop(){ //無限ループ再生
		if(clip != null)
			clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	final void setVolume(double volume){ //setVolume(0.5);  50%の音量で再生する
		if(control != null)
			control.setValue((float)Math.log10(volume) * 20);
	}
	final boolean isRunning(){ //再生しているか
		if(clip != null)
			return clip.isRunning();
		else
			return false;
	}
}