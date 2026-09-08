package net.lacnic.elections.data;

public final class CensusUpsertResult {
	public int existingRows;
	public int totalImportedRows;
	public int toDeleteRows;
	public int createdRows;
	public int updatedRows;
	public int deletedRows;
	public int regeneratedTokenRows;
	public int keptTokenRows;
	public int generatedMissingTokenRows;
}
