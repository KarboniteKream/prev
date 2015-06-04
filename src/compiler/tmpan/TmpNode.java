package compiler.tmpan;

import java.util.*;

import compiler.frames.*;

public class TmpNode
{
	public FrmTemp temp;
	public LinkedList<TmpNode> edges;

	public int register;
	public boolean spill;

	public TmpNode(FrmTemp temp)
	{
		this.temp = temp;
		edges = new LinkedList<TmpNode>();

		register = null;
		spill = false;
	}
}
