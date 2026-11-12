package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import conexionINV.ConexionBaseDatos;

/**
 * El estimado de nomina a partir de la biometria y los parametros de ley.
 *
 * PARA QUE SIRVE Y PARA QUE NO
 *
 * Esto NO liquida nomina. La nomina se liquida en SIIGO y con eso se le paga a
 * la gente. Esto estima cuanto COSTO una semana, para que el tablero de
 * rentabilidad tenga un numero mientras llega el real, y para poder comparar
 * tiendas. El estimado sale marcado como ESTIMADO en todas partes y no debe
 * salir nunca a nada que pague.
 *
 * Y es necesario de forma permanente, no provisional: la API publica de Siigo
 * Nube no expone nomina -solo contabilidad y facturacion- asi que el dato real
 * va a llegar por lotes y con retraso, siempre.
 *
 * EL BASICO NO SALE DE LAS HORAS; LOS RECARGOS SI
 *
 * A un empleado con salario mensual se le paga el mes trabaje 40 o 44 horas.
 * Por eso el basico de la semana es salario * 7 / 30 y no horas * tarifa. Lo
 * que la biometria determina son los RECARGOS -nocturno, dominical, extras- y
 * en que tienda cayeron. Eso hace el estimado mucho mas robusto: el 70% del
 * costo es deterministico y solo el resto depende de las marcaciones.
 *
 * POR QUE LOS RECARGOS NO SON UN DETALLE
 *
 * Medido sobre la semana del 21 al 27 de septiembre de 2026: el 41,9% de las
 * horas de la compania caen en la franja nocturna -que desde la Ley 2466 de
 * 2025 arranca a las 19:00 y no a las 21:00- y el 16,6% son dominicales. Un
 * calculo de "horas por tarifa" sin recargos se queda corto alrededor de una
 * cuarta parte.
 *
 * SIMPLIFICACIONES, DICHAS DE FRENTE
 *
 * 1. Las horas extra se cuentan por SEMANA -lo que exceda la jornada legal- y
 *    no dia a dia. La ley las mira tambien por dia; una semana de 40 horas con
 *    un dia de 12 aqui no genera extra y en SIIGO si. Es justo el tipo de sesgo
 *    que la calibracion contra el real termina corrigiendo.
 * 2. El recargo de extra se reparte entre diurno y nocturno en la misma
 *    proporcion que tuvo la semana, no identificando cual hora concreta fue la
 *    que excedio.
 * 3. Las ausencias no se descuentan: quien no marco queda con su basico y sin
 *    recargos, y con un aviso. Descontarselo seria peor, porque no sabemos si
 *    fue vacaciones, incapacidad o un huellero dañado.
 */
public class GerenciaNominaCalculoDAO {

	/** Igual que en el reparto: mas de esto no es un turno, es una marca olvidada. */
	private static final int MAX_MINUTOS_TURNO = 16 * 60;

	/**
	 * La hora a la que se asume que salio quien no marco salida.
	 *
	 * Son las horas en que de verdad cierra el punto de venta, medidas sobre
	 * las marcaciones: de domingo a jueves el pico de salidas es a las 22:00 y
	 * los viernes y sabados a las 23:00.
	 */
	private static final int HORA_CIERRE_SEMANA = 22;
	private static final int HORA_CIERRE_FIN_SEMANA = 23;

	/** Tope de un turno estimado. Un olvido no puede volverse una jornada absurda. */
	private static final int MAX_HORAS_ESTIMADO = 10;

	/** Dias de la semana sobre dias del mes, para llevar lo mensual a semanal. */
	private static final double SEMANA_DEL_MES = 7.0 / 30.0;

	// =======================================================================
	// Modelo
	// =======================================================================

	public static class Estimado {
		public int idEmpleado;
		public String nombre = "";
		public String cargo = "";
		public int idTiendaPrincipal;
		public double salarioBase;
		public double horasOrdinarias;
		public double horasNocturnas;
		public double horasDominicales;
		public double horasDomNoct;
		public double horasFestivas;
		public double horasFestNoct;
		public double horasExtra;
		public double horasExtraFestiva;
		public double horasTotales;
		public int turnos;
		public double sueldoBasico;
		public double sueldoVariable;
		public double auxilioTransporte;
		public double seguridadSocial;
		public double liquidacion;
		public double costoTotal;
		public double factorCorreccion;
		public double costoCorregido;
		public String aviso = "";
		public boolean entraTienda = true;
		/** El cargo, para sacar el ARL sin volver a la base. */
		public int idTipoEmpleado;
		/** Que origen tiene ya cargado esa semana, si tiene alguno. */
		public String origenCargado = "";
		public double recargoNocturno;
		public double recargoDominical;
		public double recargoFestivo;
		public double recargoExtra;
		public double recargoExtraFestiva;
		public boolean esSupernumerario = false;
		public String area = "";
		public boolean esSena = false;
		public boolean esSenaElectiva = false;
		public boolean esInactivo = false;
		public boolean esPosibleDespido = false;
		public String novedadTipo = "";
		public String novedadAccion = "";
		public String novedadObs = "";
		public double novedadDias = 7;
		public boolean novedadPermanente = false;
		// Valores reales liquidados (cargados de SIIGO o manual)
		public double realBasico;
		public double realVariable;
		public double realSegSocial;
		public double realLiquidacion;
		public double realCostoTotal;
	}

	public static class EmpleadoInfo {
		public int id;
		public String cedula = "";
		public String nombreLargo = "";
		public double salario;
		public boolean activo;
	}

	public static class PeriodoNomina {
		public int idPeriodo;
		public String fechaInferior = "";
		public String fechaSuperior = "";
		public ArrayList<String> domingos = new ArrayList<String>();
	}

	public static class NovedadReg {
		public int idEmpleado;
		public String tipoNovedad = "";
		public String accion = "";
		public double dias = 7;
		public boolean permanente = false;
		public String observacion = "";
	}

	public static class Desviacion {
		public int idTienda;
		public String tienda = "";
		public int muestras;
		public double promedio;
		public double dispersion;
		public boolean seAplica;
		public String porQueNo = "";
	}

	public static class FilaConciliacion {
		public int idEmpleado;
		public String cedula = "";
		public String nombre = "";
		public String cargo = "";
		public String tienda = "";
		public int idTienda = 0;

		public double horasBio = 0;
		public double basicoBio = 0;
		public double variableBio = 0;
		public double auxilioBio = 0;
		public double segSocialBio = 0;
		public double liquidacionBio = 0;
		public double costoBio = 0;
		public boolean tieneBiometria = false;

		public int idCargue = 0;
		public double horasReal = 0;
		public double totalPagadoReal = 0;
		public String origenReal = "";
		public String fechaCargue = "";
		public String observacion = "";
		public String estado = "PENDIENTE";
		public boolean tieneReal = false;

		public double diffHoras = 0;
		public double diffPesos = 0;
		public double diffPct = 0;
		public String semaforo = "VERDE";
		public String diagnostico = "";
		public boolean esSena = false;
	}

	public static class ResumenConciliacion {
		public String modo = "QUINCENA";
		public int idPeriodo = 0;
		public String periodoTexto = "";
		public String fechaDesde = "";
		public String fechaHasta = "";
		public ArrayList<String> domingos = new ArrayList<String>();

		public double totalRealPagado = 0;
		public double totalBiometria = 0;
		public double diffPesos = 0;
		public double diffPct = 0;

		public double horasTotalesReal = 0;
		public double horasTotalesBio = 0;
		public double diffHoras = 0;

		public int totalEmpleados = 0;
		public int cantCuadrados = 0;
		public int cantDiferencias = 0;
		public int cantSoloReal = 0;
		public int cantSoloBio = 0;

		public ArrayList<FilaConciliacion> filas = new ArrayList<FilaConciliacion>();
	}

	/** Un turno cerrado: cuando entro, cuando salio, y donde. */
	private static class Turno {
		Timestamp entra;
		Timestamp sale;
		int idTienda;
		/** true cuando la salida se asumio porque no se marco. */
		boolean estimado;
	}

	/** Las horas de una semana, ya clasificadas. */
	private static class Horas {
		double ordinarias;
		double nocturnas;
		double dominicales;
		double domNoct;
		double festivas;
		double festNoct;
		int turnos;
		/** Cuantos de esos turnos llevan la salida asumida. */
		int turnosEstimados;
		final Map<Integer, Double> porTienda = new LinkedHashMap<Integer, Double>();

		double total() {
			return (ordinarias + nocturnas + dominicales + domNoct + festivas + festNoct);
		}
	}

	// =======================================================================
	// Los parametros
	// =======================================================================

	/**
	 * Los parametros vigentes en una fecha.
	 *
	 * Se leen UNA vez por calculo y se pasan a todo el recorrido. Leerlos por
	 * empleado serian ciento cuarenta y ocho consultas identicas, y peor: si
	 * alguien cambiara un parametro a mitad del calculo, media nomina quedaria
	 * con la ley vieja y media con la nueva.
	 */
	public static class Parametros {
		private final Map<String, Double> valores = new HashMap<String, Double>();
		public String error = "";

		public double de(final String codigo, final double siNo) {
			final Double v = valores.get(codigo);
			return (v == null ? siNo : v.doubleValue());
		}

		public boolean tiene(final String codigo) {
			return (valores.containsKey(codigo));
		}
	}

	public static Parametros parametrosAl(final Connection cn, final String fecha) {
		final Parametros p = new Parametros();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT codigo, valor FROM gerencia_nomina_parametro"
					+ " WHERE vigencia_desde <= ?"
					+ "   AND (vigencia_hasta IS NULL OR vigencia_hasta >= ?)"
					//Si por un error quedaran dos vigencias solapadas, gana la
					//que empezo despues. Es lo que alguien querria decir al
					//abrir una nueva sin cerrar la anterior.
					+ " ORDER BY codigo, vigencia_desde ASC");
			ps.setString(1, fecha);
			ps.setString(2, fecha);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				p.valores.put(rs.getString("codigo"), Double.valueOf(rs.getDouble("valor")));
			}
			rs.close();
			ps.close();
			if (!p.tiene("SMLV") || !p.tiene("JORNADA_SEMANAL")) {
				p.error = "No hay parametros de nomina vigentes al " + fecha
						+ ". Cargue el ano en la pantalla de parametros antes de calcular.";
			}
		} catch (final Exception e) {
			p.error = "No se pudieron leer los parametros";
			System.out.println("GerenciaNominaCalculoDAO.parametrosAl: " + e.toString());
		}
		return (p);
	}

	// =======================================================================
	// El calculo
	// =======================================================================

	/**
	 * Calcula el estimado de toda la semana y lo guarda.
	 *
	 * Reprocesable: borra lo que hubiera de esa semana antes de escribir, para
	 * que correrlo de nuevo con parametros corregidos de el resultado correcto
	 * y no uno sumado sobre el viejo.
	 *
	 * @param semana el domingo de cierre
	 * @return cuantos empleados quedaron estimados, o -1 si no se pudo
	 */
	public static int calcular(final String semana, final String usuario) {
		final String[] rango = rangoDeLaSemana(semana);
		if (rango == null) {
			return (-1);
		}
		final ConexionBaseDatos conL = new ConexionBaseDatos();
		final ConexionBaseDatos conG = new ConexionBaseDatos();
		final Connection cn = conL.obtenerConexionBDPrincipalLocal();
		final Connection cnG = conG.obtenerConexionBDGeneral();
		if (cn == null || cnG == null) {
			cerrar(cn);
			cerrar(cnG);
			return (-1);
		}
		try {
			final Parametros p = parametrosAl(cn, semana);
			if (p.error.length() > 0) {
				System.out.println("GerenciaNominaCalculoDAO.calcular: " + p.error);
				return (-1);
			}
			final HashSet<String> festivos = festivosDe(cn, rango[0], rango[1]);
			final Map<Integer, double[]> cargos = cargosConfigurados(cn);
			final Map<Integer, String> areas = areasCargo(cn);
			final Map<Integer, String> excepciones = excepcionesPersona(cn);
			final String[] rangoMes = rangoMesDeSemana(semana);
			final String desdeMes = rangoMes != null ? rangoMes[0] : (rango[0] + " 00:00:00");
			final String hastaMes = rangoMes != null ? rangoMes[1] : (rango[1] + " 23:59:59");
			final ArrayList<Estimado> gente = empleadosParaCalcular(cnG, cargos, desdeMes, hastaMes);

			//La correccion por desviacion historica, una vez por tienda.
			final Map<Integer, Desviacion> correcciones = desviacionesVigentes(cn, p, semana);

			final PreparedStatement borrar = cn.prepareStatement(
					"DELETE FROM gerencia_nomina_estimado WHERE semana = ?");
			borrar.setString(1, semana);
			borrar.executeUpdate();
			borrar.close();

			final PreparedStatement ins = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_estimado (idempleado, semana, salario_base,"
					+ " horas_ordinarias, horas_nocturnas, horas_dominicales, horas_dom_noct,"
					+ " horas_festivas, horas_fest_noct, horas_extra, horas_extra_festiva, horas_totales,"
					+ " recargo_nocturno, recargo_dominical, recargo_festivo, recargo_extra, recargo_extra_festiva,"
					+ " turnos, sueldo_basico, sueldo_variable,"
					+ " auxilio_transporte, seguridad_social, liquidacion, costo_total,"
					+ " factor_correccion, costo_corregido, aviso)"
					+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			final HashMap<Integer, NovedadReg> novedades = novedadesVigentes(cn, semana);

			int cuantos = 0;
			for (final Estimado e : gente) {
				final ArrayList<Turno> turnos = turnosDe(cnG, e.idEmpleado, rango[0], rango[1]);
				final boolean horasEnteras = esHorasEnteras(e.idEmpleado, e.idTipoEmpleado, areas, excepciones);
				final Horas h = clasificar(turnos, festivos, p, horasEnteras);
				calcularCosto(e, h, p, cargos, areas, excepciones);

				final NovedadReg nov = novedades.get(Integer.valueOf(e.idEmpleado));
				if (nov != null) {
					aplicarNovedad(e, nov);
				}

				e.idTiendaPrincipal = tiendaPrincipal(h);
				final Desviacion d = correcciones.get(Integer.valueOf(e.idTiendaPrincipal));
				if (d != null && d.seAplica) {
					e.factorCorreccion = d.promedio;
					e.costoCorregido = e.costoTotal * (1 + d.promedio / 100.0);
				} else {
					e.factorCorreccion = 0;
					e.costoCorregido = e.costoTotal;
				}

				int i = 1;
				ins.setInt(i++, e.idEmpleado);
				ins.setString(i++, semana);
				ins.setDouble(i++, e.salarioBase);
				ins.setDouble(i++, e.horasOrdinarias);
				ins.setDouble(i++, e.horasNocturnas);
				ins.setDouble(i++, e.horasDominicales);
				ins.setDouble(i++, e.horasDomNoct);
				ins.setDouble(i++, e.horasFestivas);
				ins.setDouble(i++, e.horasFestNoct);
				ins.setDouble(i++, e.horasExtra);
				ins.setDouble(i++, e.horasExtraFestiva);
				ins.setDouble(i++, e.horasTotales);
				ins.setDouble(i++, e.recargoNocturno);
				ins.setDouble(i++, e.recargoDominical);
				ins.setDouble(i++, e.recargoFestivo);
				ins.setDouble(i++, e.recargoExtra);
				ins.setDouble(i++, e.recargoExtraFestiva);
				ins.setInt(i++, e.turnos);
				ins.setDouble(i++, e.sueldoBasico);
				ins.setDouble(i++, e.sueldoVariable);
				ins.setDouble(i++, e.auxilioTransporte);
				ins.setDouble(i++, e.seguridadSocial);
				ins.setDouble(i++, e.liquidacion);
				ins.setDouble(i++, e.costoTotal);
				ins.setDouble(i++, e.factorCorreccion);
				ins.setDouble(i++, e.costoCorregido);
				ins.setString(i++, e.aviso);
				ins.addBatch();
				cuantos++;
			}
			ins.executeBatch();
			ins.close();
			return (cuantos);
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.calcular: " + e.toString());
			return (-1);
		} finally {
			cerrar(cn);
			cerrar(cnG);
		}
	}

	/**
	 * Clasifica las horas del turno partiendolo en bloques de una hora.
	 *
	 * Se parte por bloque y no por turno completo porque un turno de 16:00 a
	 * 23:00 tiene cuatro horas nocturnas y tres diurnas: mirar solo la hora de
	 * entrada diria que es un turno diurno entero, y se perderia el recargo de
	 * cuatro horas. Con la franja nocturna arrancando a las 19:00, eso le pasa
	 * a la mayoria de los turnos de la compania.
	 */
	/**
	 * Acumulado de minutos por dia de un empleado para la regla de aproximacion diaria.
	 */
	private static class AcumuladoDia {
		double minutosOrdinarios;
		double minutosNocturnos;
		double minutosDominicales;
		double minutosDomNoct;
		double minutosFestivos;
		double minutosFestNoct;
		final Map<Integer, Double> minutosPorTienda = new LinkedHashMap<Integer, Double>();

		double minutosTotales() {
			return (minutosOrdinarios + minutosNocturnos + minutosDominicales + minutosDomNoct
					+ minutosFestivos + minutosFestNoct);
		}
	}

	/**
	 * Reparte un total de horas enteras redondeadas entre varios componentes
	 * proporcionales usando el metodo de mayor residuo (Hamilton / Hare-Niemeyer).
	 * Garantiza que la suma de los enteros asignados sea exactamente igual a totalHoras.
	 */
	private static long[] repartirHorasEnteras(final double[] minutosComponentes, final long totalHoras) {
		final int n = minutosComponentes.length;
		final long[] resultado = new long[n];
		if (totalHoras <= 0) {
			return (resultado);
		}
		double totalMin = 0;
		for (int i = 0; i < n; i++) {
			if (minutosComponentes[i] > 0) {
				totalMin += minutosComponentes[i];
			}
		}
		if (totalMin <= 0) {
			return (resultado);
		}
		final double[] ideales = new double[n];
		final double[] residuos = new double[n];
		long sumaPisos = 0;
		for (int i = 0; i < n; i++) {
			if (minutosComponentes[i] > 0) {
				ideales[i] = totalHoras * (minutosComponentes[i] / totalMin);
				resultado[i] = (long) Math.floor(ideales[i]);
				residuos[i] = ideales[i] - resultado[i];
				sumaPisos += resultado[i];
			}
		}
		long faltante = totalHoras - sumaPisos;
		while (faltante > 0) {
			int mejorIdx = -1;
			double mejorRes = -1;
			for (int i = 0; i < n; i++) {
				if (residuos[i] > mejorRes) {
					mejorRes = residuos[i];
					mejorIdx = i;
				}
			}
			if (mejorIdx < 0) {
				break;
			}
			resultado[mejorIdx]++;
			residuos[mejorIdx] = -1;
			faltante--;
		}
		return (resultado);
	}

	private static Horas clasificar(final ArrayList<Turno> turnos, final HashSet<String> festivos,
			final Parametros p, final boolean horasEnteras) {
		final Horas h = new Horas();
		final int nocIni = (int) p.de("HORA_NOCTURNA_INICIO", 19);
		final int nocFin = (int) p.de("HORA_NOCTURNA_FIN", 6);

		final Map<String, AcumuladoDia> dias = new LinkedHashMap<String, AcumuladoDia>();

		for (final Turno t : turnos) {
			h.turnos++;
			if (t.estimado) {
				h.turnosEstimados++;
			}
			final Calendar calEntra = Calendar.getInstance();
			calEntra.setTime(t.entra);
			final String diaKey = fecha(calEntra);
			AcumuladoDia dia = dias.get(diaKey);
			if (dia == null) {
				dia = new AcumuladoDia();
				dias.put(diaKey, dia);
			}

			final Calendar cursor = Calendar.getInstance();
			cursor.setTime(t.entra);
			final long fin = t.sale.getTime();
			double minutosTurno = 0;

			while (cursor.getTimeInMillis() < fin) {
				//El bloque va hasta el cambio de hora en punto, o hasta la
				//salida si llega antes.
				final Calendar siguiente = (Calendar) cursor.clone();
				siguiente.set(Calendar.MINUTE, 0);
				siguiente.set(Calendar.SECOND, 0);
				siguiente.set(Calendar.MILLISECOND, 0);
				siguiente.add(Calendar.HOUR_OF_DAY, 1);
				final long hasta = Math.min(siguiente.getTimeInMillis(), fin);
				final double minutos = (hasta - cursor.getTimeInMillis()) / 60000.0;

				final int hora = cursor.get(Calendar.HOUR_OF_DAY);
				final boolean esNoche = hora >= nocIni || hora < nocFin;
				final boolean esFestivo = festivos.contains(fecha(cursor));
				final boolean esDomingo = !esFestivo && cursor.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;

				if (esFestivo && esNoche) {
					dia.minutosFestNoct += minutos;
				} else if (esFestivo) {
					dia.minutosFestivos += minutos;
				} else if (esDomingo && esNoche) {
					dia.minutosDomNoct += minutos;
				} else if (esDomingo) {
					dia.minutosDominicales += minutos;
				} else if (esNoche) {
					dia.minutosNocturnos += minutos;
				} else {
					dia.minutosOrdinarios += minutos;
				}
				minutosTurno += minutos;
				cursor.setTimeInMillis(hasta);
			}

			final Integer tienda = Integer.valueOf(t.idTienda);
			final Double previo = dia.minutosPorTienda.get(tienda);
			dia.minutosPorTienda.put(tienda,
					Double.valueOf((previo == null ? 0 : previo.doubleValue()) + minutosTurno));
		}

		if (horasEnteras) {
			// PUNTO DE VENTA: Regla de aproximacion diaria a enteros sin decimales (Math.round).
			// Ej: 3.2 horas -> 3 horas, 3.7 horas -> 4 horas.
			// El total semanal es la suma de los dias ya redondeados a enteros.
			for (final AcumuladoDia dia : dias.values()) {
				final double minutosTot = dia.minutosTotales();
				if (minutosTot <= 0) {
					continue;
				}
				final long horasDia = Math.round(minutosTot / 60.0);
				if (horasDia <= 0) {
					continue;
				}

				// Repartir horasDia entre componentes proporcionales
				final double[] compMin = new double[] {
						dia.minutosOrdinarios, dia.minutosNocturnos,
						dia.minutosDominicales, dia.minutosDomNoct,
						dia.minutosFestivos, dia.minutosFestNoct };
				final long[] compHoras = repartirHorasEnteras(compMin, horasDia);

				h.ordinarias += compHoras[0];
				h.nocturnas += compHoras[1];
				h.dominicales += compHoras[2];
				h.domNoct += compHoras[3];
				h.festivas += compHoras[4];
				h.festNoct += compHoras[5];

				// Repartir horasDia entre tiendas del dia
				final ArrayList<Integer> idsTienda = new ArrayList<Integer>(dia.minutosPorTienda.keySet());
				final double[] tiendasMin = new double[idsTienda.size()];
				for (int k = 0; k < idsTienda.size(); k++) {
					tiendasMin[k] = dia.minutosPorTienda.get(idsTienda.get(k)).doubleValue();
				}
				final long[] tiendasHoras = repartirHorasEnteras(tiendasMin, horasDia);
				for (int k = 0; k < idsTienda.size(); k++) {
					final Integer idT = idsTienda.get(k);
					final Double prevT = h.porTienda.get(idT);
					h.porTienda.put(idT,
							Double.valueOf((prevT == null ? 0 : prevT.doubleValue()) + tiendasHoras[k]));
				}
			}
		} else {
			// BODEGA Y ADMINISTRATIVOS: Conservan los decimales exactos.
			// Marcan varias entradas/salidas al dia (desayuno, almuerzo, pausas), por lo que
			// no se redondea a entero diario para no desfigurar la jornada.
			for (final AcumuladoDia dia : dias.values()) {
				h.ordinarias += dia.minutosOrdinarios / 60.0;
				h.nocturnas += dia.minutosNocturnos / 60.0;
				h.dominicales += dia.minutosDominicales / 60.0;
				h.domNoct += dia.minutosDomNoct / 60.0;
				h.festivas += dia.minutosFestivos / 60.0;
				h.festNoct += dia.minutosFestNoct / 60.0;
				for (final Map.Entry<Integer, Double> entry : dia.minutosPorTienda.entrySet()) {
					final Integer idT = entry.getKey();
					final Double prevT = h.porTienda.get(idT);
					h.porTienda.put(idT,
							Double.valueOf((prevT == null ? 0 : prevT.doubleValue()) + entry.getValue().doubleValue() / 60.0));
				}
			}
		}

		return (h);
	}

	/** De las horas clasificadas y el salario, al costo de la semana. */
	private static void calcularCosto(final Estimado e, final Horas h, final Parametros p,
			final Map<Integer, double[]> cargos, final Map<Integer, String> areas,
			final Map<Integer, String> excepciones) {
		e.horasOrdinarias = redondear(h.ordinarias);
		e.horasNocturnas = redondear(h.nocturnas);
		e.horasDominicales = redondear(h.dominicales);
		e.horasDomNoct = redondear(h.domNoct);
		e.horasFestivas = redondear(h.festivas);
		e.horasFestNoct = redondear(h.festNoct);
		e.horasTotales = redondear(h.total());
		e.turnos = h.turnos;

		final double jornada = p.de("JORNADA_SEMANAL", 42);
		final double horasMes = p.de("HORAS_MES_LIQUIDACION", 240);
		final double vh = horasMes > 0 ? e.salarioBase / horasMes : 0;

		// El basico es del mes, no de las horas: a quien tiene salario mensual se
		// le paga igual. Las horas solo mandan en los recargos.
		e.sueldoBasico = e.salarioBase * SEMANA_DEL_MES;

		String area = excepciones != null ? excepciones.get(Integer.valueOf(e.idEmpleado)) : null;
		if (area == null || area.isEmpty()) {
			area = areas != null ? areas.get(Integer.valueOf(e.idTipoEmpleado)) : "";
		}
		e.area = area != null ? area : "";

		final boolean esAdministrativo = "ADMINISTRATIVA".equalsIgnoreCase(e.area);
		final String cargoLower = e.cargo != null ? e.cargo.toLowerCase() : "";
		final boolean esAdminTienda = e.idTipoEmpleado == 2 || cargoLower.contains("administrador");
		final boolean esSupernumerario = e.esSupernumerario || cargoLower.contains("supernumerario");
		final boolean esLiderZona = e.idTipoEmpleado == 24 || e.idEmpleado == 21 || e.idEmpleado == 270;
		final boolean esLiderOConductor = cargoLower.contains("logistica") || cargoLower.contains("conductor")
				|| cargoLower.contains("supervisor") || cargoLower.contains("lider")
				|| cargoLower.contains("director");
		final boolean sinHorasExtras = esAdministrativo || esAdminTienda || esSupernumerario || esLiderZona || esLiderOConductor;

		double recargoNocturno = 0;
		double recargoDominical = 0;
		double recargoFestivo = 0;
		double horasExtraOrd = 0;
		double horasExtraFest = 0;
		double recargoExtraOrd = 0;
		double recargoExtraFest = 0;

		if (!esAdministrativo) {
			// Recargo nocturno: franja 19:00 a 06:00 (ordinarias, dominicales y festivas)
			recargoNocturno = (h.nocturnas + h.domNoct + h.festNoct) * vh * p.de("RECARGO_NOCTURNO", 35) / 100.0;
			// Recargo dominical: domingos no festivos
			recargoDominical = (h.dominicales + h.domNoct) * vh * p.de("RECARGO_DOMINICAL", 90) / 100.0;
			// Recargo festivo: festivos oficiales
			recargoFestivo = (h.festivas + h.festNoct) * vh * p.de("RECARGO_FESTIVO", p.de("RECARGO_DOMINICAL", 90)) / 100.0;

			// Horas extras desglosadas (Ordinaria vs Festiva/Dominical)
			if (!sinHorasExtras && h.total() > jornada && h.total() > 0) {
				final double totalExtra = h.total() - jornada;
				final double hOrdSem = h.ordinarias + h.nocturnas;
				final double hDomFes = h.dominicales + h.domNoct + h.festivas + h.festNoct;

				if (hOrdSem > jornada) {
					horasExtraOrd = hOrdSem - jornada;
					horasExtraFest = hDomFes;
				} else {
					horasExtraOrd = 0;
					horasExtraFest = totalExtra;
				}
				horasExtraOrd = Math.min(horasExtraOrd, totalExtra);
				horasExtraFest = Math.min(horasExtraFest, totalExtra - horasExtraOrd);

				if (horasExtraOrd > 0) {
					final double propNocOrd = hOrdSem > 0 ? (h.nocturnas / hOrdSem) : 0;
					final double tasaOrd = p.de("EXTRA_NOCTURNA", 75) * propNocOrd
							+ p.de("EXTRA_DIURNA", 25) * (1 - propNocOrd);
					recargoExtraOrd = horasExtraOrd * vh * tasaOrd / 100.0;
				}

				if (horasExtraFest > 0) {
					final double propNocFest = hDomFes > 0 ? ((h.domNoct + h.festNoct) / hDomFes) : 0;
					final double tasaFestDiurna = p.de("EXTRA_FESTIVA_DIURNA", p.de("RECARGO_DOMINICAL", 90) + 25);
					final double tasaFestNocturna = p.de("EXTRA_FESTIVA_NOCTURNA", p.de("RECARGO_DOMINICAL", 90) + 75);
					final double tasaFest = tasaFestNocturna * propNocFest + tasaFestDiurna * (1 - propNocFest);
					recargoExtraFest = horasExtraFest * vh * tasaFest / 100.0;
				}
			}
		}

		e.horasExtra = redondear(horasExtraOrd);
		e.horasExtraFestiva = redondear(horasExtraFest);
		e.recargoNocturno = redondear(recargoNocturno);
		e.recargoDominical = redondear(recargoDominical);
		e.recargoFestivo = redondear(recargoFestivo);
		e.recargoExtra = redondear(recargoExtraOrd);
		e.recargoExtraFestiva = redondear(recargoExtraFest);
		e.sueldoVariable = redondear(e.recargoNocturno + e.recargoDominical + e.recargoFestivo + e.recargoExtra + e.recargoExtraFestiva);

		//Auxilio de transporte: solo hasta el tope de salarios minimos, y no
		//entra a la base de seguridad social ni a la de vacaciones.
		final double tope = p.de("TOPE_AUX_SMLV", 2) * p.de("SMLV", 0);
		e.auxilioTransporte = (tope > 0 && e.salarioBase > 0 && e.salarioBase <= tope && !e.esSena && !e.esSenaElectiva)
				? redondear(p.de("AUX_TRANSPORTE", 0) * SEMANA_DEL_MES) : 0;

		final double baseSalarial = e.sueldoBasico + e.sueldoVariable;

		//Seguridad social y parafiscales del empleador.
		if (e.esSenaElectiva) {
			// Aprendiz SENA (Etapa Lectiva):
			// Apoyo sostenimiento 75% del salario base mensual prorrateado.
			// Seguridad social: 15% fija sobre el salario base mensual prorrateado.
			// Sin prestaciones sociales (cesantías, prima ni vacaciones = $0) ni auxilio de transporte.
			e.sueldoBasico = redondear((e.salarioBase * 0.75) * SEMANA_DEL_MES);
			e.auxilioTransporte = 0;
			e.seguridadSocial = redondear((e.salarioBase * SEMANA_DEL_MES) * 0.15);
			e.liquidacion = 0;
			e.costoTotal = redondear(e.sueldoBasico + e.sueldoVariable + e.seguridadSocial);
			e.aviso = "Aprendiz SENA (Etapa Lectiva): Apoyo sostenimiento 75%, SS 15%, sin liquidación ni auxilio.";
		} else if (e.esSena) {
			// Aprendiz SENA (Etapa Práctica):
			// Apoyo sostenimiento 100%, SS 15%, sin liquidación.
			e.sueldoBasico = redondear(e.sueldoBasico);
			e.auxilioTransporte = 0;
			e.seguridadSocial = redondear(baseSalarial * 0.15);
			e.liquidacion = 0;
			e.costoTotal = redondear(e.sueldoBasico + e.sueldoVariable + e.seguridadSocial);
			e.aviso = "Aprendiz SENA (Etapa Práctica): Apoyo sostenimiento 100%, SS 15%, sin liquidación.";
		} else {
			final double[] cargo = cargos.get(Integer.valueOf(e.idTipoEmpleado));
			final double arl = cargo != null ? cargo[0] : 0.522;
			double porcentaje = p.de("PENSION_EMPLEADOR", 12) + p.de("CAJA_COMPENSACION", 4) + arl;
			//La exoneracion del art. 114-1 quita salud, SENA e ICBF a quien gane
			//menos del tope. Son trece puntos y medio: si esta mal puesta, el
			//estimado se va en esa proporcion para TODA la compania.
			final boolean exonera = p.de("EXONERACION_114_1", 0) == 1
					&& e.salarioBase < p.de("TOPE_EXONERACION_SMLV", 10) * p.de("SMLV", 0);
			if (!exonera) {
				porcentaje += p.de("SALUD_EMPLEADOR", 8.5) + p.de("SENA", 2) + p.de("ICBF", 3);
			}
			e.seguridadSocial = redondear(baseSalarial * porcentaje / 100.0);

			//Provisiones. El auxilio de transporte SI cuenta para cesantias y prima,
			//y NO para vacaciones.
			final double baseConAuxilio = baseSalarial + e.auxilioTransporte;
			e.liquidacion = redondear(
					baseConAuxilio * (p.de("CESANTIAS", 8.33) + p.de("INTERES_CESANTIAS", 1)
							+ p.de("PRIMA", 8.33)) / 100.0
					+ baseSalarial * p.de("VACACIONES", 4.17) / 100.0);
			e.sueldoBasico = redondear(e.sueldoBasico);
			e.costoTotal = redondear(e.sueldoBasico + e.sueldoVariable + e.auxilioTransporte
					+ e.seguridadSocial + e.liquidacion);
		}

		//Los avisos y control de inactivos/despidos
		if (e.esInactivo) {
			if (h.turnos > 0) {
				e.esPosibleDespido = true;
				e.aviso = "Posible retiro / despido: empleado inactivo en sistema con " + e.turnos + " turno(s) (" + redondear(h.total()) + " h) en la semana.";
			} else {
				// Inactivo sin turnos en esta semana: ya retirado o aún no ingresaba, no genera costo en esta semana
				e.sueldoBasico = 0;
				e.sueldoVariable = 0;
				e.auxilioTransporte = 0;
				e.seguridadSocial = 0;
				e.liquidacion = 0;
				e.costoTotal = 0;
				e.costoCorregido = 0;
				e.aviso = "Retirado / Desvinculado: empleado inactivo sin marcaciones en la semana ($0).";
			}
		} else if (e.esSenaElectiva) {
			e.aviso = "Aprendiz SENA (Etapa Lectiva): Apoyo sostenimiento 75%, SS 15%, sin liquidación ni auxilio.";
		} else if (e.esSena) {
			if (h.turnos == 0) {
				e.aviso = "Aprendiz SENA (Etapa Práctica): Sin marcaciones, apoyo sostenimiento 100%, SS 15%.";
			} else {
				e.aviso = "Aprendiz SENA (Etapa Práctica): Apoyo sostenimiento 100%, SS 15%, sin liquidación.";
			}
		} else if (h.turnos == 0) {
			e.aviso = "Sin marcaciones en la semana: va el basico sin recargos. "
					+ "Revisar si fue vacaciones, incapacidad o falta de marcacion.";
		} else if (h.total() < jornada * 0.5) {
			e.aviso = "Solo " + redondear(h.total()) + " horas marcadas de " + (int) jornada
					+ " esperadas: los recargos van incompletos.";
		}
		//Una salida asumida se dice SIEMPRE, aunque la semana se vea completa.
		//Si no, nadie va a ir a corregir el huellero: el reporte se ve bien y
		//el turno inventado se paga como si lo hubieran marcado.
		if (h.turnosEstimados > 0) {
			final String cuantos = h.turnosEstimados == 1
					? "Un turno sin salida marcada: se asumio"
					: h.turnosEstimados + " turnos sin salida marcada: se asumio";
			e.aviso = (e.aviso.length() > 0 ? e.aviso + " " : "")
					+ cuantos + " la hora de cierre del punto de venta, con tope de "
					+ MAX_HORAS_ESTIMADO + " horas. Conviene corregirlo en el huellero.";
		}
	}

	// =======================================================================
	// La desviacion: estimado contra real
	// =======================================================================

	/**
	 * Registra la desviacion de una semana, comparando lo estimado con lo que
	 * se haya cargado como real.
	 *
	 * Solo toma empleados que tengan las DOS cifras. Y compara contra
	 * costo_total -el crudo- y nunca contra costo_corregido: si se midiera
	 * contra el corregido, la correccion se estaria calibrando contra si misma.
	 *
	 * @return cuantas desviaciones quedaron registradas
	 */
	public static int registrarDesviacion(final String semana) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (-1);
		}
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_desviacion"
					+ " (idempleado, semana, idtienda, estimado_crudo, real_cargado, desviacion_pct)"
					+ " SELECT e.idempleado, e.semana,"
					//La tienda donde mas horas marco esa semana. Sirve para
					//calibrar por tienda, que es donde esta la diferencia: el
					//porcentaje de horas nocturnas va de 47% a 53% segun cual.
					+ "        IFNULL((SELECT r.idtienda FROM gerencia_nomina_empleado_tienda_semana r"
					+ "                 WHERE r.idempleado = e.idempleado AND r.semana = e.semana"
					+ "                 ORDER BY r.minutos DESC LIMIT 1), 0),"
					+ "        e.costo_total,"
					+ "        (n.sueldo_basico + n.sueldo_variable + n.seguridad_social + n.liquidacion),"
					+ "        ((n.sueldo_basico + n.sueldo_variable + n.seguridad_social + n.liquidacion)"
					+ "          - e.costo_total) * 100 / e.costo_total"
					+ "   FROM gerencia_nomina_estimado e"
					+ "   JOIN gerencia_nomina_empleado_semana n"
					+ "     ON n.idempleado = e.idempleado AND n.semana = e.semana"
					//Solo contra cifras reales: comparar el estimado contra un
					//estimado que se cargo antes no mide nada.
					+ "    AND n.origen <> 'ESTIMADO'"
					+ "  WHERE e.semana = ? AND e.costo_total > 0"
					+ " ON DUPLICATE KEY UPDATE"
					+ "   estimado_crudo = e.costo_total,"
					+ "   real_cargado = (n.sueldo_basico + n.sueldo_variable + n.seguridad_social + n.liquidacion),"
					+ "   desviacion_pct = ((n.sueldo_basico + n.sueldo_variable + n.seguridad_social"
					+ "                      + n.liquidacion) - e.costo_total) * 100 / e.costo_total");
			ps.setString(1, semana);
			final int n = ps.executeUpdate();
			ps.close();
			return (n);
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.registrarDesviacion: " + e.toString());
			return (-1);
		} finally {
			cerrar(cn);
		}
	}

	/**
	 * La desviacion historica por tienda, y si alcanza para corregir.
	 *
	 * Tres condiciones para aplicarla, y las tres importan:
	 *
	 * 1. Suficientes semanas. Con dos no hay sesgo, hay ruido.
	 * 2. Dispersion baja. Si las desviaciones son +2, +3, +2, +3, el promedio
	 *    significa algo. Si son +2, -5, +8, -1, el promedio tambien da +1 y no
	 *    significa nada: aplicarlo EMPEORA el estimado. Por eso se mira la
	 *    desviacion estandar y no solo el promedio.
	 * 3. Por tienda, no una sola para la compania. El porcentaje de horas
	 *    nocturnas va de 47,3% en America a 53,2% en Manrique, y el dominical de
	 *    17,4% a 23,2%: un solo factor quedaria mal en las dos puntas.
	 */
	public static Map<Integer, Desviacion> desviacionesVigentes(final Connection cn,
			final Parametros p, final String semana) {
		final Map<Integer, Desviacion> mapa = new LinkedHashMap<Integer, Desviacion>();
		final int minimo = (int) p.de("CORREGIR_DESDE_SEMANAS", 6);
		final double dispersionMax = p.de("DISPERSION_MAXIMA", 5);
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT d.idtienda, COUNT(*) AS muestras, AVG(d.desviacion_pct) AS promedio,"
					+ " STDDEV_SAMP(d.desviacion_pct) AS dispersion"
					+ " FROM gerencia_nomina_desviacion d"
					//Solo semanas ANTERIORES a la que se esta calculando: usar la
					//propia semana seria corregir con el resultado que todavia no
					//se conoce.
					+ " WHERE d.semana < ?"
					+ "   AND d.semana >= DATE_SUB(?, INTERVAL ? WEEK)"
					+ " GROUP BY d.idtienda");
			ps.setString(1, semana);
			ps.setString(2, semana);
			//Se mira una ventana del doble del minimo: suficiente para juzgar la
			//dispersion, y no tanta como para arrastrar un sesgo de hace medio
			//ano que ya se corrigio.
			ps.setInt(3, Math.max(minimo * 2, 12));
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Desviacion d = new Desviacion();
				d.idTienda = rs.getInt("idtienda");
				d.muestras = rs.getInt("muestras");
				d.promedio = rs.getDouble("promedio");
				d.dispersion = rs.getDouble("dispersion");
				if (rs.wasNull()) {
					d.dispersion = 0;
				}
				if (d.muestras < minimo) {
					d.seAplica = false;
					d.porQueNo = "Solo " + d.muestras + " semanas de historia; se necesitan " + minimo + ".";
				} else if (d.dispersion > dispersionMax) {
					d.seAplica = false;
					d.porQueNo = "La desviacion varia " + redondear(d.dispersion)
							+ " puntos entre semanas: el promedio no es un sesgo, es ruido.";
				} else {
					d.seAplica = true;
				}
				mapa.put(Integer.valueOf(d.idTienda), d);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.desviacionesVigentes: " + e.toString());
		}
		return (mapa);
	}

	/** La misma consulta, abriendo conexion, para mostrarla en pantalla. */
	public static ArrayList<Desviacion> desviaciones(final String semana) {
		final ArrayList<Desviacion> lista = new ArrayList<Desviacion>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (lista);
		}
		try {
			final Parametros p = parametrosAl(cn, semana);
			final Map<Integer, Desviacion> mapa = desviacionesVigentes(cn, p, semana);
			final Map<Integer, String> tiendas = nombresDeTienda(cn);
			for (final Map.Entry<Integer, Desviacion> en : mapa.entrySet()) {
				final Desviacion d = en.getValue();
				final String nombre = tiendas.get(en.getKey());
				d.tienda = nombre != null ? nombre
						: (d.idTienda == 0 ? "Sin tienda asignada" : "Tienda " + d.idTienda);
				lista.add(d);
			}
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	// =======================================================================
	// Pasar el estimado a la nomina que lee el tablero
	// =======================================================================

	/**
	 * Copia el estimado a gerencia_nomina_empleado_semana con origen ESTIMADO,
	 * que es de donde lo toma el reparto por biometria y, por ahi, el tablero.
	 *
	 * NUNCA PISA UN REAL. Si para ese empleado y esa semana ya hay una fila con
	 * origen distinto de ESTIMADO, se deja como esta: el dato de SIIGO manda
	 * sobre el calculo, siempre, y una corrida distraida del estimador no puede
	 * borrar lo que ya se cargo.
	 *
	 * Se pasa el costo CORREGIDO, que es la mejor apuesta disponible; el crudo
	 * queda guardado aparte para seguir midiendo contra el.
	 *
	 * @return cuantas filas se escribieron
	 */
	public static int pasarANomina(final String semana, final String usuario) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (-1);
		}
		try {
			//Primero se borra el estimado anterior de esa semana y despues se
			//escribe el nuevo. Es mas claro que un UPDATE de seis columnas, y
			//deja la tabla igual que si fuera la primera corrida. El DELETE
			//toca SOLO las filas con origen ESTIMADO: lo real no se toca.
			final PreparedStatement borrar = cn.prepareStatement(
					"DELETE FROM gerencia_nomina_empleado_semana"
					+ " WHERE semana = ? AND origen = 'ESTIMADO'");
			borrar.setString(1, semana);
			borrar.executeUpdate();
			borrar.close();

			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_empleado_semana"
					+ " (idempleado, semana, sueldo_basico, sueldo_variable, seguridad_social,"
					+ "  liquidacion, origen, usuario)"
					//La correccion se reparte proporcional entre las cuatro
					//columnas, para que sumen el costo corregido y el tablero no
					//tenga que saber que hubo una correccion.
					+ " SELECT e.idempleado, e.semana,"
					+ "        e.sueldo_basico * k.f, (e.sueldo_variable + e.auxilio_transporte) * k.f,"
					+ "        e.seguridad_social * k.f, e.liquidacion * k.f, 'ESTIMADO', ?"
					+ "   FROM gerencia_nomina_estimado e"
					+ "   JOIN (SELECT idempleado, semana,"
					+ "                IF(costo_total > 0, costo_corregido / costo_total, 1) AS f"
					+ "           FROM gerencia_nomina_estimado WHERE semana = ?) k"
					+ "     ON k.idempleado = e.idempleado AND k.semana = e.semana"
					+ "  WHERE e.semana = ? AND e.costo_total > 0"
					//Si ya hay un real para ese empleado y esa semana, el
					//estimado no entra: el dato de SIIGO manda sobre el calculo,
					//siempre, y una corrida distraida no puede pisarlo.
					+ "    AND NOT EXISTS (SELECT 1 FROM gerencia_nomina_empleado_semana ya"
					+ "                     WHERE ya.idempleado = e.idempleado AND ya.semana = e.semana)");
			ps.setString(1, usuario);
			ps.setString(2, semana);
			ps.setString(3, semana);
			final int n = ps.executeUpdate();
			ps.close();
			return (n);
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.pasarANomina: " + e.toString());
			return (-1);
		} finally {
			cerrar(cn);
		}
	}

	// =======================================================================
	// Lectura para la pantalla
	// =======================================================================

	public static ArrayList<Estimado> estimadosDe(final String semana) {
		final ArrayList<Estimado> lista = new ArrayList<Estimado>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (lista);
		}
		try {
			final HashMap<Integer, NovedadReg> novMap = novedadesVigentes(cn, semana);
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT e.*, IFNULL((SELECT r.idtienda"
					+ "    FROM gerencia_nomina_empleado_tienda_semana r"
					+ "   WHERE r.idempleado = e.idempleado AND r.semana = e.semana"
					+ "   ORDER BY r.minutos DESC LIMIT 1), 0) AS idtienda,"
					+ " n.origen AS origen_cargado,"
					+ " IFNULL(n.sueldo_basico, 0) AS real_basico,"
					+ " IFNULL(n.sueldo_variable, 0) AS real_variable,"
					+ " IFNULL(n.seguridad_social, 0) AS real_seg_social,"
					+ " IFNULL(n.liquidacion, 0) AS real_liquidacion"
					+ " FROM gerencia_nomina_estimado e"
					+ " LEFT JOIN gerencia_nomina_empleado_semana n"
					+ "   ON n.idempleado = e.idempleado AND n.semana = e.semana"
					+ " WHERE e.semana = ?"
					+ " ORDER BY e.costo_total DESC");
			ps.setString(1, semana);
			final ResultSet rs = ps.executeQuery();
			final HashSet<Integer> idsVistos = new HashSet<Integer>();
			while (rs.next()) {
				final int idEmp = rs.getInt("idempleado");
				if (!idsVistos.add(Integer.valueOf(idEmp))) {
					continue; // Prevencion absoluta contra duplicados
				}
				final Estimado e = new Estimado();
				e.idEmpleado = idEmp;
				e.salarioBase = rs.getDouble("salario_base");
				e.horasOrdinarias = rs.getDouble("horas_ordinarias");
				e.horasNocturnas = rs.getDouble("horas_nocturnas");
				e.horasDominicales = rs.getDouble("horas_dominicales");
				e.horasDomNoct = rs.getDouble("horas_dom_noct");
				e.horasFestivas = rs.getDouble("horas_festivas");
				e.horasFestNoct = rs.getDouble("horas_fest_noct");
				e.horasExtra = rs.getDouble("horas_extra");
				e.horasExtraFestiva = rs.getDouble("horas_extra_festiva");
				e.horasTotales = rs.getDouble("horas_totales");
				e.turnos = rs.getInt("turnos");
				e.sueldoBasico = rs.getDouble("sueldo_basico");
				e.sueldoVariable = rs.getDouble("sueldo_variable");
				e.auxilioTransporte = rs.getDouble("auxilio_transporte");
				e.seguridadSocial = rs.getDouble("seguridad_social");
				e.liquidacion = rs.getDouble("liquidacion");
				e.costoTotal = rs.getDouble("costo_total");
				e.factorCorreccion = rs.getDouble("factor_correccion");
				e.costoCorregido = rs.getDouble("costo_corregido");
				e.aviso = texto(rs.getString("aviso"));
				e.idTiendaPrincipal = rs.getInt("idtienda");
				e.origenCargado = texto(rs.getString("origen_cargado"));
				e.realBasico = rs.getDouble("real_basico");
				e.realVariable = rs.getDouble("real_variable");
				e.realSegSocial = rs.getDouble("real_seg_social");
				e.realLiquidacion = rs.getDouble("real_liquidacion");
				e.realCostoTotal = redondear(e.realBasico + e.realVariable + e.realSegSocial + e.realLiquidacion);

				e.recargoNocturno = rs.getDouble("recargo_nocturno");
				e.recargoDominical = rs.getDouble("recargo_dominical");
				e.recargoFestivo = rs.getDouble("recargo_festivo");
				e.recargoExtra = rs.getDouble("recargo_extra");
				e.recargoExtraFestiva = rs.getDouble("recargo_extra_festiva");

				final NovedadReg nov = novMap.get(Integer.valueOf(e.idEmpleado));
				if (nov != null) {
					e.novedadTipo = nov.tipoNovedad;
					e.novedadAccion = nov.accion;
					e.novedadObs = nov.observacion;
					e.novedadDias = nov.dias;
					e.novedadPermanente = nov.permanente;
				}

				lista.add(e);
			}
			rs.close();
			ps.close();
			ponerNombres(lista);
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.estimadosDe: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	public static HashMap<Integer, NovedadReg> novedadesVigentes(final Connection cn, final String semana) {
		final HashMap<Integer, NovedadReg> map = new HashMap<Integer, NovedadReg>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idempleado, tipo_novedad, accion, dias, permanente, observacion, semana "
					+ " FROM gerencia_nomina_novedad WHERE semana = ? OR permanente = 1"
					+ " ORDER BY (semana = ?) ASC, fecha_registro ASC");
			ps.setString(1, semana);
			ps.setString(2, semana);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final NovedadReg n = new NovedadReg();
				n.idEmpleado = rs.getInt("idempleado");
				n.tipoNovedad = texto(rs.getString("tipo_novedad"));
				n.accion = texto(rs.getString("accion"));
				n.dias = rs.getDouble("dias") > 0 ? rs.getDouble("dias") : 7;
				n.permanente = rs.getInt("permanente") == 1;
				n.observacion = texto(rs.getString("observacion"));
				map.put(Integer.valueOf(n.idEmpleado), n);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.novedadesVigentes: " + e.toString());
		}
		return (map);
	}

	public static void aplicarNovedad(final Estimado e, final NovedadReg nov) {
		e.novedadTipo = nov.tipoNovedad;
		e.novedadAccion = nov.accion;
		e.novedadDias = nov.dias;
		e.novedadPermanente = nov.permanente;
		e.novedadObs = nov.observacion;

		if ("EXCLUIR".equalsIgnoreCase(nov.accion)) {
			e.sueldoBasico = 0;
			e.sueldoVariable = 0;
			e.auxilioTransporte = 0;
			e.seguridadSocial = 0;
			e.liquidacion = 0;
			e.costoTotal = 0;
			e.costoCorregido = 0;
			if ("VACACIONES_EXCLUIR".equals(nov.tipoNovedad)) {
				e.aviso = "Vacaciones completas: excluido de nómina corriente (se paga por prestaciones)";
			} else {
				e.aviso = "Excluido de la nómina semanal (" + (nov.observacion.isEmpty() ? nov.tipoNovedad : nov.observacion) + ")";
			}
		} else if ("AJUSTAR".equalsIgnoreCase(nov.accion) || "VACACIONES_PARCIAL".equals(nov.tipoNovedad)) {
			final double dLab = nov.dias > 0 && nov.dias <= 7 ? nov.dias : 3;
			e.sueldoBasico = redondear(e.salarioBase * dLab / 30.0);
			e.auxilioTransporte = (e.salarioBase > 0 && e.salarioBase <= 2 * 1300000 && !e.esSena && !e.esSenaElectiva) ? redondear(162000.0 * dLab / 30.0) : 0;
			e.seguridadSocial = redondear((e.sueldoBasico + e.sueldoVariable) * ((e.esSena || e.esSenaElectiva) ? 0.15 : 0.205));
			e.liquidacion = (e.esSena || e.esSenaElectiva) ? 0 : redondear((e.sueldoBasico + e.sueldoVariable + e.auxilioTransporte) * 0.2183);
			e.costoTotal = redondear(e.sueldoBasico + e.sueldoVariable + e.auxilioTransporte + e.seguridadSocial + e.liquidacion);
			e.costoCorregido = e.costoTotal;
			final String dStr = nov.dias == (int) nov.dias ? String.valueOf((int) nov.dias) : String.format(java.util.Locale.US, "%.2f", nov.dias);
			e.aviso = "Vacaciones parciales: liquidado sobre " + dStr + " días laborados";
		} else if ("SENA_LECTIVA".equals(nov.tipoNovedad)) {
			e.esSena = true;
			e.esSenaElectiva = true;
			e.sueldoBasico = redondear((e.salarioBase * 0.75) * SEMANA_DEL_MES);
			e.auxilioTransporte = 0;
			e.seguridadSocial = redondear((e.salarioBase * SEMANA_DEL_MES) * 0.15);
			e.liquidacion = 0;
			e.costoTotal = redondear(e.sueldoBasico + e.sueldoVariable + e.seguridadSocial);
			e.costoCorregido = e.costoTotal;
			e.aviso = "Aprendiz SENA (Etapa Lectiva): Apoyo sostenimiento 75%, SS 15%, sin liquidación ni auxilio.";
		} else if ("SENA_PRACTICA".equals(nov.tipoNovedad)) {
			e.esSena = true;
			e.esSenaElectiva = false;
			e.sueldoBasico = redondear(e.salarioBase * SEMANA_DEL_MES);
			e.auxilioTransporte = 0;
			e.seguridadSocial = redondear((e.sueldoBasico + e.sueldoVariable) * 0.15);
			e.liquidacion = 0;
			e.costoTotal = redondear(e.sueldoBasico + e.sueldoVariable + e.seguridadSocial);
			e.costoCorregido = e.costoTotal;
			e.aviso = "Aprendiz SENA (Etapa Práctica): Apoyo sostenimiento 100%, SS 15%, sin liquidación.";
		} else if ("POSIBLE_DESPIDO".equals(nov.tipoNovedad)) {
			e.esPosibleDespido = true;
			e.aviso = "Posible retiro / despido (confirmado): tenido en cuenta en nómina (" + (nov.observacion.isEmpty() ? "Laboró en período" : nov.observacion) + ")";
		} else {
			// INCLUIR (Directivo, incapacidad, etc.)
			if ("DIRECTIVO_INCLUIR".equals(nov.tipoNovedad)) {
				e.sueldoVariable = 0;
				e.recargoNocturno = 0;
				e.recargoDominical = 0;
				e.recargoFestivo = 0;
				e.horasExtra = 0;
				e.horasExtraFestiva = 0;
				e.recargoExtra = 0;
				e.recargoExtraFestiva = 0;
				e.costoTotal = redondear(e.sueldoBasico + e.auxilioTransporte + e.seguridadSocial + e.liquidacion);
				e.costoCorregido = e.costoTotal;
				e.aviso = "Directivo / Administrativo: nómina completa sin requerir biometría";
			} else if ("INCAPACIDAD_INCLUIR".equals(nov.tipoNovedad)) {
				e.aviso = "Incapacidad justificada: liquidado en nómina";
			} else {
				e.aviso = "Justificado: " + (nov.observacion.isEmpty() ? nov.tipoNovedad : nov.observacion);
			}
		}
	}

	public static String guardarNovedad(final String semana, final int idEmpleado,
			final String tipoNovedad, final String accion, final double dias,
			final boolean permanente, final String observacion, final String usuario) {
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return ("No hay conexión a la base de datos");
		}
		try {
			if ("RESTABLECER".equalsIgnoreCase(tipoNovedad)) {
				final PreparedStatement del = cn.prepareStatement(
						"DELETE FROM gerencia_nomina_novedad WHERE idempleado = ? AND (semana = ? OR permanente = 1)");
				del.setInt(1, idEmpleado);
				del.setString(2, semana);
				del.executeUpdate();
				del.close();

				// Restablecer valores originales en gerencia_nomina_estimado
				final PreparedStatement psSel = cn.prepareStatement(
						"SELECT salario_base, turnos, sueldo_variable, auxilio_transporte FROM gerencia_nomina_estimado WHERE idempleado = ? AND semana = ?");
				psSel.setInt(1, idEmpleado);
				psSel.setString(2, semana);
				final ResultSet rs = psSel.executeQuery();
				if (rs.next()) {
					final double sal = rs.getDouble("salario_base");
					final int turnos = rs.getInt("turnos");
					final double bas = redondear(sal * SEMANA_DEL_MES);
					final double var = rs.getDouble("sueldo_variable");
					final double aux = rs.getDouble("auxilio_transporte");
					final double seg = redondear((bas + var) * 0.205);
					final double liq = redondear((bas + var + aux) * 0.2183);
					final double tot = redondear(bas + var + aux + seg + liq);
					final String av = turnos == 0 ? "Sin marcaciones en la semana: va el basico sin recargos. Revisar si fue vacaciones, incapacidad o falta de marcacion." : "";
					rs.close();
					psSel.close();

					final PreparedStatement psUpd = cn.prepareStatement(
							"UPDATE gerencia_nomina_estimado SET sueldo_basico = ?, seguridad_social = ?, "
							+ " liquidacion = ?, costo_total = ?, costo_corregido = ?, aviso = ? WHERE idempleado = ? AND semana = ?");
					psUpd.setDouble(1, bas);
					psUpd.setDouble(2, seg);
					psUpd.setDouble(3, liq);
					psUpd.setDouble(4, tot);
					psUpd.setDouble(5, tot);
					psUpd.setString(6, av);
					psUpd.setInt(7, idEmpleado);
					psUpd.setString(8, semana);
					psUpd.executeUpdate();
					psUpd.close();
				} else {
					rs.close();
					psSel.close();
				}
				return ("");
			}

			final PreparedStatement psNov = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_novedad (idempleado, semana, tipo_novedad, accion, dias, permanente, observacion, usuario) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
					+ " ON DUPLICATE KEY UPDATE tipo_novedad = ?, accion = ?, dias = ?, permanente = ?, observacion = ?, usuario = ?");
			psNov.setInt(1, idEmpleado);
			psNov.setString(2, semana);
			psNov.setString(3, tipoNovedad);
			psNov.setString(4, accion);
			psNov.setDouble(5, dias);
			psNov.setInt(6, permanente ? 1 : 0);
			psNov.setString(7, observacion);
			psNov.setString(8, usuario);
			psNov.setString(9, tipoNovedad);
			psNov.setString(10, accion);
			psNov.setDouble(11, dias);
			psNov.setInt(12, permanente ? 1 : 0);
			psNov.setString(13, observacion);
			psNov.setString(14, usuario);
			psNov.executeUpdate();
			psNov.close();

			if (permanente) {
				// Evitar duplicados permanentes en la tabla para el mismo empleado
				final PreparedStatement delOld = cn.prepareStatement(
						"DELETE FROM gerencia_nomina_novedad WHERE idempleado = ? AND permanente = 1 AND semana <> ?");
				delOld.setInt(1, idEmpleado);
				delOld.setString(2, semana);
				delOld.executeUpdate();
				delOld.close();
			}

			final NovedadReg nov = new NovedadReg();
			nov.idEmpleado = idEmpleado;
			nov.tipoNovedad = tipoNovedad;
			nov.accion = accion;
			nov.dias = dias;
			nov.permanente = permanente;
			nov.observacion = observacion;

			final PreparedStatement psSel = cn.prepareStatement(
					"SELECT * FROM gerencia_nomina_estimado WHERE idempleado = ? AND semana = ?");
			psSel.setInt(1, idEmpleado);
			psSel.setString(2, semana);
			final ResultSet rs = psSel.executeQuery();
			if (rs.next()) {
				final Estimado e = new Estimado();
				e.idEmpleado = idEmpleado;
				e.salarioBase = rs.getDouble("salario_base");
				e.sueldoBasico = redondear(e.salarioBase * SEMANA_DEL_MES);
				e.sueldoVariable = rs.getDouble("sueldo_variable");
				e.auxilioTransporte = rs.getDouble("auxilio_transporte");
				e.seguridadSocial = rs.getDouble("seguridad_social");
				e.liquidacion = rs.getDouble("liquidacion");
				e.costoTotal = rs.getDouble("costo_total");
				e.costoCorregido = rs.getDouble("costo_corregido");
				e.aviso = rs.getString("aviso");
				rs.close();
				psSel.close();

				aplicarNovedad(e, nov);

				final PreparedStatement psUpd = cn.prepareStatement(
						"UPDATE gerencia_nomina_estimado SET sueldo_basico = ?, sueldo_variable = ?, "
						+ " auxilio_transporte = ?, seguridad_social = ?, liquidacion = ?, costo_total = ?, "
						+ " costo_corregido = ?, aviso = ? WHERE idempleado = ? AND semana = ?");
				psUpd.setDouble(1, e.sueldoBasico);
				psUpd.setDouble(2, e.sueldoVariable);
				psUpd.setDouble(3, e.auxilioTransporte);
				psUpd.setDouble(4, e.seguridadSocial);
				psUpd.setDouble(5, e.liquidacion);
				psUpd.setDouble(6, e.costoTotal);
				psUpd.setDouble(7, e.costoCorregido);
				psUpd.setString(8, e.aviso);
				psUpd.setInt(9, idEmpleado);
				psUpd.setString(10, semana);
				psUpd.executeUpdate();
				psUpd.close();
			} else {
				rs.close();
				psSel.close();
			}
			return ("");
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.guardarNovedad: " + e.toString());
			return (e.getMessage());
		} finally {
			cerrar(cn);
		}
	}

	public static class Parametro {
		public int id;
		public String codigo = "";
		public String nombre = "";
		public double valor;
		public String unidad = "";
		public String desde = "";
		public String hasta = "";
		public String observacion = "";
	}

	public static ArrayList<Parametro> listarParametros() {
		final ArrayList<Parametro> lista = new ArrayList<Parametro>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return (lista);
		}
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idparametro, codigo, nombre, valor, unidad, vigencia_desde,"
					+ " vigencia_hasta, observacion FROM gerencia_nomina_parametro"
					+ " ORDER BY codigo, vigencia_desde");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Parametro p = new Parametro();
				p.id = rs.getInt("idparametro");
				p.codigo = texto(rs.getString("codigo"));
				p.nombre = texto(rs.getString("nombre"));
				p.valor = rs.getDouble("valor");
				p.unidad = texto(rs.getString("unidad"));
				p.desde = texto(rs.getString("vigencia_desde"));
				p.hasta = texto(rs.getString("vigencia_hasta"));
				p.observacion = texto(rs.getString("observacion"));
				lista.add(p);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.listarParametros: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	/**
	 * Cambia un parametro abriendo una vigencia nueva, no pisando la vieja.
	 *
	 * Igual que los gastos fijos: si se sobrescribiera el valor, una semana ya
	 * calculada cambiaria por detras y nadie podria explicar por que.
	 */
	public static String guardarParametro(final String codigo, final double valor,
			final String desde, final String observacion, final String usuario) {
		if (!desde.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
			return ("{\"respuesta\":\"INVALIDO\",\"detalle\":\"La fecha debe venir como aaaa-mm-dd\"}");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"Sin conexion\"}");
		}
		try {
			//El nombre y la unidad se heredan de la vigencia anterior: quien
			//cambia un valor no deberia tener que volver a escribir como se
			//llama el parametro ni en que unidad va.
			String nombre = null;
			String unidad = null;
			final PreparedStatement leer = cn.prepareStatement(
					"SELECT nombre, unidad FROM gerencia_nomina_parametro"
					+ " WHERE codigo = ? ORDER BY vigencia_desde DESC LIMIT 1");
			leer.setString(1, codigo);
			final ResultSet rs = leer.executeQuery();
			if (rs.next()) {
				nombre = rs.getString("nombre");
				unidad = rs.getString("unidad");
			}
			rs.close();
			leer.close();
			if (nombre == null) {
				return ("{\"respuesta\":\"INVALIDO\",\"detalle\":\"No existe el parametro " + codigo + "\"}");
			}

			cn.setAutoCommit(false);
			//Se le cierra la vigencia a la que estuviera abierta el dia anterior,
			//en vez de sobrescribir el valor: una semana ya calculada no puede
			//cambiar por detras.
			final PreparedStatement cerrarAnterior = cn.prepareStatement(
					"UPDATE gerencia_nomina_parametro SET vigencia_hasta = DATE_SUB(?, INTERVAL 1 DAY)"
					+ " WHERE codigo = ? AND vigencia_desde < ?"
					+ "   AND (vigencia_hasta IS NULL OR vigencia_hasta >= ?)");
			cerrarAnterior.setString(1, desde);
			cerrarAnterior.setString(2, codigo);
			cerrarAnterior.setString(3, desde);
			cerrarAnterior.setString(4, desde);
			cerrarAnterior.executeUpdate();
			cerrarAnterior.close();

			final PreparedStatement ins = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_parametro"
					+ " (codigo, nombre, valor, unidad, vigencia_desde, observacion, usuario)"
					+ " VALUES (?,?,?,?,?,?,?)"
					//Corregir dos veces el mismo dia no crea una fila nueva: se
					//sobrescribe esa, que todavia no ha regido nada.
					+ " ON DUPLICATE KEY UPDATE valor = ?, observacion = ?, usuario = ?");
			int i = 1;
			ins.setString(i++, codigo);
			ins.setString(i++, nombre);
			ins.setDouble(i++, valor);
			ins.setString(i++, unidad);
			ins.setString(i++, desde);
			ins.setString(i++, observacion);
			ins.setString(i++, usuario);
			ins.setDouble(i++, valor);
			ins.setString(i++, observacion);
			ins.setString(i++, usuario);
			ins.executeUpdate();
			ins.close();
			cn.commit();
			return ("{\"respuesta\":\"OK\"}");
		} catch (final Exception e) {
			try {
				cn.rollback();
			} catch (final Exception e2) {
				System.out.println("GerenciaNominaCalculoDAO rollback: " + e2.toString());
			}
			System.out.println("GerenciaNominaCalculoDAO.guardarParametro: " + e.toString());
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"No se pudo guardar\"}");
		} finally {
			try {
				cn.setAutoCommit(true);
			} catch (final Exception e3) {
				System.out.println("GerenciaNominaCalculoDAO autocommit: " + e3.toString());
			}
			cerrar(cn);
		}
	}

	// =======================================================================
	// La carga del dato real
	// =======================================================================

	public static class ResultadoCarga {
		public int cargados;
		public int noEncontrados;
		public int ilegibles;
		public double totalPesos = 0;
		public double totalHoras = 0;
		public final ArrayList<String> problemas = new ArrayList<String>();
	}

	/**
	 * Carga en lote la nomina real, pegada desde la exportacion de Siigo.
	 *
	 * POR QUE PEGANDO Y NO POR API
	 *
	 * La API publica de Siigo Nube expone contabilidad y facturacion -productos,
	 * clientes, facturas, comprobantes, asientos- y NO tiene endpoints de
	 * nomina. Lo que Siigo Nomina genera son planos de PILA y de dispersion
	 * bancaria, que no sirven: el PILA va por base de cotizacion y el bancario
	 * por neto a pagar, y ninguno de los dos es el COSTO del empleado. Asi que
	 * mientras no haya otra via, el real entra por aqui.
	 *
	 * EL FORMATO
	 *
	 * Una linea por empleado, separada por tabulacion, punto y coma o coma:
	 *
	 *   documento ; basico ; variable ; seguridad social ; liquidacion
	 *
	 * Se busca por el documento, que es el mismo id de general.empleado -el que
	 * usa la biometria-. Una linea que no se pueda emparejar NO se descarta en
	 * silencio: se devuelve en la lista de problemas, porque un empleado que no
	 * entro es plata que no se contabilizo y nadie lo notaria.
	 *
	 * EL ORIGEN LO MARCA TODO
	 *
	 * Lo cargado entra con el origen que se indique -SIIGO, MANUAL- y nunca
	 * como ESTIMADO. Eso es lo que hace que pasarANomina no lo pise despues, y
	 * lo que permite medir la desviacion: solo se compara contra origen real.
	 */
	public static ResultadoCarga cargarLote(final String semana, final String pegado,
			final String origen, final String usuario) {
		return (cargarLote(semana, pegado, origen, usuario, 0));
	}

	public static ResultadoCarga cargarLote(final String semana, final String pegado,
			final String origen, final String usuario, final int idPeriodo) {
		final ResultadoCarga r = new ResultadoCarga();
		final ArrayList<String> domingos = new ArrayList<String>();

		if (idPeriodo > 0) {
			final ConexionBaseDatos conGen = new ConexionBaseDatos();
			final Connection cnGen = conGen.obtenerConexionBDGeneral();
			if (cnGen != null) {
				try {
					final PreparedStatement psPer = cnGen.prepareStatement(
							"SELECT fecha_inferior, fecha_superior FROM periodo_nomina WHERE idperiodo = ?");
					psPer.setInt(1, idPeriodo);
					final ResultSet rsPer = psPer.executeQuery();
					if (rsPer.next()) {
						final String fInf = rsPer.getString("fecha_inferior");
						final String fSup = rsPer.getString("fecha_superior");
						if (fInf != null && fSup != null && fInf.length() == 10 && fSup.length() == 10) {
							final LocalDate ini = LocalDate.parse(fInf);
							final LocalDate fin = LocalDate.parse(fSup);
							LocalDate cur = ini;
							while (!cur.isAfter(fin)) {
								if (cur.getDayOfWeek() == DayOfWeek.SUNDAY) {
									domingos.add(cur.toString());
								}
								cur = cur.plusDays(1);
							}
						}
					}
					rsPer.close();
					psPer.close();
				} catch (final Exception ex) {
					System.out.println("GerenciaNominaCalculoDAO.cargarLote periodo: " + ex.toString());
				} finally {
					cerrar(cnGen);
				}
			}
		}

		if (domingos.isEmpty()) {
			if (rangoDeLaSemana(semana) == null) {
				r.problemas.add("La semana debe ser un domingo, en formato aaaa-mm-dd.");
				return (r);
			}
			domingos.add(semana);
		}

		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			r.problemas.add("Sin conexion a la base.");
			return (r);
		}
		try {
			final String origenLimpio = (origen != null && origen.length() > 20) ? origen.substring(0, 20)
					: (origen != null && !origen.trim().isEmpty() ? origen.trim() : "EXCEL");
			final HashMap<String, EmpleadoInfo> mapaEmp = mapaEmpleadosPorIdent();
			final Parametros params = parametrosAl(cn, domingos.get(0));
			final double smlv = params.de("SMLV", 1300000);
			final double auxilioMensual = params.de("AUXILIO_TRANSPORTE", 162000);

			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_empleado_semana"
					+ " (idempleado, semana, sueldo_basico, sueldo_variable, seguridad_social,"
					+ "  liquidacion, origen, usuario)"
					+ " VALUES (?,?,?,?,?,?,?,?)"
					+ " ON DUPLICATE KEY UPDATE"
					+ "   sueldo_basico = ?, sueldo_variable = ?, seguridad_social = ?,"
					+ "   liquidacion = ?, origen = ?, usuario = ?");

			final String[] lineas = partirLineas(pegado);
			for (int i = 0; i < lineas.length; i++) {
				final String linea = lineas[i].trim();
				if (linea.length() == 0) {
					continue;
				}
				final String[] campos = partirCampos(linea);
				if (campos.length < 2) {
					if (i == 0) {
						continue; // Encabezado de una columna
					}
					r.ilegibles++;
					r.problemas.add("Linea " + (i + 1) + ": se esperaban al menos 2 columnas -> " + recorte(linea));
					continue;
				}

				final String docRaw = campos[0].trim();
				final String docLimpio = docRaw.replace(".", "").replace(",", "").replace("\"", "").replace(" ", "");
				final EmpleadoInfo emp = mapaEmp.get(docLimpio);
				if (emp == null) {
					// Si es la primera linea y no es numero o no existe, ignorar como encabezado
					if (i == 0 && (!esNumero(docLimpio) || entero(docLimpio) <= 0)) {
						continue;
					}
					r.noEncontrados++;
					r.problemas.add("Linea " + (i + 1) + ": el documento '" + docRaw
							+ "' no existe en el maestro de empleados. NO se cargo.");
					continue;
				}

				// Si la columna 1 es texto no numerico (ej. nombre del empleado), desplazar las columnas numericas
				int idxNum = 1;
				if (campos.length > 2 && !esNumero(campos[1])) {
					idxNum = 2;
				}

				final int numCols = campos.length - idxNum;
				double basicoTotal = -1;
				double varTotal = 0;
				double segTotal = -1;
				double liqTotal = -1;

				if (numCols == 1) {
					varTotal = pesos(campos[idxNum]);
				} else if (numCols == 2) {
					basicoTotal = pesos(campos[idxNum]);
					varTotal = pesos(campos[idxNum + 1]);
				} else if (numCols == 3) {
					basicoTotal = pesos(campos[idxNum]);
					varTotal = pesos(campos[idxNum + 1]);
					segTotal = pesos(campos[idxNum + 2]);
				} else if (numCols >= 4) {
					basicoTotal = pesos(campos[idxNum]);
					varTotal = pesos(campos[idxNum + 1]);
					segTotal = pesos(campos[idxNum + 2]);
					liqTotal = pesos(campos[idxNum + 3]);
				}

				if (varTotal < 0) {
					r.ilegibles++;
					r.problemas.add("Linea " + (i + 1) + ": el valor variable no se entiende -> " + recorte(linea));
					continue;
				}

				final int numSemanas = domingos.size();
				// Para cada semana (domingo) asignada
				for (int s = 0; s < numSemanas; s++) {
					final String sem = domingos.get(s);
					// Fijo semanal: salario mensual / 30 * 7
					final double basicoSem = (basicoTotal > 0 && numSemanas == 1) ? basicoTotal
							: redondear(emp.salario * SEMANA_DEL_MES);
					final double varSem = redondear(varTotal / (double) numSemanas);
					final double auxSem = (emp.salario <= smlv * 2) ? redondear(auxilioMensual * SEMANA_DEL_MES) : 0;
					final double segSem = (segTotal >= 0 && numSemanas == 1) ? segTotal
							: redondear((basicoSem + varSem) * 0.205);
					final double liqSem = (liqTotal >= 0 && numSemanas == 1) ? liqTotal
							: redondear((basicoSem + varSem + auxSem) * 0.2183);

					int k = 1;
					ps.setInt(k++, emp.id);
					ps.setString(k++, sem);
					ps.setDouble(k++, basicoSem);
					ps.setDouble(k++, varSem);
					ps.setDouble(k++, segSem);
					ps.setDouble(k++, liqSem);
					ps.setString(k++, origen);
					ps.setString(k++, usuario);
					ps.setDouble(k++, basicoSem);
					ps.setDouble(k++, varSem);
					ps.setDouble(k++, segSem);
					ps.setDouble(k++, liqSem);
					ps.setString(k++, origen);
					ps.setString(k++, usuario);
					ps.addBatch();
				}
				r.cargados++;
			}
			ps.executeBatch();
			ps.close();

			// Medir desviaciones contra lo estimado para todas las semanas afectadas
			for (int s = 0; s < domingos.size(); s++) {
				registrarDesviacion(domingos.get(s));
			}
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.cargarLote: " + e.toString());
			r.problemas.add("Error guardando: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (r);
	}

	public static HashMap<String, EmpleadoInfo> mapaEmpleadosPorIdent() {
		final HashMap<String, EmpleadoInfo> map = new HashMap<String, EmpleadoInfo>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		if (cn == null) {
			return (map);
		}
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT id, nombre, nombre_largo, IFNULL(salario, 0) AS salario, activo FROM empleado");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final EmpleadoInfo emp = new EmpleadoInfo();
				emp.id = rs.getInt("id");
				emp.cedula = texto(rs.getString("nombre")).trim();
				emp.nombreLargo = texto(rs.getString("nombre_largo")).trim();
				emp.salario = rs.getDouble("salario");
				emp.activo = rs.getInt("activo") == 1;

				final String cedLimpia = emp.cedula.replace(".", "").replace(",", "").replace("\"", "").trim();
				if (cedLimpia.length() > 0) {
					map.put(cedLimpia, emp);
				}
				map.put(String.valueOf(emp.id), emp);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.mapaEmpleadosPorIdent: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (map);
	}

	public static ArrayList<PeriodoNomina> listarPeriodosRecientes() {
		final ArrayList<PeriodoNomina> lista = new ArrayList<PeriodoNomina>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		if (cn == null) {
			return (lista);
		}
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idperiodo, fecha_inferior, fecha_superior FROM periodo_nomina"
					+ " WHERE fecha_superior >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)"
					+ " ORDER BY idperiodo DESC LIMIT 20");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final PeriodoNomina p = new PeriodoNomina();
				p.idPeriodo = rs.getInt("idperiodo");
				p.fechaInferior = texto(rs.getString("fecha_inferior"));
				p.fechaSuperior = texto(rs.getString("fecha_superior"));
				if (p.fechaInferior.length() == 10 && p.fechaSuperior.length() == 10) {
					final LocalDate ini = LocalDate.parse(p.fechaInferior);
					final LocalDate fin = LocalDate.parse(p.fechaSuperior);
					LocalDate cur = ini;
					while (!cur.isAfter(fin)) {
						if (cur.getDayOfWeek() == DayOfWeek.SUNDAY) {
							p.domingos.add(cur.toString());
						}
						cur = cur.plusDays(1);
					}
				}
				lista.add(p);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.listarPeriodosRecientes: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (lista);
	}

	public static ResumenConciliacion consultarConciliacion(final String modo, final int idPeriodo, final int anio, final int mes) {
		final ResumenConciliacion res = new ResumenConciliacion();
		res.modo = (modo != null && "MES".equalsIgnoreCase(modo)) ? "MES" : "QUINCENA";
		res.idPeriodo = idPeriodo;

		final ConexionBaseDatos conGen = new ConexionBaseDatos();
		final Connection cnGen = conGen.obtenerConexionBDGeneral();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();

		if (cnGen == null || cn == null) {
			cerrar(cnGen);
			cerrar(cn);
			return (res);
		}

		try {
			if ("QUINCENA".equalsIgnoreCase(res.modo) && idPeriodo > 0) {
				final PreparedStatement ps = cnGen.prepareStatement("SELECT fecha_inferior, fecha_superior FROM periodo_nomina WHERE idperiodo = ?");
				ps.setInt(1, idPeriodo);
				final ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					res.fechaDesde = texto(rs.getString("fecha_inferior"));
					res.fechaHasta = texto(rs.getString("fecha_superior"));
					res.periodoTexto = "Quincena " + idPeriodo + " (" + res.fechaDesde + " al " + res.fechaHasta + ")";
				}
				rs.close();
				ps.close();

				if (res.fechaDesde.length() == 10 && res.fechaHasta.length() == 10) {
					final LocalDate ini = LocalDate.parse(res.fechaDesde);
					final LocalDate fin = LocalDate.parse(res.fechaHasta);
					LocalDate cur = ini;
					while (!cur.isAfter(fin)) {
						if (cur.getDayOfWeek() == DayOfWeek.SUNDAY) {
							res.domingos.add(cur.toString());
						}
						cur = cur.plusDays(1);
					}
				}
			} else {
				// Modo MES
				final int a = (anio >= 2020 && anio <= 2040) ? anio : Calendar.getInstance().get(Calendar.YEAR);
				final int m = (mes >= 1 && mes <= 12) ? mes : Calendar.getInstance().get(Calendar.MONTH) + 1;
				res.fechaDesde = String.format("%04d-%02d-01", Integer.valueOf(a), Integer.valueOf(m));
				final LocalDate ini = LocalDate.parse(res.fechaDesde);
				final LocalDate fin = ini.withDayOfMonth(ini.lengthOfMonth());
				res.fechaHasta = fin.toString();
				res.periodoTexto = "Mes " + m + "/" + a + " (" + res.fechaDesde + " al " + res.fechaHasta + ")";

				final PreparedStatement psSem = cn.prepareStatement(
						"SELECT fecha_fin FROM gerencia_semana WHERE anio = ? AND mes = ? ORDER BY numero");
				psSem.setInt(1, a);
				psSem.setInt(2, m);
				final ResultSet rsSem = psSem.executeQuery();
				while (rsSem.next()) {
					res.domingos.add(rsSem.getString("fecha_fin"));
				}
				rsSem.close();
				psSem.close();

				if (res.domingos.isEmpty()) {
					LocalDate cur = ini;
					while (!cur.isAfter(fin)) {
						if (cur.getDayOfWeek() == DayOfWeek.SUNDAY) {
							res.domingos.add(cur.toString());
						}
						cur = cur.plusDays(1);
					}
				}
			}

			if (res.domingos.isEmpty()) {
				return (res);
			}

			final StringBuilder inClause = new StringBuilder();
			for (int i = 0; i < res.domingos.size(); i++) {
				if (i > 0) {
					inClause.append(',');
				}
				inClause.append('\'').append(res.domingos.get(i)).append('\'');
			}

			final LinkedHashMap<Integer, FilaConciliacion> mapa = new LinkedHashMap<Integer, FilaConciliacion>();

			// 1. Biometría de las semanas
			final String sqlBio = "SELECT e.idempleado, emp.nombre AS cedula, emp.nombre_largo, IFNULL(t.descripcion, '') AS cargo, "
					+ " SUM(e.horas_totales) AS tot_horas, "
					+ " SUM(e.sueldo_basico) AS tot_basico, "
					+ " SUM(e.sueldo_variable) AS tot_variable, "
					+ " SUM(e.auxilio_transporte) AS tot_auxilio, "
					+ " SUM(e.seguridad_social) AS tot_seg_social, "
					+ " SUM(e.liquidacion) AS tot_liquidacion, "
					+ " SUM(e.costo_total) AS tot_costo, "
					+ " MAX(CASE WHEN e.aviso LIKE '%SENA%' THEN 1 ELSE 0 END) AS es_sena, "
					+ " IFNULL((SELECT tnd.nombre FROM gerencia_nomina_empleado_tienda_semana r "
					+ "         JOIN inventarioamericana.tienda tnd ON tnd.idtienda = r.idtienda "
					+ "         WHERE r.idempleado = e.idempleado AND r.semana IN (" + inClause + ") "
					+ "         ORDER BY r.minutos DESC LIMIT 1), 'Sin Asignar') AS tienda, "
					+ " IFNULL((SELECT r.idtienda FROM gerencia_nomina_empleado_tienda_semana r "
					+ "         WHERE r.idempleado = e.idempleado AND r.semana IN (" + inClause + ") "
					+ "         ORDER BY r.minutos DESC LIMIT 1), 0) AS idtienda "
					+ " FROM gerencia_nomina_estimado e "
					+ " JOIN general.empleado emp ON emp.id = e.idempleado "
					+ " LEFT JOIN general.tipo_empleado t ON t.idtipoempleado = emp.idtipoempleado "
					+ " WHERE e.semana IN (" + inClause + ") "
					+ " GROUP BY e.idempleado, emp.nombre, emp.nombre_largo, t.descripcion";

			final Statement st = cn.createStatement();
			final ResultSet rsBio = st.executeQuery(sqlBio);
			while (rsBio.next()) {
				final int idEmp = rsBio.getInt("idempleado");
				final FilaConciliacion f = new FilaConciliacion();
				f.idEmpleado = idEmp;
				f.cedula = texto(rsBio.getString("cedula"));
				f.nombre = texto(rsBio.getString("nombre_largo"));
				f.cargo = texto(rsBio.getString("cargo"));
				f.tienda = texto(rsBio.getString("tienda"));
				f.idTienda = rsBio.getInt("idtienda");

				f.horasBio = redondear(rsBio.getDouble("tot_horas"));
				f.basicoBio = redondear(rsBio.getDouble("tot_basico"));
				f.variableBio = redondear(rsBio.getDouble("tot_variable"));
				f.auxilioBio = redondear(rsBio.getDouble("tot_auxilio"));
				f.segSocialBio = redondear(rsBio.getDouble("tot_seg_social"));
				f.liquidacionBio = redondear(rsBio.getDouble("tot_liquidacion"));
				f.costoBio = redondear(rsBio.getDouble("tot_costo"));
				f.esSena = rsBio.getInt("es_sena") == 1;
				f.tieneBiometria = (f.costoBio > 0 || f.horasBio > 0);

				mapa.put(Integer.valueOf(idEmp), f);
			}
			rsBio.close();

			// 2. Real cargado en gerencia_nomina_real_cargue
			final PreparedStatement psReal = cn.prepareStatement(
					"SELECT idcargue, idempleado, cedula, nombre, total_pagado, horas_reales, origen, "
					+ " observacion_conciliacion, estado_conciliacion, fecha_registro "
					+ " FROM gerencia_nomina_real_cargue "
					+ " WHERE tipo_periodo = ? AND (idperiodo = ? OR (fecha_desde = ? AND fecha_hasta = ?))");
			psReal.setString(1, res.modo);
			psReal.setInt(2, res.idPeriodo);
			psReal.setString(3, res.fechaDesde);
			psReal.setString(4, res.fechaHasta);
			final ResultSet rsReal = psReal.executeQuery();
			boolean hayCargue = false;
			while (rsReal.next()) {
				hayCargue = true;
				final int idEmp = rsReal.getInt("idempleado");
				FilaConciliacion f = mapa.get(Integer.valueOf(idEmp));
				if (f == null) {
					f = new FilaConciliacion();
					f.idEmpleado = idEmp;
					f.cedula = texto(rsReal.getString("cedula"));
					f.nombre = texto(rsReal.getString("nombre"));
					f.tienda = "Solo en Nómina";
					mapa.put(Integer.valueOf(idEmp), f);
				}
				f.idCargue = rsReal.getInt("idcargue");
				f.horasReal = redondear(rsReal.getDouble("horas_reales"));
				f.totalPagadoReal = redondear(rsReal.getDouble("total_pagado"));
				f.origenReal = texto(rsReal.getString("origen"));
				f.observacion = texto(rsReal.getString("observacion_conciliacion"));
				f.estado = texto(rsReal.getString("estado_conciliacion"));
				f.fechaCargue = texto(rsReal.getString("fecha_registro"));
				f.tieneReal = true;
			}
			rsReal.close();
			psReal.close();

			// Si aún no se ha cargado en gerencia_nomina_real_cargue, consultar gerencia_nomina_empleado_semana
			if (!hayCargue) {
				final String sqlSem = "SELECT n.idempleado, SUM(n.sueldo_basico + n.sueldo_variable + n.seguridad_social + n.liquidacion) as tot_real, "
						+ " SUM(IFNULL(n.horas, 0)) as tot_horas_real, MAX(n.origen) as origen "
						+ " FROM gerencia_nomina_empleado_semana n "
						+ " WHERE n.semana IN (" + inClause + ") AND n.origen <> 'ESTIMADO' "
						+ " GROUP BY n.idempleado";
				final ResultSet rsSemReal = st.executeQuery(sqlSem);
				while (rsSemReal.next()) {
					final int idEmp = rsSemReal.getInt("idempleado");
					final FilaConciliacion f = mapa.get(Integer.valueOf(idEmp));
					if (f != null) {
						f.totalPagadoReal = redondear(rsSemReal.getDouble("tot_real"));
						f.horasReal = redondear(rsSemReal.getDouble("tot_horas_real"));
						f.origenReal = texto(rsSemReal.getString("origen"));
						f.tieneReal = true;
					}
				}
				rsSemReal.close();
			}
			st.close();

			// 3. Comparación y Diagnóstico
			for (final FilaConciliacion f : mapa.values()) {
				f.diffHoras = redondear(f.horasReal - f.horasBio);
				f.diffPesos = redondear(f.totalPagadoReal - f.costoBio);
				if (f.costoBio > 0) {
					f.diffPct = redondear((f.diffPesos / f.costoBio) * 100.0);
				}

				if (f.tieneReal && !f.tieneBiometria) {
					f.semaforo = "AZUL_SOLO_REAL";
					f.diagnostico = "Liquidado en n\u00f3mina sin registros en reloj biom\u00e9trico (personal administrativo, incapacidad o ausencia).";
					res.cantSoloReal++;
				} else if (!f.tieneReal && f.tieneBiometria) {
					f.semaforo = "AZUL_SOLO_BIO";
					f.diagnostico = "Marc\u00f3 " + String.format(Locale.US, "%.1f", Double.valueOf(f.horasBio))
							+ " hrs en tienda pero no aparece en el archivo de n\u00f3mina cargado.";
					res.cantSoloBio++;
				} else if (f.tieneReal && f.tieneBiometria) {
					final double absDifPesos = Math.abs(f.diffPesos);
					final double absDifHoras = Math.abs(f.diffHoras);

					if (absDifPesos <= 25000 && (f.horasReal == 0 || absDifHoras <= 2.0)) {
						f.semaforo = "VERDE";
						f.diagnostico = "Cuadrado: diferencia menor al 1% o margen de centavos.";
						res.cantCuadrados++;
					} else if (absDifPesos <= 95000 || Math.abs(f.diffPct) <= 5.0) {
						f.semaforo = "AMARILLO";
						if (absDifHoras > 3.0 && f.horasReal > 0) {
							f.diagnostico = "Diferencia leve: n\u00f3mina registra "
									+ String.format(Locale.US, "%+.1f", Double.valueOf(f.diffHoras))
									+ " hrs vs reloj.";
						} else {
							f.diagnostico = "Diferencia moderada en valor ("
									+ String.format(Locale.US, "%+.1f%%", Double.valueOf(f.diffPct))
									+ "). Variaci\u00f3n en recargos o bonificaci\u00f3n.";
						}
						res.cantDiferencias++;
					} else {
						f.semaforo = "ROJO";
						if (absDifHoras >= 5.0 && f.horasReal > 0) {
							f.diagnostico = "Descuadre por horas: brecha de "
									+ String.format(Locale.US, "%+.1f", Double.valueOf(f.diffHoras))
									+ " horas (" + (f.diffHoras > 0 ? "n\u00f3mina pag\u00f3 m\u00e1s horas" : "biometr\u00eda marc\u00f3 m\u00e1s horas")
									+ ").";
						} else {
							f.diagnostico = "Descuadre en valor: diferencia de $"
									+ String.format(Locale.US, "%,.0f", Double.valueOf(f.diffPesos))
									+ " (" + String.format(Locale.US, "%+.1f%%", Double.valueOf(f.diffPct))
									+ "). Verificar salario o liquidaci\u00f3n.";
						}
						res.cantDiferencias++;
					}
				}

				res.totalRealPagado += f.totalPagadoReal;
				res.totalBiometria += f.costoBio;
				res.horasTotalesReal += f.horasReal;
				res.horasTotalesBio += f.horasBio;

				res.filas.add(f);
			}

			res.totalRealPagado = redondear(res.totalRealPagado);
			res.totalBiometria = redondear(res.totalBiometria);
			res.horasTotalesReal = redondear(res.horasTotalesReal);
			res.horasTotalesBio = redondear(res.horasTotalesBio);
			res.diffPesos = redondear(res.totalRealPagado - res.totalBiometria);
			res.diffHoras = redondear(res.horasTotalesReal - res.horasTotalesBio);
			if (res.totalBiometria > 0) {
				res.diffPct = redondear((res.diffPesos / res.totalBiometria) * 100.0);
			}
			res.totalEmpleados = res.filas.size();

		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.consultarConciliacion: " + e.toString());
		} finally {
			cerrar(cnGen);
			cerrar(cn);
		}
		return (res);
	}

	public static ResultadoCarga guardarCargueConciliacion(final String modo, final int idPeriodo, final int anio,
			final int mes, final String pegado, final String tipoValor, final String origen, final String usuario) {
		final ResultadoCarga r = new ResultadoCarga();
		final ArrayList<String> domingos = new ArrayList<String>();
		String fechaDesde = "";
		String fechaHasta = "";
		String periodoTexto = "";

		final ConexionBaseDatos conGen = new ConexionBaseDatos();
		final Connection cnGen = conGen.obtenerConexionBDGeneral();
		if (cnGen == null) {
			r.problemas.add("Sin conexion a la base general.");
			return (r);
		}

		final String modoLimpio = (modo != null && "MES".equalsIgnoreCase(modo)) ? "MES" : "QUINCENA";

		try {
			if ("QUINCENA".equalsIgnoreCase(modoLimpio) && idPeriodo > 0) {
				final PreparedStatement ps = cnGen.prepareStatement(
						"SELECT fecha_inferior, fecha_superior FROM periodo_nomina WHERE idperiodo = ?");
				ps.setInt(1, idPeriodo);
				final ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					fechaDesde = texto(rs.getString("fecha_inferior"));
					fechaHasta = texto(rs.getString("fecha_superior"));
					periodoTexto = "Quincena " + idPeriodo + " (" + fechaDesde + " al " + fechaHasta + ")";
				}
				rs.close();
				ps.close();

				if (fechaDesde.length() == 10 && fechaHasta.length() == 10) {
					final LocalDate ini = LocalDate.parse(fechaDesde);
					final LocalDate fin = LocalDate.parse(fechaHasta);
					LocalDate cur = ini;
					while (!cur.isAfter(fin)) {
						if (cur.getDayOfWeek() == DayOfWeek.SUNDAY) {
							domingos.add(cur.toString());
						}
						cur = cur.plusDays(1);
					}
				}
			} else {
				// MES
				final int a = (anio >= 2020 && anio <= 2040) ? anio : Calendar.getInstance().get(Calendar.YEAR);
				final int m = (mes >= 1 && mes <= 12) ? mes : Calendar.getInstance().get(Calendar.MONTH) + 1;
				fechaDesde = String.format("%04d-%02d-01", Integer.valueOf(a), Integer.valueOf(m));
				final LocalDate ini = LocalDate.parse(fechaDesde);
				final LocalDate fin = ini.withDayOfMonth(ini.lengthOfMonth());
				fechaHasta = fin.toString();
				periodoTexto = "Mes " + m + "/" + a + " (" + fechaDesde + " al " + fechaHasta + ")";

				final ConexionBaseDatos conAux = new ConexionBaseDatos();
				final Connection cnAux = conAux.obtenerConexionBDPrincipalLocal();
				if (cnAux != null) {
					final PreparedStatement psSem = cnAux.prepareStatement(
							"SELECT fecha_fin FROM gerencia_semana WHERE anio = ? AND mes = ? ORDER BY numero");
					psSem.setInt(1, a);
					psSem.setInt(2, m);
					final ResultSet rsSem = psSem.executeQuery();
					while (rsSem.next()) {
						domingos.add(rsSem.getString("fecha_fin"));
					}
					rsSem.close();
					psSem.close();
					cerrar(cnAux);
				}
				if (domingos.isEmpty()) {
					LocalDate cur = ini;
					while (!cur.isAfter(fin)) {
						if (cur.getDayOfWeek() == DayOfWeek.SUNDAY) {
							domingos.add(cur.toString());
						}
						cur = cur.plusDays(1);
					}
				}
			}
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.guardarCargueConciliacion periodo: " + e.toString());
		} finally {
			cerrar(cnGen);
		}

		if (domingos.isEmpty()) {
			r.problemas.add("No se encontraron domingos de cierre para el periodo seleccionado.");
			return (r);
		}

		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			r.problemas.add("Sin conexion a la base principal.");
			return (r);
		}

		try {
			final String origenLimpio = (origen != null && origen.length() > 20) ? origen.substring(0, 20)
					: (origen != null && !origen.trim().isEmpty() ? origen.trim() : "EXCEL");
			final HashMap<String, EmpleadoInfo> mapaEmp = mapaEmpleadosPorIdent();
			final Parametros params = parametrosAl(cn, domingos.get(0));
			final double smlv = params.de("SMLV", 1300000);
			final double auxilioMensual = params.de("AUXILIO_TRANSPORTE", 162000);

			final PreparedStatement psCargue = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_real_cargue "
					+ " (tipo_periodo, idperiodo, periodo_texto, fecha_desde, fecha_hasta, idempleado, "
					+ "  cedula, nombre, total_pagado, horas_reales, origen, usuario) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
					+ " ON DUPLICATE KEY UPDATE "
					+ "   total_pagado = ?, horas_reales = ?, origen = ?, usuario = ?");

			final PreparedStatement psSem = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_empleado_semana "
					+ " (idempleado, semana, sueldo_basico, sueldo_variable, seguridad_social, "
					+ "  liquidacion, horas, origen, usuario) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
					+ " ON DUPLICATE KEY UPDATE "
					+ "   sueldo_basico = ?, sueldo_variable = ?, seguridad_social = ?, "
					+ "   liquidacion = ?, horas = ?, origen = ?, usuario = ?");

			final String[] lineas = partirLineas(pegado);
			final int numSemanas = domingos.size();

			for (int i = 0; i < lineas.length; i++) {
				final String linea = lineas[i].trim();
				if (linea.isEmpty()) {
					continue;
				}

				final ParsedLinea pl = parsearLineaCargue(linea, i, mapaEmp);
				if (pl == null || pl.esEncabezado) {
					continue;
				}

				if (pl.cedula.isEmpty() || pl.valor <= 0) {
					r.ilegibles++;
					r.problemas.add("Línea " + (i + 1) + ": no se identificó documento o valor válido -> " + recorte(linea));
					continue;
				}

				final EmpleadoInfo emp = mapaEmp.get(pl.cedula);
				if (emp == null) {
					r.noEncontrados++;
					r.problemas.add("Línea " + (i + 1) + ": el documento '" + pl.cedula
							+ "' no existe en el maestro de empleados.");
					continue;
				}

				final String nomReal = (pl.nombre != null && !pl.nombre.isEmpty()) ? pl.nombre : emp.nombreLargo;

				// 1. Guardar en gerencia_nomina_real_cargue
				int k = 1;
				psCargue.setString(k++, modoLimpio);
				psCargue.setInt(k++, idPeriodo);
				psCargue.setString(k++, periodoTexto);
				psCargue.setString(k++, fechaDesde);
				psCargue.setString(k++, fechaHasta);
				psCargue.setInt(k++, emp.id);
				psCargue.setString(k++, pl.cedula);
				psCargue.setString(k++, nomReal);
				psCargue.setDouble(k++, pl.valor);
				psCargue.setDouble(k++, pl.horas);
				psCargue.setString(k++, origenLimpio);
				psCargue.setString(k++, usuario);
				// UPDATE
				psCargue.setDouble(k++, pl.valor);
				psCargue.setDouble(k++, pl.horas);
				psCargue.setString(k++, origenLimpio);
				psCargue.setString(k++, usuario);
				psCargue.addBatch();

				// 2. Distribuir a gerencia_nomina_empleado_semana
				final boolean esVariableSolo = "VARIABLE".equalsIgnoreCase(tipoValor);
				for (int s = 0; s < numSemanas; s++) {
					final String sem = domingos.get(s);
					final double basicoSem = redondear(emp.salario * SEMANA_DEL_MES);
					double varSem = 0;
					if (esVariableSolo) {
						varSem = redondear(pl.valor / (double) numSemanas);
					} else {
						final double totalBasicoPeriodo = basicoSem * numSemanas;
						final double varCalc = Math.max(0, pl.valor - totalBasicoPeriodo);
						varSem = redondear(varCalc / (double) numSemanas);
					}
					final double auxSem = (emp.salario <= smlv * 2) ? redondear(auxilioMensual * SEMANA_DEL_MES) : 0;
					final double segSem = redondear((basicoSem + varSem) * 0.205);
					final double liqSem = redondear((basicoSem + varSem + auxSem) * 0.2183);
					final double horasSem = (pl.horas > 0) ? redondear(pl.horas / (double) numSemanas) : 0;

					int ks = 1;
					psSem.setInt(ks++, emp.id);
					psSem.setString(ks++, sem);
					psSem.setDouble(ks++, basicoSem);
					psSem.setDouble(ks++, varSem);
					psSem.setDouble(ks++, segSem);
					psSem.setDouble(ks++, liqSem);
					psSem.setDouble(ks++, horasSem);
					psSem.setString(ks++, origenLimpio);
					psSem.setString(ks++, usuario);
					// UPDATE
					psSem.setDouble(ks++, basicoSem);
					psSem.setDouble(ks++, varSem);
					psSem.setDouble(ks++, segSem);
					psSem.setDouble(ks++, liqSem);
					psSem.setDouble(ks++, horasSem);
					psSem.setString(ks++, origenLimpio);
					psSem.setString(ks++, usuario);
					psSem.addBatch();
				}

				r.cargados++;
				r.totalPesos += pl.valor;
				r.totalHoras += pl.horas;
			}

			psCargue.executeBatch();
			psCargue.close();

			psSem.executeBatch();
			psSem.close();

			// Medir desviaciones contra lo estimado para actualizar calibración
			for (int s = 0; s < domingos.size(); s++) {
				registrarDesviacion(domingos.get(s));
			}

		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.guardarCargueConciliacion: " + e.toString());
			r.problemas.add("Error guardando conciliacion: " + e.toString());
		} finally {
			cerrar(cn);
		}

		return (r);
	}

	public static String guardarNotaConciliacion(final int idCargue, final String observacion,
			final String estado, final String usuario) {
		if (idCargue <= 0) {
			return ("{\"respuesta\":\"INVALIDO\",\"detalle\":\"ID de registro inválido\"}");
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"Sin conexion a la base\"}");
		}
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"UPDATE gerencia_nomina_real_cargue "
					+ " SET observacion_conciliacion = ?, estado_conciliacion = ?, usuario = ? "
					+ " WHERE idcargue = ?");
			ps.setString(1, texto(observacion));
			ps.setString(2, (estado != null && !estado.isEmpty()) ? estado : "CONCILIADO");
			ps.setString(3, texto(usuario));
			ps.setInt(4, idCargue);
			final int filas = ps.executeUpdate();
			ps.close();
			if (filas > 0) {
				return ("{\"respuesta\":\"OK\",\"filas\":" + filas + "}");
			}
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"No se encontro el registro a actualizar\"}");
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.guardarNotaConciliacion: " + e.toString());
			return ("{\"respuesta\":\"NOK\",\"detalle\":\"" + e.getMessage() + "\"}");
		} finally {
			cerrar(cn);
		}
	}

	private static class ParsedLinea {
		String cedula = "";
		String nombre = "";
		double valor = 0;
		double horas = 0;
		boolean esEncabezado = false;
	}

	private static ParsedLinea parsearLineaCargue(final String linea, final int indiceLinea, final HashMap<String, EmpleadoInfo> mapaEmp) {
		if (linea == null || linea.trim().isEmpty()) {
			return (null);
		}
		final String[] tokens = partirCampos(linea);
		if (tokens.length == 0) {
			return (null);
		}

		final ParsedLinea res = new ParsedLinea();
		String candidateCedula = null;
		final StringBuilder nameBuilder = new StringBuilder();

		for (int i = 0; i < tokens.length; i++) {
			final String t = tokens[i].trim();
			if (t.isEmpty()) {
				continue;
			}

			if ("CC".equalsIgnoreCase(t) || "CE".equalsIgnoreCase(t) || "TI".equalsIgnoreCase(t)
					|| "CD".equalsIgnoreCase(t) || "PT".equalsIgnoreCase(t) || "NIT".equalsIgnoreCase(t)) {
				continue;
			}

			String numClean = t.replace(".", "").replace(",", "").replace("$", "").replace("\"", "").trim();
			if (t.contains("E") || t.contains("e")) {
				try {
					final double d = Double.parseDouble(t);
					final long l = (long) d;
					numClean = String.valueOf(l);
				} catch (final Exception ignored) {
				}
			}

			if (esSoloDigitos(numClean)) {
				try {
					final long valLong = Long.parseLong(numClean);
					if (candidateCedula == null && numClean.length() >= 6 && numClean.length() <= 11 && valLong > 100000) {
						if (mapaEmp != null && mapaEmp.containsKey(numClean)) {
							candidateCedula = numClean;
						} else if (candidateCedula == null && valLong < 2000000000L) {
							candidateCedula = numClean;
						}
					} else if (valLong >= 5000) {
						res.valor = valLong;
					} else if (valLong > 0 && valLong <= 500) {
						res.horas = valLong;
					}
				} catch (final Exception ignored) {
				}
			} else {
				try {
					final double d = Double.parseDouble(t.replace("$", "").replace(",", "."));
					if (d >= 5000) {
						res.valor = d;
					} else if (d > 0 && d <= 500) {
						res.horas = d;
					}
				} catch (final Exception ex) {
					if (!"CA".equalsIgnoreCase(t) && !"CC".equalsIgnoreCase(t) && !"AH".equalsIgnoreCase(t)) {
						if (nameBuilder.length() > 0) {
							nameBuilder.append(' ');
						}
						nameBuilder.append(t);
					}
				}
			}
		}

		if (candidateCedula == null && tokens.length >= 2) {
			final String primerCampo = tokens[0].trim().replace(".", "").replace(",", "").replace("\"", "");
			if (esSoloDigitos(primerCampo) && primerCampo.length() >= 6) {
				candidateCedula = primerCampo;
				if (tokens.length >= 3 && !esSoloDigitos(tokens[1].trim())) {
					res.valor = pesos(tokens[2]);
					if (tokens.length >= 4) {
						res.horas = pesos(tokens[3]);
					}
				} else {
					res.valor = pesos(tokens[1]);
					if (tokens.length >= 3) {
						res.horas = pesos(tokens[2]);
					}
				}
			}
		}

		res.cedula = (candidateCedula != null ? candidateCedula : "");
		res.nombre = nameBuilder.toString();
		if (indiceLinea == 0 && res.cedula.isEmpty() && res.valor == 0) {
			res.esEncabezado = true;
		}
		return (res);
	}

	private static boolean esSoloDigitos(final String s) {
		if (s == null || s.isEmpty()) {
			return (false);
		}
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i))) {
				return (false);
			}
		}
		return (true);
	}

	private static boolean esNumero(final String s) {
		if (s == null) {
			return (false);
		}
		final String t = s.trim().replace(".", "").replace(",", "").replace("$", "").replace("-", "");
		if (t.length() == 0) {
			return (false);
		}
		for (int i = 0; i < t.length(); i++) {
			if (!Character.isDigit(t.charAt(i))) {
				return (false);
			}
		}
		return (true);
	}

	private static HashSet<Integer> idsDeEmpleados() {
		final HashSet<Integer> ids = new HashSet<Integer>();
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		if (cn == null) {
			return (ids);
		}
		try {
			final PreparedStatement ps = cn.prepareStatement("SELECT id FROM empleado");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ids.add(Integer.valueOf(rs.getInt("id")));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.idsDeEmpleados: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (ids);
	}

	/** Separa por salto de linea, sirviendo para Windows y para Unix. */
	private static String[] partirLineas(final String pegado) {
		if (pegado == null) {
			return (new String[0]);
		}
		return (pegado.replace("\r\n", "\n").replace('\r', '\n').split("\n"));
	}

	/**
	 * Separa por tabulacion, punto y coma o coma, que es como sale de pegar una
	 * hoja de calculo o un CSV. Se hace a mano y no con una expresion regular
	 * para que las barras no se pierdan al editar el archivo.
	 */
	private static String[] partirCampos(final String linea) {
		final ArrayList<String> campos = new ArrayList<String>();
		final StringBuilder actual = new StringBuilder();
		for (int i = 0; i < linea.length(); i++) {
			final char c = linea.charAt(i);
			if (c == '\t' || c == ';') {
				campos.add(actual.toString());
				actual.setLength(0);
			} else {
				actual.append(c);
			}
		}
		campos.add(actual.toString());
		//Si no habia tabulaciones ni punto y coma, se intenta con coma. No se
		//intenta siempre, porque la coma tambien es separador decimal y
		//partiria un valor en dos.
		if (campos.size() < 5 && linea.indexOf(',') >= 0) {
			campos.clear();
			int desde = 0;
			for (int i = 0; i <= linea.length(); i++) {
				if (i == linea.length() || linea.charAt(i) == ',') {
					campos.add(linea.substring(desde, i));
					desde = i + 1;
				}
			}
		}
		return (campos.toArray(new String[campos.size()]));
	}

	private static int entero(final String s) {
		try {
			return (Integer.parseInt(s.trim().replace(".", "").replace("\"", "")));
		} catch (final Exception e) {
			return (0);
		}
	}

	/**
	 * Un valor en pesos como lo exporta un sistema contable: puede venir con
	 * separador de miles, con coma decimal, entre comillas o con el signo.
	 * Devuelve -1 cuando no se entiende, nunca 0: un cero se sumaria como si
	 * fuera un dato bueno.
	 */
	private static double pesos(final String crudo) {
		if (crudo == null || crudo.trim().length() == 0) {
			return (0);
		}
		String limpio = crudo.trim().replace("\"", "").replace("$", "").replace(" ", "");
		if (limpio.length() == 0) {
			return (0);
		}
		final int ultimaComa = limpio.lastIndexOf(',');
		final int ultimoPunto = limpio.lastIndexOf('.');
		if (ultimaComa >= 0 && ultimaComa > ultimoPunto) {
			limpio = limpio.replace(".", "").replace(',', '.');
		} else {
			limpio = limpio.replace(",", "");
		}
		try {
			final double v = Double.parseDouble(limpio);
			return (v < 0 ? -1 : v);
		} catch (final Exception e) {
			return (-1);
		}
	}

	private static String recorte(final String s) {
		if (s == null) {
			return ("");
		}
		return (s.length() > 60 ? s.substring(0, 60) + "..." : s);
	}

	// =======================================================================
	// Apoyo
	// =======================================================================

	/** Los turnos cerrados de un empleado, con sus horas de entrada y salida. */
	private static ArrayList<Turno> turnosDe(final Connection cnGeneral, final int idEmpleado,
			final String desde, final String hasta) {
		final ArrayList<Turno> turnos = new ArrayList<Turno>();
		try {
			final PreparedStatement ps = cnGeneral.prepareStatement(
					"SELECT tipo_evento, fecha_hora_log, idtienda FROM empleado_evento"
					+ " WHERE id = ? AND fecha >= ? AND fecha <= ?"
					+ " ORDER BY fecha_hora_log ASC");
			ps.setInt(1, idEmpleado);
			ps.setString(2, desde);
			ps.setString(3, hasta);
			final ResultSet rs = ps.executeQuery();
			Timestamp entrada = null;
			int tiendaEntrada = 0;
			while (rs.next()) {
				final String tipo = rs.getString("tipo_evento");
				final Timestamp cuando = rs.getTimestamp("fecha_hora_log");
				if (cuando == null) {
					continue;
				}
				if ("INGRESO".equalsIgnoreCase(tipo)) {
					//Dos INGRESO seguidos: al primero se le estima la salida con
					//la convencion, porque dejarlo en cero le quitaba a esa
					//persona un turno que si trabajo.
					if (entrada != null) {
						agregarEstimado(turnos, entrada, tiendaEntrada);
					}
					entrada = cuando;
					tiendaEntrada = rs.getInt("idtienda");
				} else if ("SALIDA".equalsIgnoreCase(tipo) && entrada != null) {
					final long minutos = (cuando.getTime() - entrada.getTime()) / 60000L;
					if (minutos > 0 && minutos <= MAX_MINUTOS_TURNO) {
						final Turno t = new Turno();
						t.entra = entrada;
						t.sale = cuando;
						t.idTienda = tiendaEntrada;
						turnos.add(t);
					}
					entrada = null;
				}
			}
			//El ultimo INGRESO del periodo tambien puede quedar sin su SALIDA.
			if (entrada != null) {
				agregarEstimado(turnos, entrada, tiendaEntrada);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.turnosDe " + idEmpleado + ": " + e.toString());
		}
		return (turnos);
	}

	/**
	 * Le estima la salida a un turno que quedo abierto, y lo agrega.
	 *
	 * LA CONVENCION, Y POR QUE TIENE TOPE
	 *
	 * Gerencia la definio asi: se asume que salio a la hora en que normalmente
	 * cierra el punto de venta -{@value #HORA_CIERRE_SEMANA} de domingo a
	 * jueves y {@value #HORA_CIERRE_FIN_SEMANA} viernes y sabado-, que es lo
	 * que dicen las marcaciones reales: medido sobre cuatro semanas, el pico de
	 * salidas es a las 22:00 entre semana (1.014 marcas) y a las 23:00 los
	 * viernes y sabados (470).
	 *
	 * El tope de {@value #MAX_HORAS_ESTIMADO} horas es el que hace que la
	 * convencion no se vuelva absurda. Sin el, a quien entro a las 7 de la
	 * manana y se le olvido marcar la salida se le contarian QUINCE horas, y
	 * casi todas caerian en franja nocturna, que es la que mas recarga. Con el
	 * tope sale a las 17:00. Medido: de 31 turnos abiertos en cuatro semanas,
	 * 4 tocan el tope y le quita 16 horas inventadas.
	 *
	 * Un turno estimado NUNCA genera hora extra: el tope esta por encima de la
	 * jornada ordinaria del dia pero por debajo de lo que haria falta para que
	 * un olvido se convierta en plata extra.
	 *
	 * SE MARCA, SIEMPRE
	 *
	 * {@link Turno#estimado} viaja hasta la pantalla. Un turno inventado que se
	 * vea igual que uno marcado es peor que no tenerlo: nadie va a ir a
	 * corregir el huellero si el reporte se ve completo.
	 */
	private static void agregarEstimado(final ArrayList<Turno> turnos, final Timestamp entrada,
			final int idTienda) {
		final Calendar cierre = Calendar.getInstance();
		cierre.setTimeInMillis(entrada.getTime());
		final int dia = cierre.get(Calendar.DAY_OF_WEEK);
		final boolean finDeSemana = (dia == Calendar.FRIDAY || dia == Calendar.SATURDAY);
		cierre.set(Calendar.HOUR_OF_DAY, finDeSemana ? HORA_CIERRE_FIN_SEMANA : HORA_CIERRE_SEMANA);
		cierre.set(Calendar.MINUTE, 0);
		cierre.set(Calendar.SECOND, 0);
		cierre.set(Calendar.MILLISECOND, 0);

		long minutos = (cierre.getTimeInMillis() - entrada.getTime()) / 60000L;
		//Quien entro DESPUES de la hora de cierre -el turno que cruza
		//medianoche- no puede salir antes de entrar. Ahi la convencion no
		//aplica y el turno se descarta, como antes: inventarle una duracion
		//seria peor que perderlo.
		if (minutos <= 0) {
			return;
		}
		if (minutos > MAX_HORAS_ESTIMADO * 60) {
			minutos = MAX_HORAS_ESTIMADO * 60;
		}

		final Turno t = new Turno();
		t.entra = entrada;
		t.sale = new Timestamp(entrada.getTime() + (minutos * 60000L));
		t.idTienda = idTienda;
		t.estimado = true;
		turnos.add(t);
	}

	/** Los empleados activos con salario y cargo que se calcula, más inactivos con biometría en el período. */
	private static ArrayList<Estimado> empleadosParaCalcular(final Connection cnGeneral,
			final Map<Integer, double[]> cargos, final String desdeMes, final String hastaMes) {
		final ArrayList<Estimado> lista = new ArrayList<Estimado>();
		try {
			final PreparedStatement ps = cnGeneral.prepareStatement(
					"SELECT e.id, e.nombre_largo, e.salario, e.idtipoempleado, IFNULL(e.es_sena, 0) AS es_sena, IFNULL(e.es_sena_electiva, 0) AS es_sena_electiva,"
					+ " IFNULL(e.supernumerario, 0) AS supernumerario, e.activo, e.es_empleado, IFNULL(t.descripcion,'') AS cargo"
					+ " FROM empleado e LEFT JOIN tipo_empleado t ON t.idtipoempleado = e.idtipoempleado"
					+ " WHERE e.salario > 0 AND ("
					+ "   (e.activo = 1 AND e.es_empleado = 1)"
					+ "   OR ("
					+ "     (e.activo = 0 OR e.es_empleado = 0)"
					+ "     AND EXISTS (SELECT 1 FROM empleado_evento ev WHERE ev.id = e.id AND ev.fecha >= ? AND ev.fecha <= ?)"
					+ "   )"
					+ " ) ORDER BY e.nombre_largo");
			ps.setString(1, desdeMes);
			ps.setString(2, hastaMes);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final int idTipo = rs.getInt("idtipoempleado");
				final double[] cfg = cargos.get(Integer.valueOf(idTipo));
				//calcula = 'N' en la configuracion del cargo: no se estima nunca.
				if (cfg != null && cfg[2] == 0) {
					continue;
				}
				final double sal = rs.getDouble("salario");
				//Usuarios tecnicos / no reales sin salario (Caja, BOT, Contact, etc.): descartados
				if (sal <= 0) {
					continue;
				}
				final Estimado e = new Estimado();
				e.idEmpleado = rs.getInt("id");
				e.nombre = texto(rs.getString("nombre_largo"));
				e.cargo = texto(rs.getString("cargo"));
				e.salarioBase = sal;
				e.esSena = rs.getInt("es_sena") == 1;
				e.esSenaElectiva = rs.getInt("es_sena_electiva") == 1;
				e.esSupernumerario = rs.getInt("supernumerario") == 1;
				e.esInactivo = rs.getInt("activo") == 0 || rs.getInt("es_empleado") == 0;
				e.entraTienda = cfg == null || cfg[1] == 1;
				e.idTipoEmpleado = idTipo;
				lista.add(e);
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.empleadosParaCalcular: " + e.toString());
		}
		return (lista);
	}

	/** {arl, entra_tienda, calcula} por tipo de empleado. */
	private static Map<Integer, double[]> cargosConfigurados(final Connection cn) {
		final Map<Integer, double[]> mapa = new HashMap<Integer, double[]>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idtipoempleado, arl_porcentaje, entra_tienda, calcula"
					+ " FROM gerencia_nomina_cargo");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				mapa.put(Integer.valueOf(rs.getInt("idtipoempleado")), new double[] {
						rs.getDouble("arl_porcentaje"),
						"S".equals(rs.getString("entra_tienda")) ? 1 : 0,
						"S".equals(rs.getString("calcula")) ? 1 : 0 });
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.cargosConfigurados: " + e.toString());
		}
		return (mapa);
	}

	private static Map<Integer, String> areasCargo(final Connection cn) {
		final Map<Integer, String> mapa = new HashMap<Integer, String>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idtipoempleado, IFNULL(area, '') AS area FROM gerencia_nomina_cargo");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				mapa.put(Integer.valueOf(rs.getInt("idtipoempleado")), texto(rs.getString("area")));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.areasCargo: " + e.toString());
		}
		return (mapa);
	}

	private static Map<Integer, String> excepcionesPersona(final Connection cn) {
		final Map<Integer, String> mapa = new HashMap<Integer, String>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT id, IFNULL(area, '') AS area FROM gerencia_persona_area");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				mapa.put(Integer.valueOf(rs.getInt("id")), texto(rs.getString("area")));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.excepcionesPersona: " + e.toString());
		}
		return (mapa);
	}

	/**
	 * Decide si las horas de un empleado se aproximan a ENTERAS por dia (Punto de Venta)
	 * o si CONSERVAN LOS DECIMALES (Bodega y Administrativos).
	 *
	 * Decision de Gerencia: Punto de Venta se redondea a entero por dia para evitar
	 * regateos en tienda (Math.round). Bodega (logistica/produccion) y personal
	 * administrativo marcan varias entradas y salidas (desayuno, almuerzo, etc.), por
	 * lo que sus horas SI conservan decimales.
	 */
	public static boolean esHorasEnteras(final int idEmpleado, final int idTipoEmpleado,
			final Map<Integer, String> areas, final Map<Integer, String> excepciones) {
		String area = excepciones != null ? excepciones.get(Integer.valueOf(idEmpleado)) : null;
		if (area == null || area.isEmpty()) {
			area = areas != null ? areas.get(Integer.valueOf(idTipoEmpleado)) : null;
		}
		if (area != null && !area.isEmpty()) {
			// LOGISTICA (bodega y produccion) y ADMINISTRATIVA: conservan decimales.
			if ("LOGISTICA".equalsIgnoreCase(area) || "ADMINISTRATIVA".equalsIgnoreCase(area)) {
				return (false);
			}
			// TIENDA y CONTACT: punto de venta / contact center van en enteras sin decimales.
			if ("TIENDA".equalsIgnoreCase(area) || "CONTACT".equalsIgnoreCase(area)) {
				return (true);
			}
		}
		// Respaldo por tipo de empleado: cargos de tienda y contact center en enteras (1, 2, 3, 4, 5, 7, 18).
		// Bodega (8, 9, 10, 12, 14, 33, 37) y Administrativos (6, 11, 13, 15, 16...) -> false (decimales).
		return (idTipoEmpleado == 1 || idTipoEmpleado == 2 || idTipoEmpleado == 3
				|| idTipoEmpleado == 4 || idTipoEmpleado == 5 || idTipoEmpleado == 7
				|| idTipoEmpleado == 18);
	}

	private static HashSet<String> festivosDe(final Connection cn, final String desde, final String hasta) {
		final HashSet<String> dias = new HashSet<String>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT fecha FROM gerencia_festivo WHERE fecha >= ? AND fecha <= ?");
			ps.setString(1, desde);
			ps.setString(2, hasta);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				dias.add(rs.getString("fecha"));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.festivosDe: " + e.toString());
		}
		return (dias);
	}

	/** La tienda donde mas horas marco, que es la que se usa para calibrar. */
	private static int tiendaPrincipal(final Horas h) {
		int cual = 0;
		double mejor = 0;
		for (final Map.Entry<Integer, Double> e : h.porTienda.entrySet()) {
			if (e.getValue().doubleValue() > mejor) {
				mejor = e.getValue().doubleValue();
				cual = e.getKey().intValue();
			}
		}
		return (cual);
	}

	private static void ponerNombres(final ArrayList<Estimado> lista) {
		if (lista.isEmpty()) {
			return;
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDGeneral();
		if (cn == null) {
			return;
		}
		try {
			final Map<Integer, Object[]> datos = new HashMap<Integer, Object[]>();
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT e.id, e.nombre_largo, IFNULL(t.descripcion,'') AS cargo, IFNULL(e.es_sena, 0) AS es_sena, IFNULL(e.es_sena_electiva, 0) AS es_sena_electiva,"
					+ " e.activo, e.es_empleado"
					+ " FROM empleado e LEFT JOIN tipo_empleado t ON t.idtipoempleado = e.idtipoempleado");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				datos.put(Integer.valueOf(rs.getInt("id")),
						new Object[] { texto(rs.getString("nombre_largo")), texto(rs.getString("cargo")),
								Boolean.valueOf(rs.getInt("es_sena") == 1), Boolean.valueOf(rs.getInt("es_sena_electiva") == 1),
								Boolean.valueOf(rs.getInt("activo") == 0 || rs.getInt("es_empleado") == 0) });
			}
			rs.close();
			ps.close();
			for (final Estimado e : lista) {
				final Object[] d = datos.get(Integer.valueOf(e.idEmpleado));
				if (d != null) {
					e.nombre = (String) d[0];
					e.cargo = (String) d[1];
					e.esSena = ((Boolean) d[2]).booleanValue();
					e.esSenaElectiva = ((Boolean) d[3]).booleanValue();
					e.esInactivo = ((Boolean) d[4]).booleanValue();
					if (e.esInactivo && e.costoTotal > 0) {
						e.esPosibleDespido = true;
					}
				}
			}
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.ponerNombres: " + e.toString());
		} finally {
			cerrar(cn);
		}
	}

	private static Map<Integer, String> nombresDeTienda(final Connection cn) {
		final Map<Integer, String> mapa = new HashMap<Integer, String>();
		try {
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT idtienda, nombre FROM pizzaamericana.tienda");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				mapa.put(Integer.valueOf(rs.getInt("idtienda")), texto(rs.getString("nombre")));
			}
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.nombresDeTienda: " + e.toString());
		}
		return (mapa);
	}

	private static String[] rangoMesDeSemana(final String domingo) {
		try {
			final java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("yyyy-MM-dd");
			final Calendar cal = Calendar.getInstance();
			cal.setTime(formato.parse(domingo.trim()));
			cal.add(Calendar.DAY_OF_YEAR, -3);
			final int anio = cal.get(Calendar.YEAR);
			final int mes = cal.get(Calendar.MONTH);
			final Calendar cIni = Calendar.getInstance();
			cIni.set(anio, mes, 1, 0, 0, 0);
			cIni.set(Calendar.MILLISECOND, 0);
			cIni.add(Calendar.DAY_OF_YEAR, -7);
			final Calendar cFin = Calendar.getInstance();
			cFin.set(anio, mes, cFin.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
			cFin.set(Calendar.MILLISECOND, 999);
			cFin.add(Calendar.DAY_OF_YEAR, 7);
			return (new String[] { formato.format(cIni.getTime()) + " 00:00:00",
					formato.format(cFin.getTime()) + " 23:59:59" });
		} catch (final Exception e) {
			return (null);
		}
	}

	/** Lunes a domingo, dado el domingo de cierre. */
	private static String[] rangoDeLaSemana(final String domingo) {
		try {
			final java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("yyyy-MM-dd");
			final Calendar cal = Calendar.getInstance();
			cal.setTime(formato.parse(domingo.trim()));
			if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				System.out.println("GerenciaNominaCalculoDAO: la semana " + domingo + " no cae domingo");
				return (null);
			}
			final String fin = formato.format(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, -6);
			return (new String[] { formato.format(cal.getTime()), fin });
		} catch (final Exception e) {
			return (null);
		}
	}

	private static String fecha(final Calendar c) {
		return (String.format("%04d-%02d-%02d", Integer.valueOf(c.get(Calendar.YEAR)),
				Integer.valueOf(c.get(Calendar.MONTH) + 1), Integer.valueOf(c.get(Calendar.DAY_OF_MONTH))));
	}

	private static double redondear(final double d) {
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			return (0);
		}
		return (Math.round(d * 100) / 100.0);
	}

	private static String texto(final String s) {
		return (s == null ? "" : s.trim());
	}

	private static void cerrar(final Connection cn) {
		try {
			if (cn != null) {
				cn.close();
			}
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO cerrando: " + e.toString());
		}
	}
}
