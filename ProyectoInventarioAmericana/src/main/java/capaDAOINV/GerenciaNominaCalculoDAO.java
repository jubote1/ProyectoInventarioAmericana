package capaDAOINV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
		public double horasExtra;
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

	/** Un turno cerrado: cuando entro, cuando salio, y donde. */
	private static class Turno {
		Timestamp entra;
		Timestamp sale;
		int idTienda;
	}

	/** Las horas de una semana, ya clasificadas. */
	private static class Horas {
		double ordinarias;
		double nocturnas;
		double dominicales;
		double domNoct;
		int turnos;
		final Map<Integer, Double> porTienda = new LinkedHashMap<Integer, Double>();

		double total() {
			return (ordinarias + nocturnas + dominicales + domNoct);
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
			final ArrayList<Estimado> gente = empleadosParaCalcular(cnG, cargos);

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
					+ " horas_extra, horas_totales, turnos, sueldo_basico, sueldo_variable,"
					+ " auxilio_transporte, seguridad_social, liquidacion, costo_total,"
					+ " factor_correccion, costo_corregido, aviso)"
					+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			int cuantos = 0;
			for (final Estimado e : gente) {
				final ArrayList<Turno> turnos = turnosDe(cnG, e.idEmpleado, rango[0], rango[1]);
				final Horas h = clasificar(turnos, festivos, p);
				calcularCosto(e, h, p, cargos);

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
				ins.setDouble(i++, e.horasExtra);
				ins.setDouble(i++, e.horasTotales);
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
	private static Horas clasificar(final ArrayList<Turno> turnos, final HashSet<String> festivos,
			final Parametros p) {
		final Horas h = new Horas();
		final int nocIni = (int) p.de("HORA_NOCTURNA_INICIO", 19);
		final int nocFin = (int) p.de("HORA_NOCTURNA_FIN", 6);

		for (final Turno t : turnos) {
			h.turnos++;
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
				//El domingo y el festivo se miran sobre el dia en que CAE la
				//hora, no sobre el dia en que arranco el turno: un turno que
				//cruza de sabado a domingo tiene horas de las dos clases.
				final boolean esDomingo = cursor.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
						|| festivos.contains(fecha(cursor));

				if (esDomingo && esNoche) {
					h.domNoct += minutos / 60.0;
				} else if (esDomingo) {
					h.dominicales += minutos / 60.0;
				} else if (esNoche) {
					h.nocturnas += minutos / 60.0;
				} else {
					h.ordinarias += minutos / 60.0;
				}
				minutosTurno += minutos;
				cursor.setTimeInMillis(hasta);
			}

			final Integer tienda = Integer.valueOf(t.idTienda);
			final Double previo = h.porTienda.get(tienda);
			h.porTienda.put(tienda,
					Double.valueOf((previo == null ? 0 : previo.doubleValue()) + minutosTurno / 60.0));
		}
		return (h);
	}

	/** De las horas clasificadas y el salario, al costo de la semana. */
	private static void calcularCosto(final Estimado e, final Horas h, final Parametros p,
			final Map<Integer, double[]> cargos) {
		e.horasOrdinarias = redondear(h.ordinarias);
		e.horasNocturnas = redondear(h.nocturnas);
		e.horasDominicales = redondear(h.dominicales);
		e.horasDomNoct = redondear(h.domNoct);
		e.horasTotales = redondear(h.total());
		e.turnos = h.turnos;

		final double jornada = p.de("JORNADA_SEMANAL", 42);
		final double horasMes = p.de("HORAS_MES_LIQUIDACION", 240);
		final double vh = horasMes > 0 ? e.salarioBase / horasMes : 0;

		//El basico es del mes, no de las horas: a quien tiene salario mensual se
		//le paga igual. Las horas solo mandan en los recargos.
		e.sueldoBasico = e.salarioBase * SEMANA_DEL_MES;

		//Recargos. El nocturno va sobre toda hora nocturna, sea de domingo o no;
		//el dominical sobre toda hora de domingo o festivo, sea de noche o no.
		//Una hora de domingo en la noche lleva los dos, que es como es.
		final double recargoNocturno = (h.nocturnas + h.domNoct) * vh * p.de("RECARGO_NOCTURNO", 35) / 100.0;
		final double recargoDominical = (h.dominicales + h.domNoct) * vh * p.de("RECARGO_DOMINICAL", 90) / 100.0;

		//Extras: lo que exceda la jornada de la semana. Ver la nota 1 de la
		//clase sobre por que se cuenta por semana y no por dia.
		double recargoExtra = 0;
		if (h.total() > jornada && h.total() > 0) {
			e.horasExtra = redondear(h.total() - jornada);
			final double propNoche = (h.nocturnas + h.domNoct) / h.total();
			final double tasa = p.de("EXTRA_NOCTURNA", 75) * propNoche
					+ p.de("EXTRA_DIURNA", 25) * (1 - propNoche);
			recargoExtra = e.horasExtra * vh * tasa / 100.0;
		}
		e.sueldoVariable = redondear(recargoNocturno + recargoDominical + recargoExtra);

		//Auxilio de transporte: solo hasta el tope de salarios minimos, y no
		//entra a la base de seguridad social ni a la de vacaciones.
		final double tope = p.de("TOPE_AUX_SMLV", 2) * p.de("SMLV", 0);
		e.auxilioTransporte = (tope > 0 && e.salarioBase <= tope)
				? redondear(p.de("AUX_TRANSPORTE", 0) * SEMANA_DEL_MES) : 0;

		final double baseSalarial = e.sueldoBasico + e.sueldoVariable;

		//Seguridad social y parafiscales del empleador.
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

		//Los avisos. Un estimado con algo raro tiene que decirlo, porque quien
		//mira la lista no va a revisar a ciento cuarenta y ocho personas.
		if (h.turnos == 0) {
			e.aviso = "Sin marcaciones en la semana: va el basico sin recargos. "
					+ "Revisar si fue vacaciones, incapacidad o falta de marcacion.";
		} else if (h.total() < jornada * 0.5) {
			e.aviso = "Solo " + redondear(h.total()) + " horas marcadas de " + (int) jornada
					+ " esperadas: los recargos van incompletos.";
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
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT e.*, IFNULL((SELECT r.idtienda"
					+ "    FROM gerencia_nomina_empleado_tienda_semana r"
					+ "   WHERE r.idempleado = e.idempleado AND r.semana = e.semana"
					+ "   ORDER BY r.minutos DESC LIMIT 1), 0) AS idtienda,"
					+ " (SELECT n.origen FROM gerencia_nomina_empleado_semana n"
					+ "   WHERE n.idempleado = e.idempleado AND n.semana = e.semana) AS origen_cargado"
					+ " FROM gerencia_nomina_estimado e WHERE e.semana = ?"
					+ " ORDER BY e.costo_total DESC");
			ps.setString(1, semana);
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final Estimado e = new Estimado();
				e.idEmpleado = rs.getInt("idempleado");
				e.salarioBase = rs.getDouble("salario_base");
				e.horasOrdinarias = rs.getDouble("horas_ordinarias");
				e.horasNocturnas = rs.getDouble("horas_nocturnas");
				e.horasDominicales = rs.getDouble("horas_dominicales");
				e.horasDomNoct = rs.getDouble("horas_dom_noct");
				e.horasExtra = rs.getDouble("horas_extra");
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
		final ResultadoCarga r = new ResultadoCarga();
		if (rangoDeLaSemana(semana) == null) {
			r.problemas.add("La semana debe ser un domingo, en formato aaaa-mm-dd.");
			return (r);
		}
		final ConexionBaseDatos con = new ConexionBaseDatos();
		final Connection cn = con.obtenerConexionBDPrincipalLocal();
		if (cn == null) {
			r.problemas.add("Sin conexion a la base.");
			return (r);
		}
		try {
			final HashSet<Integer> existen = idsDeEmpleados();
			final PreparedStatement ps = cn.prepareStatement(
					"INSERT INTO gerencia_nomina_empleado_semana"
					+ " (idempleado, semana, sueldo_basico, sueldo_variable, seguridad_social,"
					+ "  liquidacion, origen, usuario)"
					+ " VALUES (?,?,?,?,?,?,?,?)"
					//Un real pisa a un estimado sin preguntar, que es justo lo
					//que se quiere cuando por fin llega la cifra de verdad.
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
				if (campos.length < 5) {
					r.ilegibles++;
					r.problemas.add("Linea " + (i + 1) + ": se esperaban 5 columnas y vinieron "
							+ campos.length + " -> " + recorte(linea));
					continue;
				}
				final int id = entero(campos[0]);
				//La primera linea suele ser el encabezado de la exportacion: si
				//el documento no es un numero, se salta sin contarla como error.
				if (id <= 0) {
					if (i == 0) {
						continue;
					}
					r.ilegibles++;
					r.problemas.add("Linea " + (i + 1) + ": el documento no es un numero -> "
							+ recorte(campos[0]));
					continue;
				}
				if (!existen.contains(Integer.valueOf(id))) {
					r.noEncontrados++;
					r.problemas.add("Linea " + (i + 1) + ": el documento " + id
							+ " no existe en el maestro de empleados. NO se cargo.");
					continue;
				}
				final double basico = pesos(campos[1]);
				final double variable = pesos(campos[2]);
				final double segSocial = pesos(campos[3]);
				final double liquidacion = pesos(campos[4]);
				if (basico < 0 || variable < 0 || segSocial < 0 || liquidacion < 0) {
					r.ilegibles++;
					r.problemas.add("Linea " + (i + 1) + ": hay un valor que no se entiende -> "
							+ recorte(linea));
					continue;
				}

				int k = 1;
				ps.setInt(k++, id);
				ps.setString(k++, semana);
				ps.setDouble(k++, basico);
				ps.setDouble(k++, variable);
				ps.setDouble(k++, segSocial);
				ps.setDouble(k++, liquidacion);
				ps.setString(k++, origen);
				ps.setString(k++, usuario);
				ps.setDouble(k++, basico);
				ps.setDouble(k++, variable);
				ps.setDouble(k++, segSocial);
				ps.setDouble(k++, liquidacion);
				ps.setString(k++, origen);
				ps.setString(k++, usuario);
				ps.addBatch();
				r.cargados++;
			}
			ps.executeBatch();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.cargarLote: " + e.toString());
			r.problemas.add("Error guardando: " + e.toString());
		} finally {
			cerrar(cn);
		}
		return (r);
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
					//Dos INGRESO seguidos: el primero queda huerfano. No se le
					//inventa un cierre, igual que en el reparto.
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
			rs.close();
			ps.close();
		} catch (final Exception e) {
			System.out.println("GerenciaNominaCalculoDAO.turnosDe " + idEmpleado + ": " + e.toString());
		}
		return (turnos);
	}

	/** Los empleados activos con salario y cargo que se calcula. */
	private static ArrayList<Estimado> empleadosParaCalcular(final Connection cnGeneral,
			final Map<Integer, double[]> cargos) {
		final ArrayList<Estimado> lista = new ArrayList<Estimado>();
		try {
			final PreparedStatement ps = cnGeneral.prepareStatement(
					"SELECT e.id, e.nombre_largo, e.salario, e.idtipoempleado,"
					+ " IFNULL(t.descripcion,'') AS cargo"
					+ " FROM empleado e LEFT JOIN tipo_empleado t ON t.idtipoempleado = e.idtipoempleado"
					+ " WHERE e.activo = 1 AND e.es_empleado = 1 ORDER BY e.nombre_largo");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				final int idTipo = rs.getInt("idtipoempleado");
				final double[] cfg = cargos.get(Integer.valueOf(idTipo));
				//calcula = 'N' en la configuracion del cargo: no se estima nunca.
				if (cfg != null && cfg[2] == 0) {
					continue;
				}
				final Estimado e = new Estimado();
				e.idEmpleado = rs.getInt("id");
				e.nombre = texto(rs.getString("nombre_largo"));
				e.cargo = texto(rs.getString("cargo"));
				e.salarioBase = rs.getDouble("salario");
				e.entraTienda = cfg == null || cfg[1] == 1;
				e.idTipoEmpleado = idTipo;
				//Sin salario no hay nada que calcular, pero el empleado tiene que
				//aparecer en la lista con su aviso: si se omitiera, nadie se
				//enteraria de que falta cargarle el salario.
				if (e.salarioBase <= 0) {
					e.aviso = "Sin salario cargado en el maestro de empleados: no se puede estimar.";
				}
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
			final Map<Integer, String[]> datos = new HashMap<Integer, String[]>();
			final PreparedStatement ps = cn.prepareStatement(
					"SELECT e.id, e.nombre_largo, IFNULL(t.descripcion,'') AS cargo"
					+ " FROM empleado e LEFT JOIN tipo_empleado t ON t.idtipoempleado = e.idtipoempleado");
			final ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				datos.put(Integer.valueOf(rs.getInt("id")),
						new String[] { texto(rs.getString("nombre_largo")), texto(rs.getString("cargo")) });
			}
			rs.close();
			ps.close();
			for (final Estimado e : lista) {
				final String[] d = datos.get(Integer.valueOf(e.idEmpleado));
				if (d != null) {
					e.nombre = d[0];
					e.cargo = d[1];
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
