
public abstract class Radar{
	BreakScope bs;
	
	int cost = 0; //�g�p�R�X�g,0�ŏ�������

	public void construct(BreakScope bs){
		this.bs = bs;
	}
	public abstract void action(); //��{�A�N�V����(���[�_�[�̕`��)
}