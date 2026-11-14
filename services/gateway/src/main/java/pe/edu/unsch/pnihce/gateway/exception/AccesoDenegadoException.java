package pe.edu.unsch.pnihce.gateway.exception;

public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException() {
        super("El paciente no ha otorgado consentimiento para acceder a su historia clínica");
    }
}
