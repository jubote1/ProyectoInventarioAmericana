#!/usr/bin/perl
# Homologa los nombres de producto y de item_inventario al valor mayoritario.
#
# Se trabaja con los bytes en HEX y se escribe con UNHEX. Escribir el nombre
# como texto obligaria a que el cliente, la conexion y la columna coincidan en
# codificacion, y aqui no coinciden: la columna dice utf8 pero hay filas con
# bytes latin1 sueltos. Copiando los bytes tal cual no hay forma de corromper
# una tilde.
use strict;
use warnings;

my $dir    = shift;
my $salida = shift;
my @tiendas = qw(Manrique Bello America Calasanz Itagui LaMota Envigado Pilarica SanAntonio ManriquePiloto Niquia);

# Ids donde el nombre distinto NO es un error de escritura sino otro producto
# o una decision de operacion. No se tocan.
my %noTocar = (
    'prod_429' => 'GD Doble Online PV contra Grande DIDI: son dos productos distintos, no dos formas de escribir lo mismo.',
    'prod_453' => 'Manzana Pet 250 mL contra Pepsi Pet 250 mL en Manrique Piloto: es otro producto.',
    'prod_507' => 'Soda Saborizada Segunda 50% contra Soda Saborizada2x1 en Niquia: es otra promocion.',
    'prod_411' => 'Domicilio a $3.000 contra $2.000 en Calasanz y Niquia: es un precio, lo decide operacion.',
);

sub leer {
    my ($archivo) = @_;
    my %h;
    open(my $fh, '<', $archivo) or die "$archivo: $!";
    while (<$fh>) {
        tr/\r//d;
        chomp;
        my ($id, $hex) = split(/\t/, $_, 2);
        next unless defined $hex;
        $h{$id} = $hex;
    }
    close $fh;
    return (\%h);
}

sub deHex {
    my ($hex) = @_;
    my $t = pack('H*', $hex);
    return ($t);
}

my %sql;
my @saltados;
my $cambios = 0;

for my $caso (['prod', 'producto', 'idproducto', 'descripcion'],
              ['item', 'item_inventario', 'iditem', 'nombre_item']) {
    my ($pref, $tabla, $llave, $campo) = @{$caso};

    my %datos;
    for my $t (@tiendas) {
        $datos{$t} = leer("$dir/${pref}hex_$t.tsv");
    }

    my %ids;
    for my $t (@tiendas) {
        $ids{$_} = 1 for keys %{$datos{$t}};
    }

    for my $id (sort { $a <=> $b } keys %ids) {
        my %cuenta;
        my $presentes = 0;
        for my $t (@tiendas) {
            next unless exists $datos{$t}{$id};
            $presentes++;
            $cuenta{$datos{$t}{$id}}++;
        }
        #Un id que no esta en todas es otra cosa -falta la fila, no el nombre-.
        next if $presentes < scalar(@tiendas);
        next if scalar(keys %cuenta) == 1;

        my @ord = sort { $cuenta{$b} <=> $cuenta{$a} || $a cmp $b } keys %cuenta;
        my $top = $ord[0];

        if (exists $noTocar{"${pref}_$id"}) {
            push @saltados, sprintf("%-16s id %-6s %s", $tabla, $id, $noTocar{"${pref}_$id"});
            next;
        }
        #Empate: no hay mayoria, no se decide sola.
        if (scalar(@ord) > 1 && $cuenta{$ord[1]} == $cuenta{$top}) {
            push @saltados, sprintf("%-16s id %-6s empate, sin mayoria", $tabla, $id);
            next;
        }

        for my $t (@tiendas) {
            next if $datos{$t}{$id} eq $top;
            $cambios++;
            push @{$sql{$t}}, sprintf(
                "-- %s %s: '%s' -> '%s'   (asi lo tienen %d de 11)\nUPDATE %s SET %s = UNHEX('%s') WHERE %s = %s;",
                $tabla, $id, deHex($datos{$t}{$id}), deHex($top), $cuenta{$top},
                $tabla, $campo, $top, $llave, $id);
        }
    }
}

mkdir $salida unless -d $salida;
for my $t (@tiendas) {
    next unless $sql{$t};
    open(my $out, '>', "$salida/nombres_$t.sql") or die $!;
    print $out "-- ---------------------------------------------------------------------------\n";
    print $out "-- HOMOLOGACION DE NOMBRES - $t\n";
    print $out "--\n";
    print $out "-- Se corre en la base de ESTA tienda (tiendaamericana).\n";
    print $out "--\n";
    print $out "-- Solo cambia el TEXTO de producto.descripcion y de\n";
    print $out "-- item_inventario.nombre_item. No toca ids, ni precios, ni recetas.\n";
    print $out "--\n";
    print $out "-- El nombre se escribe con UNHEX de los bytes de la tienda mayoritaria.\n";
    print $out "-- Escribirlo como texto obligaria a que cliente, conexion y columna\n";
    print $out "-- coincidan en codificacion, y aqui no coinciden: la columna dice utf8 y\n";
    print $out "-- hay filas con bytes latin1 sueltos. Copiando los bytes no hay forma de\n";
    print $out "-- corromper una tilde.\n";
    print $out "--\n";
    print $out "-- Cambios en esta tienda: " . scalar(@{$sql{$t}}) . "\n";
    print $out "-- ---------------------------------------------------------------------------\n\n";
    print $out "START TRANSACTION;\n\n";
    print $out "$_\n\n" for @{$sql{$t}};
    print $out "COMMIT;\n";
    close $out;
    printf "%-17s %3d cambios\n", $t, scalar(@{$sql{$t}});
}
print "\nTOTAL de cambios de nombre: $cambios\n";

if (@saltados) {
    print "\nNO SE TOCAN:\n";
    print "  $_\n" for @saltados;
}
