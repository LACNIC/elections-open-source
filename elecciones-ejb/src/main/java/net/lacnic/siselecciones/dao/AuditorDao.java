package net.lacnic.siselecciones.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.siselecciones.dominio.Auditor;

public class AuditorDao {

	private static final String ID_ELECCION = "idEleccion";

	private EntityManager em;

	public AuditorDao(EntityManager em) {
		this.em = em;
	}

	/**
	 * Obtiene el Auditor de id=id
	 * 
	 * @param id
	 *            el id de un Auditor
	 * @return Devuelve la entidad Auditor correspondiente al identificador
	 */
	public Auditor obtenerAuditor(long id) {
		Query q = em.createQuery("SELECT c FROM Auditor c WHERE c.idAuditor =:idAuditor");
		q.setParameter("idAuditor", id);
		return (Auditor) q.getSingleResult();
	}

	/**
	 * Obtiene todos los auditores del sistema sin importar la eleccion a la que
	 * corresponden
	 * 
	 * @return Devfuelve todos los auditores del sistema sin importar la
	 *         eleccion a la que corresponden
	 */
	public List<Auditor> obtenerAuditores() {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c", Auditor.class);
		return q.getResultList();
	}

	/**
	 * Obtiene una lista con los auditores de la eleccion de id=idEleccion
	 * 
	 * @param idEleccion
	 *            el id de la Eleccion de la cual queremos obtener los auditores
	 * @return Devuelve una lista de Auditores que aplican para la eleccion de
	 *         idEleccion
	 */
	public List<Auditor> obtenerAuditoresEleccion(long idEleccion) {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c WHERE c.eleccion.idEleccion =:idEleccion", Auditor.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public boolean existeAuditor(long idEleccion, String nombre, String mail) {
		Query q = em.createQuery("SELECT c.idAuditor FROM Auditor c WHERE c.eleccion.idEleccion =:idEleccion AND c.nombre=:nombre AND c.mail =:mail");
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter("mail", mail);
		q.setParameter("nombre", nombre);
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

	public Auditor obtenerAuditorTokenResultado(String token) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.tokenResultado =:token", Auditor.class);
		q.setParameter("token", token);
		return q.getSingleResult();
	}

	/**
	 * Obtiene todos los UUID de auditores existentes, es utilizado para chekear
	 * que no se repitan los codigos generados
	 * 
	 * 
	 * @return Devuelve una lista con todos los UUIDS creados hasta el momento
	 *         para descartar repeticiones
	 */
	public List<String> obtenerTodosLosUUIDs() {
		Query q = em.createQuery("SELECT a.tokenResultado FROM Auditor a");
		return q.getResultList();
	}

	public List<Auditor> obtenerAuditoresEleccionQueNoConformaron(long idEleccion) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.comisionado=TRUE AND a.expresoConformidad=FALSE and a.eleccion.idEleccion=:idEleccion", Auditor.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<Auditor> obtenerAuditoresEleccionQueYaConformaron(long idEleccion) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.comisionado=TRUE AND a.expresoConformidad=TRUE and a.eleccion.idEleccion=:idEleccion", Auditor.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

}
