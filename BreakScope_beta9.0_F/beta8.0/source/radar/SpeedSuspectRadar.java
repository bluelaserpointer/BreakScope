
public class SpeedSuspectRadar extends Radar{ //�����ړ��̎@�m���[�_�[
	//���@��80%�̑��x�ňړ�����G�����@�𔭌�����ƁA�x�����o�����[�_�[
	//�ǐՌ^�̓G�̏P�������O�Ɏ@�m���邱�Ƃ��\
	//�F����1�̂܂ŁA�����̂ɔ������Ă��\���͕ω����Ȃ�

	BreakScope bs;
	
	int cost = 0; //�g�p�R�X�g,0�ŏ�������

	public void construct(BreakScope bs){
		this.bs = bs;
	}
	public abstract void action(); //��{�A�N�V����(���[�_�[�̕`��)
}