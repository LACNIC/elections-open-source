package net.lacnic.elections.ejb;

import java.util.List;
import java.io.File;

import net.lacnic.elections.data.ResultadoEleccionesData;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidato;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.SupraEleccion;
import net.lacnic.elections.domain.UsuarioPadron;
import net.lacnic.elections.exception.OperationNotPermittedException;


public interface VotanteEleccionesEJB {

	public List<Object[]> obtenerCodigosdeVotacion(long idEleccion);

	public void intentoFallidoIp(String remoteAddress);

	public List<Candidato> obtenerCandidatosEleccionOrdenados(long idEleccion) throws Exception;

	public List<Candidato> obtenerCandidatosEleccionConVotos(long idEleccion) throws Exception;

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

	UsuarioPadron verificarAccesoUP(String token);

	Auditor verificarAccesoResultadoAuditor(String token);

	Eleccion verificarAccesoResultado(String token);

	void votar(List<Candidato> candidatos, UsuarioPadron up, String ip) throws OperationNotPermittedException;

	void confirmarEleccionAuditor(long idAuditor);

	public List<Object[]> obtenerCandidatosVotacion(long idUsuarioPadron, long idEleccion);

	List<Object> obtenerDataEvolucionVotosEleccion(long idEleccion);

	void habilitarRevisionEleccionAuditor(long idAuditor, String ip);

	public Eleccion obtenerEleccion(long idAsLong);

	public boolean isEleccionSimple(long idEleccion);

	UsuarioPadron[] verificarAccesoUPEleccionJunta(String token);

	SupraEleccion obtenerSupraEleccion(long idEleccion);

}
