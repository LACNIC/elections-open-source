package net.lacnic.elections.domain;

/**
 * Enum for different recipient types for emails sent from the system
 */
public enum RecipientType {
	VOTANTES("Se enviará a todos los votantes de la elección"), 
	VOTANTES_BR("Se enviará a todos los votantes con país Brasil de la elección"), 
	VOTANTES_MX("Se enviará a todos los votantes con país México de la elección"), 
	VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES("Se enviará solamente a aquellos votantes que aún no votaron en alguna de las dos elecciones, o en una de ellas."), 
	VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_BR("Se enviará solamente a aquellos votantes que aún no votaron en alguna de las dos elecciones, o en una de ellas, con país Brasil."), 
	VOTANTES_QUE_AUN_NO_VOTARON_EN_DOS_ELECCIONES_MX("Se enviará solamente a aquellos votantes que aún no votaron en alguna de las dos elecciones, o en una de ellas, con país México."), 
	VOTANTES_QUE_AUN_NO_VOTARON("Se enviará solamente a aquellos votantes que aún no votaron en la elección"), 
	VOTANTES_QUE_AUN_NO_VOTARON_BR("Se enviará solamente a aquellos votantes con país Brasil que aún no votaron en la elección"), 
	VOTANTES_QUE_AUN_NO_VOTARON_MX("Se enviará solamente a aquellos votantes con país México que aún no votaron en la elección"), 
	VOTANTES_QUE_YA_VOTARON("Se enviará solamente a aquellos votantes que ya votaron en la elección"), 
	VOTANTES_QUE_YA_VOTARON_BR("Se enviará solamente a aquellos votantes con país Brasil que ya votaron en la elección"), 
	VOTANTES_QUE_YA_VOTARON_MX("Se enviará solamente a aquellos votantes con país México que ya votaron en la elección"), 
	AUDITORES("Se enviará a todos los auditores de la elección"), 
	AUDITORES_QUE_AUN_NO_CONFORMARON("Se enviará solamente a aquellos auditores que aún no conformaron la elección"), 
	AUDITORES_QUE_YA_CONFORMARON("Se enviará solamente a aquellos auditores que ya conformaron la elección");


	private String description;


	private RecipientType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
