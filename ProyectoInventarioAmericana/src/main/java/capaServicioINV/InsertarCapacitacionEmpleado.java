package capaServicioINV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import capaControladorINV.EmpleadoCtrl;
import capaControladorINV.InventarioCtrl;
import capaModeloINV.CapacitacionEmpleado;

/**
 * Servlet implementation class InsertarDespachoTienda
 */
@WebServlet("/InsertarCapacitacionEmpleado")
/**
 * Servlet que tiene como objetivo la INserción del encabezado de un despacho tienda.
 * @author JuanDavid
 *
 */
public class InsertarCapacitacionEmpleado extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarCapacitacionEmpleado() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		int idEmpleado = Integer.parseInt(request.getParameter("id"));
		String fecha = request.getParameter("fecha");
		String horaInicio = request.getParameter("horainicio");
		String horaFinal = request.getParameter("horafinal");
		String observacion = request.getParameter("observacion");
		String nombreLargo = request.getParameter("nombrelargo");
        CapacitacionEmpleado capEmp = new CapacitacionEmpleado(0, idEmpleado, fecha, horaInicio, horaFinal, observacion, nombreLargo);
        EmpleadoCtrl empCtrl = new EmpleadoCtrl();
        String respuesta = empCtrl.insertarCapacitacionEmpleado(capEmp);
        PrintWriter out = response.getWriter();
		out.write(respuesta);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
