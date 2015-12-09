package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcDataChunk extends ImcChunk
{
	public FrmLabel label;
	public int size;

	public static final int BYTE = 1;
	public static final int WYDE = 2;
	public static final int TETRA = 4;
	public static final int OCTA = 8;

	// stmt ali expr?
	public ImcStmt imcode;
	public String data;

	public ImcDataChunk(FrmLabel label, int size)
	{
		this(label, size, null, null);
	}

	public ImcDataChunk(FrmLabel label, int size, ImcStmt imcode, String data)
	{
		this.label = label;
		this.size = size;
		this.imcode = imcode;
		this.data = data;
	}

	@Override
	public void dump()
	{
		Report.dump(0, "DATA CHUNK: label=" + label.name() + " size=" + size);
	}
}
