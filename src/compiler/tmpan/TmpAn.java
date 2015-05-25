package compiler.tmpan;

import java.util.*;

import compiler.*;
import compiler.imcode.*;

public class TmpAn
{
	private boolean dump;

	public TmpAn(boolean dump)
	{
		this.dump = dump;
	}

	public void analyze(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;
			}
		}
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false) return;
		if(Report.dumpFile() == null) return;
	}
}
