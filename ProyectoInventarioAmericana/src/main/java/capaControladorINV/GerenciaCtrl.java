package capaControladorINV;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import capaDAOINV.GerenciaGastoDAO;
import capaDAOINV.GerenciaSemanaDAO;

/**
 * Lo que consumen las pantallas del menu Gerencia.
 *
 * Aca vive la unica regla del calendario que no esta en la base: como se genera
 * un ano nuevo. Todo lo demas -guardar, cerrar, leer- lo hace el DAO.
 */
public class GerenciaCtrl {

	private static final String[] MESES = { "", "Enero", "Febrero", "Marzo", "Abril", "Mayo",
			"Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

	/** Un mes con menos de cuatro semanas casi siempre es un calendario mal armado. */
	private static final int SEMANAS_MINIMAS_POR_MES = 4;

	// =======================================================================
	// CALENDARIO
	// =======================================================================

	/** El calendario guardado de un ano, con su estado y el resumen por mes. */
	@SuppressWarnings("unchecked")
	public String consultarCalendario(final int anio) {
		final JSONObject raiz = new JSONObject();
		final ArrayList<GerenciaSemanaDAO.Semana> semanas = GerenciaSemanaDAO.obtenerSemanas(anio);

		raiz.put("anio", Integer.valueOf(anio));
		raiz.put("estado", GerenciaSemanaDAO.estadoAnio(anio));
		raiz.put("semanas", aJsonSemanas(semanas));
		raiz.put("meses", resumenPorMes(semanas));
		raiz.put("anios", aJsonAnios());
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	/**
	 * Propone el calendario de un ano SIN guardarlo.
	 *
	 * La propuesta se muestra en pantalla, se edita si hace falta y solo
	 * entonces se guarda. Generar y guardar de un solo golpe haria que un clic
	 * por equivocado reescribiera un ano entero.
	 *
	 * LA REGLA: la semana se le carga al mes donde caen la mayoria de sus dias.
	 * Siete dias se parten cuatro y tres, nunca empatan, y por eso no hace falta
	 * desempatar. Equivale a decir "el mes donde cae el jueves".
	 *
	 * DE DONDE ARRANCA: del lunes de la semana que contiene el 1 de enero, salvo
	 * que el ano anterior ya haya generado semanas que lleguen mas alla; en ese
	 * caso arranca donde ese termino. Asi dos anos seguidos nunca se pelean un
	 * lunes ni dejan una semana sin dueno.
	 *
	 * HASTA DONDE VA: mientras la semana tenga cuatro o mas dias dentro del ano.
	 * La primera que tenga tres o menos ya es del ano siguiente.
	 */
	@SuppressWarnings("unchecked")
	public String generarCalendario(final int anio) {
		final JSONObject raiz = new JSONObject();
		final ArrayList<GerenciaSemanaDAO.Semana> semanas = new ArrayList<GerenciaSemanaDAO.Semana>();

		final Calendar cursor = Calendar.getInstance();
		cursor.clear();
		cursor.set(anio, Calendar.JANUARY, 1);
		retrocederALunes(cursor);

		//El lunes de la semana del 1 de enero NO siempre sirve de arranque.
		//Cuando el 1 de enero cae viernes, sabado o domingo, esa semana tiene
		//cuatro o mas dias en DICIEMBRE del ano anterior, o sea que es del ano
		//pasado. Empezar ahi hacia que el ciclo de abajo -que solo acepta
		//semanas con cuatro o mas dias dentro del ano- no entrara ni una vez y
		//el ano saliera con CERO semanas: pasaba con 2027 -1 de enero viernes-
		//y con 2028 -sabado-.
		if (diasDentroDelAnio(cursor, anio) < 4) {
			cursor.add(Calendar.DAY_OF_YEAR, 7);
		}

		//Si el ano pasado se estiro mas alla de ese lunes, se arranca donde
		//termino. Las dos fechas son lunes, asi que comparar textos ISO alcanza.
		final String cierreAnterior = GerenciaSemanaDAO.ultimoDomingoDe(anio - 1);
		if (cierreAnterior.length() > 0) {
			final Calendar siguiente = desdeIso(cierreAnterior);
			if (siguiente != null) {
				siguiente.add(Calendar.DAY_OF_YEAR, 1);
				if (aIso(siguiente).compareTo(aIso(cursor)) > 0) {
					cursor.setTime(siguiente.getTime());
				}
			}
		}

		int numero = 1;
		while (diasDentroDelAnio(cursor, anio) >= 4) {
			final GerenciaSemanaDAO.Semana s = new GerenciaSemanaDAO.Semana();
			s.anio = anio;
			s.numero = numero;
			s.fechaInicio = aIso(cursor);
			s.mes = mesConMasDias(cursor);

			final Calendar fin = (Calendar) cursor.clone();
			fin.add(Calendar.DAY_OF_YEAR, 6);
			s.fechaFin = aIso(fin);

			semanas.add(s);
			numero++;
			cursor.add(Calendar.DAY_OF_YEAR, 7);

			//Ningun ano tiene mas de 53 semanas. Si el contador se pasa de ahi
			//es que algo esta mal y es mejor parar que llenar la pantalla.
			if (numero > 53) {
				break;
			}
		}

		raiz.put("anio", Integer.valueOf(anio));
		raiz.put("estado", GerenciaSemanaDAO.estadoAnio(anio));
		raiz.put("semanas", aJsonSemanas(semanas));
		raiz.put("meses", resumenPorMes(semanas));
		raiz.put("generado", Boolean.TRUE);
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	/**
	 * Guarda el calendario que viene de la pantalla, despues de revisarlo.
	 *
	 * Las validaciones no son adorno: un calendario con un hueco hace que una
	 * semana de venta no entre en ningun mes y el ano no cuadre con la suma de
	 * sus meses, sin que nadie se entere hasta que alguien sume a mano.
	 *
	 * El formato que llega es una linea por semana: numero;inicio;fin;mes
	 */
	@SuppressWarnings("unchecked")
	public String guardarCalendario(final int anio, final String datos, final String usuario) {
		final JSONObject raiz = new JSONObject();

		if ("CERRADO".equals(GerenciaSemanaDAO.estadoAnio(anio))) {
			raiz.put("respuesta", "ANIOCERRADO");
			raiz.put("detalle", "El ano " + anio + " esta cerrado. Hay que reabrirlo para cambiarlo.");
			return (raiz.toJSONString());
		}

		final ArrayList<GerenciaSemanaDAO.Semana> semanas = leerSemanas(anio, datos);
		final String problema = revisar(anio, semanas);
		if (problema.length() > 0) {
			raiz.put("respuesta", "INVALIDO");
			raiz.put("detalle", problema);
			return (raiz.toJSONString());
		}

		final String error = GerenciaSemanaDAO.guardarAnio(anio, semanas, usuario);
		if (error.length() > 0) {
			raiz.put("respuesta", "NOK");
			raiz.put("detalle", error);
			return (raiz.toJSONString());
		}

		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "CALENDARIO",
				"Guardo el calendario de " + anio + " con " + semanas.size() + " semanas");

		raiz.put("respuesta", "OK");
		raiz.put("semanas", Integer.valueOf(semanas.size()));
		return (raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public String cambiarEstadoAnio(final int anio, final String estado, final String usuario) {
		final JSONObject raiz = new JSONObject();
		if (!"ABIERTO".equals(estado) && !"CERRADO".equals(estado)) {
			raiz.put("respuesta", "INVALIDO");
			raiz.put("detalle", "El estado solo puede ser ABIERTO o CERRADO");
			return (raiz.toJSONString());
		}
		if (GerenciaSemanaDAO.obtenerSemanas(anio).isEmpty()) {
			raiz.put("respuesta", "INVALIDO");
			raiz.put("detalle", "No se puede cerrar un ano que todavia no tiene calendario");
			return (raiz.toJSONString());
		}
		final boolean listo = GerenciaSemanaDAO.cambiarEstadoAnio(anio, estado, usuario);
		if (listo) {
			capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "CALENDARIO",
					"Dejo el ano " + anio + " en estado " + estado);
		}
		raiz.put("respuesta", listo ? "OK" : "NOK");
		raiz.put("estado", estado);
		return (raiz.toJSONString());
	}

	/**
	 * Revisa el calendario completo. Devuelve vacio si esta bien.
	 *
	 * Se revisa aca y no solo en la pantalla porque el servlet se puede llamar
	 * directo por URL, sin pasar por el javascript.
	 */
	private String revisar(final int anio, final ArrayList<GerenciaSemanaDAO.Semana> semanas) {
		if (semanas.isEmpty()) {
			return ("El calendario llego vacio");
		}
		if (semanas.size() < 52 || semanas.size() > 53) {
			return ("Un ano tiene 52 o 53 semanas, y este trae " + semanas.size());
		}

		String anterior = "";
		for (int i = 0; i < semanas.size(); i++) {
			final GerenciaSemanaDAO.Semana s = semanas.get(i);

			if (s.numero != (i + 1)) {
				return ("Las semanas tienen que ir numeradas de 1 en adelante sin saltos. "
						+ "La numero " + (i + 1) + " llego como " + s.numero);
			}
			if (s.mes < 1 || s.mes > 12) {
				return ("La semana " + s.numero + " quedo con un mes que no existe");
			}

			final Calendar inicio = desdeIso(s.fechaInicio);
			final Calendar fin = desdeIso(s.fechaFin);
			if (inicio == null || fin == null) {
				return ("La semana " + s.numero + " tiene fechas ilegibles");
			}
			if (inicio.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				return ("La semana " + s.numero + " no empieza un lunes");
			}
			final Calendar esperado = (Calendar) inicio.clone();
			esperado.add(Calendar.DAY_OF_YEAR, 6);
			if (!aIso(esperado).equals(s.fechaFin)) {
				return ("La semana " + s.numero + " no termina el domingo siguiente");
			}

			//El hueco y el traslape son el mismo chequeo: cada semana tiene que
			//empezar exactamente el dia despues del domingo de la anterior.
			if (anterior.length() > 0) {
				final Calendar debia = desdeIso(anterior);
				debia.add(Calendar.DAY_OF_YEAR, 1);
				if (!aIso(debia).equals(s.fechaInicio)) {
					return ("Entre la semana " + (s.numero - 1) + " y la " + s.numero
							+ " el calendario queda " + (aIso(debia).compareTo(s.fechaInicio) < 0
									? "con dias sin semana" : "con dias repetidos"));
				}
			}
			anterior = s.fechaFin;
		}

		//Doce meses, todos con semanas. Un mes vacio o con dos semanas quiere
		//decir que alguien movio de mas al editar.
		final int[] porMes = new int[13];
		for (int i = 0; i < semanas.size(); i++) {
			porMes[semanas.get(i).mes]++;
		}
		for (int m = 1; m <= 12; m++) {
			if (porMes[m] < SEMANAS_MINIMAS_POR_MES) {
				return (MESES[m] + " de " + anio + " quedo con " + porMes[m]
						+ " semana(s). Cada mes necesita al menos " + SEMANAS_MINIMAS_POR_MES + ".");
			}
		}
		return ("");
	}

	/** numero;inicio;fin;mes por linea. */
	private ArrayList<GerenciaSemanaDAO.Semana> leerSemanas(final int anio, final String datos) {
		final ArrayList<GerenciaSemanaDAO.Semana> semanas = new ArrayList<GerenciaSemanaDAO.Semana>();
		if (datos == null || datos.trim().length() == 0) {
			return (semanas);
		}
		final String[] lineas = datos.split("\n");
		for (int i = 0; i < lineas.length; i++) {
			final String linea = lineas[i].trim();
			if (linea.length() == 0) {
				continue;
			}
			final String[] campos = linea.split(";");
			if (campos.length < 4) {
				continue;
			}
			try {
				final GerenciaSemanaDAO.Semana s = new GerenciaSemanaDAO.Semana();
				s.anio = anio;
				s.numero = Integer.parseInt(campos[0].trim());
				s.fechaInicio = campos[1].trim();
				s.fechaFin = campos[2].trim();
				s.mes = Integer.parseInt(campos[3].trim());
				semanas.add(s);
			} catch (final Exception e) {
				//Una linea ilegible no se convierte en una semana en cero: se
				//descarta, y la cuenta de 52 o 53 la va a delatar.
				System.out.println("GerenciaCtrl.leerSemanas: linea ilegible '" + linea + "'");
			}
		}
		return (semanas);
	}

	// =======================================================================
	// GASTOS
	// =======================================================================

	/**
	 * Todo lo que la pantalla de gastos necesita para un mes.
	 *
	 * Va en una sola respuesta -tiendas, conceptos, fijos y servicios- porque
	 * son cuatro consultas al mismo servidor con el mismo filtro. Separarlas
	 * obligaria a la pantalla a coordinar respuestas que pueden llegar en
	 * desorden y a pintar una matriz a medio llenar.
	 *
	 * Los gastos fijos se piden vigentes al ULTIMO dia del mes que se consulta,
	 * no a hoy: mirar julio en octubre tiene que mostrar el arriendo de julio.
	 */
	@SuppressWarnings("unchecked")
	public String consultarGastos(final int anio, final int mes) {
		final JSONObject raiz = new JSONObject();
		final String ultimoDia = ultimoDiaDelMes(anio, mes);

		raiz.put("anio", Integer.valueOf(anio));
		raiz.put("mes", Integer.valueOf(mes));
		raiz.put("nombremes", MESES[mes]);
		raiz.put("vigenteal", ultimoDia);
		raiz.put("tiendas", aJsonTiendas(GerenciaGastoDAO.obtenerTiendas()));
		raiz.put("conceptos", aJsonConceptos(GerenciaGastoDAO.obtenerConceptos()));
		raiz.put("fijos", aJsonFijos(GerenciaGastoDAO.obtenerFijosVigentes(ultimoDia, 0)));
		raiz.put("servicios", aJsonServicios(anio, mes));
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	/**
	 * Los servicios del mes, completando con el estimado lo que no tenga factura.
	 *
	 * El estimado NO se guarda aca. Se calcula al mostrar, y solo se persiste
	 * cuando alguien lo acepta en la pantalla. Guardarlo solo haria que el
	 * proximo mes se estimara sobre un estimado, y al tercero el numero ya no
	 * tendria relacion con lo que se paga.
	 */
	@SuppressWarnings("unchecked")
	private JSONArray aJsonServicios(final int anio, final int mes) {
		final JSONArray lista = new JSONArray();
		final ArrayList<GerenciaGastoDAO.Servicio> cargados =
				GerenciaGastoDAO.obtenerServicios(anio, mes);
		final ArrayList<GerenciaGastoDAO.Tienda> tiendas = GerenciaGastoDAO.obtenerTiendas();
		final ArrayList<GerenciaGastoDAO.Concepto> conceptos = GerenciaGastoDAO.obtenerConceptos();

		for (int t = 0; t < tiendas.size(); t++) {
			final GerenciaGastoDAO.Tienda tienda = tiendas.get(t);
			for (int c = 0; c < conceptos.size(); c++) {
				final GerenciaGastoDAO.Concepto concepto = conceptos.get(c);
				if (!"SERVICIO".equals(concepto.tipo)) {
					continue;
				}

				final GerenciaGastoDAO.Servicio cargado =
						buscar(cargados, tienda.idTienda, concepto.idConcepto);

				final JSONObject fila = new JSONObject();
				fila.put("idtienda", Integer.valueOf(tienda.idTienda));
				fila.put("tienda", tienda.nombre);
				fila.put("idconcepto", Integer.valueOf(concepto.idConcepto));
				fila.put("concepto", concepto.nombre);

				if (cargado != null) {
					fila.put("valor", Double.valueOf(cargado.valor));
					fila.put("origen", cargado.origen);
					fila.put("meses", Integer.valueOf(cargado.mesesPromediados));
					fila.put("usuario", cargado.usuario);
				} else {
					final GerenciaGastoDAO.Estimacion e = GerenciaGastoDAO.estimarServicio(
							tienda.idTienda, concepto.idConcepto, anio, mes);
					if (e == null) {
						//Sin historia no se inventa un numero. Un cero se suma
						//al total y desaparece de la vista; un vacio se ve.
						fila.put("valor", null);
						fila.put("origen", "SINBASE");
						fila.put("meses", Integer.valueOf(0));
					} else {
						fila.put("valor", Double.valueOf(e.valor));
						fila.put("origen", "ESTIMADO");
						fila.put("meses", Integer.valueOf(e.meses));
					}
					fila.put("usuario", "");
				}
				fila.put("guardado", Boolean.valueOf(cargado != null));
				lista.add(fila);
			}
		}
		return (lista);
	}

	private GerenciaGastoDAO.Servicio buscar(final ArrayList<GerenciaGastoDAO.Servicio> lista,
			final int idTienda, final int idConcepto) {
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaGastoDAO.Servicio s = lista.get(i);
			if (s.idTienda == idTienda && s.idConcepto == idConcepto) {
				return (s);
			}
		}
		return (null);
	}

	@SuppressWarnings("unchecked")
	public String guardarGastoFijo(final int idTienda, final int idConcepto, final double valor,
			final String desde, final String observacion, final String usuario) {
		final JSONObject raiz = new JSONObject();
		final GerenciaGastoDAO.GastoFijo g = new GerenciaGastoDAO.GastoFijo();
		g.idTienda = idTienda;
		g.idConcepto = idConcepto;
		g.valorMensual = valor;
		g.vigenciaDesde = desde;
		g.observacion = observacion;

		final String error = GerenciaGastoDAO.guardarFijo(g, usuario);
		if (error.length() > 0) {
			raiz.put("respuesta", "NOK");
			raiz.put("detalle", error);
			return (raiz.toJSONString());
		}
		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "GASTOFIJO",
				"Tienda " + idTienda + " concepto " + idConcepto + " a " + valor + " desde " + desde);
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public String consultarHistoriaFijo(final int idTienda, final int idConcepto) {
		final JSONObject raiz = new JSONObject();
		raiz.put("historia", aJsonFijos(GerenciaGastoDAO.obtenerHistoriaFijo(idTienda, idConcepto)));
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public String guardarServicio(final int idTienda, final int idConcepto, final int anio,
			final int mes, final double valor, final String origen, final int meses,
			final String usuario) {
		final JSONObject raiz = new JSONObject();
		final GerenciaGastoDAO.Servicio s = new GerenciaGastoDAO.Servicio();
		s.idTienda = idTienda;
		s.idConcepto = idConcepto;
		s.anio = anio;
		s.mes = mes;
		s.valor = valor;
		s.origen = "ESTIMADO".equals(origen) ? "ESTIMADO" : "REAL";
		s.mesesPromediados = "ESTIMADO".equals(s.origen) ? meses : 0;

		final String error = GerenciaGastoDAO.guardarServicio(s, usuario);
		if (error.length() > 0) {
			raiz.put("respuesta", "NOK");
			raiz.put("detalle", error);
			return (raiz.toJSONString());
		}
		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "SERVICIO",
				"Tienda " + idTienda + " concepto " + idConcepto + " " + anio + "-" + mes
				+ " a " + valor + " (" + s.origen + ")");
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	// =======================================================================
	// ESTRUCTURA Y NOMINA
	// =======================================================================

	@SuppressWarnings("unchecked")
	public String consultarEstructura(final int anio, final int mes) {
		final JSONObject raiz = new JSONObject();
		raiz.put("anio", Integer.valueOf(anio));
		raiz.put("mes", Integer.valueOf(mes));
		raiz.put("nombremes", MESES[mes]);
		raiz.put("tiendas", aJsonTiendas(GerenciaGastoDAO.obtenerTiendas()));

		final JSONArray pools = new JSONArray();
		final ArrayList<GerenciaGastoDAO.Pool> guardados = GerenciaGastoDAO.obtenerPools(anio, mes);
		final String[] tipos = { "ADMINISTRATIVA", "LOGISTICA", "CONTACT", "PUBLICIDAD" };
		for (int i = 0; i < tipos.length; i++) {
			final JSONObject p = new JSONObject();
			p.put("tipo", tipos[i]);
			p.put("valor", Double.valueOf(valorPool(guardados, tipos[i])));
			pools.add(p);
		}
		raiz.put("pools", pools);

		final JSONArray nominas = new JSONArray();
		final ArrayList<GerenciaGastoDAO.Nomina> lista = GerenciaGastoDAO.obtenerNominas(anio, mes);
		for (int i = 0; i < lista.size(); i++) {
			final GerenciaGastoDAO.Nomina n = lista.get(i);
			final JSONObject o = new JSONObject();
			o.put("idtienda", Integer.valueOf(n.idTienda));
			o.put("empleados", Integer.valueOf(n.empleados));
			o.put("basico", Double.valueOf(n.sueldoBasico));
			o.put("variable", Double.valueOf(n.sueldoVariable));
			o.put("seguridad", Double.valueOf(n.seguridadSocial));
			o.put("liquidacion", Double.valueOf(n.liquidacion));
			o.put("total", Double.valueOf(n.sueldoVariable + n.seguridadSocial + n.liquidacion));
			o.put("usuario", n.usuario);
			nominas.add(o);
		}
		raiz.put("nominas", nominas);
		raiz.put("reparto", repartoDelMes(anio, mes));
		raiz.put("semanas", Integer.valueOf(GerenciaGastoDAO.semanasDelMes(anio, mes)));
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	/**
	 * Como quedaria repartida la estructura entre las tiendas ese mes.
	 *
	 * El reparto es por participacion en la venta: se suma la venta de todas las
	 * tiendas del mes y a cada una le toca su porcentaje. Se calcula aqui, al
	 * mostrar, y NO se guarda: guardarlo lo congelaria, que es exactamente lo
	 * que le pasa hoy al Excel, donde los pesos son puntos enteros que salieron
	 * de una venta vieja y ya no corresponden a lo que vende cada tienda.
	 *
	 * Si el mes no tiene semanas asignadas en el calendario, la lista sale vacia
	 * y la pantalla dice que falta el calendario. No se reparte parejo como plan
	 * B: un reparto que se ve bien y esta mal es peor que ninguno.
	 */
	@SuppressWarnings("unchecked")
	private JSONArray repartoDelMes(final int anio, final int mes) {
		final JSONArray lista = new JSONArray();
		final ArrayList<double[]> ventas = GerenciaGastoDAO.ventaPorTiendaDelMes(anio, mes);

		double total = 0;
		for (int i = 0; i < ventas.size(); i++) {
			total += ventas.get(i)[1];
		}
		if (total <= 0) {
			return (lista);
		}
		for (int i = 0; i < ventas.size(); i++) {
			final JSONObject o = new JSONObject();
			o.put("idtienda", Integer.valueOf((int) ventas.get(i)[0]));
			o.put("venta", Double.valueOf(ventas.get(i)[1]));
			o.put("participacion", Double.valueOf((ventas.get(i)[1] * 100) / total));
			lista.add(o);
		}
		return (lista);
	}

	private double valorPool(final ArrayList<GerenciaGastoDAO.Pool> pools, final String tipo) {
		for (int i = 0; i < pools.size(); i++) {
			if (tipo.equals(pools.get(i).tipo)) {
				return (pools.get(i).valor);
			}
		}
		return (0);
	}

	@SuppressWarnings("unchecked")
	public String guardarPool(final int anio, final int mes, final String tipo,
			final double valor, final String usuario) {
		final JSONObject raiz = new JSONObject();
		final GerenciaGastoDAO.Pool p = new GerenciaGastoDAO.Pool();
		p.anio = anio;
		p.mes = mes;
		p.tipo = tipo;
		p.valor = valor;
		final String error = GerenciaGastoDAO.guardarPool(p, usuario);
		if (error.length() > 0) {
			raiz.put("respuesta", "NOK");
			raiz.put("detalle", error);
			return (raiz.toJSONString());
		}
		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "ESTRUCTURA",
				tipo + " " + anio + "-" + mes + " a " + valor);
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public String guardarNomina(final GerenciaGastoDAO.Nomina nomina, final String usuario) {
		final JSONObject raiz = new JSONObject();
		final String error = GerenciaGastoDAO.guardarNomina(nomina, usuario);
		if (error.length() > 0) {
			raiz.put("respuesta", "NOK");
			raiz.put("detalle", error);
			return (raiz.toJSONString());
		}
		//La bitacora guarda la tienda y el total, no el detalle: es informacion
		//de nomina y no tiene por que quedar repetida en otra tabla mas.
		capaDAOINV.GerenciaAccesoDAO.registrar(usuario, "NOMINA",
				"Tienda " + nomina.idTienda + " " + nomina.anio + "-" + nomina.mes
				+ ", " + nomina.empleados + " empleados");
		raiz.put("respuesta", "OK");
		return (raiz.toJSONString());
	}

	// =======================================================================
	// AYUDAS
	// =======================================================================

	@SuppressWarnings("unchecked")
	private JSONArray aJsonSemanas(final ArrayList<GerenciaSemanaDAO.Semana> semanas) {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < semanas.size(); i++) {
			final GerenciaSemanaDAO.Semana s = semanas.get(i);
			final JSONObject o = new JSONObject();
			o.put("numero", Integer.valueOf(s.numero));
			o.put("inicio", s.fechaInicio);
			o.put("fin", s.fechaFin);
			o.put("mes", Integer.valueOf(s.mes));
			o.put("nombremes", MESES[s.mes]);
			o.put("propuesto", Integer.valueOf(mesConMasDias(desdeIso(s.fechaInicio))));
			lista.add(o);
		}
		return (lista);
	}

	@SuppressWarnings("unchecked")
	private JSONArray resumenPorMes(final ArrayList<GerenciaSemanaDAO.Semana> semanas) {
		final int[] porMes = new int[13];
		for (int i = 0; i < semanas.size(); i++) {
			final int m = semanas.get(i).mes;
			if (m >= 1 && m <= 12) {
				porMes[m]++;
			}
		}
		final JSONArray lista = new JSONArray();
		for (int m = 1; m <= 12; m++) {
			final JSONObject o = new JSONObject();
			o.put("mes", Integer.valueOf(m));
			o.put("nombre", MESES[m]);
			o.put("semanas", Integer.valueOf(porMes[m]));
			lista.add(o);
		}
		return (lista);
	}

	@SuppressWarnings("unchecked")
	private JSONArray aJsonAnios() {
		final JSONArray lista = new JSONArray();
		final ArrayList<int[]> anios = GerenciaSemanaDAO.aniosConCalendario();
		for (int i = 0; i < anios.size(); i++) {
			final JSONObject o = new JSONObject();
			o.put("anio", Integer.valueOf(anios.get(i)[0]));
			o.put("semanas", Integer.valueOf(anios.get(i)[1]));
			lista.add(o);
		}
		return (lista);
	}

	@SuppressWarnings("unchecked")
	private JSONArray aJsonTiendas(final ArrayList<GerenciaGastoDAO.Tienda> tiendas) {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < tiendas.size(); i++) {
			final JSONObject o = new JSONObject();
			o.put("idtienda", Integer.valueOf(tiendas.get(i).idTienda));
			o.put("nombre", tiendas.get(i).nombre);
			lista.add(o);
		}
		return (lista);
	}

	@SuppressWarnings("unchecked")
	private JSONArray aJsonConceptos(final ArrayList<GerenciaGastoDAO.Concepto> conceptos) {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < conceptos.size(); i++) {
			final GerenciaGastoDAO.Concepto c = conceptos.get(i);
			final JSONObject o = new JSONObject();
			o.put("idconcepto", Integer.valueOf(c.idConcepto));
			o.put("nombre", c.nombre);
			o.put("tipo", c.tipo);
			o.put("linea", c.linea);
			lista.add(o);
		}
		return (lista);
	}

	@SuppressWarnings("unchecked")
	private JSONArray aJsonFijos(final ArrayList<GerenciaGastoDAO.GastoFijo> gastos) {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < gastos.size(); i++) {
			final GerenciaGastoDAO.GastoFijo g = gastos.get(i);
			final JSONObject o = new JSONObject();
			o.put("idgasto", Integer.valueOf(g.idGasto));
			o.put("idtienda", Integer.valueOf(g.idTienda));
			o.put("idconcepto", Integer.valueOf(g.idConcepto));
			o.put("concepto", g.concepto);
			o.put("valor", Double.valueOf(g.valorMensual));
			o.put("desde", g.vigenciaDesde);
			o.put("hasta", g.vigenciaHasta);
			o.put("observacion", g.observacion);
			o.put("usuario", g.usuario);
			lista.add(o);
		}
		return (lista);
	}

	/** Cuantos de los siete dias de esa semana caen dentro del ano. */
	private int diasDentroDelAnio(final Calendar lunes, final int anio) {
		final Calendar dia = (Calendar) lunes.clone();
		int cuantos = 0;
		for (int i = 0; i < 7; i++) {
			if (dia.get(Calendar.YEAR) == anio) {
				cuantos++;
			}
			dia.add(Calendar.DAY_OF_YEAR, 1);
		}
		return (cuantos);
	}

	/**
	 * El mes donde caen la mayoria de los siete dias.
	 *
	 * Una semana toca a lo sumo dos meses, y siete dias se parten cuatro y tres:
	 * nunca hay empate, asi que no hace falta desempatar.
	 */
	private int mesConMasDias(final Calendar lunes) {
		if (lunes == null) {
			return (1);
		}
		final Calendar dia = (Calendar) lunes.clone();
		final int[] cuenta = new int[13];
		for (int i = 0; i < 7; i++) {
			cuenta[dia.get(Calendar.MONTH) + 1]++;
			dia.add(Calendar.DAY_OF_YEAR, 1);
		}
		int mejor = 1;
		for (int m = 1; m <= 12; m++) {
			if (cuenta[m] > cuenta[mejor]) {
				mejor = m;
			}
		}
		return (mejor);
	}

	private void retrocederALunes(final Calendar c) {
		while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			c.add(Calendar.DAY_OF_YEAR, -1);
		}
	}

	private String ultimoDiaDelMes(final int anio, final int mes) {
		final Calendar c = Calendar.getInstance();
		c.clear();
		c.set(anio, mes - 1, 1);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		return (aIso(c));
	}

	private String aIso(final Calendar c) {
		return (new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
	}

	private Calendar desdeIso(final String fecha) {
		if (fecha == null || !fecha.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
			return (null);
		}
		try {
			final SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
			formato.setLenient(false);
			final Date d = formato.parse(fecha);
			final Calendar c = Calendar.getInstance();
			c.clear();
			c.setTime(d);
			return (c);
		} catch (final Exception e) {
			return (null);
		}
	}
}
