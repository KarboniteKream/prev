package compiler.lincode;

import java.util.*;

import compiler.*;
import compiler.imcode.*;

public class LinCode
{
	private boolean dump;

	public LinCode(boolean dump)
	{
		this.dump = dump;
	}

	public void generate(LinkedList<ImcChunk> chunks)
	{
		// Ali ta class sploh potrebujemo?
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;
				codeChunk.lincode = codeChunk.imcode.linear();
			}
		}
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if (!dump)
			return;
		if (Report.dumpFile() == null)
			return;
		for (int chunk = 0; chunk < chunks.size(); chunk++)
			chunks.get(chunk).dump();
	}
}
