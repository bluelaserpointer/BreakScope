
public class HPInferRadar extends Radar{ //�M���T�m���[�_�[
	//�����Ƃ��߂��M���̕��p���w���A�����͂킩��Ȃ�
	//�ǂ�Ȃɉ����Ă��킸���ȔM�ʂ𐳊m�ɂƂ炦�邱�Ƃ��\
	//�F����1�̂܂�

	BreakScope bs;
	
	int cost = 0; //�g�p�R�X�g,0�ŏ�������

	public void construct(BreakScope bs){
		this.bs = bs;
	}
	public abstract void action(); //��{�A�N�V����(���[�_�[�̕`��)
}