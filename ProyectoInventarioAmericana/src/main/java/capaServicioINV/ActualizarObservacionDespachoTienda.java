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

/**
 * Servlet implementation class InsertarDespachoTienda
 */
@WebServlet("/ActualizarObservacionDespachoTienda")
/**
 * Servlet que tiene como objetivo la INserción del encabezado de un despacho tienda.
 * @author JuanDavid
 *
 */
public class ActualizarObservacionDespachoTienda extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarObservacionDespachoTienda() {
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
		String observacion = request.getParameter("observacion");
        int idDespacho;
        try
        {
        	idDespacho = Integer.parseInt(request.getParameter("iddespacho"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idDespacho = 0;
        }
        InventarioCtrl inv = new InventarioCtrl();
        String respuesta = inv.actualizarObservacionDespachoTienda(idDespacho, observacion);
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
