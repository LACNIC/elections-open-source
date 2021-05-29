package net.lacnic.elections.ejb;

import java.util.List;
import java.io.File;

import net.lacnic.elections.data.ResultadoEleccionesData;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.OperationNotPermittedException;


public interface VotanteEleccionesEJB {

	public List<Object[]> obtenerCodigosdeVotacion(long idEleccion);

	public void intentoFallidoIp(String remoteAddress);

	public List<Candidate> obtenerCandidatosEleccionOrdenados(long idEleccion) throws Exception;

	public List<Candidate> obtenerCandidatosEleccionConVotos(long idEleccion) throws Exception;

	public long obtenerVotosCandidato(long idCandidato) throws Exception;

	public long obtenerCantidadVotantesQueVotaronEleccion(long idEleccion) throws Exception;

	public boolean yaVoto(long idUsuarioPadron) throws Exception;

	public long obtenerTotalVotosEleccion(long idEleccion) throws Exception;

	byte[] obtenerAuditoriaAquaIt();

	byte[] obtenerRequerimientosV1();

	byte[] obtenerEleccionesRolesFuncionamientoRevision();
	
	byte[] obtenerEleccionesRolesFuncionamientoRevision(String filePath);
	
	File getEleccionesRolesFuncionamientoRevision(String filePath);

	ResultadoEleccionesData obtenerResultadoEleccionesData(long idEleccion) throws Exception;

	UserVoter verificarAccesoUP(String token);

	Auditor verificarAccesoResultadoAuditor(String token);

	Election verificarAccesoResultado(String token);

	void votar(List<Candidate> candidatos, UserVoter up, String ip) throws OperationNotPermittedException;

	void confirmarEleccionAuditor(long idAuditor);

	public List<Object[]> obtenerCandidatosVotacion(long idUsuarioPadron, long idEleccion);

	List<Object> obtenerDataEvolucionVotosEleccion(long idEleccion);

	void habilitarRevisionEleccionAuditor(long idAuditor, String ip);

	public Election obtenerEleccion(long idAsLong);

	public boolean isEleccionSimple(long idEleccion);

	UserVoter[] verificarAccesoUPEleccionJunta(String token);

	JointElection obtenerSupraEleccion(long idEleccion);

}
