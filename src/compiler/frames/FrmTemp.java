package compiler.frames;

public class FrmTemp {
	private static int count = 0;
	private final int num;

	public FrmTemp() {
		num = count++;
	}

	public String name() {
		return "T" + num;
	}

	@Override
	public boolean equals(Object t) {
		return num == ((FrmTemp) t).num;
	}
}
