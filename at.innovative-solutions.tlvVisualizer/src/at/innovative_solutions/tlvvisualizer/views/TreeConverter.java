package at.innovative_solutions.tlvvisualizer.views;

import at.innovative_solutions.tlv.ConstructedTLV;
import at.innovative_solutions.tlv.PrimitiveTLV;
import at.innovative_solutions.tlv.TLV;

public class TreeConverter implements at.innovative_solutions.tlv.Formatter<TreeObject> {
	@Override
	public TreeObject format(TLV tlv) {
		return tlv.accept(this);
	}

	@Override
	public TreeObject format(PrimitiveTLV tlv) {
		TreeObject o = new TreeObject(tlv.getID().toString());
		return o;
	}

	@Override
	public TreeObject format(ConstructedTLV tlv) {
		TreeParent parent = new TreeParent(tlv.getID().toString());
		
		for(TLV child : tlv.getTLVs()) {
			TreeObject o = child.accept(this);
			parent.addChild(o);
		}
		
		return parent;
	}
}
