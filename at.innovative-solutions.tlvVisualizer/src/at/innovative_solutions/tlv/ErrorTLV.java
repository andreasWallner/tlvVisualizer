package at.innovative_solutions.tlv;

public class ErrorTLV extends TLV {
	public enum ParseStage {
		ParsingID (0),
		ParsingLength (1),
		FindingEnd (2),
		GettingData (3);
		
		private final int _order;
		ParseStage(int order) {
			_order = order;
		}
		public int order() {
			return _order;
		}
	};
	
	final ParseStage _stage; 
	final String _msg;
	final Integer _length;
	final byte[] _data;
	
	public ErrorTLV(
			ParseStage stage,
			ID id,
			String msg,
			Integer length,
			boolean indefiniteLength,
			byte[] data) {
		super(id, indefiniteLength);
		_stage = stage;
		_msg = msg;
		_length = length;
		_data = data;
	}
	
	public String getError() {
		return _msg;
	}
	
	public ParseStage getStage() {
		return _stage;
	}
	
	public byte[] getRemainingData() {
		return _data;
	}
	
	@Override
	public int getLength() {
		return _length != null ? _length : 0;
	}

	@Override
	public <T> T accept(Formatter<T> formatter) {
		return formatter.format(this);
	}
}
