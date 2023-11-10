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
import capaModeloINV.LlaveEvento;

/**
 * Servlet implementation class CalcularInventarioTienda
 */
@WebServlet("/InsertarLlaveEvento")
public class InsertarLlaveEvento extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarLlaveEvento() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * Este es el método del servlet que se encarga de recuperar los valores de idtienda y fecha a surtir
	 * con estos instanciará un objeto de la capa Controlador y la respuesta en un string con formato JSON será retornada.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		Logger logger = Logger.getLogger("log_file");
		HttpSession sesion = request.getSession(true);
		int idLlave;
		int idUsuarioRegistro;
		int idUsuarioPrestamo;
		String observacion;
		String evento;
        try
        {
        	idLlave = Integer.parseInt(request.getParameter("idllave"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idLlave = 0;
        }
        
        try
        {
        	idUsuarioRegistro = Integer.parseInt(request.getParameter("idusuarioregistro"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idUsuarioRegistro = 0;
        }
        
        try
        {
        	idUsuarioPrestamo = Integer.parseInt(request.getParameter("idusuarioprestamo"));
        	
        }catch(Exception e)
        {
        	logger.error(e.toString());
        	idUsuarioPrestamo = 0;
        }
        observacion = request.getParameter("observacion");
        evento = request.getParameter("evento");
        InventarioCtrl inv = new InventarioCtrl();
        LlaveEvento llavEv = new LlaveEvento(idLlave, idUsuarioRegistro, idUsuarioPrestamo, observacion);
        llavEv.setEvento(evento);
        String respuesta = inv.insertarLlavesEvento(llavEv);
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
