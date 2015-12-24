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
		public String toString() {
			switch(_order) {
				case 0: return "ParsingID";
				case 1: return "ParsingLength";
				case 2: return "FindingEnd";
				case 3: return "GettingData";
			}
			throw new RuntimeException("Invalid enum");
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
	public int getSerializedLength() {
		return 0; // TODO fix what to do here
	}
	
	@Override
	public byte[] toBytes() { return new byte[0]; }
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ErrorTLV(");
		sb.append(_stage.toString()).append(", ");
		
		if(_id != null)
			sb.append(_id.toString()).append(", ");
		else
			sb.append("null, ");
		
		if(_msg != null)
			sb.append(_msg).append(", ");
		else
			sb.append("null, ");
		
		if(_length != null)
			sb.append(_length).append(", ");
		else
			sb.append("null, ");
		
		sb.append(_lengthIndefinite).append(", ");
	
		if(_data != null)
			sb.append(_data).append(", ");
		else
			sb.append("null");
		sb.append(")");
		return sb.toString();
	}

	@Override
	public <T> T accept(Formatter<T> formatter) {
		return formatter.format(this);
	}
}
