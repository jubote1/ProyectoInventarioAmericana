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

import capaControladorINV.InventarioCtrl;
import capaModeloINV.DesechoTienda;

/**
 * Servlet implementation class InsertarDespachoTienda
 */
@WebServlet("/CambiarEstadoAprovechable")
/**
 * Servlet que tiene como objetivo la INserci�n del encabezado de un despacho tienda.
 * @author JuanDavid
 *
 */
public class CambiarEstadoAprovechable extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CambiarEstadoAprovechable() {
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
        int idaprovechable;
        try
        {
        	idaprovechable = Integer.parseInt(request.getParameter("idaprovechable"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idaprovechable = 0;
        }
        int idestado;
        try
        {
        	idestado = Integer.parseInt(request.getParameter("idestado"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idestado = 0;
        }
        InventarioCtrl inv = new InventarioCtrl();
        String respuesta = inv.actualizarEstadoAprovechable(idaprovechable, idestado);
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
