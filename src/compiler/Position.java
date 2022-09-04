package compiler;

public class Position {
	private final int begLine;
	private final int begColumn;

	private final int endLine;
	private final int endColumn;

	public Position(int begLine, int begColumn, int endLine, int endColumn) {
		this.begLine = begLine;
		this.begColumn = begColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	public Position(int line, int column) {
		this(line, column, line, column);
	}

	public Position(Position begPos, Position endPos) {
		this.begLine = begPos.begLine;
		this.begColumn = begPos.begColumn;
		this.endLine = endPos.endLine;
		this.endColumn = endPos.endColumn;
	}

	@Override
	public String toString() {
		return (begLine + ":" + begColumn + "-" + endLine + ":" + endColumn);
	}
}
