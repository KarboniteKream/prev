package compiler.frames;

public class FrmLabel {
	private static int labelCount = 0;
	private final String name;

	public FrmLabel(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object l) {
		return name.equals(((FrmLabel) l).name);
	}

	public String name() {
		return name;
	}


	public static FrmLabel newLabel() {
		return new FrmLabel("L" + (labelCount++));
	}

	public static FrmLabel newLabel(String name) {
		return new FrmLabel("_" + name);
	}
}
