package compiler.imcode;

import java.util.*;

import compiler.*;

public class ImCode {
	private final boolean dump;

	public ImCode(boolean dump) {
		this.dump = dump;
	}

	public void dump(LinkedList<ImcChunk> chunks) {
		if (!dump || Report.dumpFile() == null) {
			return;
		}

		for (ImcChunk imcChunk : chunks) {
			imcChunk.dump();
		}
	}
}
