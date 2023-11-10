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
import capaControladorINV.TiendaCtrl;
import capaModeloINV.CapacitacionEmpleado;
import capaModeloINV.ControlPorcionEmpleado;

/**
 * Servlet implementation class InsertarDespachoTienda
 */
@WebServlet("/InsertarControlPorcionEmpleado")
/**
 * Servlet que tiene como objetivo la INserción del encabezado de un despacho tienda.
 * @author JuanDavid
 *
 */
public class InsertarControlPorcionEmpleado extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarControlPorcionEmpleado() {
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
		int idUsuario = Integer.parseInt(request.getParameter("idusuario"));
		int idTienda = Integer.parseInt(request.getParameter("idtienda"));
		int idPedido = Integer.parseInt(request.getParameter("idpedido"));
		int idDetallePedido = Integer.parseInt(request.getParameter("iddetallepedido"));
		String fecha = request.getParameter("fecha");
        ControlPorcionEmpleado porEmp = new ControlPorcionEmpleado(idUsuario,idTienda, idPedido, idDetallePedido,fecha);
        TiendaCtrl tiendaCtrl = new TiendaCtrl();
        String respuesta = tiendaCtrl.insertarControlPorcionEmpleado(porEmp);
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
